package org.ethereum.gui;

import org.ethereum.vm.DataWord;
import org.ethereum.vm.Program;
import org.ethereum.vm.ProgramInvoke;
import org.ethereum.vm.VM;
import org.spongycastle.util.encoders.Hex;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.util.*;
import java.util.List;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 02/06/2014 16:58
 */

public class ProgramPlayDialog extends JPanel implements ActionListener, ChangeListener, Program.ProgramListener {

    public List<String> outputList;
    public JTextArea console;
    public JSlider stepSlider;

    public ProgramPlayDialog() {

        outputList = new ArrayList<String>();
        VM vm = new VM();
//        Program program = new Program(Hex.decode("630000000060445960CC60DD611234600054615566602054630000000060445960CC60DD611234600054615566602054630000000060445960CC60DD611234600054615566602054"));
        Program program = new Program(Hex.decode("6000605f556014600054601e60205463abcddcba6040545b51602001600a5254516040016014525451606001601e5254516080016028525460a052546016604860003960166000f26000603f556103e75660005460005360200235602054"), null);

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

        JScrollPane scrollPane = new JScrollPane(console);

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
    private static void createAndShowGUI() {



        ProgramPlayDialog ppd = new ProgramPlayDialog();

        //Create and set up the window.
        JFrame frame = new JFrame("SliderDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setPreferredSize(new Dimension(600, 500));
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
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });

    }
}
