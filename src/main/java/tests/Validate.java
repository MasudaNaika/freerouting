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
 * Validate.java
 *
 * Created on 7. Dezember 2002, 18:26
 */
package tests;

import board.BasicBoard;
import board.Item;
import board.PolylineTrace;
import board.SearchTreeObject;
import geometry.planar.IntOctagon;
import geometry.planar.IntPoint;
import geometry.planar.Polyline;
import geometry.planar.TileShape;
import gui.Freerouter;
import java.util.Collection;

/**
 * Some consistancy checking on a routing board.
 *
 * @author Alfons Wirtz
 */
public class Validate {

    /**
     * Does some consistancy checking on the routing board and may be some other
     * actions. Returns false, if problems were detected.
     */
    public static boolean check(String p_s, BasicBoard p_board) {
        if (p_board.get_test_level() == board.TestLevel.RELEASE_VERSION) {
            return true;
        }
        boolean result = true;

        IntOctagon surr_oct = p_board.bounding_box.to_IntOctagon();
        int layer_count = p_board.get_layer_count();
        if (last_violation_count == null) {
            last_violation_count = new int[layer_count];
        }
        for (int layer = 0; layer < layer_count; ++layer) {
            if (first_time) {
                Freerouter.logInfo(" validate board is on ");
                first_time = false;
            }

            int clearance_violation_count = 0;
            int conflict_ob_count = 0;
            int trace_count = 0;

            Collection<SearchTreeObject> objects = p_board.overlapping_objects(surr_oct, layer);
            for (SearchTreeObject obj : objects) {
                Item curr_ob = (Item) obj;
                if (!curr_ob.validate()) {
                    Freerouter.logInfo(p_s);
                }
                int cl_count = curr_ob.clearance_violation_count();
                if (cl_count > 0) {
                    ++conflict_ob_count;
                    clearance_violation_count += cl_count;
                }
                if (curr_ob instanceof PolylineTrace) {
                    ++trace_count;
                }
            }
            if (conflict_ob_count == 1) {
                Freerouter.logInfo("conflicts not symmetric");
            }
            if (clearance_violation_count != last_violation_count[layer]) {
                result = false;
                StringBuilder sb = new StringBuilder();
                sb.append(clearance_violation_count + " clearance violations on layer " + layer + " " + p_s);
                if (clearance_violation_count > 0) {
                    sb.append("with items of nets: ");
                }
                for (SearchTreeObject obj : objects) {
                    Item curr_ob = (Item) obj;
                    int cl_count = curr_ob.clearance_violation_count();
                    if (cl_count == 0) {
                        continue;
                    }
                    int curr_net_no = 0;
                    if (curr_ob instanceof PolylineTrace) {
                        PolylineTrace curr_trace = (PolylineTrace) curr_ob;
                        if (curr_trace.net_count() > 0) {
                            curr_net_no = curr_trace.get_net_no(0);
                        }
                    }
                    sb.append(curr_net_no + ", ");
                }
                Freerouter.logInfo(sb.toString());
            }
            if (clearance_violation_count != last_violation_count[layer]) {
                last_violation_count[layer] = clearance_violation_count;
            }

        }
        return result;
    }

