package org.ethereum.gui;

import org.ethereum.config.SystemProperties;
import org.ethereum.listener.EthereumListenerAdapter;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rtextarea.RTextScrollPane;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

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
public class ConnectionConsoleWindow extends JFrame {

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
    public ConnectionConsoleWindow(ToolBar toolBar) {
        final ConnectionConsoleWindow thisConsole = this;
        this.toolBar = toolBar;

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

        changeStyleProgrammatically();
        RTextScrollPane sp = new RTextScrollPane(textArea);

        cp.add(sp);

        setContentPane(cp);
        setTitle("Connection Console");
//        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocation(802, 460);

        if (CONFIG.peerDiscovery())
            UIEthereumManager.ethereum.startPeerDiscovery();

        Thread t = new Thread() {
            public void run() {

                UIEthereumManager.ethereum.connect(
                        SystemProperties.CONFIG.activePeerIP(),
                        SystemProperties.CONFIG.activePeerPort(),
                        SystemProperties.CONFIG.activePeerNodeid());
            }
        };

        UIEthereumManager.ethereum.addListener(new EthereumListenerAdapter() {
            @Override
            public void trace(final String output) {
                SwingUtilities.invokeLater(() -> {
                    textArea.append(output);
                    textArea.append("\n");

                    if (autoScroll)
                        textArea.setCaretPosition(textArea.getText().length());
                });
            }
        });
        t.start();
    }


    private void changeStyleProgrammatically() {

        // Set the font for all token types.
        setFont(textArea, new Font("Courier New", Font.PLAIN, 12));

        // Change a few things here and there.
        SyntaxScheme scheme = textArea.getSyntaxScheme();
        scheme.getStyle(Token.RESERVED_WORD).background = Color.white;
        scheme.getStyle(Token.RESERVED_WORD).foreground = Color.MAGENTA.darker().darker();

        scheme.getStyle(Token.DATA_TYPE).foreground = Color.blue;
        scheme.getStyle(Token.LITERAL_STRING_DOUBLE_QUOTE).underline = true;
        scheme.getStyle(Token.LITERAL_NUMBER_HEXADECIMAL).underline = true;
        scheme.getStyle(Token.LITERAL_NUMBER_HEXADECIMAL).background = Color.pink;

        scheme.getStyle(Token.COMMENT_EOL).font = new Font("Georgia",
                Font.ITALIC, 10);

        textArea.revalidate();
    }

    public static void setFont(RSyntaxTextArea textArea, Font font) {
        if (font != null) {
            SyntaxScheme ss = textArea.getSyntaxScheme();
            ss = (SyntaxScheme) ss.clone();
            for (int i = 0; i < ss.getStyleCount(); i++) {
                if (ss.getStyle(i) != null) {
                    ss.getStyle(i).font = font;
                }
            }
            textArea.setSyntaxScheme(ss);
            textArea.setFont(font);
        }
    }

    public void addCloseAction() {
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                toolBar.logToggle.setSelected(false);
            }
        });
    }

    public static void main(String[] args) {
        // Start all Swing applications on the EDT.
        SwingUtilities.invokeLater(() -> new ConnectionConsoleWindow(null).setVisible(true));
    }


}


