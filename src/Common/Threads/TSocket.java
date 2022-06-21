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
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;
    static final String defaultAddress = "localhost";
    
    private ISocketRunner runnable;

    public TSocket(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(this.socket.getOutputStream());
        this.in = new ObjectInputStream(this.socket.getInputStream());
    }
    
    public TSocket(String address, int port, ISocketRunner runnable) throws IOException {
        this(new Socket(address, port));
        this.runnable = runnable;
    }

    public Integer getPort() {
        return this.socket.getPort();
    }

    public Socket getSocket() {
        return this.socket;
    }

    public TSocket(String address, int port) throws IOException {
        this(address, port, (ObjectInputStream in, ObjectOutputStream out) -> {});
    }

    public TSocket(int port) throws IOException {
        this(TSocket.defaultAddress, port);
    }

    public void send(EMessage payload) throws IOException {
        this.out.writeObject(payload);
        this.out.flush();
    }

    public EMessage receive() throws ClassNotFoundException, IOException {
        return (EMessage) this.in.readObject();
    }

    public void close() throws IOException {
        this.out.close();
        this.in.close();
        this.socket.close();
    }

    @Override
    public void run() {
        this.runnable.run(in, out);
    }
    
}


