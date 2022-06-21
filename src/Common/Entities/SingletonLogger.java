package Common.Entities;

import java.util.Map;

import Common.Enums.EColor;

public class SingletonLogger {

    private static final Map<EColor, String> colors = Map.of(
        EColor.RESET, "\u001B[0m",
        EColor.WHITE, "\u001B[37m",
        EColor.RED, "\u001B[31m",
        EColor.GREEN, "\u001B[32m"
    );
    private static String resetCode = colors.get(EColor.RESET);

    private static class LoadSingleton{
        static final SingletonLogger INSTANCE = new SingletonLogger();
    }    

    public static SingletonLogger getInstance(){
        return LoadSingleton.INSTANCE;
    }

    public void log(String message, EColor color) {
        System.out.println(String.format("%d - %s%s%s", System.currentTimeMillis(), (color==null) ? "" : colors.getOrDefault(color, ""), message, resetCode));
    }

    public void log(String message) {
        this.log(message, null);
    }

}
