package Monitor.Utils;

import java.util.Random;

public class Utils {

    public static int getRandomWithExclusion(int start, int end, int... exclude) {
        int random = start + new Random().nextInt(end - start + 1 - exclude.length);
        for (int ex : exclude) {
            if (random < ex) {
                break;
            }
            random++;
        }
        return random;
    }
    
}
