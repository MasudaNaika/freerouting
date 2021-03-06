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
 * WindowAutorouteDetailParameter.java
 *
 * Created on 25. Juli 2006, 08:17
 *
 */
package gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ResourceBundle;
import javax.swing.*;

/**
 *
 * @author Alfons Wirtz
 */
public class WindowAutorouteDetailParameter extends BoardSavableSubWindow {

    /**
     * Creates a new instance of WindowAutorouteDetailParameter
     */
    public WindowAutorouteDetailParameter(BoardFrame p_board_frame) {
        board_handling = p_board_frame.board_panel.board_handling;
        ResourceBundle resources
                = ResourceBundle.getBundle("gui.resources.WindowAutorouteParameter", p_board_frame.get_locale());
        setTitle(resources.getString("detail_autoroute_parameter"));

        // create main panel
        final JPanel main_panel = new JPanel();
        getContentPane().add(main_panel);
        GridBagLayout gridbag = new GridBagLayout();
        main_panel.setLayout(gridbag);
        GridBagConstraints gridbag_constraints = new GridBagConstraints();
        gridbag_constraints.anchor = GridBagConstraints.WEST;
        gridbag_constraints.insets = new Insets(5, 10, 5, 10);

        // add label and number field for the via costs.
        gridbag_constraints.gridwidth = 2;
        JLabel via_cost_label = new JLabel(resources.getString("via_costs"));
        gridbag.setConstraints(via_cost_label, gridbag_constraints);
        main_panel.add(via_cost_label);

        NumberFormat number_format = NumberFormat.getIntegerInstance(p_board_frame.get_locale());
        via_cost_field = new JFormattedTextField(number_format);
        via_cost_field.setColumns(3);
        via_cost_field.addActionListener(new ViaCostFieldListener());
        gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(via_cost_field, gridbag_constraints);
        main_panel.add(via_cost_field);

        plane_via_cost_field = new JFormattedTextField(number_format);
        plane_via_cost_field.setColumns(3);
        plane_via_cost_field.addActionListener(new PlaneViaCostFieldListener());

        gridbag_constraints.gridwidth = 2;
        JLabel plane_via_cost_label = new JLabel(resources.getString("plane_via_costs"));
        gridbag.setConstraints(plane_via_cost_label, gridbag_constraints);
        main_panel.add(plane_via_cost_label);
        gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(plane_via_cost_field, gridbag_constraints);
        main_panel.add(plane_via_cost_field);

        // add label and number field for the start pass no.
        gridbag_constraints.gridwidth = 2;
        JLabel start_pass_label = new JLabel(resources.getString("start_pass"));
        gridbag.setConstraints(start_pass_label, gridbag_constraints);
        main_panel.add(start_pass_label);

        start_pass_no = new JFormattedTextField(number_format);
        start_pass_no.setColumns(2);
        start_pass_no.addActionListener(new StartPassFieldListener());
        gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(start_pass_no, gridbag_constraints);
        main_panel.add(start_pass_no);

        // add label and number field for the start ripup costs.
        gridbag_constraints.gridwidth = 2;
        JLabel start_ripup_costs_label = new JLabel();
        start_ripup_costs_label.setText(resources.getString("start_ripup_costs"));
        gridbag.setConstraints(start_ripup_costs_label, gridbag_constraints);
        main_panel.add(start_ripup_costs_label);

        start_ripup_costs = new JFormattedTextField(number_format);
        start_ripup_costs.setColumns(3);
        start_ripup_costs.addActionListener(new StartRipupCostFieldListener());
        gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(start_ripup_costs, gridbag_constraints);
        main_panel.add(start_ripup_costs);

        // add label and combo box for the router speed (if the speed is set to slow, free angle geometry
        // is used also in the 45 and 90 degree modes.
        speed_fast = resources.getString("fast");
        speed_slow = resources.getString("slow");
        speed_combo_box = new JComboBox();
        speed_combo_box.addItem(speed_fast);
        speed_combo_box.addItem(speed_slow);
        speed_combo_box.addActionListener(new SpeedListener());

        if (board_handling.get_routing_board().get_test_level() != board.TestLevel.RELEASE_VERSION) {
            gridbag_constraints.gridwidth = 2;
            JLabel speed_label = new JLabel();
            speed_label.setText(resources.getString("speed"));
            gridbag.setConstraints(speed_label, gridbag_constraints);
            main_panel.add(speed_label);

            gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;
            gridbag.setConstraints(speed_combo_box, gridbag_constraints);
            main_panel.add(speed_combo_box);
        }

        JLabel separator = new JLabel("----------------------------------------------------------------  ");
        gridbag.setConstraints(separator, gridbag_constraints);
        main_panel.add(separator, gridbag_constraints);

        // add label and number field for the trace costs on each layer.
        gridbag_constraints.gridwidth = 3;
        JLabel layer_label = new JLabel(resources.getString("trace_costs_on_layer"));
        gridbag.setConstraints(layer_label, gridbag_constraints);
        main_panel.add(layer_label);

        JLabel pref_dir_label = new JLabel(resources.getString("in_preferred_direction"));
        gridbag.setConstraints(pref_dir_label, gridbag_constraints);
        main_panel.add(pref_dir_label);

        gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;
        JLabel against_pref_dir_label = new JLabel(resources.getString("against_preferred_direction"));
        gridbag.setConstraints(against_pref_dir_label, gridbag_constraints);
        main_panel.add(against_pref_dir_label);

        board.LayerStructure layer_structure = board_handling.get_routing_board().layer_structure;
        int signal_layer_count = layer_structure.signal_layer_count();
        layer_name_arr = new JLabel[signal_layer_count];
        preferred_direction_trace_cost_arr = new JFormattedTextField[signal_layer_count];
        against_preferred_direction_trace_cost_arr = new JFormattedTextField[signal_layer_count];
        number_format = NumberFormat.getInstance(p_board_frame.get_locale());
        number_format.setMaximumFractionDigits(2);
        final int TEXT_FIELD_LENGTH = 2;
        for (int i = 0; i < signal_layer_count; ++i) {
            layer_name_arr[i] = new JLabel();
            board.Layer curr_signal_layer = layer_structure.get_signal_layer(i);
            layer_name_arr[i].setText(curr_signal_layer.name);
            gridbag_constraints.gridwidth = 3;
            gridbag.setConstraints(layer_name_arr[i], gridbag_constraints);
            main_panel.add(layer_name_arr[i]);
            preferred_direction_trace_cost_arr[i] = new JFormattedTextField(number_format);
            preferred_direction_trace_cost_arr[i].setColumns(TEXT_FIELD_LENGTH);
            preferred_direction_trace_cost_arr[i].addActionListener(new PreferredDirectionTraceCostListener(i));
            gridbag.setConstraints(preferred_direction_trace_cost_arr[i], gridbag_constraints);
            main_panel.add(preferred_direction_trace_cost_arr[i]);
            against_preferred_direction_trace_cost_arr[i] = new JFormattedTextField(number_format);
            against_preferred_direction_trace_cost_arr[i].setColumns(TEXT_FIELD_LENGTH);
            against_preferred_direction_trace_cost_arr[i].addActionListener(new AgainstPreferredDirectionTraceCostListener(i));
            gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;
            gridbag.setConstraints(against_preferred_direction_trace_cost_arr[i], gridbag_constraints);
            main_panel.add(against_preferred_direction_trace_cost_arr[i]);

        }

        p_board_frame.set_context_sensitive_help(this, "WindowAutorouteDetailParameter");

        refresh();
        pack();
        setResizable(false);
    }

