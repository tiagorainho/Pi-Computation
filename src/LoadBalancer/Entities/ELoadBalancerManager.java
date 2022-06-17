package LoadBalancer.Entities;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Common.Entities.EMessage;
import Common.Entities.EServiceNode;
import Common.Enums.EMessageType;
import Common.Threads.TSocket;

public class ELoadBalancerManager {

    private ELoadBalancer loadBalancer;
    private final String address = "localhost";
    private EServiceNode node;
    private ServerSocket serverSocket;
    private int masterLoadBalancerPort = 200;
    private final String serviceName = "LB";
    private final List<String> dependencies = List.of(serviceName, "computation");
    public final Map<String, List<EServiceNode>> dependenciesInfo;

    public ELoadBalancerManager() {
        this.dependenciesInfo = new HashMap<>();
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
            // registry load balancer with dependencies
            socket.send(new EMessage(
                EMessageType.RegisterServiceRegistry, 
                this.serviceName + "-" + masterLoadBalancerPort)
            );

            // receive response
            response = (EMessage) socket.receive();
            socket.getSocket().close();
            
            // consume the response
            switch(response.getMessageType()) {
                case ResponseServiceRegistry:
                    this.node  = (EServiceNode) response.getMessage();
                    
                    this.serverSocket = new ServerSocket(this.node.getPort());
                    System.out.println("Load Balancer started on port " + this.node.getPort());
                    break;
            }

            // wait until it becomes the main load balancer
            while(this.node.getPort() != masterLoadBalancerPort) {

                // restart socket in the new port if closed
                if(socket.getSocket().isClosed())
                    socket = new TSocket(this.node.getPort());

                message = (EMessage) socket.receive();

                switch(response.getMessageType()) {

                    case Heartbeat:
                        response = new EMessage(EMessageType.Heartbeat, null);
                        socket.send(response);
                        break;

                    case TopologyChange:
                        // verify if pi computation


                        // verify if load balancer
                        List<EServiceNode> registeredNodes = (List<EServiceNode>) message.getMessage();

                        EServiceNode minNode = registeredNodes.stream().min((node1, node2) -> {
                            return node1.getID().compareTo(node2.getID());
                        }).get();
                        
                        // start work on server socket if is the node with the minimum id
                        if(minNode.getID() == this.node.getID()) {

                            // send server registry an update on the port
                            EServiceNode proposedServiceNode = this.node;
                            proposedServiceNode.updatePort(this.masterLoadBalancerPort);
                            socket.send(new EMessage(
                                EMessageType.RequestUpdateServiceRegistry,
                                proposedServiceNode
                            ));

                            // verify if update request was successfull


                            // close sockets
                            this.serverSocket.close();
                            this.serverSocket = new ServerSocket();
                        }
                        System.out.println("Master Load Balancer started on port " + this.node.getPort());
                        break;
                }

            }


            
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
    }


}
