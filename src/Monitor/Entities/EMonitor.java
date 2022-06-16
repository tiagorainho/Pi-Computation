package Monitor.Entities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import Monitor.Interfaces.IMonitor;
import Monitor.Interfaces.IServiceDiscovery;

public class EMonitor implements IMonitor, IServiceDiscovery {

    private final EServiceDiscovery serviceDiscovery;
    private final Map<String, Set<EServiceNode>> servicesDependencies;
    private final int heartBeatWindowSize;
    private final int heartBeatPeriod;

    public EMonitor(int heartBeatWindowSize, int heartBeatPeriod, int weightPerNode) {
        this.heartBeatWindowSize = heartBeatWindowSize;
        this.heartBeatPeriod = heartBeatPeriod;
        this.serviceDiscovery = new EServiceDiscovery(weightPerNode);
        this.servicesDependencies = new HashMap<>();
    }

    @Override
    public void heartBeat(EServiceNode node) {
        int counter = 0;
        while(counter < this.heartBeatWindowSize) {
            try {
                Thread.sleep(this.heartBeatPeriod);

                // send heart beat
                // here
                throw new Exception();
                // restart counter when there is a correct response
                //counter = 0;
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

    @Override
    public EServiceNode request(String serviceName, int weight) throws NoSuchElementException {
        return this.serviceDiscovery.request(serviceName, weight);
    }
    
}