    static public boolean check(String p_s, BasicBoard p_board, Polyline p_polyline,
            int p_layer, int p_half_width, int[] p_net_no_arr, int p_cl_type) {
        TileShape[] offset_shapes = p_polyline.offset_shapes(p_half_width,
                0, p_polyline.arr.length - 1);
        for (TileShape ts : offset_shapes) {
            Collection<Item> obstacles = p_board.search_tree_manager.get_default_tree()
                    .overlapping_items_with_clearance(ts, p_layer, p_net_no_arr, p_cl_type);
            for (Item curr_obs : obstacles) {
                if (!curr_obs.shares_net_no(p_net_no_arr)) {
                    Freerouter.logInfo(p_s + ": cannot insert trace without violations");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * check, that all traces on p_board are orthogonal
     */
    static public void orthogonal(String p_s, BasicBoard p_board) {
        for (Item curr_ob : p_board.get_items()) {
            if (curr_ob instanceof PolylineTrace) {
                PolylineTrace curr_trace = (PolylineTrace) curr_ob;
                if (!curr_trace.polyline().is_orthogonal()) {
                    Freerouter.logInfo(p_s + ": trace not orthogonal");
                    break;
                }
            }
        }
    }

    /**
     * check, that all traces on p_board are multiples of 45 degree
     */
    static public void multiple_of_45_degree(String p_s, BasicBoard p_board) {
        int count = 0;
        for (Item curr_ob : p_board.get_items()) {
            if (curr_ob instanceof PolylineTrace) {
                PolylineTrace curr_trace = (PolylineTrace) curr_ob;
                if (!curr_trace.polyline().is_multiple_of_45_degree()) {
                    ++count;
                }
            }
        }
        if (count > 1) {
            Freerouter.logInfo(p_s + count + " traces not 45 degree");
        }
    }

    static public boolean corners_on_grid(String p_s, Polyline p_polyline) {
        for (int i = 0; i < p_polyline.corner_count(); ++i) {
            if (!(p_polyline.corner(i) instanceof IntPoint)) {
                Freerouter.logInfo(p_s + ": corner not on grid");
                return false;
            }
        }
        return true;
    }

    static public int stub_count(String p_s, BasicBoard p_board, int p_net_no) {
        if (first_time) {
            Freerouter.logInfo(" stub_count is on ");
            first_time = false;
        }
        int result = 0;
        for (Item curr_ob : p_board.get_items()) {
            if (curr_ob instanceof PolylineTrace) {
                PolylineTrace curr_trace = (PolylineTrace) curr_ob;
                if (curr_trace.contains_net(p_net_no)) {
                    if (curr_trace.get_start_contacts().isEmpty()) {
                        ++result;
                    }
                    if (curr_trace.get_end_contacts().isEmpty()) {
                        ++result;
                    }
                }
            }
        }
        if (result != prev_stub_count) {
            Freerouter.logInfo(result + " stubs " + p_s);
            prev_stub_count = result;
        }
        return result;
    }

    static public boolean has_cycles(String p_s, BasicBoard p_board) {
        boolean result = false;
        for (Item curr_item : p_board.get_items()) {
            if (!(curr_item instanceof board.Trace)) {
                continue;
            }
            if (((board.Trace) curr_item).is_cycle()) {
                Freerouter.logInfo(p_s + ": cycle found");
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * checks, if there are more than p_max_count traces with net number
     * p_net_no
     */
    static public boolean trace_count_exceeded(String p_s, BasicBoard p_board, int p_net_no, int p_max_count) {
        int found_traces = 0;
        for (Item curr_ob : p_board.get_items()) {
            if (curr_ob instanceof board.Trace) {
                if (((board.Trace) curr_ob).contains_net(p_net_no)) {
                    ++found_traces;
                }
            }
        }
        if (found_traces > p_max_count) {
            Freerouter.logInfo(p_s + ": " + p_max_count + " traces exceeded");
            return true;
        }
        return false;
    }

    /**
     * checks, if there are unconnectedtraces ore vias on the board
     */
    static public boolean unconnnected_routing_items(String p_s, BasicBoard p_board) {
        for (Item curr_item : p_board.get_items()) {
            if (curr_item.is_route()) {
                Collection<Item> contact_list = curr_item.get_normal_contacts();
                if (contact_list.isEmpty()) {
                    Freerouter.logInfo(p_s + ": uncontacted routing item found ");
                    return true;
                }
            }
        }
        return false;
    }

    static private int[] last_violation_count = null;
    static private boolean first_time = true;
    static private int prev_stub_count = 0;
}
