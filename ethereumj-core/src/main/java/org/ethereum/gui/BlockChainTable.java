package org.ethereum.gui;

import org.ethereum.core.Block;
import org.ethereum.manager.MainData;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 15/05/14 12:36
 */
public class BlockChainTable extends JFrame {

    private JPanel topPanel;
    private	JTable		table;
    private	JScrollPane scrollPane;

    private int lastFindIndex = 0;

    ToolBar toolBar;

    public BlockChainTable(ToolBar toolBar) {

        this.toolBar = toolBar;
        addCloseAction();

        final BlockChainTable blockChainTable = this;

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
        table.setRowHeight(420);

        table.getColumnModel().getColumn(0).setCellRenderer(new TableCellLongTextRenderer());

        table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), "Copy");
        table.getActionMap().put("Copy", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (MainData.instance.getBlockchain().size() - 1 < lastFindIndex) return;

                Block block = MainData.instance.getBlockchain().get(lastFindIndex);
                StringSelection stsel = new StringSelection(block.toString());
                Clipboard system = Toolkit.getDefaultToolkit().getSystemClipboard();
                system.setContents(stsel,stsel);
            }
        } );

        table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK), "Find");
        table.getActionMap().put("Find", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String toFind = JOptionPane.showInputDialog(blockChainTable, "Find:",
                        "Find in BlockChain", JOptionPane.QUESTION_MESSAGE);

                if (toFind.equals("")) {
                    lastFindIndex = 0;
                    return;
                }

                for (int i = lastFindIndex + 1; i < MainData.instance.getBlockchain().size(); ++i) {

                    if (MainData.instance.getBlockchain().size() - 1 < i) return;
                    Block block = MainData.instance.getBlockchain().get(i);
                    boolean found = block.toString().toLowerCase().contains(toFind.toLowerCase());
                    if (found) {
                        // todo: now we find the first occur
                        // todo: in the future I should keep
                        // todo: all of them and allow to jump over them
                        table.scrollRectToVisible(table.getCellRect(i, 0, true));
                        lastFindIndex = i;
                        break;
                    }
                }
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                lastFindIndex = ((JTable)(e.getSource())).rowAtPoint(e.getPoint());

                super.mouseClicked(e);
            }
        });

        // Add the table to a scrolling pane
        scrollPane = new JScrollPane(table);
        topPanel.add(scrollPane, BorderLayout.CENTER);
    }

    public void addCloseAction(){
        this.addWindowListener( new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                toolBar.chainToggle.setSelected(false);

            }
        });
    }


    public static void main(String args[]){
        BlockChainTable mainFrame = new BlockChainTable(null);
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
