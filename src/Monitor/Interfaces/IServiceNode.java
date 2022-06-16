package Monitor.Interfaces;

public interface IServiceNode {

    void updatePort(int port);
    void deactivate();
    void activate();

    String getServiceName();
    int getPort();
    int getID();
    
}
