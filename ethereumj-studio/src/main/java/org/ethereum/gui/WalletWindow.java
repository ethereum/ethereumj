package org.ethereum.gui;

import org.ethereum.core.Account;
import org.ethereum.core.Wallet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

/**
 * www.ethereumJ.com
 *
 * @author Roman Mandeleil
 * Created on: 17/05/14 12:00
 */
public class WalletWindow extends JFrame implements Wallet.WalletListener {

    WalletWindow walletWindow;
    ToolBar toolBar;

    public WalletWindow(ToolBar toolBar) {

        addCloseAction();
        this.toolBar = toolBar;

        walletWindow = this;
        URL url = ClassLoader.getSystemResource("ethereum-icon.png");
        Toolkit kit = Toolkit.getDefaultToolkit();
        Image img = kit.createImage(url);
        this.setIconImage(img);
        setTitle("Ethereum Wallet");
        setSize(550, 280);
        setLocation(215, 280);
        setResizable(false);

        Container contentPane = this.getContentPane();
        contentPane.setBackground(new Color(255, 255, 255));

        Wallet wallet = UIEthereumManager.ethereum.getWallet();
        wallet.addListener(this);
        loadWallet();

    }

    private void loadWallet() {

        Container contentPane = this.getContentPane();
        contentPane.removeAll();
        contentPane.setLayout(new FlowLayout());

        Wallet wallet = UIEthereumManager.ethereum.getWallet();

        for (Account account : wallet.getAccountCollection()) {
            WalletAddressPanel rowPanel = new WalletAddressPanel(account);
            contentPane.add(rowPanel);
        }

        WalletSumPanel sumPanel = new WalletSumPanel(wallet.totalBalance());
        contentPane.add(sumPanel);

        // TODO: move this to some add button method
        URL addAddressIconURL = ClassLoader.getSystemResource("buttons/add-address.png");
        ImageIcon addAddressIcon = new ImageIcon(addAddressIconURL);
        JLabel addAddressLabel = new JLabel(addAddressIcon);
        addAddressLabel.setToolTipText("Add new address");
        addAddressLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addAddressLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                Wallet wallet = UIEthereumManager.ethereum.getWallet();
                if (wallet.getAccountCollection().size() >= 5) {
                    JOptionPane.showMessageDialog(walletWindow,
                            "Hey do you really need more than 5 address for a demo wallet");
                    return;
                }

                wallet.addNewAccount();
                Dimension dimension = walletWindow.getSize();
                int height = dimension.height;
                int width = dimension.width;

                Dimension newDimension = new Dimension(width, (height + 45));
                walletWindow.setSize(newDimension);
            }
        });
        contentPane.add(addAddressLabel);
        contentPane.revalidate();
        contentPane.repaint();
    }

    public void addCloseAction() {
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                toolBar.walletToggle.setSelected(false);
            }
        });
    }

    @Override
    public void valueChanged() {
        loadWallet();
    }
}
