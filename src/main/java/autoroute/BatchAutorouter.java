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
 */
package autoroute;

import board.Connectable;
import board.DrillItem;
import board.Item;
import board.RoutingBoard;
import datastructures.TimeLimit;
import datastructures.UndoableObjects;
import geometry.planar.FloatLine;
import geometry.planar.FloatPoint;
import gui.Freerouter;
import interactive.BoardHandling;
import interactive.InteractiveActionThread;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedSet;

/**
 * Handles the sequencing of the batch autoroute passes.
 *
 * @author Alfons Wirtz
 */
public class BatchAutorouter {

    /**
     * Autoroutes ripup passes until the board is completed or the autorouter is
     * stopped by the user, or if p_max_pass_count is exceeded. Is currently
     * used in the optimize via batch pass. Returns the number of oasses to
     * complete the board or p_max_pass_count + 1, if the board is not
     * completed.
     */
    public static int autoroute_passes_for_optimizing_item(InteractiveActionThread p_thread,
            int p_max_pass_count, int p_ripup_costs, boolean p_with_prefered_directions) {
        BatchAutorouter router_instance = new BatchAutorouter(p_thread, true, p_with_prefered_directions, p_ripup_costs);
        boolean still_unrouted_items = true;
        int curr_pass_no = 1;
        while (still_unrouted_items && !router_instance.is_interrupted && curr_pass_no <= p_max_pass_count) {
            if (p_thread.is_stop_requested()) {
                router_instance.is_interrupted = true;
            }
            still_unrouted_items = router_instance.autoroute_pass(curr_pass_no, false);
            if (still_unrouted_items && !router_instance.is_interrupted) {
                p_thread.hdlg.get_settings().autoroute_settings.increment_pass_no();
            }
            ++curr_pass_no;
        }
        router_instance.remove_tails(Item.StopConnectionOption.NONE);
        if (!still_unrouted_items) {
            --curr_pass_no;
        }
        return curr_pass_no;
    }

    /**
     * Creates a new batch autorouter.
     */
    public BatchAutorouter(InteractiveActionThread p_thread, boolean p_remove_unconnected_vias, boolean p_with_preferred_directions,
            int p_start_ripup_costs) {
        thread = p_thread;
        hdlg = p_thread.hdlg;
        routing_board = hdlg.get_routing_board();
        remove_unconnected_vias = p_remove_unconnected_vias;
        if (p_with_preferred_directions) {
            trace_cost_arr = hdlg.get_settings().autoroute_settings.get_trace_cost_arr();
        } else {
            // remove prefered direction
            trace_cost_arr = new AutorouteControl.ExpansionCostFactor[routing_board.get_layer_count()];
            for (int i = 0; i < trace_cost_arr.length; ++i) {
                double curr_min_cost = hdlg.get_settings().autoroute_settings.get_preferred_direction_trace_costs(i);
                trace_cost_arr[i] = new AutorouteControl.ExpansionCostFactor(curr_min_cost, curr_min_cost);
            }
        }

        start_ripup_costs = p_start_ripup_costs;
        retain_autoroute_database = false;
    }

    /**
     * Autoroutes ripup passes until the board is completed or the autorouter is
     * stopped by the user. Returns true if the board is completed.
     */
    public boolean autoroute_passes() {
        ResourceBundle resources
                = ResourceBundle.getBundle("interactive.resources.InteractiveState", hdlg.get_locale());
        boolean still_unrouted_items = true;
        while (still_unrouted_items && !is_interrupted) {
            if (thread.is_stop_requested()) {
                is_interrupted = true;
            }
            int curr_pass_no = hdlg.get_settings().autoroute_settings.get_pass_no();
            String start_message = resources.getString("batch_autorouter") + " " + resources.getString("stop_message")
                    + "        " + resources.getString("pass") + " " + curr_pass_no + ": ";
            hdlg.screen_messages.set_status_message(start_message);

            Freerouter.logInfo("Batch autorouter pass: " + curr_pass_no + " start.");
            still_unrouted_items = autoroute_pass(curr_pass_no, true);
            Freerouter.logInfo("Batch autorouter pass: " + curr_pass_no + " end.");

            if (still_unrouted_items && !is_interrupted) {
                hdlg.get_settings().autoroute_settings.increment_pass_no();
            }
        }
        if (!(remove_unconnected_vias || still_unrouted_items || is_interrupted)) {
            // clean up the route if the board is completed and if fanout is used.
            remove_tails(Item.StopConnectionOption.NONE);
        }
        return !is_interrupted;
    }

