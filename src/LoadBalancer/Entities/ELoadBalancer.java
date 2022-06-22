package LoadBalancer.Entities;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import Common.Entities.EComputationPayload;
import Common.Entities.EServiceNode;

public class ELoadBalancer implements Serializable {

    private List<EServiceNode> nodes;
    
    public ELoadBalancer() {

    }

    public List<EServiceNode> getNodes() {
        return this.nodes;
    }

    public void updateNodes(List<EServiceNode> nodes) {
        this.nodes = nodes;
    }

    public boolean hasNext() {
        for(EServiceNode node: this.nodes)
            if(node.isActive())
                return true;
        return false;
    }

    public EServiceNode load(EComputationPayload computationPayload) {
        
        // find the node with the least amount of weight
        EServiceNode minNode = this.nodes.stream().min(Comparator.comparingInt((node) -> node.getWeight())).get();

        // add weight
        minNode.setWeight(minNode.getWeight()+computationPayload.getIteractions());

        return minNode;
    }

    public void aliviate(EComputationPayload computationPayload) {
        Integer nodeID = computationPayload.getServerID();
        int weight = computationPayload.getIteractions();
        for(EServiceNode node: this.nodes) {
            if(node.getID().equals(nodeID)) {
                int newWeight = node.getWeight() - weight;
                node.setWeight(newWeight);
            }
        }
    }

}
