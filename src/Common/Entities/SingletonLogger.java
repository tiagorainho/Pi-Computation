package Common.Entities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    private FileWriter fileWriter;
    private final String logPath="logs/";

    private SingletonLogger(){
        File myObj=null;
        String filename=String.format("%slog%s.txt",logPath,System.currentTimeMillis());
        try{
            if (!(new File(logPath)).exists()) {
                (new File(logPath)).mkdir();
            }
            myObj = new File(filename);
            myObj.createNewFile();
            this.fileWriter= new FileWriter(filename, false);
        }catch(IOException e){
            System.out.println("An error occurred on SingletonLogger");
            e.printStackTrace();
        }
        addHook(this.fileWriter);
    }

    private static class LoadSingleton{
        static final SingletonLogger INSTANCE = new SingletonLogger();
    }    

    public static SingletonLogger getInstance(){
        return LoadSingleton.INSTANCE;
    }

    public void log(String message, EColor color) {
        System.out.println(String.format("%d - %s%s%s", System.currentTimeMillis(), (color==null) ? "" : colors.getOrDefault(color, ""), message, resetCode));
        try {
            this.fileWriter.write(String.format("%d - %s\n", System.currentTimeMillis(), message));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void log(String message) {
        this.log(message, null);
    }

    public static void addHook(FileWriter fileWriter) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }
}
