package Client;

import java.util.Random;

import Client.Entities.EClient;

public class Main {

    public static void main(String[] args) {

        int serverPort = 800;
        int monitorPort = 100;
        int loadBalancerPort = 200;
        EClient client = null;
        try {
            client = new EClient(monitorPort, serverPort);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        for(int i=0;i<10;i++) {
            try {
                Thread.sleep(1000);

                Random r = new Random();
                int interactions = 1;//r.nextInt(1, 10);
                int deadline = r.nextInt(1, 4);

                client.sendRequest(loadBalancerPort, interactions, deadline);
            } catch (InterruptedException e) {}
        }

    }

}