    /**
     * Autoroutes one ripup pass of all items of the board. Returns false, if
     * the board is already completely routed.
     */
    private boolean autoroute_pass(int p_pass_no, boolean p_with_screen_message) {
        try {
            Collection<Item> autoroute_item_list = new LinkedList<>();
            Set<Item> handeled_items = Freerouter.newHashSet();
            Iterator<UndoableObjects.UndoableObjectNode> it = routing_board.item_list.start_read_object();
            while (true) {
                UndoableObjects.Storable curr_ob = routing_board.item_list.read_object(it);
                if (curr_ob == null) {
                    break;
                }
                if (curr_ob instanceof Connectable && curr_ob instanceof Item) {
                    Item curr_item = (Item) curr_ob;
                    if (!curr_item.is_route() && !handeled_items.contains(curr_item)) {
                        for (int i = 0; i < curr_item.net_count(); ++i) {
                            int curr_net_no = curr_item.get_net_no(i);
                            Set<Item> connected_set = curr_item.get_connected_set(curr_net_no);
                            for (Item curr_connected_item : connected_set) {
                                if (curr_connected_item.net_count() <= 1) {
                                    handeled_items.add(curr_connected_item);
                                }
                            }
                            int net_item_count = routing_board.connectable_item_count(curr_net_no);
                            if (connected_set.size() < net_item_count) {
                                autoroute_item_list.add(curr_item);
                            }
                        }
                    }
                }
            }
            if (autoroute_item_list.isEmpty()) {
                air_line = null;
                return false;
            }
            int items_to_go_count = autoroute_item_list.size();
            int ripped_item_count = 0;
            int not_found = 0;
            int routed = 0;
            if (p_with_screen_message) {
                hdlg.screen_messages.set_batch_autoroute_info(items_to_go_count, routed, ripped_item_count, not_found);
            }
            for (Item curr_item : autoroute_item_list) {
                if (is_interrupted) {
                    break;
                }
                for (int i = 0; i < curr_item.net_count(); ++i) {
                    if (thread.is_stop_requested()) {
                        is_interrupted = true;
                        break;
                    }
                    routing_board.start_marking_changed_area();
                    SortedSet<Item> ripped_item_list = Freerouter.newSortedSet();
                    if (autoroute_item(curr_item, curr_item.get_net_no(i), ripped_item_list, p_pass_no)) {
                        ++routed;
                        hdlg.repaint();
                    } else {
                        ++not_found;
                    }
                    --items_to_go_count;
                    ripped_item_count += ripped_item_list.size();
                    if (p_with_screen_message) {
                        hdlg.screen_messages.set_batch_autoroute_info(items_to_go_count, routed, ripped_item_count, not_found);
                    }
                }
            }
            if (routing_board.get_test_level() != board.TestLevel.ALL_DEBUGGING_OUTPUT) {
                Item.StopConnectionOption stop_connection_option;
                if (remove_unconnected_vias) {
                    stop_connection_option = Item.StopConnectionOption.NONE;
                } else {
                    stop_connection_option = Item.StopConnectionOption.FANOUT_VIA;
                }
                remove_tails(stop_connection_option);
            }
            air_line = null;
            return true;
        } catch (Exception e) {
//            Freerouter.logError(e);
            air_line = null;
            return false;
        }
    }

    private void remove_tails(Item.StopConnectionOption p_stop_connection_option) {
        routing_board.start_marking_changed_area();
        routing_board.remove_trace_tails(-1, p_stop_connection_option);
        routing_board.opt_changed_area(new int[0], null, hdlg.get_settings().get_trace_pull_tight_accuracy(),
                trace_cost_arr, thread, TIME_LIMIT_TO_PREVENT_ENDLESS_LOOP);
    }

