package mandelbrot.communicationprotocol;

/**
 *
 * @author ninu
 */
public class MessageComputeImageLine extends MessageComputeImage {
    public int line_begin, line_end;
    public byte[] rez;

    public MessageComputeImageLine(double left, double right, double top, double bottom,
            int width, int height, int max_iter, int line_begin, int line_end) {
        super(left, right, top, bottom, width, height, max_iter);
        this.line_begin = line_begin;
        this.line_end = line_end;
    }

    public MessageComputeImageLine(MessageComputeImage message, int line_begin, int line_end) {
        super(message.left, message.right, message.top, message.bottom, message.width, message.height, message.max_iter);
        this.line_begin = line_begin;
        this.line_end = line_end;
    }
}
