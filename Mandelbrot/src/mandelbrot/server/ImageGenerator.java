package mandelbrot.server;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import mandelbrot.communicationprotocol.MessageComputeImage;
import mandelbrot.communicationprotocol.MessageComputeImageLine;
import mandelbrot.server.rmi.MandelbrotGenerator;

/**
 * Clasa care gestioneaza serverele RMI si este responsabila cu apelurile
 * metodelor la distanta. Calculeaza pe rand cate o poza daca este ceruta de
 * client
 * 
 * @author ninu
 */
class ImageGenerator {
	private final ArrayList<RMIThread> generators;
	private ObjectPipe pipe;

	public ImageGenerator() {
		generators = new ArrayList<RMIThread>();
		try {
			pipe = new ObjectPipe();
		} catch (IOException ex) {
			System.err
					.println("Eroare la crearea ObjectPipe in ImageGenerator. IO + "
							+ ex);
		}
	}

	public synchronized void addRMIServer(MandelbrotGenerator generator) {
		generators.add(new RMIThread(generator, pipe));
		(new Thread(generators.get(generators.size() - 1))).start();
	}

	public synchronized byte[] computeImage(MessageComputeImage message) {
		// pun sclavii sa calculeze
		BufferedImage image = new BufferedImage(message.width, message.height,
				BufferedImage.TYPE_INT_RGB);

		if (generators.isEmpty()) {
			image.getGraphics().drawString(
					"Nici un sclav nu gandeste imaginea.", 10, 20);
		}

		for (int i = 0; i < generators.size(); ++i) {
			try {
				if (i + 1 < generators.size())
					generators
							.get(i)
							.getOutput()
							.writeObject(
									new MessageComputeImageLine(message, i
											* (message.height / generators
													.size()), (i + 1)
											* (message.height / generators
													.size())));
				else
					generators
							.get(i)
							.getOutput()
							.writeObject(
									new MessageComputeImageLine(message, i
											* (message.height / generators
													.size()), message.height));
			} catch (IOException ex) {
				System.err
						.println("Eroare la trimiterea mesajelor catre sclavi. IO : "
								+ ex);
			}
		}
		Graphics canvas = image.getGraphics();
		for (int i = 0; i < generators.size(); ++i) {
			MessageComputeImageLine computed_line = null;
			try {
				computed_line = (MessageComputeImageLine) pipe.readObject();
			} catch (Exception ex) {
				System.err
						.println("Eroare la primirea raspunsului din RMIThread. "
								+ ex);
			}
			BufferedImage computed_image = null;
			try {
				computed_image = ImageIO.read(new ByteArrayInputStream(
						computed_line.rez));
			} catch (IOException ex) {
				Logger.getLogger(ImageGenerator.class.getName()).log(
						Level.SEVERE, null, ex);
			}
			canvas.drawImage(computed_image, 0, computed_line.line_begin, null);
			try {
				ImageIO.write(computed_image, "png", new File(
						"/Users/rhasna/manimag/imageGen" + i + ".png"));
			} catch (IOException ex) {
				Logger.getLogger(ImageGenerator.class.getName()).log(
						Level.SEVERE, null, ex);
			}
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "png", baos);
		} catch (IOException ex) {
			Logger.getLogger(ImageGenerator.class.getName()).log(Level.SEVERE,
					null, ex);
		}
		return baos.toByteArray();
	}
}
