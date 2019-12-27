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
 * WindowSnapshotSettings.java
 *
 * Created on 17. September 2005, 07:23
 *
 */
package gui;

import interactive.SnapShot;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.*;

/**
 * Window for the settinngs of interactive snapshots.
 *
 * @author Alfons Wirtz
 */
public class WindowSnapshotSettings extends BoardSavableSubWindow {

    /**
     * Creates a new instance of WindowSnapshotSettings
     */
    public WindowSnapshotSettings(BoardFrame p_board_frame) {
        board_handling = p_board_frame.board_panel.board_handling;

        ResourceBundle resources
                = ResourceBundle.getBundle("gui.resources.WindowSnapshotSettings", p_board_frame.get_locale());
        setTitle(resources.getString("title"));

        // create main panel
        final JPanel main_panel = new JPanel();
        getContentPane().add(main_panel);
        GridBagLayout gridbag = new GridBagLayout();
        main_panel.setLayout(gridbag);
        GridBagConstraints gridbag_constraints = new GridBagConstraints();
        gridbag_constraints.anchor = GridBagConstraints.WEST;
        gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridbag_constraints.insets = new Insets(1, 10, 1, 10);

        SnapShot.Attributes attr = board_handling.settings.get_snapshot_attributes();
        // add check box for the object colors
        object_color_check_box = new JCheckBox(resources.getString("object_colors"));
        gridbag.setConstraints(object_color_check_box, gridbag_constraints);
        main_panel.add(object_color_check_box, gridbag_constraints);
        object_color_check_box.addActionListener(ae -> {
            attr.object_colors = object_color_check_box.isSelected();
        });

        // add check box for the object visibility
        object_visibility_check_box = new JCheckBox(resources.getString("object_visibility"));
        gridbag.setConstraints(object_visibility_check_box, gridbag_constraints);
        main_panel.add(object_visibility_check_box, gridbag_constraints);
        object_visibility_check_box.addActionListener(ae -> {
            attr.object_visibility = object_visibility_check_box.isSelected();
        });

        // add check box for the layer visibility
        layer_visibility_check_box = new JCheckBox(resources.getString("layer_visibility"));
        gridbag.setConstraints(layer_visibility_check_box, gridbag_constraints);
        main_panel.add(layer_visibility_check_box, gridbag_constraints);
        layer_visibility_check_box.addActionListener(ae -> {
            attr.layer_visibility = layer_visibility_check_box.isSelected();
        });

        // add check box for display region
        display_region_check_box = new JCheckBox(resources.getString("display_region"));
        gridbag.setConstraints(display_region_check_box, gridbag_constraints);
        main_panel.add(display_region_check_box, gridbag_constraints);
        display_region_check_box.addActionListener(ae -> {
            attr.display_region = display_region_check_box.isSelected();
        });

        JLabel separator = new JLabel("  ----------------------------------------  ");
        gridbag.setConstraints(separator, gridbag_constraints);
        main_panel.add(separator, gridbag_constraints);

        // add check box for the interactive state
        interactive_state_check_box = new JCheckBox(resources.getString("interactive_state"));
        gridbag.setConstraints(interactive_state_check_box, gridbag_constraints);
        main_panel.add(interactive_state_check_box, gridbag_constraints);
        interactive_state_check_box.addActionListener(ae -> {
            attr.interactive_state = interactive_state_check_box.isSelected();
        });

        separator = new JLabel("  ----------------------------------------  ");
        gridbag.setConstraints(separator, gridbag_constraints);
        main_panel.add(separator, gridbag_constraints);

        // add check box for the selection layers
        selection_layers_check_box = new JCheckBox(resources.getString("selection_layers"));
        gridbag.setConstraints(selection_layers_check_box, gridbag_constraints);
        main_panel.add(selection_layers_check_box, gridbag_constraints);
        selection_layers_check_box.addActionListener(ae -> {
            attr.selection_layers = selection_layers_check_box.isSelected();
        });

        // add check box for the selectable items
        selectable_items_check_box = new JCheckBox(resources.getString("selectable_items"));
        gridbag.setConstraints(selectable_items_check_box, gridbag_constraints);
        main_panel.add(selectable_items_check_box, gridbag_constraints);
        selectable_items_check_box.addActionListener(ae -> {
            attr.selectable_items = selectable_items_check_box.isSelected();
        });

        // add check box for the current layer
        current_layer_check_box = new JCheckBox(resources.getString("current_layer"));
        gridbag.setConstraints(current_layer_check_box, gridbag_constraints);
        main_panel.add(current_layer_check_box, gridbag_constraints);
        current_layer_check_box.addActionListener(ae -> {
            attr.current_layer = current_layer_check_box.isSelected();
        });

        separator = new JLabel("  ----------------------------------------  ");
        gridbag.setConstraints(separator, gridbag_constraints);
        main_panel.add(separator, gridbag_constraints);

        // add check box for the rule selection
        rule_selection_check_box = new JCheckBox(resources.getString("rule_selection"));
        gridbag.setConstraints(rule_selection_check_box, gridbag_constraints);
        main_panel.add(rule_selection_check_box, gridbag_constraints);
        rule_selection_check_box.addActionListener(ae -> {
            attr.rule_selection = rule_selection_check_box.isSelected();
        });

        // add check box for the manual rule settings
        manual_rule_settings_check_box = new JCheckBox(resources.getString("manual_rule_settings"));
        gridbag.setConstraints(manual_rule_settings_check_box, gridbag_constraints);
        main_panel.add(manual_rule_settings_check_box, gridbag_constraints);
        manual_rule_settings_check_box.addActionListener(ae -> {
            attr.manual_rule_settings = manual_rule_settings_check_box.isSelected();
        });

        // add check box for push and shove enabled
        push_and_shove_enabled_check_box = new JCheckBox(resources.getString("push&shove_enabled"));
        gridbag.setConstraints(push_and_shove_enabled_check_box, gridbag_constraints);
        main_panel.add(push_and_shove_enabled_check_box, gridbag_constraints);
        push_and_shove_enabled_check_box.addActionListener(ae -> {
            attr.push_and_shove_enabled = push_and_shove_enabled_check_box.isSelected();
        });

        // add check box for drag components enabled
        drag_components_enabled_check_box = new JCheckBox(resources.getString("drag_components_enabled"));
        gridbag.setConstraints(drag_components_enabled_check_box, gridbag_constraints);
        main_panel.add(drag_components_enabled_check_box, gridbag_constraints);
        drag_components_enabled_check_box.addActionListener(ae -> {
            attr.drag_components_enabled = drag_components_enabled_check_box.isSelected();
        });

        // add check box for the pull tight region
        pull_tight_region_check_box = new JCheckBox(resources.getString("pull_tight_region"));
        gridbag.setConstraints(pull_tight_region_check_box, gridbag_constraints);
        main_panel.add(pull_tight_region_check_box, gridbag_constraints);
        pull_tight_region_check_box.addActionListener(ae -> {
            attr.pull_tight_region = pull_tight_region_check_box.isSelected();
        });

        separator = new JLabel("  ----------------------------------------  ");
        gridbag.setConstraints(separator, gridbag_constraints);
        main_panel.add(separator, gridbag_constraints);

        // add check box for the component grid
        component_grid_check_box = new JCheckBox(resources.getString("component_grid"));
        gridbag.setConstraints(component_grid_check_box, gridbag_constraints);
        main_panel.add(component_grid_check_box, gridbag_constraints);
        component_grid_check_box.addActionListener(ae -> {
            attr.component_grid = component_grid_check_box.isSelected();
        });

        separator = new JLabel("  ----------------------------------------  ");
        gridbag.setConstraints(separator, gridbag_constraints);
        main_panel.add(separator, gridbag_constraints);

        // add check box for the info list filters
        info_list_filter_check_box = new JCheckBox(resources.getString("info_list_selections"));
        gridbag.setConstraints(info_list_filter_check_box, gridbag_constraints);
        main_panel.add(info_list_filter_check_box, gridbag_constraints);
        info_list_filter_check_box.addActionListener(ae -> {
            attr.info_list_selections = info_list_filter_check_box.isSelected();
        });

        p_board_frame.set_context_sensitive_help(this, "WindowSnapshots_SnapshotSettings");

        refresh();
        pack();
        setResizable(false);
    }

