package org.ethereum.gui;

import org.ethereum.core.Account;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.core.Wallet;
import org.ethereum.db.ContractDetails;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.*;
import java.math.BigInteger;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 18/05/14 22:21
 */
class ContractCallDialog extends JDialog implements MessageAwareDialog {

	private static final long serialVersionUID = -7561153561155037293L;

	private Logger logger = LoggerFactory.getLogger("ui");

	private ContractCallDialog dialog;
	private JComboBox<AccountWrapper> creatorAddressCombo;
	private final JTextField gasInput;
	private final JTextField contractAddrInput;

	private JScrollPane contractDataInput;
	private JTextArea   msgDataTA;

	private JLabel statusMsg = null;
	private JLabel playLabel = null;
	private JLabel rejectLabel = null;
	private JLabel approveLabel = null;

    public ContractCallDialog(Frame parent) {
        super(parent, "Call Contract: ", false);
        dialog = this;

        contractAddrInput = new JTextField(5);
        GUIUtils.addStyle(contractAddrInput, "Contract Address: ");
        contractAddrInput.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        populateContractDetails();
                    }
                });

            }
        });

        contractAddrInput.setBounds(70, 30, 350, 45);
        this.getContentPane().add(contractAddrInput);

        gasInput = new JTextField(5);
        GUIUtils.addStyle(gasInput, "Gas: ");

        msgDataTA = new JTextArea();
        msgDataTA.setLineWrap(true);
        contractDataInput = new JScrollPane(msgDataTA);
        GUIUtils.addStyle(msgDataTA, null, false);
        GUIUtils.addStyle(contractDataInput, "Input:");

        msgDataTA.setText("");
        msgDataTA.setCaretPosition(0);

        this.getContentPane().setBackground(Color.WHITE);
        this.getContentPane().setLayout(null);

        contractDataInput.setBounds(70, 80, 350, 165);
        this.getContentPane().add(contractDataInput);

        gasInput.setBounds(330, 260, 90, 45);
        this.getContentPane().add(gasInput);

        URL rejectIconURL = ClassLoader.getSystemResource("buttons/reject.png");
        ImageIcon rejectIcon = new ImageIcon(rejectIconURL);
        rejectLabel = new JLabel(rejectIcon);
        rejectLabel.setToolTipText("Cancel");
        rejectLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        URL playIconURL = ClassLoader.getSystemResource("buttons/play.png");
        ImageIcon playIcon = new ImageIcon(playIconURL);
        playLabel = new JLabel(playIcon);
        playLabel.setToolTipText("Play Drafted");
        playLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        playLabel.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                          ContractCallDialog.this.playContractCall();
                    }}
        );

        playLabel.setBounds(438, 100, 42, 42);
        this.getContentPane().add(playLabel);

        JLabel statusMessage = new JLabel("");
        statusMessage.setBounds(50, 360, 400, 50);
        statusMessage.setHorizontalAlignment(SwingConstants.CENTER);
        this.statusMsg = statusMessage;
        this.getContentPane().add(statusMessage);

        rejectLabel.setBounds(260, 325, 45, 45);
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
        approveLabel = new JLabel(approveIcon);
        approveLabel.setToolTipText("Submit the transaction");
        approveLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        approveLabel.setBounds(200, 325, 45, 45);
        this.getContentPane().add(approveLabel);
        approveLabel.setVisible(true);

        approveLabel.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        submitContractCall();
                    }
                }
        );

        gasInput.setText("1000");

		JComboBox<AccountWrapper> creatorAddressCombo = new JComboBox<AccountWrapper>() {
			private static final long serialVersionUID = -3748305421049121671L;
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
			private static final long serialVersionUID = 6100091092527477892L;

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
                CellRendererPane crp = ((CellRendererPane) (creatorAddressCombo.getComponent(i)));
            }
            if (creatorAddressCombo.getComponent(i) instanceof AbstractButton) {
                ((AbstractButton) creatorAddressCombo.getComponent(i)).setBorder(line);
            }
        }
        creatorAddressCombo.setBounds(70, 267, 230, 36);
        this.getContentPane().add(creatorAddressCombo);

        this.getContentPane().revalidate();
        this.getContentPane().repaint();
        this.setResizable(false);
        
        this.setVisible(true);
    }

    private void populateContractDetails() {
		byte[] addr = Utils.addressStringToBytes(contractAddrInput.getText());
		if(addr == null) {
			alertStatusMsg("Not a valid contract address");
        	return;
		}
			
		ContractDetails contractDetails = UIEthereumManager.ethereum
				.getRepository().getContractDetails(addr);
        if (contractDetails == null) {
            alertStatusMsg("No contract for that address");
            return;
        }

        final byte[] programCode = contractDetails.getCode();
        if (programCode == null || programCode.length == 0) {
            alertStatusMsg("Such account exist but no code in the db");
            return;
        }
        
        final Map storageMap = contractDetails.getStorage();

        contractDataInput.setBounds(70, 80, 350, 145);
        contractDataInput.setViewportView(msgDataTA);

        URL expandIconURL = ClassLoader.getSystemResource("buttons/add-23x23.png");
        ImageIcon expandIcon = new ImageIcon(expandIconURL);
        final JLabel expandLabel = new JLabel(expandIcon);
        expandLabel.setToolTipText("Cancel");
        expandLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        expandLabel.setBounds(235, 232, 23, 23);
        this.getContentPane().add(expandLabel);
        expandLabel.setVisible(true);

        final Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        final JPanel detailPanel = new JPanel();
        detailPanel.setBorder(border);
        detailPanel.setBounds(135, 242, 230, 2);

        final JPanel spacer = new JPanel();
        spacer.setForeground(Color.white);
        spacer.setBackground(Color.white);
        spacer.setBorder(null);
        spacer.setBounds(225, 232, 40, 20);

        this.getContentPane().add(spacer);
        this.getContentPane().add(detailPanel);

        expandLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ContractCallDialog.this.setSize(500, 530);

                ContractCallDialog.this.creatorAddressCombo.setBounds(70, 367, 230, 36);
                ContractCallDialog.this.gasInput.setBounds(330, 360, 90, 45);
                ContractCallDialog.this.rejectLabel.setBounds(260, 425, 45, 45);
                ContractCallDialog.this.approveLabel.setBounds(200, 425, 45, 45);
                ContractCallDialog.this.statusMsg.setBounds(50, 460, 400, 50);

                spacer.setVisible(false);
                expandLabel.setVisible(false);
                detailPanel.setVisible(false);

                JTextField contractCode = new JTextField(15);
                contractCode.setText(Hex.toHexString( programCode ));
                GUIUtils.addStyle(contractCode, "Code: ");
                contractCode.setBounds(70, 230, 350, 45);

                JTable storage = new JTable(2, 2);
                storage.setTableHeader(null);
                storage.setShowGrid(false);
                storage.setIntercellSpacing(new Dimension(15, 0));
                storage.setCellSelectionEnabled(false);
                GUIUtils.addStyle(storage);

                JTableStorageModel tableModel = new JTableStorageModel(storageMap);
                storage.setModel(tableModel);

                JScrollPane scrollPane = new JScrollPane(storage);
                scrollPane.setBorder(null);
                scrollPane.getViewport().setBackground(Color.WHITE);
                scrollPane.setBounds(70, 290, 350, 50);


                ContractCallDialog.this.getContentPane().add(contractCode);
                ContractCallDialog.this.getContentPane().add(scrollPane);
            }
        });
        this.repaint();
    }
    
    private void playContractCall() {   	
        byte[] addr = Utils.addressStringToBytes(contractAddrInput.getText());
		if(addr == null) {
			alertStatusMsg("Not a valid contract address");
        	return;
		}
		
		ContractDetails contractDetails = UIEthereumManager.ethereum
				.getRepository().getContractDetails(addr);
        if (contractDetails == null) {
            alertStatusMsg("No contract for that address");
            return;
        }

        final byte[] programCode = contractDetails.getCode();
        if (programCode == null || programCode.length == 0) {
            alertStatusMsg("Such account exist but no code in the db");
            return;
        }

        Transaction tx = createTransaction();
        if (tx == null) return;

        Block lastBlock = UIEthereumManager.ethereum.getBlockchain().getLastBlock();
        ProgramPlayDialog.createAndShowGUI(programCode, tx, lastBlock);
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

    public void submitContractCall() {

        if (!UIEthereumManager.ethereum.isConnected()) {
            dialog.alertStatusMsg("Not connected to any peer");
            return;
        }

        Transaction tx = createTransaction();
        if (tx == null) return;

        if (logger.isInfoEnabled()) {
            logger.info("tx.hash: {}", (new BigInteger(tx.getHash()).toString(16)));
        }
        // SwingWorker
        DialogWorker worker = new DialogWorker(tx, this);
        worker.execute();
    }

	private Transaction createTransaction() {

		byte[] data;
		if (!msgDataTA.getText().trim().equals("")) {
			Object[] lexaList = msgDataTA.getText().split(",");
			data = ByteUtil.encodeDataList(lexaList);
		} else {
			data = new byte[] {};
		}

        byte[] contractAddress = Hex.decode( contractAddrInput.getText());

        Account account = ((AccountWrapper)creatorAddressCombo.getSelectedItem()).getAccount();

        byte[] senderPrivKey = account.getEcKey().getPrivKeyBytes();
        byte[] nonce = account.getNonce() == BigInteger.ZERO ? null : account.getNonce().toByteArray();
        BigInteger gasPrice = new BigInteger("10000000000000");

        BigInteger gasBI = new BigInteger(gasInput.getText());
        byte[] gasValue  = BigIntegers.asUnsignedByteArray(gasBI);
        BigInteger endowment = new BigInteger("1000");

        if (logger.isInfoEnabled()) {
            logger.info("Contract call:");
            logger.info("tx.nonce: {}", nonce == null ? "null" : Hex.toHexString(nonce));
            logger.info("tx.gasPrice: {}", Hex.toHexString(BigIntegers.asUnsignedByteArray( gasPrice )));
            logger.info("tx.gasValue: {}", Hex.toHexString(gasValue));
            logger.info("tx.address: {}", Hex.toHexString(contractAddress));
            logger.info("tx.endowment: {}", Hex.toHexString(BigIntegers.asUnsignedByteArray( endowment)));
            logger.info("tx.data: {}", Hex.toHexString(data));
        }

        Transaction tx = UIEthereumManager.ethereum.createTransaction(account.getNonce(),
                gasPrice, gasBI,
                contractAddress, endowment, data);

        try {
            tx.sign(senderPrivKey);
        } catch (Exception e1) {
            dialog.alertStatusMsg("Failed to sign the transaction");
            return null;
        }
        return tx;
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
			String valueShort = Utils.getValueShortString(account.getBalance());
			String result = String.format(" By: [%s] %s", addressShort,
					valueShort);
			return result;
		}
    }

    private class JTableStorageModel extends DefaultTableModel {
        private JTableStorageModel(Map<String, String> data) {

            if (data != null) {

                this.setColumnCount(2);
                this.setRowCount(data.size());

                int i = 0;
                for (String key : data.keySet()) {
                    this.setValueAt(key, i, 0);
                    this.setValueAt(data.get(key), i, 1);
                    ++i;
                }
            }
        }
    }

    public static void main(String args[]) {
        ContractCallDialog ccd = new ContractCallDialog(null);
        ccd.setVisible(true);
        ccd.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        ccd.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                UIEthereumManager.ethereum.close();
            }
        });
    }
}

