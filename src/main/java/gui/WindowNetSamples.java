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
 * WindowNetSampleDesigns.java
 *
 * Created on 11. November 2006, 07:49
 *
 */
package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.*;
import javax.swing.border.Border;

/**
 * Window with a list for selecting samples in the net.
 *
 * @author Alfons Wirtz
 */
public abstract class WindowNetSamples extends BoardSubWindow {

    /**
     * Creates a new instance of WindowNetSampleDesigns
     */
    public WindowNetSamples(Locale p_locale, String p_title, String p_button_name, int p_row_count) {
        locale = p_locale;
        resources = ResourceBundle.getBundle("gui.resources.WindowNetSamples", p_locale);
        setTitle(resources.getString(p_title));

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // create main panel
        final JPanel main_panel = new JPanel();
        add(main_panel);
        main_panel.setLayout(new BorderLayout());
        Border panel_border = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        main_panel.setBorder(panel_border);

        // create open button
        JButton open_button = new JButton(resources.getString(p_button_name));
        open_button.addActionListener(ae -> button_pushed());
        main_panel.add(open_button, BorderLayout.SOUTH);

        // create list with the sample designs
        list = new JList(list_model);
        fill_list();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        list.setVisibleRowCount(p_row_count);
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() > 1) {
                    button_pushed();
                }
            }
        });

        JScrollPane list_scroll_pane = new JScrollPane(list);
        list_scroll_pane.setPreferredSize(new Dimension(200, 20 * p_row_count));
        main_panel.add(list_scroll_pane, BorderLayout.CENTER);
        pack();
    }

    /**
     * Fill the list with the examples.
     */
    protected abstract void fill_list();

    /**
     * Action to be perfomed. when the button is pushed after selecting an item
     * in the list.
     */
    protected abstract void button_pushed();

    /**
     * Opens a zipped archive from an URL in the net. Returns a zipped input
     * stream, who is positioned at the start of p_file_name, or null, if an
     * error occured,
     */
    protected static ZipInputStream open_zipped_file(String p_archive_name, String p_file_name) {
        String archive_path_name = MainApplication.WEB_FILE_BASE_NAME + p_archive_name + ".zip";
        URL archive_url = null;
        try {
            archive_url = new URL(archive_path_name);
        } catch (java.net.MalformedURLException e) {
            return null;
        }
        InputStream input_stream = null;
        ZipInputStream zip_input_stream = null;
        URLConnection net_connection = null;
        try {
            net_connection = archive_url.openConnection();
        } catch (IOException e) {
            return null;
        }
        try {
            input_stream = net_connection.getInputStream();
        } catch (IOException | java.security.AccessControlException e) {
            return null;
        }
        try {
            zip_input_stream = new ZipInputStream(input_stream);
        } catch (Exception e) {
            Freerouter.logError(e);
            WindowMessage.show("unable to get zip input stream");
            return null;
        }
        String compare_name = p_archive_name + "/" + p_file_name;
        ZipEntry curr_entry = null;
        while (true) {
            try {
                curr_entry = zip_input_stream.getNextEntry();
            } catch (IOException E) {
                return null;
            }
            if (curr_entry == null) {
                return null;
            }
            String design_name = curr_entry.getName();
            if (design_name.equals(compare_name)) {
                break;
            }
        }
        return zip_input_stream;
    }

    /**
     * Opens a sample design on the website.
     */
    protected static BoardFrame open_design(String p_archive_name, String p_design_name, Locale p_locale) {
        ZipInputStream zip_input_stream = open_zipped_file(p_archive_name, p_design_name);
        if (zip_input_stream == null) {
            return null;
        }
        DesignFile design_file = DesignFile.get_instance("sharc_routed.dsn", true);
        BoardFrame new_frame
                = new BoardFrame(design_file, BoardFrame.Option.WEBSTART, board.TestLevel.RELEASE_VERSION,
                        p_locale, false);
        boolean read_ok = new_frame.read(zip_input_stream, true, null);
        if (!read_ok) {
            return null;
        }
        new_frame.setVisible(true);
        return new_frame;
    }

    protected final ResourceBundle resources;
    protected final Locale locale;

    protected DefaultListModel list_model = new DefaultListModel();
    protected final JList list;

}
