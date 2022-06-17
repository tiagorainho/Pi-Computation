package Monitor;

import java.io.IOException;
import java.util.List;

import Common.Entities.EServiceNode;
import Monitor.Entities.EMonitor;
import Monitor.Interfaces.IMonitor;

class Test {

    public static void main(String[] args) throws IOException {


        // create nodes
        final EServiceNode[] nodes = new EServiceNode[] {
            new EServiceNode(1, "lb", 10),
            new EServiceNode(2, "lb", 20),
            new EServiceNode(3, "lb", 30),
            new EServiceNode(4, "server", 40),
            new EServiceNode(5, "server", 50)
        };

        // create monitor
        final int heartBeatWindowSize = 3;
        final int heartBeatPeriod = 1000;

        IMonitor monitor = new EMonitor(99, heartBeatWindowSize, heartBeatPeriod);

        // test
        try {
            monitor.registry("lb", 0);
            monitor.dependencies(0, List.of("lb"));
            Thread.sleep(500);
        }
        catch(Exception e) {}


        // registry nodes
        for(EServiceNode node: nodes) {
            monitor.registry(node.getServiceName(), node.getPort());
        }

        // add dependencies
        monitor.dependencies(1, List.of("lb", "server"));
        monitor.dependencies(2, List.of());
        monitor.dependencies(3, List.of("lb"));
        monitor.dependencies(4, List.of("server"));
        // monitor.dependencies(5, List.of("lb", "server"));
        
        
        // test heartBeat
        try {
            Thread.sleep(10000);
        }
        catch(Exception e) {}


        System.out.println("\n -------------------");
        System.out.println("| Testes passados!! |");
        System.out.println(" -------------------");
    }
}


