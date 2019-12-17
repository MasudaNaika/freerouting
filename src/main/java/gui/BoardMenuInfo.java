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
 * BoardLibraryMenu.java
 *
 * Created on 6. Maerz 2005, 05:37
 */
package gui;

import java.util.ResourceBundle;
import javax.swing.*;

/**
 *
 * @author Alfons Wirtz
 */
public class BoardMenuInfo extends JMenu {

    /**
     * Returns a new info menu for the board frame.
     */
    public static BoardMenuInfo get_instance(BoardFrame p_board_frame) {
        final BoardMenuInfo info_menu = new BoardMenuInfo(p_board_frame);

        info_menu.setText(info_menu.resources.getString("info"));

        JMenuItem package_window = new JMenuItem();
        package_window.setText(info_menu.resources.getString("library_packages"));
        package_window.addActionListener(e -> {
            info_menu.board_frame.packages_window.setVisible(true);
        });
        info_menu.add(package_window);

        JMenuItem padstacks_window = new JMenuItem();
        padstacks_window.setText(info_menu.resources.getString("library_padstacks"));
        padstacks_window.addActionListener(e -> {
            info_menu.board_frame.padstacks_window.setVisible(true);
        });
        info_menu.add(padstacks_window);

        JMenuItem components_window = new JMenuItem();
        components_window.setText(info_menu.resources.getString("board_components"));
        components_window.addActionListener(e -> {
            info_menu.board_frame.components_window.setVisible(true);
        });
        info_menu.add(components_window);

        JMenuItem incompletes_window = new JMenuItem();
        incompletes_window.setText(info_menu.resources.getString("incompletes"));
        incompletes_window.addActionListener(e -> {
            info_menu.board_frame.incompletes_window.setVisible(true);
        });
        info_menu.add(incompletes_window);

        JMenuItem length_violations_window = new JMenuItem();
        length_violations_window.setText(info_menu.resources.getString("length_violations"));
        length_violations_window.addActionListener(e -> {
            info_menu.board_frame.length_violations_window.setVisible(true);
        });
        info_menu.add(length_violations_window);

        JMenuItem clearance_violations_window = new JMenuItem();
        clearance_violations_window.setText(info_menu.resources.getString("clearance_violations"));
        clearance_violations_window.addActionListener(e -> {
            info_menu.board_frame.clearance_violations_window.setVisible(true);
        });
        info_menu.add(clearance_violations_window);

        JMenuItem unconnnected_route_window = new JMenuItem();
        unconnnected_route_window.setText(info_menu.resources.getString("unconnected_route"));
        unconnnected_route_window.addActionListener(e -> {
            info_menu.board_frame.unconnected_route_window.setVisible(true);
        });
        info_menu.add(unconnnected_route_window);

        JMenuItem route_stubs_window = new JMenuItem();
        route_stubs_window.setText(info_menu.resources.getString("route_stubs"));
        route_stubs_window.addActionListener(e -> {
            info_menu.board_frame.route_stubs_window.setVisible(true);
        });
        info_menu.add(route_stubs_window);

        return info_menu;
    }

    /**
     * Creates a new instance of BoardLibraryMenu
     */
    private BoardMenuInfo(BoardFrame p_board_frame) {
        board_frame = p_board_frame;
        resources = ResourceBundle.getBundle("gui.resources.BoardMenuInfo", p_board_frame.get_locale());
    }

    private final BoardFrame board_frame;
    private final ResourceBundle resources;
}
