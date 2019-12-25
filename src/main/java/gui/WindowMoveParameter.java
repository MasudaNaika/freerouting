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
 * WindowMoveParameter.java
 *
 * Created on 16. September 2005, 06:53
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
 * Window with the parameters for moving components.
 *
 * @author Alfons Wirtz
 */
public class WindowMoveParameter extends BoardSavableSubWindow {

    /**
     * Creates a new instance of WindowMoveParameter
     */
    public WindowMoveParameter(BoardFrame p_board_frame) {
        board_handling = p_board_frame.board_panel.board_handling;
        ResourceBundle resources
                = ResourceBundle.getBundle("gui.resources.WindowMoveParameter", p_board_frame.get_locale());
        setTitle(resources.getString("title"));

        // create main panel
        final JPanel main_panel = new JPanel();
        add(main_panel);
        GridBagLayout gridbag = new GridBagLayout();
        main_panel.setLayout(gridbag);
        GridBagConstraints gridbag_constraints = new GridBagConstraints();
        gridbag_constraints.anchor = GridBagConstraints.WEST;
        gridbag_constraints.insets = new Insets(1, 10, 1, 10);

        // Create label and number field for the horizontal and verical component grid
        gridbag_constraints.gridwidth = 2;
        JLabel horizontal_grid_label = new JLabel(resources.getString("horizontal_component_grid"));
        gridbag.setConstraints(horizontal_grid_label, gridbag_constraints);
        main_panel.add(horizontal_grid_label);

        NumberFormat number_format = NumberFormat.getInstance(p_board_frame.get_locale());
        number_format.setMaximumFractionDigits(7);
        horizontal_grid_field = new JFormattedTextField(number_format);
        horizontal_grid_field.setColumns(5);
        gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(horizontal_grid_field, gridbag_constraints);
        main_panel.add(horizontal_grid_field);
        set_horizontal_grid_field(board_handling.settings.get_horizontal_component_grid());
        horizontal_grid_field.addActionListener(new HorizontalGridFieldListener());

        gridbag_constraints.gridwidth = 2;
        JLabel vertical_grid_label = new JLabel(resources.getString("vertical_component_grid"));
        gridbag.setConstraints(vertical_grid_label, gridbag_constraints);
        main_panel.add(vertical_grid_label);

        vertical_grid_field = new JFormattedTextField(number_format);
        vertical_grid_field.setColumns(5);
        gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(vertical_grid_field, gridbag_constraints);
        main_panel.add(vertical_grid_field);
        set_vertical_grid_field(board_handling.settings.get_vertical_component_grid());
        vertical_grid_field.addActionListener(new VerticalGridFieldListener());

        JLabel separator = new JLabel("  -----------------------------------------------  ");
        gridbag.setConstraints(separator, gridbag_constraints);
        main_panel.add(separator, gridbag_constraints);

        // add label and button group for the wheel function.
        JLabel wheel_function_label = new JLabel(resources.getString("wheel_function"));
        gridbag_constraints.gridwidth = GridBagConstraints.RELATIVE;
        gridbag_constraints.gridheight = 2;
        gridbag.setConstraints(wheel_function_label, gridbag_constraints);
        main_panel.add(wheel_function_label);
        wheel_function_label.setToolTipText(resources.getString("wheel_function_tooltip"));

        zoom_button = new JRadioButton(resources.getString("zoom"));
        rotate_button = new JRadioButton(resources.getString("rotate"));

        zoom_button.addActionListener(new ZoomButtonListener());
        rotate_button.addActionListener(new RotateButtonListener());

        ButtonGroup button_group = new ButtonGroup();
        button_group.add(zoom_button);
        button_group.add(rotate_button);
        if (board_handling.settings.get_zoom_with_wheel()) {
            zoom_button.setSelected(true);
        } else {
            rotate_button.setSelected(true);
        }

        gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridbag_constraints.gridheight = 1;
        gridbag.setConstraints(zoom_button, gridbag_constraints);
        main_panel.add(zoom_button, gridbag_constraints);
        gridbag.setConstraints(rotate_button, gridbag_constraints);
        main_panel.add(rotate_button, gridbag_constraints);

        p_board_frame.set_context_sensitive_help(this, "WindowMoveParameter");

        refresh();
        pack();
        setResizable(false);
    }

    private void set_horizontal_grid_field(double p_value) {
        if (p_value <= 0) {
            horizontal_grid_field.setValue(0);
        } else {
            Float grid_width = (float) board_handling.coordinate_transform.board_to_user(p_value);
            horizontal_grid_field.setValue(grid_width);
        }
    }

    private void set_vertical_grid_field(double p_value) {
        if (p_value <= 0) {
            vertical_grid_field.setValue(0);
        } else {
            Float grid_width = (float) board_handling.coordinate_transform.board_to_user(p_value);
            vertical_grid_field.setValue(grid_width);
        }
    }

    private final interactive.BoardHandling board_handling;
    private final JFormattedTextField horizontal_grid_field;
    private final JFormattedTextField vertical_grid_field;
    private final JRadioButton zoom_button;
    private final JRadioButton rotate_button;

    private class HorizontalGridFieldListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String input = horizontal_grid_field.getText();
            try {
                double input_value = Double.parseDouble(input);
                if (input_value < 0) {
                    input_value = 0;
                }
                board_handling.settings.set_horizontal_component_grid((int) Math.round(board_handling.coordinate_transform.user_to_board(input_value)));
                set_horizontal_grid_field(board_handling.settings.get_horizontal_component_grid());
            } catch (NumberFormatException ex) {
                set_horizontal_grid_field(board_handling.settings.get_horizontal_component_grid());
            }
        }
    }

    private class VerticalGridFieldListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String input = vertical_grid_field.getText();
            try {
                double input_value = Double.parseDouble(input);
                if (input_value < 0) {
                    input_value = 0;
                }
                board_handling.settings.set_vertical_component_grid((int) Math.round(board_handling.coordinate_transform.user_to_board(input_value)));
                set_vertical_grid_field(board_handling.settings.get_vertical_component_grid());
            } catch (NumberFormatException ex) {
                // restore the text field.
                set_vertical_grid_field(board_handling.settings.get_vertical_component_grid());
            }

        }
    }

    private class ZoomButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            board_handling.settings.set_zoom_with_wheel(true);
        }
    }

    private class RotateButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            board_handling.settings.set_zoom_with_wheel(false);
        }
    }

}
