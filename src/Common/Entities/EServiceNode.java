package Common.Entities;

import java.io.Serializable;

import Common.Interfaces.IDeepCopyable;
import Common.Interfaces.IServiceNode;

public class EServiceNode implements IServiceNode, Serializable, IDeepCopyable {

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

    public Integer getPort() {
        return this.port;
    }

    public Integer getID() {
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

    @Override
    public String toString() {
        String color = this.isActive() ? "\u001B[32m" : "\u001B[31m";
        return String.format("%s[%s-%d]:%d \u001B[0m", color, this.serviceName, this.id, this.port);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        
        if(!(obj instanceof EServiceNode))
            return false;
        
        EServiceNode node = (EServiceNode) obj;

        return this.getID().equals(node.getID())
            && this.getPort().equals(node.getPort())
            && this.getServiceName().equals(node.getServiceName())
            && this.isActive() == this.isActive();
    }

}
