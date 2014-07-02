package org.ethereum.gui;

import org.ethereum.core.Block;
import org.ethereum.manager.WorldManager;

import javax.swing.table.AbstractTableModel;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 15/05/14 12:42
 */
public class BlockTableModel extends AbstractTableModel {

    @Override
    public int getRowCount() {

        fireTableDataChanged();
        int rowCount = WorldManager.getInstance().getBlockChain().getSize();
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

        Block block = WorldManager.getInstance().getBlockChain().getByNumber(rowIndex);
        if (block == null) return "";

        return block.toString();
    }
}
