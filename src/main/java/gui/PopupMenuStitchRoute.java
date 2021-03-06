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
 * PopupMenuStitchRoute.java
 *
 * Created on 7. Februar 2006, 08:10
 *
 */
package gui;

import java.util.ResourceBundle;
import javax.swing.JMenuItem;

/**
 *
 * @author Alfons Wirtz
 */
public class PopupMenuStitchRoute extends PopupMenuDisplay {

    /**
     * Creates a new instance of PopupMenuStitchRoute
     */
    public PopupMenuStitchRoute(BoardFrame p_board_frame) {
        super(p_board_frame);
        board.LayerStructure layer_structure = board_panel.board_handling.get_routing_board().layer_structure;

        if (layer_structure.arr.length > 0) {
            change_layer_menu = new PopupMenuChangeLayer(p_board_frame);
            add(change_layer_menu, 0);
        } else {
            change_layer_menu = null;
        }
        ResourceBundle resources
                = ResourceBundle.getBundle("gui.resources.Default", p_board_frame.get_locale());
        JMenuItem insert_item = new JMenuItem();
        insert_item.setText(resources.getString("insert"));
        insert_item.addActionListener(e -> {
            board_panel.board_handling.left_button_clicked(board_panel.right_button_click_location);
        });

        add(insert_item, 0);

        JMenuItem done_item = new JMenuItem();
        done_item.setText(resources.getString("done"));
        done_item.addActionListener(e -> {
            board_panel.board_handling.return_from_state();
        });

        add(done_item, 1);

        JMenuItem cancel_item = new JMenuItem();
        cancel_item.setText(resources.getString("cancel"));
        cancel_item.addActionListener(e -> {
            board_panel.board_handling.cancel_state();
        });

        add(cancel_item, 2);

        board.Layer curr_layer = layer_structure.arr[board_panel.board_handling.settings.get_layer()];
        disable_layer_item(layer_structure.get_signal_layer_no(curr_layer));
    }

    /**
     * Disables the p_no-th item in the change_layer_menu.
     */
    void disable_layer_item(int p_no) {
        if (change_layer_menu != null) {
            change_layer_menu.disable_item(p_no);
        }
    }

    private final PopupMenuChangeLayer change_layer_menu;
}
