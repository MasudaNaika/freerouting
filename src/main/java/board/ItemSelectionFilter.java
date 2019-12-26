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
 * ItemSelectionFilter.java
 *
 * Created on 14. Dezember 2004, 10:57
 */
package board;

import gui.Freerouter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;

/**
 * Filter for selecting items on the board.
 *
 * @author Alfons Wirtz
 */
public class ItemSelectionFilter implements Serializable {

    /**
     * The possible choices in the filter.
     */
    public enum SelectableChoices {
        TRACES, VIAS, PINS, CONDUCTION, KEEPOUT, VIA_KEEPOUT, COMPONENT_KEEPOUT, BOARD_OUTLINE, FIXED, UNFIXED
    }

    /**
     * Creates a new filter with all item types selected.
     */
    public ItemSelectionFilter() {
        values = new boolean[SelectableChoices.values().length];
        Arrays.fill(values, true);
        values[SelectableChoices.KEEPOUT.ordinal()] = false;
        values[SelectableChoices.VIA_KEEPOUT.ordinal()] = false;
        values[SelectableChoices.COMPONENT_KEEPOUT.ordinal()] = false;
        values[SelectableChoices.CONDUCTION.ordinal()] = false;
        values[SelectableChoices.BOARD_OUTLINE.ordinal()] = false;
    }

    /**
     * Creates a new filter with only p_item_type selected.
     */
    public ItemSelectionFilter(SelectableChoices p_item_type) {
        values = new boolean[SelectableChoices.values().length];
//        Arrays.fill(values, false);
        values[p_item_type.ordinal()] = true;
        values[SelectableChoices.FIXED.ordinal()] = true;
        values[SelectableChoices.UNFIXED.ordinal()] = true;
    }

    /**
     * Creates a new filter with only p_item_types selected.
     */
    public ItemSelectionFilter(SelectableChoices[] p_item_types) {
        values = new boolean[SelectableChoices.values().length];
//        Arrays.fill(values, false);
        for (int i = 0; i < p_item_types.length; ++i) {
            values[p_item_types[i].ordinal()] = true;
        }
        values[SelectableChoices.FIXED.ordinal()] = true;
        values[SelectableChoices.UNFIXED.ordinal()] = true;
    }

    /**
     * Copy constructor
     */
    public ItemSelectionFilter(ItemSelectionFilter p_item_selection_filter) {
        values = new boolean[SelectableChoices.values().length];
        for (int i = 0; i < values.length; ++i) {
            values[i] = p_item_selection_filter.values[i];
        }
    }

    /**
     * Selects or deselects an item type
     */
    public void set_selected(SelectableChoices p_choice, boolean p_value) {
        values[p_choice.ordinal()] = p_value;
    }

    /**
     * Selects all item types.
     */
    public void select_all() {
        Arrays.fill(values, true);
    }

    /**
     * Deselects all item types.
     */
    public void deselect_all() {
        Arrays.fill(values, false);
    }

    /**
     * Filters a collection of items with this filter.
     */
    public Set<Item> filter(Set<board.Item> p_items) {
        Set<Item> result = Freerouter.newSortedSet();
        for (board.Item curr_item : p_items) {
            if (curr_item.is_selected_by_filter(this)) {
                result.add(curr_item);
            }
        }
        return result;
    }

    /**
     * Looks, if the input item type is selected.
     */
    public boolean is_selected(SelectableChoices p_choice) {
        return values[p_choice.ordinal()];
    }

    /**
     * the filter array of the item types
     */
    private final boolean[] values;
}
