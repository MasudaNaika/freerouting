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
 * ViaWindow.java
 *
 * Created on 31. Maerz 2005, 08:36
 */
package gui;

import board.Layer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ResourceBundle;
import javax.swing.*;
import rules.BoardRules;
import rules.ViaRule;

/**
 * Window for interactive editing of via rules.
 *
 * @author Alfons Wirtz
 */
public class WindowVia extends BoardSavableSubWindow {

    /**
     * Creates a new instance of ViaWindow
     */
    public WindowVia(BoardFrame p_board_frame) {
        resources = ResourceBundle.getBundle("gui.resources.WindowVia", p_board_frame.get_locale());
        setTitle(resources.getString("title"));

        board_frame = p_board_frame;

        main_panel = new JPanel();
        main_panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        main_panel.setLayout(new BorderLayout());

        JPanel north_panel = new JPanel();
        main_panel.add(north_panel, BorderLayout.NORTH);
        GridBagLayout gridbag = new GridBagLayout();
        north_panel.setLayout(gridbag);
        GridBagConstraints gridbag_constraints = new GridBagConstraints();
        gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;

        JLabel available_via_padstack_label = new JLabel(resources.getString("available_via_padstacks"));
        available_via_padstack_label.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));
        gridbag.setConstraints(available_via_padstack_label, gridbag_constraints);
        north_panel.add(available_via_padstack_label, gridbag_constraints);

        JPanel padstack_button_panel = new JPanel();
        padstack_button_panel.setLayout(new FlowLayout());
        gridbag.setConstraints(padstack_button_panel, gridbag_constraints);
        north_panel.add(padstack_button_panel, gridbag_constraints);

        final JButton show_padstack_button = new JButton(resources.getString("info"));
        show_padstack_button.setToolTipText(resources.getString("info_tooltip"));
        show_padstack_button.addActionListener(new ShowPadstacksListener());
        padstack_button_panel.add(show_padstack_button);

        final JButton add_padstack_button = new JButton(resources.getString("create"));
        add_padstack_button.setToolTipText(resources.getString("create_tooltip"));
        add_padstack_button.addActionListener(new AddPadstackListener());
        padstack_button_panel.add(add_padstack_button);

        final JButton remove_padstack_button = new JButton(resources.getString("remove"));
        remove_padstack_button.setToolTipText(resources.getString("remove_tooltip"));
        remove_padstack_button.addActionListener(new RemovePadstackListener());
        padstack_button_panel.add(remove_padstack_button);

        JLabel separator_label = new JLabel("---------------------------------------------------------");
        separator_label.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        gridbag.setConstraints(separator_label, gridbag_constraints);
        north_panel.add(separator_label, gridbag_constraints);

        JLabel available_vias_label = new JLabel(resources.getString("available_vias"));
        available_vias_label.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));
        gridbag.setConstraints(available_vias_label, gridbag_constraints);
        north_panel.add(available_vias_label, gridbag_constraints);

        JPanel via_button_panel = new JPanel();
        via_button_panel.setLayout(new FlowLayout());
        gridbag.setConstraints(via_button_panel, gridbag_constraints);
        north_panel.add(via_button_panel, gridbag_constraints);

        final JButton show_vias_button = new JButton(resources.getString("info"));
        show_vias_button.setToolTipText(resources.getString("info_tooltip_2"));
        show_vias_button.addActionListener(new ShowViasListener());
        via_button_panel.add(show_vias_button);

        final JButton edit_vias_button = new JButton(resources.getString("edit"));
        edit_vias_button.setToolTipText(resources.getString("edit_tooltip"));
        edit_vias_button.addActionListener(new EditViasListener());
        via_button_panel.add(edit_vias_button);

        separator_label = new JLabel("---------------------------------------------------------");
        separator_label.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        gridbag.setConstraints(separator_label, gridbag_constraints);
        north_panel.add(separator_label, gridbag_constraints);

        JLabel via_rule_list_name = new JLabel(resources.getString("via_rules"));
        via_rule_list_name.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));
        gridbag.setConstraints(via_rule_list_name, gridbag_constraints);
        north_panel.add(via_rule_list_name, gridbag_constraints);
        north_panel.add(via_rule_list_name, gridbag_constraints);

        rule_list_model = new DefaultListModel();
        rule_list = new JList(rule_list_model);

        rule_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rule_list.setSelectedIndex(0);
        rule_list.setVisibleRowCount(5);
        JScrollPane list_scroll_pane = new JScrollPane(rule_list);
        list_scroll_pane.setPreferredSize(new Dimension(200, 100));
        main_panel.add(list_scroll_pane, BorderLayout.CENTER);

        // fill the list
        BoardRules board_rules = board_frame.board_panel.board_handling.get_routing_board().rules;
        for (ViaRule curr_rule : board_rules.via_rules) {
            rule_list_model.addElement(curr_rule);
        }

        // Add buttons to edit the via rules.
        JPanel via_rule_button_panel = new JPanel();
        via_rule_button_panel.setLayout(new FlowLayout());
        add(via_rule_button_panel, BorderLayout.SOUTH);

        final JButton show_rule_button = new JButton(resources.getString("info"));
        show_rule_button.setToolTipText(resources.getString("info_tooltip_3"));
        show_rule_button.addActionListener(new ShowViaRuleListener());
        via_rule_button_panel.add(show_rule_button);

        final JButton add_rule_button = new JButton(resources.getString("create"));
        add_rule_button.setToolTipText(resources.getString("create_tooltip_2"));
        add_rule_button.addActionListener(new AddViaRuleListener());
        via_rule_button_panel.add(add_rule_button);

        final JButton edit_rule_button = new JButton(resources.getString("edit"));
        edit_rule_button.setToolTipText(resources.getString("edit_tooltip_2"));
        edit_rule_button.addActionListener(new EditViaRuleListener());
        via_rule_button_panel.add(edit_rule_button);

        final JButton remove_rule_button = new JButton(resources.getString("remove"));
        remove_rule_button.setToolTipText(resources.getString("remove_tooltip_2"));
        remove_rule_button.addActionListener(new RemoveViaRuleListener());
        via_rule_button_panel.add(remove_rule_button);

        p_board_frame.set_context_sensitive_help(this, "WindowVia");

        add(main_panel);
        pack();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    @Override
    public void refresh() {
        // reinsert the elements in the rule list
        rule_list_model.removeAllElements();
        BoardRules board_rules = board_frame.board_panel.board_handling.get_routing_board().rules;
        for (ViaRule curr_rule : board_rules.via_rules) {
            rule_list_model.addElement(curr_rule);
        }

        // Dispose all subwindows because they may be no longer uptodate.
        Iterator<JFrame> it = subwindows.iterator();
        while (it.hasNext()) {
            JFrame curr_subwindow = it.next();
            if (curr_subwindow != null) {
                curr_subwindow.dispose();
            }
            it.remove();
        }
    }

    @Override
    public void dispose() {
        for (JFrame curr_subwindow : subwindows) {
            if (curr_subwindow != null) {
                curr_subwindow.dispose();
            }
        }
        if (board_frame.edit_vias_window != null) {
            board_frame.edit_vias_window.dispose();
        }
        super.dispose();
    }

    private final BoardFrame board_frame;

    private final ResourceBundle resources;

    private final JList rule_list;
    private final DefaultListModel rule_list_model;

    private final JPanel main_panel;

    /**
     * The subwindows with information about selected object
     */
    private final Collection<JFrame> subwindows = new LinkedList<>();

    private static final int WINDOW_OFFSET = 30;

    private class ShowPadstacksListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            Collection<WindowObjectInfo.Printable> object_list = new LinkedList<>();
            library.BoardLibrary board_library = board_frame.board_panel.board_handling.get_routing_board().library;
            for (int i = 0; i < board_library.via_padstack_count(); ++i) {
                object_list.add(board_library.get_via_padstack(i));
            }
            board.CoordinateTransform coordinate_transform = board_frame.board_panel.board_handling.coordinate_transform;
            WindowObjectInfo new_window
                    = WindowObjectInfo.display(resources.getString("available_via_padstacks"), object_list, board_frame, coordinate_transform);
            java.awt.Point loc = getLocation();
            java.awt.Point new_window_location
                    = new java.awt.Point((int) (loc.getX() + WINDOW_OFFSET), (int) (loc.getY() + WINDOW_OFFSET));
            new_window.setLocation(new_window_location);
            subwindows.add(new_window);
        }
    }

    private class AddPadstackListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            board.BasicBoard pcb = board_frame.board_panel.board_handling.get_routing_board();
            if (pcb.layer_structure.arr.length <= 1) {
                return;
            }
            String padstack_name = JOptionPane.showInputDialog(resources.getString("message_1"));
            if (padstack_name == null) {
                return;
            }
            while (pcb.library.padstacks.get(padstack_name) != null) {
                padstack_name = JOptionPane.showInputDialog(resources.getString("message_2"), padstack_name);
                if (padstack_name == null) {
                    return;
                }
            }
            Layer start_layer = pcb.layer_structure.arr[0];
            Layer end_layer = pcb.layer_structure.arr[pcb.layer_structure.arr.length - 1];
            boolean layers_selected = false;
            if (pcb.layer_structure.arr.length == 2) {
                layers_selected = true;
            } else {
                Layer[] possible_start_layers = new board.Layer[pcb.layer_structure.arr.length - 1];
                System.arraycopy(pcb.layer_structure.arr, 0, possible_start_layers, 0, possible_start_layers.length);
                Object selected_value = JOptionPane.showInputDialog(null, resources.getString("select_start_layer"),
                        resources.getString("start_layer_selection"),
                        JOptionPane.INFORMATION_MESSAGE, null, possible_start_layers, possible_start_layers[0]);
                if (selected_value == null) {
                    return;
                }
                start_layer = (Layer) selected_value;
                if (start_layer == possible_start_layers[possible_start_layers.length - 1]) {
                    layers_selected = true;
                }
            }
            if (!layers_selected) {
                int first_possible_end_layer_no = pcb.layer_structure.get_no(start_layer) + 1;
                Layer[] possible_end_layers = new board.Layer[pcb.layer_structure.arr.length - first_possible_end_layer_no];
                System.arraycopy(pcb.layer_structure.arr, first_possible_end_layer_no,
                        possible_end_layers, 0, pcb.layer_structure.arr.length - first_possible_end_layer_no);

                Object selected_value = JOptionPane.showInputDialog(null, resources.getString("select_end_layer"),
                        resources.getString("end_layer_selection"),
                        JOptionPane.INFORMATION_MESSAGE, null, possible_end_layers,
                        possible_end_layers[possible_end_layers.length - 1]);
                if (selected_value == null) {
                    return;
                }
                end_layer = (Layer) selected_value;
            }
            Double default_radius = 100.0;

            // ask for the default radius
            JPanel default_radius_input_panel = new JPanel();
            default_radius_input_panel.add(new JLabel(resources.getString("message_3")));
            NumberFormat number_format = NumberFormat.getInstance(board_frame.get_locale());
            number_format.setMaximumFractionDigits(7);
            JFormattedTextField default_radius_input_field = new JFormattedTextField(number_format);
            default_radius_input_field.setColumns(7);
            default_radius_input_panel.add(default_radius_input_field);
            JOptionPane.showMessageDialog(board_frame, default_radius_input_panel,
                    null, JOptionPane.PLAIN_MESSAGE);
            Object input_value = default_radius_input_field.getValue();
            if (input_value instanceof Number) {
                default_radius = ((Number) input_value).doubleValue();
            }

            // input panel  to make the default radius layer-depemdent
            PadstackInputPanel padstack_input_panel = new PadstackInputPanel(start_layer, end_layer, default_radius);
            JOptionPane.showMessageDialog(board_frame, padstack_input_panel,
                    resources.getString("adjust_circles"), JOptionPane.PLAIN_MESSAGE);
            int from_layer_no = pcb.layer_structure.get_no(start_layer);
            int to_layer_no = pcb.layer_structure.get_no(end_layer);
            geometry.planar.ConvexShape[] padstack_shapes = new geometry.planar.ConvexShape[pcb.layer_structure.arr.length];
            board.CoordinateTransform coordinate_transform = board_frame.board_panel.board_handling.coordinate_transform;
            boolean shape_exists = false;
            for (int i = from_layer_no; i <= to_layer_no; ++i) {
                Object input = padstack_input_panel.circle_radius[i - from_layer_no].getValue();
                double radius = default_radius;
                if (input instanceof Number) {
                    radius = ((Number) input).doubleValue();
                }
                int circle_radius = (int) Math.round(coordinate_transform.user_to_board(radius));
                if (circle_radius > 0) {
                    padstack_shapes[i] = new geometry.planar.Circle(geometry.planar.Point.ZERO, circle_radius);
                    shape_exists = true;
                }
            }
            if (!shape_exists) {
                return;
            }
            library.Padstack new_padstack = pcb.library.padstacks.add(padstack_name, padstack_shapes, true, true);
            pcb.library.add_via_padstack(new_padstack);
        }
    }

    /**
     * Internal class used in AddPadstackListener
     */
    private class PadstackInputPanel extends JPanel {

        PadstackInputPanel(Layer p_from_layer, Layer p_to_layer, Double p_default_radius) {
            GridBagLayout gridbag = new GridBagLayout();
            setLayout(gridbag);
            GridBagConstraints gridbag_constraints = new GridBagConstraints();

            board.LayerStructure layer_structure = board_frame.board_panel.board_handling.get_routing_board().layer_structure;
            int from_layer_no = layer_structure.get_no(p_from_layer);
            int to_layer_no = layer_structure.get_no(p_to_layer);
            int layer_count = to_layer_no - from_layer_no + 1;
            layer_names = new JLabel[layer_count];
            circle_radius = new JFormattedTextField[layer_count];
            for (int i = 0; i < layer_count; ++i) {
                String label_string = resources.getString("radius_on_layer") + " " + layer_structure.arr[from_layer_no + i].name + ": ";
                layer_names[i] = new JLabel(label_string);
                NumberFormat number_format = NumberFormat.getInstance(board_frame.get_locale());
                number_format.setMaximumFractionDigits(7);
                circle_radius[i] = new JFormattedTextField(number_format);
                circle_radius[i].setColumns(7);
                circle_radius[i].setValue(p_default_radius);
                gridbag.setConstraints(layer_names[i], gridbag_constraints);
                gridbag_constraints.gridwidth = 2;
                add(layer_names[i], gridbag_constraints);
                gridbag.setConstraints(circle_radius[i], gridbag_constraints);
                gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;
                add(circle_radius[i], gridbag_constraints);
            }
        }

        private final JLabel[] layer_names;
        private final JFormattedTextField[] circle_radius;
    }

    private class RemovePadstackListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            board.BasicBoard pcb = board_frame.board_panel.board_handling.get_routing_board();
            library.Padstack[] via_padstacks = pcb.library.get_via_padstacks();
            Object selected_value = JOptionPane.showInputDialog(null,
                    resources.getString("choose_padstack_to_remove"), resources.getString("remove_via_padstack"),
                    JOptionPane.INFORMATION_MESSAGE, null, via_padstacks, via_padstacks[0]);
            if (selected_value == null) {
                return;
            }
            library.Padstack selected_padstack = (library.Padstack) selected_value;
            rules.ViaInfo via_with_selected_padstack = null;
            for (int i = 0; i < pcb.rules.via_infos.count(); ++i) {
                if (pcb.rules.via_infos.get(i).get_padstack() == selected_padstack) {
                    via_with_selected_padstack = pcb.rules.via_infos.get(i);
                    break;
                }
            }
            if (via_with_selected_padstack != null) {
                String message
                        = resources.getString("message_4") + " " + via_with_selected_padstack.get_name();
                board_frame.screen_messages.set_status_message(message);
                return;
            }
            pcb.library.remove_via_padstack(selected_padstack, pcb);
        }
    }

    private class ShowViasListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            Collection<WindowObjectInfo.Printable> object_list = new LinkedList<>();
            rules.ViaInfos via_infos = board_frame.board_panel.board_handling.get_routing_board().rules.via_infos;
            for (int i = 0; i < via_infos.count(); ++i) {
                object_list.add(via_infos.get(i));
            }
            board.CoordinateTransform coordinate_transform = board_frame.board_panel.board_handling.coordinate_transform;
            WindowObjectInfo new_window
                    = WindowObjectInfo.display(resources.getString("available_vias"), object_list, board_frame, coordinate_transform);
            java.awt.Point loc = getLocation();
            java.awt.Point new_window_location
                    = new java.awt.Point((int) (loc.getX() + WINDOW_OFFSET), (int) (loc.getY() + WINDOW_OFFSET));
            new_window.setLocation(new_window_location);
            subwindows.add(new_window);
        }
    }

    private class EditViasListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            board_frame.edit_vias_window.setVisible(true);
        }
    }

    private class ShowViaRuleListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            Object[] selected_objects = rule_list.getSelectedValuesList().toArray();
            if (selected_objects.length <= 0) {
                return;
            }
            Collection<WindowObjectInfo.Printable> object_list = new LinkedList<>();
            for (Object obj : selected_objects) {
                object_list.add((WindowObjectInfo.Printable) obj);
            }
            board.CoordinateTransform coordinate_transform = board_frame.board_panel.board_handling.coordinate_transform;
            WindowObjectInfo new_window
                    = WindowObjectInfo.display(resources.getString("selected_rule"), object_list, board_frame, coordinate_transform);
            java.awt.Point loc = getLocation();
            java.awt.Point new_window_location
                    = new java.awt.Point((int) (loc.getX() + WINDOW_OFFSET), (int) (loc.getY() + WINDOW_OFFSET));
            new_window.setLocation(new_window_location);
            subwindows.add(new_window);
        }
    }

    private class EditViaRuleListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            Object selected_object = rule_list.getSelectedValue();
            if (selected_object == null || !(selected_object instanceof ViaRule)) {
                return;
            }
            rules.BoardRules board_rules = board_frame.board_panel.board_handling.get_routing_board().rules;
            WindowViaRule new_window = new WindowViaRule((ViaRule) selected_object, board_rules.via_infos, board_frame);
            java.awt.Point loc = getLocation();
            java.awt.Point new_window_location
                    = new java.awt.Point((int) (loc.getX() + WINDOW_OFFSET), (int) (loc.getY() + WINDOW_OFFSET));
            new_window.setLocation(new_window_location);
            subwindows.add(new_window);
        }
    }

    private class AddViaRuleListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            String new_name = JOptionPane.showInputDialog(resources.getString("message_5"));
            if (new_name == null) {
                return;
            }
            new_name = new_name.trim();
            if (new_name.isEmpty()) {
                return;
            }
            ViaRule new_via_rule = new ViaRule(new_name);
            rules.BoardRules board_rules = board_frame.board_panel.board_handling.get_routing_board().rules;
            board_rules.via_rules.add(new_via_rule);
            rule_list_model.addElement(new_via_rule);
            board_frame.refresh_windows();
        }
    }

    private class RemoveViaRuleListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            Object selected_object = rule_list.getSelectedValue();
            if (selected_object == null || !(selected_object instanceof ViaRule)) {
                return;
            }
            ViaRule selected_rule = (ViaRule) selected_object;
            String message = resources.getString("remove_via_rule") + " " + selected_rule.name + "?";
            if (WindowMessage.confirm(message)) {
                rules.BoardRules board_rules = board_frame.board_panel.board_handling.get_routing_board().rules;
                board_rules.via_rules.remove(selected_rule);
                rule_list_model.removeElement(selected_rule);
            }
        }
    }
}
