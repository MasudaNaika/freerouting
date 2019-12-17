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
 * BoardMenuOther.java
 *
 * Created on 19. Oktober 2005, 08:34
 *
 */
package gui;

import java.util.ResourceBundle;
import javax.swing.*;

/**
 *
 * @author Alfons Wirtz
 */
public class BoardMenuOther extends JMenu {

    /**
     * Returns a new other menu for the board frame.
     */
    public static BoardMenuOther get_instance(BoardFrame p_board_frame) {
        final BoardMenuOther other_menu = new BoardMenuOther(p_board_frame);

        other_menu.setText(other_menu.resources.getString("other"));

        JMenuItem snapshots = new JMenuItem();
        snapshots.setText(other_menu.resources.getString("snapshots"));
        snapshots.setToolTipText(other_menu.resources.getString("snapshots_tooltip"));
        snapshots.addActionListener(e -> {
            other_menu.board_frame.snapshot_window.setVisible(true);
        });

        other_menu.add(snapshots);

        return other_menu;
    }

    /**
     * Creates a new instance of BoardMenuOther
     */
    private BoardMenuOther(BoardFrame p_board_frame) {
        board_frame = p_board_frame;
        resources = ResourceBundle.getBundle("gui.resources.BoardMenuOther", p_board_frame.get_locale());
    }

    private final BoardFrame board_frame;
    private final ResourceBundle resources;

}
