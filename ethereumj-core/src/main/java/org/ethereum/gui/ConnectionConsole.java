package org.ethereum.gui;

import org.ethereum.net.client.ClientPeer;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.TimerTask;

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
public class ConnectionConsole extends JFrame implements PeerListener{

    private static final long serialVersionUID = 1L;

    private RSyntaxTextArea textArea;


    public ConnectionConsole() {

        final ConnectionConsole thisConsole = this;

        java.net.URL url = ClassLoader.getSystemResource("ethereum-icon.png");
        Toolkit kit = Toolkit.getDefaultToolkit();
        Image img = kit.createImage(url);
        this.setIconImage(img);

        JPanel cp = new JPanel(new BorderLayout());

        textArea = new RSyntaxTextArea(16, 47);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_LISP);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
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
//                        new ClientPeer(thisConsole).connect("54.201.28.117", 30303);
//                        new ClientPeer(thisConsole).connect("82.217.72.169", 30303);
                        new ClientPeer(thisConsole).connect("54.204.10.41", 30303);
//                        new ClientPeer(thisConsole).connect("82.217.72.169", 30303);
                    }
                };
                t.start();



            }

        });

    }

    @Override
    public void console(final String output) {


        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                textArea.append(output);
                textArea.append("\n");
                textArea.setCaretPosition(textArea.getText().length());
            }
        });


    }

    public static void main(String[] args) {
        // Start all Swing applications on the EDT.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ConnectionConsole().setVisible(true);
            }
        });

    }

}
