//package lspu_hnn_ui;
//
//
//import java.awt.Color;
//import java.awt.Font;
//
//import javax.swing.UIManager;
//
///** Colors, fonts and the Nimbus dark settings used by every screen. */
//public final class Theme {
//
//    private Theme() {
//    }
//
//    public static final Color BG = new Color(0x0B1322);
//    public static final Color SIDEBAR = new Color(0x0E182B);
//    public static final Color PANEL = new Color(0x131F36);
//    public static final Color PANEL_ALT = new Color(0x0F1A2D);
//    public static final Color BORDER = new Color(0x23324F);
//    public static final Color TEXT = new Color(0xE6EDF7);
//    public static final Color MUTED = new Color(0x8FA1BF);
//    public static final Color ACCENT = new Color(0x4F9CF9);
//    public static final Color ACCENT_DARK = new Color(0x2F6FD6);
//    public static final Color YELLOW = new Color(0xF5C451);
//    public static final Color GREEN = new Color(0x4ADE80);
//    public static final Color RED = new Color(0xF87171);
//    public static final Color ORANGE = new Color(0xFB923C);
//    public static final Color NODE = new Color(0x1E3A66);
//    public static final Color EDGE = new Color(0x3A5078);
//    /** Dim yellow: a node already passed on the way to the target. */
//    public static final Color PASSED = new Color(0x8A7A3A);
//
//    public static Font font(int style, float size) {
//        return new Font(Font.SANS_SERIF, style, 12).deriveFont(style, size);
//    }
//
//    /** Dark text on bright fills, white text on dark fills. */
//    public static Color onColor(Color c) {
//        double lum = 0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue();
//        return lum > 140 ? new Color(0x0B1322) : Color.WHITE;
//    }
//
//    public static void install() {
//        UIManager.put("control", PANEL);
//        UIManager.put("nimbusLightBackground", PANEL_ALT);
//        UIManager.put("text", TEXT);
//        UIManager.put("nimbusBase", new Color(0x1E3A66));
//        UIManager.put("nimbusBlueGrey", new Color(0x31456A));
//        UIManager.put("nimbusSelectionBackground", ACCENT_DARK);
//        UIManager.put("nimbusFocus", ACCENT);
//        UIManager.put("nimbusDisabledText", MUTED);
//        UIManager.put("info", PANEL);
//        UIManager.put("defaultFont", font(Font.PLAIN, 13));
//        UIManager.put("OptionPane.background", PANEL);
//        UIManager.put("Panel.background", PANEL);
//        try {
//            for (UIManager.LookAndFeelInfo i : UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(i.getName())) {
//                    UIManager.setLookAndFeel(i.getClassName());
//                    break;
//                }
//            }
//        } catch (Exception ignored) {
//            // fall back to the default look and feel
//        }
//    }
//}





package lspu_hnn_ui;


import java.awt.Color;
import java.awt.Font;

import javax.swing.UIManager;

/** Colors, fonts and the Nimbus settings used by every screen (purple LSPU-HNN theme). */
public final class Theme {

    private Theme() {
    }

    // Solid colors. Keep these opaque: Swing paints translucent fills wrongly on opaque components.
    public static final Color BG = new Color(0x2A0A4A);
    public static final Color SIDEBAR = new Color(0x240840);
    public static final Color PANEL = new Color(0x1E0C33);
    public static final Color PANEL_ALT = new Color(0x170A28);
    public static final Color BORDER = new Color(0x4A2F6E);
    public static final Color TEXT = new Color(0xF5EEFF);
    public static final Color MUTED = new Color(0xC4B0DE);
    public static final Color ACCENT = new Color(0xC084FC);
    public static final Color ACCENT_DARK = new Color(0x6A2480);
    public static final Color YELLOW = new Color(0xF5C451);
    public static final Color GREEN = new Color(0x4ADE80);
    public static final Color RED = new Color(0xFF7A90);
    public static final Color ORANGE = new Color(0xFB923C);
    public static final Color NODE = new Color(0x4A2A7A);
    public static final Color EDGE = new Color(0x8A6BB8);
    /** Dim yellow: a node already passed on the way to the target. */
    public static final Color PASSED = new Color(0x8A7A3A);

    // Translucent colors. Only use these in paintComponent() of a NON-opaque panel (see Ui.Card),
    // so the background image shows through.
    public static final Color CARD = new Color(18, 8, 30, 205);
    public static final Color CARD_EDGE = new Color(255, 255, 255, 40);
    public static final Color SIDEBAR_GLASS = new Color(18, 8, 30, 150);

    public static Font font(int style, float size) {
        return new Font(Font.SANS_SERIF, style, 12).deriveFont(style, size);
    }

    /** Dark text on bright fills, white text on dark fills. */
    public static Color onColor(Color c) {
        double lum = 0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue();
        return lum > 140 ? new Color(0x1E0C33) : Color.WHITE;
    }

    public static void install() {
        UIManager.put("control", PANEL);
        UIManager.put("nimbusLightBackground", PANEL_ALT);
        UIManager.put("text", TEXT);
        UIManager.put("nimbusBase", new Color(0x4A1D78));
        UIManager.put("nimbusBlueGrey", new Color(0x4A3070));
        UIManager.put("nimbusSelectionBackground", ACCENT_DARK);
        UIManager.put("nimbusFocus", ACCENT);
        UIManager.put("nimbusDisabledText", MUTED);
        UIManager.put("info", PANEL);
        UIManager.put("defaultFont", font(Font.PLAIN, 13));
        UIManager.put("OptionPane.background", PANEL);
        UIManager.put("Panel.background", PANEL);
        try {
            for (UIManager.LookAndFeelInfo i : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(i.getName())) {
                    UIManager.setLookAndFeel(i.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // fall back to the default look and feel
        }
    }
}
