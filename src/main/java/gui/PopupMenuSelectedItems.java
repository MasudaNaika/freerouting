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
 * SelectedItemPopupMenu.java
 *
 * Created on 17. Februar 2005, 07:47
 */
package gui;

import java.util.ResourceBundle;
import javax.swing.JMenuItem;

/**
 * Popup menu used in the interactive selected item state..
 *
 * @author Alfons Wirtz
 */
class PopupMenuSelectedItems extends PopupMenuDisplay {

    /**
     * Creates a new instance of SelectedItemPopupMenu
     */
    PopupMenuSelectedItems(BoardFrame p_board_frame) {
        super(p_board_frame);
        ResourceBundle resources
                = ResourceBundle.getBundle("gui.resources.Default", p_board_frame.get_locale());
        JMenuItem copy_item = new JMenuItem();
        copy_item.setText(resources.getString("copy"));
        copy_item.addActionListener(e -> {
            board_panel.board_handling.copy_selected_items(board_panel.right_button_click_location);
        });

        if (board_panel.board_handling.get_routing_board().get_test_level() != board.TestLevel.RELEASE_VERSION) {
            add(copy_item);
        }

        JMenuItem move_item = new JMenuItem();
        move_item.setText(resources.getString("move"));
        move_item.addActionListener(e -> {
            board_panel.board_handling.move_selected_items(board_panel.right_button_click_location);
        });

        add(move_item, 0);
    }
}
