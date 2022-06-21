package LoadBalancer.Entities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Common.Entities.EServiceNode;
import Common.Interfaces.IServiceIterator;

public class ELoadBalancerBackUp implements IServiceIterator<EServiceNode> {

    
    private final Map<EServiceNode, Integer> nodesWeight;
    private final int maxWeightPerNode;
    private int nodeIdx;
    
    public ELoadBalancerBackUp(int weightPerNode) {
        this.maxWeightPerNode = weightPerNode;
        this.nodesWeight = new HashMap<>();
        this.nodeIdx = 0;
    }

    public Set<EServiceNode> getNodes() {
        return this.nodesWeight.keySet();
    }


    public void update(EServiceNode node) {
        
        // find if the node was already stored based on the node ID
        for(EServiceNode storedNode: this.getNodes()) {
            if(storedNode.getID() == node.getID()) {

                // update the node
                Integer nodeWeight = this.nodesWeight.get(storedNode);
                this.nodesWeight.remove(storedNode);
                this.nodesWeight.put(node, nodeWeight);
                return;
            }
        }

        // add node when it was not found
        this.nodesWeight.put(node, 0);
    }

    @Override
    public boolean hasNext() {
        for(EServiceNode node: this.getNodes())
            if(node.isActive())
                return true;
        return false;
    }

    @Override
    public EServiceNode next(int weight) {

        List<EServiceNode> nodes = new ArrayList<>(this.getNodes());
        nodes.sort(Comparator.comparing(EServiceNode::getID));

        EServiceNode node = nodes.get(this.nodeIdx);

        // use the current node if remains active
        if(node.isActive()) {
            int nodeWeight = this.nodesWeight.get(node);

            // check the current node load
            if(nodeWeight < this.maxWeightPerNode) {
                this.nodesWeight.put(node, nodeWeight + weight);
                return node;
            }
        }

        // loop thought all the other nodes sequentially
        for(int i=this.nodeIdx+1;i<this.nodeIdx+1+nodes.size();i++) {

            // calculate circular idx
            int idx = i % nodes.size();

            // find the first which is active
            if(nodes.get(idx).isActive()) {
                this.nodeIdx = idx;
                node = nodes.get(idx);
                this.nodesWeight.put(node, 0);
                break;
            }
        }
        return node;
    }

}
