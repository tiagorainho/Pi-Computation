package LoadBalancer.Entities;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Common.Entities.EMessage;
import Common.Entities.EMessageRegistry;
import Common.Entities.EServiceNode;
import Common.Entities.SingletonLogger;
import Common.Enums.EColor;
import Common.Enums.EMessageType;
import Common.Threads.TSocket;

public class ELoadBalancerManager extends Thread {

    private ELoadBalancer loadBalancer;
    private final String address = "localhost";
    private EServiceNode node;
    private ServerSocket serverSocket;
    private int masterLoadBalancerPort = 200;
    private final String LBserviceName = "LoadBalancer";
    private final String ComputationServiceName = "computation";
    private final List<String> dependencies = List.of(LBserviceName, ComputationServiceName);
    public final Map<String, List<EServiceNode>> dependenciesState;

    private final SingletonLogger logger = SingletonLogger.getInstance();

    public ELoadBalancerManager() {
        this.dependenciesState = new HashMap<>();
    }

    public void startLoadBalancer(int serviceRegistryPort, int weightPerNode) {
        
        // try to connect to service registry
        this.loadBalancer = new ELoadBalancer(weightPerNode);
        TSocket socket = null;
        try {
            socket = new TSocket(serviceRegistryPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

        EMessage response;

        try {
            // registry load balancer
            socket.send(
                new EMessageRegistry(LBserviceName, masterLoadBalancerPort, dependencies)
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
        EMessage message;

        // wait until it becomes the main load balancer
        while(this.node.getPort() != masterLoadBalancerPort) {
            try {
                Socket newConn = this.serverSocket.accept();
                TSocket temporarySocket = new TSocket(newConn);

                message = temporarySocket.receive();

                switch(message.getMessageType()) {
                    case Heartbeat:
                        this.logger.log(String.format("HeartBeat: Waiting"), EColor.GREEN);
                        temporarySocket.send(new EMessage(EMessageType.Heartbeat, null));
                    break;

                    case TopologyChange:
                        List<EServiceNode> registeredNodes = (List<EServiceNode>) message.getMessage();
                        if(registeredNodes.size() == 0) break;

                        for(EServiceNode a: registeredNodes) {
                            System.out.println(a.toString());
                        }

                        // update the depencies state
                        String updatedService = registeredNodes.get(0).getServiceName();
                        this.dependenciesState.put(updatedService, registeredNodes);

                        // break if the updated service is not a load balancer
                        if(!updatedService.equals(LBserviceName)) break;

                        this.logger.log(String.format("%s will update its master", LBserviceName), EColor.RED);

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

                case Heartbeat:
                    this.logger.log("HeartBeat: Working", EColor.GREEN);
                    socket.send(new EMessage(EMessageType.Heartbeat, null));
                break;

            }
        }
        catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }




}
