/*
 *  Copyright (C) 2014  Alfons Wirtz  
 *   website www.freerouting.net
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License at <http://www.gnu.org/licenses/> 
 *   for more details.
 *
 * WindowMessage.java
 *
 * Created on 8. Dezember 2005, 06:20
 *
 */
package gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.*;

/**
 * Startup window visible when the program is loading.
 *
 * @author Alfons Wirtz
 */
public class WindowMessage extends JFrame {

    public WindowMessage() {
        Freerouter.setWindowIcon(this);
    }

    /**
     * Displays a window with the input message at the center of the screen.
     */
    public static WindowMessage show(String p_message) {
        String[] message_arr = {p_message};
        return new WindowMessage(message_arr);
    }

    /**
     * Displays a window with the input messages at the center of the screen.
     */
    public static WindowMessage show(String[] p_messages) {
        return new WindowMessage(p_messages);
    }

    /**
     * Calls a confirm dialog. Returns true, if the user confirmed the action or
     * if p_message is null.
     */
    public static boolean confirm(String p_message) {
        if (p_message == null) {
            return true;
        }

        int option = JOptionPane.showConfirmDialog(null, p_message, null, JOptionPane.YES_NO_OPTION);
        boolean result = option == JOptionPane.YES_OPTION;
        return result;
    }

    /**
     * Calls a dialog with an ok-button.
     */
    public static void ok(String p_message) {
        JOptionPane.showMessageDialog(null, p_message);
    }

    /**
     * Creates a new instance of WindowMessage
     */
    private WindowMessage(String[] p_message_arr) {
        final JPanel main_panel = new JPanel();
        final GridBagLayout gridbag = new GridBagLayout();
        main_panel.setLayout(gridbag);
        final GridBagConstraints gridbag_constraints = new GridBagConstraints();
        gridbag_constraints.insets = new Insets(40, 40, 40, 40);
        gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;
        for (int i = 0; i < p_message_arr.length; ++i) {
            final JLabel message_label = new JLabel();
            message_label.setText(p_message_arr[i]);

            gridbag.setConstraints(message_label, gridbag_constraints);
            main_panel.add(message_label, gridbag_constraints);
        }
        add(main_panel);
        pack();
        setLocation(500, 400);
        setVisible(true);
    }
}
