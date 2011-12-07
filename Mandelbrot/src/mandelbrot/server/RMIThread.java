package mandelbrot.server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import mandelbrot.communicationprotocol.Message;
import mandelbrot.communicationprotocol.MessageComputeImageLine;
import mandelbrot.communicationprotocol.MessageRMIServerOff;
import mandelbrot.server.rmi.MandelbrotGenerator;

/**
 *
 * @author ninu
 */
class RMIThread implements Runnable {
    private MandelbrotGenerator generator;
    private ObjectPipe out, in;

    /**
     * Constructor
     * @param generator referinta catre obiectul la distanta care va efectua calculele
     * @param pipe ObjectPipe la care se va scrie rezultatul
     */
    public RMIThread(MandelbrotGenerator generator, ObjectPipe pipe_out) {
        this.generator = generator;
        try {
            this.in = new ObjectPipe();
        }
        catch (IOException ex) {
            System.err.println("Eroare la crearea ObjectPipe in RMIThread. IO + " + ex);
        }
        this.out = pipe_out;
    }

    public ObjectPipe getOutput() { return in; }

    public void run() {
        while (true) {
            Message message = null;
            try {
                message = (Message)in.readObject();
            } catch (Exception ex) {
                try {
                    //System.err.println("RMIThred Exception : " + ex);
                    Thread.sleep(10);
                } catch (InterruptedException ex1) {
                    Logger.getLogger(RMIThread.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }

            //verific ce trebuie sa fac
            if (message instanceof MessageRMIServerOff) {
                break;
            }
            else if (message instanceof MessageComputeImageLine) {
                //tre sa calculez o linie dintr-o poza
                MessageComputeImageLine msg = (MessageComputeImageLine)message;
                try {
                    msg.rez = generator.getLines(msg.left, msg.right, msg.top, msg.bottom, msg.width, msg.height, msg.line_begin, msg.line_end, msg.max_iter);
                }
                catch (RemoteException ex) {
                    System.err.println("RemoteException in RMIThread : " + ex);
                    break;
                }

                //acum trimit mesajul cu rezultatul inapoi
                try {
                    //BufferedImage image = ImageIO.read(new ByteArrayInputStream(msg.rez));
                    //ImageIO.write(image, "png", new File("/home/ninu/rmithread.png"));
                    out.writeObject(msg);
                }
                catch (IOException ex) {
                    System.err.println("Eroare la scrierea in pipe. IO + " + ex);
                    break;
                }
            }
        }
    }
}
