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
 * DesignFile.java
 *
 * Created on 25. Oktober 2006, 07:48
 *
 */
package gui;

import datastructures.FileFilter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ResourceBundle;
import javax.swing.JFileChooser;

/**
 * File functionality with security restrictions used, when the application is
 * opened with Java Webstart
 *
 * @author Alfons Wirtz
 */
public class DesignFile {

    public static final String[] all_file_extensions = {"bin", "dsn"};
    public static final String[] text_file_extensions = {"dsn"};
    public static final String binary_file_extension = "bin";

    public static DesignFile get_instance(String p_design_file_name, boolean p_is_webstart) {
        if (p_design_file_name == null) {
            return null;
        }
        DesignFile result = new DesignFile(p_is_webstart, null, new File(p_design_file_name), null);
        return result;
    }

    /**
     * Shows a file chooser for opening a design file. If p_is_webstart there
     * are security restrictions because the application was opened with java
     * web start.
     */
    public static DesignFile open_dialog(boolean p_is_webstart, String p_design_dir_name) {
        DesignFile result;
        if (p_is_webstart) {
            result = webstart_open_dialog(p_design_dir_name);
        } else {
            JFileChooser file_chooser = new JFileChooser(p_design_dir_name);
            FileFilter file_filter = new FileFilter(all_file_extensions);
            file_chooser.setFileFilter(file_filter);
            file_chooser.showOpenDialog(null);
            File curr_design_file = file_chooser.getSelectedFile();
            if (curr_design_file == null) {
                return null;
            }
            result = new DesignFile(false, null, curr_design_file, file_chooser);
        }
        return result;
    }

    /**
     * Creates a new instance of DesignFile. If p_is_webstart, the application
     * was opened with Java Web Start.
     */
    private DesignFile(boolean p_is_webstart, Object p_file_contents,
            File p_design_file, JFileChooser p_file_chooser) {
        is_webstart = p_is_webstart;
        file_contents = p_file_contents;
        file_chooser = p_file_chooser;
        input_file = p_design_file;
        output_file = p_design_file;
        if (p_design_file != null) {
            String file_name = p_design_file.getName();
            String[] name_parts = file_name.split("\\.");
            if (name_parts[name_parts.length - 1].compareToIgnoreCase(binary_file_extension) != 0) {
                String binfile_name = name_parts[0] + "." + binary_file_extension;
                output_file = new File(p_design_file.getParent(), binfile_name);
            }
        }
    }

    /**
     * Gets an InputStream from the file. Returns null, if the algorithm failed.
     */
    public InputStream get_input_stream() {
        InputStream result = null;
        if (is_webstart) {
//            if (file_contents == null) {
//                return null;
//            }
//            try {
//                result = file_contents.getInputStream();
//            } catch (Exception e) {
//                // todo: better error handling...
//                result = null;
//            }
        } else {
            if (input_file == null) {
                return null;
            }
            try {
                result = new FileInputStream(input_file);
            } catch (FileNotFoundException e) {
                // todo: better error handling...
                result = null;
            }
        }
        return result;
    }

    /**
     * Gets the file name as a String. Returns null on failure.
     */
    public String get_name() {

        String result = null;
        if (file_contents != null) {
//            try {
//                result = file_contents.getName();
//            } catch (Exception e) {
//                result = null;
//            }
        } else if (input_file != null) {
            result = input_file.getName();
        } else {
            result = null;
        }
        return result;
    }

