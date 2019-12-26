package gui;

import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.awt.Desktop;
import java.awt.Taskbar;
import java.awt.Window;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
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
        try (InputStream is = Freerouter.class.getResourceAsStream("resources/log4j2.xml")) {
            ConfigurationSource src = new ConfigurationSource(is);
            Configurator.initialize(null, src);
        } catch (IOException ex) {
        }
    }
    
    public static <E> SortedSet<E> newSortedSet() {
//        return new TreeSet<>();
        return new ObjectAVLTreeSet<>();
    }

    public static <E> Set<E> newHashSet() {
//        return new HashMap<>();
        return new ObjectOpenHashSet<>();
    }
    
    public static <E> Set<E> newLinkedHashSet() {
//        return new LinkedHashSet<>();
        return new ObjectLinkedOpenHashSet<>();
    }

    public static SortedSet newIntSortedSet() {
//        return new TreeSet<Integer>();
        return new IntAVLTreeSet();
    }
    
    public static List<Integer> newIntArrayList() {
//        return new ArrayList<>();
        return new IntArrayList();
    }
    
    public static void toArray(List<Integer> list, int[] array) {
        ((IntArrayList) list).toArray(array);
    }
    
    public static void toArray(Set<Integer> set, int[] array) {
        ((IntSet) set).toArray(array);
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
