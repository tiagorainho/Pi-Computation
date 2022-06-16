package Monitor.Entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Common.Interfaces.IProducer;
import Monitor.Interfaces.IServiceIterator;

public class EServiceNodes implements IServiceIterator<EServiceNode>, IProducer<EServiceNode> {

    private final List<EServiceNode> serviceNodes;
    private final Map<EServiceNode, Integer> nodesWeight;
    private final int weightPerNode;
    private Integer nodeIdx;
    
    public EServiceNodes(int weightPerNode) {
        this.serviceNodes = new ArrayList<EServiceNode>();
        this.nodesWeight = new HashMap<>();
        this.weightPerNode = weightPerNode;
        this.nodeIdx = 0;
    }

    public List<EServiceNode> getNodes() {
        return this.serviceNodes;
    }

    @Override
    public boolean hasNext() {
        for(EServiceNode node: this.serviceNodes)
            if(node.isActive())
                return true;
        return false;
    }

    @Override
    public EServiceNode next(int weight) {
        EServiceNode node = this.serviceNodes.get(this.nodeIdx);

        // use the current node if remains active
        if(node.isActive()) {
            int nodeWeight = this.nodesWeight.get(node);

            // check the current node load
            if(nodeWeight < this.weightPerNode) {
                this.nodesWeight.put(node, nodeWeight + weight);
                return node;
            }
        }

        // loop thought all the other nodes sequentially
        for(int i=this.nodeIdx+1;i<this.nodeIdx+1+this.serviceNodes.size();i++) {

            // calculate circular idx
            int idx = i % this.serviceNodes.size();

            // find the first which is active
            if(this.serviceNodes.get(idx).isActive()) {
                this.nodeIdx = idx;
                node = this.serviceNodes.get(idx);
                this.nodesWeight.put(node, 0);
                break;
            }
        }
        return node;
    }

    @Override
    public void put(EServiceNode element) {
        this.serviceNodes.add(element);
    }

}
