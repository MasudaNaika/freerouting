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
 * LocationAndVisibilitySavableWindow.java
 *
 * Created on 20. Dezember 2004, 09:03
 */
package gui;

import net.freerouting.Freerouter;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Subwindow of the board frame, whose location and visibility can be saved and
 * read from disc.
 *
 * @author Alfons Wirtz
 */
public abstract class BoardSavableSubWindow extends BoardSubWindow {

    /**
     * Reads the data of this frame from disc. Returns false, if the reading
     * failed.
     */
    public boolean read(ObjectInputStream p_object_stream) {
        try {
            SavedAttributes saved_attributes = (SavedAttributes) p_object_stream.readObject();
            setBounds(saved_attributes.bounds);
            setVisible(saved_attributes.is_visible);
            return true;
        } catch (IOException | ClassNotFoundException e) {
            Freerouter.logError("SelectParameterWindow.read: read failed");
            return false;
        }
    }

    /**
     * Saves this frame to disk.
     */
    public void save(ObjectOutputStream p_object_stream) {
        SavedAttributes saved_attributes = new SavedAttributes(getBounds(), isVisible());

        try {
            p_object_stream.writeObject(saved_attributes);
        } catch (IOException e) {
            Freerouter.logError("BoardSubWindow.save: save failed");
        }
    }

    /**
     * Refreshs the displayed values in this window. To be overwritten in
     * derived classes.
     */
    public void refresh() {

    }

    /**
     * Type for attributes of this class, which are saved to an Objectstream.
     */
    static private class SavedAttributes implements Serializable {

        public SavedAttributes(Rectangle p_bounds, boolean p_is_visible) {
            bounds = p_bounds;
            is_visible = p_is_visible;
        }

        public final Rectangle bounds;
        public final boolean is_visible;
    }
}
