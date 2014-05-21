package org.ethereum.gui;

import org.ethereum.core.Address;
import org.ethereum.core.Transaction;
import org.ethereum.crypto.HashUtil;
import org.ethereum.manager.MainData;
import org.ethereum.net.client.ClientPeer;
import org.ethereum.wallet.AddressState;
import org.spongycastle.util.BigIntegers;
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

    AddressState addressState = null;

    public PayOutDialog(Frame parent, final AddressState addressState) {
        super(parent, "Payout details: ", false);

        this.addressState = addressState;

        JLabel receiver = new JLabel("receiver: ");
        final JTextField receiverInput = new JTextField(18);
        receiverInput.setHorizontalAlignment(SwingConstants.RIGHT);

        final JLabel amount = new JLabel("amount: ");
        final JTextField amountInput = new JTextField(18);
        amountInput.setHorizontalAlignment(SwingConstants.RIGHT);
        amountInput.setText(addressState.getBalance().toString());

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

                        BigInteger value = new BigInteger(amountInput.getText());
                        byte[] address = Hex.decode(receiverInput.getText());

                        byte[] senderPrivKey = HashUtil.sha3("cow".getBytes());

                        byte[] nonce =    addressState.getNonce() == BigInteger.ZERO ?
                                                     null : addressState.getNonce().toByteArray();
                        byte[] gasPrice=  Hex.decode("09184e72a000");
                        byte[] gas =      Hex.decode("4255");

                        Transaction tx = new Transaction(nonce, gasPrice, gas,
                                address, BigIntegers.asUnsignedByteArray(value), null);

                        try {
                            tx.sign(senderPrivKey);
                        } catch (Exception e1) {

                            // todo something if sign fails
                            e1.printStackTrace();
                        }

                        peer.sendTransaction(tx);
                        addressState.incrementTheNonce();
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

