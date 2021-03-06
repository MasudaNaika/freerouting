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
 * ComponentObstacleArea.java
 *
 * Created on 8. Mai 2005, 07:28
 */
package board;

import geometry.planar.Area;
import geometry.planar.Vector;
import java.awt.Color;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Describes areas of the board, where components arre not allowed.
 *
 * @author alfons
 */
public class ComponentObstacleArea extends ObstacleArea {

    /**
     * Creates a new instance of ComponentObstacleArea If p_is_obstacle ist
     * false, the new instance is not regarded as obstacle and used only for
     * displaying on the screen.
     */
    ComponentObstacleArea(Area p_area, int p_layer, Vector p_translation, double p_rotation_in_degree,
            boolean p_side_changed, int p_clearance_type, int p_id_no, int p_component_no,
            String p_name, FixedState p_fixed_state, BasicBoard p_board) {
        super(p_area, p_layer, p_translation, p_rotation_in_degree, p_side_changed, new int[0],
                p_clearance_type, p_id_no, p_component_no, p_name, p_fixed_state, p_board);
    }

    @Override
    public Item copy(int p_id_no) {
        return new ComponentObstacleArea(get_relative_area(), get_layer(), get_translation(),
                get_rotation_in_degree(), get_side_changed(), clearance_class_no(), p_id_no,
                get_component_no(), name, get_fixed_state(), board);
    }

    @Override
    public boolean is_obstacle(Item p_other) {
        return p_other != this && p_other instanceof ComponentObstacleArea
                && p_other.get_component_no() != get_component_no();
    }

    @Override
    public boolean is_trace_obstacle(int p_net_no) {
        return false;
    }

    @Override
    public boolean is_selected_by_filter(ItemSelectionFilter p_filter) {
        if (!is_selected_by_fixed_filter(p_filter)) {
            return false;
        }
        return p_filter.is_selected(ItemSelectionFilter.SelectableChoices.COMPONENT_KEEPOUT);
    }

    @Override
    public Color[] get_draw_colors(boardgraphics.GraphicsContext p_graphics_context) {
        return p_graphics_context.get_place_obstacle_colors();
    }

    @Override
    public double get_draw_intensity(boardgraphics.GraphicsContext p_graphics_context) {
        return p_graphics_context.get_place_obstacle_color_intensity();
    }

    public boolean is_selectrd_by_filter(ItemSelectionFilter p_filter) {
        if (!is_selected_by_fixed_filter(p_filter)) {
            return false;
        }
        return p_filter.is_selected(ItemSelectionFilter.SelectableChoices.COMPONENT_KEEPOUT);
    }

    @Override
    public void print_info(ObjectInfoPanel p_window, Locale p_locale) {
        ResourceBundle resources
                = ResourceBundle.getBundle("board.resources.ObjectInfoPanel", p_locale);
        p_window.append_bold(resources.getString("component_keepout"));
        print_shape_info(p_window, p_locale);
        print_clearance_info(p_window, p_locale);
        print_clearance_violation_info(p_window, p_locale);
        p_window.newline();
    }

    @Override
    public Object clone() {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }
}
