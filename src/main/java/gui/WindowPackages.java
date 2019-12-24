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
 * PackagesWindow.java
 *
 * Created on 7. Maerz 2005, 09:14
 */
package gui;

import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import library.Package;
import library.Packages;

/**
 * Window displaying the library packagess.
 *
 * @author Alfons Wirtz
 */
public class WindowPackages extends WindowObjectListWithFilter {

    /**
     * Creates a new instance of PackagesWindow
     */
    public WindowPackages(BoardFrame p_board_frame) {
        super(p_board_frame);
        ResourceBundle resources
                = ResourceBundle.getBundle("gui.resources.Default", p_board_frame.get_locale());
        setTitle(resources.getString("packages"));
        p_board_frame.set_context_sensitive_help(this, "WindowObjectList_LibraryPackages");
    }

    /**
     * Fills the list with the library packages.
     */
    @Override
    protected void fill_list() {
        Packages packages = board_frame.board_panel.board_handling.get_routing_board().library.packages;
        Package[] sorted_arr = new Package[packages.count()];
        for (int i = 0; i < sorted_arr.length; ++i) {
            sorted_arr[i] = packages.get(i + 1);
        }
        Arrays.sort(sorted_arr);
        for (Package p : sorted_arr) {
            add_to_list(p);
        }
        list.setVisibleRowCount(Math.min(packages.count(), DEFAULT_TABLE_SIZE));
    }

    @Override
    protected void select_instances() {
        List selected_packages = list.getSelectedValuesList();
        if (selected_packages.isEmpty()) {
            return;
        }
        board.RoutingBoard routing_board = board_frame.board_panel.board_handling.get_routing_board();
        Set<board.Item> board_instances = new ObjectAVLTreeSet<>();
        Collection<board.Item> board_items = routing_board.get_items();
        for (board.Item curr_item : board_items) {
            if (curr_item.get_component_no() > 0) {
                board.Component curr_component = routing_board.components.get(curr_item.get_component_no());
                Package curr_package = curr_component.get_package();
                boolean package_matches = false;
                for (Object obj : selected_packages) {
                    if (curr_package == obj) {
                        package_matches = true;
                        break;
                    }
                }
                if (package_matches) {
                    board_instances.add(curr_item);
                }
            }
        }
        board_frame.board_panel.board_handling.select_items(board_instances);
        board_frame.board_panel.board_handling.zoom_selection();
    }
}
