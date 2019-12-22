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
 * ObjectListWindow.java
 *
 * Created on 7. Maerz 2005, 09:26
 */
package gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.*;

/**
 * Abstract class for windows displaying a list of objects
 *
 * @author Alfons Wirtz
 */
public abstract class WindowObjectList extends BoardSavableSubWindow {

    /**
     * Creates a new instance of ObjectListWindow
     */
    public WindowObjectList(BoardFrame p_board_frame) {
        board_frame = p_board_frame;
        resources = ResourceBundle.getBundle("gui.resources.WindowObjectList", p_board_frame.get_locale());

        // create main panel
        main_panel = new JPanel();
        main_panel.setLayout(new BorderLayout());
        add(main_panel);

        // create a panel for adding buttons
        south_panel = new JPanel();
        south_panel.setLayout(new BorderLayout());
        main_panel.add(south_panel, BorderLayout.SOUTH);

        JPanel button_panel = new JPanel();
        button_panel.setLayout(new BorderLayout());
        south_panel.add(button_panel, BorderLayout.CENTER);

        JPanel north_button_panel = new JPanel();
        button_panel.add(north_button_panel, BorderLayout.NORTH);

        JButton show_button = new JButton(resources.getString("info"));
        show_button.setToolTipText(resources.getString("info_tooltip"));
        ShowListener show_listener = new ShowListener();
        show_button.addActionListener(show_listener);
        north_button_panel.add(show_button);

        JButton instance_button = new JButton(resources.getString("select"));
        instance_button.setToolTipText(resources.getString("select_tooltip"));
        SelectListener instance_listener = new SelectListener();
        instance_button.addActionListener(instance_listener);
        north_button_panel.add(instance_button);

        JPanel south_button_panel = new JPanel();
        button_panel.add(south_button_panel, BorderLayout.SOUTH);

        JButton invert_button = new JButton(resources.getString("invert"));
        invert_button.setToolTipText(resources.getString("invert_tooltip"));
        invert_button.addActionListener(new InvertListener());
        south_button_panel.add(invert_button);

        JButton recalculate_button = new JButton(resources.getString("recalculate"));
        recalculate_button.setToolTipText(resources.getString("recalculate_tooltip"));
        RecalculateListener recalculate_listener = new RecalculateListener();
        recalculate_button.addActionListener(recalculate_listener);
        south_button_panel.add(recalculate_button);

        list_empty_message = new JLabel(resources.getString("list_empty"));
        list_empty_message.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        /**
         * Dispose this window and all subwindows when closing the window.
         */
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                dispose();
            }
        });
    }

    @Override
    public void setVisible(boolean p_value) {
        if (p_value == true) {
            recalculate();
        }
        super.setVisible(p_value);
    }

    protected void recalculate() {
        if (list_scroll_pane != null) {
            main_panel.remove(list_scroll_pane);
        }
        main_panel.remove(list_empty_message);
        // Create display list
        list_model = new DefaultListModel();
        list = new JList(list_model);
        list.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        fill_list();
        if (list.getVisibleRowCount() > 0) {
            list_scroll_pane = new JScrollPane(list);
            main_panel.add(list_scroll_pane, BorderLayout.CENTER);
        } else {
            main_panel.add(list_empty_message, BorderLayout.CENTER);
        }
        pack();

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() > 1) {
                    select_instances();
                }
            }
        });
    }

    @Override
    public void dispose() {
        for (WindowObjectInfo curr_subwindow : subwindows) {
            if (curr_subwindow != null) {
                curr_subwindow.dispose();
            }
        }
        super.dispose();
    }

    protected void add_to_list(Object p_object) {
        list_model.addElement(p_object);
    }

    /**
     * Fills the list with the objects to display.
     */
    abstract protected void fill_list();

    abstract protected void select_instances();

    protected final BoardFrame board_frame;

    private final JPanel main_panel;

    private JScrollPane list_scroll_pane = null;
    protected JLabel list_empty_message;

    private DefaultListModel list_model = null;
    protected JList list;

    protected final JPanel south_panel;

    /**
     * The subwindows with information about selected object
     */
    protected final Collection<WindowObjectInfo> subwindows = new LinkedList<>();

    private final ResourceBundle resources;

    protected static final int DEFAULT_TABLE_SIZE = 20;

    /**
     * Listens to the button for showing the selected padstacks
     */
    private class ShowListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            List selected_objects = list.getSelectedValuesList();
            if (selected_objects.isEmpty()) {
                return;
            }
            Collection<WindowObjectInfo.Printable> object_list = new LinkedList<>();
            for (Object obj : selected_objects) {
                object_list.add((WindowObjectInfo.Printable) obj);
            }
            board.CoordinateTransform coordinate_transform = board_frame.board_panel.board_handling.coordinate_transform;
            WindowObjectInfo new_window
                    = WindowObjectInfo.display(resources.getString("window_title"), object_list, board_frame, coordinate_transform);
            java.awt.Point loc = getLocation();
            java.awt.Point new_window_location
                    = new java.awt.Point((int) (loc.getX() + WINDOW_OFFSET), (int) (loc.getY() + WINDOW_OFFSET));
            new_window.setLocation(new_window_location);
            subwindows.add(new_window);
        }

        private static final int WINDOW_OFFSET = 30;
    }

    /**
     * Listens to the button for showing the selected incompletes
     */
    private class SelectListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            select_instances();
        }
    }

    /**
     * Listens to the button for inverting the selection
     */
    private class InvertListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            if (list_model == null) {
                return;
            }
            int[] new_selected_indices = new int[list_model.getSize() - list.getSelectedIndices().length];
            int curr_index = 0;
            for (int i = 0; i < list_model.getSize(); ++i) {
                if (!list.isSelectedIndex(i)) {
                    new_selected_indices[curr_index] = i;
                    ++curr_index;
                }
            }
            list.setSelectedIndices(new_selected_indices);
        }
    }

    /**
     * Saves also the filter string to disk.
     */
    @Override
    public void save(ObjectOutputStream p_object_stream) {
        int[] selected_indices;
        if (list != null) {
            selected_indices = list.getSelectedIndices();
        } else {
            selected_indices = new int[0];
        }
        try {
            p_object_stream.writeObject(selected_indices);
        } catch (IOException e) {
            System.out.println("WindowObjectList.save: save failed");
        }
        super.save(p_object_stream);
    }

    @Override
    public boolean read(ObjectInputStream p_object_stream) {
        int[] saved_selected_indices = null;
        try {
            saved_selected_indices = (int[]) p_object_stream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("WindowObjectListWithFilter.read: read failed");
            return false;
        }
        boolean result = super.read(p_object_stream);
        if (list != null && saved_selected_indices.length > 0) {
            list.setSelectedIndices(saved_selected_indices);
        }
        return result;
    }

    /**
     * Listens to the button for recalculating the content of the window
     */
    private class RecalculateListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent p_evt) {
            recalculate();
        }
    }
}
