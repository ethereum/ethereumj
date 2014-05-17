package org.ethereum.gui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 30/04/14 06:29
 */
public class ToolBar extends JFrame {

    public ToolBar() throws HeadlessException {
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
        editorToggle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        new SerpentEditor().setVisible(true);
                    }
                });
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
        logToggle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        new ConnectionConsoleWindow().setVisible(true);
                    }
                });
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
        peersToggle.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PeersTableWindow mainFrame	= new PeersTableWindow();
                mainFrame.setVisible( true );
//                mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        chainToggle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BlockChainTable mainFrame = new BlockChainTable();
                mainFrame.setVisible(true);
//                mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        walletToggle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WalletWindow walletWindow = new WalletWindow();
                walletWindow.setVisible(true);
            }
        });

        cp.add(editorToggle);
        cp.add(logToggle);
        cp.add(peersToggle);
        cp.add(chainToggle);
        cp.add(walletToggle);
    }

    public static void main(String args[]){

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ToolBar().setVisible(true);
            }
        });
    }

}
