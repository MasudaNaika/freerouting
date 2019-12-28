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
 * BoardMenuHelp.java
 *
 * Created on 19. Oktober 2005, 08:15
 *
 */
package gui;

import net.freerouting.Freerouter;
import java.net.URL;
import java.util.Locale;
import javax.help.CSH;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.swing.*;

/**
 *
 * @author Alfons Wirtz
 */
public class BoardMenuHelp extends BoardMenuHelpReduced {

    /**
     * Creates a new instance of BoardMenuHelp Separated from
     * BoardMenuHelpReduced to avoid ClassNotFound exception when the library
     * jh.jar is not found, which is only used in this extended class.
     */
    public BoardMenuHelp(BoardFrame p_board_frame) {
        super(p_board_frame);
        initialize_help(p_board_frame.get_locale());
        JMenuItem direct_help_window = new JMenuItem();
        direct_help_window.setText(resources.getString("direct_help"));
        if (direct_help != null) {
            direct_help_window.addActionListener(direct_help);
        }
        add(direct_help_window, 0);
        JMenuItem contents_window = new JMenuItem();
        contents_window.setText(resources.getString("contents"));
        if (contents_help != null) {
            contents_window.addActionListener(contents_help);
        }
        add(contents_window, 0);
    }

    private void initialize_help(Locale p_locale) {
        // try to find the helpset and create a HelpBroker object
        if (BoardFrame.help_broker == null) {
            String language = p_locale.getLanguage();
            String helpset_name;
            if (language.equalsIgnoreCase("de")) {
                helpset_name = "helpset/de/Help.hs";
            } else {
                helpset_name = "helpset/en/Help.hs";
            }
            try {
                URL hsURL = HelpSet.findHelpSet(getClass().getClassLoader(), helpset_name);
                if (hsURL == null) {
                    Freerouter.logWarn("HelpSet " + helpset_name + " not found.");
                } else {
                    BoardFrame.help_set = new HelpSet(null, hsURL);
                }
            } catch (HelpSetException ee) {
                Freerouter.logError("HelpSet " + helpset_name + " could not be opened.");
                Freerouter.logError(ee);
            }
            if (BoardFrame.help_set != null) {
                BoardFrame.help_broker = BoardFrame.help_set.createHelpBroker();
            }
            if (BoardFrame.help_broker != null) {
                // CSH.DisplayHelpFromSource is a convenience class to display the helpset
                contents_help = new CSH.DisplayHelpFromSource(BoardFrame.help_broker);
                direct_help = new CSH.DisplayHelpAfterTracking(BoardFrame.help_broker);
            }
        }
    }

    private static CSH.DisplayHelpFromSource contents_help = null;
    private static CSH.DisplayHelpAfterTracking direct_help = null;
}
