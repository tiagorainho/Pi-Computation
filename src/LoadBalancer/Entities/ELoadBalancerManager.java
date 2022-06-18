package LoadBalancer.Entities;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
            socket = new TSocket(address, serviceRegistryPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

        EMessage message, response;

        try {
            // registry load balancer
            socket.send(
                new EMessageRegistry(LBserviceName, masterLoadBalancerPort, dependencies)
            );
            this.logger.log(String.format("Registry request: %s:%d -> Monitor:%d", LBserviceName, masterLoadBalancerPort, serviceRegistryPort));

            // receive response
            response = (EMessage) socket.receive();
            socket.getSocket().close();
            
            // consume the response
            switch(response.getMessageType()) {
                case ResponseServiceRegistry:
                    this.node  = (EServiceNode) response.getMessage();
                    this.logger.log(String.format("Registry received: %s", this.node));
                    this.serverSocket = new ServerSocket(this.node.getPort());
                    break;
            }

            // wait until it becomes the main load balancer
            while(this.node.getPort() != masterLoadBalancerPort) {
                TSocket temporarySocket = new TSocket(this.serverSocket.accept());

                message = (EMessage) temporarySocket.receive();

                switch(response.getMessageType()) {

                    case Heartbeat:
                        this.logger.log(String.format("HeartBeat: Waiting"), EColor.GREEN);
                        response = new EMessage(EMessageType.Heartbeat, null);
                        temporarySocket.send(response);
                    break;

                    case TopologyChange:
                        List<EServiceNode> registeredNodes = (List<EServiceNode>) message.getMessage();
                        if(registeredNodes.size() == 0) break;

                        // update the depencies state
                        String updatedService = registeredNodes.get(0).getServiceName();
                        this.dependenciesState.put(updatedService, registeredNodes);

                        // break if the updated service is not a load balancer
                        if(updatedService != LBserviceName) break;

                        // get the minimum id of the list of load balancers
                        EServiceNode minNode = registeredNodes.stream().min((node1, node2) -> {
                            return node1.getID().compareTo(node2.getID());
                        }).get();
                        
                        // start work on server socket if is the node with the minimum id
                        if(minNode.getID() == this.node.getID()) {

                            // send server registry an update on the port
                            EServiceNode proposedServiceNode = this.node;
                            proposedServiceNode.updatePort(this.masterLoadBalancerPort);
                            temporarySocket.send(new EMessage(
                                EMessageType.RequestUpdateServiceRegistry,
                                proposedServiceNode
                            ));

                            // verify if update request was unsuccessfull
                            response = (EMessage) temporarySocket.receive();
                            EServiceNode possiblyUpdatedNode = (EServiceNode) response.getMessage();
                            if(possiblyUpdatedNode.equals(proposedServiceNode)) break;

                            // update node
                            this.node = possiblyUpdatedNode;

                            // close sockets
                            this.serverSocket.close();
                            this.serverSocket = new ServerSocket(this.node.getPort());
                        }
                        System.out.println("Master Load Balancer started on port " + this.node.getPort());
                    break;
                }
                temporarySocket.getSocket().close();
            }

            // start load balancing thread
            this.run();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
    }

    @Override
    public void run() {
        // perform load balancing
        while (true) {
            try {
                Socket clientSocket = this.serverSocket.accept();
                new Thread(() -> {
                    serveRequest(clientSocket);
                }).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void serveRequest(Socket socket) {
        try {
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            EMessage message = (EMessage) input.readObject();

            switch(message.getMessageType()) {

                case Heartbeat:
                    this.logger.log("HeartBeat: Working", EColor.GREEN);
                    output.writeObject(new EMessage(EMessageType.Heartbeat, null));
                break;
                    
            }
            socket.close();
        }
        catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




}
