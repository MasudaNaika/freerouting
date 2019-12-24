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
 * SelectItemsInRegionState.java
 *
 * Created on 9. November 2003, 12:02
 */
package interactive;

import board.Item;
import geometry.planar.FloatPoint;
import geometry.planar.IntBox;
import geometry.planar.IntPoint;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import java.util.Set;

/**
 * Interactive state for selecting all items in a rectangle.
 *
 * @author Alfons Wirtz
 */
public class SelectItemsInRegionState extends SelectRegionState {

    /**
     * Returns a new instance of this class.
     */
    public static SelectItemsInRegionState get_instance(InteractiveState p_parent_state,
            BoardHandling p_board_handling, Logfile p_logfile) {
        return get_instance(null, p_parent_state, p_board_handling, p_logfile);
    }

    /**
     * Returns a new instance of this class with first point p_location.
     */
    public static SelectItemsInRegionState get_instance(FloatPoint p_location, InteractiveState p_parent_state,
            BoardHandling p_board_handling, Logfile p_logfile) {
        p_board_handling.display_layer_messsage();
        SelectItemsInRegionState new_instance
                = new SelectItemsInRegionState(p_parent_state, p_board_handling, p_logfile);
        new_instance.corner1 = p_location;
        if (new_instance.logfile != null) {
            new_instance.logfile.add_corner(p_location);
        }
        new_instance.hdlg.screen_messages.set_status_message(new_instance.resources.getString("drag_left_mouse_button_to_selects_items_in_region"));
        return new_instance;
    }

    /**
     * Creates a new instance of SelectItemsInRegionState
     */
    private SelectItemsInRegionState(InteractiveState p_parent_state,
            BoardHandling p_board_handling, Logfile p_logfile) {
        super(p_parent_state, p_board_handling, p_logfile);
        if (logfile != null) {
            logfile.start_scope(LogfileScope.SELECT_REGION);
        }
    }

    @Override
    public InteractiveState complete() {
        if (!hdlg.is_board_read_only()) {
            hdlg.screen_messages.set_status_message("");
            corner2 = hdlg.get_current_mouse_position();
            if (logfile != null) {
                logfile.add_corner(corner2);
            }
            select_all_in_region();
        }
        return return_state;
    }

    /**
     * Selects all items in the rectangle defined by corner1 and corner2.
     */
    private void select_all_in_region() {
        IntPoint p1 = corner1.round();
        IntPoint p2 = corner2.round();

        IntBox b = new IntBox(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y), Math.max(p1.x, p2.x), Math.max(p1.y, p2.y));
        int select_layer;
        if (hdlg.settings.select_on_all_visible_layers) {
            select_layer = -1;
        } else {
            select_layer = hdlg.settings.layer;
        }
        Set<Item> found_items = hdlg.settings.item_selection_filter.filter(hdlg.get_routing_board().overlapping_items(b, select_layer));
        if (hdlg.settings.select_on_all_visible_layers) {
            // remove items, which are not visible
            Set<Item> visible_items = new ObjectAVLTreeSet<>();
            for (Item curr_item : found_items) {
                for (int i = curr_item.first_layer(); i <= curr_item.last_layer(); ++i) {
                    if (hdlg.graphics_context.get_layer_visibility(i) > 0) {
                        visible_items.add(curr_item);
                        break;
                    }
                }
            }
            found_items = visible_items;
        }
        boolean something_found = (found_items.size() > 0);
        if (something_found) {
            if (return_state instanceof SelectedItemState) {
                ((SelectedItemState) return_state).get_item_list().addAll(found_items);
            } else {
                return_state = SelectedItemState.get_instance(found_items, return_state, hdlg, logfile);
            }
        } else {
            hdlg.screen_messages.set_status_message(resources.getString("nothing_selected"));
        }
    }
}
