package org.ethereum.gui;

import org.ethereum.util.Utils;

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
 * @author: Roman Mandeleil
 * Created on: 17/05/14 12:32
 */
public class WalletSumPanel extends JPanel{

    public WalletSumPanel(BigInteger balance) {

        this.setBackground(Color.WHITE);
        double width = this.getSize().getWidth();
        this.setPreferredSize(new Dimension(500, 50));
        Border line = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        Border empty = new EmptyBorder(5, 8, 5, 8);
        CompoundBorder border = new CompoundBorder(line, empty);

        JLabel addressField = new JLabel();
        addressField.setPreferredSize(new Dimension(300, 35));
        this.add(addressField);

        JTextField amount = new JTextField();
        amount.setBorder(border);
        amount.setEnabled(true);
        amount.setEditable(false);
        amount.setText(Utils.getValueShortString(balance));

        amount.setPreferredSize(new Dimension(100, 35));
        amount.setForeground(new Color(143, 170, 220));
        amount.setHorizontalAlignment(SwingConstants.RIGHT);
        amount.setFont(new Font("Monospaced", 0, 13));
        amount.setBackground(Color.WHITE);
        this.add(amount);

        URL payoutIconURL = ClassLoader.getSystemResource("buttons/wallet-pay.png");
        ImageIcon payOutIcon = new ImageIcon(payoutIconURL);
        JLabel payOutLabel = new JLabel(payOutIcon);
        payOutLabel.setToolTipText("Payout for all address list");
        payOutLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        payOutLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(null, "Under construction");
            }
        });

        this.add(payOutLabel);
    }
}
