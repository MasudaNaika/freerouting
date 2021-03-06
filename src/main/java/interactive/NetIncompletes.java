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
 * NetIncompletes.java
 *
 * Created on 16. Maerz 2004, 06:47
 */
package interactive;

import board.BasicBoard;
import board.Item;
import boardgraphics.GraphicsContext;
import datastructures.PlanarDelaunayTriangulation;
import geometry.planar.FloatPoint;
import geometry.planar.Point;
import net.freerouting.Freerouter;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;
import java.util.SortedSet;
import rules.Net;

/**
 * Creates the Incompletes (Ratsnest) of one net to display them on the screen.
 *
 * @author Alfons Wirtz
 */
public class NetIncompletes {

    /**
     * Creates a new instance of NetIncompletes
     */
    public NetIncompletes(int p_net_no, Collection<Item> p_net_items, BasicBoard p_board, Locale p_locale) {
        draw_marker_radius = p_board.rules.get_min_trace_half_width() * 2;
        incompletes = new LinkedList<>();
        net = p_board.rules.nets.get(p_net_no);

        // Create an array of Item-connected_set pairs.
        NetItem[] net_items = calculate_net_items(p_net_items);
        if (net_items.length <= 1) {
            return;
        }

        // create a Delauny Triangulation for the net_items
        Collection<PlanarDelaunayTriangulation.Storable> triangulation_objects
                = new LinkedList<>();
        triangulation_objects.addAll(Arrays.asList(net_items));
        PlanarDelaunayTriangulation triangulation = new PlanarDelaunayTriangulation(triangulation_objects);

        // sort the result edges of the triangulation by length in ascending order.
        Collection<PlanarDelaunayTriangulation.ResultEdge> triangulation_lines = triangulation.get_edge_lines();
        SortedSet<Edge> sorted_edges = Freerouter.newSortedSet();

        for (PlanarDelaunayTriangulation.ResultEdge curr_line : triangulation_lines) {
            Edge new_edge = new Edge((NetItem) curr_line.start_object, curr_line.start_point.to_float(),
                    (NetItem) curr_line.end_object, curr_line.end_point.to_float());
            sorted_edges.add(new_edge);
        }

        // Create the Airlines. Skip edges, whose from_item and to_item are already in the same connected set
        // or whose connected sets have already an airline.
        Net curr_net = p_board.rules.nets.get(p_net_no);
        for (Edge curr_edge : sorted_edges) {
            if (curr_edge.from_item.connected_set == curr_edge.to_item.connected_set) {
                continue; // airline exists already
            }
            incompletes.add(new RatsNest.AirLine(curr_net, curr_edge.from_item.item,
                    curr_edge.from_corner, curr_edge.to_item.item, curr_edge.to_corner, p_locale));
            join_connected_sets(net_items, curr_edge.from_item.connected_set, curr_edge.to_item.connected_set);
        }
        calc_length_violation();
    }

    /**
     * Returns the number of incompletes of this net.
     */
    public int count() {
        return incompletes.size();
    }

    /**
     * Recalculates the length violations. Return false, if the lenght violation
     * has not changed.
     */
    boolean calc_length_violation() {
        double old_violation = length_violation;
        double max_length = net.get_class().get_maximum_trace_length();
        double min_length = net.get_class().get_minimum_trace_length();
        if (max_length <= 0 && min_length <= 0) {
            length_violation = 0;
            return false;
        }
        double new_violation = 0;
        double trace_length = net.get_trace_length();
        if (max_length > 0 && trace_length > max_length) {
            new_violation = trace_length - max_length;
        }
        if (min_length > 0 && trace_length < min_length && incompletes.isEmpty()) {
            new_violation = trace_length - min_length;
        }
        length_violation = new_violation;
        boolean result = Math.abs(new_violation - old_violation) > 0.1;
        return result;
    }

    /**
     * Returns the length of the violation of the length restriction of the net,
     * > 0, if the cumulative trace length is to big, < 0, if the trace length
     * is to smalll, 0, if the thace length is ok or the net has no length
     * restrictions
     */
    double get_length_violation() {
        return length_violation;
    }

    public void draw(Graphics p_graphics, GraphicsContext p_graphics_context, boolean p_length_violations_only) {
        if (!p_length_violations_only) {
            Color draw_color = p_graphics_context.get_incomplete_color();
            double draw_intensity = p_graphics_context.get_incomplete_color_intensity();
            if (draw_intensity <= 0) {
                return;
            }
            FloatPoint[] draw_points = new FloatPoint[2];
            int draw_width = 1;
            for (RatsNest.AirLine curr_incomplete : incompletes) {
                draw_points[0] = curr_incomplete.from_corner;
                draw_points[1] = curr_incomplete.to_corner;
                p_graphics_context.draw(draw_points, draw_width, draw_color, p_graphics, draw_intensity);
                if (!curr_incomplete.from_item.shares_layer(curr_incomplete.to_item)) {
                    draw_layer_change_marker(curr_incomplete.from_corner, draw_marker_radius, p_graphics, p_graphics_context);
                    draw_layer_change_marker(curr_incomplete.to_corner, draw_marker_radius, p_graphics, p_graphics_context);
                }
            }
        }
        if (length_violation == 0) {
            return;
        }
        // draw the length violation around every Pin of the net.
        Collection<board.Pin> net_pins = net.get_pins();
        for (board.Pin curr_pin : net_pins) {
            draw_length_violation_marker(curr_pin.get_center().to_float(), length_violation, p_graphics, p_graphics_context);
        }
    }

