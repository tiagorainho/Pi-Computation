package LoadBalancer.Entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Common.Entities.EServiceNode;
import Common.Interfaces.IServiceIterator;

public class ELoadBalancer implements IServiceIterator<EServiceNode> {

    private List<EServiceNode> nodes;
    private final Map<Integer, Integer> nodesWeight;
    private final int maxWeightPerNode;
    private int nodeIdx;
    
    public ELoadBalancer(int weightPerNode) {
        this.nodes = new ArrayList<>();
        this.maxWeightPerNode = weightPerNode;
        this.nodesWeight = new HashMap<>();
        this.nodeIdx = 0;
    }

    public List<EServiceNode> getNodes() {
        return this.nodes;
    }

    public void updateNodes(List<EServiceNode> nodes) {
        this.nodes = nodes;
    }

    @Override
    public boolean hasNext() {
        for(EServiceNode node: this.nodes)
            if(node.isActive())
                return true;
        return false;
    }

    @Override
    public EServiceNode next(int weight) {

        EServiceNode node = this.nodes.get(this.nodeIdx);

        // use the current node if remains active
        if(node.isActive()) {
            int nodeWeight = this.nodesWeight.get(node.getID());

            // check the current node load
            if(nodeWeight < this.maxWeightPerNode) {
                this.nodesWeight.put(node.getID(), nodeWeight + weight);
                return node;
            }
        }

        // loop thought all the other nodes sequentially
        for(int i=this.nodeIdx+1;i<this.nodeIdx+1+this.nodes.size();i++) {

            // calculate circular idx
            int idx = i % this.nodes.size();

            // find the first which is active
            if(nodes.get(idx).isActive()) {
                this.nodeIdx = idx;
                node = this.nodes.get(idx);
                this.nodesWeight.put(node.getID(), 0);
                break;
            }
        }
        return node;
    }

}
