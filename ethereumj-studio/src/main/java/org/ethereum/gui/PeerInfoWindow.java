package org.ethereum.gui;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * A simple example showing how to modify the fonts and colors used in an
 * RSyntaxTextArea. There are two methods to do this - via the Java API, and via
 * an XML file. The latter method is preferred since it's more modular, and
 * provides a way for your users to customize RSTA in your application.<p>
 *
 * This example uses RSyntaxTextArea 2.0.1.<p>
 *
 * Project Home: http://fifesoft.com/rsyntaxtextarea<br>
 * Downloads: https://sourceforge.net/projects/rsyntaxtextarea
 */
public class PeerInfoWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    private boolean autoScroll = false;
    private RSyntaxTextArea textArea;
    private ToolBar toolBar = null;

    /**
     * ERROR (exceptions) WARN (when something happens that's not supposed to)
     *                    INFO (wire output)
     *                    DEBUG (test/displaying intermediate values),
     *                    TRACE (start/end method)
     */
    public PeerInfoWindow(PeersTableModel.PeerInfo peerInfo) {
        final PeerInfoWindow thisConsole = this;

        java.net.URL url = ClassLoader.getSystemResource("ethereum-icon.png");
        Toolkit kit = Toolkit.getDefaultToolkit();
        Image img = kit.createImage(url);
        this.setIconImage(img);
        addCloseAction();

        JPanel cp = new JPanel(new BorderLayout());

        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
        atmf.putMapping("text/console", "org.ethereum.gui.ConsoleTokenMaker");

        textArea = new RSyntaxTextArea(16, 44);
        textArea.setSyntaxEditingStyle("text/console");
//        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_LISP);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);

        RTextScrollPane sp = new RTextScrollPane(textArea);
        textArea.setText(peerInfo.toString());

        cp.add(sp);

        setContentPane(cp);
        setTitle("Connection Console");
//        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocation(802, 460);
        
        if (CONFIG.peerDiscovery())
            UIEthereumManager.ethereum.startPeerDiscovery();
        
    }


    public void addCloseAction() {
        this.addWindowListener(new WindowAdapter() {
        });
    }

    public static void main(String[] args) {
        // Start all Swing applications on the EDT.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new PeerInfoWindow(null).setVisible(true);
            }
        });
    }
}