    public void save_as_dialog(java.awt.Component p_parent, BoardFrame p_board_frame) {
        final ResourceBundle resources
                = ResourceBundle.getBundle("gui.resources.BoardMenuFile", p_board_frame.get_locale());
        String[] file_name_parts = get_name().split("\\.", 2);
        String design_name = file_name_parts[0];
//        if (is_webstart) {
//            javax.jnlp.FileContents new_file_contents;
//            ByteArrayOutputStream output_stream = new ByteArrayOutputStream();
//
//            if (p_board_frame.board_panel.board_handling.export_to_dsn_file(output_stream, design_name, false)) {
//                InputStream input_stream = new ByteArrayInputStream(output_stream.toByteArray());
//                new_file_contents = WebStart.save_dialog(get_parent(), DesignFile.text_file_extensions,
//                        input_stream, get_name());
//            } else {
//                new_file_contents = null;
//            }
//
//            if (new_file_contents != null) {
//                file_contents = new_file_contents;
//                p_board_frame.screen_messages.set_status_message(resources.getString("message_4") + " " + get_name() + " " + resources.getString("message_5"));
//            } else {
//                p_board_frame.screen_messages.set_status_message(resources.getString("message_19"));
//            }
//            return;
//        }

        if (file_chooser == null) {
            String design_dir_name;
            if (output_file == null) {
                design_dir_name = null;
            } else {
                design_dir_name = output_file.getParent();
            }
            file_chooser = new JFileChooser(design_dir_name);
            FileFilter file_filter = new FileFilter(all_file_extensions);
            file_chooser.setFileFilter(file_filter);
        }

        file_chooser.showSaveDialog(p_parent);
        File new_file = file_chooser.getSelectedFile();
        if (new_file == null) {
            p_board_frame.screen_messages.set_status_message(resources.getString("message_1"));
            return;
        }
        String new_file_name = new_file.getName();
        String[] new_name_parts = new_file_name.split("\\.");
        String found_file_extension = new_name_parts[new_name_parts.length - 1];
        if (found_file_extension.compareToIgnoreCase(binary_file_extension) == 0) {
            p_board_frame.screen_messages.set_status_message(resources.getString("message_2") + " " + new_file.getName());
            output_file = new_file;
            p_board_frame.save();
        } else {
            if (found_file_extension.compareToIgnoreCase("dsn") != 0) {
                p_board_frame.screen_messages.set_status_message(resources.getString("message_3"));
                return;
            }
            OutputStream output_stream;
            try {
                output_stream = new FileOutputStream(new_file);
            } catch (FileNotFoundException e) {
                output_stream = null;
            }
            if (p_board_frame.board_panel.board_handling.export_to_dsn_file(output_stream, design_name, false)) {
                p_board_frame.screen_messages.set_status_message(resources.getString("message_4") + " " + new_file_name + " " + resources.getString("message_5"));
            } else {
                p_board_frame.screen_messages.set_status_message(resources.getString("message_6") + " " + new_file_name + " " + resources.getString("message_7"));
            }
        }
    }

    /**
     * Writes a Specctra Session File to update the design file in the host
     * system. Returns false, if the write failed
     */
    public boolean write_specctra_session_file(BoardFrame p_board_frame) {
        final ResourceBundle resources
                = ResourceBundle.getBundle("gui.resources.BoardMenuFile", p_board_frame.get_locale());
        String design_file_name = get_name();
        String[] file_name_parts = design_file_name.split("\\.", 2);
        String design_name = file_name_parts[0];
        if (is_webstart) {
            String session_file_name = secure_write_session_file(p_board_frame);
            if (session_file_name == null) {
                p_board_frame.screen_messages.set_status_message(resources.getString("message_13") + " "
                        + resources.getString("message_7"));
                return false;
            }
            p_board_frame.screen_messages.set_status_message(resources.getString("message_11") + " "
                    + session_file_name + " " + resources.getString("message_12"));
        } else {
            String output_file_name = design_name + ".ses";
            File curr_output_file = new File(get_parent(), output_file_name);
            OutputStream output_stream;
            try {
                output_stream = new FileOutputStream(curr_output_file);
            } catch (FileNotFoundException e) {
                output_stream = null;
            }

            if (p_board_frame.board_panel.board_handling.export_specctra_session_file(design_file_name, output_stream)) {
                p_board_frame.screen_messages.set_status_message(resources.getString("message_11") + " "
                        + output_file_name + " " + resources.getString("message_12"));
            } else {
                p_board_frame.screen_messages.set_status_message(resources.getString("message_13") + " "
                        + output_file_name + " " + resources.getString("message_7"));
                return false;
            }
        }
        if (WindowMessage.confirm(resources.getString("confirm"))) {
            return write_rules_file(design_name, p_board_frame.board_panel.board_handling);
        }
        return true;
    }

