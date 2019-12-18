package gui;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Delegaion class of MainApplication
 * 
 * @author masuda
 */
public class Freerouter {

    private static final String FLAT_LAF_LIGHT = "com.formdev.flatlaf.FlatLightLaf";
    private static final String FLAT_LAF_DARK = "com.formdev.flatlaf.FlatDarkLaf";
    private static final String FLAT_LAF_INTELLIJ = "com.formdev.flatlaf.FlatIntelliJLaf";
    private static final String FLAT_LAF_DARCULA = "com.formdev.flatlaf.FlatDarculaLaf";
    
    public static void main(String... args) {

        if (System.getProperty("os.name").toLowerCase().startsWith("mac")) {
            System.setProperty("apple.laf.useScreenMenuBar", String.valueOf(true));
            System.setProperty("com.apple.macos.smallTabs", String.valueOf(true));
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Freerouter");    // doesn't work
            
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
//        Taskbar.getTaskbar().setIconImage(icon_image);
        }
        
        System.setProperty("awt.useSystemAAFontSettings", "on");
        
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
        
        MainApplication.main(args);
    }
    
}
