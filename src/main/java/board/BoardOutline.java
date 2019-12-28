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
 * BoardOutline.java
 *
 * Created on 18. August 2004, 07:24
 */
package board;

import boardgraphics.GraphicsContext;
import geometry.planar.Area;
import geometry.planar.FloatPoint;
import geometry.planar.IntBox;
import geometry.planar.IntPoint;
import geometry.planar.PolylineArea;
import geometry.planar.PolylineShape;
import geometry.planar.TileShape;
import geometry.planar.Vector;
import net.freerouting.Freerouter;
import java.awt.Color;
import java.awt.Graphics;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Class describing a board outline.
 *
 * @author alfons
 */
public class BoardOutline extends Item implements Serializable {

    /**
     * Creates a new instance of BoardOutline
     */
    public BoardOutline(PolylineShape[] p_shapes, int p_clearance_class_no, int p_id_no, BasicBoard p_board) {
        super(new int[0], p_clearance_class_no, p_id_no, 0, FixedState.SYSTEM_FIXED, p_board);
        shapes = p_shapes;
    }

    @Override
    public int tile_shape_count() {
        int result;
        if (keepout_outside_outline) {
            TileShape[] tile_shapes = get_keepout_area().split_to_convex();
            if (tile_shapes == null) {
                // an error accured while dividing the area
                result = 0;
            } else {
                result = tile_shapes.length * board.layer_structure.arr.length;
            }
        } else {
            result = line_count() * board.layer_structure.arr.length;
        }
        return result;
    }

    @Override
    public int shape_layer(int p_index) {
        int shape_count = tile_shape_count();
        int result;
        if (shape_count > 0) {
            result = p_index * board.layer_structure.arr.length / shape_count;
        } else {
            result = 0;
        }
        if (result < 0 || result >= board.layer_structure.arr.length) {
            Freerouter.logInfo("BoardOutline.shape_layer: p_index out of range");
        }
        return result;
    }

    @Override
    public boolean is_obstacle(Item p_other) {
        return !(p_other instanceof BoardOutline || p_other instanceof ObstacleArea);
    }

    @Override
    public IntBox bounding_box() {
        IntBox result = IntBox.EMPTY;
        for (PolylineShape curr_shape : shapes) {
            result = result.union(curr_shape.bounding_box());
        }
        return result;
    }

    @Override
    public int first_layer() {
        return 0;
    }

    @Override
    public int last_layer() {
        return board.layer_structure.arr.length - 1;
    }

    @Override
    public boolean is_on_layer(int p_layer) {
        return true;
    }

    @Override
    public void translate_by(Vector p_vector) {
        for (PolylineShape curr_shape : shapes) {
            curr_shape = curr_shape.translate_by(p_vector);
        }
        if (keepout_area != null) {
            keepout_area = keepout_area.translate_by(p_vector);
        }
        keepout_lines = null;
    }

    @Override
    public void turn_90_degree(int p_factor, IntPoint p_pole) {
        for (PolylineShape curr_shape : shapes) {
            curr_shape = curr_shape.turn_90_degree(p_factor, p_pole);
        }
        if (keepout_area != null) {
            keepout_area = keepout_area.turn_90_degree(p_factor, p_pole);
        }
        keepout_lines = null;
    }

    @Override
    public void rotate_approx(double p_angle_in_degree, FloatPoint p_pole) {
        double angle = Math.toRadians(p_angle_in_degree);
        for (PolylineShape curr_shape : shapes) {
            curr_shape = curr_shape.rotate_approx(angle, p_pole);

        }
        if (keepout_area != null) {
            keepout_area = keepout_area.rotate_approx(angle, p_pole);
        }
        keepout_lines = null;
    }

    @Override
    public void change_placement_side(IntPoint p_pole) {
        for (PolylineShape curr_shape : shapes) {
            curr_shape = curr_shape.mirror_vertical(p_pole);
        }
        if (keepout_area != null) {
            keepout_area = keepout_area.mirror_vertical(p_pole);
        }
        keepout_lines = null;
    }

    @Override
    public double get_draw_intensity(GraphicsContext p_graphics_context) {
        return 1;
    }

    @Override
    public int get_draw_priority() {
        return boardgraphics.Drawable.MAX_DRAW_PRIORITY;
    }

    public int shape_count() {
        return shapes.length;
    }

