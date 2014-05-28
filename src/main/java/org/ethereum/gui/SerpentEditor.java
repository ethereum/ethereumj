package org.ethereum.gui;

import org.abego.treelayout.internal.util.Contract;
import org.ethereum.serpent.SerpentCompiler;
import org.fife.ui.rsyntaxtextarea.*;
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


    private final RSyntaxTextArea codeArea;
    private static final long serialVersionUID = 1L;

    public SerpentEditor() {

        final JPanel cp = new JPanel(new BorderLayout());
        final JFrame mainWindow = this;

        java.net.URL url = ClassLoader.getSystemResource("ethereum-icon.png");
        Toolkit kit = Toolkit.getDefaultToolkit();
        Image img = kit.createImage(url);
        this.setIconImage(img);
        this.setLocation(30, 70);

        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory)TokenMakerFactory.getDefaultInstance();
        atmf.putMapping("text/serpent", "org.ethereum.gui.SerpentTokenMaker");

        codeArea = new RSyntaxTextArea(32, 80);
        codeArea.setSyntaxEditingStyle("text/serpent");
        codeArea.setCodeFoldingEnabled(true);
        codeArea.setAntiAliasingEnabled(true);
        codeArea.setText(codeSample2);
        changeStyleProgrammatically();

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
        FlowLayout fl = new FlowLayout(FlowLayout.CENTER, 10, 5);
//        fl.setAlignment(FlowLayout.RIGHT);
        controlsPanel.setLayout(fl);

        JButton buildButton = new JButton("Build");
        JButton sendButton = new JButton("Send");
        JButton callButton = new JButton("Call");

        callButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ContractCallDialog payOutDialog =
                        new ContractCallDialog((Frame)SwingUtilities.getAncestorOfClass(JFrame.class,
                                cp));

            }
        });


        sendButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {


                String asmResult = "";
                try {

                    // todo: integrate new compiler when avail
                    asmResult = SerpentCompiler.compile(codeArea.getText());
                } catch (Throwable th) {
                    th.printStackTrace();

                    splitPanel.setDividerLocation(0.7);
                    result.setVisible(true);
                    result.setText(th.getMessage());
                    result.setForeground(Color.RED);
                    return;

                }

                byte[] machineCode =
                    SerpentCompiler.compileAssemblyToMachine(asmResult);

                ContractSubmitDialog payOutDialog =
                        new ContractSubmitDialog((Frame)SwingUtilities.getAncestorOfClass(JFrame.class,
                                cp), machineCode);
            }
        });


        buildButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                String asmResult = "";
                try {

                    // todo: integrate new compiler when avail
                    asmResult = SerpentCompiler.compile(codeArea.getText());
                } catch (Throwable th) {
                    th.printStackTrace();

                    splitPanel.setDividerLocation(0.7);
                    result.setVisible(true);
                    result.setText(th.getMessage());
                    result.setForeground(Color.RED);
                    return;

                }

                result.setForeground(Color.BLUE);
                splitPanel.setDividerLocation(0.7);
                result.setVisible(true);
                result.setText(asmResult);
            }
        });


        controlsPanel.add(callButton, FlowLayout.LEFT);

        controlsPanel.add(sendButton);
        controlsPanel.add(buildButton);

        cp.add(controlsPanel, BorderLayout.SOUTH);


        setContentPane(cp);
        setTitle("Serpent Editor");
//        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
//        setLocationRelativeTo(null);

    }

    private void changeStyleProgrammatically() {

        // Set the font for all token types.

        // Change a few things here and there.
        SyntaxScheme scheme = codeArea.getSyntaxScheme();

//        scheme.getStyle(Token.RESERVED_WORD).background = Color.white;
//        scheme.getStyle(Token.RESERVED_WORD).foreground = Color.BLUE;

        scheme.getStyle(Token.IDENTIFIER).foreground = Color.black;


        scheme.getStyle(Token.RESERVED_WORD_2).background = Color.white;
        scheme.getStyle(Token.RESERVED_WORD_2).foreground = Color.MAGENTA.darker().darker();


//        scheme.getStyle(Token.LITERAL_STRING_DOUBLE_QUOTE).underline = true;
//        scheme.getStyle(Token.LITERAL_NUMBER_HEXADECIMAL).underline = true;
//        scheme.getStyle(Token.LITERAL_NUMBER_HEXADECIMAL).background = Color.pink;

//        scheme.getStyle(Token.COMMENT_EOL).font = new Font("Georgia", Font.ITALIC, 10);

        codeArea.revalidate();
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