    /**
     * Recalculates all displayed values
     */
    @Override
    public void refresh() {
        interactive.AutorouteSettings settings = board_handling.settings.autoroute_settings;
        board.LayerStructure layer_structure = board_handling.get_routing_board().layer_structure;
        via_cost_field.setValue(settings.get_via_costs());
        plane_via_cost_field.setValue(settings.get_plane_via_costs());
        start_ripup_costs.setValue(settings.get_start_ripup_costs());
        start_pass_no.setValue(settings.get_pass_no());
        for (int i = 0; i < preferred_direction_trace_cost_arr.length; ++i) {
            preferred_direction_trace_cost_arr[i].setValue(settings.get_preferred_direction_trace_costs(layer_structure.get_layer_no(i)));
        }
        for (int i = 0; i < against_preferred_direction_trace_cost_arr.length; ++i) {
            against_preferred_direction_trace_cost_arr[i].setValue(settings.get_against_preferred_direction_trace_costs(layer_structure.get_layer_no(i)));
        }
    }
    private final interactive.BoardHandling board_handling;
    private final JFormattedTextField via_cost_field;
    private final JFormattedTextField plane_via_cost_field;
    private final JFormattedTextField start_ripup_costs;
    private final JFormattedTextField start_pass_no;
    private final JComboBox speed_combo_box;
    private final String speed_fast;
    private final String speed_slow;
    private final JLabel[] layer_name_arr;
    private final JFormattedTextField[] preferred_direction_trace_cost_arr;
    private final JFormattedTextField[] against_preferred_direction_trace_cost_arr;

