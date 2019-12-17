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
 * RoutePopupMenu.java
 *
 * Created on 17. Februar 2005, 07:08
 */
package gui;

import java.util.ResourceBundle;
import javax.swing.JMenuItem;

/**
 * Popup menu used in the interactive route state.
 *
 * @author Alfons Wirtz
 */
public class PopupMenuDynamicRoute extends PopupMenuDisplay {

    /**
     * Creates a new instance of RoutePopupMenu
     */
    PopupMenuDynamicRoute(BoardFrame p_board_frame) {
        super(p_board_frame);

        ResourceBundle resources
                = ResourceBundle.getBundle("gui.resources.Default", p_board_frame.get_locale());
        board.LayerStructure layer_structure = board_panel.board_handling.get_routing_board().layer_structure;

        JMenuItem end_route_item = new JMenuItem();
        end_route_item.setText(resources.getString("end_route"));
        end_route_item.addActionListener(e -> {
            board_panel.board_handling.return_from_state();
        });

        add(end_route_item, 0);

        JMenuItem cancel_item = new JMenuItem();
        cancel_item.setText(resources.getString("cancel_route"));
        cancel_item.addActionListener(e -> {
            board_panel.board_handling.cancel_state();
        });

        add(cancel_item, 1);

        JMenuItem snapshot_item = new JMenuItem();
        snapshot_item.setText(resources.getString("generate_snapshot"));
        snapshot_item.addActionListener(e -> {
            board_panel.board_handling.generate_snapshot();
        });

        add(snapshot_item, 2);

        if (layer_structure.arr.length > 0) {
            change_layer_menu = new PopupMenuChangeLayer(p_board_frame);
            add(change_layer_menu, 0);
        } else {
            change_layer_menu = null;
        }

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
