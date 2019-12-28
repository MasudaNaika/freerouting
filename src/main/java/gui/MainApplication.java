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
 * MainApplication.java
 *
 * Created on 19. Oktober 2002, 17:58
 *
 */
package gui;

import net.freerouting.Freerouter;
import board.TestLevel;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.*;

/**
 *
 * Main application for creating frames with new or existing board designs.
 *
 * @author Alfons Wirtz
 */
public class MainApplication extends JFrame {

    /**
     * Main function of the Application
     */
    public static void main(String p_args[]) {

        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());
        StartupOptions startupOptions = StartupOptions.parse(p_args);

        if (!(OFFLINE_ALLOWED || startupOptions.webstart_option)) {
            Runtime.getRuntime().exit(1);
        }

        if (startupOptions.single_design_option) {
            ResourceBundle resources
                    = ResourceBundle.getBundle("gui.resources.MainApplication", startupOptions.current_locale);
            BoardFrame.Option board_option;
            if (startupOptions.session_file_option) {
                board_option = BoardFrame.Option.SESSION_FILE;
            } else {
                board_option = BoardFrame.Option.SINGLE_FRAME;
            }
            DesignFile design_file = DesignFile.get_instance(startupOptions.design_file_name, false);
            if (design_file == null) {
                Freerouter.logInfo(resources.getString("message_6") + " "
                        + startupOptions.design_file_name + " "
                        + resources.getString("message_7"));
                return;
            }
            String message = resources.getString("loading_design") + " " + startupOptions.design_file_name;
            WindowMessage welcome_window = WindowMessage.show(message);
            final BoardFrame new_frame
                    = create_board_frame(design_file, null, board_option, startupOptions.test_version_option, startupOptions.current_locale);
            welcome_window.dispose();
            if (new_frame == null) {
                exit(1);
                return;
            }
            new_frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent evt) {
                    exit(0);
                }
            });
        } else {
            MainApplication app = new MainApplication(startupOptions);
            app.setVisible(true);
        }
    }

    /**
     * Creates new form MainApplication It takes the directory of the board
     * designs as optional argument.
     */
    public MainApplication(StartupOptions startupOptions) {

        Freerouter.setWindowIcon(this);

        design_dir_name = startupOptions.getDesignDir();
        is_test_version = startupOptions.isTestVersion();
        is_webstart = startupOptions.getWebstartOption();
        locale = startupOptions.getCurrentLocale();
        resources = ResourceBundle.getBundle("gui.resources.MainApplication", locale);
        main_panel = new JPanel();
        getContentPane().add(main_panel);
        GridBagLayout gridbag = new GridBagLayout();
        main_panel.setLayout(gridbag);

        GridBagConstraints gridbag_constraints = new GridBagConstraints();
        gridbag_constraints.insets = new Insets(10, 10, 10, 10);
        gridbag_constraints.gridwidth = GridBagConstraints.REMAINDER;

        demonstration_button = new JButton();
        sample_board_button = new JButton();
        open_board_button = new JButton();
        restore_defaults_button = new JButton();
        message_field = new JTextField();
        message_field.setText("-de or -di not specified");
        window_net_demonstrations = new WindowNetDemonstrations(locale);
        java.awt.Point location = getLocation();
        window_net_demonstrations.setLocation((int) location.getX() + 50, (int) location.getY() + 50);
        window_net_sample_designs = new WindowNetSampleDesigns(locale);
        window_net_sample_designs.setLocation((int) location.getX() + 90, (int) location.getY() + 90);

//        setTitle(resources.getString("title") + " " + VERSION_NUMBER_STRING);
        setTitle(resources.getString("title"));
        boolean add_buttons = true;

        if (startupOptions.getWebstartOption()) {

            if (add_buttons) {
                demonstration_button.setText(resources.getString("router_demonstrations"));
                demonstration_button.setToolTipText(resources.getString("router_demonstrations_tooltip"));
                demonstration_button.addActionListener(e -> {
                    window_net_demonstrations.setVisible(true);
                });

                gridbag.setConstraints(demonstration_button, gridbag_constraints);
                main_panel.add(demonstration_button, gridbag_constraints);

                sample_board_button.setText(resources.getString("sample_designs"));
                sample_board_button.setToolTipText(resources.getString("sample_designs_tooltip"));
                sample_board_button.addActionListener(e -> {
                    window_net_sample_designs.setVisible(true);
                });

                gridbag.setConstraints(sample_board_button, gridbag_constraints);
                main_panel.add(sample_board_button, gridbag_constraints);
            }
        }

        open_board_button.setText(resources.getString("open_own_design"));
        open_board_button.setToolTipText(resources.getString("open_own_design_tooltip"));
        open_board_button.addActionListener(this::open_board_design_action);

        gridbag.setConstraints(open_board_button, gridbag_constraints);
        if (add_buttons) {
            main_panel.add(open_board_button, gridbag_constraints);
        }

        if (startupOptions.getWebstartOption() && add_buttons) {
            restore_defaults_button.setText(resources.getString("restore_defaults"));
            restore_defaults_button.setToolTipText(resources.getString("restore_defaults_tooltip"));
            restore_defaults_button.addActionListener(evt -> {
                if (is_webstart) {
                    restore_defaults_action(evt);
                }
            });

            gridbag.setConstraints(restore_defaults_button, gridbag_constraints);
            main_panel.add(restore_defaults_button, gridbag_constraints);
        }

        message_field.setPreferredSize(new Dimension(230, 20));
        message_field.setRequestFocusEnabled(false);
        gridbag.setConstraints(message_field, gridbag_constraints);
        main_panel.add(message_field, gridbag_constraints);

        main_panel.add(new JLabel(resources.getString("title") + " " + VERSION_NUMBER_STRING));

        addWindowListener(new WindowStateListener());
        pack();
    }

    /**
     * opens a board design from a binary file or a specctra dsn file.
     */
    private void open_board_design_action(ActionEvent evt) {

        if (design_dir_name == null) {
            design_dir_name = System.getProperty("user.dir");
        }

        DesignFile design_file = DesignFile.open_dialog(is_webstart, design_dir_name, this);

        if (design_file == null) {
            message_field.setText(resources.getString("message_3"));
            return;
        }

        BoardFrame.Option option;
        if (is_webstart) {
            option = BoardFrame.Option.WEBSTART;
        } else {
            option = BoardFrame.Option.FROM_START_MENU;
        }
        String message = resources.getString("loading_design") + " " + design_file.get_name();
        message_field.setText(message);
        WindowMessage welcome_window = WindowMessage.show(message);
        welcome_window.setTitle(message);

        Freerouter.setWindowIcon(welcome_window);

        BoardFrame new_frame
                = create_board_frame(design_file, message_field, option, is_test_version, locale);
        welcome_window.dispose();
        if (new_frame == null) {
            return;
        }
        message_field.setText(resources.getString("message_4") + " " + design_file.get_name() + " " + resources.getString("message_5"));
        board_frames.add(new_frame);
        new_frame.addWindowListener(new BoardFrameWindowListener(new_frame));

        new_frame.setLocationRelativeTo(null);
    }

    /**
     * Exit the Application
     */
    private static void exit(int code) {
        System.exit(code);
    }

    /**
     * deletes the setting stored by the user if the application is run by Java
     * Web Start
     */
    private void restore_defaults_action(ActionEvent evt) {
//        if (!is_webstart) {
//            return;
//        }
//        boolean file_deleted = WebStart.delete_files(BoardFrame.GUI_DEFAULTS_FILE_NAME, resources.getString("confirm_delete"));
//        if (file_deleted) {
//            message_field.setText(resources.getString("defaults_restored"));
//        } else {
//            message_field.setText(resources.getString("nothing_to_restore"));
//        }
    }

    /**
     * Creates a new board frame containing the data of the input design file.
     * Returns null, if an error occured.
     */
    static private BoardFrame create_board_frame(DesignFile p_design_file, JTextField p_message_field,
            BoardFrame.Option p_option, boolean p_is_test_version, Locale p_locale) {
        ResourceBundle resources
                = ResourceBundle.getBundle("gui.resources.MainApplication", p_locale);

        InputStream input_stream = p_design_file.get_input_stream();
        if (input_stream == null) {
            if (p_message_field != null) {
                p_message_field.setText(resources.getString("message_8") + " " + p_design_file.get_name());
            }
            return null;
        }

        TestLevel test_level;
        if (p_is_test_version) {
            test_level = DEBUG_LEVEL;
        } else {
            test_level = TestLevel.RELEASE_VERSION;
        }
        BoardFrame new_frame = new BoardFrame(p_design_file, p_option, test_level, p_locale, !p_is_test_version);
        boolean read_ok = new_frame.read(input_stream, p_design_file.is_created_from_text_file(), p_message_field);
        if (!read_ok) {
            return null;
        }
        new_frame.menubar.add_design_dependent_items();
        if (p_design_file.is_created_from_text_file()) {
            // Read the file  with the saved rules, if it is existing.

            String file_name = p_design_file.get_name();
            String[] name_parts = file_name.split("\\.");
            String confirm_import_rules_message = resources.getString("confirm_import_rules");
            DesignFile.read_rules_file(name_parts[0], p_design_file.get_parent(),
                    new_frame.board_panel.board_handling, p_option == BoardFrame.Option.WEBSTART,
                    confirm_import_rules_message);
            new_frame.refresh_windows();
        }
        return new_frame;
    }
    private final ResourceBundle resources;
    private final JButton demonstration_button;
    private final JButton sample_board_button;
    private final JButton open_board_button;
    private final JButton restore_defaults_button;
    private JTextField message_field;
    private JPanel main_panel;
    /**
     * A Frame with routing demonstrations in the net.
     */
    private final WindowNetSamples window_net_demonstrations;
    /**
     * A Frame with sample board designs in the net.
     */
    private final WindowNetSamples window_net_sample_designs;
    /**
     * The list of open board frames
     */
    private Collection<BoardFrame> board_frames = new LinkedList<>();
    private String design_dir_name = null;
    private final boolean is_test_version;
    private final boolean is_webstart;
    private final Locale locale;
    private static final TestLevel DEBUG_LEVEL = TestLevel.CRITICAL_DEBUGGING_OUTPUT;

    private class BoardFrameWindowListener extends WindowAdapter {

        public BoardFrameWindowListener(BoardFrame p_board_frame) {
            board_frame = p_board_frame;
        }

        @Override
        public void windowClosed(WindowEvent evt) {
            if (board_frame != null) {
                // remove this board_frame from the list of board frames
                board_frame.dispose();
                board_frames.remove(board_frame);
                board_frame = null;
            }
        }

        @Override
        public void windowOpened(WindowEvent e) {
            board_frame.toFront();
        }

        private BoardFrame board_frame;
    }

    private class WindowStateListener extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent evt) {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            boolean exit_program = true;
            if (!is_test_version && board_frames.size() > 0) {
                int option = JOptionPane.showConfirmDialog(null, resources.getString("confirm_cancel"),
                        null, JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.NO_OPTION) {
                    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
                    exit_program = false;
                }
            }
            if (exit_program) {
                exit(0);
            }
        }

        @Override
        public void windowIconified(WindowEvent evt) {
            window_net_sample_designs.parent_iconified();
        }

        @Override
        public void windowDeiconified(WindowEvent evt) {
            window_net_sample_designs.parent_deiconified();
        }
    }
    static final String WEB_FILE_BASE_NAME = "http://www.freerouting.net/java/";
    private static final boolean OFFLINE_ALLOWED = true;
    /**
     * Change this string when creating a new version
     */
    static final String VERSION_NUMBER_STRING = "1.2.43-m";
}
