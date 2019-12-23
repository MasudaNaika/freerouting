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
 * SessionToEagle.java
 *
 * Created on 8. Dezember 2004, 07:42
 */
package designformats.specctra;

import gui.Freerouter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import javax.swing.JFrame;

/**
 * Transformes a Specctra session file into an Eagle script file.
 *
 * @author Alfons Wirtz
 */
public class SessionToEagle extends JFrame {

    public static boolean get_instance(InputStream p_session, OutputStream p_output_stream,
            board.BasicBoard p_board) {
        if (p_output_stream == null) {
            return false;
        }

        // create a scanner for reading the session_file.
        Scanner scanner = new SpecctraFileScanner(p_session);

        // create a file_writer for the eagle script file.
        OutputStreamWriter file_writer = new OutputStreamWriter(p_output_stream);

        boolean result = true;

        double board_scale_factor = p_board.communication.coordinate_transform.board_to_dsn(1);
        SessionToEagle new_instance = new SessionToEagle(scanner, file_writer, p_board,
                p_board.communication.unit, p_board.communication.resolution, board_scale_factor);

        try {
            result = new_instance.process_session_scope();
        } catch (IOException e) {
            System.out.println("unable to process session scope");
            result = false;
        }

        // close files
        try {
            p_session.close();
            file_writer.close();
        } catch (IOException e) {
            System.out.println("unable to close files");
        }
        return result;
    }

    SessionToEagle(Scanner p_scanner, OutputStreamWriter p_out_file, board.BasicBoard p_board,
            board.Unit p_unit, double p_session_file_scale_dominator, double p_board_scale_factor) {

        Freerouter.setWindowIcon(this);

        scanner = p_scanner;
        out_file = p_out_file;
        board = p_board;
        specctra_layer_structure = new LayerStructure(p_board.layer_structure);
        unit = p_unit;
        session_file_scale_denominator = p_session_file_scale_dominator;
        board_scale_factor = p_board_scale_factor;
    }

    /**
     * Processes the outmost scope of the session file. Returns false, if an
     * error occured.
     */
    private boolean process_session_scope() throws IOException {

        // read the first line of the session file
        Object next_token = null;
        for (int i = 0; i < 3; ++i) {
            next_token = scanner.next_token();
            boolean keyword_ok = true;
            if (i == 0) {
                keyword_ok = (next_token == Keyword.OPEN_BRACKET);
            } else if (i == 1) {
                keyword_ok = (next_token == Keyword.SESSION);
                scanner.yybegin(SpecctraFileScanner.NAME); // to overread the name of the pcb for i = 2
            }
            if (!keyword_ok) {
                System.out.println("SessionToEagle.process_session_scope specctra session file format expected");
                return false;
            }
        }

        // Write the header of the eagle script file.
        out_file.write("GRID ");
        out_file.write(unit.toString());
        out_file.write("\n");
        out_file.write("SET WIRE_BEND 2\n");
        out_file.write("SET OPTIMIZING OFF\n");

        // Activate all layers in Eagle.
        for (int i = 0; i < board.layer_structure.arr.length; ++i) {
            out_file.write("LAYER " + get_eagle_layer_string(i) + ";\n");
        }

        out_file.write("LAYER 17;\n");
        out_file.write("LAYER 18;\n");
        out_file.write("LAYER 19;\n");
        out_file.write("LAYER 20;\n");
        out_file.write("LAYER 23;\n");
        out_file.write("LAYER 24;\n");

        // Generate Code to remove the complete route.
        // Write a bounding rectangle with GROUP (Min_X-1 Min_Y-1) (Max_X+1 Max_Y+1);
        geometry.planar.IntBox board_bounding_box = board.get_bounding_box();

        Float min_x = (float) board_scale_factor * (board_bounding_box.ll.x - 1);
        Float min_y = (float) board_scale_factor * (board_bounding_box.ll.y - 1);
        Float max_x = (float) board_scale_factor * (board_bounding_box.ur.x + 1);
        Float max_y = (float) board_scale_factor * (board_bounding_box.ur.y + 1);

        out_file.write("GROUP (");
        out_file.write(min_x.toString());
        out_file.write(" ");
        out_file.write(min_y.toString());
        out_file.write(") (");
        out_file.write(max_x.toString());
        out_file.write(" ");
        out_file.write(max_y.toString());
        out_file.write(");\n");
        out_file.write("RIPUP;\n");

        // read the direct subscopes of the session scope
        while (true) {
            Object prev_token = next_token;
            next_token = scanner.next_token();
            if (next_token == null) {
                // end of file
                return true;
            }
            if (next_token == Keyword.CLOSED_BRACKET) {
                // end of scope
                break;
            }

            if (prev_token == Keyword.OPEN_BRACKET) {
                if (next_token == Keyword.ROUTES) {
                    if (!process_routes_scope()) {
                        return false;
                    }
                } else if (next_token == Keyword.PLACEMENT_SCOPE) {
                    if (!process_placement_scope()) {
                        return false;
                    }
                } else {
                    // overread all scopes except the routes scope for the time being
                    ScopeKeyword.skip_scope(scanner);
                }
            }
        }
        // Wird nur einmal am Ende benoetigt!
        out_file.write("RATSNEST\n");
        return true;
    }

