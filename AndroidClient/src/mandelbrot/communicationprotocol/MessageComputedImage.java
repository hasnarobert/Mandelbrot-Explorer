/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mandelbrot.communicationprotocol;

/**
 *
 * @author ninu
 */
public class MessageComputedImage implements Message {
    public byte[] image;

    public MessageComputedImage(byte[] image) {
        this.image = image;
    }
}
