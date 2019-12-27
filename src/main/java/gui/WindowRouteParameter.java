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
 * RouteParameterWindow.java
 *
 * Created on 17. November 2004, 07:11
 */
package gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.*;

/**
 * Window handling parameters of the interactive routing.
 *
 * @author Alfons Wirtz
 */
public class WindowRouteParameter extends BoardSavableSubWindow {

    /**
     * Creates a new instance of RouteParameterWindow
     */
    public WindowRouteParameter(BoardFrame p_board_frame) {
        board_handling = p_board_frame.board_panel.board_handling;
        current_locale = p_board_frame.get_locale();
        detail_window = new WindowRouteDetail(p_board_frame);
        manual_rule_window = new WindowManualRules(p_board_frame);

        ResourceBundle resources
                = ResourceBundle.getBundle("gui.resources.WindowRouteParameter", p_board_frame.get_locale());
        setTitle(resources.getString("title"));

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // create main panel
        final JPanel main_panel = new JPanel();
        getContentPane().add(main_panel);
        GridBagLayout gridbag = new GridBagLayout();
        main_panel.setLayout(gridbag);
        GridBagConstraints gridbag_constraints = new GridBagConstraints();
        gridbag_constraints.anchor = GridBagConstraints.WEST;
        gridbag_constraints.insets = new Insets(1, 10, 1, 10);

        // add label and button group for the route snap angle.
        JLabel snap_angle_label = new JLabel(resources.getString("snap_angle"));
        snap_angle_label.setToolTipText(resources.getString("snap_angle_tooltip"));

        gridbag_constraints.gridwidth = GridBagConstraints.RELATIVE;
        gridbag_constraints.gridheight = 3;
        gridbag.setConstraints(snap_angle_label, gridbag_constraints);
        main_panel.add(snap_angle_label);

        snap_angle_90_button = new JRadioButton(resources.getString("90_degree"));
        snap_angle_45_button = new JRadioButton(resources.getString("45_degree"));
        snap_angle_none_button = new JRadioButton(resources.getString("none"));

        snap_angle_90_button.addActionListener(new SnapAngle90Listener());
        snap_angle_45_button.addActionListener(new SnapAngle45Listener());
        snap_angle_none_button.addActionListener(ae
                -> board_handling.set_current_snap_angle(board.AngleRestriction.NONE));

        ButtonGroup snap_angle_button_group = new ButtonGroup();
        snap_angle_button_group.add(snap_angle_90_button);
        snap_angle_button_group.add(snap_angle_45_button);
        snap_angle_button_group.add(snap_angle_none_button);
        snap_angle_none_button.setSelected(true);

        gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridbag_constraints.gridheight = 1;
        gridbag.setConstraints(snap_angle_90_button, gridbag_constraints);
        main_panel.add(snap_angle_90_button, gridbag_constraints);

        gridbag.setConstraints(snap_angle_45_button, gridbag_constraints);
        main_panel.add(snap_angle_45_button, gridbag_constraints);
        gridbag.setConstraints(snap_angle_none_button, gridbag_constraints);
        main_panel.add(snap_angle_none_button, gridbag_constraints);

        JLabel separator = new JLabel("  ----------------------------------------  ");
        gridbag.setConstraints(separator, gridbag_constraints);
        main_panel.add(separator, gridbag_constraints);

        // add label and button group for the route mode.
        JLabel route_mode_label = new JLabel(resources.getString("route_mode"));
        gridbag_constraints.gridwidth = GridBagConstraints.RELATIVE;
        gridbag_constraints.gridheight = 2;
        gridbag.setConstraints(route_mode_label, gridbag_constraints);
        main_panel.add(route_mode_label);

        dynamic_button = new JRadioButton(resources.getString("dynamic"));
        stitch_button = new JRadioButton(resources.getString("stitching"));

        dynamic_button.addActionListener(ae
                -> board_handling.settings.set_stitch_route(false));
        stitch_button.addActionListener(ae
                -> board_handling.settings.set_stitch_route(true));

        ButtonGroup route_mode_button_group = new ButtonGroup();
        route_mode_button_group.add(dynamic_button);
        route_mode_button_group.add(stitch_button);
        dynamic_button.setSelected(true);

        gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridbag_constraints.gridheight = 1;
        gridbag.setConstraints(dynamic_button, gridbag_constraints);
        main_panel.add(dynamic_button, gridbag_constraints);
        gridbag.setConstraints(stitch_button, gridbag_constraints);
        main_panel.add(stitch_button, gridbag_constraints);

        separator = new JLabel("  ----------------------------------------  ");
        gridbag.setConstraints(separator, gridbag_constraints);
        main_panel.add(separator, gridbag_constraints);

        // add label and buttongroup for automatic or manual trace width selection.
        JLabel trace_widths_label = new JLabel(resources.getString("rule_selection"));
        gridbag_constraints.gridwidth = GridBagConstraints.RELATIVE;
        gridbag_constraints.gridheight = 2;
        gridbag.setConstraints(trace_widths_label, gridbag_constraints);
        main_panel.add(trace_widths_label);

        automatic_button = new JRadioButton(resources.getString("automatic"));
        manual_button = new JRadioButton(resources.getString("manual"));

        automatic_button.addActionListener(new AutomaticTraceWidthListener());
        manual_trace_width_listener = new ManualTraceWidthListener();
        manual_button.addActionListener(manual_trace_width_listener);

        ButtonGroup trace_widths_button_group = new ButtonGroup();
        trace_widths_button_group.add(automatic_button);
        trace_widths_button_group.add(manual_button);
        automatic_button.setSelected(true);

        gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridbag_constraints.gridheight = 1;
        gridbag.setConstraints(automatic_button, gridbag_constraints);
        main_panel.add(automatic_button, gridbag_constraints);
        gridbag.setConstraints(manual_button, gridbag_constraints);
        main_panel.add(manual_button, gridbag_constraints);

        separator = new JLabel("  ----------------------------------------  ");
        gridbag.setConstraints(separator, gridbag_constraints);
        main_panel.add(separator, gridbag_constraints);

        // add check box for push enabled
        shove_check_box = new JCheckBox(resources.getString("push&shove_enabled"));
        shove_check_box.addActionListener(ae -> {
            board_handling.settings.set_push_enabled(shove_check_box.isSelected());
            refresh();
        });
        gridbag.setConstraints(shove_check_box, gridbag_constraints);
        shove_check_box.setToolTipText(resources.getString("push&shove_enabled_tooltip"));
        main_panel.add(shove_check_box, gridbag_constraints);

        // add check box for drag components enabled
        drag_component_check_box = new JCheckBox(resources.getString("drag_components_enabled"));
        drag_component_check_box.addActionListener(ae -> {
            board_handling.settings.set_drag_components_enabled(drag_component_check_box.isSelected());
            refresh();
        });
        gridbag.setConstraints(drag_component_check_box, gridbag_constraints);
        drag_component_check_box.setToolTipText(resources.getString("drag_components_enabled_tooltip"));
        main_panel.add(drag_component_check_box, gridbag_constraints);

        // add check box for via snap to smd center
        via_snap_to_smd_center_check_box = new JCheckBox(resources.getString("via_snap_to_smd_center"));
        via_snap_to_smd_center_check_box.addActionListener(ae -> {
            board_handling.settings.set_via_snap_to_smd_center(via_snap_to_smd_center_check_box.isSelected());
        });
        gridbag.setConstraints(via_snap_to_smd_center_check_box, gridbag_constraints);
        via_snap_to_smd_center_check_box.setToolTipText(resources.getString("via_snap_to_smd_center_tooltip"));
        main_panel.add(via_snap_to_smd_center_check_box, gridbag_constraints);

        // add check box for hilighting the routing obstacle
        hilight_routing_obstacle_check_box = new JCheckBox(resources.getString("hilight_routing_obstacle"));
        hilight_routing_obstacle_check_box.addActionListener(ae -> {
            board_handling.settings.set_hilight_routing_obstacle(hilight_routing_obstacle_check_box.isSelected());
        });
        gridbag.setConstraints(hilight_routing_obstacle_check_box, gridbag_constraints);
        hilight_routing_obstacle_check_box.setToolTipText(resources.getString("hilight_routing_obstacle_tooltip"));
        main_panel.add(hilight_routing_obstacle_check_box, gridbag_constraints);

        // add check box for ignore_conduction_areas
        ignore_conduction_check_box = new JCheckBox(resources.getString("ignore_conduction_areas"));
        ignore_conduction_check_box.addActionListener(ae -> {
            board_handling.set_ignore_conduction(ignore_conduction_check_box.isSelected());
        });
        gridbag.setConstraints(ignore_conduction_check_box, gridbag_constraints);
        ignore_conduction_check_box.setToolTipText(resources.getString("ignore_conduction_areas_tooltip"));
        main_panel.add(ignore_conduction_check_box, gridbag_constraints);

        // add check box for automatic neckdown
        neckdown_check_box = new JCheckBox(resources.getString("automatic_neckdown"));
        neckdown_check_box.addActionListener(ae -> {
            board_handling.settings.set_automatic_neckdown(neckdown_check_box.isSelected());
        });
        gridbag.setConstraints(neckdown_check_box, gridbag_constraints);
        neckdown_check_box.setToolTipText(resources.getString("automatic_neckdown_tooltip"));
        main_panel.add(neckdown_check_box, gridbag_constraints);

        // add labels and text field for restricting pin exit directions
        separator = new JLabel("  ----------------------------------------  ");
        gridbag.setConstraints(separator, gridbag_constraints);
        main_panel.add(separator, gridbag_constraints);

        restrict_pin_exit_directions_check_box = new JCheckBox(resources.getString("restrict_pin_exit_directions"));
        restrict_pin_exit_directions_check_box.addActionListener(new RestrictPinExitDirectionsListener());
        gridbag.setConstraints(restrict_pin_exit_directions_check_box, gridbag_constraints);
        restrict_pin_exit_directions_check_box.setToolTipText(resources.getString("restrict_pin_exit_directions_tooltip"));
        main_panel.add(restrict_pin_exit_directions_check_box, gridbag_constraints);

        gridbag_constraints.gridwidth = GridBagConstraints.RELATIVE;
        JLabel pin_exit_edge_to_turn_label = new JLabel(resources.getString("pin_pad_to_turn_gap"));
        pin_exit_edge_to_turn_label.setToolTipText("pin_pad_to_turn_gap_tooltip");
        gridbag.setConstraints(pin_exit_edge_to_turn_label, gridbag_constraints);
        main_panel.add(pin_exit_edge_to_turn_label);
        NumberFormat number_format = NumberFormat.getInstance(p_board_frame.get_locale());
        number_format.setMaximumFractionDigits(7);
        edge_to_turn_dist_field = new JFormattedTextField(number_format);
        edge_to_turn_dist_field.setColumns(5);
        gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(edge_to_turn_dist_field, gridbag_constraints);
        main_panel.add(edge_to_turn_dist_field);
        edge_to_turn_dist_field.addActionListener(new EdgeToTurnDistFieldListener());

        gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;
        separator = new JLabel("----------------------------------------  ");
        gridbag.setConstraints(separator, gridbag_constraints);
        main_panel.add(separator, gridbag_constraints);

        // add label and slider for the pull tight region around the cursor.
        gridbag_constraints.gridwidth = GridBagConstraints.RELATIVE;
        JLabel pull_tight_region_label = new JLabel(resources.getString("pull_tight_region"));
        pull_tight_region_label.setToolTipText(resources.getString("pull_tight_region_tooltip"));
        gridbag.setConstraints(pull_tight_region_label, gridbag_constraints);
        main_panel.add(pull_tight_region_label);

        region_width_field = new JFormattedTextField(number_format);
        region_width_field.setColumns(3);
        gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(region_width_field, gridbag_constraints);
        main_panel.add(region_width_field);
        region_width_field.addActionListener(new RegionWidthFieldListener());

        region_slider = new JSlider();
        region_slider.setMaximum(c_max_slider_value);
        region_slider.addChangeListener(evt -> {
            set_pull_tight_region_width(region_slider.getValue());
        });
        gridbag.setConstraints(region_slider, gridbag_constraints);
        main_panel.add(region_slider);

        separator = new JLabel("----------------------------------------  ");
        gridbag.setConstraints(separator, gridbag_constraints);
        main_panel.add(separator, gridbag_constraints);

        JButton detail_button = new JButton(resources.getString("detail_parameter"));
        detail_listener = new DetailListener();
        detail_button.addActionListener(detail_listener);
        gridbag.setConstraints(detail_button, gridbag_constraints);
        if (board_handling.get_routing_board().get_test_level() != board.TestLevel.RELEASE_VERSION) {
            main_panel.add(detail_button);
        }

        p_board_frame.set_context_sensitive_help(this, "WindowRouteParameter");

        refresh();
        pack();
        setResizable(false);
    }

