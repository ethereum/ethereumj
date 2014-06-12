package org.ethereum.gui;

import org.ethereum.core.Block;
import org.ethereum.core.ContractDetails;
import org.ethereum.core.Transaction;
import org.ethereum.db.TrackDatabase;
import org.ethereum.manager.WorldManager;
import org.ethereum.trie.TrackTrie;
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

    public ProgramPlayDialog(byte[] code, Transaction tx, Block lastBlock, ContractDetails contractDetails) {

        this.tx = tx;

        outputList = new ArrayList<String>();
        VM vm = new VM();

        TrackDatabase trackDetailDB = new TrackDatabase( WorldManager.instance.detaildDB );
        TrackDatabase trackChainDb  = new TrackDatabase( WorldManager.instance.chainDB);
        TrackTrie trackStateDB  = new TrackTrie(WorldManager.instance.worldState );

        Program program = new Program(code ,
                ProgramInvokeFactory.createProgramInvoke(tx, lastBlock, contractDetails,
                        trackDetailDB, trackChainDb, trackStateDB));

        trackDetailDB.rollbackTrack();
        trackChainDb.rollbackTrack();
        trackStateDB.rollbackTrack();

        program.addListener(this);
        program.fullTrace();
        vm.play(program);

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
    public static void createAndShowGUI(byte[] runCode, Transaction tx, Block lastBlock, ContractDetails details) {

        ProgramPlayDialog ppd = new ProgramPlayDialog(runCode, tx, lastBlock, details);

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
