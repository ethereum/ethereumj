package org.ethereum.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.TextArea;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.math.BigInteger;
import java.util.Map;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.db.ContractDetails;
import org.ethereum.manager.WorldManager;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.OpCode;
import org.ethereum.vm.Program;
import org.ethereum.vm.ProgramInvoke;
import org.ethereum.vm.ProgramInvokeFactory;
import org.ethereum.vm.Program.ProgramListener;
import org.spongycastle.util.encoders.DecoderException;
import org.spongycastle.util.encoders.Hex;
import java.awt.Component;
import java.awt.FlowLayout;

public class StateExplorerWindow extends JFrame{
	
	private ToolBar toolBar = null;
	private JTextField txfAccountAddress;
	private WindowTextArea txaPrinter;
	private JButton btnPlayCode;
	private AccountsListWindow accountsListWindow;
	ProgramPlayDialog codePanel;
	
	private JTable tblStateDataTable;
	private StateDataTableModel dataModel;
	String[] dataTypes = {"String", "Hex", "Number"};
	
	public StateExplorerWindow(ToolBar toolBar) {
		this.toolBar = toolBar;
		
        java.net.URL url = ClassLoader.getSystemResource("ethereum-icon.png");
        Toolkit kit = Toolkit.getDefaultToolkit();
        Image img = kit.createImage(url);
        this.setIconImage(img);
        setTitle("State Explorer");
        setSize(700, 530);
        setLocation(50, 180);
        setResizable(false);
        
        /*
         * top search panel 
         */
        JPanel panel = new JPanel();
        getContentPane().add(panel);
        
        Box horizontalBox = Box.createHorizontalBox();
        panel.add(horizontalBox);
        
        java.net.URL imageURL = ClassLoader.getSystemResource("buttons/list.png");
        ImageIcon image = new ImageIcon(imageURL);
        JToggleButton btnListAccounts = new JToggleButton(""); 
        btnListAccounts.setIcon(image);
        btnListAccounts.setContentAreaFilled(true);
        btnListAccounts.setToolTipText("Serpent Editor");
        btnListAccounts.setBackground(Color.WHITE);
        btnListAccounts.setBorderPainted(false);
        btnListAccounts.setFocusPainted(false);
        btnListAccounts.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnListAccounts.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
            	SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                       if(accountsListWindow == null)
                    	   accountsListWindow = new AccountsListWindow();
                       accountsListWindow.setVisible(true);
                    }
                });
            }
        });
        horizontalBox.add(btnListAccounts);
        
        
        txfAccountAddress = new JTextField();
        horizontalBox.add(txfAccountAddress);
        txfAccountAddress.setColumns(30);
        
        JButton btnSearch = new JButton("Search");
        horizontalBox.add(btnSearch);
        btnSearch.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				searchAccount(txfAccountAddress.getText());
			}			
        });
        
        btnPlayCode = new JButton("Play Code");
        horizontalBox.add(btnPlayCode);
        btnPlayCode.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				byte[] code = WorldManager.getInstance().getRepository().getCode(Hex.decode(txfAccountAddress.getText()));
				if(code != null)
					ProgramPlayDialog.createAndShowGUI(code, null, null);
			}			
        });
        
        /*
         * center text panel
         */
        JPanel centerPanel = new JPanel();
        panel.add(centerPanel);
        
        txaPrinter = new WindowTextArea();
        centerPanel.add(txaPrinter);
        
        /*
         * bottom data panel
         */      
        // data type choice boxes
        Box Hbox = Box.createHorizontalBox();
        panel.add(Hbox);
                
        Box VBox1 = Box.createVerticalBox();
        JLabel l1 = new JLabel("Key Encoding");
        l1.setAlignmentX(Component.CENTER_ALIGNMENT);
        JComboBox cmbKey = new JComboBox(dataTypes);
        cmbKey.setSelectedIndex(1);
        cmbKey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cmb = (JComboBox) e.getSource();
				DataEncodingType t = DataEncodingType.getTypeFromString((String) cmb.getSelectedItem());
				dataModel.setKeyEncoding(t);
			}
		});
        VBox1.add(l1);
        VBox1.add(cmbKey);
        
        Box VBox2 = Box.createVerticalBox();
        VBox2.setAlignmentX(LEFT_ALIGNMENT);
        JLabel l2 = new JLabel("Value Encoding");
        l2.setAlignmentX(Component.CENTER_ALIGNMENT);
        JComboBox cmbValue = new JComboBox(dataTypes);
        cmbValue.setSelectedIndex(1);
        cmbValue.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cmb = (JComboBox) e.getSource();
				DataEncodingType t = DataEncodingType.getTypeFromString((String) cmb.getSelectedItem());
				dataModel.setValueEncoding(t);
			}
		});
        VBox2.add(l2);
        VBox2.add(cmbValue);
        
        Hbox.add(VBox1);
        
        JPanel spacer = new JPanel();
        FlowLayout flowLayout = (FlowLayout) spacer.getLayout();
        flowLayout.setHgap(100);
        Hbox.add(spacer);
        Hbox.add(VBox2);
        
        // table
        tblStateDataTable = new JTable();
        dataModel = new StateDataTableModel();
        tblStateDataTable.setModel(dataModel);
        
        
        JScrollPane scrollPane = new JScrollPane(tblStateDataTable);
        scrollPane.setPreferredSize(new Dimension(600,200));
        panel.add(scrollPane);
	}
	
	private void searchAccount(String accountStr){
		txaPrinter.clean();
		
		byte[] add = null;
		try { add = Hex.decode(txfAccountAddress.getText()); } 
		catch(DecoderException ex) {  return; }
		
		txaPrinter.println(accountDetailsString(add, dataModel));
	}
	
	private String accountDetailsString(byte[] account, StateDataTableModel dataModel){	
		String ret = "";
		// 1) print account address
		ret = "Account: " + Hex.toHexString(account) + "\n";
		
		//2) print state 
		AccountState state = WorldManager.getInstance().getRepository().getAccountState(account);
		if(state != null)
			ret += state.toString() + "\n";
		
		//3) print storage
		ContractDetails contractDetails = WorldManager.getInstance().getRepository().getContractDetails(account);
		if(contractDetails != null) {
			Map<DataWord, DataWord> accountStorage = contractDetails.getStorage();
			dataModel.setData(accountStorage);
		}
		
		//4) code print
		byte[] code = WorldManager.getInstance().getRepository().getCode(account);
		if(code != null) {
			ret += "\n\nCode:\n";
			ret += Program.stringify(code, 0, "");
		}
		
		return ret;
	}
	
	private class StateDataTableModel extends AbstractTableModel {

		Map<DataWord, DataWord> data;
		DataEncodingType keyEncodingType = DataEncodingType.HEX;
		DataEncodingType valueEncodingType = DataEncodingType.HEX;
		String[] columns = new String[]{ "Key", "Value"};
		
		public StateDataTableModel() { }
		
		public StateDataTableModel(Map<DataWord, DataWord> initData) {
			setData(initData);
		}
		
		public void setData(Map<DataWord, DataWord> initData) {
			data = initData;
			fireTableDataChanged();
		}
		
		public void setKeyEncoding(DataEncodingType type) {
			keyEncodingType  = type;
			fireTableDataChanged();
		}
		
		public void setValueEncoding(DataEncodingType type) {
			valueEncodingType  = type;
			fireTableDataChanged();
		}
		
		@Override
		public String getColumnName(int column) {
			return columns[column];
		}
		
		@Override
		public int getRowCount() {
			return data == null? 0:data.size();
		}

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			DataWord key = (DataWord) this.data.keySet().toArray()[rowIndex];
			
			if(columnIndex == 0) {
				return getDataWithEncoding(key.getData(), keyEncodingType);
			}
			else {
				DataWord value = this.data.get(key);
				return getDataWithEncoding(value.getData(), valueEncodingType);
			}
		}
		
		private String getDataWithEncoding(byte[] data, DataEncodingType enc) {
			switch(enc) {
			case STRING:
				return new String(data);
			case HEX:
				return Hex.toHexString(data);
			case NUMBER:
				return new BigInteger(data).toString();
			}
			
			return data.toString();
		}
	}
	
	private enum DataEncodingType{
		STRING,
		HEX,
		NUMBER;
		
		static public DataEncodingType getTypeFromString(String value) {
			switch(value){
			case "String":
				return STRING;
			case "Hex":
				return HEX;
			case "Number":
				return NUMBER;
			}
			return STRING;
		}
	}
	
	private class WindowTextArea extends TextArea {
		
		public WindowTextArea() {
			super();
		}
		
		public void println(String txt) {
			setText(getText() + txt + "\n");
		}
		
		public void clean() {
			setText("");
		}
	}
	
	public static void main(String[] args) {
        // Start all Swing applications on the EDT.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new StateExplorerWindow(null).setVisible(true);
            }
        });
    }
}
