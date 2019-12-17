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

import board.RoutingBoard;
import datastructures.TimeLimit;
import geometry.planar.FloatPoint;
import interactive.InteractiveActionThread;
import java.util.Collection;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Handles the sequencing of the fanout inside the batch autorouter.
 *
 * @author Alfons Wirtz
 */
public class BatchFanout {

    public static void fanout_board(InteractiveActionThread p_thread) {
        BatchFanout fanout_instance = new BatchFanout(p_thread);
        final int MAX_PASS_COUNT = 20;
        for (int i = 0; i < MAX_PASS_COUNT; ++i) {
            int routed_count = fanout_instance.fanout_pass(i);
            if (routed_count == 0) {
                break;
            }
        }
    }

    private BatchFanout(InteractiveActionThread p_thread) {
        thread = p_thread;
        routing_board = p_thread.hdlg.get_routing_board();
        Collection<board.Pin> board_smd_pin_list = routing_board.get_smd_pins();
        sorted_components = new TreeSet<>();
        for (int i = 1; i <= routing_board.components.count(); ++i) {
            board.Component curr_board_component = routing_board.components.get(i);
            Component curr_component = new Component(curr_board_component, board_smd_pin_list);
            if (curr_component.smd_pin_count > 0) {
                sorted_components.add(curr_component);
            }
        }
    }

    /**
     * Routes a fanout pass and returns the number of new fanouted SMD-pins in
     * this pass.
     */
    private int fanout_pass(int p_pass_no) {
        int components_to_go = sorted_components.size();
        int routed_count = 0;
        int not_routed_count = 0;
        int insert_error_count = 0;
        int ripup_costs = thread.hdlg.get_settings().autoroute_settings.get_start_ripup_costs() * (p_pass_no + 1);
        for (Component curr_component : sorted_components) {
            thread.hdlg.screen_messages.set_batch_fanout_info(p_pass_no + 1, components_to_go);
            for (Component.Pin curr_pin : curr_component.smd_pins) {
                double max_milliseconds = 10000 * (p_pass_no + 1);
                TimeLimit time_limit = new TimeLimit((int) max_milliseconds);
                routing_board.start_marking_changed_area();
                AutorouteEngine.AutorouteResult curr_result
                        = routing_board.fanout(curr_pin.board_pin, thread.hdlg.get_settings(), ripup_costs, thread, time_limit);
                if (null != curr_result) {
                    switch (curr_result) {
                        case ROUTED:
                            ++routed_count;
                            break;
                        case NOT_ROUTED:
                            ++not_routed_count;
                            break;
                        case INSERT_ERROR:
                            ++insert_error_count;
                            break;
                        default:
                            break;
                    }
                }
                if (curr_result != AutorouteEngine.AutorouteResult.NOT_ROUTED) {
                    thread.hdlg.repaint();
                }
                if (thread.is_stop_requested()) {
                    return routed_count;
                }
            }
            --components_to_go;
        }
        if (routing_board.get_test_level() != board.TestLevel.RELEASE_VERSION) {
            System.out.println("fanout pass: " + (p_pass_no + 1) + ", routed: " + routed_count
                    + ", not routed: " + not_routed_count + ", errors: " + insert_error_count);
        }
        return routed_count;
    }

    private final InteractiveActionThread thread;
    private final RoutingBoard routing_board;

    private static class Component implements Comparable<Component> {

        Component(board.Component p_board_component, Collection<board.Pin> p_board_smd_pin_list) {
            board_component = p_board_component;

            // calcoulate the center of gravity of all SMD pins of this component.
            Collection<board.Pin> curr_pin_list = new LinkedList<>();
            int cmp_no = p_board_component.no;
            for (board.Pin curr_board_pin : p_board_smd_pin_list) {
                if (curr_board_pin.get_component_no() == cmp_no) {
                    curr_pin_list.add(curr_board_pin);
                }
            }
            double x = 0;
            double y = 0;
            for (board.Pin curr_pin : curr_pin_list) {
                FloatPoint curr_point = curr_pin.get_center().to_float();
                x += curr_point.x;
                y += curr_point.y;
            }
            smd_pin_count = curr_pin_list.size();
            x /= smd_pin_count;
            y /= smd_pin_count;
            gravity_center_of_smd_pins = new FloatPoint(x, y);

            // calculate the sorted SMD pins of this component
            smd_pins = new TreeSet<>();

            for (board.Pin curr_board_pin : curr_pin_list) {
                smd_pins.add(new Pin(curr_board_pin));
            }

        }

        /**
         * Sort the components, so that components with maor pins come first
         */
        @Override
        public int compareTo(Component p_other) {
            int compare_value = smd_pin_count - p_other.smd_pin_count;
            int result;
            if (compare_value > 0) {
                result = -1;
            } else if (compare_value < 0) {
                result = 1;
            } else {
                result = board_component.no - p_other.board_component.no;
            }
            return result;
        }
        final board.Component board_component;
        final int smd_pin_count;
        final SortedSet<Pin> smd_pins;
        /**
         * The center of gravity of all SMD pins of this component.
         */
        final FloatPoint gravity_center_of_smd_pins;

        class Pin implements Comparable<Pin> {

            Pin(board.Pin p_board_pin) {
                board_pin = p_board_pin;
                FloatPoint pin_location = p_board_pin.get_center().to_float();
                distance_to_component_center = pin_location.distance(gravity_center_of_smd_pins);
            }

            @Override
            public int compareTo(Pin p_other) {
                int result;
                double delta_dist = distance_to_component_center - p_other.distance_to_component_center;
                if (delta_dist > 0) {
                    result = 1;
                } else if (delta_dist < 0) {
                    result = -1;
                } else {
                    result = board_pin.pin_no - p_other.board_pin.pin_no;
                }
                return result;
            }
            final board.Pin board_pin;
            final double distance_to_component_center;
        }
    }
    private final SortedSet<Component> sorted_components;
}
