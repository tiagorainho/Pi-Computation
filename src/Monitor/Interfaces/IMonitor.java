package Monitor.Interfaces;

import java.io.IOException;
import java.util.List;

import Common.Entities.EServiceNode;

public interface IMonitor {

    void topologyChange(String serviceName);

    void dependencies(int id, List<String> services);

    void heartBeat(EServiceNode node) throws IOException;

}
