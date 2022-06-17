package Monitor.Entities;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Common.Entities.EMessage;
import Common.Entities.EServiceNode;
import Common.Enums.EMessageType;
import Monitor.Interfaces.IMonitor;

public class EMonitor extends Thread implements IMonitor {

    private final EServiceDiscovery serviceDiscovery;
    private final Map<String, Set<EServiceNode>> servicesDependencies;
    private final int heartBeatWindowSize;
    private final int heartBeatPeriod;
    private final ServerSocket socket;

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
                    serveClient(clientSocket);
                }).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void serveClient(Socket clientSocket) {
        try {
            ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
            EMessage message = (EMessage) input.readObject();

            switch(message.getMessageType()) {

                case RegisterServiceRegistry:

                    // process incoming message
                    String req = (String) message.getMessage();
                    String parts[] =  req.split("-");
                    String serviceName = parts[0];
                    int port = Integer.parseInt(parts[1]);

                    // registry service
                    EServiceNode registriedNode = this.registry(serviceName, port);

                    // provide response
                    EMessage response = new EMessage(EMessageType.ResponseServiceRegistry, registriedNode);
                    output.writeObject(response);
                    break;
            }
        }
        catch(IOException | ClassNotFoundException e) {

        }
        
    }

    @Override
    public void heartBeat(EServiceNode node) {
        int counter = 0;
        while(counter < this.heartBeatWindowSize) {
            try {
                Thread.sleep(this.heartBeatPeriod);

                // send heart beat
                // here
                //throw new Exception();
                
                // restart counter when there is a correct response
                counter = 0;
            }
            catch(Exception e) {
                counter++;
            }
        }
        node.deactivate();
        this.topologyChange(node.getServiceName());
    }

    @Override
    public void topologyChange(String serviceName) {

        // updated service nodes
        EServiceNodes updatedNodes = this.serviceDiscovery.getServiceNodesByService(serviceName);

        // create msg to send
        String msg = "";
        for(EServiceNode node: updatedNodes.getNodes()) {
            if(node.isActive()) {
                msg += String.format("%d,%s,%d|", node.getID(), node.getServiceName(), node.getPort());
            }
        }

        // nodes to send the notification
        Set<EServiceNode> nodesToNotify = this.servicesDependencies.get(serviceName);

        // send message with all the current nodes
        for(EServiceNode node: nodesToNotify) {
            if(node.isActive()) {
                System.out.println("Node " + node.getID() + " -> " + msg);
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
    
    @Override
    public EServiceNode registry(String serviceName, int port) {
        // registry service node
        EServiceNode node = this.serviceDiscovery.registry(serviceName, port);
        
        // start heartbeat
        new Thread(() -> {
            this.heartBeat(node);
        }).start();

        return node;
    }
    
}
