package lspu_hnn;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import lspu_hnn_core.AppModel;
import lspu_hnn_ui.AppShell;
import lspu_hnn_ui.Theme;

/** Starts LSPU-HNN. Saved data goes in a "data" folder next to the .jar (or -Dhnn.data=folder). */
public final class Main {

    private Main() {
    }

    public static Path dataDir() {
        String custom = System.getProperty("hnn.data");
        if (custom != null && !custom.isBlank()) return Paths.get(custom);
        try {
            File where = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (where.isFile()) return where.getParentFile().toPath().resolve("data");
        } catch (URISyntaxException | RuntimeException ignored) {
            // fall through to the working folder
        }
        return Paths.get(System.getProperty("user.dir"), "data");
    }

    public static BufferedImage icon() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Theme.ACCENT_DARK);
        g.fillOval(2, 2, 60, 60);
        g.setColor(Color.WHITE);
        g.setFont(Theme.font(java.awt.Font.BOLD, 20f));
        g.drawString("HNN", 8, 40);
        g.dispose();
        return img;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.install();
            Path dir = dataDir();
            AppModel model;
            try {
                model = new AppModel(dir);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(null, "Could not open the data folder:\n" + dir.toAbsolutePath() + "\n\n" + ex.getMessage(),
                        "LSPU-HNN", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JFrame f = new JFrame("LSPU-HNN – Heritage & Campus Navigator");
            f.setIconImage(icon());
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setContentPane(new AppShell(model, dir));
            Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
            f.setMinimumSize(new Dimension(1100, 700));
            f.setSize(Math.min(1360, screen.width), Math.min(860, screen.height));
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
