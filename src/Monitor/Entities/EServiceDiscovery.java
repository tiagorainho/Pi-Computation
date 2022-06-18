package Monitor.Entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Common.Entities.EServiceNode;
import Monitor.Interfaces.IServiceDiscovery;
import Monitor.Utils.Utils;

public class EServiceDiscovery implements IServiceDiscovery {

    private final Map<String, List<EServiceNode>> servicesNodes;
    // for performant search
    private final Map<Integer, EServiceNode> nodes;
    private int nodeCounter;

    public EServiceDiscovery() {
        this.servicesNodes = new HashMap<>();
        this.nodes = new HashMap<>();
        this.nodeCounter = 0;
    }

    public EServiceNode getNodeByID(int id) {
        return this.nodes.get(id);
    }

    public Set<String> getServices() {
        return this.servicesNodes.keySet();
    }

    public List<EServiceNode> getServiceNodesByService(String service) {
        return this.servicesNodes.getOrDefault(service, new ArrayList<>());
    }
    
    @Override
    public EServiceNode registry(String serviceName, int port) {

        // get the list of nodes with the respective service name
        List<EServiceNode> serviceNodes = this.servicesNodes.getOrDefault(serviceName, new ArrayList<>());

        // check if the port is not available, if not then generate a new port
        if(!Utils.portIsAvailable(port)) {

            List<Integer> portsInUse = new ArrayList<>();
            
            // remove ports already in use
            Iterator<EServiceNode> it = serviceNodes.iterator();
            while(it.hasNext()) {
                portsInUse.add(it.next().getPort());
            }

            // generate random port until find one available
            do {
                port = Utils.getRandomWithExclusion(0, 10000, portsInUse);
                portsInUse.add(port);
            } while((!Utils.portIsAvailable(port)));
        }

        // create a new service node
        EServiceNode newServiceNode = new EServiceNode(this.nodeCounter++, serviceName, port);

        // add new service node to the list of nodes from the same service
        serviceNodes.add(newServiceNode);

        // update the service nodes to the services map
        this.servicesNodes.put(serviceName, serviceNodes);

        // link id with the service node
        this.nodes.put(newServiceNode.getID(), newServiceNode);

        return newServiceNode;
    }
    
}
