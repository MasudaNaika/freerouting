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
 * CornerItemConstructionPopupMenu.java
 *
 * Created on 17. Februar 2005, 07:31
 */
package gui;

import java.util.ResourceBundle;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * Popup menu used while constructing a cornered shape..
 *
 * @author Alfons Wirtz
 */
class PupupMenuCornerItemConstruction extends JPopupMenu {

    /**
     * Creates a new instance of CornerItemConstructionPopupMenu
     */
    PupupMenuCornerItemConstruction(BoardFrame p_board_frame) {
        board_panel = p_board_frame.board_panel;
        ResourceBundle resources
                = ResourceBundle.getBundle("gui.resources.Default", p_board_frame.get_locale());
        JMenuItem add_corner_item = new JMenuItem();
        add_corner_item.setText(resources.getString("add_corner"));
        add_corner_item.addActionListener(e -> {
            // Same action as if the left button is clicked with
            // the current mouse coordinates in this situation
            // because the left button is a short cut for this action.
            board_panel.board_handling.left_button_clicked(board_panel.right_button_click_location);
        });

        add(add_corner_item);

        JMenuItem close_item = new JMenuItem();
        close_item.setText(resources.getString("close"));
        close_item.addActionListener(e -> {
            board_panel.board_handling.return_from_state();
        });

        add(close_item);

        JMenuItem cancel_item = new JMenuItem();
        cancel_item.setText(resources.getString("cancel"));
        cancel_item.addActionListener(e -> {
            board_panel.board_handling.cancel_state();
        });

        add(cancel_item);
    }

    private final BoardPanel board_panel;
}
