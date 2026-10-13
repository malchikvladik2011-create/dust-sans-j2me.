Java
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class DustSansMidlet extends MIDlet {
    private Display display;
    private DustSansCanvas canvas;

    public void startApp() {
        display = Display.getDisplay(this);
        canvas = new DustSansCanvas(this);
        display.setCurrent(canvas);
    }

    public void pauseApp() {}

    public void destroyApp(boolean unconditional) {
        notifyDestroyed();
    }
}
