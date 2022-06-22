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
import PiComputator.GUI.ServiceGUI;

public class EServerManager extends Thread {

    private final String ComputationServiceName = "Computation";
    private final SingletonLogger logger = SingletonLogger.getInstance();
    private EServiceNode node;
    private EComputationServer server;
    private ServerSocket serverSocket;
    private ServiceGUI serviceGUI;
    private final int sendRetrials = 3;
    private final int retrialWaitPeriod = 500;
    private int serviceRegistryPort;

    public EServerManager() {
        this.serviceGUI=new ServiceGUI(this);
    }

    public void startServer(int serviceRegistryPort, int port, int maxFifoSize) throws Exception {
        this.server = new EComputationServer(maxFifoSize);
        this.serviceRegistryPort=serviceRegistryPort;

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
            if(response.getMessageType() == EMessageType.ResponseServiceRegistry) {
                this.node  = (EServiceNode) response.getMessage();
                this.logger.log(String.format("Registry received: %s", this.node));
                this.serverSocket = new ServerSocket(this.node.getPort());
            }
            serviceGUI.setTitle("Service "+node.getID());
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        
        new Thread(() -> {
            this.run();
        }).start();
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
                System.out.println("aqui");
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
                    this.logger.log(String.format("ComputationRequest: %s", this.node.toString()), EColor.GREEN);

                    EComputationPayload request = (EComputationPayload) message.getMessage();
                    request.setServerID(this.node.getID());
                    serviceGUI.addRequest(request);

                    //send error code to monitor
                    TSocket requestMonitorSocket = new TSocket(this.serviceRegistryPort);
                    requestMonitorSocket.send(new EMessage(EMessageType.ComputationRequest, new Object[]{this.node,request}));
                    requestMonitorSocket.close();

                    Integer loadBalancerPort = request.getLoadbalancerPort();

                    Double pi = this.server.computePI(request.getIteractions(), request.getDeadline());

                    // create response message based on the success of the compute PI operation
                    if(pi == null) {
                        response = new EMessage(EMessageType.ComputationRejection, request);
                        
                        this.logger.log(String.format("Sending error response to Load Balancer on port %d: %s", loadBalancerPort, response.toString()), EColor.RED);

                        request.setCode(3);
                        serviceGUI.updateRequest(request);

                        //send error code to monitor
                        TSocket errorMonitorSocket = new TSocket(this.serviceRegistryPort);
                        errorMonitorSocket.send(new EMessage(EMessageType.ComputationRejection, new Object[]{this.node,request}));
                        errorMonitorSocket.close();

                        // notify the load balancer with N retrials
                        for(int i=0; i<this.sendRetrials;i++) {
                            try {
                                TSocket lbErrorSocket = new TSocket(loadBalancerPort);
                                lbErrorSocket.send(response);
                                lbErrorSocket.close();
                                this.logger.log(String.format("Success sending Error Response to Load Balancer: %s", response),EColor.GREEN);
                                break;
                            }
                            catch(IOException e) {
                                this.logger.log(String.format("Error sending Error Response to Load Balancer: %s", response), EColor.RED);
                                try {
                                    Thread.sleep(retrialWaitPeriod);
                                } catch (InterruptedException e1) {}
                            }
                        }
                        break;
                    }

                    request.setPI(pi);
                    request.setCode(2);
                    serviceGUI.updateRequest(request);

                    response = new EMessage(EMessageType.ComputationResult, request);

                    //send error code to monitor
                    TSocket responseMonitorSocket = new TSocket(this.serviceRegistryPort);
                    responseMonitorSocket.send(new EMessage(EMessageType.ComputationResult, new Object[]{this.node,request}));
                    responseMonitorSocket.close();

                    this.logger.log(String.format("Sending success response to Load Balancer on port %d: %s", loadBalancerPort, response.toString()), EColor.GREEN);

                    // notify the load balancer with N retrials
                    for(int i=0; i<this.sendRetrials;i++) {
                        try {
                            TSocket lbResultSocket = new TSocket(loadBalancerPort);
                            lbResultSocket.send(response);
                            lbResultSocket.close();
                            this.logger.log(String.format("Success sending response to Load Balancer: %s", response),EColor.GREEN);
                            break;
                        }
                        catch(IOException e) {
                            this.logger.log(String.format("Error sending success response to Load Balancer: %s", response),EColor.RED);
                            try {
                                Thread.sleep(retrialWaitPeriod);
                            } catch (InterruptedException e1) {}
                        }
                    }
                    
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
