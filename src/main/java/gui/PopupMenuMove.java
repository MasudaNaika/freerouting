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
 * PopupMenuMove.java
 *
 * Created on 15. Mai 2005, 11:21
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package gui;

import java.util.ResourceBundle;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 *
 * @author Alfons Wirtz
 */
public class PopupMenuMove extends PopupMenuDisplay {

    /**
     * Creates a new instance of PopupMenuMove
     */
    public PopupMenuMove(BoardFrame p_board_frame) {
        super(p_board_frame);
        ResourceBundle resources
                = ResourceBundle.getBundle("gui.resources.PopupMenuMove", p_board_frame.get_locale());

        // Add menu for turning the items by a multiple of 90 degree
        JMenuItem rotate_menu = new JMenu();
        rotate_menu.setText(resources.getString("turn"));
        add(rotate_menu, 0);

        JMenuItem turn_90_item = new JMenuItem();
        turn_90_item.setText(resources.getString("90_degree"));
        turn_90_item.addActionListener(e -> {
            turn_45_degree(2);
        });
        rotate_menu.add(turn_90_item);

        JMenuItem turn_180_item = new JMenuItem();
        turn_180_item.setText(resources.getString("180_degree"));
        turn_180_item.addActionListener(e -> {
            turn_45_degree(4);
        });
        rotate_menu.add(turn_180_item);

        JMenuItem turn_270_item = new JMenuItem();
        turn_270_item.setText(resources.getString("-90_degree"));
        turn_270_item.addActionListener(e -> {
            turn_45_degree(6);
        });
        rotate_menu.add(turn_270_item);

        JMenuItem turn_45_item = new JMenuItem();
        turn_45_item.setText(resources.getString("45_degree"));
        turn_45_item.addActionListener(e -> {
            turn_45_degree(1);
        });
        rotate_menu.add(turn_45_item);

        JMenuItem turn_135_item = new JMenuItem();
        turn_135_item.setText(resources.getString("135_degree"));
        turn_135_item.addActionListener(e -> {
            turn_45_degree(3);
        });
        rotate_menu.add(turn_135_item);

        JMenuItem turn_225_item = new JMenuItem();
        turn_225_item.setText(resources.getString("-135_degree"));
        turn_225_item.addActionListener(e -> {
            turn_45_degree(5);
        });
        rotate_menu.add(turn_225_item);

        JMenuItem turn_315_item = new JMenuItem();
        turn_315_item.setText(resources.getString("-45_degree"));
        turn_315_item.addActionListener(e -> {
            turn_45_degree(7);
        });
        rotate_menu.add(turn_315_item);

        JMenuItem change_side_item = new JMenuItem();
        change_side_item.setText(resources.getString("change_side"));
        change_side_item.addActionListener(e -> {
            board_panel.board_handling.change_placement_side();
        });

        add(change_side_item, 1);

        JMenuItem reset_rotation_item = new JMenuItem();
        reset_rotation_item.setText(resources.getString("reset_rotation"));
        reset_rotation_item.addActionListener(e -> {
            interactive.InteractiveState interactive_state = board_panel.board_handling.get_interactive_state();
            if (interactive_state instanceof interactive.MoveItemState) {
                ((interactive.MoveItemState) interactive_state).reset_rotation();
            }
        });

        add(reset_rotation_item, 2);

        JMenuItem insert_item = new JMenuItem();
        insert_item.setText(resources.getString("insert"));
        insert_item.addActionListener(e -> {
            board_panel.board_handling.return_from_state();
        });

        add(insert_item, 3);

        JMenuItem cancel_item = new JMenuItem();
        cancel_item.setText(resources.getString("cancel"));
        cancel_item.addActionListener(e -> {
            board_panel.board_handling.cancel_state();
        });

        add(cancel_item, 4);
    }

    private void turn_45_degree(int p_factor) {
        board_panel.board_handling.turn_45_degree(p_factor);
        board_panel.move_mouse(board_panel.right_button_click_location);
    }
}
