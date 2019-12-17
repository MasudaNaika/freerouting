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
 * OtherColorTableModel.java
 *
 * Created on 5. August 2003, 07:39
 */
package boardgraphics;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Stores the colors used for the background and highlighting.
 *
 * @author Alfons Wirtz
 */
public class OtherColorTableModel extends ColorTableModel implements Serializable {

    public OtherColorTableModel(Locale p_locale) {
        super(1, p_locale);
        data[0] = new Color[ColumnNames.values().length];
        Object[] curr_row = data[0];
        curr_row[ColumnNames.BACKGROUND.ordinal()] = Color.black;   //new Color(70, 70, 70);
        curr_row[ColumnNames.HIGHLIGHT.ordinal()] = Color.white;
        curr_row[ColumnNames.INCOMPLETES.ordinal()] = Color.white;
        curr_row[ColumnNames.OUTLINE.ordinal()] = new Color(100, 150, 255);
        curr_row[ColumnNames.VIOLATIONS.ordinal()] = Color.magenta;
        curr_row[ColumnNames.COMPONENT_FRONT.ordinal()] = Color.blue;
        curr_row[ColumnNames.COMPONENT_BACK.ordinal()] = Color.red;
        curr_row[ColumnNames.LENGTH_MATCHING_AREA.ordinal()] = Color.green;
    }

    public OtherColorTableModel(ObjectInputStream p_stream) throws IOException, ClassNotFoundException {
        super(p_stream);
    }

    /**
     * Copy construcror.
     */
    public OtherColorTableModel(OtherColorTableModel p_item_color_model) {
        super(p_item_color_model.data.length, p_item_color_model.locale);
        for (int i = 0; i < data.length; ++i) {
            data[i] = new Object[p_item_color_model.data[i].length];
            System.arraycopy(p_item_color_model.data[i], 0, data[i], 0, data[i].length);
        }
    }

    @Override
    public int getColumnCount() {
        return ColumnNames.values().length;
    }

    @Override
    public String getColumnName(int p_col) {
        ResourceBundle resources
                = ResourceBundle.getBundle("boardgraphics.resources.ColorTableModel", locale);
        return resources.getString(ColumnNames.values()[p_col].toString());
    }

    @Override
    public boolean isCellEditable(int p_row, int p_col) {
        return true;
    }

    public Color get_background_color() {
        return (Color) (data[0][ColumnNames.BACKGROUND.ordinal()]);
    }

    public Color get_hilight_color() {
        return (Color) (data[0][ColumnNames.HIGHLIGHT.ordinal()]);
    }

    public Color get_incomplete_color() {
        return (Color) (data[0][ColumnNames.INCOMPLETES.ordinal()]);
    }

    public Color get_outline_color() {
        return (Color) (data[0][ColumnNames.OUTLINE.ordinal()]);
    }

    public Color get_violations_color() {
        return (Color) (data[0][ColumnNames.VIOLATIONS.ordinal()]);
    }

    public Color get_component_color(boolean p_front) {
        Color result;
        if (p_front) {
            result = (Color) (data[0][ColumnNames.COMPONENT_FRONT.ordinal()]);
        } else {
            result = (Color) (data[0][ColumnNames.COMPONENT_BACK.ordinal()]);
        }
        return result;
    }

    public Color get_length_matching_area_color() {
        return (Color) (data[0][ColumnNames.LENGTH_MATCHING_AREA.ordinal()]);
    }

    public void set_background_color(Color p_color) {
        data[0][ColumnNames.BACKGROUND.ordinal()] = p_color;
    }

    public void set_hilight_color(Color p_color) {
        data[0][ColumnNames.HIGHLIGHT.ordinal()] = p_color;
    }

    public void set_incomplete_color(Color p_color) {
        data[0][ColumnNames.INCOMPLETES.ordinal()] = p_color;
    }

    public void set_violations_color(Color p_color) {
        data[0][ColumnNames.VIOLATIONS.ordinal()] = p_color;
    }

    public void set_outline_color(Color p_color) {
        data[0][ColumnNames.OUTLINE.ordinal()] = p_color;
    }

    public void set_component_color(Color p_color, boolean p_front) {
        if (p_front) {
            data[0][ColumnNames.COMPONENT_FRONT.ordinal()] = p_color;
        } else {
            data[0][ColumnNames.COMPONENT_BACK.ordinal()] = p_color;
        }
    }

    public void set_length_matching_area_color(Color p_color) {
        data[0][ColumnNames.LENGTH_MATCHING_AREA.ordinal()] = p_color;
    }

    private enum ColumnNames {
        BACKGROUND, HIGHLIGHT, INCOMPLETES, VIOLATIONS, OUTLINE, COMPONENT_FRONT, COMPONENT_BACK, LENGTH_MATCHING_AREA
    }
}
