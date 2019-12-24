
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
 * AssignNetRulesWindow.java
 *
 * Created on 12. April 2005, 06:09
 */
package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import rules.Net;
import rules.NetClass;

/**
 *
 * @author Alfons Wirtz
 */
public class WindowAssignNetClass extends BoardSavableSubWindow {

    /**
     * Creates a new instance of AssignNetRulesWindow
     */
    public WindowAssignNetClass(BoardFrame p_board_frame) {
        resources = ResourceBundle.getBundle("gui.resources.WindowAssignNetClass", p_board_frame.get_locale());
        setTitle(resources.getString("title"));

        board_frame = p_board_frame;

        main_panel = new JPanel();
        main_panel.setLayout(new BorderLayout());

        table_model = new AssignRuleTableModel();
        table = new AssignRuleTable(table_model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scroll_pane = new JScrollPane(table);
        int table_height = TEXTFIELD_HEIGHT * Math.min(table_model.getRowCount(), 20);
        int table_width = TEXTFIELD_WIDTH * table_model.getColumnCount();
        table.setPreferredScrollableViewportSize(new Dimension(table_width, table_height));
        main_panel.add(scroll_pane, BorderLayout.CENTER);
        add_net_class_combo_box();

        p_board_frame.set_context_sensitive_help(this, "WindowNetClasses_AssignNetClass");

        add(main_panel);
        pack();
    }

    private void add_net_class_combo_box() {
        net_rule_combo_box = new JComboBox();
        board.RoutingBoard routing_board = board_frame.board_panel.board_handling.get_routing_board();
        for (int i = 0; i < routing_board.rules.net_classes.count(); ++i) {
            net_rule_combo_box.addItem(routing_board.rules.net_classes.get(i));
        }
        table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(net_rule_combo_box));
    }

    @Override
    public void refresh() {
        // Reinsert the net class column.
        for (int i = 0; i < table_model.getRowCount(); ++i) {
            table_model.setValueAt(((Net) table_model.getValueAt(i, 0)).get_class(), i, 1);
        }

        // Reinsert the net rule combobox because a rule may have  been added or deleted.
        add_net_class_combo_box();
    }

    private final BoardFrame board_frame;

    private final JPanel main_panel;

    private JScrollPane scroll_pane;
    private AssignRuleTable table;
    private AssignRuleTableModel table_model;

    private JComboBox net_rule_combo_box;

    private final ResourceBundle resources;

    private static final int TEXTFIELD_HEIGHT = 16;
    private static final int TEXTFIELD_WIDTH = 100;

    private class AssignRuleTable extends JTable {

        public AssignRuleTable(AssignRuleTableModel p_table_model) {
            super(p_table_model);
        }

        //Implement table header tool tips.
        @Override
        protected JTableHeader createDefaultTableHeader() {
            return new JTableHeader(columnModel) {
                @Override
                public String getToolTipText(MouseEvent e) {
                    java.awt.Point p = e.getPoint();
                    int index = columnModel.getColumnIndexAtX(p.x);
                    int realIndex = columnModel.getColumn(index).getModelIndex();
                    return column_tool_tips[realIndex];
                }
            };
        }

        private final String[] column_tool_tips
                = {
                    resources.getString("net_name_tooltip"), resources.getString("class_name_tooltip")
                };
    }

    /**
     * Table model of the net rule table.
     */
    private class AssignRuleTableModel extends AbstractTableModel {

        public AssignRuleTableModel() {
            column_names = new String[]{
                resources.getString("net_name"),
                resources.getString("class_name")
            };

            rules.BoardRules board_rules = board_frame.board_panel.board_handling.get_routing_board().rules;
            data = new Object[board_rules.nets.max_net_no()][];
            for (int i = 0; i < data.length; ++i) {
                data[i] = new Object[column_names.length];
            }
            set_values();
        }

        /**
         * Calculates the the valus in this table
         */
        public void set_values() {
            rules.BoardRules board_rules = board_frame.board_panel.board_handling.get_routing_board().rules;
            Net[] sorted_arr = new Net[getRowCount()];
            for (int i = 0; i < sorted_arr.length; ++i) {
                sorted_arr[i] = board_rules.nets.get(i + 1);
            }
            Arrays.sort(sorted_arr);
            for (int i = 0; i < data.length; ++i) {
                data[i][0] = sorted_arr[i];
                data[i][1] = sorted_arr[i].get_class();
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
        public boolean isCellEditable(int p_row, int p_col) {
            return p_col > 0;
        }

        @Override
        public void setValueAt(Object p_value, int p_row, int p_col) {
            if (p_col != 1 || !(p_value instanceof NetClass)) {
                return;
            }
            Object first_row_object = getValueAt(p_row, 0);
            if (!(first_row_object instanceof Net)) {
                System.out.println("AssignNetRuLesVindow.setValueAt: Net expected");
                return;
            }
            Net curr_net = (Net) first_row_object;
            NetClass curr_net_rule = (NetClass) p_value;
            curr_net.set_class(curr_net_rule);

            data[p_row][p_col] = p_value;
            fireTableCellUpdated(p_row, p_col);
        }

        private Object[][] data = null;
        private String[] column_names = null;
    }
}
