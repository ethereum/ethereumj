package org.ethereum.gui;

import org.ethereum.core.Transaction;
import org.ethereum.manager.MainData;
import org.ethereum.net.client.ClientPeer;
import org.ethereum.wallet.AddressState;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigInteger;
import java.net.URL;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 18/05/14 22:21
 */
class ContractSubmitDialog extends JDialog {


    ContractSubmitDialog dialog;

    AddressState addressState = null;
    JLabel statusMsg = null;

    public ContractSubmitDialog(Frame parent, final AddressState addressState) {
        super(parent, "Contract Details: ", false);
        dialog = this;

        this.addressState = addressState;

        final JTextField gasInput = new JTextField(5);
        GUIUtils.addStyle(gasInput, "Gas: ");

        JTextArea   contractInitTA = new JTextArea();
        contractInitTA.setLineWrap(true);
        JScrollPane contractInitInput = new JScrollPane(contractInitTA);
        GUIUtils.addStyle(contractInitTA, null, false);
        GUIUtils.addStyle(contractInitInput, "Init:");

        JTextArea   contractDataTA = new JTextArea();
        contractDataTA.setLineWrap(true);
        JScrollPane contractDataInput = new JScrollPane(contractDataTA);
        GUIUtils.addStyle(contractDataTA, null, false);
        GUIUtils.addStyle(contractDataInput, "Data:");

        this.getContentPane().setBackground(Color.WHITE);
        this.getContentPane().setLayout(null);

        contractInitInput.setBounds(70, 30, 350, 165);
        this.getContentPane().add(contractInitInput);

        contractDataInput.setBounds(70, 200, 350, 165);
        this.getContentPane().add(contractDataInput);

        gasInput.setBounds(330, 380, 90, 45);
        this.getContentPane().add(gasInput);

        URL rejectIconURL = ClassLoader.getSystemResource("buttons/reject.png");
        ImageIcon rejectIcon = new ImageIcon(rejectIconURL);
        JLabel rejectLabel = new JLabel(rejectIcon);
        rejectLabel.setToolTipText("Cancel");
        rejectLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel statusMessage = new JLabel("");
        statusMessage.setBounds(50, 480, 400, 50);
        statusMessage.setHorizontalAlignment(SwingConstants.CENTER);
        this.statusMsg = statusMessage;
        this.getContentPane().add(statusMessage);

        rejectLabel.setBounds(260, 445, 45, 45);
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

        approveLabel.setBounds(200, 445, 45, 45);
        this.getContentPane().add(approveLabel);
        approveLabel.setVisible(true);


        approveLabel.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                    }
                }
        );


        gasInput.setText("1000");



        JComboBox jComboBox = new JComboBox(){
            @Override
            public ComboBoxUI getUI() {

                BasicComboBoxUI ui = (BasicComboBoxUI)super.getUI();

                return super.getUI();
            }
        };
        jComboBox.setOpaque(true);
        jComboBox.setEnabled(true);

        jComboBox.setBackground(Color.WHITE);
        jComboBox.setFocusable(false);

        final Border line = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
//        jComboBox.setBorder(line);
        JComponent editor = (JComponent)(jComboBox.getEditor().getEditorComponent());
        editor.setForeground(Color.RED);

        jComboBox.addItem(" By: 1f21c - 1000 (10^9)");
        jComboBox.setRenderer(new DefaultListCellRenderer() {

            @Override
            public void paint(Graphics g) {
                setBackground(Color.WHITE);
                setForeground(new Color(143, 170, 220));
                setFont(new Font("Monospaced", 0, 13));
                setBorder(BorderFactory.createEmptyBorder());
                super.paint(g);
            }

        });

        jComboBox.setPopupVisible(false);

        Object child = jComboBox.getAccessibleContext().getAccessibleChild(0);
        BasicComboPopup popup = (BasicComboPopup)child;

        JList list = popup.getList();
        list.setSelectionBackground(Color.cyan);
        list.setBorder(null);

        for (int i = 0; i < jComboBox.getComponentCount(); i++)
        {
            if (jComboBox.getComponent(i) instanceof CellRendererPane) {

                CellRendererPane crp = ((CellRendererPane) (jComboBox.getComponent(i)));
            }

            if (jComboBox.getComponent(i) instanceof AbstractButton) {
                ((AbstractButton) jComboBox.getComponent(i)).setBorder(line);
            }
        }
        jComboBox.setBounds(73, 387, 230, 36);
        this.getContentPane().add(jComboBox);


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

        this.setSize(500, 550);
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

        ContractSubmitDialog pod = new ContractSubmitDialog(null,  as);


        pod.setVisible(true);


    }
}