    @Override
    public void dispose() {
        detail_window.dispose();
        manual_rule_window.dispose();
        super.dispose();
    }

    /**
     * Reads the data of this frame from disk. Returns false, if the reading
     * failed.
     */
    @Override
    public boolean read(ObjectInputStream p_object_stream) {

        boolean read_ok = super.read(p_object_stream);
        if (!read_ok) {
            return false;
        }
        read_ok = manual_rule_window.read(p_object_stream);
        if (!read_ok) {
            return false;
        }
        read_ok = detail_window.read(p_object_stream);
        if (!read_ok) {
            return false;
        }
        manual_trace_width_listener.first_time = false;
        detail_listener.first_time = false;
        refresh();
        return true;

    }

    /**
     * Saves this frame to disk.
     */
    @Override
    public void save(ObjectOutputStream p_object_stream) {
        super.save(p_object_stream);
        manual_rule_window.save(p_object_stream);
        detail_window.save(p_object_stream);
    }

    /**
     * Recalculates all displayed values
     */
    @Override
    public void refresh() {
        board.AngleRestriction snap_angle = board_handling.get_routing_board().rules.get_trace_angle_restriction();

        if (snap_angle == board.AngleRestriction.NINETY_DEGREE) {
            snap_angle_90_button.setSelected(true);
        } else if (snap_angle == board.AngleRestriction.FORTYFIVE_DEGREE) {
            snap_angle_45_button.setSelected(true);
        } else {
            snap_angle_none_button.setSelected(true);
        }

        if (board_handling.settings.get_is_stitch_route()) {
            stitch_button.setSelected(true);
        } else {
            dynamic_button.setSelected(true);
        }

        if (board_handling.settings.get_manual_rule_selection()) {
            manual_button.setSelected(true);
            if (manual_rule_window != null) {
                manual_rule_window.setVisible(true);
            }
        } else {
            automatic_button.setSelected(true);
        }

        shove_check_box.setSelected(board_handling.settings.get_push_enabled());
        drag_component_check_box.setSelected(board_handling.settings.get_drag_components_enabled());
        via_snap_to_smd_center_check_box.setSelected(board_handling.settings.get_via_snap_to_smd_center());
        ignore_conduction_check_box.setSelected(board_handling.get_routing_board().rules.get_ignore_conduction());
        hilight_routing_obstacle_check_box.setSelected(board_handling.settings.get_hilight_routing_obstacle());
        neckdown_check_box.setSelected(board_handling.settings.get_automatic_neckdown());

        double edge_to_turn_dist = board_handling.get_routing_board().rules.get_pin_edge_to_turn_dist();
        edge_to_turn_dist = board_handling.coordinate_transform.board_to_user(edge_to_turn_dist);
        edge_to_turn_dist_field.setValue(edge_to_turn_dist);
        restrict_pin_exit_directions_check_box.setSelected(edge_to_turn_dist > 0);

        int region_slider_value = board_handling.settings.get_trace_pull_tight_region_width() / c_region_scale_factor;
        region_slider_value = Math.min(region_slider_value, c_max_slider_value);
        region_slider.setValue(region_slider_value);
        region_width_field.setValue(region_slider_value);

        if (manual_rule_window != null) {
            manual_rule_window.refresh();
        }
        if (detail_window != null) {
            detail_window.refresh();
        }
    }

