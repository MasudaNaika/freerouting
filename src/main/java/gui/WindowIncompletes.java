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
 * IncompletesWindow.java
 *
 * Created on 21. Maerz 2005, 05:30
 */
package gui;

import interactive.RatsNest;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.List;

/**
 *
 * @author Alfons Wirtz
 */
public class WindowIncompletes extends WindowObjectListWithFilter {

    /**
     * Creates a new instance of IncompletesWindow
     */
    public WindowIncompletes(BoardFrame p_board_frame) {
        super(p_board_frame);
        ResourceBundle resources
                = ResourceBundle.getBundle("gui.resources.Default", p_board_frame.get_locale());
        setTitle(resources.getString("incompletes"));
        list_empty_message.setText(resources.getString("route_completed"));
        p_board_frame.set_context_sensitive_help(this, "WindowObjectList_Incompletes");
    }

    /**
     * Fills the list with the board incompletes.
     */
    @Override
    protected void fill_list() {
        RatsNest ratsnest = board_frame.board_panel.board_handling.get_ratsnest();
        RatsNest.AirLine[] sorted_arr = ratsnest.get_airlines();

        Arrays.sort(sorted_arr);
        for (RatsNest.AirLine airLine : sorted_arr) {
            add_to_list(airLine);
        }
        list.setVisibleRowCount(Math.min(sorted_arr.length, DEFAULT_TABLE_SIZE));
    }

    @Override
    protected void select_instances() {
        List selected_incompletes = list.getSelectedValuesList();
        if (selected_incompletes.isEmpty()) {
            return;
        }
        Set<board.Item> selected_items = new TreeSet<>();
        for (Object obj : selected_incompletes) {
            RatsNest.AirLine curr_airline = (RatsNest.AirLine) obj;
            selected_items.add(curr_airline.from_item);
            selected_items.add(curr_airline.to_item);
        }

        board_frame.board_panel.board_handling.select_items(selected_items);
        board_frame.board_panel.board_handling.zoom_selection();
    }
}
