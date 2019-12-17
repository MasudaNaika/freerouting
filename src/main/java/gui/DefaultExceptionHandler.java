package gui;

import java.awt.Frame;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import logger.FRLogger;

/**
 * Andrey Belomutskiy 6/28/2014
 */
public class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        handleException(e);
    }

    public static void handleException(Throwable e) {
        // Here you should have a more robust, permanent record of problems
        JOptionPane.showMessageDialog(findActiveFrame(), e.toString(), "Exception Occurred", JOptionPane.OK_OPTION);
        FRLogger.error(e);
    }

    private static Frame findActiveFrame() {
        Frame[] frames = JFrame.getFrames();
        for (Frame frame : frames) {
            if (frame.isVisible()) {
                return frame;
            }
        }
        return null;
    }
}