package Common.Interfaces;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public interface ISocketRunner {

    public void run(ObjectInputStream in, ObjectOutputStream out);

}
