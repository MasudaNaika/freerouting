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
 * ViaTablePanel.java
 *
 * Created on 4. April 2005, 07:05
 */
package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import rules.BoardRules;
import rules.ViaInfo;
import rules.ViaInfos;

/**
 * Edit window for the table of available vias.
 *
 * @author Alfons Wirtz
 */
public class WindowEditVias extends BoardSavableSubWindow {

    /**
     * Creates a new instance of ViaTablePanel
     */
    public WindowEditVias(BoardFrame p_board_frame) {
        resources = ResourceBundle.getBundle("gui.resources.WindowEditVias", p_board_frame.get_locale());
        setTitle(resources.getString("title"));

        board_frame = p_board_frame;

        main_panel = new JPanel();
        main_panel.setLayout(new BorderLayout());

        cl_class_combo_box = new JComboBox();
        padstack_combo_box = new JComboBox();
        add_combobox_items();

        add_table();

        JPanel via_info_button_panel = new JPanel();
        via_info_button_panel.setLayout(new FlowLayout());
        main_panel.add(via_info_button_panel, BorderLayout.SOUTH);
        final JButton add_via_button = new JButton(resources.getString("add"));
        add_via_button.setToolTipText(resources.getString("add_tooltip"));
        add_via_button.addActionListener(new AddViaListener());
        via_info_button_panel.add(add_via_button);
        final JButton remove_via_button = new JButton(resources.getString("remove"));
        remove_via_button.setToolTipText(resources.getString("remove_tooltip"));
        remove_via_button.addActionListener(new RemoveViaListener());
        via_info_button_panel.add(remove_via_button);

        p_board_frame.set_context_sensitive_help(this, "WindowVia_EditVia");

        add(main_panel);
        pack();
    }

    /**
     * Recalculates all values displayed in the parent window
     */
    @Override
    public void refresh() {
        padstack_combo_box.removeAllItems();
        cl_class_combo_box.removeAllItems();
        add_combobox_items();
        table_model.set_values();
    }

    private void add_table() {
        table_model = new ViaTableModel();
        table = new JTable(table_model);
        scroll_pane = new JScrollPane(table);
        int table_height = TEXTFIELD_HEIGHT * table_model.getRowCount();
        int table_width = TEXTFIELD_WIDTH * table_model.getColumnCount();
        table.setPreferredScrollableViewportSize(new Dimension(table_width, table_height));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        main_panel.add(scroll_pane, BorderLayout.CENTER);

        table.getColumnModel().getColumn(ColumnName.CLEARANCE_CLASS.ordinal()).setCellEditor(new DefaultCellEditor(cl_class_combo_box));

        table.getColumnModel().getColumn(ColumnName.PADSTACK.ordinal()).setCellEditor(new DefaultCellEditor(padstack_combo_box));
    }

    private void add_combobox_items() {
        board.RoutingBoard routing_board = board_frame.board_panel.board_handling.get_routing_board();
        for (int i = 0; i < routing_board.rules.clearance_matrix.get_class_count(); ++i) {
            cl_class_combo_box.addItem(routing_board.rules.clearance_matrix.get_name(i));
        }
        for (int i = 0; i < routing_board.library.via_padstack_count(); ++i) {
            padstack_combo_box.addItem(routing_board.library.get_via_padstack(i).name);
        }
    }

    /**
     * Adjusts the displayed window with the via table after the size of the
     * table has been changed.
     */
    private void adjust_table() {
        table_model = new ViaTableModel();
        table = new JTable(table_model);
        main_panel.remove(scroll_pane);
        add_table();
        pack();
        board_frame.refresh_windows();
    }

    private final BoardFrame board_frame;

    private final JPanel main_panel;

    private JScrollPane scroll_pane;
    private JTable table;
    private ViaTableModel table_model;

    private final JComboBox cl_class_combo_box;
    private final JComboBox padstack_combo_box;

    private final ResourceBundle resources;

    private static final int TEXTFIELD_HEIGHT = 16;
    private static final int TEXTFIELD_WIDTH = 100;

