package org.ethereum.gui;

import org.ethereum.serpent.SerpentCompiler;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 24/04/14 11:32
 */


public class SerpentEditor extends JFrame {

    private String codeSample = "\n\n\n" +
                                "" +
                                "if !contract.storage[msg.data[0]]:\n" +
                                "        contract.storage[msg.data[0]] = msg.data[1]\n" +
                                "    return(1)\n" +
                                "else:\n" +
                                "    return(0)\n";

    private String codeSample2 = "\n\n\n" +
            "" +
            "\n" +
            "\n" +
            "a=block.gaslimit\n" +
            "b = block.difficulty\n" +
            "if 10*2+5 > 15:\n" +
            "    b = 2\n" +
            "elif 2*6+5 < a ^ 6:\n" +
            "    c = 4\n" +
            "else:\n" +
            "    d = 5\n" +
            "    \n" +
            "\n" +
            "return(0)\n";



    private static final long serialVersionUID = 1L;

    public SerpentEditor() {

        final JPanel cp = new JPanel(new BorderLayout());
        final JFrame mainWindow = this;

        java.net.URL url = ClassLoader.getSystemResource("ethereum-icon.png");
        Toolkit kit = Toolkit.getDefaultToolkit();
        Image img = kit.createImage(url);
        this.setIconImage(img);
        this.setLocation(30, 80);

        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory)TokenMakerFactory.getDefaultInstance();
        atmf.putMapping("text/serpent", "org.ethereum.gui.SerpentTokenMaker");

        final RSyntaxTextArea codeArea = new RSyntaxTextArea(32, 80);
        codeArea.setSyntaxEditingStyle("text/serpent");
        codeArea.setCodeFoldingEnabled(true);
        codeArea.setAntiAliasingEnabled(true);
        codeArea.setText(codeSample2);

        RTextScrollPane sp = new RTextScrollPane(codeArea);

        sp.setFoldIndicatorEnabled(true);
        cp.setLayout(new BorderLayout());

        final JSplitPane splitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPanel.setOneTouchExpandable(true);
        splitPanel.setDividerSize(5);
        splitPanel.setContinuousLayout(true);

        cp.add(splitPanel, BorderLayout.CENTER);
        splitPanel.add(sp);

        final JTextArea result = new JTextArea();
        result.setLineWrap(true);
        result.setWrapStyleWord(true);
        result.setVisible(false);

        splitPanel.add(result);

        JPanel controlsPanel = new JPanel();
        FlowLayout fl = new FlowLayout(FlowLayout.LEADING, 30, 5);
        fl.setAlignment(FlowLayout.RIGHT);
        controlsPanel.setLayout(fl);

        JButton buildButton = new JButton("Build");
        buildButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                String asmResult = "";
                try {

                    // todo: integrate new compiler when avail
                    asmResult = SerpentCompiler.compile(codeArea.getText());
                } catch (Throwable th) {th.printStackTrace();}

                splitPanel.setDividerLocation(0.7);

                result.setVisible(true);
                result.setText(asmResult);
            }
        });


        controlsPanel.add(buildButton);

        cp.add(controlsPanel, BorderLayout.SOUTH);


        setContentPane(cp);
        setTitle("Serpent Editor");
//        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
//        setLocationRelativeTo(null);

    }

    public static void main(String[] args) {
        // Start all Swing applications on the EDT.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new SerpentEditor().setVisible(true);
            }
        });
    }


}