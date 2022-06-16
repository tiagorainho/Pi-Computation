package Monitor.Interfaces;

import java.util.List;

import Monitor.Entities.EServiceNode;

public interface IMonitor {
    
    EServiceNode registry(String serviceName, int port);

    void topologyChange(String serviceName);

    void dependencies(int id, List<String> services);

    void heartBeat(EServiceNode node);

}
