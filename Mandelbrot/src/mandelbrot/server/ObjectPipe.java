package mandelbrot.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Clasa care incapsuleaza un pipe. Scrierea este sync pentru ca vor scrie mai
 * multe threaduri
 * @author ninu
 */
class ObjectPipe {
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private PipedInputStream pipe_in;
    private PipedOutputStream pipe_out;
    private final Integer mutex_out = new Integer(0), mutex_in = new Integer(0);

    public ObjectPipe() throws IOException {
        pipe_in = new PipedInputStream();
        pipe_out = new PipedOutputStream(pipe_in);
        out = new ObjectOutputStream(pipe_out);
        in = new ObjectInputStream(pipe_in);
    }

    public void writeObject(Object obj) throws IOException {
        synchronized (mutex_out) {
            out.writeObject(obj);
        }
    }

    public Object readObject() throws IOException, ClassNotFoundException {
        synchronized(mutex_in) {
            return in.readObject();
        }
    }

    public synchronized void close() throws IOException {
        pipe_in.close();
        pipe_out.close();
    }

    public synchronized boolean available() throws IOException {
        return pipe_in.available() != 0;
    }
}
