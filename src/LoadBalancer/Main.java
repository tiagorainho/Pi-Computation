package LoadBalancer;

import LoadBalancer.Entities.ELoadBalancerManager;

public class Main {
    
    public static void main(String[] args) {
        
        ELoadBalancerManager lb = new ELoadBalancerManager();
        
        try {
            lb.startLoadBalancer(100, 2, 200);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        
    }

}

