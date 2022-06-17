package LoadBalancer.Entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Common.Entities.EServiceNode;
import Common.Interfaces.IProducer;
import Common.Interfaces.IServiceIterator;

public class ELoadBalancer implements IServiceIterator<EServiceNode>, IProducer<EServiceNode> {

    private final List<EServiceNode> nodes;
    private final Map<EServiceNode, Integer> nodesWeight;
    private final int maxWeightPerNode;
    private int nodeIdx;
    
    public ELoadBalancer(int weightPerNode) {
        this.maxWeightPerNode = weightPerNode;
        this.nodes = new ArrayList<>();
        this.nodesWeight = new HashMap<>();
        this.nodeIdx = 0;
    }

    public Set<EServiceNode> getNodes() {
        return this.nodesWeight.keySet();
    }

    @Override
    public void put(EServiceNode newNode) {
        if(this.nodesWeight.containsKey(newNode)) {
            this.nodes.add(newNode);
            this.nodesWeight.put(newNode, 0);
        }
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
        EServiceNode node = this.nodes.get(this.nodeIdx);

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
        for(int i=this.nodeIdx+1;i<this.nodeIdx+1+this.nodes.size();i++) {

            // calculate circular idx
            int idx = i % this.nodes.size();

            // find the first which is active
            if(this.nodes.get(idx).isActive()) {
                this.nodeIdx = idx;
                node = this.nodes.get(idx);
                this.nodesWeight.put(node, 0);
                break;
            }
        }
        return node;
    }

}
