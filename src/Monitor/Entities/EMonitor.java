package Monitor.Entities;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Common.Entities.EMessage;
import Common.Entities.EMessageRegistry;
import Common.Entities.EServiceNode;
import Common.Entities.SingletonLogger;
import Common.Entities.TopologyChangePayload;
import Common.Enums.EColor;
import Common.Enums.EMessageType;
import Common.Threads.TSocket;
import Monitor.Interfaces.IMonitor;

public class EMonitor extends Thread implements IMonitor {

    private final EServiceDiscovery serviceDiscovery;
    private final Map<String, Set<EServiceNode>> servicesDependencies;
    private final int heartBeatWindowSize;
    private final int heartBeatPeriod;
    private final ServerSocket socket;
    private final SingletonLogger logger = SingletonLogger.getInstance();

    public EMonitor(int port, int heartBeatWindowSize, int heartBeatPeriod) throws IOException {
        this.socket = new ServerSocket(port);
        this.heartBeatWindowSize = heartBeatWindowSize;
        this.heartBeatPeriod = heartBeatPeriod;
        this.serviceDiscovery = new EServiceDiscovery();
        this.servicesDependencies = new HashMap<>();
    }

    @Override
    public void run() {
        System.out.printf("Monitor listening on port %d\n", this.socket.getLocalPort());

        while (true) {
            try {
                Socket clientSocket = this.socket.accept();
                new Thread(() -> {
                    try {
                        serveClient(new TSocket(clientSocket));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void serveClient(TSocket clientSocket) {
        try {
            EMessage message = clientSocket.receive();

            switch(message.getMessageType()) {

                case RegisterServiceRegistry -> {

                    // process incoming message
                    EMessageRegistry registry = (EMessageRegistry) message;

                    // registry service
                    EServiceNode registriedNode = this.serviceDiscovery.registry(registry.getServiceName(), registry.getPort());

                    // save its dependencies
                    this.dependencies(registriedNode.getID(), registry.getDependencies());

                    // provide response
                    EMessage response = new EMessage(EMessageType.ResponseServiceRegistry, registriedNode);
                    clientSocket.send(response);

                    // start heartbeat                    
                    new Thread(() -> {
                        try {
                            this.heartBeat(registriedNode);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }

                case RequestUpdateServiceRegistry -> {

                    EServiceNode nodeToUpdate = (EServiceNode) message.getMessage();

                    this.logger.log(String.format("Request to Update: %s", nodeToUpdate));

                    // try to update
                    EServiceNode updatedNode = this.serviceDiscovery.update(nodeToUpdate);

                    boolean accepted = updatedNode != null;
                    this.logger.log(String.format("Update%s accepted: %s", accepted? "": " not", nodeToUpdate.toString()), accepted? EColor.GREEN: EColor.RED );

                    // send response
                    clientSocket.send(new EMessage(EMessageType.ResponseUpdateServiceRegistry, nodeToUpdate));
                }
            }
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void heartBeat(EServiceNode node) throws IOException {
        int counter = 0;
        TSocket heartBeatSocket = null;

        while(counter < this.heartBeatWindowSize) {
            try {
                Thread.sleep(this.heartBeatPeriod);

                this.logger.log(String.format("Heartbeat -> %d", node.getPort()));
                heartBeatSocket = new TSocket(node.getPort());
                
                //// heartBeatSocket.getSocket().setSoTimeout(1000);

                // send heart beat
                heartBeatSocket.send(new EMessage(EMessageType.Heartbeat, null));

                // check response
                EMessage response = (EMessage) heartBeatSocket.receive();
                if(response.getMessageType() == EMessageType.Heartbeat) {
                    // restart counter when there is a correct response
                    counter = 0;
                }
            }
            catch(InterruptedException e) { }
            catch(IOException | ClassNotFoundException e) {
                counter++;
                this.logger.log(String.format("Service %s -> %d", node.toString(), counter));
            }
        }
        node.deactivate();
        this.logger.log(String.format("Service %s deactivated", node.toString()));
        this.topologyChange(node.getServiceName());
    }

    @Override
    public void topologyChange(String serviceName) {

        this.logger.log(String.format("Topology change in the service: %s", serviceName), EColor.RED);

        // updated service nodes
        List<EServiceNode> updatedNodes = this.serviceDiscovery.getServiceNodesByService(serviceName);

        //List<EServiceNode> updatedNodes = new ArrayList<>(serviceNodes.size());

        // create msg to send
        EMessage message = new EMessage(EMessageType.TopologyChange, new TopologyChangePayload(serviceName, updatedNodes));

        // nodes to send the notification
        Set<EServiceNode> nodesToNotify = this.servicesDependencies.getOrDefault(serviceName, new HashSet<>());

        // send message with all the current nodes
        for(EServiceNode nodeToNotify: nodesToNotify) {
            if(!nodeToNotify.isActive()) continue;
            try {
                TSocket auxSocket = new TSocket(new Socket("localhost", nodeToNotify.getPort()));
                auxSocket.send(message);
                this.logger.log(String.format("Sent Topology change to node %s", nodeToNotify.toString()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
    }

    @Override
    public void dependencies(int id, List<String> services) {
        
        // get node based on id
        EServiceNode node = this.serviceDiscovery.getNodeByID(id);
        
        // save on the dependencies
        for(String serviceName: services) {
            Set<EServiceNode> nodes = this.servicesDependencies.getOrDefault(serviceName, new HashSet<EServiceNode>());
            
            if(nodes.add(node)) {
                this.servicesDependencies.put(serviceName, nodes);
            }
        }
    }
    
}
