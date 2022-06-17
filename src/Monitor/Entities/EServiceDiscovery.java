package Monitor.Entities;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import Common.Entities.EServiceNode;
import Monitor.Interfaces.IServiceDiscovery;

public class EServiceDiscovery implements IServiceDiscovery {

    private final Map<String, EServiceNodes> servicesNodes;
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

    public EServiceNodes getServiceNodesByService(String service) {
        return this.servicesNodes.getOrDefault(service, new EServiceNodes());
    }
    
    @Override
    public EServiceNode registry(String serviceName, int port) {

        // create a new service node
        EServiceNode newServiceNode = new EServiceNode(this.nodeCounter++, serviceName, port);

        // get the list of nodes with the respective service name
        EServiceNodes serviceNodes = this.servicesNodes.getOrDefault(serviceName, new EServiceNodes());

        // get a new port to use
        /*
        int[] exclude = new int[serviceNodes.size()];
        int i=0;
        for (Iterator<EServiceNode> it = serviceNodes.iterator(); it.hasNext(); i++) {
            exclude[i] = it.next().getPort();
        }
        int port = Utils.getRandomWithExclusion(4000, 4050, exclude);
        */

        // add new service node to the list of nodes from the same service
        serviceNodes.put(newServiceNode);

        // update the service nodes to the services map
        this.servicesNodes.put(serviceName, serviceNodes);

        // link id with the service node
        this.nodes.put(newServiceNode.getID(), newServiceNode);

        return newServiceNode;
    }
    
}
