package Monitor.Entities;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Common.Entities.EServiceNode;
import Monitor.Interfaces.IServiceDiscovery;

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

    public EServiceNode update(EServiceNode nodeToUpdate) {
        List<EServiceNode> nodes = this.servicesNodes.get(nodeToUpdate.getServiceName());

        Integer portToSwitch = nodeToUpdate.getPort();
        boolean hasPortConflict = false;
        EServiceNode desiredNode = null;
        
        for (EServiceNode node: nodes) {

            // ignore inactive nodes
            if(!node.isActive()) continue;

            // break if the port to switch to is in use
            if(portToSwitch.equals(node.getPort())) {
                hasPortConflict = true;
            }

            // find the node before update by its ID
            if(node.getID().equals(nodeToUpdate.getID())) {
                desiredNode = node;
            }
        }

        // check if node was found
        if(desiredNode == null) return null;

        // check for port conflicts
        if(hasPortConflict) return desiredNode;

        // update the node with the new port number
        desiredNode.updatePort(nodeToUpdate.getPort());

        return desiredNode;
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
    public EServiceNode registry(String serviceName, Integer port) {
        int desiredPort = port;

        // get the list of nodes with the respective service name
        List<EServiceNode> serviceNodes = this.servicesNodes.getOrDefault(serviceName, new ArrayList<>());

        // check if the port is not available, if not then generate a new port
        boolean portAvailable = true;
        for(EServiceNode node: serviceNodes) {
            if(node.getPort().equals(port)) {
                portAvailable = false;
                break;
            }
        }
        if(!portAvailable) {
            // port += serviceNodes.size();
            try {
                ServerSocket s = new ServerSocket(0);
                port = s.getLocalPort();
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // create a new service node
        EServiceNode newServiceNode = new EServiceNode(this.nodeCounter++, serviceName, desiredPort, port);

        // add new service node to the list of nodes from the same service
        serviceNodes.add(newServiceNode);

        // update the service nodes to the services map
        this.servicesNodes.put(serviceName, serviceNodes);

        // link id with the service node
        this.nodes.put(newServiceNode.getID(), newServiceNode);

        return newServiceNode;
    }
    
}