    private boolean autoroute_item(Item p_item, int p_route_net_no, SortedSet<Item> p_ripped_item_list, int p_ripup_pass_no) {
        try {
            boolean contains_plane = false;
            rules.Net route_net = routing_board.rules.nets.get(p_route_net_no);
            if (route_net != null) {
                contains_plane = route_net.contains_plane();
            }
            int curr_via_costs;

            if (contains_plane) {
                curr_via_costs = hdlg.get_settings().autoroute_settings.get_plane_via_costs();
            } else {
                curr_via_costs = hdlg.get_settings().autoroute_settings.get_via_costs();
            }
            AutorouteControl autoroute_control = new AutorouteControl(routing_board, p_route_net_no, hdlg.get_settings(), curr_via_costs, trace_cost_arr);
            autoroute_control.ripup_allowed = true;
            autoroute_control.ripup_costs = start_ripup_costs * p_ripup_pass_no;
            autoroute_control.remove_unconnected_vias = remove_unconnected_vias;

            Set<Item> unconnected_set = p_item.get_unconnected_set(p_route_net_no);
            if (unconnected_set.isEmpty()) {
                return true; // p_item is already routed.

            }
            Set<Item> connected_set = p_item.get_connected_set(p_route_net_no);
            Set<Item> route_start_set;
            Set<Item> route_dest_set;
            if (contains_plane) {
                for (Item curr_item : connected_set) {
                    if (curr_item instanceof board.ConductionArea) {
                        return true; // already connected to plane
                    }
                }
            }
            if (contains_plane) {
                route_start_set = connected_set;
                route_dest_set = unconnected_set;
            } else {
                route_start_set = unconnected_set;
                route_dest_set = connected_set;
            }

            calc_airline(route_start_set, route_dest_set);
            double max_milliseconds = 100000 * Math.pow(2, p_ripup_pass_no - 1);
            max_milliseconds = Math.min(max_milliseconds, Integer.MAX_VALUE);
            TimeLimit time_limit = new TimeLimit((int) max_milliseconds);
            AutorouteEngine autoroute_engine = routing_board.init_autoroute(p_route_net_no,
                    autoroute_control.trace_clearance_class_no, thread, time_limit, retain_autoroute_database);
            AutorouteEngine.AutorouteResult autoroute_result = autoroute_engine
                    .autoroute_connection(route_start_set, route_dest_set, autoroute_control, p_ripped_item_list);
            if (autoroute_result == AutorouteEngine.AutorouteResult.ROUTED) {
                routing_board.opt_changed_area(new int[0], null,
                        hdlg.get_settings().get_trace_pull_tight_accuracy(),
                        autoroute_control.trace_costs, thread, TIME_LIMIT_TO_PREVENT_ENDLESS_LOOP);
            }
            // tests.Validate.check("Autoroute  ", hdlg.get_routing_board());
            boolean result = autoroute_result == AutorouteEngine.AutorouteResult.ROUTED
                    || autoroute_result == AutorouteEngine.AutorouteResult.ALREADY_CONNECTED;
            return result;
        } catch (Exception e) {
//            Freerouter.logError(e);
            return false;
        }
    }

    /**
     * Returns the airline of the current autorouted connnection or null, if no
     * such airline exists
     */
    public FloatLine get_air_line() {
        if (air_line == null) {
            return null;
        }
        if (air_line.a == null || air_line.b == null) {
            return null;
        }
        return air_line;
    }

    private void calc_airline(Collection<Item> p_from_items, Collection<Item> p_to_items) {
        FloatPoint from_corner = null;
        FloatPoint to_corner = null;
        double min_distance = Double.MAX_VALUE;
        for (Item curr_from_item : p_from_items) {
            if (!(curr_from_item instanceof DrillItem)) {
                continue;
            }
            FloatPoint curr_from_corner = ((DrillItem) curr_from_item).get_center().to_float();
            for (Item curr_to_item : p_to_items) {
                if (!(curr_to_item instanceof DrillItem)) {
                    continue;
                }
                FloatPoint curr_to_corner = ((DrillItem) curr_to_item).get_center().to_float();
                double curr_distance = curr_from_corner.distance_square(curr_to_corner);
                if (curr_distance < min_distance) {
                    min_distance = curr_distance;
                    from_corner = curr_from_corner;
                    to_corner = curr_to_corner;
                }
            }
        }
        air_line = new FloatLine(from_corner, to_corner);
    }
    private final InteractiveActionThread thread;
    private final BoardHandling hdlg;
    private final RoutingBoard routing_board;
    private boolean is_interrupted = false;
    private final boolean remove_unconnected_vias;
    private final AutorouteControl.ExpansionCostFactor[] trace_cost_arr;
    private final boolean retain_autoroute_database;
    private final int start_ripup_costs;
    /**
     * Used to draw the airline of the current routed incomplete.
     */
    private FloatLine air_line = null;
    private static final int TIME_LIMIT_TO_PREVENT_ENDLESS_LOOP = 1000;
}
