package PiComputator.Entities;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import Common.Entities.EMessage;
import Common.Entities.EMessageRegistry;
import Common.Entities.EServiceNode;
import Common.Entities.SingletonLogger;
import Common.Enums.EColor;
import Common.Enums.EMessageType;
import Common.Threads.TSocket;

public class EServerManager extends Thread {

    private final String address = "localhost";
    private final String ComputationServiceName = "computation";
    private EServer server;
    private final SingletonLogger logger = SingletonLogger.getInstance();
    private EServiceNode node;
    private ServerSocket serverSocket;

    public EServerManager() {

    }

    public void startServer(int serviceRegistryPort, int port) throws Exception {

        // try to connect to service registry
        this.server = new EServer();
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
                new EMessageRegistry(ComputationServiceName, port, List.of())
            );
            this.logger.log(String.format("Registry request: %s:%d -> Monitor:%d", ComputationServiceName, port, serviceRegistryPort));

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
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        
        this.run();
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
                    this.logger.log("HeartBeat", EColor.GREEN);
                    socket.send(new EMessage(EMessageType.Heartbeat, null));
                break;


            }
        }
        catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
}
