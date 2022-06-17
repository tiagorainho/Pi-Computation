package Common.Interfaces;

import java.io.IOException;
import java.net.Socket;

public interface ISocket<E> {

    void send(E element) throws IOException;

    E receive() throws ClassNotFoundException, IOException;

    Socket getSocket();
    
}
