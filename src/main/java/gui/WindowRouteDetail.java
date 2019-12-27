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
 * RouteDetailWindow.java
 *
 * Created on 18. November 2004, 07:31
 */
package gui;

import board.BoardOutline;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Window handling detail parameters of the interactive routing.
 *
 * @author Alfons Wirtz
 */
public class WindowRouteDetail extends BoardSavableSubWindow {

    /**
     * Creates a new instance of RouteDetailWindow
     */
    public WindowRouteDetail(BoardFrame p_board_frame) {
        board_handling = p_board_frame.board_panel.board_handling;
        ResourceBundle resources
                = ResourceBundle.getBundle("gui.resources.WindowRouteDetail", p_board_frame.get_locale());
        setTitle(resources.getString("title"));

        // create main panel
        final JPanel main_panel = new JPanel();
        getContentPane().add(main_panel);
        GridBagLayout gridbag = new GridBagLayout();
        main_panel.setLayout(gridbag);
        GridBagConstraints gridbag_constraints = new GridBagConstraints();
        gridbag_constraints.anchor = GridBagConstraints.WEST;
        gridbag_constraints.insets = new Insets(5, 10, 5, 10);

        // add label and button group for the clearance compensation.
        JLabel clearance_compensation_label = new JLabel(resources.getString("clearance_compensation"));
        clearance_compensation_label.setToolTipText(resources.getString("clearance_compensation_tooltip"));

        gridbag_constraints.gridwidth = GridBagConstraints.RELATIVE;
        gridbag_constraints.gridheight = 2;
        gridbag.setConstraints(clearance_compensation_label, gridbag_constraints);
        main_panel.add(clearance_compensation_label);

        on_button = new JRadioButton(resources.getString("on"));
        off_button = new JRadioButton(resources.getString("off"));

        on_button.addActionListener(ae
                -> board_handling.set_clearance_compensation(true));
        off_button.addActionListener(ae
                -> board_handling.set_clearance_compensation(false));

        ButtonGroup clearance_compensation_button_group = new ButtonGroup();
        clearance_compensation_button_group.add(on_button);
        clearance_compensation_button_group.add(off_button);
        off_button.setSelected(true);

        gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridbag_constraints.gridheight = 1;
        gridbag.setConstraints(on_button, gridbag_constraints);
        main_panel.add(on_button, gridbag_constraints);
        gridbag.setConstraints(off_button, gridbag_constraints);
        main_panel.add(off_button, gridbag_constraints);

        JLabel separator = new JLabel("  ----------------------------------------  ");
        gridbag.setConstraints(separator, gridbag_constraints);
        main_panel.add(separator, gridbag_constraints);

        // add label and slider for the pull tight accuracy.
        JLabel pull_tight_accuracy_label = new JLabel(resources.getString("pull_tight_accuracy"));
        pull_tight_accuracy_label.setToolTipText(resources.getString("pull_tight_accuracy_tooltip"));
        gridbag_constraints.insets = new Insets(5, 10, 5, 10);
        gridbag.setConstraints(pull_tight_accuracy_label, gridbag_constraints);
        main_panel.add(pull_tight_accuracy_label);

        accuracy_slider = new JSlider();
        accuracy_slider.setMaximum(c_max_slider_value);
        accuracy_slider.addChangeListener(evt -> {
            int new_accurracy = (c_max_slider_value - accuracy_slider.getValue() + 1) * c_accuracy_scale_factor;
            board_handling.settings.set_current_pull_tight_accuracy(new_accurracy);
        });
        gridbag.setConstraints(accuracy_slider, gridbag_constraints);
        main_panel.add(accuracy_slider);

        separator = new JLabel("  ----------------------------------------  ");
        gridbag.setConstraints(separator, gridbag_constraints);
        main_panel.add(separator, gridbag_constraints);

        // add switch to define, if keepout is generated outside the outline.
        outline_keepout_check_box = new JCheckBox(resources.getString("keepout_outside_outline"));
        outline_keepout_check_box.setSelected(false);
        outline_keepout_check_box.addActionListener(ae -> {
            if (board_handling.is_board_read_only()) {
                return;
            }
            BoardOutline outline = board_handling.get_routing_board().get_outline();
            if (outline != null) {
                outline.generate_keepout_outside(outline_keepout_check_box.isSelected());
            }
        });
        gridbag.setConstraints(outline_keepout_check_box, gridbag_constraints);
        outline_keepout_check_box.setToolTipText(resources.getString("keepout_outside_outline_tooltip"));
        main_panel.add(outline_keepout_check_box, gridbag_constraints);

        separator = new JLabel();
        gridbag.setConstraints(separator, gridbag_constraints);
        main_panel.add(separator, gridbag_constraints);

        refresh();
        pack();
        setResizable(false);
    }

    /**
     * Recalculates all displayed values
     */
    @Override
    public void refresh() {
        if (board_handling.get_routing_board().search_tree_manager.is_clearance_compensation_used()) {
            on_button.setSelected(true);
        } else {
            off_button.setSelected(true);
        }
        BoardOutline outline = board_handling.get_routing_board().get_outline();
        if (outline != null) {
            outline_keepout_check_box.setSelected(outline.keepout_outside_outline_generated());
        }
        int accuracy_slider_value = c_max_slider_value - board_handling.settings.get_trace_pull_tight_accuracy() / c_accuracy_scale_factor + 1;
        accuracy_slider.setValue(accuracy_slider_value);
    }
    
    private final interactive.BoardHandling board_handling;
    private final JSlider accuracy_slider;
    private final JRadioButton on_button;
    private final JRadioButton off_button;
    private final JCheckBox outline_keepout_check_box;
    private static final int c_max_slider_value = 100;
    private static final int c_accuracy_scale_factor = 20;

}
