package Monitor.Interfaces;

import java.util.NoSuchElementException;

import Monitor.Entities.EServiceNode;

public interface IServiceDiscovery {
    

    /**
     * * `registry` is a function that takes a `String` and returns an `EServiceNode`
     * 
     * @param serviceName The name of the service to be registered.
     * @return A reference to the registry.
     */
    EServiceNode registry(String serviceName, int port);


    /**
     * Request a service from the service pool. If the service is not available, throw an exception.
     * 
     * @param serviceName The name of the service to request.
     * @return The number of the port in which the service is running.
     */
    EServiceNode request(String serviceName, int weight) throws NoSuchElementException;
    
}
