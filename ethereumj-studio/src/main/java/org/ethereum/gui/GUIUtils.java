package org.ethereum.gui;

import org.ethereum.serpent.SerpentCompiler;
import org.spongycastle.util.encoders.Hex;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 23/05/2014 13:51
 */
public class GUIUtils {

    public static void addStyle(JTextField textField, String labelName) {
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

    public static void addStyle(JTextArea textArea, String labelName, boolean isBorder) {

        Border border = null;
        if (isBorder) {
            Border line = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
            TitledBorder titled = BorderFactory.createTitledBorder(line, labelName);
            titled.setTitleFont(new Font("Verdana", 0, 13));
            titled.setTitleColor(new Color(213, 225, 185));
            Border empty = new EmptyBorder(5, 8, 5, 8);
            CompoundBorder cBorder = new CompoundBorder(titled, empty);
        }
        textArea.setBorder(border);
        textArea.setForeground(new Color(143, 170, 220));
        textArea.setFont(new Font("Monospaced", 0, 13));
    }

    public static void addStyle(JScrollPane jScrollPane, String labelName) {
        Border line = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        TitledBorder titled = BorderFactory.createTitledBorder(line, labelName);
        titled.setTitleFont(new Font("Verdana", 0, 13));
        titled.setTitleColor(new Color(213, 225, 185));
        Border empty = new EmptyBorder(5, 8, 5, 8);
        CompoundBorder border = new CompoundBorder(titled, empty);
        jScrollPane.setBorder(border);
        jScrollPane.setForeground(new Color(143, 170, 220));
        jScrollPane.setBackground(Color.WHITE);
        jScrollPane.setFont(new Font("Monospaced", 0, 13));
        jScrollPane.setHorizontalScrollBar(null);
    }

    public static void addStyle(JTable jTable) {
        jTable.setForeground(new Color(143, 170, 220));
        jTable.setBackground(Color.WHITE);
        jTable.setFont(new Font("Monospaced", 0, 13));
    }

    public static String getHexStyledText(byte[] data) {
        String[] dataHex = Hex.toHexString(data).split("(?<=\\G.{2})");
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < dataHex.length; ++i) {
            sb.append(dataHex[i]).append(" ");
            if ((i + 1) % 8 == 0 && i != 0) sb.append("\n");
        }
        return sb.toString();
    }

    public static String getStyledAsmCode(String asmCode) {

        String initBlock = SerpentCompiler.extractInitBlock(asmCode);
        String codeBlock = SerpentCompiler.extractCodeBlock(asmCode);

        return String.format(" \n\n *** [Init] *** \n\n     %s \n" +
                "\n *** [Code] *** \n\n      %s \n\n", initBlock, codeBlock);
    }
}
