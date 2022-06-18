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
import Common.Entities.EMessageRegistry;
import Common.Entities.EServiceNode;
import Common.Enums.EMessageType;
import Common.Threads.TSocket;
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

                case RegisterServiceRegistry -> {

                    // process incoming message
                    EMessageRegistry registry = (EMessageRegistry) message;

                    // registry service
                    EServiceNode registriedNode = this.serviceDiscovery.registry(registry.getServiceName(), registry.getPort());

                    // provide response
                    EMessage response = new EMessage(EMessageType.ResponseServiceRegistry, registriedNode);
                    output.writeObject(response);

                    // start heartbeat
                    new Thread(() -> {
                        this.heartBeat(registriedNode);
                    }).start();
                }
            }
        }
        catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void heartBeat(EServiceNode node) {
        int counter = 0;
        while(counter < this.heartBeatWindowSize) {
            TSocket heartBeatSocket = null;
            try {
                Thread.sleep(this.heartBeatPeriod);

                // send heart beat
                heartBeatSocket = new TSocket(node.getPort());
                //// heartBeatSocket.getSocket().setSoTimeout(1000);

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
                System.out.println(String.format("Service %s -> %d", node.toString(), counter));
            }
        }
        node.deactivate();
        System.out.println(String.format("Service %s deactivated", node.toString()));
        this.topologyChange(node.getServiceName());
    }

    @Override
    public void topologyChange(String serviceName) {

        // updated service nodes
        List<EServiceNode> updatedNodes = this.serviceDiscovery.getServiceNodesByService(serviceName);

        // create msg to send
        String msg = "";
        for(EServiceNode node: updatedNodes) {
            if(node.isActive()) {
                msg += String.format("%d,%s,%d|", node.getID(), node.getServiceName(), node.getPort());
            }
        }

        // nodes to send the notification
        Set<EServiceNode> nodesToNotify = this.servicesDependencies.getOrDefault(serviceName, new HashSet<>());

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
    
}