    public PolylineShape get_shape(int p_index) {
        if (p_index < 0 || p_index >= shapes.length) {
            Freerouter.logInfo("BoardOutline.get_shape: p_index out of range");
            return null;
        }
        return shapes[p_index];
    }

    @Override
    public boolean is_selected_by_filter(ItemSelectionFilter p_filter) {
        if (!is_selected_by_fixed_filter(p_filter)) {
            return false;
        }
        return p_filter.is_selected(ItemSelectionFilter.SelectableChoices.BOARD_OUTLINE);
    }

    @Override
    public Color[] get_draw_colors(GraphicsContext p_graphics_context) {
        Color[] color_arr = new Color[board.layer_structure.arr.length];
        Color draw_color = p_graphics_context.get_outline_color();
        Arrays.fill(color_arr, draw_color);
        return color_arr;
    }

    /**
     * The board shape outside the outline curves, where a keepout will be
     * generated The outline curves are holes of the keepout_area.
     */
    Area get_keepout_area() {
        if (keepout_area == null) {
            PolylineShape[] hole_arr = new PolylineShape[shapes.length];
            System.arraycopy(shapes, 0, hole_arr, 0, hole_arr.length);
            keepout_area = new PolylineArea(board.bounding_box, hole_arr);
        }
        return keepout_area;
    }

    TileShape[] get_keepout_lines() {
        if (keepout_lines == null) {
            keepout_lines = new TileShape[0];
        }
        return keepout_lines;
    }

    @Override
    public void draw(Graphics p_g, GraphicsContext p_graphics_context, Color[] p_color_arr, double p_intensity) {
        if (p_graphics_context == null || p_intensity <= 0) {
            return;
        }
        for (PolylineShape curr_shape : shapes) {
            FloatPoint[] draw_corners = curr_shape.corner_approx_arr();
            FloatPoint[] closed_draw_corners = new FloatPoint[draw_corners.length + 1];
            System.arraycopy(draw_corners, 0, closed_draw_corners, 0, draw_corners.length);
            closed_draw_corners[closed_draw_corners.length - 1] = draw_corners[0];
            p_graphics_context.draw(closed_draw_corners, HALF_WIDTH, p_color_arr[0], p_g, p_intensity);
        }
    }

    @Override
    public Item copy(int p_id_no) {
        return new BoardOutline(shapes, clearance_class_no(), p_id_no, board);
    }

    @Override
    public void print_info(ObjectInfoPanel p_window, Locale p_locale) {
        ResourceBundle resources
                = ResourceBundle.getBundle("board.resources.ObjectInfoPanel", p_locale);
        p_window.append_bold(resources.getString("board_outline"));
        print_clearance_info(p_window, p_locale);
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

    /**
     * Returns, if keepout is generated outside the board outline. Otherwise
     * only the line shapes of the outlines are inserted as keepout.
     */
    public boolean keepout_outside_outline_generated() {
        return keepout_outside_outline;
    }

    /**
     * Makes the area outside this Outline to Keepout, if p_valus = true.
     * Reinserts this Outline into the search trees, if the value changes.
     */
    public void generate_keepout_outside(boolean p_value) {
        if (p_value == keepout_outside_outline) {
            return;
        }
        keepout_outside_outline = p_value;
        if (board == null || board.search_tree_manager == null) {
            return;
        }
        board.search_tree_manager.remove(this);
        board.search_tree_manager.insert(this);

    }

    /**
     * Returns the sum of the lines of all outline poligons.
     */
    public int line_count() {
        int result = 0;
        for (PolylineShape curr_shape : shapes) {
            result += curr_shape.border_line_count();
        }
        return result;
    }

    /**
     * Returns the half width of the lines of this outline.
     */
    public int get_half_width() {
        return HALF_WIDTH;
    }

    @Override
    protected TileShape[] calculate_tree_shapes(ShapeSearchTree p_search_tree) {
        return p_search_tree.calculate_tree_shapes(this);
    }
    /**
     * The board shapes inside the outline curves.
     */
    private PolylineShape[] shapes;
    /**
     * The board shape outside the outline curves, where a keepout will be
     * generated The outline curves are holes of the keepout_area.
     */
    private Area keepout_area = null;
    /**
     * Used instead of keepout_area if only the line shapes of the outlines are
     * inserted as keepout.
     */
    private TileShape[] keepout_lines = null;
    private boolean keepout_outside_outline = false;
    private static final int HALF_WIDTH = 100;

}
