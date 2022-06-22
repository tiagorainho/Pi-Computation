package Monitor;

import java.io.IOException;

import Monitor.Entities.EMonitor;

public class Main {
    
    public static void main(String[] args) {
        // create monitor
        try {
            EMonitor m = new EMonitor();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
