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
 */
package interactive;

import geometry.planar.FloatPoint;
import gui.Freerouter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

/**
 * Logfile to track the actions in the interactive board handling for automatic
 * replay.
 *
 * @author Alfons Wirtz
 *
 */
public class Logfile {

    /**
     * opens the logfile for reading
     */
    public boolean start_read(InputStream p_input_stream) {
        scanner = new LogfileScanner(p_input_stream);
        return (scanner != null);
    }

    /**
     * Reads the next corner from the logfile. Return null, if no valid corner
     * is found.
     */
    public FloatPoint read_corner() {

        double x = 0;
        double y = 0;
        for (int i = 0; i < 2; ++i) {
            Object curr_ob = next_token();
            if (!(curr_ob instanceof Double)) {
                pending_token = curr_ob;
                return null;
            }
            double f = ((Double) curr_ob);
            if (i == 0) {
                x = f;
            } else {
                y = f;
            }
        }
        return new FloatPoint(x, y);
    }

    /**
     * closes the logfile after writing
     */
    public void close_output() {
        if (file_writer != null) {
            try {
                file_writer.close();
            } catch (IOException e) {
                Freerouter.logError("unable to close logfile");
            }
        }
        write_enabled = false;
    }

    /**
     * opens a logfile for writing
     */
    public boolean start_write(File p_file) {
        try {
            file_writer = new FileWriter(p_file);
        } catch (IOException e) {
            Freerouter.logError("unable to create logfile");
            return false;
        }
        write_enabled = true;
        return true;
    }

    /**
     * Marks the beginning of a new item in the olutput stream
     */
    public void start_scope(LogfileScope p_logfile_scope) {
        if (write_enabled) {
            try {
                file_writer.write(p_logfile_scope.name);
                file_writer.write("\n");
            } catch (IOException e2) {
                Freerouter.logError("Logfile.start_scope: write failed");
            }
        }
    }

    /**
     * Marks the beginning of a new scope in the olutput stream Writes also an
     * integer value.
     */
    public void start_scope(LogfileScope p_logfile_scope, int p_int_value) {
        start_scope(p_logfile_scope);
        add_int(p_int_value);
    }

    /**
     * Marks the beginning of a new scope in the olutput stream Writes also 1,
     * if p_boolean_value is true, or 0, if p_boolean_value is false;
     */
    public void start_scope(LogfileScope p_logfile_scope, boolean p_boolean_value) {
        start_scope(p_logfile_scope);
        int int_value;
        if (p_boolean_value) {
            int_value = 1;
        } else {
            int_value = 0;
        }
        add_int(int_value);
    }

    /**
     * Marks the beginning of a new item in the olutput stream Writes also the
     * start corner.
     */
    public void start_scope(LogfileScope p_logfile_scope, FloatPoint p_start_corner) {
        start_scope(p_logfile_scope);
        add_corner(p_start_corner);
    }

    /**
     * Reads the next scope iidentifier from the logfile. Returns null if no
     * more item scope was found.
     */
    public LogfileScope start_read_scope() {
        Object curr_ob = next_token();
        if (curr_ob == null) {
            return null;
        }
        if (!(curr_ob instanceof String)) {
            Freerouter.logInfo("Logfile.start_read_scope: String expected");
            pending_token = curr_ob;
            return null;
        }
        LogfileScope result = LogfileScope.get_scope((String) curr_ob);
        return result;
    }

    /**
     * adds an int to the logfile
     */
    public void add_int(int p_int) {

        if (write_enabled) {
            try {
                file_writer.write(Integer.toString(p_int));
                file_writer.write("\n");
            } catch (IOException e2) {
                Freerouter.logError("unable to write integer to logfile");
            }
        }
    }

    /**
     * Reads the next int from the logfile. Returns -1, if no valid integer was
     * found.
     */
    public int read_int() {
        Object curr_ob = next_token();
        if (!(curr_ob instanceof Integer)) {
            Freerouter.logInfo("Logfile.read_int: Integer expected");
            pending_token = curr_ob;
            return -1;
        }
        return (((Integer) curr_ob));
    }

    /**
     * adds a FloatPoint to the logfile
     */
    public void add_corner(FloatPoint p_corner) {
        if (write_enabled) {
            if (p_corner == null) {
                Freerouter.logInfo("logfile.add_corner: p_corner is null");
                return;
            }
            try {
                file_writer.write(Double.toString(p_corner.x));
                file_writer.write(" ");
                file_writer.write(Double.toString(p_corner.y));
                file_writer.write("\n");
            } catch (IOException e2) {
                Freerouter.logError("unable to write to logfile while adding corner");
            }
        }
    }

    private Object next_token() {
        if (pending_token != null) {
            Object result = pending_token;
            pending_token = null;
            return result;
        }
        try {
            Object result = scanner.next_token();
            return result;
        } catch (IOException e) {
            Freerouter.logError("Logfile.next_token: IO error scanning file");
            return null;
        }
    }

    private LogfileScanner scanner = null;
    private FileWriter file_writer = null;
    private boolean write_enabled = false;
    private Object pending_token = null;
}
