package mandelbrot.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;

import mandelbrot.communicationprotocol.Message;
import mandelbrot.communicationprotocol.MessageClientOn;
import mandelbrot.communicationprotocol.MessageRMIServerOn;
import mandelbrot.server.rmi.MandelbrotGenerator;

/**
 * Clasa pentru serverul principal. La ea se vor conecta clientii.
 * 
 * @author ninu
 */
public class Server {
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out
					.println("Serverul nu a primit parametrii pe care ii astepta");
			System.exit(0);
		}

		ServerSocket srv_socket = null;
		try {
			srv_socket = new ServerSocket(Integer.parseInt(args[0]));
			System.out.println("Serverul a pornit");
		} catch (IOException ex) {
			System.err.println("Eroare la crearea socketului de tip server");
		}

		ImageGenerator generator = new ImageGenerator();

		while (true) {
			Socket socket = null;
			try {
				socket = srv_socket.accept();
			} catch (IOException ex) {
				System.err.println("Eroare la acceptarea unei noi conexiuni");
				continue;
			}

			ObjectOutputStream out = null;
			ObjectInputStream in = null;
			try {
				out = new ObjectOutputStream(socket.getOutputStream());
				in = new ObjectInputStream(socket.getInputStream());
			} catch (IOException ex) {
				System.err
						.println("Eroare la crearea streamurilor de comunicare");
				continue;
			}
			Message message = null;
			try {
				message = (Message) in.readObject();
			} catch (ClassNotFoundException ex) {
				System.err.println("Clientul nu a trimis ce trebuia, il ignor");
				continue;
			} catch (IOException ex) {
				System.err
						.println("Eroare la citirea din treamului clientului");
				continue;
			}

			if (message instanceof MessageRMIServerOn) {
				// s-a conectat un server RMI
				String IP = socket.getInetAddress().getHostAddress();
				MandelbrotGenerator temp = null;
				try {
					temp = (MandelbrotGenerator) Naming.lookup("//" + IP
							+ ":7777/MandelbrotRMIServer");
					// temp =
					// (MandelbrotGenerator)Naming.lookup("//localhost:7777/MandelbrotRMIServer");
				} catch (Exception ex) {
					System.err
							.println("Eroare la obtinerea referintei la distanta. "
									+ ex);
				}

				generator.addRMIServer(temp);
				try {
					// anunt serverul RMI ca l-am adaugat
					out.writeObject(message);
				} catch (IOException ex) {
					System.err
							.println("Eroare la anuntarea serverului RMI ca a fost adaugat. IO : "
									+ ex);
				}
				System.out.println("Un server RMI a fost adaugat");
			} else if (message instanceof MessageClientOn) {
				// s-a conectat un client normal
				(new Thread(new ClientService(in, out, generator))).start();
			}
		}
	}
}
