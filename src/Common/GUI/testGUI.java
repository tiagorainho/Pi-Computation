package Common.GUI;

import Client.GUI.ClientGUI;
import Common.Entities.EMessage;
import Common.Entities.EServiceNode;
import LoadBalancer.GUI.LoadBalancerGUI;
import Monitor.GUI.MonitorGUI;
import PiComputator.GUI.ServiceGUI;

public class testGUI {
    public static void main(String[] args) {
    
        /*
        ServiceGUI s = new ServiceGUI(0);
        MonitorGUI m = new MonitorGUI(null);
        LoadBalancerGUI l = new LoadBalancerGUI(0);
        m.addService(new EServiceNode(0, "LoadBalancer", 0, 0));
        */
        ClientGUI c = new ClientGUI(0);
    }
}
