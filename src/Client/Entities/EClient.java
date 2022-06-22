package Client.Entities;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import Common.Entities.EComputationPayload;
import Common.Entities.EMessage;
import Common.Entities.EMessageRegistry;
import Common.Entities.EServiceNode;
import Common.Entities.SingletonLogger;
import Common.Enums.EColor;
import Common.Enums.EMessageType;
import Common.Threads.TSocket;

public class EClient extends Thread {

    private ServerSocket serverSocket;
    private final SingletonLogger logger = SingletonLogger.getInstance();
    private EServiceNode node;
    private AtomicInteger requestCounter;
    private final int numberOfRetries = 3;
    private final int retryWaitPeriod = 200;
    private final String ClientServiceName = "Client";

    public EClient(int serviceRegistryPort, int serverPort) throws Exception {
        this.requestCounter = new AtomicInteger(0);

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
                new EMessageRegistry(ClientServiceName, serverPort, List.of())
            );
            this.logger.log(String.format("Registry request: %s:%d -> Monitor:%d", ClientServiceName, serverPort, serviceRegistryPort));

            // receive response
            response = (EMessage) socket.receive();
            socket.close();
            
            // consume the response
            if(response.getMessageType() == EMessageType.ResponseServiceRegistry) {
                this.node  = (EServiceNode) response.getMessage();
                this.logger.log(String.format("Registry received: %s", this.node));
                this.serverSocket = new ServerSocket(this.node.getPort());
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        new Thread(() -> this.run()).start();
    }

    public void sendRequest(Integer loadBalancerPort, Integer interactions, Integer deadline) {

        new Thread(() -> {
            int requestID = this.node.getID() * 1000 + this.requestCounter.getAndIncrement();

            EComputationPayload payload = null;
            TSocket socket = null;

            for(int i=0;i<this.numberOfRetries;i++) {

                try {
                    payload = new EComputationPayload(this.serverSocket.getLocalPort(), 1, this.node.getID(), requestID, interactions, deadline);
                    EMessage message = new EMessage(EMessageType.ComputationRequest, payload);
    
                    this.logger.log(String.format("Sending computation payload: %s", payload.toString()));
    
                    socket = new TSocket(loadBalancerPort);
                    socket.send(message);
                    return;
                }
                catch(Exception e) {
                    i--;
                    this.logger.log(String.format("Error sending payload, retry: %d", i));
                }
                finally {
                    try {
                        socket.close();
                    } catch (IOException e) {}
                }
            }
            this.logger.log(String.format("Error sending payload, no more retries for %s", payload.toString()), EColor.RED);
        }).start();
    }

    @Override
    public void run() {
        this.logger.log(String.format("%s-%d started server on port %d", this.node.getServiceName(), this.node.getID(), this.serverSocket.getLocalPort()));

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
            EMessageType messageType = message.getMessageType();

            switch(messageType) {
                case Heartbeat -> {
                    this.logger.log(String.format("HeartBeat: %s", this.node.toString()), EColor.GREEN);
                    socket.send(new EMessage(EMessageType.Heartbeat, null));
                }

                case ComputationRejection -> {
                    EComputationPayload payload = (EComputationPayload) message.getMessage();

                    System.out.println(String.format("%s: %s", messageType.toString(), (payload==null)? "":payload.toString()));
                }

                case ComputationResult -> {
                    EComputationPayload payload = (EComputationPayload) message.getMessage();

                    System.out.println(String.format("%s: %s", messageType.toString(), (payload==null)? "":payload.toString()));
                }
            }

        }
        catch(Exception e) {
            try {
                Thread.sleep(this.retryWaitPeriod);
            } catch (InterruptedException e1) { }
            e.printStackTrace();
        }
    }
    
}