    @Override
    public void parent_iconified() {
        manual_rule_window.parent_iconified();
        detail_window.parent_iconified();
        super.parent_iconified();
    }

    @Override
    public void parent_deiconified() {
        manual_rule_window.parent_deiconified();
        detail_window.parent_deiconified();
        super.parent_deiconified();
    }

    private void set_pull_tight_region_width(int p_slider_value) {
        int slider_value = Math.max(p_slider_value, 0);
        slider_value = Math.min(p_slider_value, c_max_slider_value);
        int new_tidy_width;
        if (slider_value >= 0.9 * c_max_slider_value) {
            p_slider_value = c_max_slider_value;
            new_tidy_width = Integer.MAX_VALUE;
        } else {
            new_tidy_width = slider_value * c_region_scale_factor;
        }
        region_slider.setValue(slider_value);
        region_width_field.setValue(slider_value);
        board_handling.settings.set_current_pull_tight_region_width(new_tidy_width);
    }

    private final interactive.BoardHandling board_handling;
    private final Locale current_locale;
    final WindowManualRules manual_rule_window;
    final WindowRouteDetail detail_window;
    private final JSlider region_slider;
    private final JFormattedTextField region_width_field;
    private final JFormattedTextField edge_to_turn_dist_field;

    private final JRadioButton snap_angle_90_button;
    private final JRadioButton snap_angle_45_button;
    private final JRadioButton snap_angle_none_button;
    private final JRadioButton dynamic_button;
    private final JRadioButton stitch_button;
    private final JRadioButton automatic_button;
    private final JRadioButton manual_button;
    private final JCheckBox shove_check_box;
    private final JCheckBox drag_component_check_box;
    private final JCheckBox ignore_conduction_check_box;
    private final JCheckBox via_snap_to_smd_center_check_box;
    private final JCheckBox hilight_routing_obstacle_check_box;
    private final JCheckBox neckdown_check_box;
    private final JCheckBox restrict_pin_exit_directions_check_box;

