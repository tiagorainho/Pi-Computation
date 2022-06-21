package PiComputator.Entities;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import Common.Entities.EComputationPayload;
import Common.Entities.EMessage;
import Common.Entities.EMessageRegistry;
import Common.Entities.EServiceNode;
import Common.Entities.SingletonLogger;
import Common.Enums.EColor;
import Common.Enums.EMessageType;
import Common.Threads.TSocket;

public class EServerManager extends Thread {

    private final String ComputationServiceName = "Computation";
    private final SingletonLogger logger = SingletonLogger.getInstance();
    private EServiceNode node;
    private final EComputationServer server;
    private ServerSocket serverSocket;

    public EServerManager() {
        this.server = new EComputationServer();
    }

    public void startServer(int serviceRegistryPort, int port) throws Exception {

        // try to connect to service registry
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

        // handle the computations
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
            EMessage response;

            switch(message.getMessageType()) {

                case Heartbeat:
                    this.logger.log(String.format("HeartBeat: %s", this.node.toString()), EColor.GREEN);
                    socket.send(new EMessage(EMessageType.Heartbeat, null));
                break;

                case ComputationRequest:
                    this.logger.log(String.format("HeartBeat: %s", this.node.toString()), EColor.GREEN);

                    EComputationPayload request = (EComputationPayload) message.getMessage();
                    request.setServerID(this.node.getID());

                    Double pi = this.server.computePI(request.getIteractions(), request.getDeadline());

                    // create response message based on the success of the compute PI operation
                    if(pi == null) {
                        response = new EMessage(EMessageType.ComputationRejection, null);
                        
                        // notify the loadbalancer
                        this.logger.log(String.format("Sending error response to port %d: %s", socket.getPort(), response.toString()));
                        socket.send(response);
                    }

                    request.setPI(pi);
                    response = new EMessage(EMessageType.ComputationResult, request);

                    // respond to the final client
                    this.logger.log(String.format("Sending Computation Response for client on port %d: %s", request.getClientPort(), response));
                    TSocket finalClientSocket = new TSocket(request.getClientPort());
                    finalClientSocket.send(response);
                    finalClientSocket.close();
                break;
            }
        }
        catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
}