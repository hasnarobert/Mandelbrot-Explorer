package ninu.mandelbrotclient;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import mandelbrot.communicationprotocol.MessageClientOff;
import mandelbrot.communicationprotocol.MessageClientOn;
import mandelbrot.communicationprotocol.MessageComputeImage;
import mandelbrot.communicationprotocol.MessageComputedImage;

/**
 *
 * @author ninu
 */
public class MandelbrotClient extends Activity {

    private double zoom = 1.5, X = -0.5, Y = 0;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket sock;

    /**
     * Metoda care afiseaza EROARE cu rosu pe ecran.
     */
    private void error(String mesaj) {
        TextView tv = new TextView(this);
        tv.setText(mesaj);
        setContentView(tv);
    }

    /**
     * Metoda care cere serverului imagine si apoi o afiseaza
     */
    private void getImage() {
        MessageComputedImage message = null;
        try {
            out.writeObject(new MessageComputeImage(X - zoom, X + zoom, Y + zoom, Y - zoom, 300, 300, 1000));
            message = (MessageComputedImage)in.readObject();
        }
        catch (Exception ex) {
            error("Eroare in getImage : " + ex);
            return;
        }
        Bitmap bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(message.image));
        ((ImageView)findViewById(R.id.image)).setImageBitmap(bitmap);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.login);

        findViewById(R.id.ok).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                String IP = ((EditText)findViewById(R.id.IP)).getText().toString();
                int Port = Integer.parseInt(((EditText)findViewById(R.id.Port)).getText().toString());

                setContentView(R.layout.main);

                try {
                    sock = new Socket(IP, Port);
                    out = new ObjectOutputStream(sock.getOutputStream());
                    in = new ObjectInputStream(sock.getInputStream());

                    out.writeObject(new MessageClientOn());

                    if (!(in.readObject() instanceof MessageClientOn)) 
                            throw new Exception("Serverul nu a raspuns corect");

                    getImage();

                }
                catch (Exception ex) {
                    error("Eroare la initiere : " + ex);
                }
                
                findViewById(R.id.plus).setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        zoom /= 1.5;
                        getImage();
                    }
                });

                findViewById(R.id.minus).setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        zoom *= 1.5;
                        getImage();
                    }
                });

                findViewById(R.id.dreapta).setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        X -= zoom / 3;
                        getImage();
                    }
                });

                findViewById(R.id.stanga).setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        X += zoom / 3;
                        getImage();
                    }
                });

                findViewById(R.id.sus).setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        Y += zoom / 3;
                        getImage();
                    }
                });

                findViewById(R.id.jos).setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        Y -= zoom / 3;
                        getImage();
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            out.writeObject(new MessageClientOff());
            sock.close();
        } catch (Exception ex) {
            error("Eroare la inchiderea conexiunii : " + ex);
        }
    }


}
