package LoadBalancer.Entities;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Common.Entities.EComputationPayload;
import Common.Entities.EMessage;
import Common.Entities.EMessageRegistry;
import Common.Entities.EServiceNode;
import Common.Entities.SingletonLogger;
import Common.Entities.TopologyChangePayload;
import Common.Enums.EColor;
import Common.Enums.EMessageType;
import Common.Threads.TSocket;

public class ELoadBalancerManager extends Thread {

    private ELoadBalancer loadBalancer;
    private final String address = "localhost";
    private EServiceNode node;
    private ServerSocket serverSocket;
    private int masterLoadBalancerPort;
    private final String LBserviceName = "LoadBalancer";
    private final String ComputationServiceName = "Computation";
    private final List<String> dependenciesList = List.of(LBserviceName, ComputationServiceName);
    private final Map<String, List<EServiceNode>> dependentNodesByService;
    private final Map<Integer, EComputationPayload> pendingComputations;

    private final SingletonLogger logger = SingletonLogger.getInstance();

    public ELoadBalancerManager() {
        this.dependentNodesByService = new HashMap<>();
        this.pendingComputations = new HashMap<>();
    }

    public void startLoadBalancer(int serviceRegistryPort, int weightPerNode, int masterLoadBalancerPort) throws Exception {
        
        this.masterLoadBalancerPort = masterLoadBalancerPort;

        // try to connect to service registry
        this.loadBalancer = new ELoadBalancer(weightPerNode);
        TSocket socket = null;
        try {
            socket = new TSocket(serviceRegistryPort);
        } catch (IOException e) {
            e.printStackTrace();
            this.logger.log(String.format("Error connecting to Monitor on port %d", serviceRegistryPort));
            throw new Exception(String.format("Error connecting to monitor on port %d", serviceRegistryPort));
        }

        EMessage response;

        try {
            // registry load balancer
            socket.send(
                new EMessageRegistry(LBserviceName, masterLoadBalancerPort, dependenciesList)
            );
            this.logger.log(String.format("Registry request: %s:%d -> Monitor:%d", LBserviceName, masterLoadBalancerPort, serviceRegistryPort));

            // receive response
            response = (EMessage) socket.receive();
            socket.close();
            
            // consume the response
            switch(response.getMessageType()) {
                case ResponseServiceRegistry:
                    this.node  = (EServiceNode) response.getMessage();
                    this.logger.log(String.format("Registry received: %s", this.node));
                    this.serverSocket = new ServerSocket(this.node.getPort());
                    break;
            }

            // wait until it becomes the main load balancer
            this.waitUntilMaster(serviceRegistryPort);

            // start load balancing thread
            this.run();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void waitUntilMaster(int serviceRegistryPort) {
        EMessage message=null;

        while(this.node.getPort() != masterLoadBalancerPort) {
            try {
                Socket newConn = this.serverSocket.accept();
                TSocket temporarySocket = new TSocket(newConn);

                message = temporarySocket.receive();

                switch(message.getMessageType()) {
                    case Heartbeat:
                        this.logger.log(String.format("HeartBeat Waiting: %s", this.node.toString()), EColor.GREEN);

                        temporarySocket.send(new EMessage(EMessageType.Heartbeat, null));
                    break;

                    case TopologyChange:
                        TopologyChangePayload payload = (TopologyChangePayload) message.getMessage();

                        List<EServiceNode> registeredNodes = payload.getNodes();
                        String updatedService = payload.getServiceName();

                        this.logger.log(String.format("Topology Change on %s: %s", payload.getServiceName(), payload.getNodes().toString()), EColor.RED);

                        // update the depencies state
                        this.dependentNodesByService.put(updatedService, registeredNodes);

                        // break if the updated service is not a load balancer
                        if(updatedService.equals(ComputationServiceName)) {
                            loadBalancer.updateNodes(registeredNodes);
                            break;
                        }

                        // remove inactive nodes
                        for (Iterator<EServiceNode> iterator = registeredNodes.iterator(); iterator.hasNext(); ) {
                            if(!iterator.next().isActive()) {
                                iterator.remove();
                            }
                        }

                        // get the minimum id of the list of load balancers
                        EServiceNode minNode = registeredNodes.stream().min((node1, node2) -> {
                            return node1.getID().compareTo(node2.getID());
                        }).get();
                        
                        // start work on server socket if is the node with the minimum id
                        if(minNode.getID() == this.node.getID()) {

                            // get the proposed node
                            EServiceNode proposedServiceNode = this.node.deepCopy();
                            proposedServiceNode.updatePort(this.masterLoadBalancerPort);

                            this.logger.log(String.format("Request change %s -> %s to master", minNode.toString(), proposedServiceNode.toString()), EColor.GREEN);

                            // send server registry an update on the port
                            TSocket requestUpdateSocket = new TSocket(serviceRegistryPort);
                            requestUpdateSocket.send(new EMessage(
                                EMessageType.RequestUpdateServiceRegistry,
                                proposedServiceNode
                            ));

                            // verify if update request was unsuccessfull
                            message = (EMessage) requestUpdateSocket.receive();
                            requestUpdateSocket.close();

                            // analyse response
                            if(message.getMessageType() != EMessageType.ResponseUpdateServiceRegistry) {
                                this.logger.log(String.format("Request Error"), EColor.RED);
                                break;
                            }

                            EServiceNode possiblyUpdatedNode = (EServiceNode) message.getMessage();
                            if(possiblyUpdatedNode == null) {
                                this.logger.log(String.format("Requested Node not Found"), EColor.RED);
                                break;
                            }

                            this.logger.log(String.format("Response from the master: %s", possiblyUpdatedNode.toString()));

                            if(!possiblyUpdatedNode.equals(proposedServiceNode)) {
                                this.logger.log(String.format("Request not accepted"), EColor.RED);
                                break;
                            }

                            this.logger.log(String.format("%s updated to master with success", this.LBserviceName), EColor.GREEN);

                            // update node
                            this.node = possiblyUpdatedNode;

                            // close sockets
                            this.serverSocket.close();
                            this.serverSocket = new ServerSocket(this.node.getPort());
                            this.logger.log(String.format("Master Load Balancer started on port %d", this.node.getPort()));
                        }
                    break;
                }
                temporarySocket.close();
            }
            catch(IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        // perform load balancing
        while (true) {
            try {
                Socket clientSocket = this.serverSocket.accept();
                TSocket auxSocket = new TSocket(clientSocket);
                new Thread(() -> {
                    try {
                        serveRequest(auxSocket);
                    } finally {
                        try {
                            auxSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void serveRequest(TSocket socket) {
        try {
            EMessage message = socket.receive();

            switch(message.getMessageType()) {

                case Heartbeat -> {
                    this.logger.log(String.format("HeartBeat Working: %s", this.node.toString()), EColor.GREEN);

                    socket.send(new EMessage(EMessageType.Heartbeat, null));
                }

                case TopologyChange -> {
                    TopologyChangePayload typologyPayload = (TopologyChangePayload) message.getMessage();

                    List<EServiceNode> registeredNodes = typologyPayload.getNodes();
                    String updatedService = typologyPayload.getServiceName();       
                    
                    this.logger.log(String.format("Topology Change on %s: %s", typologyPayload.getServiceName(), typologyPayload.getNodes().toString()));

                    // update the depencies state
                    this.dependentNodesByService.put(updatedService, registeredNodes);

                    // only continue if it is a topology change from a computational node
                    if(!updatedService.equals(ComputationServiceName)) break;

                    // update the load balancer state
                    this.loadBalancer.updateNodes(registeredNodes);

                    List<EServiceNode> activeComputationNodes = this.dependentNodesByService.get(this.ComputationServiceName);

                    // get active nodes IDs
                    Set<Integer> activeComputationNodesIDs = new HashSet<>();
                    for(EServiceNode node: activeComputationNodes)
                        if(node.isActive())
                            activeComputationNodesIDs.add(node.getID());
                    
                    // fetch rejected payloads by server being down
                    List<EComputationPayload> rejectedPayloads = new LinkedList<>();
                    for(EComputationPayload payload: this.pendingComputations.values()) {
                        if(activeComputationNodesIDs.contains(payload.getServerID())) continue;

                        // in case it needs another proxy
                        rejectedPayloads.add(payload);
                    }

                    if(rejectedPayloads.size() > 0)
                        this.logger.log(String.format("Computation Server: %d error%s", rejectedPayloads.size(), (rejectedPayloads.size()>1)?"s":""), EColor.RED);

                    // handle payloads which its computation server went down
                    for(EComputationPayload rejectedPayload: rejectedPayloads) {
                        
                        // if there is still active servers
                        if(this.loadBalancer.hasNext()) {
                            EServiceNode nodeToRequest = this.loadBalancer.next(rejectedPayload.getIteractions());

                            this.logger.log(String.format("Computation Server: resend %s to %s", rejectedPayload.toString(), nodeToRequest.toString()), EColor.GREEN);
    
                            // proxy the request to a server
                            TSocket newRequestSocket = new TSocket(nodeToRequest.getPort());
                            newRequestSocket.send(new EMessage(EMessageType.ComputationRequest, rejectedPayload));
                            newRequestSocket.close();
                        }
                        else {
                            this.logger.log(String.format("Computation Server: return error response: %s", rejectedPayload.toString()), EColor.RED);

                            // proxy the request to the client
                            TSocket errorSocket = new TSocket(rejectedPayload.getClientPort());
                            errorSocket.send(new EMessage(EMessageType.ComputationRejection, rejectedPayload));
                            errorSocket.close();
                        }
                    }
                }

                case ComputationRequest -> {
                    EComputationPayload computationPayload = (EComputationPayload) message.getMessage();

                    computationPayload.setLoadBalancerPort(this.node.getPort());
                
                    this.logger.log(String.format("Computation request on %s: %s", this.node.toString(), computationPayload.toString()), EColor.GREEN);

                    // check if there is an available load balancer
                    if(!this.loadBalancer.hasNext()) {
                        this.logger.log(String.format("No computation servers available"), EColor.RED);

                        TSocket errorSocket = new TSocket(computationPayload.getClientID());
                        errorSocket.send(new EMessage(EMessageType.ComputationRejection, computationPayload));
                        errorSocket.close();
                        break;
                    }

                    // get the next load balancer to be used
                    EServiceNode nodeToRequest = this.loadBalancer.next(computationPayload.getIteractions());
                    computationPayload.setServerID(nodeToRequest.getID());

                    this.logger.log(String.format("Computation request %s will be proxied to %s", computationPayload.toString(), nodeToRequest.toString()), EColor.GREEN);

                    // save the pending computation
                    this.pendingComputations.put(computationPayload.getRequestID(), computationPayload);

                    // proxy the request to a server
                    TSocket newRequestSocket = new TSocket(nodeToRequest.getPort());
                    newRequestSocket.send(new EMessage(EMessageType.ComputationRequest, computationPayload));
                    newRequestSocket.close();

                }

                case ComputationRejection -> {
                    EComputationPayload computationPayload = (EComputationPayload) message.getMessage();

                    this.logger.log(String.format("Computation Rejection on request ID: %d",computationPayload.getRequestID()), EColor.RED);

                    computationPayload.setServerID(null);
                    
                    // proxy error back to the user that the server cannot handle more computations
                    TSocket errorSocket = new TSocket(computationPayload.getClientID());
                    errorSocket.send(new EMessage(EMessageType.ComputationRejection, computationPayload));
                    errorSocket.close();

                    // remove from the pending requests
                    this.pendingComputations.remove(computationPayload.getRequestID());
                }

                case ComputationResult -> {
                    EComputationPayload computationResultPayload = (EComputationPayload) message.getMessage();

                    this.logger.log(String.format("Computation Result on request ID %d -> %s", computationResultPayload.getRequestID(), computationResultPayload.getPI().toString()), EColor.GREEN);

                    this.pendingComputations.remove(computationResultPayload.getRequestID());
                }
            }
        }
        catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }




}
