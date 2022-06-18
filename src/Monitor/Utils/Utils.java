package Monitor.Utils;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Random;

public class Utils {

    public static int getRandomWithExclusion(int start, int end, List<Integer> exclude) {
        int random = start + new Random().nextInt(end - start + 1 - exclude.size());
        for (int ex : exclude) {
            if (random < ex) {
                break;
            }
            random++;
        }
        return random;
    }

    public static boolean portIsAvailable(int port) {
        try (Socket ignored = new Socket("localhost", port)) {
            return false;
        } catch (IOException ignored) {
            return true;
        }
    }
    
}
