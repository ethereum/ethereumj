package org.ethereum.gui;

import org.ethereum.manager.MainData;

import javax.swing.table.AbstractTableModel;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 15/05/14 12:42
 */
public class BlockTableModel extends AbstractTableModel {

    @Override
    public int getRowCount() {

        fireTableDataChanged();
        int rowCount = MainData.instance.getBlockchain().size();
        return rowCount;
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

//        byte[] hash = MainData.instance.getAllBlocks().get(rowIndex).getHash();
//        return Hex.toHexString(hash);

        return MainData.instance.getBlockchain().get(rowIndex).toString();
    }
}
