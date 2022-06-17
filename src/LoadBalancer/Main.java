package LoadBalancer;

import LoadBalancer.Entities.ELoadBalancerManager;

public class Main {
    
    public static void main(String[] args) {
        
        ELoadBalancerManager lb = new ELoadBalancerManager();
        lb.startLoadBalancer(100, 2);
        
    }

}

