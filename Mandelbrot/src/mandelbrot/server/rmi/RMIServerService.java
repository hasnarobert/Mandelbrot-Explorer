package mandelbrot.server.rmi;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Clasa care efectueaza calculul. Este conceputa pentru a fi apelata prin RMI
 * @author ninu
 */
public class RMIServerService extends UnicastRemoteObject implements MandelbrotGenerator {
    
    public RMIServerService() throws RemoteException {
    
    }

    public byte[] getLines(double left, double right, double top, double bottom,
            int width, int height, int line_begin, int line_end, int max_iter) throws RemoteException {
        System.out.println("Mi s-a cerut o imagine");
        double alfa1 = (right - left) / width;
        double beta1 = left;

        double alfa2 = (top - bottom)/ height;
        double beta2 = bottom;

        double X, Y;
        BufferedImage image = new BufferedImage(width, line_end - line_begin, BufferedImage.TYPE_INT_RGB);
        Graphics canvas = image.getGraphics();

        for (int i = 0; i < width; ++i) {
            for (int j = line_begin; j < line_end; ++j) {
                canvas.setColor(getColor(getDivergenceSpeed(alfa1*i + beta1,alfa2*j + beta2, max_iter), max_iter));
                canvas.drawLine(i, j - line_begin, i, j - line_begin);
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
        } catch (IOException ex) {
            Logger.getLogger(RMIServerService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return baos.toByteArray();
    }

    private int getDivergenceSpeed(double x, double y, int max_iter) {
        int iter = 0;
        double X = 0, Y = 0;

        for (;X*X + Y*Y <= 4 && iter < max_iter; ++iter) {
            double temp = X*X - Y*Y + x;
            Y = 2*X*Y + y;
            X = temp;
        }
        return iter;
    }

    private Color getColor(int divergence, int max_iter) {
        //return new Color( divergence /** 16777215 / divergence*/ );
        return new Color( (int)1677.7215*divergence );
    }
}
