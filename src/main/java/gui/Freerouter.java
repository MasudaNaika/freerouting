package gui;

import java.awt.Desktop;
import java.awt.Taskbar;
import java.awt.Window;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

/**
 * Delegaion class of MainApplication
 *
 * @author masuda, Masuda Naika
 */
public class Freerouter {

    private static final String FLAT_LAF_LIGHT = "com.formdev.flatlaf.FlatLightLaf";
    private static final String FLAT_LAF_DARK = "com.formdev.flatlaf.FlatDarkLaf";
    private static final String FLAT_LAF_INTELLIJ = "com.formdev.flatlaf.FlatIntelliJLaf";
    private static final String FLAT_LAF_DARCULA = "com.formdev.flatlaf.FlatDarculaLaf";

    // https://icon-icons.com/ja/アイコン/回路/2679
    public static ImageIcon ICON;

    private static ImageIcon getImageIcon(String fileName) {
        URL url = Freerouter.class.getResource("/gui/resources/" + fileName);
        ImageIcon icon = new ImageIcon(url);
        return icon;
    }

    public static void setWindowIcon(Window w) {
        w.setIconImage(ICON.getImage());
    }

    public static Logger getLogger() {
        return LogManager.getLogger("freerooter.logger");
    }

    public static void logInfo(String msg) {
        getLogger().log(Level.INFO, msg);
    }

    public static void logWarn(String msg) {
        getLogger().log(Level.WARN, msg);
    }

    public static void logError(String msg) {
        getLogger().log(Level.ERROR, msg);
    }

    public static void logError(Throwable ex) {
        getLogger().log(Level.ERROR, ex.getMessage(), ex);
    }

    private static void initLogger() {
        try {
            URL url = Freerouter.class.getResource("/gui/resources/log4j2.xml");
            Path log4jxml = Path.of(url.toURI());
            try (InputStream in = new BufferedInputStream(Files.newInputStream(log4jxml))) {
                ConfigurationSource src = new ConfigurationSource(in);
                Configurator.initialize(null, src);
            } catch (IOException ex) {
            }
        } catch (URISyntaxException ex) {
        }
    }

    public static void main(String... args) {

        System.setProperty("awt.useSystemAAFontSettings", "on");

        boolean isMac = System.getProperty("os.name").toLowerCase().startsWith("mac");

        if (isMac) {
            System.setProperty("apple.laf.useScreenMenuBar", String.valueOf(true));
            System.setProperty("com.apple.macos.smallTabs", String.valueOf(true));
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Freerouter");    // doesn't work
            System.setProperty("apple.awt.application.name", "Freerouter");
        }

        String userLaf = FLAT_LAF_INTELLIJ;
        try {
            UIManager.setLookAndFeel(userLaf);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
            userLaf = UIManager.getSystemLookAndFeelClassName();
            try {
                UIManager.setLookAndFeel(userLaf);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {
            }
        }

        // shoud load after Sytem.setProperty
        ICON = getImageIcon("circuit_3241.png");

        if (isMac) {
            Desktop desktop = Desktop.getDesktop();

            // About
            desktop.setAboutHandler(e -> {
                try {
                    desktop.browse(new URI("http://www.freerouting.net"));
                } catch (IOException | URISyntaxException ex) {
                }
            });

            // Preference
            desktop.setPreferencesHandler(e -> {
            });

            // Quit
            desktop.setQuitHandler((qe, qr) -> {
                System.exit(0);
            });

            // Dock Icon
            Taskbar.getTaskbar().setIconImage(ICON.getImage());
        }

        initLogger();

        MainApplication.main(args);
    }

}
