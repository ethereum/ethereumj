package org.ethereum.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 17/05/14 12:00
 */
public class WalletWindow extends JFrame {


    public WalletWindow() {

        java.net.URL url = ClassLoader.getSystemResource("ethereum-icon.png");
        Toolkit kit = Toolkit.getDefaultToolkit();
        Image img = kit.createImage(url);
        this.setIconImage(img);
        setTitle("Ethereum Wallet");
        setSize(490, 370);
        setLocation(215, 280);
        setBackground(Color.WHITE);
        setResizable(false);

        Container contentPane = this.getContentPane();
        contentPane.setLayout(new FlowLayout());
        contentPane.setBackground(Color.WHITE);

        WalletAddressPanel panel1 = new WalletAddressPanel();
        WalletAddressPanel panel2 = new WalletAddressPanel();
        WalletAddressPanel panel3 = new WalletAddressPanel();
        WalletAddressPanel panel4 = new WalletAddressPanel();
        WalletSumPanel panel5 = new WalletSumPanel();

        contentPane.add(panel1);
        contentPane.add(panel2);
        contentPane.add(panel3);
        contentPane.add(panel4);
        contentPane.add(panel5);

        URL addAddressIconURL = ClassLoader.getSystemResource("buttons/add-address.png");
        ImageIcon addAddressIcon = new ImageIcon(addAddressIconURL);
        JLabel addAddressLabel = new JLabel(addAddressIcon);
        addAddressLabel.setToolTipText("Add new address");
        addAddressLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addAddressLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("boom");
            }
        });

        contentPane.add(addAddressLabel);

    }
}
