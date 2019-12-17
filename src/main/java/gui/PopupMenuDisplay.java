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
 * PopupMenuDisplay.java
 *
 * Created on 22. Mai 2005, 09:46
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package gui;

import java.util.ResourceBundle;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author Alfons Wirtz
 */
public class PopupMenuDisplay extends JPopupMenu {

    /**
     * Creates a new instance of PopupMenuDisplay
     */
    public PopupMenuDisplay(BoardFrame p_board_frame) {
        board_panel = p_board_frame.board_panel;
        ResourceBundle resources
                = ResourceBundle.getBundle("gui.resources.Default", p_board_frame.get_locale());
        JMenuItem center_display_item = new JMenuItem();
        center_display_item.setText(resources.getString("center_display"));
        center_display_item.addActionListener(e -> {
            board_panel.center_display(board_panel.right_button_click_location);
        });

        add(center_display_item);

        JMenu zoom_menu = new JMenu();
        zoom_menu.setText(resources.getString("zoom"));

        JMenuItem zoom_in_item = new JMenuItem();
        zoom_in_item.setText(resources.getString("zoom_in"));
        zoom_in_item.addActionListener(e -> {
            board_panel.zoom_in(board_panel.right_button_click_location);
        });

        zoom_menu.add(zoom_in_item);

        JMenuItem zoom_out_item = new JMenuItem();
        zoom_out_item.setText(resources.getString("zoom_out"));
        zoom_out_item.addActionListener(e -> {
            board_panel.zoom_out(board_panel.right_button_click_location);
        });

        zoom_menu.add(zoom_out_item);

        add(zoom_menu);
    }

    protected final BoardPanel board_panel;
}