    private final DetailListener detail_listener;
    private final ManualTraceWidthListener manual_trace_width_listener;
    private boolean key_input_completed = true;

    private static final int c_max_slider_value = 999;
    private static final int c_region_scale_factor = 200;

    private class SnapAngle90Listener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            if (board_handling.get_routing_board().rules.get_trace_angle_restriction() == board.AngleRestriction.NINETY_DEGREE) {
                return;
            }
            Collection<board.Trace> trace_list = board_handling.get_routing_board().get_traces();
            boolean free_angle_traces_found = false;
            for (board.Trace curr_trace : trace_list) {
                if (curr_trace instanceof board.PolylineTrace) {
                    if (!((board.PolylineTrace) curr_trace).polyline().is_orthogonal()) {
                        free_angle_traces_found = true;
                        break;
                    }
                }
            }
            if (free_angle_traces_found) {
                ResourceBundle resources
                        = ResourceBundle.getBundle("gui.resources.WindowRouteParameter", current_locale);
                String curr_message = resources.getString("change_snap_angle_90");
                if (!WindowMessage.confirm(curr_message)) {
                    refresh();
                    return;
                }
            }
            board_handling.set_current_snap_angle(board.AngleRestriction.NINETY_DEGREE);
        }
    }

    private class SnapAngle45Listener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            if (board_handling.get_routing_board().rules.get_trace_angle_restriction() == board.AngleRestriction.FORTYFIVE_DEGREE) {
                return;
            }
            Collection<board.Trace> trace_list = board_handling.get_routing_board().get_traces();
            boolean free_angle_traces_found = false;
            for (board.Trace curr_trace : trace_list) {
                if (curr_trace instanceof board.PolylineTrace) {
                    if (!((board.PolylineTrace) curr_trace).polyline().is_multiple_of_45_degree()) {
                        free_angle_traces_found = true;
                        break;
                    }
                }
            }
            if (free_angle_traces_found) {
                ResourceBundle resources
                        = ResourceBundle.getBundle("gui.resources.WindowRouteParameter", current_locale);
                String curr_message = resources.getString("change_snap_angle_45");
                if (!WindowMessage.confirm(curr_message)) {
                    refresh();
                    return;
                }
            }
            board_handling.set_current_snap_angle(board.AngleRestriction.FORTYFIVE_DEGREE);
        }
    }

    private class DetailListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            if (first_time) {
                java.awt.Point location = getLocation();
                detail_window.setLocation((int) location.getX() + 200, (int) location.getY() + 300);
                first_time = false;
            }
            detail_window.setVisible(true);
        }
        private boolean first_time = true;
    }

    private class AutomaticTraceWidthListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            manual_rule_window.setVisible(false);
            board_handling.settings.set_manual_tracewidth_selection(false);
        }
    }

    private class ManualTraceWidthListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            if (first_time) {
                java.awt.Point location = getLocation();
                manual_rule_window.setLocation((int) location.getX() + 200, (int) location.getY() + 200);
                first_time = false;
            }
            manual_rule_window.setVisible(true);
            board_handling.settings.set_manual_tracewidth_selection(true);
        }

        boolean first_time = true;
    }

    private class RestrictPinExitDirectionsListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            if (restrict_pin_exit_directions_check_box.isSelected()) {
                rules.BoardRules board_rules = board_handling.get_routing_board().rules;
                double edge_to_turn_dist
                        = board_handling.coordinate_transform.board_to_user(board_rules.get_min_trace_half_width());
                board_handling.set_pin_edge_to_turn_dist(edge_to_turn_dist);
            } else {
                board_handling.set_pin_edge_to_turn_dist(0);
            }
            refresh();
        }
    }

    private class EdgeToTurnDistFieldListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            try {
                String input = edge_to_turn_dist_field.getText();
                float input_value = Float.parseFloat(input);
                if (input_value <= 0) {
                    throw new NumberFormatException();
                }
                board_handling.set_pin_edge_to_turn_dist(input_value);
                restrict_pin_exit_directions_check_box.setSelected(input_value > 0);
                refresh();
            } catch (NumberFormatException ex) {
                // restore the text field.
                double edge_to_turn_dist = board_handling.get_routing_board().rules.get_pin_edge_to_turn_dist();
                edge_to_turn_dist = board_handling.coordinate_transform.board_to_user(edge_to_turn_dist);
                edge_to_turn_dist_field.setValue(edge_to_turn_dist);
            }

        }
    }

    private class RegionWidthFieldListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            try {
                String input = region_width_field.getText();
                int input_value = Integer.parseInt(input);
                if (input_value < 0 || input_value > c_max_slider_value) {
                    throw new NumberFormatException();
                }
                set_pull_tight_region_width(input_value);
            } catch (NumberFormatException ex) {
                // restore the text field.
                region_width_field.setValue(region_slider.getValue());
            }
        }
    }

}
