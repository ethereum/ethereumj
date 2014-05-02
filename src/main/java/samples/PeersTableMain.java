package samples;

import org.ethereum.gui.PeersTableModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 25/04/14 07:11
 */
public class PeersTableMain extends JFrame{



    // Instance attributes used in this example
    private JPanel topPanel;
    private	JTable		table;
    private	JScrollPane scrollPane;

    // Constructor of main frame
    public PeersTableMain()
    {
        // Set the frame characteristics
        setTitle( "Ethereum Peers" );
        setSize( 355, 300 );
        setLocation(815, 80);
        setBackground( Color.gray );

        java.net.URL url = ClassLoader.getSystemResource("ethereum-icon.png");
        Toolkit kit = Toolkit.getDefaultToolkit();
        Image img = kit.createImage(url);
        this.setIconImage(img);


        // Create a panel to hold all other components
        topPanel = new JPanel();
        topPanel.setLayout( new BorderLayout() );
        getContentPane().add( topPanel );


        // Create a new table instance
        table = new JTable( );
        table.setModel(new PeersTableModel());

        table.setFont(new Font("Courier New", Font.PLAIN, 18));
        table.setForeground(Color.GRAY);
        table.setTableHeader(null);


        TableCellRenderer tcr = table.getDefaultRenderer(String.class);
        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer)tcr;
        renderer.setHorizontalAlignment(SwingConstants.CENTER);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setCellSelectionEnabled(true);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(60);

        table.setRowMargin(3);
        table.setRowHeight(50);

        // Add the table to a scrolling pane
        scrollPane = new JScrollPane( table );
        topPanel.add( scrollPane, BorderLayout.CENTER );
    }

    public static void main(String args[]){

        PeersTableMain mainFrame	= new PeersTableMain();
        mainFrame.setVisible( true );
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }
}
