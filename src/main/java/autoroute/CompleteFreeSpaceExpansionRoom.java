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
 * CompleteFreeSpaceExpansionRoom.java
 *
 * Created on 10. Februar 2004, 10:12
 */
package autoroute;

import board.Connectable;
import board.Item;
import board.SearchTreeObject;
import board.ShapeSearchTree;
import datastructures.ShapeTree;
import geometry.planar.TileShape;
import gui.Freerouter;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Collection;
import java.util.LinkedList;

/**
 * An expansion room, whose shape is completely calculated, so that it can be
 * stored in a shape tree.
 *
 * @author Alfons Wirtz
 */
public class CompleteFreeSpaceExpansionRoom extends FreeSpaceExpansionRoom implements CompleteExpansionRoom, SearchTreeObject {

    /**
     * Creates a new instance of CompleteFreeSpaceExpansionRoom
     */
    public CompleteFreeSpaceExpansionRoom(TileShape p_shape, int p_layer, int p_id_no) {
        super(p_shape, p_layer);
        target_doors = new LinkedList<>();
        id_no = p_id_no;
    }

    @Override
    public void set_search_tree_entries(ShapeTree.Leaf[] p_entries, ShapeTree p_tree) {
        tree_entries = p_entries;
    }

    @Override
    public int compareTo(Object p_other) {
        int result;
        if (p_other instanceof FreeSpaceExpansionRoom) {
            result = ((CompleteFreeSpaceExpansionRoom) p_other).id_no - id_no;
        } else {
            result = -1;
        }
        return result;
    }

    /**
     * Removes the tree entries of this roomm from p_shape_tree.
     */
    public void remove_from_tree(ShapeTree p_shape_tree) {
        p_shape_tree.remove(tree_entries);
    }

    @Override
    public int tree_shape_count(ShapeTree p_shape_tree) {
        return 1;
    }

    @Override
    public TileShape get_tree_shape(ShapeTree p_shape_tree, int p_index) {
        return get_shape();
    }

    @Override
    public int shape_layer(int p_index) {
        return get_layer();
    }

    @Override
    public boolean is_obstacle(int p_net_no) {
        return true;
    }

    @Override
    public boolean is_trace_obstacle(int p_net_no) {
        return true;
    }

    /**
     * Will be called, when the room overlaps with net dependent objects.
     */
    public void set_net_dependent() {
        room_is_net_dependent = true;
    }

    /**
     * Returns, if the room overlaps with net dependent objects. In this case it
     * cannot be retained, when the net number changes in autorouting.
     */
    public boolean is_net_dependent() {
        return room_is_net_dependent;
    }

    /**
     * Returns the list doors to target items of this room
     */
    @Override
    public Collection<TargetItemExpansionDoor> get_target_doors() {
        return target_doors;
    }

    /**
     * Adds p_door to the list of target doors of this room.
     */
    public void add_target_door(TargetItemExpansionDoor p_door) {
        target_doors.add(p_door);
    }

    @Override
    public boolean remove_door(ExpandableObject p_door) {
        boolean result;
        if (p_door instanceof TargetItemExpansionDoor) {
            result = target_doors.remove(p_door);
        } else {
            result = super.remove_door(p_door);
        }
        return result;
    }

    @Override
    public SearchTreeObject get_object() {
        return this;
    }

    /**
     * Calculates the doors to the start and destination items of the autoroute
     * algorithm.
     */
    public void calculate_target_doors(ShapeTree.TreeEntry p_own_net_object, int p_net_no, ShapeSearchTree p_autoroute_search_tree) {
        set_net_dependent();

        if (p_own_net_object.object instanceof Connectable) {
            Connectable curr_object = (Connectable) p_own_net_object.object;
            if (curr_object.contains_net(p_net_no)) {
                TileShape curr_connection_shape
                        = curr_object.get_trace_connection_shape(p_autoroute_search_tree, p_own_net_object.shape_index_in_object);
                if (curr_connection_shape != null && get_shape().intersects(curr_connection_shape)) {
                    Item curr_item = (Item) curr_object;
                    TargetItemExpansionDoor new_target_door
                            = new TargetItemExpansionDoor(curr_item, p_own_net_object.shape_index_in_object, this,
                                    p_autoroute_search_tree);
                    add_target_door(new_target_door);
                }
            }
        }
    }

    /**
     * Draws the shape of this room.
     */
    @Override
    public void draw(Graphics p_graphics, boardgraphics.GraphicsContext p_graphics_context, double p_intensity) {
        Color draw_color = p_graphics_context.get_trace_colors(false)[get_layer()];
        double layer_visibility = p_graphics_context.get_layer_visibility(get_layer());
        p_graphics_context.fill_area(get_shape(), p_graphics, draw_color, p_intensity * layer_visibility);
        p_graphics_context.draw_boundary(get_shape(), 0, draw_color, p_graphics, layer_visibility);
    }

    /**
     * Check, if this FreeSpaceExpansionRoom is valid.
     */
    public boolean validate(AutorouteEngine p_autoroute_engine) {
        boolean result = true;
        Collection<ShapeTree.TreeEntry> overlapping_objects = new LinkedList<>();
        int[] net_no_arr = {p_autoroute_engine.get_net_no()};
        p_autoroute_engine.autoroute_search_tree.overlapping_tree_entries(get_shape(), get_layer(),
                net_no_arr, overlapping_objects);
        for (ShapeTree.TreeEntry curr_entry : overlapping_objects) {
            if (curr_entry.object == this) {
                continue;
            }
            SearchTreeObject curr_object = (SearchTreeObject) curr_entry.object;
            if (!curr_object.is_trace_obstacle(p_autoroute_engine.get_net_no())) {
                continue;
            }
            if (curr_object.shape_layer(curr_entry.shape_index_in_object) != get_layer()) {
                continue;
            }
            TileShape curr_shape
                    = curr_object.get_tree_shape(p_autoroute_engine.autoroute_search_tree, curr_entry.shape_index_in_object);
            TileShape intersection = get_shape().intersection(curr_shape);
            if (intersection.dimension() > 1) {
                Freerouter.logInfo("ExpansionRoom overlap conflict");
                result = false;
            }
        }
        return result;
    }

    /**
     * Removes all doors and target doors from this room.
     */
    @Override
    public void clear_doors() {
        super.clear_doors();
        target_doors = new LinkedList<>();
    }

    @Override
    public void reset_doors() {
        super.reset_doors();
        for (ExpandableObject curr_door : target_doors) {
            curr_door.reset();
        }
    }

    /**
     * The array of entries in the SearchTree. Consists of just one element
     */
    private ShapeTree.Leaf[] tree_entries = null;

    //** identification number for implementong the Comparable interfacw */
    private final int id_no;

    /**
     * The list of doors to items of the own net
     */
    private Collection<TargetItemExpansionDoor> target_doors;

    private boolean room_is_net_dependent = false;
}
