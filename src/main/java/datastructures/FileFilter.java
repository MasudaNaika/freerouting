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
 * FileFilter.java
 *
 * Created on 31. Mai 2004, 09:23
 */
package datastructures;

import java.io.File;

/**
 * Used in the file chooser to filter all files which do not have an extension
 * from the input array.
 *
 * @author alfons
 */
public class FileFilter extends javax.swing.filechooser.FileFilter {

    /**
     * Creates a new FileFilter for the input extension
     */
    public FileFilter(String[] p_extensions) {
        extensions = p_extensions;
    }

    @Override
    public String getDescription() {
        String message = "files with the extensions";
        StringBuilder sb = new StringBuilder();
        sb.append(message);
        for (int i = 0; i < extensions.length; ++i) {
            message += " ." + extensions[i];
            if (i == extensions.length - 2) {
                sb.append(" or ");
            } else if (i < extensions.length - 2) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    @Override
    public boolean accept(File p_file) {
        if (p_file.isDirectory()) {
            return true;
        }
        String file_name = p_file.getName();
        String[] name_parts = file_name.split("\\.");
        if (name_parts.length < 2) {
            return false;
        }
        String found_extension = name_parts[name_parts.length - 1];
        for (String extension : extensions) {
            if (found_extension.compareToIgnoreCase(extension) == 0) {
                return true;
            }
        }
        return false;
    }

    private final String[] extensions;
}
