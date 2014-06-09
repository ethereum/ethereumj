package org.ethereum.gui;

import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.vm.Program;
import org.ethereum.vm.ProgramInvokeFactory;
import org.ethereum.vm.ProgramInvokeImpl;
import org.ethereum.vm.VM;
import org.spongycastle.util.encoders.Hex;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 02/06/2014 16:58
 */

public class ProgramPlayDialog extends JPanel implements ActionListener,
		ChangeListener, Program.ProgramListener {

    public List<String> outputList;
    public JTextArea console;
    public JSlider stepSlider;

    private Transaction tx;

    public ProgramPlayDialog(byte[] code, Transaction tx, Block lastBlock) {

        this.tx = tx;

        outputList = new ArrayList<String>();
        VM vm = new VM();
//        Program program = new Program(Hex.decode("630000000060445960CC60DD611234600054615566602054630000000060445960CC60DD611234600054615566602054630000000060445960CC60DD611234600054615566602054"));
//        Program program = new Program(Hex.decode("60016023576000605f556014600054601e60205463abcddcba6040545b51602001600a5254516040016014525451606001601e5254516080016028525460a052546016604860003960166000f26000603f556103e75660005460005360200235602054"), null);

//        String code = "60016000546006601160003960066000f261778e600054";
//        String code = "620f424073cd2a3d9f938e13cd947ec05abc7fe734df8dd826576086602660003960866000f26001602036040e0f630000002159600060200235600054600053565b525b54602052f263000000765833602054602053566040546000602002356060546001602002356080546080536040530a0f0f630000006c59608053604053036020535760805360605356016060535760015b525b54602052f263000000765860005b525b54602052f2";

//        byte[] codeBytes =
//            Hex.decode(code);

        Program program = new Program(code ,
                ProgramInvokeFactory.createProgramInvoke(tx, lastBlock));

        program.addListener(this);
        program.fullTrace();

        while(!program.isStopped())
            vm.step(program);

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        //Create the slider.
        stepSlider = new JSlider(JSlider.HORIZONTAL,
                0, outputList.size() - 1, 0);


        stepSlider.addChangeListener(this);

        //Turn on labels at major tick marks.

        stepSlider.setMajorTickSpacing(1);
        if (outputList.size() > 40)
            stepSlider.setMajorTickSpacing(3);

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

    public void setFocus(){
        stepSlider.requestFocus();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        int step = (int)source.getValue();

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
    public static void createAndShowGUI(byte[] runCode, Transaction tx, Block lastBlock) {

        ProgramPlayDialog ppd = new ProgramPlayDialog(runCode, tx, lastBlock);

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

        //Display the window.
        frame.pack();
        frame.setVisible(true);
        ppd.setFocus();
    }

    @Override
    public void output(String out) {
        outputList.add(out);
    }

    public static void main(String []args){

        /* Turn off metal's use of bold fonts */
        UIManager.put("swing.boldMetal", Boolean.FALSE);

        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.

/*  todo: make dummy tx for dialog single invokation
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
*/

    }
}
