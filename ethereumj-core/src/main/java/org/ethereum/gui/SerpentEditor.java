package org.ethereum.gui;

import org.ethereum.serpent.SerpentCompiler;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.ethereum.config.SystemProperties.CONFIG;

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

    private String defaultCode = "\n" +
            "\n" +
            "init: \n" +
            "\n" +
            "  # [init block] - executed once when contract\n" +
            "  # being initialized.\n" +
            "  contract.storage[999] = 3 \n" +
            "\n" +
            "code:\n" +
            "\n" +
            "  # [code block] - the actual code\n" +
            "  # executed when the call msg\n" +
            "  # hit the peer\n" +
            "  a = contract.storage[999]\n" +
            "  b = msg.data[a]\n";



    private final RSyntaxTextArea codeArea;
    private static final long serialVersionUID = 1L;
    final JSplitPane splitPanel;
    final JTextArea result;
    final JPanel contentPane;
    JFileChooser fileChooser = null;

    ToolBar toolBar = null;

    public SerpentEditor(ToolBar toolBar) {

        this.toolBar = toolBar;
        addCloseAction();
        contentPane = new JPanel(new BorderLayout());
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
        codeArea.setText(defaultCode);

        changeStyleProgrammatically();

        RTextScrollPane sp = new RTextScrollPane(codeArea);

        sp.setFoldIndicatorEnabled(true);
        contentPane.setLayout(new BorderLayout());

        splitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPanel.setOneTouchExpandable(true);
        splitPanel.setDividerSize(5);
        splitPanel.setContinuousLayout(true);



        contentPane.add(splitPanel, BorderLayout.CENTER);
        splitPanel.add(sp);

        result = new JTextArea();
        result.setLineWrap(true);
        result.setWrapStyleWord(true);
        result.setVisible(false);

        splitPanel.add(result);

        JPanel controlsPanel = new JPanel();
        FlowLayout fl = new FlowLayout(FlowLayout.CENTER, 10, 5);
//        fl.setAlignment(FlowLayout.RIGHT);
        controlsPanel.setLayout(fl);
        controlsPanel.setMaximumSize(new Dimension(10000, 20));
        controlsPanel.setPreferredSize(new Dimension(600, 20));
        controlsPanel.setMinimumSize(new Dimension(1, 20));

        contentPane.add(controlsPanel, BorderLayout.SOUTH);


        createToolBar();

        setContentPane(contentPane);
        setTitle("Serpent Editor");


        pack();
        this.revalidate();
        this.repaint();

    }

    private void changeStyleProgrammatically() {

        // Set the font for all token types.

        // Change a few things here and there.
        SyntaxScheme scheme = codeArea.getSyntaxScheme();

        scheme.getStyle(Token.RESERVED_WORD).background = Color.white;
        scheme.getStyle(Token.RESERVED_WORD).foreground = Color.BLUE.darker();

        scheme.getStyle(Token.IDENTIFIER).foreground = Color.black;


        scheme.getStyle(Token.RESERVED_WORD_2).background = Color.WHITE;
        scheme.getStyle(Token.RESERVED_WORD_2).foreground = Color.MAGENTA.darker();


        scheme.getStyle(Token.ANNOTATION).foreground = Color.ORANGE;
        scheme.getStyle(Token.ANNOTATION).background = Color.black;
        scheme.getStyle(Token.ANNOTATION).font = new Font("Consolas", Font.BOLD, 15);


//        scheme.getStyle(Token.LITERAL_STRING_DOUBLE_QUOTE).underline = true;
//        scheme.getStyle(Token.LITERAL_NUMBER_HEXADECIMAL).underline = true;
//        scheme.getStyle(Token.LITERAL_NUMBER_HEXADECIMAL).background = Color.pink;

        scheme.getStyle(Token.COMMENT_EOL).foreground = Color.lightGray;

//        scheme.getStyle(Token.COMMENT_EOL).font = new Font("Georgia", Font.ITALIC, 10);

        codeArea.revalidate();
    }


    protected void compileCode(){

        String code = codeArea.getText();
        String asmResult = "";

        Pattern pattern = Pattern.compile("(.*?)init:(.*?)code:(.*?)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(code);

        try {
            if (matcher.find()) {

                asmResult = SerpentCompiler.compileFullNotion(codeArea.getText());
                asmResult = GUIUtils.getStyledAsmCode(asmResult);
            }else{

                asmResult = SerpentCompiler.compile(codeArea.getText());
            }
        } catch (Throwable th) {
            th.printStackTrace();

            splitPanel.setDividerLocation(0.8);
            result.setVisible(true);
            result.setText(th.getMessage());
            result.setForeground(Color.RED);
            return ;
        }

        result.setForeground(Color.BLACK.brighter());
        result.setVisible(true);
        result.setText(asmResult);

        splitPanel.setDividerLocation(
                1 - result.getPreferredSize().getHeight() / codeArea.getPreferredSize().getHeight());

        this.repaint();
    }

    protected byte[] prepareCodeForSend(){

        String asmResult = "";
        byte[] machineCode = null;

        try {

            String code = codeArea.getText();

            Pattern pattern = Pattern.compile("(.*?)init:(.*?)code:(.*?)", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(code);
            if (matcher.find()) {

                asmResult = SerpentCompiler.compileFullNotion(codeArea.getText());
                machineCode = SerpentCompiler.compileFullNotionAssemblyToMachine(asmResult);

            }else{
                asmResult = SerpentCompiler.compile(codeArea.getText());
                machineCode = SerpentCompiler.compileAssemblyToMachine(asmResult);
            }

        } catch (Throwable th) {
            th.printStackTrace();
            splitPanel.setDividerLocation(0.7);
            result.setVisible(true);
            result.setText(th.getMessage());
            result.setForeground(Color.RED);
            return null;
        }

        return machineCode;
    }


    public void createToolBar(){


        JToolBar toolbar = new JToolBar(SwingConstants.VERTICAL);
        toolbar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        toolbar.setFloatable(false);
        final JPanel mainContentPane = SerpentEditor.this.contentPane;

        {
            java.net.URL url = ClassLoader.getSystemResource("buttons/open-file.png");
            Toolkit kit = Toolkit.getDefaultToolkit();
            Image img = kit.createImage(url);
            ImageIcon imageIcon = new ImageIcon(img);
            final JButton button = new JButton(imageIcon);
            button.setToolTipText("Open File < Ctrl + O >");

            Action openFile = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    button.doClick();
                }
            };

            mainContentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                    put(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK),
                            "OpenFileButton");

            mainContentPane.getActionMap().put("OpenFileButton",openFile);

            button.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {

                           File file = callFileChooser();
                            try {
                                if (file == null) return;
                                String content = new Scanner(file).useDelimiter("\\Z").next();
                                codeArea.setText(content);

                            } catch (FileNotFoundException e1) {

                                e1.printStackTrace();
                            } catch (java.util.NoSuchElementException e2){

                                // don't worry it's just the file is empty
                                codeArea.setText("");
                            }
                        }
                    }
            );

            toolbar.add(button);
        }

        {
            java.net.URL url = ClassLoader.getSystemResource("buttons/save-file.png");
            Toolkit kit = Toolkit.getDefaultToolkit();
            Image img = kit.createImage(url);
            ImageIcon imageIcon = new ImageIcon(img);
            final JButton button = new JButton(imageIcon);
            button.setToolTipText("Save File < Ctrl + S >");

            Action saveFile = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    button.doClick();
                }
            };

            mainContentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                    put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK),
                            "OpenSaveButton");

            mainContentPane.getActionMap().put("OpenSaveButton",saveFile);

            button.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {

                            File file = null;
                            if (fileChooser == null || fileChooser.getSelectedFile() == null) {
                                file = callFileChooser();
                                if (fileChooser.getSelectedFile() == null)
                                    return;
                            }
                            else{
                                    file = fileChooser.getSelectedFile();
                            }

                            try {
                                BufferedWriter out = new BufferedWriter(new FileWriter(file), 32768);
                                out.write(codeArea.getText());
                                out.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
            );

            toolbar.add(button);
        }


        toolbar.addSeparator();

        {

            java.net.URL url = ClassLoader.getSystemResource("buttons/compile.png");
            Toolkit kit = Toolkit.getDefaultToolkit();
            Image img = kit.createImage(url);
            ImageIcon imageIcon = new ImageIcon(img);
            final JButton button = new JButton(imageIcon);
            button.setToolTipText("Compile the contract < Ctrl + F9 >");

            Action compile = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    button.doClick();
                }
            };

            mainContentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                    put(KeyStroke.getKeyStroke(KeyEvent.VK_F9, InputEvent.CTRL_DOWN_MASK),
                            "CompileButton");

            mainContentPane.getActionMap().put("CompileButton",compile);


            button.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            compileCode();
                        }
                    }
            );
            toolbar.add(button);
        }

        {

            java.net.URL url = ClassLoader.getSystemResource("buttons/deploy.png");
            Toolkit kit = Toolkit.getDefaultToolkit();
            Image img = kit.createImage(url);
            ImageIcon imageIcon = new ImageIcon(img);
            final JButton button = new JButton(imageIcon);
            button.setToolTipText("Deploy contract to the chain < Ctrl + Shift + F9 >");

            Action deploy = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    button.doClick();
                }
            };

            mainContentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                    put(KeyStroke.getKeyStroke(KeyEvent.VK_F9,
                                    InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK),
                            "DeployButton");

            mainContentPane.getActionMap().put("DeployButton",deploy);


            button.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {

                    byte[] machineCode = prepareCodeForSend();
                    if (machineCode == null) return;

                    ContractSubmitDialog payOutDialog =
                            new ContractSubmitDialog((Frame) SwingUtilities.getAncestorOfClass(JFrame.class,
                                    contentPane), machineCode);
                }
            });
            toolbar.add(button);
        }

        {

            java.net.URL url = ClassLoader.getSystemResource("buttons/call.png");
            Toolkit kit = Toolkit.getDefaultToolkit();
            Image img = kit.createImage(url);
            ImageIcon imageIcon = new ImageIcon(img);
            final JButton button = new JButton(imageIcon);
            button.setToolTipText("Call contract <Ctrl + F8>");

            Action call = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    button.doClick();
                }
            };

            mainContentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                    put(KeyStroke.getKeyStroke(KeyEvent.VK_F8,
                                    InputEvent.CTRL_DOWN_MASK),
                            "CallButton");

            mainContentPane.getActionMap().put("CallButton", call);


            button.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    ContractCallDialog payOutDialog =
                            new ContractCallDialog((Frame)SwingUtilities.getAncestorOfClass(JFrame.class,
                                    contentPane));

                }
            });

            toolbar.add(button);
        }


        this.contentPane.add(toolbar, BorderLayout.EAST);
    }


    protected File callFileChooser(){

        File file = null;

        if (fileChooser == null) {
            fileChooser = new JFileChooser(CONFIG.samplesDir());
            fileChooser.setMultiSelectionEnabled(false);
        }

        switch (fileChooser.showOpenDialog(SerpentEditor.this))
        {
            case JFileChooser.APPROVE_OPTION:

                    file = fileChooser.getSelectedFile();
                break;
        }

        return file;
    }

    public void addCloseAction(){
        this.addWindowListener( new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                toolBar.editorToggle.setSelected(false);

            }
        });
    }


    public static void main(String[] args) {
        // Start all Swing applications on the EDT.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new SerpentEditor(null).setVisible(true);
            }
        });
    }


}