    private class ViaCostFieldListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String input = via_cost_field.getText();
                int input_value = Math.max(Integer.parseInt(input), 1);
                via_cost_field.setValue(input_value);
                board_handling.settings.autoroute_settings.set_via_costs(input_value);
                refresh();
            } catch (NumberFormatException ex) {
                int old_value = board_handling.settings.autoroute_settings.get_via_costs();
                via_cost_field.setValue(old_value);
            }
        }
    }

    private class PlaneViaCostFieldListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String input = plane_via_cost_field.getText();
                int input_value = Math.max(Integer.parseInt(input), 1);
                plane_via_cost_field.setValue(input_value);
                board_handling.settings.autoroute_settings.set_plane_via_costs(input_value);
                refresh();
            } catch (NumberFormatException ex) {
                int old_value = board_handling.settings.autoroute_settings.get_plane_via_costs();
                plane_via_cost_field.setValue(old_value);
            }
        }
    }

    private class StartRipupCostFieldListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String input = start_ripup_costs.getText();
                int input_value = Math.max(Integer.parseInt(input), 1);
                board_handling.settings.autoroute_settings.set_start_ripup_costs(input_value);
                start_ripup_costs.setValue(input_value);
                refresh();
            } catch (NumberFormatException ex) {
                int old_value = board_handling.settings.autoroute_settings.get_start_ripup_costs();
                start_ripup_costs.setValue(old_value);

            }
        }
    }

    private class StartPassFieldListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String input = start_pass_no.getText();
                int input_value = Math.max(Integer.parseInt(input), 1);
                input_value = Math.min(input_value, 99);
                board_handling.settings.autoroute_settings.set_pass_no(input_value);
                start_pass_no.setValue(input_value);
                refresh();
            } catch (NumberFormatException ex) {
                int old_value = board_handling.settings.autoroute_settings.get_pass_no();
                start_pass_no.setValue(old_value);
            }
        }
    }

    private class SpeedListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            boolean old_is_slow = board_handling.get_routing_board().rules.get_slow_autoroute_algorithm();
            boolean new_is_slow = speed_combo_box.getSelectedItem() == speed_slow;
            if (old_is_slow != new_is_slow) {
                board_handling.get_routing_board().rules.set_slow_autoroute_algorithm(new_is_slow);
                board_handling.get_routing_board().search_tree_manager.reset_compensated_trees();
            }
        }
    }

    private class PreferredDirectionTraceCostListener implements ActionListener {

        private final int signal_layer_no;

        public PreferredDirectionTraceCostListener(int p_layer_no) {
            signal_layer_no = p_layer_no;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int curr_layer_no = board_handling.get_routing_board().layer_structure.get_layer_no(signal_layer_no);
            try {
                String input = preferred_direction_trace_cost_arr[signal_layer_no].getText();
                double input_value = Double.parseDouble(input);
                if (input_value <= 0) {
                    throw new NumberFormatException();
                }
                board_handling.settings.autoroute_settings.set_preferred_direction_trace_costs(curr_layer_no, input_value);
                preferred_direction_trace_cost_arr[signal_layer_no].setValue(input_value);
                refresh();
            } catch (NumberFormatException ex) {
                double old_value = board_handling.settings.autoroute_settings.get_preferred_direction_trace_costs(curr_layer_no);
                preferred_direction_trace_cost_arr[signal_layer_no].setValue(old_value);
            }
        }
    }

    private class AgainstPreferredDirectionTraceCostListener implements ActionListener {

        private final int signal_layer_no;

        public AgainstPreferredDirectionTraceCostListener(int p_layer_no) {
            signal_layer_no = p_layer_no;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int curr_layer_no = board_handling.get_routing_board().layer_structure.get_layer_no(signal_layer_no);
            try {
                String input = against_preferred_direction_trace_cost_arr[signal_layer_no].getText();
                double input_value = Double.parseDouble(input);
                if (input_value <= 0) {
                    throw new NumberFormatException();
                }
                board_handling.settings.autoroute_settings.set_against_preferred_direction_trace_costs(curr_layer_no, input_value);
                against_preferred_direction_trace_cost_arr[signal_layer_no].setValue(input_value);
                refresh();
            } catch (NumberFormatException ex) {
                double old_value = board_handling.settings.autoroute_settings.get_against_preferred_direction_trace_costs(curr_layer_no);
                against_preferred_direction_trace_cost_arr[signal_layer_no].setValue(old_value);
            }
        }

    }

}
