package Common.Threads;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import Common.Monitors.MFifo;

public class TClientTCP<E> extends Thread {

    private PrintWriter out;
    private final MFifo<E> fifo;

    public TClientTCP(Socket socket, MFifo<E> fifo) {
        this.fifo = fifo;
        try {
            this.out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Comunications has started on " + socket.getLocalSocketAddress().toString() + ":" + socket.getPort());
        } catch (UnknownHostException e) {

        } catch (IOException e) {

        }
    }

    public TClientTCP(Socket socket) {
        this(socket, new MFifo<>());
    }

    public void send(E element) {
        this.fifo.put(element);
    }

    @Override
    public void run() {
        E element;
        while (!(fifo.isBlocked() && fifo.isEmpty())) {
            element = fifo.pop();
            this.out.println(element);
        }
        element = null;
        this.out.println(element);
    }
    
}


