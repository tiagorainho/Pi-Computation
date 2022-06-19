package Common.Entities;

import java.io.Serializable;
import java.util.List;

public class TopologyChangePayload implements Serializable {

    private final List<EServiceNode> nodes;
    private final String serviceName;
    
    public TopologyChangePayload(String serviceName, List<EServiceNode> nodes) {
        this.nodes = nodes;
        this.serviceName = serviceName;
    }

    public List<EServiceNode> getNodes() {
        return this.nodes;
    }

    public String getServiceName() {
        return this.serviceName;
    }


}
