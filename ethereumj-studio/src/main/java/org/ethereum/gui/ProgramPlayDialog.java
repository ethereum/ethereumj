package org.ethereum.gui;

import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.vm.*;
import org.spongycastle.util.encoders.Hex;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Roman Mandeleil
 * @since 02.06.2014
 */
public class ProgramPlayDialog extends JPanel implements ActionListener,
        ChangeListener, Program.ProgramListener {

    private List<String> outputList;
    private JTextArea console;
    private JSlider stepSlider;

    private ProgramInvoke pi;

    public ProgramPlayDialog(byte[] code) {
        this(code, new ProgramInvokeMockImpl());
    }

    public ProgramPlayDialog(byte[] code, Transaction tx, Block lastBlock) {
//      this(code, ProgramInvokeFactory.createProgramInvoke(tx, lastBlock,
//                UIEthereumManager.ethereum.getRepository()));
    }

    public ProgramPlayDialog(byte[] code, ProgramInvoke programInvoke) {
        pi = programInvoke;

        outputList = new ArrayList<String>();
        VM vm = new VM();

        Program program = new Program(code, programInvoke);
        program.addListener(this);
        program.fullTrace();
        vm.play(program);

        if (programInvoke.getRepository() != null)
            programInvoke.getRepository().rollback();

        doGUI();
    }

    public void doGUI() {

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        //Create the slider.
        stepSlider = new JSlider(JSlider.HORIZONTAL,
                0, outputList.size() - 1, 0);

        stepSlider.addChangeListener(this);

        //Turn on labels at major tick marks.

        stepSlider.setMajorTickSpacing(1);
        if (outputList.size() > 40)
            stepSlider.setMajorTickSpacing(3);
        if (outputList.size() > 100)
            stepSlider.setMajorTickSpacing(20);

        stepSlider.setMinorTickSpacing(1);
        stepSlider.setPaintTicks(true);
        stepSlider.setPaintLabels(true);
        stepSlider.setBorder(
                BorderFactory.createEmptyBorder(0, 0, 10, 0));
        Font font = new Font("Courier New", Font.PLAIN, 10);
        stepSlider.setFont(font);
        stepSlider.addChangeListener(this);

        //Create the label that displays the animation.

        int i = stepSlider.getValue();
        console = new JTextArea(outputList.get(i));
        console.setFont(new Font("Courier New", Font.PLAIN, 13));
        console.setForeground(new Color(183, 209, 253));
        console.setBackground(Color.BLACK);
        console.setLineWrap(true);

        stepSlider.setFocusable(true);

        JScrollPane scrollPane = new JScrollPane(console,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));

        add(scrollPane);
        add(stepSlider);
    }

    public void setFocus() {
        stepSlider.requestFocus();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
        int step = (int) source.getValue();

        int i = source.getValue();
        String out = outputList.get(i);

        console.setText(out);
        console.setCaretPosition(0);
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    public static void createAndShowGUI(byte[] runCode, final Transaction tx, Block lastBlock) {

        final ProgramPlayDialog ppd;
        if (tx != null)
            ppd = new ProgramPlayDialog(runCode, tx, lastBlock);
        else {
            ppd = new ProgramPlayDialog(runCode);
        }

        //Create and set up the window.
        JFrame frame = new JFrame("Program Draft Play");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        java.net.URL url = ClassLoader.getSystemResource("ethereum-icon.png");
        Toolkit kit = Toolkit.getDefaultToolkit();
        Image img = kit.createImage(url);
        frame.setIconImage(img);

        frame.setPreferredSize(new Dimension(580, 500));
        frame.setLocation(400, 200);

        //Add content to the window.
        frame.add(ppd, BorderLayout.CENTER);

        // close event
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (tx == null) {
                    ppd.pi.getRepository().close();
                    ppd.pi = null;
                }
            }
        });

        //Display the window.
        frame.pack();
        frame.setVisible(true);
        ppd.setFocus();
    }


    @Override
    public void output(String out) {
        outputList.add(out);
    }

    public static void main(String[] args) {

        /* Turn off metal's use of bold fonts */
        UIManager.put("swing.boldMetal", Boolean.FALSE);

        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.

        String asmCode = "11 0 MSTORE 22 32 MSTORE 33 64 MSTORE 44 96 MSTORE 55 128 MSTORE 66 160 MSTORE 192 0 RETURN";
//        final byte[] code = SerpentCompiler.compileAssemblyToMachine(asmCode);

        final byte[] code = Hex.decode("7f4e616d65526567000000000000000000000000000000000000000000000000003057307f4e616d6552656700000000000000000000000000000000000000000000000000573360455760415160566000396000f20036602259604556330e0f600f5933ff33560f601e5960003356576000335700604158600035560f602b590033560f60365960003356573360003557600035335700");
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(code, null, null);
            }
        });
    }
}
