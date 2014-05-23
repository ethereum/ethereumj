package org.ethereum.gui;

import org.ethereum.manager.MainData;
import org.ethereum.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 30/04/14 06:29
 */
public class ToolBar extends JFrame {

    Logger logger = LoggerFactory.getLogger(getClass());
    Logger introLogger = LoggerFactory.getLogger("Intro");

    ConnectionConsoleWindow connectionConsoleWindow = null;
    PeersTableWindow mainFrame	= null;
    BlockChainTable blockChainWindow = null;
    WalletWindow walletWindow = null;
    SerpentEditor serpentEditor = null;


    public ToolBar() throws HeadlessException {

        introLogger.info("");
        introLogger.info("♢ EthereumJ [v0.5.1] ");
        introLogger.info("♢ Code by Roman Mandeleil, (c) 2014.");
        introLogger.info("♢ Contribution: Nick Savers ");
        introLogger.info("♢ Based on a design by Vitaly Buterin.");
        introLogger.info("");
        introLogger.info("java.version: " + System.getProperty("java.version"));
        introLogger.info("java.home:    " + System.getProperty("java.home"));
        introLogger.info("java.vendor:  " + System.getProperty("java.vendor"));
        introLogger.info("");

        if (Utils.JAVA_VERSION < 1.7) {
            introLogger.info("EthereumJ support version 1.7 and higher of Java Runtime please update");
            System.exit(0);
        }




        final JPanel cp = new JPanel(new FlowLayout());
        cp.setBackground(Color.WHITE);

        java.net.URL url = ClassLoader.getSystemResource("ethereum-icon.png");
        Toolkit kit = Toolkit.getDefaultToolkit();
        Image img = kit.createImage(url);
        this.setIconImage(img);
        this.setSize(350, 200);
        this.setLocation(460, 25);
        this.setAlwaysOnTop(true);
        this.setResizable(false);
        this.setBackground(Color.WHITE);

        setTitle("EthereumJ Studio");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.setContentPane(cp);

        java.net.URL imageURL_1 = ClassLoader.getSystemResource("buttons/feedly.png");
        ImageIcon image_1 = new ImageIcon(imageURL_1);

        java.net.URL imageURL_2 = ClassLoader.getSystemResource("buttons/winamp.png");
        ImageIcon image_2 = new ImageIcon(imageURL_2);

        java.net.URL imageURL_3 = ClassLoader.getSystemResource("buttons/browser.png");
        ImageIcon image_3 = new ImageIcon(imageURL_3);

        java.net.URL imageURL_4 = ClassLoader.getSystemResource("buttons/shazam.png");
        ImageIcon image_4 = new ImageIcon(imageURL_4);

        java.net.URL imageURL_5 = ClassLoader.getSystemResource("buttons/wallet.png");
        ImageIcon image_5 = new ImageIcon(imageURL_5);


        JToggleButton editorToggle = new JToggleButton("");
        editorToggle.setIcon(image_1);
        editorToggle.setContentAreaFilled(true);
        editorToggle.setToolTipText("Serpent Editor");
        editorToggle.setBackground(Color.WHITE);
        editorToggle.setBorderPainted(false);
        editorToggle.setFocusPainted(false);
        editorToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editorToggle.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED){
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (serpentEditor == null)
                                serpentEditor = new SerpentEditor();
                            serpentEditor.setVisible(true);
                        }
                    });
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    serpentEditor.setVisible(false);
                }
            }
        });

        JToggleButton logToggle = new JToggleButton();
        logToggle.setIcon(image_2);
        logToggle.setToolTipText("Log Console");
        logToggle.setContentAreaFilled(true);
        logToggle.setBackground(Color.WHITE);
        logToggle.setBorderPainted(false);
        logToggle.setFocusPainted(false);
        logToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logToggle.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {

                if (e.getStateChange() == ItemEvent.SELECTED){
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                        if (connectionConsoleWindow == null)
                            connectionConsoleWindow =  new ConnectionConsoleWindow();
                        connectionConsoleWindow.setVisible(true);
                        }
                    });
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                        connectionConsoleWindow.setVisible(false);
                }
            }
        });

        JToggleButton peersToggle = new JToggleButton();
        peersToggle.setIcon(image_3);
        peersToggle.setToolTipText("Peers");
        peersToggle.setContentAreaFilled(true);
        peersToggle.setBackground(Color.WHITE);
        peersToggle.setBorderPainted(false);
        peersToggle.setFocusPainted(false);
        peersToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        peersToggle.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED){
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (mainFrame == null)
                                mainFrame	= new PeersTableWindow();
                            mainFrame.setVisible( true );
                        }
                    });
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    mainFrame.setVisible( false );
                }
            }
        });


        JToggleButton chainToggle = new JToggleButton();
        chainToggle.setIcon(image_4);
        chainToggle.setToolTipText("Block Chain");
        chainToggle.setContentAreaFilled(true);
        chainToggle.setBackground(Color.WHITE);
        chainToggle.setBorderPainted(false);
        chainToggle.setFocusPainted(false);
        chainToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chainToggle.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED){
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {

                            if (blockChainWindow == null)
                                blockChainWindow = new BlockChainTable();
                            blockChainWindow.setVisible(true);
                        }
                    });
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    blockChainWindow.setVisible(false);
                }
            }
        });

        JToggleButton walletToggle = new JToggleButton();
        walletToggle.setIcon(image_5);
        walletToggle.setToolTipText("Wallet");
        walletToggle.setContentAreaFilled(true);
        walletToggle.setBackground(Color.WHITE);
        walletToggle.setBorderPainted(false);
        walletToggle.setFocusPainted(false);
        walletToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        walletToggle.addItemListener(
                new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        if (e.getStateChange() == ItemEvent.SELECTED){
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    if (walletWindow == null)
                                        walletWindow = new WalletWindow();
                                    walletWindow.setVisible(true);
                                }
                            });
                        } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                            walletWindow.setVisible(false);
                        }

                    }
                }
        );

        cp.add(editorToggle);
        cp.add(logToggle);
        cp.add(peersToggle);
        cp.add(chainToggle);
        cp.add(walletToggle);


        MainData.instance.toString();
    }

    public static void main(String args[]){

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ToolBar().setVisible(true);
            }
        });
    }

}
