package org.ethereum.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.ethereum.core.Account;
import org.ethereum.core.AccountState;
import org.ethereum.core.Denomination;
import org.ethereum.crypto.HashUtil;
import org.ethereum.manager.WorldManager;
import org.iq80.leveldb.DBIterator;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.encoders.Hex;

public class AccountsListWindow  extends JFrame {
	
	private JTable tblAccountsDataTable;
	private AccountsDataAdapter adapter;
	
	public AccountsListWindow() {
		java.net.URL url = ClassLoader.getSystemResource("ethereum-icon.png");
        Toolkit kit = Toolkit.getDefaultToolkit();
        Image img = kit.createImage(url);
        this.setIconImage(img);
        setTitle("Accounts List");
        setSize(700, 500);
        setLocation(50, 180);
        setResizable(false);
        
        JPanel panel = new JPanel();
        getContentPane().add(panel);
        
        tblAccountsDataTable = new JTable();
        
        adapter = new AccountsDataAdapter(new ArrayList<DataClass>());
        tblAccountsDataTable.setModel(adapter);
        
        JScrollPane scrollPane = new JScrollPane(tblAccountsDataTable);
        scrollPane.setPreferredSize(new Dimension(680,490));
        panel.add(scrollPane);
     
        loadAccounts();
	}
	
	private void loadAccounts() {
		new Thread(){
			
			@Override
			public void run(){
				DBIterator i = WorldManager.getInstance().getRepository().getAccountsIterator();
				while(i.hasNext()) {
					DataClass dc = new DataClass();
					dc.address = i.next().getKey();
					
					AccountState state = WorldManager.getInstance().getRepository().getAccountState(dc.address);
					dc.accountState = state;
					
					adapter.addDataPiece(dc);
				}
			}
		}.start();
	}
	
	private class AccountsDataAdapter extends AbstractTableModel {
		List<DataClass> data;
		
		final String[] columns = new String[]{ "Account", "Balance", "Is Contract"};
		
		public AccountsDataAdapter(List<DataClass> data) {
			this.data = data;
		}
		
		public void addDataPiece(DataClass d) {
			data.add(d);
			this.fireTableRowsInserted(Math.min(data.size() - 2, 0), data.size() - 1);
		}

		@Override
		public int getRowCount() {
			return data.size();
		}

		@Override
		public int getColumnCount() {
			return 3;
		}
		
		@Override
		public String getColumnName(int column) {
			return columns[column];
		}
		
		@Override
	    public boolean isCellEditable(int row, int column) { // custom isCellEditable function
	       return column == 0? true:false;
	    }

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if(columnIndex == 0) {
				return Hex.toHexString(data.get(rowIndex).address);
			}
			else if(columnIndex == 1 ){
				if(data.get(rowIndex).accountState != null) {
					return Denomination.toFriendlyString(data.get(rowIndex).accountState.getBalance());
				}
				return "---";
			}
			else {
				if(data.get(rowIndex).accountState != null) {
					if(!Arrays.areEqual(data.get(rowIndex).accountState.getCodeHash(), HashUtil.EMPTY_DATA_HASH))
						return "Yes";
				}
				return "No";
			}
		}
	}
	
	private class DataClass {
		public byte[] address;
		public AccountState accountState;
	}

}
