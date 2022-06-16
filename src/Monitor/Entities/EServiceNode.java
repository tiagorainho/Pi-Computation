package Monitor.Entities;

import Monitor.Interfaces.IServiceNode;

public class EServiceNode implements IServiceNode {

    private final int id;
    private final String serviceName;
    private int port;
    private boolean active;

    public EServiceNode(int id, String serviceName, int port) {
        this.id = id;
        this.serviceName = serviceName;
        this.port = port;
        this.active = true;
    }

    public void updatePort(int port) {
        this.port = port;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public int getPort() {
        return this.port;
    }

    public int getID() {
        return this.id;
    }

    public boolean isActive() {
        return this.active;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }
    
}
