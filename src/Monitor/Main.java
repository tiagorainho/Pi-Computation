package Monitor;

import java.io.IOException;

import Monitor.Entities.EMonitor;
import Monitor.Interfaces.IMonitor;

public class Main {
    
    public static void main(String[] args) {
        // create monitor
        final int heartBeatWindowSize = 3;
        final int heartBeatPeriod = 1000;
        final int port = 5000;

        IMonitor monitor = null;
        try {
            monitor = new EMonitor(port, heartBeatWindowSize, heartBeatPeriod);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread monitorThread = (Thread) monitor;
        monitorThread.start();

    }

}
