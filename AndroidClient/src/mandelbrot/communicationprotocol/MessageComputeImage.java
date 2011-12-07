package mandelbrot.communicationprotocol;

/**
 *
 * @author ninu
 */
public class MessageComputeImage implements Message {
    public double left, right, top, bottom;
    public int width, height, max_iter;

    public MessageComputeImage(double left, double right, double top, double bottom,
            int width, int height, int max_iter) {
        this.bottom = bottom;
        this.height = height;
        this.left = left;
        this.max_iter = max_iter;
        this.right = right;
        this.top = top;
        this.width = width;
    }
}
