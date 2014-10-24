package org.ethereum.gui;



import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 25/04/14 07:11
 */
public class PeersTableWindow extends JFrame {

    // Instance attributes used in this example
    private JPanel topPanel;
    private	JTable		table;
    private	JScrollPane scrollPane;
    private Timer updater = new Timer();

    private ToolBar toolBar;

	// Constructor of main frame
	public PeersTableWindow(ToolBar toolBar) {

        this.toolBar = toolBar;
        addCloseAction();

		// Set the frame characteristics
		setTitle("Ethereum Peers");
		setSize(515, 400);
		setLocation(615, 30);

		java.net.URL url = ClassLoader.getSystemResource("ethereum-icon.png");
		Toolkit kit = Toolkit.getDefaultToolkit();
		Image img = kit.createImage(url);
		this.setIconImage(img);

		// Create a panel to hold all other components
		topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
        topPanel.setBackground(Color.WHITE);

		getContentPane().add(topPanel);
        getContentPane().setBackground(Color.WHITE);

		// Create a new table instance
		table = new JTable();
		table.setModel(new PeersTableModel());

        table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                JTable table =(JTable) me.getSource();
                Point p = me.getPoint();
                int row = table.rowAtPoint(p);
                PeersTableModel model = (PeersTableModel) table.getModel();
                if (me.getClickCount() == 2) {
                    final PeersTableModel.PeerInfo peerInfo = model.getPeerInfo(row);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            new PeerInfoWindow(peerInfo).setVisible(true);
                        }
                    });
                    System.out.println(peerInfo);
                }
            }
        });

		table.setFont(new Font("Courier New", Font.PLAIN, 15));
		table.setForeground(Color.GRAY);
		table.setTableHeader(null);

		TableCellRenderer tcr = table.getDefaultRenderer(String.class);
		DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) tcr;
		renderer.setHorizontalAlignment(SwingConstants.CENTER);

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setCellSelectionEnabled(true);

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.getColumnModel().getColumn(0).setPreferredWidth(60);
		table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(60);

		table.setRowMargin(3);
		table.setRowHeight(50);

        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        table.setGridColor(new Color(230, 230, 230));

		// Add the table to a scrolling pane
		scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);

		topPanel.add(scrollPane, BorderLayout.CENTER);

        updater.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                table.revalidate();
                table.repaint();
            }
        }, 1000, 1000);

        if (CONFIG.peerDiscovery())
            UIEthereumManager.ethereum.startPeerDiscovery();
    }

	public void addCloseAction() {
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				toolBar.peersToggle.setSelected(false);
			}
		});
	}

	public static void main(String args[]) {
		PeersTableWindow mainFrame = new PeersTableWindow(null);
		mainFrame.setVisible(true);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
