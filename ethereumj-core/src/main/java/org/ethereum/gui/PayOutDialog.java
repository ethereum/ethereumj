package org.ethereum.gui;

import org.ethereum.core.Account;
import org.ethereum.core.AccountState;
import org.ethereum.core.Transaction;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.client.ClientPeer;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigInteger;
import java.net.URL;
import java.util.regex.Pattern;

import javax.swing.*;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 18/05/14 22:21
 */
class PayOutDialog extends JDialog implements MessageAwareDialog {

	private static final long serialVersionUID = -2838121935782110981L;

	private PayOutDialog dialog;

    private AccountState accountState = null;
    private JLabel statusMsg = null;

    private final JTextField receiverInput;
    private final JTextField amountInput;
    private final JTextField feeInput;

	public PayOutDialog(Frame parent, final Account account) {
		super(parent, "Payout details: ", false);
		dialog = this;

		this.accountState = account;

        receiverInput = new JTextField(18);
        GUIUtils.addStyle(receiverInput, "Pay to:");

        amountInput = new JTextField(18);
        GUIUtils.addStyle(amountInput, "Amount: ");

        feeInput = new JTextField(5);
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
		rejectLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				dialog.dispose();
			}
		});

        URL approveIconURL = ClassLoader.getSystemResource("buttons/approve.png");
        ImageIcon approveIcon = new ImageIcon(approveIconURL);
        JLabel approveLabel = new JLabel(approveIcon);
        approveLabel.setToolTipText("Submit the transaction");
        approveLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        approveLabel.setBounds(200, 145, 45, 45);
        this.getContentPane().add(approveLabel);
        approveLabel.setVisible(true);

		approveLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

                if (!validInput()) {
                    return;
                }

                BigInteger fee = new BigInteger(feeInput.getText());
                BigInteger value = new BigInteger(amountInput.getText());
                byte[] address = Hex.decode(receiverInput.getText());

                // Client
				ClientPeer peer = WorldManager.getInstance().getActivePeer();

				if (peer == null) {
					dialog.alertStatusMsg("Not connected to any peer");
					return;
				}

				byte[] senderPrivKey = account.getEcKey().getPrivKeyBytes();
				byte[] nonce = accountState.getNonce() == BigInteger.ZERO ? null : accountState.getNonce().toByteArray();

                byte[] gasPrice = BigInteger.valueOf( WorldManager.getInstance().getBlockchain().getGasPrice()).toByteArray();

				Transaction tx = new Transaction(nonce, gasPrice, BigIntegers
						.asUnsignedByteArray(fee), address, BigIntegers
						.asUnsignedByteArray(value), null);

				try {
					tx.sign(senderPrivKey);
				} catch (Exception e1) {
					dialog.alertStatusMsg("Failed to sign the transaction");
					return;
				}

				// SwingWorker
				DialogWorker worker = new DialogWorker(tx, dialog);
				worker.execute();
			}
		});

        feeInput.setText("1000");
        amountInput.setText("0");

        this.getContentPane().revalidate();
        this.getContentPane().repaint();
        this.setResizable(false);
    }

    private boolean validInput() {

        String receiverText = receiverInput.getText();
        if (receiverText == null || receiverText.isEmpty()) {
            alertStatusMsg("Should specify valid receiver address");
            return false;
        }

        if (!Pattern.matches("[0-9a-fA-F]+", receiverText)) {
            alertStatusMsg("Should specify valid receiver address");
            return false;
        }

        if (Hex.decode(receiverText).length != 20) {
            alertStatusMsg("Should specify valid receiver address");
            return false;
        }

        String amountText = amountInput.getText();
        if (amountText == null || amountText.isEmpty()) {
            alertStatusMsg("Should specify amount to transfer");
            return false;
        }

        if (!Pattern.matches("[0-9]+", amountText)) {
            alertStatusMsg("Should specify numeric value for amount ");
            return false;
        }

        if (amountText.equals("0")) {
            alertStatusMsg("Should specify more than zero for transaction");
            return false;
        }

        String feeText = feeInput.getText();
        if (feeText == null || feeText.isEmpty()) {
            alertStatusMsg("Should specify fee to fund the transaction");
            return false;
        }

        if (!Pattern.matches("[0-9]+", feeText)) {
            alertStatusMsg("Should specify numeric value for a fee");
            return false;
        }

        // check if the tx is affordable
        BigInteger ammountValue = new BigInteger(amountText);
        BigInteger feeValue = new BigInteger(feeText);
        BigInteger gasPrice = BigInteger.valueOf(WorldManager.getInstance().getBlockchain().getGasPrice());
        BigInteger currentBalance = accountState.getBalance();

        boolean canAfford = gasPrice.multiply(feeValue).add(ammountValue).compareTo(currentBalance) != 1;

        if (!canAfford) {
            alertStatusMsg("The address can't afford this transaction");
            return false;
        }
        return true;
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

        SwingUtilities.invokeLater(new Runnable() {
        	  public void run() {
        		  setSize(500, 255);
        	      setVisible(true);
        	  }
        });
       

        return rootPane;
    }

    public void infoStatusMsg(final String text) {

        final PayOutDialog dialog = this;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                dialog.statusMsg.setForeground(Color.GREEN.darker().darker());
                dialog.statusMsg.setText(text);
                dialog.revalidate();
                dialog.repaint();
            }
        });
    }

    public void alertStatusMsg(final String text) {
        final PayOutDialog dialog = this;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                dialog.statusMsg.setForeground(Color.RED);
                dialog.statusMsg.setText(text);
                dialog.revalidate();
                dialog.repaint();
            }
        });
    }

    public static void main(String args[]) {
        Account account = new Account();
        PayOutDialog pod = new PayOutDialog(null,  account);
        pod.setVisible(true);
    }
}

