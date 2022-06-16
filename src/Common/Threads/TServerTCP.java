package Common.Threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;

import Common.Monitors.MFifo;

public class TServerTCP<E> extends Thread {
    
    private Socket socket = null;
    private final MFifo<E> fifo;

    /**
     * Constructor to instantiate a TMultiServer object
     * @param socket Client Server socket
     * @param control SimulationControl interface
     */
    public TServerTCP(Socket socket, MFifo<E> fifo) {
        super("MultiServerThread");
        this.socket = socket;
        this.fifo = fifo;
    }

    public TServerTCP(Socket socket) {
        this(socket, new MFifo<E>());
    }

     
    public void run() {
        E element;
        try {
            // PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            while (!(element = (E) in.readObject()).equals("null")) {
                this.fifo.put(element);
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            this.fifo.blockPuts();
        }
    }

}
