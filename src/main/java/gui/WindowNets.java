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
 * NetsWindow.java
 *
 * Created on 24. Maerz 2005, 07:41
 */
package gui;

import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import rules.Net;
import rules.Nets;

/**
 *
 * @author Alfons Wirtz
 */
public class WindowNets extends WindowObjectListWithFilter {

    /**
     * Creates a new instance of NetsWindow
     */
    public WindowNets(BoardFrame p_board_frame) {
        super(p_board_frame);
        resources = ResourceBundle.getBundle("gui.resources.WindowNets", p_board_frame.get_locale());
        setTitle(resources.getString("title"));

        JPanel curr_button_panel = new JPanel();
        south_panel.add(curr_button_panel, BorderLayout.NORTH);

        final JButton assign_class_button = new JButton(resources.getString("assign_class"));
        curr_button_panel.add(assign_class_button);
        assign_class_button.setToolTipText(resources.getString("assign_class_tooltip"));
        assign_class_button.addActionListener(new AssignClassListener());

        final JButton filter_incompletes_button = new JButton(resources.getString("filter_incompletes"));
        curr_button_panel.add(filter_incompletes_button);
        filter_incompletes_button.setToolTipText(resources.getString("filter_incompletes_tooltip"));
        filter_incompletes_button.addActionListener(new FilterIncompletesListener());
        p_board_frame.set_context_sensitive_help(this, "WindowObjectList_Nets");
    }

    /**
     * Fills the list with the nets in the net list.
     */
    @Override
    protected void fill_list() {
        Nets nets = board_frame.board_panel.board_handling.get_routing_board().rules.nets;
        Net[] sorted_arr = new Net[nets.max_net_no()];
        for (int i = 0; i < sorted_arr.length; ++i) {
            sorted_arr[i] = nets.get(i + 1);
        }
        Arrays.sort(sorted_arr);
        for (Net net : sorted_arr) {
            add_to_list(net);
        }
        list.setVisibleRowCount(Math.min(sorted_arr.length, DEFAULT_TABLE_SIZE));
    }

    @Override
    protected void select_instances() {
        Object[] selected_nets = list.getSelectedValuesList().toArray();
        if (selected_nets.length <= 0) {
            return;
        }
        int[] selected_net_numbers = new int[selected_nets.length];
        for (int i = 0; i < selected_nets.length; ++i) {
            selected_net_numbers[i] = ((Net) selected_nets[i]).net_number;
        }
        board.RoutingBoard routing_board = board_frame.board_panel.board_handling.get_routing_board();
        Set<board.Item> selected_items = new ObjectAVLTreeSet<>();
        Collection<board.Item> board_items = routing_board.get_items();
        for (board.Item curr_item : board_items) {
            boolean item_matches = false;
            for (int curr_net_no : selected_net_numbers) {
                if (curr_item.contains_net(curr_net_no)) {
                    item_matches = true;
                    break;
                }
            }
            if (item_matches) {
                selected_items.add(curr_item);
            }
        }
        board_frame.board_panel.board_handling.select_items(selected_items);
        board_frame.board_panel.board_handling.zoom_selection();
    }

    private final ResourceBundle resources;

    private class AssignClassListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            List selected_nets = list.getSelectedValuesList();
            if (selected_nets.isEmpty()) {
                return;
            }
            rules.NetClasses net_classes = board_frame.board_panel.board_handling.get_routing_board().rules.net_classes;
            rules.NetClass[] class_arr = new rules.NetClass[net_classes.count()];
            for (int i = 0; i < class_arr.length; ++i) {
                class_arr[i] = net_classes.get(i);
            }
            Object selected_value = JOptionPane.showInputDialog(null, resources.getString("message_1"),
                    resources.getString("message_2"), JOptionPane.INFORMATION_MESSAGE,
                    null, class_arr, class_arr[0]);
            if (!(selected_value instanceof rules.NetClass)) {
                return;
            }
            rules.NetClass selected_class = (rules.NetClass) selected_value;
            for (Object obj : selected_nets) {
                ((Net) obj).set_class(selected_class);
            }
            board_frame.refresh_windows();
        }
    }

    private class FilterIncompletesListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            Object[] selected_nets = list.getSelectedValuesList().toArray();
            if (selected_nets.length <= 0) {
                return;
            }
            interactive.BoardHandling board_handling = board_frame.board_panel.board_handling;
            int max_net_no = board_handling.get_routing_board().rules.nets.max_net_no();
            for (int i = 1; i <= max_net_no; ++i) {
                board_handling.set_incompletes_filter(i, true);
            }
            for (Object obj : selected_nets) {
                board_handling.set_incompletes_filter(((Net) obj).net_number, false);
            }
            board_frame.board_panel.repaint();
        }
    }
}
