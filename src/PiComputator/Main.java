package PiComputator;

import PiComputator.Entities.EServerManager;

public class Main {

    public static void main(String[] args) {
        
        EServerManager server = new EServerManager();
        
        try {
            server.startServer(100, 300, 2);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        
    }
    
}
