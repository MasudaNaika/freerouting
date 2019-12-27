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
 * SnapshotFrame.java
 *
 * Created on 9. November 2004, 09:42
 */
package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ResourceBundle;
import javax.swing.*;

/**
 * Window handling snapshots of the interactive situation.
 *
 * @author Alfons Wirtz
 */
public class WindowSnapshot extends BoardSavableSubWindow {

    /**
     * Creates a new instance of SnapshotFrame
     */
    public WindowSnapshot(BoardFrame p_board_frame) {
        board_frame = p_board_frame;
        settings_window = new WindowSnapshotSettings(p_board_frame);
        resources = ResourceBundle.getBundle("gui.resources.WindowSnapshot", p_board_frame.get_locale());
        setTitle(resources.getString("title"));

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // create main panel
        final JPanel main_panel = new JPanel();
        getContentPane().add(main_panel);
        main_panel.setLayout(new BorderLayout());

        // create goto button
        JButton goto_button = new JButton(resources.getString("goto_snapshot"));
        goto_button.setToolTipText(resources.getString("goto_tooltip"));
        goto_button.addActionListener(ae -> goto_selected());
        main_panel.add(goto_button, BorderLayout.NORTH);

        // create snapshot list
        list = new JList(list_model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        list.setVisibleRowCount(5);
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() > 1) {
                    goto_selected();
                }
            }
        });

        JScrollPane list_scroll_pane = new JScrollPane(list);
        list_scroll_pane.setPreferredSize(new Dimension(200, 100));
        main_panel.add(list_scroll_pane, BorderLayout.CENTER);

        // create the south panel
        final JPanel south_panel = new JPanel();
        main_panel.add(south_panel, BorderLayout.SOUTH);
        GridBagLayout gridbag = new GridBagLayout();
        south_panel.setLayout(gridbag);
        GridBagConstraints gridbag_constraints = new GridBagConstraints();
        gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;

        // create panel to add a new snapshot
        final JPanel add_panel = new JPanel();
        gridbag.setConstraints(add_panel, gridbag_constraints);
        add_panel.setLayout(new BorderLayout());
        south_panel.add(add_panel);

        JButton add_button = new JButton(resources.getString("create"));
        AddListener add_listener = new AddListener();
        add_button.addActionListener(add_listener);
        add_panel.add(add_button, BorderLayout.WEST);

        name_field = new JTextField(10);
        name_field.setText(resources.getString("snapshot") + " 1");
        add_panel.add(name_field, BorderLayout.EAST);

        // create delete buttons
        JButton delete_button = new JButton(resources.getString("remove"));
        delete_button.addActionListener(ae -> {
            Object selected_snapshot = list.getSelectedValue();
            if (selected_snapshot != null) {
                list_model.removeElement(selected_snapshot);
            }
        });
        gridbag.setConstraints(delete_button, gridbag_constraints);
        south_panel.add(delete_button);

        JButton delete_all_button = new JButton(resources.getString("remove_all"));
        delete_all_button.addActionListener(ae -> list_model.removeAllElements());
        gridbag.setConstraints(delete_all_button, gridbag_constraints);
        south_panel.add(delete_all_button);

        // create button for the snapshot settings
        JButton settings_button = new JButton(resources.getString("settings"));
        settings_button.setToolTipText(resources.getString("settings_tooltip"));
        SettingsListener settings_listener = new SettingsListener();
        settings_button.addActionListener(settings_listener);
        gridbag.setConstraints(delete_all_button, gridbag_constraints);
        south_panel.add(settings_button);

        p_board_frame.set_context_sensitive_help(this, "WindowSnapshots");

        pack();
    }

    @Override
    public void dispose() {
        settings_window.dispose();
        super.dispose();
    }

    @Override
    public void parent_iconified() {
        settings_window.parent_iconified();
        super.parent_iconified();
    }

    @Override
    public void parent_deiconified() {
        settings_window.parent_deiconified();
        super.parent_deiconified();
    }

    /**
     * Reads the data of this frame from disk. Returns false, if the reading
     * failed.
     */
    @Override
    public boolean read(ObjectInputStream p_object_stream) {
        try {
            SavedAttributes saved_attributes = (SavedAttributes) p_object_stream.readObject();
            snapshot_count = saved_attributes.snapshot_count;
            list_model = saved_attributes.list_model;
            list.setModel(list_model);
            String next_default_name = "snapshot " + Integer.toString(snapshot_count + 1);
            name_field.setText(next_default_name);
            setLocation(saved_attributes.location);
            setVisible(saved_attributes.is_visible);
            settings_window.read(p_object_stream);
            return true;
        } catch (IOException | ClassNotFoundException e) {
            Freerouter.logError("VisibilityFrame.read_attriutes: read failed");
            return false;
        }
    }

    /**
     * Saves this frame to disk.
     */
    @Override
    public void save(ObjectOutputStream p_object_stream) {
        SavedAttributes saved_attributes = new SavedAttributes(list_model, snapshot_count, getLocation(), isVisible());
        try {
            p_object_stream.writeObject(saved_attributes);
        } catch (IOException e) {
            Freerouter.logError("VisibilityFrame.save_attriutes: save failed");
        }
        settings_window.save(p_object_stream);
    }

    void goto_selected() {
        int index = list.getSelectedIndex();
        if (index >= 0 && list_model.getSize() > index) {
            interactive.BoardHandling board_handling = board_frame.board_panel.board_handling;
            interactive.SnapShot curr_snapshot = (interactive.SnapShot) list_model.elementAt(index);

            curr_snapshot.go_to(board_handling);

            if (curr_snapshot.settings.get_snapshot_attributes().object_colors) {
                board_handling.graphics_context.item_color_table
                        = new boardgraphics.ItemColorTableModel(curr_snapshot.graphics_context.item_color_table);
                board_handling.graphics_context.other_color_table
                        = new boardgraphics.OtherColorTableModel(curr_snapshot.graphics_context.other_color_table);

                board_frame.color_manager.set_table_models(board_handling.graphics_context);
            }

            if (curr_snapshot.settings.get_snapshot_attributes().display_region) {
                java.awt.Point viewport_position = curr_snapshot.copy_viewport_position();
                if (viewport_position != null) {
                    board_handling.graphics_context.coordinate_transform = new boardgraphics.CoordinateTransform(curr_snapshot.graphics_context.coordinate_transform);
                    java.awt.Dimension panel_size = board_handling.graphics_context.get_panel_size();
                    board_frame.board_panel.setSize(panel_size);
                    board_frame.board_panel.setPreferredSize(panel_size);
                    board_frame.board_panel.set_viewport_position(viewport_position);
                }
            }

            board_frame.refresh_windows();
            board_frame.hilight_selected_button();
            board_frame.setVisible(true);
            board_frame.repaint();
        }
    }

    /**
     * Refreshs the displayed values in this window.
     */
    @Override
    public void refresh() {
        settings_window.refresh();
    }

    private final BoardFrame board_frame;

    private DefaultListModel list_model = new DefaultListModel();
    private final JList list;
    private final JTextField name_field;
    final WindowSnapshotSettings settings_window;
    private int snapshot_count = 0;
    private final ResourceBundle resources;

    private class AddListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            interactive.SnapShot new_snapshot = interactive.SnapShot.get_instance(name_field.getText(), board_frame.board_panel.board_handling);
            if (new_snapshot != null) {
                ++snapshot_count;
                list_model.addElement(new_snapshot);
                String next_default_name = resources.getString("snapshot") + " " + Integer.toString(snapshot_count + 1);
                name_field.setText(next_default_name);
            }
        }
    }

    /**
     * Selects the item, which is previous to the current selected item in the
     * list. The current selected item is then no more selected.
     */
    public void select_previous_item() {
        if (!isVisible()) {
            return;
        }
        int selected_index = list.getSelectedIndex();
        if (selected_index <= 0) {
            return;
        }
        list.setSelectedIndex(selected_index - 1);
    }

    /**
     * Selects the item, which is next to the current selected item in the list.
     * The current selected item is then no more selected.
     */
    public void select_next_item() {
        if (!isVisible()) {
            return;
        }
        int selected_index = list.getSelectedIndex();
        if (selected_index < 0 || selected_index >= list_model.getSize() - 1) {
            return;
        }

        list.setSelectedIndex(selected_index + 1);
    }

    private class SettingsListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            if (first_time) {
                java.awt.Point location = getLocation();
                settings_window.setLocation((int) location.getX() + 200, (int) location.getY());
                first_time = false;
            }
            settings_window.setVisible(true);
        }
        boolean first_time = true;
    }

    /**
     * Type for attributes of this class, which are saved to an Objectstream.
     */
    static private class SavedAttributes implements Serializable {

        public SavedAttributes(DefaultListModel p_list_model, int p_snapshot_count, java.awt.Point p_location, boolean p_is_visible) {
            list_model = p_list_model;
            snapshot_count = p_snapshot_count;
            location = p_location;
            is_visible = p_is_visible;

        }

        public final DefaultListModel list_model;
        public final int snapshot_count;
        public final java.awt.Point location;
        public final boolean is_visible;
    }
}
