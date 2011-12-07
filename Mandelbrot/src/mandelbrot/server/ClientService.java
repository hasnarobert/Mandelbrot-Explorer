package mandelbrot.server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.imageio.ImageIO;
import mandelbrot.communicationprotocol.Message;
import mandelbrot.communicationprotocol.MessageClientOff;
import mandelbrot.communicationprotocol.MessageClientOn;
import mandelbrot.communicationprotocol.MessageComputeImage;
import mandelbrot.communicationprotocol.MessageComputedImage;

/**
 *
 * @author ninu
 */
class ClientService implements Runnable {
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private ImageGenerator generator;

    public ClientService(ObjectInputStream in, ObjectOutputStream out, ImageGenerator generator) {
        this.in = in;
        this.out = out;
        this.generator = generator;
    }

    public void run() {
        System.out.println("S-a conectat un client");
        try {
            out.writeObject(new MessageClientOn());
        } catch (IOException ex) {
            System.err.println("Eroare la anuntarea clientului ca a fost adaugat. IO : " + ex);
        }

        while (true) {
            Message message = null;
            try {
                message = (Message)in.readObject();
            }
            catch (Exception ex) {
                System.err.println("Eroare la primirea mesajului de la client. IO : " + ex);
            }

            if (message instanceof MessageClientOff) {
                System.out.println("S-a deconectat un client");
                break;
            }
            else if (message instanceof MessageComputeImage) {
                System.out.println("Un client a cerut o imagine");
                try {
                    byte[] rez = generator.computeImage((MessageComputeImage) message);
                    System.out.println("Imaginea a fost calculata");
                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(rez));
                    out.writeObject(new MessageComputedImage(rez));
                    System.out.println("Imaginea a fost trimisa");
                } catch (IOException ex) {
                    System.err.println("Eroare la trimiterea imaginii calculata. IO : " + ex);
                }
            }
        }
    }
}
