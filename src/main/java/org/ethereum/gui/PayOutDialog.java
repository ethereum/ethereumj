package org.ethereum.gui;

import org.ethereum.core.Transaction;
import org.ethereum.manager.MainData;
import org.ethereum.net.client.ClientPeer;
import org.ethereum.wallet.AddressState;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigInteger;
import java.net.URL;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 18/05/14 22:21
 */
class PayOutDialog extends JDialog {

    PayOutDialog dialog;

    AddressState addressState = null;
    JLabel statusMsg = null;

    public PayOutDialog(Frame parent, final AddressState addressState) {
        super(parent, "Payout details: ", false);
        dialog = this;

        this.addressState = addressState;

        final JTextField receiverInput = new JTextField(18);
        GUIUtils.addStyle(receiverInput, "Pay to:");

        final JTextField amountInput = new JTextField(18);
        GUIUtils.addStyle(amountInput, "Amount: ");

        final JTextField feeInput = new JTextField(5);
        GUIUtils.addStyle(feeInput, "Fee: ");

        this.getContentPane().setBackground(Color.WHITE);
        this.getContentPane().setLayout(null);

        receiverInput.setBounds(70, 30, 350, 45);
        this.getContentPane().add(receiverInput);

        amountInput.setBounds(70, 80, 250, 45);
        this.getContentPane().add(amountInput);

        feeInput.setBounds(330, 80, 90, 45);
        this.getContentPane().add(feeInput);

        URL rejectIconURL = ClassLoader.getSystemResource("buttons/reject.png");
        ImageIcon rejectIcon = new ImageIcon(rejectIconURL);
        JLabel rejectLabel = new JLabel(rejectIcon);
        rejectLabel.setToolTipText("Cancel");
        rejectLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel statusMessage = new JLabel("");
        statusMessage.setBounds(50, 180, 400, 50);
        statusMessage.setHorizontalAlignment(SwingConstants.CENTER);
        this.statusMsg = statusMessage;
        this.getContentPane().add(statusMessage);

        rejectLabel.setBounds(260, 145, 45, 45);
        this.getContentPane().add(rejectLabel);
        rejectLabel.setVisible(true);
        rejectLabel.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {

                        dialog.dispose();
                    }}
        );


        URL approveIconURL = ClassLoader.getSystemResource("buttons/approve.png");
        ImageIcon approveIcon = new ImageIcon(approveIconURL);
        JLabel approveLabel = new JLabel(approveIcon);
        approveLabel.setToolTipText("Submit the transaction");
        approveLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        approveLabel.setBounds(200, 145, 45, 45);
        this.getContentPane().add(approveLabel);
        approveLabel.setVisible(true);


        approveLabel.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {


                        BigInteger fee = new BigInteger(feeInput.getText());
                        BigInteger value = new BigInteger(amountInput.getText());
                        byte[] address = Hex.decode( receiverInput.getText());


//                        Client
                        ClientPeer peer = MainData.instance.getActivePeer();

                        if (peer == null){
                            dialog.alertStatusMsg("Not connected to any peer");
                            return;
                        }

                        byte[] senderPrivKey = addressState.getEcKey().getPrivKeyBytes();

                        byte[] nonce = addressState.getNonce() == BigInteger.ZERO ?
                                null : addressState.getNonce().toByteArray();

                        // todo: in the future it should be retrieved from the block
                        byte[] gasPrice = new BigInteger("10000000000000").toByteArray();

                        Transaction tx = new Transaction(nonce, gasPrice,
                                BigIntegers.asUnsignedByteArray(fee),
                                address,
                                BigIntegers.asUnsignedByteArray(value), null);

                        try {
                            tx.sign(senderPrivKey);
                        } catch (Exception e1) {

                            // todo something if sign fails
                            e1.printStackTrace();
                        }

                        peer.sendTransaction(tx);
                        dialog.infoStatusMsg("Transaction sent to the network, waiting for approve");
                    }
                }
        );


        feeInput.setText("1000");
        amountInput.setText("0");

        this.getContentPane().revalidate();
        this.getContentPane().repaint();
        this.setResizable(false);
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
                dispose();
            }
        };
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(stroke, "ESCAPE");
        rootPane.getActionMap().put("ESCAPE", actionListener);

        this.setSize(500, 255);
        this.setVisible(true);


        return rootPane;
    }

    public void infoStatusMsg(String text){
        this.statusMsg.setForeground(Color.GREEN.darker().darker());
        this.statusMsg.setText(text);
    }

    public void alertStatusMsg(String text){
        this.statusMsg.setForeground(Color.RED);
        this.statusMsg.setText(text);
    }


    public static void main(String args[]) {

        AddressState as = new AddressState();

        PayOutDialog pod = new PayOutDialog(null,  as);
        pod.setVisible(true);


    }
}

