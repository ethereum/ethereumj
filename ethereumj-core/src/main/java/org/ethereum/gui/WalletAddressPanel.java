package org.ethereum.gui;

import org.ethereum.core.Address;
import org.ethereum.util.Utils;
import org.spongycastle.util.encoders.Hex;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigInteger;
import java.net.URL;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 17/05/14 12:32
 */
public class WalletAddressPanel extends JPanel{

    public WalletAddressPanel(Address address, BigInteger balance) {

        this.setBackground(Color.WHITE);
        this.setPreferredSize(new Dimension(500, 45));

        JTextField addressField = new JTextField();
        Border line = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        Border empty = new EmptyBorder(5, 8, 5, 8);
        CompoundBorder border = new CompoundBorder(line, empty);
        addressField.setBorder(border);
        addressField.setEnabled(true);
        addressField.setEditable(false);
        addressField.setText(Hex.toHexString(address.getPubKey()).toUpperCase());
        addressField.setForeground(new Color(143, 170, 220));
        addressField.setFont(new Font("Monospaced", 0, 12));
        addressField.setPreferredSize(new Dimension(300, 35));
        addressField.setBackground(Color.WHITE);
        this.add(addressField);

        JTextField amount = new JTextField();
        amount.setBorder(border);
        amount.setEnabled(true);
        amount.setEditable(false);
        amount.setText(Utils.getValueShortString(balance));
        amount.setForeground(new Color(143, 170, 220));
        amount.setBackground(Color.WHITE);
        amount.setPreferredSize(new Dimension(100, 35));
        amount.setFont(new Font("Monospaced", 0, 13));
        amount.setHorizontalAlignment(SwingConstants.RIGHT);
        this.add(amount);

        URL payoutIconURL = ClassLoader.getSystemResource("buttons/wallet-pay.png");
        ImageIcon payOutIcon = new ImageIcon(payoutIconURL);
        JLabel payOutLabel = new JLabel(payOutIcon);
        payOutLabel.setToolTipText("Payout for address");
        payOutLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        payOutLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("boom");
            }
        });


        this.add(payOutLabel);
    }
}
