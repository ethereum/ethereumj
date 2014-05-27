package org.ethereum.gui;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.ethereum.net.client.ClientPeer;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.RTextScrollPane;

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
public class ConnectionConsoleWindow extends JFrame implements PeerListener{

    private static final long serialVersionUID = 1L;

    private boolean autoScroll = false;

    private RSyntaxTextArea textArea;

    /**
     * ERROR (exceptions) WARN (when something happens that's not supposed to)
     *                    INFO (wire output)
     *                    DEBUG (test/displaying intermediate values),
     *                    TRACE (start/end method)
     */

    public ConnectionConsoleWindow() {
        final ConnectionConsoleWindow thisConsole = this;

        java.net.URL url = ClassLoader.getSystemResource("ethereum-icon.png");
        Toolkit kit = Toolkit.getDefaultToolkit();
        Image img = kit.createImage(url);
        this.setIconImage(img);

        JPanel cp = new JPanel(new BorderLayout());

        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
        atmf.putMapping("text/console", "org.ethereum.gui.ConsoleTokenMaker");

        textArea = new RSyntaxTextArea(16, 47);
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
        setLocation(775, 390);

        this.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent e) {
                Thread t = new Thread() {
                    public void run() {

                    	// Peer Server Zero: peer discovery
//                    	new ClientPeer(thisConsole).connect("54.201.28.117", 30303);
                    	
                    	// Peer Server One: peer discovery
//                    	new ClientPeer(thisConsole).connect("54.204.10.41", 30303);
                    	
                    	  // Some dude in Canada
//                    	new ClientPeer(thisConsole).connect("131.104.247.135", 30303);
                    	
                    	  // Nick
//                    	new ClientPeer(thisConsole).connect("82.217.72.169", 30303);
                    	
                    	// c++: ZeroGox
//                    	new ClientPeer(thisConsole).connect("54.204.10.41", 30303);
                    	
                    	// RomanJ
                    	new ClientPeer(thisConsole).connect("54.211.14.10", 40404);
                    }
                };
                t.start();
            }
        });
    }

	@Override
	public void console(final String output) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				textArea.append(output);
				textArea.append("\n");

                if (autoScroll)
                    textArea.setCaretPosition(textArea.getText().length());
			}
		});
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




	public static void main(String[] args) {
		// Start all Swing applications on the EDT.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new ConnectionConsoleWindow().setVisible(true);
			}
		});
	}
}
