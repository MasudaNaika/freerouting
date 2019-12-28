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
 * ObjectListWindowWithFilter.java
 *
 * Created on 24. Maerz 2005, 10:10
 */
package gui;

import net.freerouting.Freerouter;
import java.awt.BorderLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ResourceBundle;
import javax.swing.*;

/**
 * Abstract class for windows displaying a list of objects The object name can
 * be filttered by an alphanumeric input string. * @author Alfons Wirtz
 */
public abstract class WindowObjectListWithFilter extends WindowObjectList {

    /**
     * Creates a new instance of ObjectListWindowWithFilter
     */
    public WindowObjectListWithFilter(BoardFrame p_board_frame) {
        super(p_board_frame);
        ResourceBundle resources
                = ResourceBundle.getBundle("gui.resources.WindowObjectList", p_board_frame.get_locale());
        JPanel input_panel = new JPanel();
        south_panel.add(input_panel, BorderLayout.SOUTH);

        JLabel filter_label = new JLabel(resources.getString("filter"));
        input_panel.add(filter_label, BorderLayout.WEST);

        filter_string = new JTextField(10);
        filter_string.setText("");
        input_panel.add(filter_string, BorderLayout.EAST);
    }

    /**
     * Adds p_object to the list only if its name matches the filter.
     */
    @Override
    protected void add_to_list(Object p_object) {
        String curr_filter_string = filter_string.getText().trim();
        boolean object_matches;
        if (curr_filter_string.length() == 0) {
            object_matches = true;
        } else {
            object_matches = p_object.toString().contains(curr_filter_string);
        }
        if (object_matches) {
            super.add_to_list(p_object);
        }
    }

    /**
     * Returns the filter text string of this window.
     */
    public SnapshotInfo get_snapshot_info() {
        int[] selected_indices;
        if (list != null) {
            selected_indices = list.getSelectedIndices();
        } else {
            selected_indices = new int[0];
        }
        return new SnapshotInfo(filter_string.getText(), selected_indices);
    }

    public void set_snapshot_info(SnapshotInfo p_snapshot_info) {
        if (!p_snapshot_info.filter.equals(filter_string.getText())) {
            filter_string.setText(p_snapshot_info.filter);
            recalculate();
        }
        if (list != null && p_snapshot_info.selected_indices.length > 0) {
            list.setSelectedIndices(p_snapshot_info.selected_indices);
        }
    }

    /**
     * Saves also the filter string to disk.
     */
    @Override
    public void save(ObjectOutputStream p_object_stream) {
        try {
            p_object_stream.writeObject(filter_string.getText());
        } catch (IOException e) {
            Freerouter.logError("WindowObjectListWithFilter.save: save failed");
        }
        super.save(p_object_stream);
    }

    @Override
    public boolean read(ObjectInputStream p_object_stream) {
        try {
            String curr_string = (String) p_object_stream.readObject();
            filter_string.setText(curr_string);
        } catch (IOException | ClassNotFoundException e) {
            Freerouter.logError("WindowObjectListWithFilter.read: read failed");
        }
        return super.read(p_object_stream);
    }

    private final JTextField filter_string;

    /**
     * Information to be stored in a SnapShot.
     */
    public static class SnapshotInfo implements Serializable {

        private SnapshotInfo(String p_filter, int[] p_selected_indices) {
            filter = p_filter;
            selected_indices = p_selected_indices;
        }
        private final String filter;
        private final int[] selected_indices;
    }
}
