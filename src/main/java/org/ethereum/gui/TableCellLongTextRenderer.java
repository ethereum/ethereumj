package org.ethereum.gui;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 15/05/14 13:08
 */
public class TableCellLongTextRenderer extends JTextArea implements TableCellRenderer{

    protected static Border m_noFocusBorder;

    public TableCellLongTextRenderer() {
        m_noFocusBorder = new EmptyBorder(1, 2, 1, 2);
        setOpaque(true);
        setBorder(m_noFocusBorder);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        this.setText((String) value);
        this.setSelectedTextColor(Color.BLUE);
        this.setWrapStyleWord(true);
        this.setLineWrap(true);

        setBackground(isSelected && !hasFocus ? table.getSelectionBackground() : table.getBackground());
        setForeground(isSelected && !hasFocus ? table.getSelectionForeground() : table.getForeground());
        setBorder(hasFocus? UIManager.getBorder("Table.focusCellHighlightBorder") : m_noFocusBorder);

        //set the JTextArea to the width of the table column
        setSize(table.getColumnModel().getColumn(column).getWidth(), getPreferredSize().height);
        if (table.getRowHeight(row) != getPreferredSize().height) {
            //set the height of the table row to the calculated height of the JTextArea
            table.setRowHeight(row, getPreferredSize().height);
        }

        return this;
    }

}