    private boolean process_placement_scope() throws IOException {
        // read the component scopes
        Object next_token = null;
        while (true) {
            Object prev_token = next_token;
            next_token = scanner.next_token();
            if (next_token == null) {
                // unexpected end of file
                return false;
            }
            if (next_token == Keyword.CLOSED_BRACKET) {
                // end of scope
                break;
            }

            if (prev_token == Keyword.OPEN_BRACKET) {

                if (next_token == Keyword.COMPONENT_SCOPE) {
                    if (!process_component_placement()) {
                        return false;
                    }
                } else {
                    // skip unknown scope
                    ScopeKeyword.skip_scope(scanner);
                }

            }
        }
        process_swapped_pins();
        return true;
    }

    private boolean process_component_placement() throws IOException {
        ComponentPlacement component_placement = Component.read_scope(scanner);
        if (component_placement == null) {
            return false;
        }
        for (ComponentPlacement.ComponentLocation curr_location : component_placement.locations) {
            out_file.write("ROTATE =");
            Integer rotation = (int) Math.round(curr_location.rotation);
            String rotation_string;
            if (curr_location.is_front) {
                rotation_string = "R" + rotation.toString();
            } else {
                rotation_string = "MR" + rotation.toString();
            }
            out_file.write(rotation_string);
            out_file.write(" '");
            out_file.write(curr_location.name);
            out_file.write("';\n");
            out_file.write("move '");
            out_file.write(curr_location.name);
            out_file.write("' (");
            Double x_coor = curr_location.coor[0] / session_file_scale_denominator;
            out_file.write(x_coor.toString());
            out_file.write(" ");
            Double y_coor = curr_location.coor[1] / session_file_scale_denominator;
            out_file.write(y_coor.toString());
            out_file.write(");\n");
        }
        return true;
    }

    private boolean process_routes_scope() throws IOException {
        // read the direct subscopes of the routes scope
        boolean result = true;
        Object next_token = null;
        while (true) {
            Object prev_token = next_token;
            next_token = scanner.next_token();
            if (next_token == null) {
                // unexpected end of file
                return false;
            }
            if (next_token == Keyword.CLOSED_BRACKET) {
                // end of scope
                break;
            }

            if (prev_token == Keyword.OPEN_BRACKET) {

                if (next_token == Keyword.NETWORK_OUT) {
                    result = process_network_scope();
                } else {
                    // skip unknown scope
                    ScopeKeyword.skip_scope(scanner);
                }

            }
        }
        return result;
    }

    private boolean process_network_scope() throws IOException {
        boolean result = true;
        Object next_token = null;
        // read the net scopes
        while (true) {
            Object prev_token = next_token;
            next_token = scanner.next_token();
            if (next_token == null) {
                // unexpected end of file
                return false;
            }
            if (next_token == Keyword.CLOSED_BRACKET) {
                // end of scope
                break;
            }

            if (prev_token == Keyword.OPEN_BRACKET) {

                if (next_token == Keyword.NET) {
                    result = process_net_scope();
                } else {
                    // skip unknown scope
                    ScopeKeyword.skip_scope(scanner);
                }

            }
        }
        return result;
    }

    private boolean process_net_scope() throws IOException {
        // read the net name
        Object next_token = scanner.next_token();
        if (!(next_token instanceof String)) {
            System.out.println("SessionToEagle.processnet_scope: String expected");
            return false;
        }
        String net_name = (String) next_token;

        // Hier alle nicht gefixten Traces und Vias des Netz mit Namen net_name
        // in der Eagle Datenhaltung loeschen.
        // read the wires and vias of this net
        while (true) {
            Object prev_token = next_token;
            next_token = scanner.next_token();
            if (next_token == null) {
                // end of file
                return true;
            }
            if (next_token == Keyword.CLOSED_BRACKET) {
                // end of scope
                break;
            }

            if (prev_token == Keyword.OPEN_BRACKET) {
                if (next_token == Keyword.WIRE) {
                    if (!process_wire_scope(net_name)) {
                        return false;
                    }
                } else if (next_token == Keyword.VIA) {
                    if (!process_via_scope(net_name)) {
                        return false;
                    }
                } else {
                    ScopeKeyword.skip_scope(scanner);
                }
            }
        }
        return true;
    }

