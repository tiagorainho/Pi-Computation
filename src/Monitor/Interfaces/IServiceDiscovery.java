package Monitor.Interfaces;

import Common.Entities.EServiceNode;

public interface IServiceDiscovery {
    

    /**
     * * `registry` is a function that takes a `String` and returns an `EServiceNode`
     * 
     * @param serviceName The name of the service to be registered.
     * @return A reference to the registry.
     */
    EServiceNode registry(String serviceName, int port);

    EServiceNode update(EServiceNode nodeToUpdate);
    
}
