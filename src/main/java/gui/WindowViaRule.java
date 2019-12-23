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
 * ViaRuleWindow.java
 *
 * Created on 5. April 2005, 06:29
 */
package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.*;
import rules.ViaInfo;
import rules.ViaInfos;
import rules.ViaRule;

/**
 * Window for editing a single via rule.
 *
 * @author Alfons Wirtz
 */
public class WindowViaRule extends JFrame {

    /**
     * Creates a new instance of ViaRuleWindow
     */
    public WindowViaRule(ViaRule p_via_rule, ViaInfos p_via_list, BoardFrame p_board_frame) {

        Freerouter.setWindowIcon(this);

        via_rule = p_via_rule;
        via_list = p_via_list;

        resources = ResourceBundle.getBundle("gui.resources.WindowViaRule", p_board_frame.get_locale());
        setTitle(resources.getString("title") + " " + p_via_rule.name);

        main_panel = new JPanel();
        main_panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        main_panel.setLayout(new BorderLayout());

        rule_list_model = new DefaultListModel();
        rule_list = new JList(rule_list_model);

        rule_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rule_list.setVisibleRowCount(10);
        JScrollPane list_scroll_pane = new JScrollPane(rule_list);
        list_scroll_pane.setPreferredSize(new Dimension(200, 100));
        main_panel.add(list_scroll_pane, BorderLayout.CENTER);

        // fill the list
        for (int i = 0; i < p_via_rule.via_count(); ++i) {
            rule_list_model.addElement(p_via_rule.get_via(i));
        }

        // Add a panel with buttons for editing the via list.
        JPanel button_panel = new JPanel();
        main_panel.add(button_panel, BorderLayout.SOUTH);
        GridBagLayout gridbag = new GridBagLayout();
        button_panel.setLayout(gridbag);
        GridBagConstraints gridbag_constraints = new GridBagConstraints();

        final JButton add_button = new JButton(resources.getString("append"));
        gridbag_constraints.gridwidth = GridBagConstraints.RELATIVE;
        gridbag.setConstraints(add_button, gridbag_constraints);
        add_button.setToolTipText(resources.getString("append_tooltip"));
        add_button.addActionListener(new AppendListener());
        button_panel.add(add_button);

        final JButton delete_button = new JButton(resources.getString("remove"));
        gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(delete_button, gridbag_constraints);
        delete_button.setToolTipText(resources.getString("remove_tooltip"));
        delete_button.addActionListener(new DeleteListener());
        button_panel.add(delete_button);

        final JButton move_up_button = new JButton(resources.getString("move_up"));
        gridbag_constraints.gridwidth = GridBagConstraints.RELATIVE;
        gridbag.setConstraints(move_up_button, gridbag_constraints);
        move_up_button.setToolTipText(resources.getString("move_up_tooltip"));
        move_up_button.addActionListener(new MoveUpListener());
        button_panel.add(move_up_button);

        final JButton move_down_button = new JButton(resources.getString("move_down"));
        gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(move_down_button, gridbag_constraints);
        move_down_button.setToolTipText(resources.getString("move_down_tooltip"));
        move_down_button.addActionListener(new MoveDownListener());
        button_panel.add(move_down_button);

        p_board_frame.set_context_sensitive_help(this, "WindowVia_EditViaRule");

        add(main_panel);
        pack();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    /**
     * Swaps the position of the vias with index p_1 and p_2.
     */
    private void swap_position(int p_1, int p_2) {
        ViaInfo via_1 = (ViaInfo) rule_list_model.get(p_1);
        ViaInfo via_2 = (ViaInfo) rule_list_model.get(p_2);
        if (via_1 == null || via_2 == null) {
            return;
        }
        rule_list_model.set(p_1, via_2);
        rule_list_model.set(p_2, via_1);
        via_rule.swap(via_1, via_2);
    }

    private final ViaRule via_rule;

    /**
     * the list of possible vias in a rule
     */
    private final ViaInfos via_list;

    private final JPanel main_panel;

    private final JList rule_list;
    private final DefaultListModel rule_list_model;

    private final ResourceBundle resources;

    private class AppendListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            Object[] possible_values = new Object[via_list.count() - via_rule.via_count()];
            if (possible_values.length <= 0) {
                return;
            }
            int curr_index = 0;
            for (int i = 0; i < via_list.count(); ++i) {
                ViaInfo curr_via = via_list.get(i);
                if (!via_rule.contains(curr_via)) {
                    if (curr_index >= possible_values.length) {
                        System.out.println("ViaRuleWindow.AppendListener.actionPerformed: index inconsistent");
                        break;
                    }
                    possible_values[curr_index] = curr_via;
                    ++curr_index;
                }
            }
            assert (curr_index == possible_values.length);
            Object selected_value = JOptionPane.showInputDialog(null,
                    resources.getString("choose_via_to_append"), resources.getString("append_via_to_rule"),
                    JOptionPane.INFORMATION_MESSAGE, null, possible_values, possible_values[0]);
            if (selected_value != null) {
                ViaInfo selected_via = (ViaInfo) selected_value;
                via_rule.append_via(selected_via);
                rule_list_model.addElement(selected_via);
            }
        }
    }

    private class DeleteListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            ViaInfo selected_via = (ViaInfo) rule_list.getSelectedValue();
            if (selected_via != null) {
                String message = resources.getString("remove_2") + " " + selected_via.get_name()
                        + " " + resources.getString("from_the_rule") + " " + via_rule.name + "?";
                if (WindowMessage.confirm(message)) {
                    rule_list_model.removeElement(selected_via);
                    via_rule.remove_via(selected_via);
                }
            }
        }
    }

    private class MoveUpListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            int selected_index = rule_list.getSelectedIndex();
            if (selected_index <= 0) {
                return;
            }
            swap_position(selected_index - 1, selected_index);
            rule_list.setSelectedIndex(selected_index - 1);
        }
    }

    private class MoveDownListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            int selected_index = rule_list.getSelectedIndex();
            if (selected_index < 0 || selected_index >= rule_list_model.getSize() - 1) {
                return;
            }
            swap_position(selected_index, selected_index + 1);
            rule_list.setSelectedIndex(selected_index + 1);
        }
    }
}
