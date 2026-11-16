package lspu_hnn_ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * Panel that paints src/images/Student.png as its background (already contains the
 * LSPU-HNN logo). Use it as the base of every screen: login, dashboard shell, etc.
 *
 * The image is scaled to "cover" the panel and anchored top-left, so the logo in the
 * corner never gets cropped when the window is resized. Pass showLogo=false for pages
 * that have their own header (dashboard). Children placed on it should
 * call setOpaque(false) (or paint their own translucent card) so the image shows through.
 */
public class BackgroundPanel extends JPanel {

    public static final String IMAGE_PATH = "/images/Student.png";
    private static final Color FALLBACK_TOP = new Color(0x4A0F78);
    private static final Color FALLBACK_BOTTOM = new Color(0x1E0C33);
    private static final BufferedImage SOURCE = load();

    // The logo sits in the top 210 px of the 1920x1080 artwork; the lockup is the box below.
    private static final double LOGO_BOTTOM = 210 / 1080.0;
    private static final int[] LOGO_BOX = {70, 20, 620, 175};     // x, y, w, h at 1920x1080

    private final boolean showLogo;
    private BufferedImage scaled;
    private int scaledW = -1, scaledH = -1;

    public BackgroundPanel() {
        this(new BorderLayout(), true);
    }

    /** Login-style screen: full artwork including the big logo. */
    public BackgroundPanel(LayoutManager layout) {
        this(layout, true);
    }

    /**
     * @param showLogo false for dashboard pages: the top strip with the big logo is cropped
     *                 away, so content can start at the top (use {@link #logoIcon(int)} in the sidebar).
     */
    public BackgroundPanel(LayoutManager layout, boolean showLogo) {
        super(layout);
        this.showLogo = showLogo;
        setOpaque(true);
        setBackground(FALLBACK_BOTTOM);
    }

    /** The "seal + LSPU-HNN Heritage & Campus Navigator" lockup cropped from the artwork, scaled to this width. */
    public static ImageIcon logoIcon(int width) {
        if (SOURCE == null) return new ImageIcon();
        double k = SOURCE.getWidth() / 1920.0;
        BufferedImage crop = SOURCE.getSubimage((int) (LOGO_BOX[0] * k), (int) (LOGO_BOX[1] * k),
                (int) (LOGO_BOX[2] * k), (int) (LOGO_BOX[3] * k));
        int h = Math.max(1, width * crop.getHeight() / crop.getWidth());
        return new ImageIcon(crop.getScaledInstance(width, h, Image.SCALE_SMOOTH));
    }

    private static BufferedImage load() {
        try (InputStream in = BackgroundPanel.class.getResourceAsStream(IMAGE_PATH)) {
            if (in == null) {
                System.err.println("BackgroundPanel: " + IMAGE_PATH + " not found. Check src/images/Student.png");
                return null;
            }
            return ImageIO.read(in);
        } catch (IOException ex) {
            System.err.println("BackgroundPanel: could not read image: " + ex.getMessage());
            return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;

        if (SOURCE == null) {                       // purple gradient if the image is missing
            Graphics2D g = (Graphics2D) g0.create();
            g.setPaint(new GradientPaint(0, 0, FALLBACK_TOP, w, h, FALLBACK_BOTTOM));
            g.fillRect(0, 0, w, h);
            g.dispose();
            return;
        }
        if (scaled == null || scaledW != w || scaledH != h) rebuild(w, h);
        g0.drawImage(scaled, 0, 0, null);
    }

    /** Scale once per size change, not on every repaint. */
    private void rebuild(int w, int h) {
        BufferedImage src = SOURCE;
        if (!showLogo) {
            int cut = (int) (SOURCE.getHeight() * LOGO_BOTTOM);
            src = SOURCE.getSubimage(0, cut, SOURCE.getWidth(), SOURCE.getHeight() - cut);
        }
        double s = Math.max((double) w / src.getWidth(), (double) h / src.getHeight());
        int dw = (int) Math.ceil(src.getWidth() * s);
        int dh = (int) Math.ceil(src.getHeight() * s);
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, dw, dh, null);       // top-left anchor keeps the logo visible
        g.dispose();
        scaled = out;
        scaledW = w;
        scaledH = h;
    }
}