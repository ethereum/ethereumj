package org.ethereum.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 23/05/2014 13:51
 */

public class GUIUtils {


    public static void addStyle(JTextField textField, String labelName){
        textField.setHorizontalAlignment(SwingConstants.RIGHT);
        Border line = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        TitledBorder titled = BorderFactory.createTitledBorder(line, labelName);
        titled.setTitleFont(new Font("Verdana", 0, 13));
        titled.setTitleColor(new Color(213, 225, 185));
        Border empty = new EmptyBorder(5, 8, 5, 8);
        CompoundBorder border = new CompoundBorder(titled, empty);
        textField.setBorder(border);
        textField.setForeground(new Color(143, 170, 220));
        textField.setFont(new Font("Monospaced", 0, 13));
    }
}