    private boolean process_wire_scope(String p_net_name) throws IOException {
        PolygonPath wire_path = null;
        Object next_token = null;
        while (true) {
            Object prev_token = next_token;
            next_token = scanner.next_token();
            if (next_token == null) {
                System.out.println("SessionToEagle.process_wire_scope: unexpected end of file");
                return false;
            }
            if (next_token == Keyword.CLOSED_BRACKET) {
                // end of scope
                break;
            }
            if (prev_token == Keyword.OPEN_BRACKET) {
                if (next_token == Keyword.POLYGON_PATH) {
                    wire_path = Shape.read_polygon_path_scope(scanner, specctra_layer_structure);
                } else {
                    ScopeKeyword.skip_scope(scanner);
                }
            }
        }
        if (wire_path == null) {
            // conduction areas are skipped
            return true;
        }

        out_file.write("CHANGE LAYER ");

        out_file.write(wire_path.layer.name);
        out_file.write(";\n");

        //WIRE ['signal_name'] [width] [ROUND | FLAT]  [curve | @radius]
        out_file.write("WIRE '");

        out_file.write(p_net_name);
        out_file.write("' ");
        Double wire_width = wire_path.width / session_file_scale_denominator;
        out_file.write(wire_width.toString());
        out_file.write(" (");
        for (int i = 0; i < wire_path.coordinate_arr.length; ++i) {
            Double wire_coor = wire_path.coordinate_arr[i] / session_file_scale_denominator;
            out_file.write(wire_coor.toString());
            if (i % 2 == 0) {
                out_file.write(" ");
            } else {
                if (i == wire_path.coordinate_arr.length - 1) {
                    out_file.write(")");
                } else {
                    out_file.write(") (");
                }
            }
        }
        out_file.write(";\n");

        return true;
    }

    private boolean process_via_scope(String p_net_name) throws IOException {
        // read the padstack name
        Object next_token = scanner.next_token();
        if (!(next_token instanceof String)) {
            System.out.println("SessionToEagle.process_via_scope: padstack name expected");
            return false;
        }
        String padstack_name = (String) next_token;
        // read the location
        double[] location = new double[2];
        for (int i = 0; i < 2; ++i) {
            next_token = scanner.next_token();
            if (next_token instanceof Double) {
                location[i] = ((Double) next_token);
            } else if (next_token instanceof Integer) {
                location[i] = ((Integer) next_token);
            } else {
                System.out.println("SessionToEagle.process_via_scope: number expected");
                return false;
            }
        }
        next_token = scanner.next_token();
        while (next_token == Keyword.OPEN_BRACKET) {
            // skip unknown scopes
            ScopeKeyword.skip_scope(scanner);
            next_token = scanner.next_token();
        }
        if (next_token != Keyword.CLOSED_BRACKET) {
            System.out.println("SessionToEagle.process_via_scope: closing bracket expected");
            return false;
        }

        if (padstack_name == null) {
            System.out.println("SessionToEagle.process_via_scope: padstack_name missing");
            return false;
        }

        library.Padstack via_padstack = board.library.padstacks.get(padstack_name);

        if (via_padstack == null) {
            System.out.println("SessionToEagle.process_via_scope: via padstack not found");
            return false;
        }

        geometry.planar.ConvexShape via_shape = via_padstack.get_shape(via_padstack.from_layer());

        Double via_diameter = via_shape.max_width() * board_scale_factor;

        // The Padstack name is of the form Name$drill_diameter$from_layer-to_layer
        String[] name_parts = via_padstack.name.split("\\$", 3);

        // example CHANGE DRILL 0.2
        out_file.write("CHANGE DRILL ");
        if (name_parts.length > 1) {
            out_file.write(name_parts[1]);
        } else {
            // create a default drill, because it is needed in Eagle
            out_file.write("0.1");
        }
        out_file.write(";\n");

        //VIA ['signal_name'] [diameter] [shape] [layers] [flags]
        // Via Net2 0.6 round 1-4 (20.0, 222.0);
        out_file.write("VIA '");

        out_file.write(p_net_name);
        out_file.write("' ");

        //Durchmesser aus Padstack
        out_file.write(via_diameter.toString());

        //Shape lesen und einsetzen Square / Round / Octagon
        if (via_shape instanceof geometry.planar.Circle) {
            out_file.write(" round ");
        } else if (via_shape instanceof geometry.planar.IntOctagon) {
            out_file.write(" octagon ");
        } else {
            out_file.write(" square ");
        }
        out_file.write(get_eagle_layer_string(via_padstack.from_layer()));
        out_file.write("-");
        out_file.write(get_eagle_layer_string(via_padstack.to_layer()));
        out_file.write(" (");
        Double x_coor = location[0] / session_file_scale_denominator;
        out_file.write(x_coor.toString());
        out_file.write(" ");
        Double y_coor = location[1] / session_file_scale_denominator;
        out_file.write(y_coor.toString());
        out_file.write(");\n");

        return true;
    }

