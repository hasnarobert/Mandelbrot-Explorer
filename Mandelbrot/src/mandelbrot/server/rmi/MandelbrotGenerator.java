package mandelbrot.server.rmi;

import java.awt.image.BufferedImage;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interfata pentru obiectul aflat la distanta.
 * @author ninu
 */
public interface MandelbrotGenerator extends Remote {
    /**
     * Metoda care calculeaza cat de repede diverg de multimea mandelbrot
     * punctele date ca parametrii.
     * @param left Coordonata pe axa OX a marginii din stanga a regiunii
     * @param right Coordonata pe axa OX a marginii din dreapta a regiunii
     * @param top Coordonata pe axa OY a marginii de sus a regiunii
     * @param bottom Coordonata pe axa OY a marginii de jos a regiunii
     * @param width Latimea imaginii care trebuie generata
     * @param height Inaltimea imaginii care trebuie generata
     * @param line Numarul liniei din imagina care trebuie calculata
     * @param max_iter Numarul maxim de iteratii
     * @return Un vector de intregi
     * @throws RemoteException
     */
    public byte[] getLines(double left, double right, double top, double bottom,
            int width, int height, int line_begin, int line_end, int max_iter) throws RemoteException;
}
