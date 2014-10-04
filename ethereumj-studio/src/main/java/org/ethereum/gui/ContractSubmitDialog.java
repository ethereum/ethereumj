package org.ethereum.gui;

import org.ethereum.core.*;
import org.ethereum.util.Utils;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigInteger;
import java.net.URL;
import java.util.Collection;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 18/05/14 22:21
 */
class ContractSubmitDialog extends JDialog implements MessageAwareDialog {

    private static final long serialVersionUID = -3622984456084608996L;
	
	ContractSubmitDialog dialog;
    JComboBox<AccountWrapper> creatorAddressCombo;
    final JTextField gasInput;
    final JTextField contractAddrInput;

    private byte[]       initByteCode;


    JLabel statusMsg = null;

    public ContractSubmitDialog(Frame parent, byte[] byteCode) {
        super(parent, "Contract Details: ", false);
        dialog = this;
        this.initByteCode = byteCode;

        contractAddrInput = new JTextField(5);
        GUIUtils.addStyle(contractAddrInput, "Contract Address: ");

        contractAddrInput.setBounds(70, 30, 350, 45);
        this.getContentPane().add(contractAddrInput);

        gasInput = new JTextField(5);
        GUIUtils.addStyle(gasInput, "Gas: ");

        JTextArea   contractDataTA = new JTextArea();
        contractDataTA.setLineWrap(true);
        JScrollPane contractDataInput = new JScrollPane(contractDataTA);
        GUIUtils.addStyle(contractDataTA, null, false);
        GUIUtils.addStyle(contractDataInput, "Code:");

        String byteCodeText = GUIUtils.getHexStyledText(byteCode);
        contractDataTA.setText(byteCodeText);
        contractDataTA.setEditable(false);
        contractDataTA.setCaretPosition(0);

        this.getContentPane().setBackground(Color.WHITE);
        this.getContentPane().setLayout(null);


        contractDataInput.setBounds(70, 80, 350, 165);
        this.getContentPane().add(contractDataInput);

        gasInput.setBounds(330, 260, 90, 45);
        this.getContentPane().add(gasInput);

        URL rejectIconURL = ClassLoader.getSystemResource("buttons/reject.png");
        ImageIcon rejectIcon = new ImageIcon(rejectIconURL);
        JLabel rejectLabel = new JLabel(rejectIcon);
        rejectLabel.setToolTipText("Cancel");
        rejectLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        URL playIconURL = ClassLoader.getSystemResource("buttons/play.png");
        ImageIcon playIcon = new ImageIcon(playIconURL);
        JLabel playLabel = new JLabel(playIcon);
        playLabel.setToolTipText("Play Drafted");
        playLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        playLabel.setBounds(438, 100, 42, 42);
        this.getContentPane().add(playLabel);

        playLabel.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {

                        Transaction tx = null;
                        try {
                            tx = createTransaction();
                        } catch (Exception e1) {

                            dialog.alertStatusMsg("Failed to sign the transaction");
                            return;
                        }
                        contractAddrInput.setText(Hex.toHexString(tx.getContractAddress()));

                        Block lastBlock = UIEthereumManager.ethereum.getBlockchain().getLastBlock();
                        ProgramPlayDialog.createAndShowGUI(tx.getData(), tx, lastBlock);
                    }}
        );

        JLabel statusMessage = new JLabel("");
        statusMessage.setBounds(50, 360, 400, 50);
        statusMessage.setHorizontalAlignment(SwingConstants.CENTER);
        this.statusMsg = statusMessage;
        this.getContentPane().add(statusMessage);

        rejectLabel.setBounds(260, 325, 45, 45);
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

        approveLabel.setBounds(200, 325, 45, 45);
        this.getContentPane().add(approveLabel);
        approveLabel.setVisible(true);

        approveLabel.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        submitContract();
                    }
                }
        );

        gasInput.setText("1000");

        JComboBox<AccountWrapper> creatorAddressCombo = new JComboBox<AccountWrapper>() {
            @Override
            public ComboBoxUI getUI() {
                return super.getUI();
            }
        };
        creatorAddressCombo.setOpaque(true);
        creatorAddressCombo.setEnabled(true);

        creatorAddressCombo.setBackground(Color.WHITE);
        creatorAddressCombo.setFocusable(false);

        this.creatorAddressCombo = creatorAddressCombo;

        final Border line = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        JComponent editor = (JComponent)(creatorAddressCombo.getEditor().getEditorComponent());
        editor.setForeground(Color.RED);

        Wallet wallet = UIEthereumManager.ethereum.getWallet();
        Collection<Account> accounts = wallet.getAccountCollection();

        for (Account account : accounts) {
            creatorAddressCombo.addItem(new AccountWrapper(account));
        }

        creatorAddressCombo.setRenderer(new DefaultListCellRenderer() {

            @Override
            public void paint(Graphics g) {
                setBackground(Color.WHITE);
                setForeground(new Color(143, 170, 220));
                setFont(new Font("Monospaced", 0, 13));
                setBorder(BorderFactory.createEmptyBorder());
                super.paint(g);
            }

        });

        creatorAddressCombo.setPopupVisible(false);

        Object child = creatorAddressCombo.getAccessibleContext().getAccessibleChild(0);
        BasicComboPopup popup = (BasicComboPopup)child;

        JList list = popup.getList();
        list.setSelectionBackground(Color.cyan);
        list.setBorder(null);

		for (int i = 0; i < creatorAddressCombo.getComponentCount(); i++) {
			if (creatorAddressCombo.getComponent(i) instanceof CellRendererPane) {
				CellRendererPane crp = ((CellRendererPane) 
						(creatorAddressCombo.getComponent(i)));
			}
			if (creatorAddressCombo.getComponent(i) instanceof AbstractButton) {
				((AbstractButton) creatorAddressCombo.getComponent(i))
						.setBorder(line);
			}
		}
        creatorAddressCombo.setBounds(73, 267, 230, 36);
        this.getContentPane().add(creatorAddressCombo);
        this.getContentPane().revalidate();
        this.getContentPane().repaint();
        this.setResizable(false);
        
        this.setVisible(true);
    }

    protected JRootPane createRootPane() {

        Container parent = this.getParent();

        if (parent != null) {
            Dimension parentSize = parent.getSize();
            Point p = parent.getLocation();
            setLocation(p.x + parentSize.width / 4, p.y + 10);
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

        this.setSize(500, 430);
        return rootPane;
    }

    public void infoStatusMsg(String text) {
        this.statusMsg.setForeground(Color.GREEN.darker().darker());
        this.statusMsg.setText(text);
    }

    public void alertStatusMsg(String text) {
        this.statusMsg.setForeground(Color.RED);
        this.statusMsg.setText(text);
    }

    public void submitContract() {

        if (!validInput())
            return;

        Transaction tx = null;
        try {
            tx = createTransaction();
        } catch (Exception e1) {

            dialog.alertStatusMsg("Failed to sign the transaction");
            return;
        }
        contractAddrInput.setText(Hex.toHexString(tx.getContractAddress()));

        if (!UIEthereumManager.ethereum.isConnected()) {
            dialog.alertStatusMsg("Not connected to any peer");
            return;
        }

        // SwingWorker
        DialogWorker worker = new DialogWorker(tx, this);
        worker.execute();
    }

    private Transaction createTransaction() {

        Account account = ((AccountWrapper)creatorAddressCombo.getSelectedItem()).getAccount();

        byte[] senderPrivKey = account.getEcKey().getPrivKeyBytes();
        byte[] nonce = account.getNonce() == BigInteger.ZERO ? null : account.getNonce().toByteArray();
        byte[] gasPrice = new BigInteger("10000000000000").toByteArray();

        BigInteger gasBI = new BigInteger(gasInput.getText());
        byte[] gasValue  = BigIntegers.asUnsignedByteArray(gasBI);
        byte[] endowment = BigIntegers.asUnsignedByteArray(new BigInteger("1000"));

        byte[] zeroAddress = null;

        Transaction tx = new Transaction(nonce, gasPrice, gasValue,
                zeroAddress, endowment, initByteCode);

        tx.sign(senderPrivKey);

        return tx;
    }

    private boolean validInput() {

        Account account = ((AccountWrapper)creatorAddressCombo.getSelectedItem()).getAccount();
        BigInteger currentBalance = account.getBalance();

        long currGasPrice = UIEthereumManager.ethereum.getBlockchain().getGasPrice();
        BigInteger gasPrice = BigInteger.valueOf(currGasPrice);
        BigInteger gasInput = new BigInteger( this.gasInput.getText());

        boolean canAfford = currentBalance.compareTo(gasPrice.multiply(gasInput)) >= 0;

        if (!canAfford) {
            alertStatusMsg("The address can't afford this transaction");
            return false;
        }
        return true;
    }

    public static void main(String args[]) {
        AccountState as = new AccountState();
        ContractSubmitDialog pod = new ContractSubmitDialog(null, null);
        pod.setVisible(true);
    }

    public class AccountWrapper {

        private Account account;

        public AccountWrapper(Account account) {
            this.account = account;
        }

        public Account getAccount() {
            return account;
        }

        @Override
        public String toString() {
            String addressShort = Utils.getAddressShortString(account.getEcKey().getAddress());
            String valueShort   = Utils.getValueShortString(account.getBalance());

			String result = String.format(" By: [%s] %s", addressShort, valueShort);
            return result;
        }
    }
}

