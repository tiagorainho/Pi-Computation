package LoadBalancer.Entities;

import java.util.Comparator;
import java.util.List;

import Common.Entities.EComputationPayload;
import Common.Entities.EServiceNode;
import Common.Interfaces.IServiceIterator;

public class ELoadBalancer {

    private List<EServiceNode> nodes;
    
    public ELoadBalancer() {

    }

    public List<EServiceNode> getNodes() {
        return this.nodes;
    }

    public void updateNodes(List<EServiceNode> nodes) {
        this.nodes = nodes;
        for(EServiceNode node: nodes) {
            if(!this.nodes.contains(node))
                node.set("weight", 0);
        }
    }

    public boolean hasNext() {
        for(EServiceNode node: this.nodes)
            if(node.isActive())
                return true;
        return false;
    }

    public EServiceNode load(EComputationPayload computationPayload) {
        
        // find the node with the least amount of weight
        EServiceNode minNode = this.nodes.stream().min(Comparator.comparingInt((node) -> (int) node.get("weight"))).get();

        // add weight
        minNode.set("weight", (int) minNode.get("weight") + computationPayload.getIteractions());

        return minNode;
    }

    public void aliviate(EComputationPayload computationPayload) {
        Integer nodeID = computationPayload.getServerID();
        int weight = computationPayload.getIteractions();
        for(EServiceNode node: this.nodes) {
            if(node.getID().equals(nodeID)) {
                int newWeight = (int) node.get("weight") - weight;
                node.set("weight", newWeight);
            }
        }
    }

}
