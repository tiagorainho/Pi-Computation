package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import Common.Entities.EComputationPayload;
import Common.Entities.EMessage;
import Common.Enums.EMessageType;
import Common.Threads.TSocket;

public class Main {

    public static void main(String[] args) {

        int serverPort = 600;

        try {
            Thread.sleep(1000);
            ServerSocket serverSocket = new ServerSocket(serverPort);
            new Thread(() -> { run(serverSocket); }).start();

            for(int i=20;i<50;i++) {
                Thread.sleep(500);
                System.out.println("Enviado computation payload");
                try {
                    TSocket s = new TSocket(200);
                    EComputationPayload p = new EComputationPayload(serverPort, 1, 1, 1000+i, 2, 1);
                    EMessage m = new EMessage(EMessageType.ComputationRequest, p);
                    s.send(m);
                    s.close();
                }
                catch(Exception e) {
                    System.err.println("Voltou p tras");
                    i--;
                }
                
            }
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    public static void run(ServerSocket serverSocket) {
        System.out.println("Aberto o server socket");
        while(true) {
            try {
                Socket newConn = serverSocket.accept();
                TSocket temporarySocket = new TSocket(newConn);
                
                EMessage message = temporarySocket.receive();

                EMessageType messageType = message.getMessageType();

                EComputationPayload payload = (EComputationPayload) message.getMessage();
                System.out.println(String.format("%s: %s", messageType.toString(), payload.toString()));

            }
            catch(Exception e) {

            }
        }
    }

}