    /**
     * Saves the board rule to file, so that they can be reused later on.
     */
    private boolean write_rules_file(String p_design_name, interactive.BoardHandling p_board_handling) {
        String rules_file_name = p_design_name + RULES_FILE_EXTENSION;
        OutputStream output_stream = null;
        if (is_webstart) {
//            output_stream = WebStart.get_file_output_stream(rules_file_name);
        } else {
            File rules_file = new File(get_parent(), rules_file_name);
            try {
                output_stream = new FileOutputStream(rules_file);
            } catch (FileNotFoundException e) {
                System.out.println("unable to create rules file");
                return false;
            }
        }
        if (output_stream == null) {
            return false;
        }
        designformats.specctra.RulesFile.write(p_board_handling, output_stream, p_design_name);
        return true;
    }

    public static boolean read_rules_file(String p_design_name, String p_parent_name,
            interactive.BoardHandling p_board_handling, boolean p_is_web_start, String p_confirm_message) {

        boolean result = true;
        String rule_file_name = p_design_name + ".rules";
        boolean dsn_file_generated_by_host = p_board_handling.get_routing_board().communication.specctra_parser_info.dsn_file_generated_by_host;
        if (p_is_web_start) {
//            InputStream input_stream = WebStart.get_file_input_stream(rule_file_name);
//            if (input_stream != null && dsn_file_generated_by_host && WindowMessage.confirm(p_confirm_message)) {
//                result = designformats.specctra.RulesFile.read(input_stream, p_design_name, p_board_handling);
//                try {
//                    input_stream.close();
//                } catch (Exception e) {
//                    result = false;
//                }
//            } else {
//                result = false;
//            }
//            WebStart.delete_files(RULES_FILE_EXTENSION, null);
        } else {
            try {
                File rules_file = new File(p_parent_name, rule_file_name);
                InputStream input_stream = new FileInputStream(rules_file);
                if (input_stream != null && dsn_file_generated_by_host && WindowMessage.confirm(p_confirm_message)) {
                    result = designformats.specctra.RulesFile.read(input_stream, p_design_name, p_board_handling);
                } else {
                    result = false;
                }
                try {
                    if (input_stream != null) {
                        input_stream.close();
                    }
                    rules_file.delete();
                } catch (IOException e) {
                    result = false;
                }
            } catch (FileNotFoundException e) {
                result = false;
            }
        }
        return result;
    }

    public void update_eagle(BoardFrame p_board_frame) {
        final ResourceBundle resources
                = ResourceBundle.getBundle("gui.resources.BoardMenuFile", p_board_frame.get_locale());
        String design_file_name = get_name();
        ByteArrayOutputStream session_output_stream = new ByteArrayOutputStream();
        if (!p_board_frame.board_panel.board_handling.export_specctra_session_file(design_file_name, session_output_stream)) {
            return;
        }
        InputStream input_stream = new ByteArrayInputStream(session_output_stream.toByteArray());

        String[] file_name_parts = design_file_name.split("\\.", 2);
        String design_name = file_name_parts[0];
        String output_file_name = design_name + ".scr";
        if (is_webstart) {
            String script_file_name = webstart_update_eagle(p_board_frame, output_file_name, input_stream);

            if (script_file_name == null) {
                p_board_frame.screen_messages.set_status_message(resources.getString("message_16") + " "
                        + resources.getString("message_7"));
                return;
            }
            p_board_frame.screen_messages.set_status_message(resources.getString("message_14") + " " + script_file_name + " " + resources.getString("message_15"));
        } else {
            File curr_output_file = new File(get_parent(), output_file_name);
            OutputStream output_stream;
            try {
                output_stream = new FileOutputStream(curr_output_file);
            } catch (FileNotFoundException e) {
                output_stream = null;
            }

            if (p_board_frame.board_panel.board_handling.export_eagle_session_file(input_stream, output_stream)) {
                p_board_frame.screen_messages.set_status_message(resources.getString("message_14") + " " + output_file_name + " " + resources.getString("message_15"));
            } else {
                p_board_frame.screen_messages.set_status_message(resources.getString("message_16") + " " + output_file_name + " " + resources.getString("message_7"));
            }
        }
        if (WindowMessage.confirm(resources.getString("confirm"))) {
            write_rules_file(design_name, p_board_frame.board_panel.board_handling);
        }
    }

