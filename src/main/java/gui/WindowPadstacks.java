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
 * PadstacksWindow.java
 *
 * Created on 6. Maerz 2005, 06:47
 */
package gui;

import datastructures.UndoableObjects;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import library.Padstack;
import library.Padstacks;

/**
 * Window displaying the library padstacks.
 *
 * @author Alfons Wirtz
 */
public class WindowPadstacks extends WindowObjectListWithFilter {

    /**
     * Creates a new instance of PadstacksWindow
     */
    public WindowPadstacks(BoardFrame p_board_frame) {
        super(p_board_frame);
        ResourceBundle resources
                = ResourceBundle.getBundle("gui.resources.Default", p_board_frame.get_locale());
        setTitle(resources.getString("padstacks"));
        p_board_frame.set_context_sensitive_help(this, "WindowObjectList_LibraryPadstacks");
    }

    /**
     * Fills the list with the library padstacks.
     */
    @Override
    protected void fill_list() {
        Padstacks padstacks = board_frame.board_panel.board_handling.get_routing_board().library.padstacks;
        Padstack[] sorted_arr = new Padstack[padstacks.count()];
        for (int i = 0; i < sorted_arr.length; ++i) {
            sorted_arr[i] = padstacks.get(i + 1);
        }
        Arrays.sort(sorted_arr);
        for (Padstack p : sorted_arr) {
            add_to_list(p);
        }
        list.setVisibleRowCount(Math.min(padstacks.count(), DEFAULT_TABLE_SIZE));
    }

    @Override
    protected void select_instances() {
        List selected_padstacks = list.getSelectedValuesList();
        if (selected_padstacks.isEmpty()) {
            return;
        }
        Collection<Padstack> padstack_list = new LinkedList<>();
        for (Object obj : selected_padstacks) {
            padstack_list.add((Padstack) obj);
        }
        board.RoutingBoard routing_board = board_frame.board_panel.board_handling.get_routing_board();
        Set<board.Item> board_instances = Freerouter.newSortedSet();
        Iterator<UndoableObjects.UndoableObjectNode> it = routing_board.item_list.start_read_object();
        while (true) {
            datastructures.UndoableObjects.Storable curr_object = routing_board.item_list.read_object(it);
            if (curr_object == null) {
                break;
            }
            if (curr_object instanceof board.DrillItem) {
                library.Padstack curr_padstack = ((board.DrillItem) curr_object).get_padstack();
                for (Padstack curr_selected_padstack : padstack_list) {
                    if (curr_padstack == curr_selected_padstack) {
                        board_instances.add((board.Item) curr_object);
                        break;
                    }
                }
            }
        }
        board_frame.board_panel.board_handling.select_items(board_instances);
        board_frame.board_panel.board_handling.zoom_selection();
    }
}
