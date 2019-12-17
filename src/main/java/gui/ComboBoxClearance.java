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
 * ClearanceComboBox.java
 *
 * Created on 1. Maerz 2005, 09:27
 */
package gui;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import rules.ClearanceMatrix;

/**
 * A Combo Box with an item for each clearance class of the board..
 *
 * @author alfons
 */
public class ComboBoxClearance extends JComboBox {

    /**
     * Creates a new instance of ClearanceComboBox
     */
    public ComboBoxClearance(ClearanceMatrix p_clearance_matrix) {
        class_arr = new ClearanceClass[p_clearance_matrix.get_class_count()];
        for (int i = 0; i < class_arr.length; ++i) {
            class_arr[i] = new ClearanceClass(p_clearance_matrix.get_name(i), i);
        }
        setModel(new DefaultComboBoxModel(class_arr));
        setSelectedIndex(1);
    }

    /**
     * Adjusts this combo box to p_new_clearance_matrix.
     */
    public void adjust(ClearanceMatrix p_new_clearance_matrix) {
        int old_index = get_selected_class_index();
        class_arr = new ClearanceClass[p_new_clearance_matrix.get_class_count()];
        for (int i = 0; i < class_arr.length; ++i) {
            class_arr[i] = new ClearanceClass(p_new_clearance_matrix.get_name(i), i);
        }
        setModel(new DefaultComboBoxModel(class_arr));
        setSelectedIndex(Math.min(old_index, class_arr.length - 1));
    }

    /**
     * Returns the index of the selected clearance class in the clearance
     * matrix.
     */
    public int get_selected_class_index() {
        return ((ClearanceClass) getSelectedItem()).index;
    }

    /**
     * Returns the number of clearance classes in this combo box.
     */
    public int get_class_count() {
        return class_arr.length;
    }

    private ClearanceClass[] class_arr;

    /**
     * Contains the name of a clearance class and its index in the clearance
     * matrix.
     */
    private static class ClearanceClass {

        public ClearanceClass(String p_name, int p_index) {
            name = p_name;
            index = p_index;
        }

        @Override
        public String toString() {
            return name;
        }

        public final String name;
        public final int index;
    }
}
