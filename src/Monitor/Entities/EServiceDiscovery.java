package Monitor.Entities;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import Monitor.Interfaces.IServiceDiscovery;

public class EServiceDiscovery implements IServiceDiscovery {

    private final Map<String, EServiceNodes> servicesNodes;
    private final Map<Integer, EServiceNode> nodes;
    private int nodeCounter;
    private final int weightPerNode;

    public EServiceDiscovery(int weightPerNode) {
        this.weightPerNode = weightPerNode;
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
        return this.servicesNodes.getOrDefault(service, new EServiceNodes(this.weightPerNode));
    }

    /**
     * It gets a list of nodes with the same service name, gets a random port that is not already in
     * use, creates a new service node, adds the new service node to the list of nodes with the same
     * service name, and updates the list of nodes with the same service name in the services map
     * 
     * @param serviceName the name of the service to be registered
     * @return The newly created EServiceNode object.
     */
    @Override
    public EServiceNode registry(String serviceName, int port) {
        // get the list of nodes with the respective service name
        EServiceNodes serviceNodes = this.servicesNodes.getOrDefault(serviceName, new EServiceNodes(this.weightPerNode));

        // get a new port to use
        /*
        int[] exclude = new int[serviceNodes.size()];
        int i=0;
        for (Iterator<EServiceNode> it = serviceNodes.iterator(); it.hasNext(); i++) {
            exclude[i] = it.next().getPort();
        }
        int port = Utils.getRandomWithExclusion(4000, 4050, exclude);
        */

        // create a new service node
        EServiceNode newServiceNode = new EServiceNode(this.nodeCounter++, serviceName, port);

        // add new service node to the list of nodes from the same service
        serviceNodes.put(newServiceNode);

        // update the service nodes to the services map
        this.servicesNodes.put(serviceName, serviceNodes);

        // link id with the service node
        this.nodes.put(newServiceNode.getID(), newServiceNode);

        return newServiceNode;
    }

    /**
     * > The function returns the next node in the list of nodes for the service name based on the weight of the request
     * 
     * @param serviceName The name of the service.
     * @param weight The weight of the request.
     * @return A service node.
     */
    @Override
    public EServiceNode request(String serviceName, int weight) throws NoSuchElementException {

        EServiceNodes nodes = this.servicesNodes.get(serviceName);
        return nodes.next(weight);
    }
    
}
