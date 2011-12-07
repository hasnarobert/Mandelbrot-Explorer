/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mandelbrot.client;

import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import mandelbrot.communicationprotocol.MessageClientOff;
import mandelbrot.communicationprotocol.MessageClientOn;
import mandelbrot.communicationprotocol.MessageComputeImage;
import mandelbrot.communicationprotocol.MessageComputedImage;

/**
 *
 * @author ninu
 */
class Client extends JFrame implements ActionListener {
    private MandelbrotImage image = null;
    private Socket sock;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private double zoom = 1.5, X = -0.5, Y = 0;
    private JTextField IP, Port;

    private void getImage() {
        MessageComputedImage message = null;
        BufferedImage img = null;
        try {
            out.writeObject(new MessageComputeImage(X - zoom, X + zoom, Y + zoom*0.75, Y - zoom*0.75, 800, 600, 1000));
            message = (MessageComputedImage)in.readObject();
            img = ImageIO.read(new ByteArrayInputStream(message.image));
        }
        catch (Exception ex) {
            System.err.println("Eroare la comunicarea cu serverul : " + ex);
            System.exit(1);
        }
        setImage(img);
    }

    public Client(String nume) {
        super(nume);
        setSize(800, 665);
        setLayout(null);
        image = new MandelbrotImage();
        image.setLocation(0, 0);
        add(image);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (sock == null) System.exit(0);
                try {
                    out.writeObject(new MessageClientOff());
                    sock.close();
                } catch (IOException ex) {
                    System.err.println("Eroare la inchiderea conexiunii : " + ex);
                }
                System.exit(0);
            }
        });


        Panel panel = new Panel(new FlowLayout());
        panel.setSize(800, 65);
        panel.setLocation(0, 600);
        add(panel);
        IP = new JTextField("IP", 10);
        Port = new JTextField("Port", 4);
        JButton connect = new JButton("Conectare");
        JButton stanga = new JButton("<");
        JButton dreapta = new JButton(">");
        JButton sus = new JButton("^");
        JButton jos = new JButton("v");
        JButton plus = new JButton("+");
        JButton minus = new JButton("-");

        connect.addActionListener(this);
        stanga.addActionListener(this);
        dreapta.addActionListener(this);
        sus.addActionListener(this);
        jos.addActionListener(this);
        plus.addActionListener(this);
        minus.addActionListener(this);


        panel.add(IP);
        panel.add(new JLabel(":"));
        panel.add(Port);
        panel.add(connect);
        panel.add(new JLabel("                   "));
        panel.add(stanga);
        panel.add(dreapta);
        panel.add(sus);
        panel.add(jos);
        panel.add(plus);
        panel.add(minus);
    }

    public void setImage(BufferedImage image) {
        this.image.setImage(image);
    }

    public void actionPerformed(ActionEvent e) {
        String actiune = ((JButton)e.getSource()).getText();
        if (actiune.equalsIgnoreCase("Conectare")) {
            try {
                sock = new Socket(IP.getText(), Integer.parseInt(Port.getText()));
            }
            catch (IOException ex) {
                System.err.println("Eroare la conectarea la server. IO : " + ex);
            }

            try {
                out = new ObjectOutputStream(sock.getOutputStream());
                in = new ObjectInputStream(sock.getInputStream());
            }
            catch (IOException ex) {
                System.err.println("Eroare la crearea canalelor de comunicatie cu serverul. IO : " + ex);
            }

            //comunicarea cu serverul sa stie ce fel de client sunt
            try {
                out.writeObject(new MessageClientOn());
                try {
                    if (!(in.readObject() instanceof MessageClientOn)) {
                        throw new Exception("Nu a trimis MessageClientOn");
                    }
                }
                catch (Exception ex) {
                    System.err.println("Eroare la primirea mesajului de la server cu ce tip de client m-a adaugat. " + ex);
                }
            }
            catch (IOException ex) {
                System.err.println("Eroare la anuntarea serverului ce tip de client sunt. IO : "  +ex);
            }

            ((JButton)e.getSource()).setText("Deconectare");

        } else if (actiune.equalsIgnoreCase("Deconectare")){
            try {
                out.writeObject(new MessageClientOff());
                sock.close();
            } catch (IOException ex) {
                System.err.println("Eroare la inchiderea conexiunii : " + ex);
            }
            ((JButton)e.getSource()).setText("Conectare");

        } else if (actiune.equalsIgnoreCase("<")) {
            X += zoom/3;
        } else if (actiune.equalsIgnoreCase(">")) {
            X -= zoom/3;
        } else if (actiune.equalsIgnoreCase("^")) {
            Y += zoom/3;
        } else if (actiune.equalsIgnoreCase("v")) {
            Y -= zoom/3;
        } else if (actiune.equalsIgnoreCase("+")) {
            zoom /= 1.5;
        } else if (actiune.equalsIgnoreCase("-")) {
            zoom *= 1.5;
        }
        getImage();
    }

    public static void main(String[] args) {
        Client gui = new Client("Mandelbrot Client");
        gui.setVisible(true);
    }
}




class MandelbrotImage extends JPanel {
    private BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);

    public MandelbrotImage() {
        setSize(800, 600);
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        g.drawImage(image, 0, 0, null);
    }

}
