package Common.Entities;

import java.io.Serializable;
import java.util.Map;

import LoadBalancer.Entities.ELoadBalancer;

public class SyncRequestsPayload implements Serializable {

    private final Map<Integer, EComputationPayload> pendingRequests;
    private final ELoadBalancer loadBalancer;
    
    public SyncRequestsPayload(Map<Integer, EComputationPayload> pendingRequests, ELoadBalancer loadBalancer) {
        this.pendingRequests = pendingRequests;
        this.loadBalancer = loadBalancer;
    }

    public Map<Integer, EComputationPayload> getPendingRequests() {
        return this.pendingRequests;
    }

    public ELoadBalancer getLoadBalancer() {
        return this.loadBalancer;
    }
    
}
