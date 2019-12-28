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
 * Via.java
 *
 * Created on 5. Juni 2003, 10:36
 */
package board;

import geometry.planar.IntPoint;
import geometry.planar.Point;
import geometry.planar.Shape;
import geometry.planar.TileShape;
import geometry.planar.Vector;
import net.freerouting.Freerouter;
import java.awt.Color;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import library.Padstack;

/**
 * Class describing the functionality of an electrical Item on the board, which
 * may have a shape on several layer, whose geometry is described by a padstack.
 *
 * @author Alfons Wirtz
 */
public class Via extends DrillItem implements Serializable {

    /**
     * Creates a new instance of Via with the input parameters
     */
    public Via(Padstack p_padstack, Point p_center, int[] p_net_no_arr, int p_clearance_type, int p_id_no,
            int p_group_no, FixedState p_fixed_state, boolean p_attach_allowed, BasicBoard p_board) {
        super(p_center, p_net_no_arr, p_clearance_type, p_id_no, p_group_no, p_fixed_state, p_board);
        padstack = p_padstack;
        attach_allowed = p_attach_allowed;
    }

    @Override
    public Item copy(int p_id_no) {
        return new Via(padstack, get_center(), net_no_arr, clearance_class_no(), p_id_no, get_component_no(),
                get_fixed_state(), attach_allowed, board);
    }

    @Override
    public Shape get_shape(int p_index) {
        if (padstack == null) {
            Freerouter.logInfo("Via.get_shape: padstack is null");
            return null;
        }
        if (precalculated_shapes == null) {
            precalculated_shapes = new Shape[padstack.to_layer() - padstack.from_layer() + 1];
            for (int i = 0; i < precalculated_shapes.length; ++i) {
                int padstack_layer = i + first_layer();
                Vector translate_vector = get_center().difference_by(Point.ZERO);
                Shape curr_shape = padstack.get_shape(padstack_layer);

                if (curr_shape == null) {
                    precalculated_shapes[i] = null;
                } else {
                    precalculated_shapes[i] = (Shape) curr_shape.translate_by(translate_vector);
                }
            }
        }
        return precalculated_shapes[p_index];
    }

    @Override
    public Padstack get_padstack() {
        return padstack;
    }

    public void set_padstack(Padstack p_padstack) {
        padstack = p_padstack;
    }

    @Override
    public boolean is_route() {
        return !is_user_fixed() && net_count() > 0;
    }

    @Override
    public boolean is_obstacle(Item p_other) {
        if (p_other == this || p_other instanceof ComponentObstacleArea) {
            return false;
        }
        if ((p_other instanceof ConductionArea) && !((ConductionArea) p_other).get_is_obstacle()) {
            return false;
        }
        if (!p_other.shares_net(this)) {
            return true;
        }
        if (p_other instanceof Trace) {
            return false;
        }
        if (attach_allowed && p_other instanceof Pin && ((Pin) p_other).drill_allowed()) {
            return false;
        }
        return true;
    }

    /**
     * Checks, if the Via has contacts on at most 1 layer.
     */
    @Override
    public boolean is_tail() {
        Collection<Item> contact_list = get_normal_contacts();
        if (contact_list.size() <= 1) {
            return true;
        }
        Iterator<Item> it = contact_list.iterator();
        Item curr_contact_item = it.next();
        int first_contact_first_layer = curr_contact_item.first_layer();
        int first_contact_last_layer = curr_contact_item.last_layer();
        while (it.hasNext()) {
            curr_contact_item = it.next();
            if (curr_contact_item.first_layer() != first_contact_first_layer
                    || curr_contact_item.last_layer() != first_contact_last_layer) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void change_placement_side(IntPoint p_pole) {
        if (board == null) {
            return;
        }
        Padstack new_padstack = board.library.get_mirrored_via_padstack(padstack);
        if (new_padstack == null) {
            return;
        }
        padstack = new_padstack;
        super.change_placement_side(p_pole);
        clear_derived_data();
    }

    public autoroute.ExpansionDrill get_autoroute_drill_info(ShapeSearchTree p_autoroute_tree) {
        if (autoroute_drill_info == null) {
            autoroute.ItemAutorouteInfo via_autoroute_info = get_autoroute_info();
            TileShape curr_drill_shape = TileShape.get_instance(get_center());
            autoroute_drill_info
                    = new autoroute.ExpansionDrill(curr_drill_shape, get_center(), first_layer(), last_layer());
            int via_layer_count = last_layer() - first_layer() + 1;
            for (int i = 0; i < via_layer_count; ++i) {
                autoroute_drill_info.room_arr[i] = via_autoroute_info.get_expansion_room(i, p_autoroute_tree);
            }
        }
        return autoroute_drill_info;
    }

    @Override
    public void clear_derived_data() {
        super.clear_derived_data();
        precalculated_shapes = null;
        autoroute_drill_info = null;
    }

    @Override
    public void clear_autoroute_info() {
        super.clear_autoroute_info();
        autoroute_drill_info = null;
    }

    @Override
    public boolean is_selected_by_filter(ItemSelectionFilter p_filter) {
        if (!is_selected_by_fixed_filter(p_filter)) {
            return false;
        }
        return p_filter.is_selected(ItemSelectionFilter.SelectableChoices.VIAS);
    }

    @Override
    public Color[] get_draw_colors(boardgraphics.GraphicsContext p_graphics_context) {
        Color[] result;
        if (net_count() == 0) {
            // display unconnected vias as obstacles
            result = p_graphics_context.get_obstacle_colors();

        } else if (first_layer() >= last_layer()) {
            // display vias with only one layer as pins
            result = p_graphics_context.get_pin_colors();
        } else {
            result = p_graphics_context.get_via_colors(is_user_fixed());
        }
        return result;
    }

    @Override
    public double get_draw_intensity(boardgraphics.GraphicsContext p_graphics_context) {
        double result;
        if (net_count() == 0) {
            // display unconnected vias as obstacles
            result = p_graphics_context.get_obstacle_color_intensity();

        } else if (first_layer() >= last_layer()) {
            // display vias with only one layer as pins
            result = p_graphics_context.get_pin_color_intensity();
        } else {
            result = p_graphics_context.get_via_color_intensity();
        }
        return result;
    }

    @Override
    public void print_info(ObjectInfoPanel p_window, Locale p_locale) {
        ResourceBundle resources
                = ResourceBundle.getBundle("board.resources.ObjectInfoPanel", p_locale);
        p_window.append_bold(resources.getString("via"));
        p_window.append(" " + resources.getString("at"));
        p_window.append(get_center().to_float());
        p_window.append(", " + resources.getString("padstack"));
        p_window.append(padstack.name, resources.getString("padstack_info"), padstack);
        print_connectable_item_info(p_window, p_locale);
        p_window.newline();
    }

    @Override
    public boolean write(ObjectOutputStream p_stream) {
        try {
            p_stream.writeObject(this);
        } catch (IOException e) {
            Freerouter.logError(e);
            return false;
        }
        return true;
    }
    private Padstack padstack;
    /**
     * True, if coppersharing of this via with smd pins of the same net is
     * allowed.
     */
    public final boolean attach_allowed;
    transient private Shape[] precalculated_shapes = null;
    /**
     * Temporary data used in the autoroute algorithm.
     */
    transient private autoroute.ExpansionDrill autoroute_drill_info = null;

    @Override
    public Object clone() {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }
}
