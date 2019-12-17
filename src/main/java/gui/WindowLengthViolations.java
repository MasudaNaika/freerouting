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
 * WindowLengthViolations.java
 *
 * Created on 1. Juni 2005, 06:52
 *
 */
package gui;

import interactive.RatsNest;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import rules.Net;
import rules.NetClass;
import rules.Nets;

/**
 *
 * @author Alfons Wirtz
 */
public class WindowLengthViolations extends WindowObjectListWithFilter {

    /**
     * Creates a new instance of WindowLengthViolations
     */
    public WindowLengthViolations(BoardFrame p_board_frame) {
        super(p_board_frame);
        resources = ResourceBundle.getBundle("gui.resources.WindowLengthViolations", p_board_frame.get_locale());
        setTitle(resources.getString("title"));
        list_empty_message.setText(resources.getString("list_empty"));
        p_board_frame.set_context_sensitive_help(this, "WindowObjectList_LengthViolations");
    }

    @Override
    protected void fill_list() {
        RatsNest ratsnest = board_frame.board_panel.board_handling.get_ratsnest();
        Nets net_list = board_frame.board_panel.board_handling.get_routing_board().rules.nets;
        SortedSet<LengthViolation> length_violations = new TreeSet<>();
        for (int net_index = 1; net_index <= net_list.max_net_no(); ++net_index) {
            double curr_violation_length = ratsnest.get_length_violation(net_index);
            if (curr_violation_length != 0) {
                LengthViolation curr_length_violation = new LengthViolation(net_list.get(net_index), curr_violation_length);
                length_violations.add(curr_length_violation);
            }
        }

        for (LengthViolation curr_violation : length_violations) {
            add_to_list(curr_violation);
        }
        list.setVisibleRowCount(Math.min(length_violations.size(), DEFAULT_TABLE_SIZE));
    }

    @Override
    protected void select_instances() {
        Object[] selected_violations = list.getSelectedValues();
        if (selected_violations.length <= 0) {
            return;
        }
        Set<board.Item> selected_items = new TreeSet<>();
        for (int i = 0; i < selected_violations.length; ++i) {
            LengthViolation curr_violation = ((LengthViolation) selected_violations[i]);
            selected_items.addAll(curr_violation.net.get_items());
        }
        interactive.BoardHandling board_handling = board_frame.board_panel.board_handling;
        board_handling.select_items(selected_items);
        board_handling.zoom_selection();
    }

    private final ResourceBundle resources;

    private class LengthViolation implements Comparable<LengthViolation> {

        LengthViolation(Net p_net, double p_violation_length) {
            net = p_net;
            violation_length = p_violation_length;
        }

        @Override
        public int compareTo(LengthViolation p_other) {
            return net.name.compareToIgnoreCase(p_other.net.name);
        }

        @Override
        public String toString() {
            board.CoordinateTransform coordinate_transform = board_frame.board_panel.board_handling.coordinate_transform;
            NetClass net_class = net.get_class();
            Float allowed_length;
            String allowed_string;
            if (violation_length > 0) {
                allowed_length = (float) coordinate_transform.board_to_user(net_class.get_maximum_trace_length());
                allowed_string = " " + resources.getString("maximum_allowed") + " ";
            } else {
                allowed_length = (float) coordinate_transform.board_to_user(net_class.get_minimum_trace_length());
                allowed_string = " " + resources.getString("minimum_allowed") + " ";
            }
            Float length = (float) coordinate_transform.board_to_user(net.get_trace_length());
            String result = resources.getString("net") + " " + net.name + resources.getString("trace_length")
                    + " " + length.toString() + allowed_string + allowed_length;
            return result;
        }

        final Net net;
        final double violation_length;
    }
}
