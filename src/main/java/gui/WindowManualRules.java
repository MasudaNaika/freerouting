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
 * TraceWidthWindow.java
 *
 * Created on 18. November 2004, 09:08
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
 * Used for manual choice of trace widths in interactive routing.
 *
 * @author Alfons Wirtz
 */
public class WindowManualRules extends BoardSavableSubWindow {

    /**
     * Creates a new instance of TraceWidthWindow
     */
    public WindowManualRules(BoardFrame p_board_frame) {
        board_handling = p_board_frame.board_panel.board_handling;
        ResourceBundle resources
                = ResourceBundle.getBundle("gui.resources.WindowManualRule", p_board_frame.get_locale());
        setTitle(resources.getString("title"));

        // create main panel
        final JPanel main_panel = new JPanel();
        getContentPane().add(main_panel);
        GridBagLayout gridbag = new GridBagLayout();
        main_panel.setLayout(gridbag);
        GridBagConstraints gridbag_constraints = new GridBagConstraints();
        gridbag_constraints.insets = new Insets(5, 10, 5, 10);
        gridbag_constraints.anchor = GridBagConstraints.WEST;

        JLabel via_rule_label = new JLabel(resources.getString("via_rule"));
        gridbag_constraints.gridwidth = 2;
        gridbag.setConstraints(via_rule_label, gridbag_constraints);
        main_panel.add(via_rule_label);

        board.RoutingBoard routing_board = board_handling.get_routing_board();
        via_rule_combo_box = new JComboBox(routing_board.rules.via_rules.toArray());
        gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(via_rule_combo_box, gridbag_constraints);
        main_panel.add(via_rule_combo_box);
        via_rule_combo_box.addActionListener(new ViaRuleComboBoxListener());

        JLabel class_label = new JLabel(resources.getString("trace_clearance_class"));
        gridbag_constraints.gridwidth = 2;
        gridbag.setConstraints(class_label, gridbag_constraints);
        main_panel.add(class_label);

        clearance_combo_box = new ComboBoxClearance(routing_board.rules.clearance_matrix);
        gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(clearance_combo_box, gridbag_constraints);
        main_panel.add(clearance_combo_box);
        clearance_combo_box.addActionListener(new ClearanceComboBoxListener());

        JLabel separator = new JLabel("  ----------------------------------------  ");
        gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(separator, gridbag_constraints);
        main_panel.add(separator, gridbag_constraints);

        JLabel width_label = new JLabel(resources.getString("trace_width"));
        gridbag_constraints.gridwidth = 2;
        gridbag.setConstraints(width_label, gridbag_constraints);
        main_panel.add(width_label);
        NumberFormat number_format = NumberFormat.getInstance(p_board_frame.get_locale());
        number_format.setMaximumFractionDigits(7);
        trace_width_field = new JFormattedTextField(number_format);
        trace_width_field.setColumns(7);
        int curr_half_width = board_handling.settings.get_manual_trace_half_width(0);
        set_trace_width_field(curr_half_width);
        gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(trace_width_field, gridbag_constraints);
        main_panel.add(trace_width_field);
        trace_width_field.addActionListener(new TraceWidthFieldListener());

        JLabel layer_label = new JLabel(resources.getString("on_layer"));
        gridbag_constraints.gridwidth = 2;
        gridbag.setConstraints(layer_label, gridbag_constraints);
        main_panel.add(layer_label);

        layer_combo_box
                = new ComboBoxLayer(board_handling.get_routing_board().layer_structure, p_board_frame.get_locale());
        gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(layer_combo_box, gridbag_constraints);
        main_panel.add(layer_combo_box);
        layer_combo_box.addActionListener(new LayerComboBoxListener());

        JLabel empty_label = new JLabel();
        gridbag.setConstraints(empty_label, gridbag_constraints);
        main_panel.add(empty_label);

        p_board_frame.set_context_sensitive_help(this, "WindowManualRules");

        pack();
        setResizable(false);
    }