    private String get_eagle_layer_string(int p_layer_no) {
        if (p_layer_no < 0 || p_layer_no >= specctra_layer_structure.arr.length) {
            return "0";
        }
        String[] name_pieces = specctra_layer_structure.arr[p_layer_no].name.split("#", 2);
        return name_pieces[0];
    }

    private boolean process_swapped_pins() throws IOException {
        for (int i = 1; i <= board.components.count(); ++i) {
            if (!process_swapped_pins(i)) {
                return false;
            }
        }
        return true;
    }

    private boolean process_swapped_pins(int p_component_no) throws IOException {
        Collection<board.Pin> component_pins = board.get_component_pins(p_component_no);
        boolean component_has_swapped_pins = false;
        for (board.Pin curr_pin : component_pins) {
            if (curr_pin.get_changed_to() != curr_pin) {
                component_has_swapped_pins = true;
                break;
            }
        }
        if (!component_has_swapped_pins) {
            return true;
        }
        PinInfo[] pin_info_arr = new PinInfo[component_pins.size()];
        int i = 0;
        for (board.Pin curr_pin : component_pins) {
            pin_info_arr[i] = new PinInfo(curr_pin);
            ++i;
        }
        for (i = 0; i < pin_info_arr.length; ++i) {
            PinInfo curr_pin_info = pin_info_arr[i];
            if (curr_pin_info.curr_changed_to != curr_pin_info.pin.get_changed_to()) {
                PinInfo other_pin_info = null;
                for (int j = i + 1; j < pin_info_arr.length; ++j) {
                    if (pin_info_arr[j].pin.get_changed_to() == curr_pin_info.pin) {
                        other_pin_info = pin_info_arr[j];
                    }
                }
                if (other_pin_info == null) {
                    System.out.println("SessuinToEagle.process_swapped_pins: other_pin_info not found");
                    return false;
                }
                write_pin_swap(curr_pin_info.pin, other_pin_info.pin);
                curr_pin_info.curr_changed_to = other_pin_info.pin;
                other_pin_info.curr_changed_to = curr_pin_info.pin;
            }
        }
        return true;
    }

    private void write_pin_swap(board.Pin p_pin_1, board.Pin p_pin_2) throws IOException {
        int layer_no = Math.max(p_pin_1.first_layer(), p_pin_2.first_layer());
        String layer_name = board.layer_structure.arr[layer_no].name;

        out_file.write("CHANGE LAYER ");
        out_file.write(layer_name);
        out_file.write(";\n");

        double[] location_1
                = board.communication.coordinate_transform.board_to_dsn(p_pin_1.get_center().to_float());
        double[] location_2
                = board.communication.coordinate_transform.board_to_dsn(p_pin_2.get_center().to_float());

        out_file.write("PINSWAP ");
        out_file.write(" (");
        Double curr_coor = location_1[0];
        out_file.write(curr_coor.toString());
        out_file.write(" ");
        curr_coor = location_1[1];
        out_file.write(curr_coor.toString());
        out_file.write(") (");
        curr_coor = location_2[0];
        out_file.write(curr_coor.toString());
        out_file.write(" ");
        curr_coor = location_2[1];
        out_file.write(curr_coor.toString());
        out_file.write(");\n");
    }

    /**
     * The function for scanning the session file
     */
    private final Scanner scanner;

    /**
     * The generated Eagle script file.
     */
    private final OutputStreamWriter out_file;

    /**
     * Some information is read from the board, because it is not contained in
     * the speccctra session file.
     */
    private final board.BasicBoard board;

    /**
     * The layer structure in specctra format
     */
    private final LayerStructure specctra_layer_structure;

    private final board.Unit unit;

    /**
     * The scale factor for transforming coordinates from the session file to
     * Eagle
     */
    private final double session_file_scale_denominator;

    /**
     * The scale factor for transforming coordinates from the board to Eagle
     */
    private final double board_scale_factor;

    private static class PinInfo {

        PinInfo(board.Pin p_pin) {
            pin = p_pin;
            curr_changed_to = p_pin;
        }
        final board.Pin pin;
        board.Pin curr_changed_to;
    }
}