    private static DesignFile webstart_open_dialog(String p_design_dir_name) {
//        try {
//            javax.jnlp.FileOpenService file_open_service
//                    = (javax.jnlp.FileOpenService) javax.jnlp.ServiceManager.lookup("javax.jnlp.FileOpenService");
//            javax.jnlp.FileContents file_contents
//                    = file_open_service.openFileDialog(p_design_dir_name, DesignFile.text_file_extensions);
//            return new DesignFile(true, file_contents, null, null);
//        } catch (Exception e) {
//            return null;
//        }
        return null;
    }

    /**
     * Returns the name of the created session file or null, if the write
     * failed. Put into a separate function to avoid undefines in the offline
     * version.
     */
    private String secure_write_session_file(BoardFrame p_board_frame) {
//        ByteArrayOutputStream output_stream = new ByteArrayOutputStream();
//        String file_name = get_name();
//        if (file_name == null) {
//            return null;
//        }
//        String session_file_name = file_name.replace(".dsn", ".ses");
//
//        if (!p_board_frame.board_panel.board_handling.export_specctra_session_file(file_name, output_stream)) {
//            return null;
//        }
//
//        InputStream input_stream = new ByteArrayInputStream(output_stream.toByteArray());
//
//        javax.jnlp.FileContents session_file_contents
//                = WebStart.save_dialog(get_parent(), null, input_stream, session_file_name);
//
//        if (session_file_contents == null) {
//            return null;
//        }
//        String new_session_file_name;
//        try {
//            new_session_file_name = session_file_contents.getName();
//        } catch (Exception e) {
//            return null;
//        }
//        if (!new_session_file_name.equalsIgnoreCase(session_file_name)) {
//            final java.util.ResourceBundle resources
//                    = java.util.ResourceBundle.getBundle("gui.resources.BoardMenuFile", p_board_frame.get_locale());
//            String curr_message = resources.getString("message_20") + " " + session_file_name + "\n" + resources.getString("message_21");
//            WindowMessage.ok(curr_message);
//        }
//        return new_session_file_name;
        return null;
    }

    /**
     * Returns the name of the created script file or null, if the write failed.
     * Put into a separate function to avoid undefines in the offline version.
     */
    private String webstart_update_eagle(BoardFrame p_board_frame,
            String p_outfile_name, InputStream p_input_stream) {
//        ByteArrayOutputStream output_stream = new ByteArrayOutputStream();
//        if (!p_board_frame.board_panel.board_handling.export_eagle_session_file(p_input_stream, output_stream)) {
//            return null;
//        }
//        InputStream input_stream = new ByteArrayInputStream(output_stream.toByteArray());
//        javax.jnlp.FileContents script_file_contents
//                = WebStart.save_dialog(get_parent(), null, input_stream, p_outfile_name);
//
//        if (script_file_contents == null) {
//            return null;
//        }
//        String new_script_file_name;
//        try {
//            new_script_file_name = script_file_contents.getName();
//        } catch (Exception e) {
//            return null;
//        }
//
//        if (!new_script_file_name.endsWith(".scr")) {
//            final java.util.ResourceBundle resources
//                    = java.util.ResourceBundle.getBundle("gui.resources.BoardMenuFile", p_board_frame.get_locale());
//            String curr_message = resources.getString("message_22") + "\n" + resources.getString("message_21");
//            WindowMessage.ok(curr_message);
//        }
//
//        return new_script_file_name;
        return null;
    }

    /**
     * Gets the binary file for saving or null, if the design file is not
     * available because the application is run with Java Web Start.
     */
    public File get_output_file() {
        return output_file;
    }

    public File get_input_file() {
        return input_file;
    }

    public String get_parent() {
        if (input_file != null) {
            return input_file.getParent();
        }
        return null;
    }

    public File get_parent_file() {
        if (input_file != null) {
            return input_file.getParentFile();
        }
        return null;
    }

    public boolean is_created_from_text_file() {
        return is_webstart || input_file != output_file;
    }
    private final boolean is_webstart;
    /**
     * Used, if the application is run with Java Web Start.
     */
    private Object file_contents;
    /**
     * Used, if the application is run without Java Web Start.
     */
    private File output_file;
    private final File input_file;
    private JFileChooser file_chooser;
    private static final String RULES_FILE_EXTENSION = ".rules";
}
