package org.ethereum.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 15/05/14 12:36
 */
public class BlockChainTable extends JFrame {

    private JPanel topPanel;
    private	JTable		table;
    private	JScrollPane scrollPane;


    public BlockChainTable() {

        setTitle("Block Chain Table");
        setSize(700, 400);
        setLocation(315, 180);
        setBackground(Color.gray);

        java.net.URL url = ClassLoader.getSystemResource("ethereum-icon.png");
        Toolkit kit = Toolkit.getDefaultToolkit();
        Image img = kit.createImage(url);
        this.setIconImage(img);

        // Create a panel to hold all other components
        topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        getContentPane().add(topPanel);

        // Create a new table instance
        table = new JTable();
        table.setModel(new BlockTableModel());

        table.setFont(new Font("Courier New", Font.PLAIN, 13));
        table.setForeground(Color.GRAY);
        table.setTableHeader(null);

        TableCellRenderer tcr = table.getDefaultRenderer(String.class);
        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) tcr;
        renderer.setHorizontalAlignment(SwingConstants.LEFT);
        renderer.setVerticalAlignment(SwingConstants.TOP);


        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setCellSelectionEnabled(true);

        table.setRowMargin(3);
        table.setRowHeight(120);

        table.getColumnModel().getColumn(0).setCellRenderer(new TableCellLongTextRenderer());

        // Add the table to a scrolling pane
        scrollPane = new JScrollPane(table);
        topPanel.add(scrollPane, BorderLayout.CENTER);

    }

    public static void main(String args[]){

        BlockChainTable mainFrame = new BlockChainTable();
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


    }
}