    /**
     * Recalculates all displayed values
     */
    @Override
    public void refresh() {
        interactive.SnapShot.Attributes attributes = board_handling.settings.get_snapshot_attributes();
        object_color_check_box.setSelected(attributes.object_colors);
        object_visibility_check_box.setSelected(attributes.object_visibility);
        layer_visibility_check_box.setSelected(attributes.layer_visibility);
        display_region_check_box.setSelected(attributes.display_region);
        interactive_state_check_box.setSelected(attributes.interactive_state);
        selection_layers_check_box.setSelected(attributes.selection_layers);
        selectable_items_check_box.setSelected(attributes.selectable_items);
        current_layer_check_box.setSelected(attributes.current_layer);
        rule_selection_check_box.setSelected(attributes.rule_selection);
        manual_rule_settings_check_box.setSelected(attributes.manual_rule_settings);
        push_and_shove_enabled_check_box.setSelected(attributes.push_and_shove_enabled);
        drag_components_enabled_check_box.setSelected(attributes.drag_components_enabled);
        pull_tight_region_check_box.setSelected(attributes.pull_tight_region);
        component_grid_check_box.setSelected(attributes.component_grid);
        info_list_filter_check_box.setSelected(attributes.info_list_selections);
    }

    private final interactive.BoardHandling board_handling;

    final JCheckBox object_color_check_box;
    final JCheckBox object_visibility_check_box;
    final JCheckBox layer_visibility_check_box;
    final JCheckBox display_region_check_box;
    final JCheckBox interactive_state_check_box;
    final JCheckBox selection_layers_check_box;
    final JCheckBox selectable_items_check_box;
    final JCheckBox current_layer_check_box;
    final JCheckBox rule_selection_check_box;
    final JCheckBox manual_rule_settings_check_box;
    final JCheckBox push_and_shove_enabled_check_box;
    final JCheckBox drag_components_enabled_check_box;
    final JCheckBox pull_tight_region_check_box;
    final JCheckBox component_grid_check_box;
    final JCheckBox info_list_filter_check_box;

}
