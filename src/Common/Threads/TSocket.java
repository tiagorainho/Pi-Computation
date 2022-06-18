package Common.Threads;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import Common.Entities.EMessage;
import Common.Interfaces.ISocket;
import Common.Interfaces.ISocketRunner;

public class TSocket extends Thread implements ISocket<EMessage> {

    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    static final String defaultAddress = "localhost";
    
    private ISocketRunner runnable;

    public TSocket(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }
    
    public TSocket(String address, int port, ISocketRunner runnable) throws IOException {
        this.runnable = runnable;

        this.socket = new Socket(address, port);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    public Socket getSocket() {
        return this.socket;
    }

    public TSocket(String address, int port) throws IOException {
        this(address, port, (ObjectInputStream in, ObjectOutputStream out) -> {});
    }

    public TSocket(int port) throws IOException {
        this(TSocket.defaultAddress, port, (ObjectInputStream in, ObjectOutputStream out) -> {});
    }

    public void send(EMessage payload) throws IOException {
        this.out.writeObject(payload);
    }

    public EMessage receive() throws ClassNotFoundException, IOException {
        return (EMessage) this.in.readObject();
    }

    @Override
    public void run() {
        this.runnable.run(in, out);
    }
    
}


