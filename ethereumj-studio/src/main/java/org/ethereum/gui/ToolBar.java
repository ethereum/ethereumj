package org.ethereum.gui;

import org.ethereum.config.SystemProperties;
import org.ethereum.facade.Ethereum;
import org.ethereum.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author Roman Mandeleil
 * @since 30.04.14
 */
public class ToolBar extends JFrame {

    private Logger introLogger = LoggerFactory.getLogger("Intro");

    private ConnectionConsoleWindow connectionConsoleWindow = null;
    private PeersTableWindow mainFrame = null;
    private BlockChainTable blockchainWindow = null;
    private WalletWindow walletWindow = null;
    private SerpentEditor serpentEditor = null;
    private StateExplorerWindow stateExplorerWindow = null;

    JToggleButton editorToggle;
    JToggleButton logToggle;
    JToggleButton peersToggle;
    JToggleButton chainToggle;
    JToggleButton walletToggle;
    JToggleButton stateExplorer;

    public ToolBar() throws HeadlessException {

        String version = SystemProperties.CONFIG.projectVersion();

        introLogger.info("");
        introLogger.info("|Ξ|  EthereumJ [v" + version + "]");
        introLogger.info("|Ξ|  Code by Roman Mandeleil, (c) 2014.");
        introLogger.info("|Ξ|  Contribution: Nick Savers ");
        introLogger.info("|Ξ|  Based on a design by Vitalik Buterin.");
        introLogger.info("");
        introLogger.info("java.version: " + System.getProperty("java.version"));
        introLogger.info("java.home:    " + System.getProperty("java.home"));
        introLogger.info("java.vendor:  " + System.getProperty("java.vendor"));
        introLogger.info("");

        if (Utils.JAVA_VERSION < 1.7 && Utils.JAVA_VERSION != 0) {
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

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                UIEthereumManager.ethereum.close();
            }
        });

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

        java.net.URL imageURL_6 = ClassLoader.getSystemResource("buttons/stateExplorer.png");
        ImageIcon image_6 = new ImageIcon(imageURL_6);

        editorToggle = new JToggleButton("");
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
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (serpentEditor == null)
                                serpentEditor = new SerpentEditor(ToolBar.this);
                            serpentEditor.setVisible(true);
                        }
                    });
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    serpentEditor.setVisible(false);
                }
            }
        });

        logToggle = new JToggleButton();
        logToggle.setIcon(image_2);
        logToggle.setToolTipText("Connect");
        logToggle.setContentAreaFilled(true);
        logToggle.setBackground(Color.WHITE);
        logToggle.setBorderPainted(false);
        logToggle.setFocusPainted(false);
        logToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logToggle.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (connectionConsoleWindow == null)
                                connectionConsoleWindow = new ConnectionConsoleWindow(ToolBar.this);
                            connectionConsoleWindow.setVisible(true);
                        }
                    });
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    connectionConsoleWindow.setVisible(false);
                }
            }
        });

        peersToggle = new JToggleButton();
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
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (mainFrame == null)
                                mainFrame = new PeersTableWindow(ToolBar.this);
                            mainFrame.setVisible(true);
                        }
                    });
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    mainFrame.setVisible(false);
                }
            }
        });

        chainToggle = new JToggleButton();
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
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {

                            if (blockchainWindow == null)
                                blockchainWindow = new BlockChainTable(ToolBar.this);
                            blockchainWindow.setVisible(true);
                        }
                    });
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    blockchainWindow.setVisible(false);
                }
            }
        });

        walletToggle = new JToggleButton();
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
                        if (e.getStateChange() == ItemEvent.SELECTED) {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    if (walletWindow == null)
                                        walletWindow = new WalletWindow(ToolBar.this);
                                    walletWindow.setVisible(true);
                                }
                            });
                        } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                            walletWindow.setVisible(false);
                        }
                    }
                }
        );

        stateExplorer = new JToggleButton();
        stateExplorer.setIcon(image_6);
        stateExplorer.setToolTipText("State Explorer");
        stateExplorer.setContentAreaFilled(true);
        stateExplorer.setBackground(Color.WHITE);
        stateExplorer.setBorderPainted(false);
        stateExplorer.setFocusPainted(false);
        stateExplorer.setCursor(new Cursor(Cursor.HAND_CURSOR));
        stateExplorer.addItemListener(
                new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        if (e.getStateChange() == ItemEvent.SELECTED) {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    if (stateExplorerWindow == null)
                                        stateExplorerWindow = new StateExplorerWindow(ToolBar.this);
                                    stateExplorerWindow.setVisible(true);
                                }
                            });
                        } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                            stateExplorerWindow.setVisible(false);
                        }
                    }
                }
        );


        cp.add(editorToggle);
        cp.add(logToggle);
        cp.add(peersToggle);
        cp.add(chainToggle);
        cp.add(walletToggle);
        cp.add(stateExplorer);

        Ethereum ethereum = UIEthereumManager.ethereum;

    }

    public static void main(String args[]) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ToolBar().setVisible(true);
            }
        });
    }
}