    private class AddViaListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            board.RoutingBoard routing_board = board_frame.board_panel.board_handling.get_routing_board();
            ViaInfos via_infos = routing_board.rules.via_infos;
            Integer no = 1;
            String new_name = null;
            final String name_start = resources.getString("new_via");
            while (true) {
                new_name = name_start + no.toString();
                if (!via_infos.name_exists(new_name)) {
                    break;
                }
                ++no;
            }
            rules.NetClass default_net_class = routing_board.rules.get_default_net_class();
            ViaInfo new_via = new ViaInfo(new_name, routing_board.library.get_via_padstack(0),
                    default_net_class.default_item_clearance_classes.get(rules.DefaultItemClearanceClasses.ItemClass.VIA),
                    false, routing_board.rules);
            via_infos.add(new_via);
            adjust_table();
        }
    }

    private class RemoveViaListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            if (table_model.getRowCount() <= 1) {
                board_frame.screen_messages.set_status_message(resources.getString("message_1"));
                return;
            }
            int selected_row = table.getSelectedRow();
            if (selected_row < 0) {
                return;
            }
            Object via_name = table_model.getValueAt(selected_row, ColumnName.NAME.ordinal());
            if (!(via_name instanceof String)) {
                return;
            }
            BoardRules board_rules = board_frame.board_panel.board_handling.get_routing_board().rules;
            ViaInfo via_info = board_rules.via_infos.get((String) via_name);
            // Check, if via_info is used in a via rule.
            for (rules.ViaRule curr_rule : board_rules.via_rules) {
                if (curr_rule.contains(via_info)) {
                    String message = resources.getString("message_2") + " " + curr_rule.name;
                    board_frame.screen_messages.set_status_message(message);
                    return;
                }
            }
            if (board_rules.via_infos.remove(via_info)) {
                adjust_table();
                String message = resources.getString("via") + "via " + via_info.get_name() + " "
                        + resources.getString("removed");
                board_frame.screen_messages.set_status_message(message);
            }
        }
    }

    /**
     * Table model of the via table.
     */
    private class ViaTableModel extends AbstractTableModel {

        public ViaTableModel() {
            column_names = new String[ColumnName.values().length];

            for (int i = 0; i < column_names.length; ++i) {
                column_names[i] = resources.getString((ColumnName.values()[i]).toString());
            }
            rules.BoardRules board_rules = board_frame.board_panel.board_handling.get_routing_board().rules;
            data = new Object[board_rules.via_infos.count()][];
            for (int i = 0; i < data.length; ++i) {
                data[i] = new Object[ColumnName.values().length];
            }
            set_values();
        }

        /**
         * Calculates the the valus in this table
         */
        public void set_values() {
            rules.BoardRules board_rules = board_frame.board_panel.board_handling.get_routing_board().rules;
            for (int i = 0; i < data.length; ++i) {
                ViaInfo curr_via = board_rules.via_infos.get(i);
                data[i][ColumnName.NAME.ordinal()] = curr_via.get_name();
                data[i][ColumnName.PADSTACK.ordinal()] = curr_via.get_padstack().name;
                data[i][ColumnName.CLEARANCE_CLASS.ordinal()] = board_rules.clearance_matrix.get_name(curr_via.get_clearance_class());
                data[i][ColumnName.ATTACH_SMD.ordinal()] = curr_via.attach_smd_allowed();
            }
        }

        @Override
        public String getColumnName(int p_col) {
            return column_names[p_col];
        }

        @Override
        public int getRowCount() {
            return data.length;
        }

        @Override
        public int getColumnCount() {
            return column_names.length;
        }

        @Override
        public Object getValueAt(int p_row, int p_col) {
            return data[p_row][p_col];
        }

        @Override
        public void setValueAt(Object p_value, int p_row, int p_col) {
            board.RoutingBoard routing_board = board_frame.board_panel.board_handling.get_routing_board();
            BoardRules board_rules = routing_board.rules;
            Object via_name = getValueAt(p_row, ColumnName.NAME.ordinal());
            if (!(via_name instanceof String)) {
                Freerouter.logInfo("ViaVindow.setValueAt: String expected");
                return;
            }
            ViaInfo via_info = board_rules.via_infos.get((String) via_name);
            if (via_info == null) {
                Freerouter.logInfo("ViaVindow.setValueAt: via_info not found");
                return;
            }

            if (p_col == ColumnName.NAME.ordinal()) {
                if (!(p_value instanceof String)) {
                    return;
                }
                String new_name = (String) p_value;
                if (board_rules.via_infos.name_exists(new_name)) {
                    return;
                }
                via_info.set_name(new_name);
                board_frame.via_window.refresh();
            } else if (p_col == ColumnName.PADSTACK.ordinal()) {
                if (!(p_value instanceof String)) {
                    return;
                }
                String new_name = (String) p_value;
                library.Padstack new_padstack = routing_board.library.get_via_padstack(new_name);
                if (new_padstack == null) {
                    Freerouter.logInfo("ViaVindow.setValueAt: via padstack not found");
                    return;
                }
                via_info.set_padstack(new_padstack);
            } else if (p_col == ColumnName.CLEARANCE_CLASS.ordinal()) {
                if (!(p_value instanceof String)) {
                    return;
                }
                String new_name = (String) p_value;
                int new_cl_class_index = board_rules.clearance_matrix.get_no(new_name);
                {
                    if (new_cl_class_index < 0) {
                        Freerouter.logInfo("ViaVindow.setValueAt: clearance class not found");
                        return;
                    }
                }
                via_info.set_clearance_class(new_cl_class_index);
            } else if (p_col == ColumnName.ATTACH_SMD.ordinal()) {
                if (!(p_value instanceof Boolean)) {
                    Freerouter.logInfo("ViaVindow.setValueAt: Boolean expected");
                    return;
                }
                Boolean attach_smd = (Boolean) p_value;
                via_info.set_attach_smd_allowed(attach_smd);
            }
            data[p_row][p_col] = p_value;
            fireTableCellUpdated(p_row, p_col);
        }

        @Override
        public boolean isCellEditable(int p_row, int p_col) {
            return true;
        }

        @Override
        public Class<?> getColumnClass(int p_col) {
            return getValueAt(0, p_col).getClass();
        }

        private Object[][] data = null;
        private String[] column_names = null;
    }

    private enum ColumnName {
        NAME, PADSTACK, CLEARANCE_CLASS, ATTACH_SMD
    }
}