    static void draw_layer_change_marker(FloatPoint p_location, double p_radius, Graphics p_graphics, GraphicsContext p_graphics_context) {
        final int draw_width = 1;
        Color draw_color = p_graphics_context.get_incomplete_color();
        double draw_intensity = p_graphics_context.get_incomplete_color_intensity();
        FloatPoint[] draw_points = new FloatPoint[2];
        draw_points[0] = new FloatPoint(p_location.x - p_radius, p_location.y - p_radius);
        draw_points[1] = new FloatPoint(p_location.x + p_radius, p_location.y + p_radius);
        p_graphics_context.draw(draw_points, draw_width, draw_color, p_graphics, draw_intensity);
        draw_points[0] = new FloatPoint(p_location.x + p_radius, p_location.y - p_radius);
        draw_points[1] = new FloatPoint(p_location.x - p_radius, p_location.y + p_radius);
        p_graphics_context.draw(draw_points, draw_width, draw_color, p_graphics, draw_intensity);
    }

    static void draw_length_violation_marker(FloatPoint p_location, double p_diameter, Graphics p_graphics, GraphicsContext p_graphics_context) {
        final int draw_width = 1;
        Color draw_color = p_graphics_context.get_incomplete_color();
        double draw_intensity = p_graphics_context.get_incomplete_color_intensity();
        double circle_radius = 0.5 * Math.abs(p_diameter);
        p_graphics_context.draw_circle(p_location, circle_radius, draw_width, draw_color, p_graphics, draw_intensity);
        FloatPoint[] draw_points = new FloatPoint[2];
        draw_points[0] = new FloatPoint(p_location.x - circle_radius, p_location.y);
        draw_points[1] = new FloatPoint(p_location.x + circle_radius, p_location.y);
        p_graphics_context.draw(draw_points, draw_width, draw_color, p_graphics, draw_intensity);
        if (p_diameter > 0) {
            // draw also the vertical diameter to create a "+"
            draw_points[0] = new FloatPoint(p_location.x, p_location.y - circle_radius);
            draw_points[1] = new FloatPoint(p_location.x, p_location.y + circle_radius);
            p_graphics_context.draw(draw_points, draw_width, draw_color, p_graphics, draw_intensity);
        }
    }

    /**
     * Calculates an array of Item-connected_set pairs for the items of this
     * net. Pairs belonging to the same connected set are located next to each
     * other.
     */
    private NetItem[] calculate_net_items(Collection<Item> p_item_list) {
        NetItem[] result = new NetItem[p_item_list.size()];
        int curr_index = 0;
        while (!p_item_list.isEmpty()) {
            Item start_item = p_item_list.iterator().next();
            Collection<Item> curr_connected_set = start_item.get_connected_set(net.net_number);
            p_item_list.removeAll(curr_connected_set);
            for (Item curr_item : curr_connected_set) {
                if (curr_index >= result.length) {
                    Freerouter.logInfo("NetIncompletes.calculate_net_items: too many items");
                    return result;
                }
                result[curr_index] = new NetItem(curr_item, curr_connected_set);
                ++curr_index;
            }
        }
        if (curr_index < result.length) {
            Freerouter.logInfo("NetIncompletes.calculate_net_items: too few items");
        }
        return result;
    }

    /**
     * Joins p_from_connected_set to p_to_connected_set and updates the
     * connected sets of the items in p_net_items.
     */
    private void join_connected_sets(NetItem[] p_net_items, Collection<Item> p_from_connected_set, Collection<Item> p_to_connected_set) {
        for (NetItem curr_item : p_net_items) {
            if (curr_item.connected_set == p_from_connected_set) {
                p_to_connected_set.add(curr_item.item);
                curr_item.connected_set = p_to_connected_set;
            }
        }
    }

    /**
     * Collection of elements of class AirLine.
     */
    final Collection<RatsNest.AirLine> incompletes;

    /**
     * The length of the violation of the length restriction of the net, > 0, if
     * the cumulative trace length is to big, < 0, if the trace length is to
     * smalll, 0, if the thace length is ok or the net has no length
     * restrictions
     */
    private double length_violation = 0;

    private final Net net;
    private final double draw_marker_radius;

    private static class Edge implements Comparable<Edge> {

        private Edge(NetItem p_from_item, FloatPoint p_from_corner, NetItem p_to_item, FloatPoint p_to_corner) {
            from_item = p_from_item;
            from_corner = p_from_corner;
            to_item = p_to_item;
            to_corner = p_to_corner;
            length_square = p_to_corner.distance_square(p_from_corner);
        }

        public final NetItem from_item;
        public final FloatPoint from_corner;
        public final NetItem to_item;
        public final FloatPoint to_corner;
        public final double length_square;

        @Override
        public int compareTo(Edge p_other) {
            double result = length_square - p_other.length_square;
            if (result == 0) {
                // prevent result 0, so that edges with the same length as another edge are not skipped in the set
                result = from_corner.x - p_other.from_corner.x;
                if (result == 0) {
                    result = from_corner.y - p_other.from_corner.y;
                }
                if (result == 0) {
                    result = to_corner.x - p_other.to_corner.x;
                }
                if (result == 0) {
                    result = to_corner.y - p_other.to_corner.y;
                }
            }
            return datastructures.Signum.as_int(result);
        }
    }

    private static class NetItem implements PlanarDelaunayTriangulation.Storable {

        NetItem(Item p_item, Collection<Item> p_connected_set) {
            item = p_item;
            connected_set = p_connected_set;
        }

        @Override
        public Point[] get_triangulation_corners() {
            return item.get_ratsnest_corners();
        }

        final Item item;
        Collection<Item> connected_set;

    }
}
