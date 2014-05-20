package org.ethereum.gui;

import org.ethereum.core.Address;
import org.ethereum.core.Transaction;
import org.ethereum.crypto.HashUtil;
import org.ethereum.manager.MainData;
import org.ethereum.net.client.ClientPeer;
import org.spongycastle.util.encoders.Hex;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;

import javax.swing.*;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 18/05/14 22:21
 */
class PayOutDialog extends JDialog {

    public PayOutDialog(Frame parent, BigInteger maxAmount) {
        super(parent, "Payout details: ", false);

        JLabel receiver = new JLabel("receiver: ");
        JTextField receiverInput = new JTextField(18);
        receiverInput.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel amount = new JLabel("amount: ");
        JTextField amountInput = new JTextField(18);
        amountInput.setHorizontalAlignment(SwingConstants.RIGHT);
        amountInput.setText(maxAmount.toString());

        this.getContentPane().setBackground(Color.WHITE);
        this.getContentPane().setLayout(new GridLayout(0, 1, 0, 0));

        JPanel row1 = new JPanel();
        row1.setBackground(Color.WHITE);
        row1.add(receiver);
        row1.add(receiverInput);
        this.getContentPane().add(row1);

        JPanel row2 = new JPanel();
        row2.setBackground(Color.WHITE);
        row2.add(amount);
        row2.add(amountInput);
        this.getContentPane().add(row2);

        JPanel row3 = new JPanel();
        row3.setBackground(Color.WHITE);

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

//                        Client
                       ClientPeer peer = MainData.instance.getActivePeer();

                        BigInteger value = new BigInteger("1000000000000000000000000");

                        byte[] privKey = HashUtil.sha3("cat".getBytes());
                        Address receiveAddress = new Address(privKey);

                        byte[] senderPrivKey = HashUtil.sha3("cow".getBytes());

                        byte[] gasPrice=  Hex.decode("09184e72a000");
                        byte[] gas =      Hex.decode("4255");

                        Transaction tx = new Transaction(null, value.toByteArray(),
                                receiveAddress.getAddress(),  gasPrice, gas, null);

                        try {
                            tx.sign(senderPrivKey);
                        } catch (Exception e1) {

                            // todo something if sign fails
                            e1.printStackTrace();
                        }

                        peer.sendTransaction(tx);
                    }
                }
        );

        row3.add(sendButton);

        row3.add(new JButton("Cancel"));
        row3.setAlignmentY(Component.TOP_ALIGNMENT);
        this.getContentPane().add(row3);
    }

    protected JRootPane createRootPane() {

        Container parent = this.getParent();

        if (parent != null) {
            Dimension parentSize = parent.getSize();
            Point p = parent.getLocation();
            setLocation(p.x + parentSize.width / 4, p.y + parentSize.height / 4);
        }

        JRootPane rootPane = new JRootPane();
        KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");
        Action actionListener = new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                setVisible(false);
            }
        };
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(stroke, "ESCAPE");
        rootPane.getActionMap().put("ESCAPE", actionListener);

        this.setSize(350, 140);
        this.setVisible(true);

        return rootPane;
    }
}