    /**
     * Recalculates the values in the trace width fields.
     */
    @Override
    public void refresh() {
        board.RoutingBoard routing_board = board_handling.get_routing_board();
        ComboBoxModel new_model = new DefaultComboBoxModel(routing_board.rules.via_rules.toArray());
        via_rule_combo_box.setModel(new_model);
        rules.ClearanceMatrix clearance_matrix = board_handling.get_routing_board().rules.clearance_matrix;
        if (clearance_combo_box.get_class_count() != routing_board.rules.clearance_matrix.get_class_count()) {
            clearance_combo_box.adjust(clearance_matrix);
        }
        clearance_combo_box.setSelectedIndex(board_handling.settings.get_manual_trace_clearance_class());
        int via_rule_index = board_handling.settings.get_manual_via_rule_index();
        if (via_rule_index < via_rule_combo_box.getItemCount()) {
            via_rule_combo_box.setSelectedIndex(board_handling.settings.get_manual_via_rule_index());
        }
        set_selected_layer(layer_combo_box.get_selected_layer());
        repaint();
    }

    public void set_trace_width_field(int p_half_width) {
        if (p_half_width < 0) {
            trace_width_field.setText("");
        } else {
            Float trace_width = (float) board_handling.coordinate_transform.board_to_user(2 * p_half_width);
            trace_width_field.setValue(trace_width);
        }
    }

    /**
     * Sets the selected layer to p_layer.
     */
    private void set_selected_layer(ComboBoxLayer.Layer p_layer) {
        int curr_half_width;
        switch (p_layer.index) {
            case ComboBoxLayer.ALL_LAYER_INDEX: {
                // check if the half width is layer_dependent.
                boolean trace_widths_layer_dependent = false;
                int first_half_width = board_handling.settings.get_manual_trace_half_width(0);
                for (int i = 1; i < board_handling.get_layer_count(); ++i) {
                    if (board_handling.settings.get_manual_trace_half_width(i) != first_half_width) {
                        trace_widths_layer_dependent = true;
                        break;
                    }
                }
                if (trace_widths_layer_dependent) {
                    curr_half_width = -1;
                } else {
                    curr_half_width = first_half_width;
                }
                break;
            }
            case ComboBoxLayer.INNER_LAYER_INDEX: {
                // check if the half width is layer_dependent on the inner layers.
                boolean trace_widths_layer_dependent = false;
                int first_half_width = board_handling.settings.get_manual_trace_half_width(1);
                for (int i = 2; i < board_handling.get_layer_count() - 1; ++i) {
                    if (board_handling.settings.get_manual_trace_half_width(i) != first_half_width) {
                        trace_widths_layer_dependent = true;
                        break;
                    }
                }
                if (trace_widths_layer_dependent) {
                    curr_half_width = -1;
                } else {
                    curr_half_width = first_half_width;
                }
                break;
            }
            default:
                curr_half_width = board_handling.settings.get_manual_trace_half_width(p_layer.index);
                break;
        }
        set_trace_width_field(curr_half_width);
    }

    private final interactive.BoardHandling board_handling;
    private final ComboBoxLayer layer_combo_box;
    private final ComboBoxClearance clearance_combo_box;
    private final JComboBox via_rule_combo_box;
    private final JFormattedTextField trace_width_field;
    private static final int max_slider_value = 15000;
    private static double scale_factor = 1;

    private class LayerComboBoxListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent evt) {
            ComboBoxLayer.Layer new_selected_layer = layer_combo_box.get_selected_layer();
            set_selected_layer(new_selected_layer);
        }
    }

    private class ClearanceComboBoxListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent evt) {
            int new_index = clearance_combo_box.get_selected_class_index();
            board_handling.settings.set_manual_trace_clearance_class(new_index);
        }
    }

    private class ViaRuleComboBoxListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent evt) {
            int new_index = via_rule_combo_box.getSelectedIndex();
            board_handling.settings.set_manual_via_rule_index(new_index);
        }
    }

    private class TraceWidthFieldListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String input = trace_width_field.getText();
            try {
                double input_value = Double.parseDouble(input);
                if (input_value <= 0) {
                    return;
                }
                double board_value = board_handling.coordinate_transform.user_to_board(input_value);
                int new_half_width = (int) Math.round(0.5 * board_value);
                board_handling.set_manual_trace_half_width(layer_combo_box.get_selected_layer().index, new_half_width);
                set_trace_width_field(new_half_width);
            } catch (NumberFormatException ex) {
                // restore the text field.
                set_selected_layer(layer_combo_box.get_selected_layer());
            }
        }
    }

}
