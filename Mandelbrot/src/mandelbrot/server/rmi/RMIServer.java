package mandelbrot.server.rmi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import mandelbrot.communicationprotocol.Message;
import mandelbrot.communicationprotocol.MessageRMIServerOn;

/**
 * Al doilea tip de server.
 * Are rolul de a calcula cererile facute catre serverul principal
 * @author ninu
 */
public class RMIServer {
    /**
     *
     * @param args Se da ca parmetru adresa serverului principal si portul
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Serverul nu a primit parametrii pe care ii astepta");
            System.exit(0);
        }

        if (args[0].equalsIgnoreCase("stop")) {
            //trebuie sa opresc serverul RMI. Adica sa scot obiectul din registrii
            
            System.out.println("Serverul RMI a fost oprit cu succes");
            System.exit(0);
        }
        
        Socket socket = null;
        
        //ma conectez la serverul principal
        try {
            socket = new Socket(args[0], Integer.parseInt(args[1]));
        } catch (UnknownHostException ex) {
            System.err.println("Eroare la conectarea la server. " + ex);
            System.exit(1);
        } catch (IOException ex) {
            System.err.println("Eroare la comunicarea prin socket. " + ex);
            System.exit(1);
        }

        //instalez managerul de securitate daca el nu este deja instalat
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new RMISecurityManager());

        try {
            LocateRegistry.createRegistry(7777);
        }
        catch (RemoteException ex) {
            //inseamna ca registrul este deja creat
        }

        //creez instanta obiectului la distanta
        try {
            RMIServerService service = new RMIServerService();
            Naming.rebind("//localhost:7777/MandelbrotRMIServer", service);
        }
        catch (Exception ex) {
            System.err.println("Eroare la crearea serverului RMI. " + ex);
            try {
                socket.close();
            }
            catch (IOException ex2) {
                System.err.println("Eroare la inchiderea socketului. " + ex2);
            }
            System.exit(1);
        }

        //anunt serverul principal ca exist si ce scop am
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        }
        catch (IOException ex) {
            System.err.println("Eroare la crearea ObjectStream. " + ex);
            System.exit(1);
        }

        try {
            out.writeObject(new MessageRMIServerOn());
            Message message = (Message)in.readObject();
            if (!(message instanceof MessageRMIServerOn))
                throw new ClassNotFoundException();
        }
        catch (IOException ex) {
            System.err.println("Eroare la comunicarea cu serverul. " + ex);
            System.exit(1);
        }
        catch (ClassNotFoundException ex) {
            System.err.println("Serverul a raspuns cu un mesaj neasteptat.");
            System.exit(1);
        }

        System.out.println("Serverul RMI a fost creat cu succes si serverul principal a fost anuntat.");
    }
}
