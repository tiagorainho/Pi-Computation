package Monitor.Entities;

import java.util.ArrayList;
import java.util.List;

import Common.Entities.EServiceNode;

public class EServiceNodes {

    private final List<EServiceNode> serviceNodes;
    
    public EServiceNodes() {
        this.serviceNodes = new ArrayList<EServiceNode>();
    }

    public List<EServiceNode> getNodes() {
        return this.serviceNodes;
    }

    public void put(EServiceNode element) {
        this.serviceNodes.add(element);
    }

}
