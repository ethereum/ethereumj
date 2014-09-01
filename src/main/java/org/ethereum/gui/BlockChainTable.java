package org.ethereum.gui;

import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.manager.WorldManager;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.Utils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * www.ethereumJ.com
 *
 * @author: Adrian Benko
 * Created on: 27/08/14 18:22
 */
public class BlockChainTable extends JFrame implements ActionListener {

  final static int BLOCK_CHECK_INTERVAL = 1000;
  final static String FONT_NAME         = "Courier New";
  final static int FONT_SIZE_TITLE      = 20;
  final static int FONT_SIZE            = 13;

  final static Font boldTitle = new Font(FONT_NAME, Font.BOLD, FONT_SIZE_TITLE);
  final static Font plainTitle = new Font(FONT_NAME, Font.PLAIN, FONT_SIZE_TITLE);
  final static Font bold = new Font(FONT_NAME, Font.BOLD, FONT_SIZE);
  final static Font plain = new Font(FONT_NAME, Font.PLAIN, FONT_SIZE);
  final static Color HILIT_COLOR = Color.LIGHT_GRAY;

  class MyDocumentFilter extends DocumentFilter {
    @Override
    public void insertString(FilterBypass fb, int off
        , String str, AttributeSet attr)
        throws BadLocationException
    {
      // remove non-digits
      fb.insertString(off, str.replaceAll("\\D++", ""), attr);
    }
    @Override
    public void replace(FilterBypass fb, int off
        , int len, String str, AttributeSet attr)
        throws BadLocationException
    {
      // remove non-digits
      fb.replace(off, len, str.replaceAll("\\D++", ""), attr);
    }
  }

  private volatile boolean running;
  private TransactionData transactionDataWindow = null;

  private JPanel topPanel;
  private JPanel titlePanel;
  private JPanel blockPanel;
  private JPanel transactionsPanel;
  private JScrollPane scrollPane;

  JTextField blockNumberText;
  JButton firstBlock;
  JButton prevBlock;
  JButton nextBlock;
  JButton lastBlock;
  JLabel blocksCount;
  JTextField findText;
  JButton findPrev;
  JButton findNext;

  JTextField blockn;
  JTextField minGasPrice;
  JTextField gasLimit;
  JTextField gasUsed;
  JTextField timestamp;
  JTextField difficulty;
  JTextField hash;
  JTextField parentHash;
  JTextField uncleHash;
  JTextField stateRoot;
  JTextField trieRoot;
  JTextField coinbase;
  JTextField nonce;
  JTextField extraData;

  Thread t;

  private int lastFindIndex = -1;
  private String textToFind = "";
  private java.util.List<Long> foundBlocks;
  final Highlighter.HighlightPainter painter;

  ToolBar toolBar;

  public BlockChainTable(ToolBar toolBar) {
    this.toolBar = toolBar;
    addCloseAction();

    final BlockChainTable blockchainTable = this;
    foundBlocks = new ArrayList<Long>();
    painter = new DefaultHighlighter.DefaultHighlightPainter(HILIT_COLOR);

    setTitle("Block Chain Table");
    setSize(900, 400);
    setLocation(315, 270);
    setBackground(Color.gray);

    java.net.URL url = ClassLoader.getSystemResource("ethereum-icon.png");
    Toolkit kit = Toolkit.getDefaultToolkit();
    Image img = kit.createImage(url);
    this.setIconImage(img);


    // Create a panel to hold all other components
    topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));
    getContentPane().add(topPanel, BorderLayout.LINE_START);

    titlePanel = new JPanel(new FlowLayout());
    titlePanel.setMaximumSize(new Dimension(Short.MAX_VALUE,50));
    createTitlePanel(this);

    blockPanel = new JPanel(new GridBagLayout());
    blockPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,160));
    createBlockPanel();

    transactionsPanel = new JPanel(new GridBagLayout());
    transactionsPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,160));
    fillBlock(this);

    titlePanel.setAlignmentX(0);
    topPanel.add(titlePanel);
    blockPanel.setAlignmentX(0);
    topPanel.add(blockPanel);

    JLabel transactionsLabel = new JLabel("Transactions ");
    transactionsLabel.setFont(bold);
    transactionsLabel.setAlignmentX(0);
    transactionsLabel.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
    topPanel.add(transactionsLabel);

    scrollPane = new JScrollPane(transactionsPanel);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    scrollPane.setAlignmentX(0);
    topPanel.add(scrollPane);

    topPanel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), "Copy");
    topPanel.getActionMap().put("Copy", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {

        if (WorldManager.getInstance().getBlockchain().getSize() - 1 < lastFindIndex) return;

        Block block = WorldManager.getInstance().getBlockchain().getByNumber(lastFindIndex);
        StringSelection selection = new StringSelection(block.toString());
        Clipboard system = Toolkit.getDefaultToolkit().getSystemClipboard();
        system.setContents(selection, selection);
      }
    });

    t = new Thread() {
      public void run() {
        running = true;
        while (running) {
          blocksCount.setText("" + WorldManager.getInstance().getBlockchain().getSize());
          try {
            sleep(BLOCK_CHECK_INTERVAL);
          } catch (InterruptedException e) {
          }
        }
      }
    };
    t.start();
  }

  public void actionPerformed(ActionEvent e) {
    long blockNum = Long.parseLong(blockNumberText.getText());
    if ("firstBlock".equals(e.getActionCommand())) {
      blockNum = 0;
    } else if ("prevBlock".equals(e.getActionCommand())) {
      if (blockNum > 0) {
        blockNum--;
      }
    } else if ("nextBlock".equals(e.getActionCommand())) {
      if (blockNum < WorldManager.getInstance().getBlockchain().getSize() - 1) {
        blockNum++;
      }
    } else if ("lastBlock".equals(e.getActionCommand())) {
      blockNum = WorldManager.getInstance().getBlockchain().getSize()-1;
    } else if ("findPrev".equals(e.getActionCommand())) {
      if (findText.getText().length() > 0) {
        if (textToFind.equals(findText.getText())) {
          if (lastFindIndex > 0) {
            blockNum = foundBlocks.get(lastFindIndex-1);
            lastFindIndex--;
          } else {
            blockNum = findBlock(textToFind, blockNum, false);
          }
        } else {
          textToFind = findText.getText();
          lastFindIndex = -1;
          foundBlocks.clear();
          blockNum = findBlock(textToFind, blockNum, false);
        }
      }
    } else if ("findNext".equals(e.getActionCommand())) {
      if (findText.getText().length() > 0) {
        if (textToFind.equals(findText.getText())) {
          if (lastFindIndex > -1 && foundBlocks.size() > lastFindIndex + 1) {
            blockNum = foundBlocks.get(lastFindIndex+1);
            lastFindIndex++;
          } else {
            blockNum = findBlock(textToFind, blockNum, true);
          }
        } else {
          textToFind = findText.getText();
          lastFindIndex = -1;
          foundBlocks.clear();
          blockNum = findBlock(textToFind, blockNum, true);
        }
      }
    }
    blockNumberText.setText("" + blockNum);
    fillBlock(this);
  }

  private long findBlock(String textToFind, long blockNum, boolean forward) {
    if (forward) {
      for (long i = blockNum + 1; i < WorldManager.getInstance().getBlockchain().getSize(); i++) {
        Block block = WorldManager.getInstance().getBlockchain().getByNumber(i);
        if (block.toString().toLowerCase().contains(textToFind.toLowerCase())) {
          foundBlocks.add(i);
          lastFindIndex = foundBlocks.size() - 1;
          break;
        }
      }
    } else {
      for (long i = blockNum - 1; i >= 0; i--) {
        Block block = WorldManager.getInstance().getBlockchain().getByNumber(i);
        if (block.toString().toLowerCase().contains(textToFind.toLowerCase())) {
          foundBlocks.add(0, i);
          lastFindIndex = 0;
          break;
        }
      }
    }
    return foundBlocks.get(lastFindIndex);
  }

  public void terminate() {
    running = false;
  }

  public void addCloseAction() {
    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        toolBar.chainToggle.setSelected(false);
        if (transactionDataWindow != null) {
          transactionDataWindow.setVisible(false);
        }
      }
    });
  }

  public static void main(String args[]) {
    BlockChainTable mainFrame = new BlockChainTable(null);
    mainFrame.setVisible(true);
    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }


  private void createTitlePanel(final BlockChainTable blockchainTable) {
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;

    JLabel blockNumberLabel = new JLabel("Block #");
    blockNumberLabel.setFont(boldTitle);
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.0;
    c.insets = new Insets(0,10,0,0);
    titlePanel.add(blockNumberLabel, c);

    blockNumberText = new JTextField("0", 7);
    ((AbstractDocument)blockNumberText.getDocument()).setDocumentFilter(
        new MyDocumentFilter());
    // Listen for changes in the text
    blockNumberText.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        fillBlock(blockchainTable);
      }
      public void removeUpdate(DocumentEvent e) {
        fillBlock(blockchainTable);
      }
      public void insertUpdate(DocumentEvent e) {
        fillBlock(blockchainTable);
      }
    });

    blockNumberText.setFont(boldTitle);
    c.gridx = 1;
    c.gridy = 0;
    c.weightx = 1.0;
    c.insets = new Insets(0,0,0,10);
    titlePanel.add(blockNumberText, c);

    firstBlock = new JButton("|<");
    firstBlock.setFont(plain);
    firstBlock.setActionCommand("firstBlock");
    firstBlock.addActionListener(this);
    c.gridx = 2;
    c.gridy = 0;
    c.weightx = 0.0;
    c.insets = new Insets(0,0,0,0);
    titlePanel.add(firstBlock, c);

    prevBlock = new JButton("<");
    prevBlock.setFont(plain);
    prevBlock.setActionCommand("prevBlock");
    prevBlock.addActionListener(this);
    c.gridx = 3;
    c.gridy = 0;
    c.weightx = 0.0;
    c.insets = new Insets(0,0,0,0);
    titlePanel.add(prevBlock, c);

    nextBlock = new JButton(">");
    nextBlock.setFont(plain);
    nextBlock.setActionCommand("nextBlock");
    nextBlock.addActionListener(this);
    c.gridx = 4;
    c.gridy = 0;
    c.weightx = 0.0;
    c.insets = new Insets(0,0,0,0);
    titlePanel.add(nextBlock, c);

    lastBlock = new JButton(">|");
    lastBlock.setFont(plain);
    lastBlock.setActionCommand("lastBlock");
    lastBlock.addActionListener(this);
    c.gridx = 5;
    c.gridy = 0;
    c.weightx = 0.0;
    c.insets = new Insets(0,0,0,0);
    titlePanel.add(lastBlock, c);

    JLabel blocksCountLabel = new JLabel("Total blocks: ");
    blocksCountLabel.setFont(plain);
    c.gridx = 6;
    c.gridy = 0;
    c.weightx = 0.0;
    c.insets = new Insets(0,10,0,0);
    titlePanel.add(blocksCountLabel, c);

    blocksCount = new JLabel();
    blocksCount.setFont(plain);
    c.gridx = 7;
    c.gridy = 0;
    c.weightx = 0.0;
    c.insets = new Insets(0,0,0,0);
    titlePanel.add(blocksCount, c);

    JLabel findLabel = new JLabel("Find ");
    findLabel.setFont(plain);
    c.gridx = 8;
    c.gridy = 0;
    c.weightx = 0.0;
    c.insets = new Insets(0,10,0,0);
    titlePanel.add(findLabel, c);

    findText = new JTextField(12);
    findText.setFont(plain);
    c.gridx = 9;
    c.gridy = 0;
    c.weightx = 3.0;
    c.insets = new Insets(0,0,0,0);
    titlePanel.add(findText, c);

    findPrev = new JButton("<");
    findPrev.setFont(plain);
    findPrev.setActionCommand("findPrev");
    findPrev.addActionListener(this);
    c.gridx = 10;
    c.gridy = 0;
    c.weightx = 0.0;
    c.insets = new Insets(0,10,0,0);
    titlePanel.add(findPrev, c);

    findNext = new JButton(">");
    findNext.setFont(plain);
    findNext.setActionCommand("findNext");
    findNext.addActionListener(this);
    c.gridx = 11;
    c.gridy = 0;
    c.weightx = 0.0;
    c.insets = new Insets(0,0,0,10);
    titlePanel.add(findNext, c);
  }

  private void createBlockPanel() {
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;

    JLabel summaryLabel = new JLabel("Summary ");
    summaryLabel.setFont(bold);
    c.gridx = 0;
    c.gridy = 1;
    c.insets = new Insets(0,10,0,0);
    blockPanel.add(summaryLabel, c);

    JLabel blocknLabel = new JLabel("Block#");
    blocknLabel.setFont(plain);
    c.weightx = 1.0;
    c.gridx = 0;
    c.gridy = 2;
    blockPanel.add(blocknLabel, c);

    blockn = new JTextField();
    blockn.setEditable(false);
    blockn.setBorder(null);
    blockn.setFont(plain);
    c.gridx = 1;
    c.gridy = 2;
    blockPanel.add(blockn, c);

    JLabel minGasPriceLabel = new JLabel("Min gas price");
    minGasPriceLabel.setFont(plain);
    c.weightx = 1.0;
    c.gridx = 0;
    c.gridy = 3;
    blockPanel.add(minGasPriceLabel, c);

    minGasPrice = new JTextField();
    minGasPrice.setEditable(false);
    minGasPrice.setBorder(null);
    minGasPrice.setFont(plain);
    c.gridx = 1;
    c.gridy = 3;
    blockPanel.add(minGasPrice, c);

    JLabel gasLimitLabel = new JLabel("Gas limit");
    gasLimitLabel.setFont(plain);
    c.gridx = 0;
    c.gridy = 4;
    blockPanel.add(gasLimitLabel, c);

    gasLimit = new JTextField();
    gasLimit.setEditable(false);
    gasLimit.setBorder(null);
    gasLimit.setFont(plain);
    c.gridx = 1;
    c.gridy = 4;
    blockPanel.add(gasLimit, c);

    JLabel gasUsedLabel = new JLabel("Gas used");
    gasUsedLabel.setFont(plain);
    c.gridx = 0;
    c.gridy = 5;
    blockPanel.add(gasUsedLabel, c);

    gasUsed = new JTextField();
    gasUsed.setEditable(false);
    gasUsed.setBorder(null);
    gasUsed.setFont(plain);
    c.gridx = 1;
    c.gridy = 5;
    blockPanel.add(gasUsed, c);

    JLabel timestampLabel = new JLabel("Timestamp");
    timestampLabel.setFont(plain);
    c.gridx = 0;
    c.gridy = 6;
    blockPanel.add(timestampLabel, c);

    timestamp = new JTextField();
    timestamp.setEditable(false);
    timestamp.setBorder(null);
    timestamp.setFont(plain);
    c.gridx = 1;
    c.gridy = 6;
    blockPanel.add(timestamp, c);

    JLabel difficultyLabel = new JLabel("Difficulty");
    difficultyLabel.setFont(plain);
    c.gridx = 0;
    c.gridy = 7;
    blockPanel.add(difficultyLabel, c);

    difficulty = new JTextField();
    difficulty.setEditable(false);
    difficulty.setBorder(null);
    difficulty.setFont(plain);
    c.gridx = 1;
    c.gridy = 7;
    blockPanel.add(difficulty, c);

    JLabel extraDataLabel = new JLabel("Extra data");
    extraDataLabel.setFont(plain);
    c.gridx = 0;
    c.gridy = 9;
    blockPanel.add(extraDataLabel, c);

    extraData = new JTextField();
    extraData.setEditable(false);
    extraData.setBorder(null);
    extraData.setFont(plain);
    c.ipady = 1;
    c.ipadx = 1;
    c.gridx = 1;
    c.gridy = 9;
    c.gridwidth = GridBagConstraints.REMAINDER;
    blockPanel.add(extraData, c);

    JLabel hashesLabel = new JLabel("Hashes ");
    hashesLabel.setFont(bold);
    c.gridx = 3;
    c.gridy = 1;
    c.gridwidth = 1;
    blockPanel.add(hashesLabel, c);

    JLabel hashLabel = new JLabel("Hash");
    hashLabel.setFont(plain);
    c.gridx = 3;
    c.gridy = 2;
    blockPanel.add(hashLabel, c);

    hash = new JTextField();
    hash.setEditable(false);
    hash.setBorder(null);
    hash.setFont(plain);
    c.weightx = 3.0;
    c.gridx = 4;
    c.gridy = 2;
    blockPanel.add(hash, c);

    JLabel parentHashLabel = new JLabel("Parent hash");
    parentHashLabel.setFont(plain);
    c.weightx = 1.0;
    c.gridx = 3;
    c.gridy = 3;
    blockPanel.add(parentHashLabel, c);

    parentHash = new JTextField();
    parentHash.setEditable(false);
    parentHash.setBorder(null);
    parentHash.setFont(plain);
    c.gridx = 4;
    c.gridy = 3;
    blockPanel.add(parentHash, c);

    JLabel uncleHashLabel = new JLabel("Uncle hash");
    uncleHashLabel.setFont(plain);
    c.gridx = 3;
    c.gridy = 4;
    blockPanel.add(uncleHashLabel, c);

    uncleHash = new JTextField();
    uncleHash.setEditable(false);
    uncleHash.setBorder(null);
    uncleHash.setFont(plain);
    c.gridx = 4;
    c.gridy = 4;
    blockPanel.add(uncleHash, c);

    JLabel stateRootLabel = new JLabel("State root");
    stateRootLabel.setFont(plain);
    c.weightx = 1.0;
    c.gridx = 3;
    c.gridy = 5;
    blockPanel.add(stateRootLabel, c);

    stateRoot = new JTextField();
    stateRoot.setEditable(false);
    stateRoot.setBorder(null);
    stateRoot.setFont(plain);
    c.gridx = 4;
    c.gridy = 5;
    blockPanel.add(stateRoot, c);

    JLabel trieRootLabel = new JLabel("Trie root");
    trieRootLabel.setFont(plain);
    c.gridx = 3;
    c.gridy = 6;
    blockPanel.add(trieRootLabel, c);

    trieRoot = new JTextField();
    trieRoot.setEditable(false);
    trieRoot.setBorder(null);
    trieRoot.setFont(plain);
    c.gridx = 4;
    c.gridy = 6;
    blockPanel.add(trieRoot, c);

    JLabel coinbaseLabel = new JLabel("Coinbase");
    coinbaseLabel.setFont(plain);
    c.gridx = 3;
    c.gridy = 7;
    blockPanel.add(coinbaseLabel, c);

    coinbase = new JTextField();
    coinbase.setEditable(false);
    coinbase.setBorder(null);
    coinbase.setFont(plain);
    c.gridx = 4;
    c.gridy = 7;
    blockPanel.add(coinbase, c);

    JLabel nonceLabel = new JLabel("Nonce");
    nonceLabel.setFont(plain);
    c.gridx = 3;
    c.gridy = 8;
    blockPanel.add(nonceLabel, c);

    nonce = new JTextField();
    nonce.setEditable(false);
    nonce.setBorder(null);
    nonce.setFont(plain);
    c.gridx = 4;
    c.gridy = 8;
    blockPanel.add(nonce, c);
  }

  private void fillBlock(final BlockChainTable blockchainTable) {
    if (blockNumberText.getText().length() == 0) return;

    long blockNum = Long.parseLong(blockNumberText.getText());
    if (blockNum > WorldManager.getInstance().getBlockchain().getSize() - 1) {
      blockNum = WorldManager.getInstance().getBlockchain().getSize() - 1;
    }
    Block block = WorldManager.getInstance().getBlockchain().getByNumber(blockNum);
    blockn.setText("" + block.getNumber());
    highlightText(blockn);
    minGasPrice.setText("" + block.getMinGasPrice());
    highlightText(minGasPrice);
    gasLimit.setText("" + block.getGasLimit());
    highlightText(gasLimit);
    gasUsed.setText("" + block.getGasUsed());
    highlightText(gasUsed);
    timestamp.setText(Utils.longToDateTime(block.getTimestamp()));
    highlightText(timestamp);
    difficulty.setText(ByteUtil.toHexString(block.getDifficulty()));
    highlightText(difficulty);

    hash.setText(ByteUtil.toHexString(block.getHash()));
    highlightText(hash);
    parentHash.setText(ByteUtil.toHexString(block.getParentHash()));
    highlightText(parentHash);
    uncleHash.setText(ByteUtil.toHexString(block.getUnclesHash()));
    highlightText(uncleHash);
    stateRoot.setText(ByteUtil.toHexString(block.getStateRoot()));
    highlightText(stateRoot);
    trieRoot.setText(ByteUtil.toHexString(block.getTxTrieRoot()));
    highlightText(trieRoot);
    coinbase.setText(ByteUtil.toHexString(block.getCoinbase()));
    highlightText(coinbase);
    nonce.setText(ByteUtil.toHexString(block.getNonce()));
    highlightText(nonce);
    if (block.getExtraData() != null) {
      extraData.setText(ByteUtil.toHexString(block.getExtraData()));
      highlightText(extraData);
    }

    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;

    transactionsPanel.removeAll();
    transactionsPanel.repaint();

    int row = 1;
    for (Transaction transaction : block.getTransactionsList()) {
      JPanel transactionPanel = createTransactionPanel(blockchainTable, transaction);

      c.gridx = 0;
      c.gridy = row;
      c.weighty = 1;
      c.weightx = 1;
      c.anchor = GridBagConstraints.NORTHWEST;
      c.insets = new Insets(10,10,0,10);
      transactionsPanel.add(transactionPanel, c);
      row++;
    }
  }

  private JPanel createTransactionPanel(final BlockChainTable blockchainTable, final Transaction transaction) {
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;

    JPanel transactionPanel = new JPanel(new GridBagLayout());
    transactionPanel.setBorder(BorderFactory.createLineBorder(Color.black));

    JLabel senderLabel = new JLabel("Sender");
    senderLabel.setFont(plain);
    c.gridx = 0;
    c.gridy = 0;
    c.insets = new Insets(10,0,0,0);
    transactionPanel.add(senderLabel, c);

    JTextField sender = new JTextField(ByteUtil.toHexString(transaction.getSender()));
    highlightText(sender);
    sender.setEditable(false);
    sender.setBorder(null);
    sender.setFont(plain);
    c.gridx = 1;
    c.gridy = 0;
    c.insets = new Insets(0,10,0,0);
    transactionPanel.add(sender, c);

    JLabel gasPriceLabel = new JLabel("Gas price");
    gasPriceLabel.setFont(plain);
    c.gridx = 2;
    c.gridy = 0;
    c.insets = new Insets(0,10,0,0);
    transactionPanel.add(gasPriceLabel, c);

    JTextField gasPrice = new JTextField(ByteUtil.toHexString(transaction.getGasPrice()));
    highlightText(gasPrice);
    gasPrice.setEditable(false);
    gasPrice.setBorder(null);
    gasPrice.setFont(plain);
    c.gridx = 3;
    c.gridy = 0;
    c.insets = new Insets(0,10,0,0);
    transactionPanel.add(gasPrice, c);

    JLabel receiveAddressLabel = new JLabel("Receive address");
    receiveAddressLabel.setFont(plain);
    c.gridx = 0;
    c.gridy = 1;
    c.insets = new Insets(0,0,0,0);
    transactionPanel.add(receiveAddressLabel, c);

    JTextField receiveAddress = new JTextField(ByteUtil.toHexString(transaction.getReceiveAddress()));
    highlightText(receiveAddress);
    receiveAddress.setEditable(false);
    receiveAddress.setBorder(null);
    receiveAddress.setFont(plain);
    c.gridx = 1;
    c.gridy = 1;
    c.insets = new Insets(0,10,0,0);
    transactionPanel.add(receiveAddress, c);

    JLabel gasLimitLabel = new JLabel("Gas limit");
    gasLimitLabel.setFont(plain);
    c.gridx = 2;
    c.gridy = 1;
    c.insets = new Insets(0,10,0,0);
    transactionPanel.add(gasLimitLabel, c);

    JTextField gasLimit = new JTextField(ByteUtil.toHexString(transaction.getGasLimit()));
    highlightText(gasLimit);
    gasLimit.setEditable(false);
    gasLimit.setBorder(null);
    gasLimit.setFont(plain);
    c.gridx = 3;
    c.gridy = 1;
    c.insets = new Insets(0,10,0,0);
    transactionPanel.add(gasLimit, c);

    JLabel hashLabel = new JLabel("Hash");
    hashLabel.setFont(plain);
    c.gridx = 0;
    c.gridy = 2;
    c.insets = new Insets(0,0,0,0);
    transactionPanel.add(hashLabel, c);

    JTextField hash = new JTextField(ByteUtil.toHexString(transaction.getHash()));
    highlightText(hash);
    hash.setEditable(false);
    hash.setBorder(null);
    hash.setFont(plain);
    c.gridx = 1;
    c.gridy = 2;
    c.insets = new Insets(0,10,0,0);
    transactionPanel.add(hash, c);

    JLabel valueLabel = new JLabel("Value");
    valueLabel.setFont(plain);
    c.gridx = 2;
    c.gridy = 2;
    c.insets = new Insets(0,10,0,0);
    transactionPanel.add(valueLabel, c);

    JTextField value = new JTextField(transaction.getValue() != null ? ByteUtil.toHexString(transaction.getValue()) : "");
    highlightText(value);
    value.setEditable(false);
    value.setBorder(null);
    value.setFont(plain);
    c.gridx = 3;
    c.gridy = 2;
    c.insets = new Insets(0,10,0,0);
    transactionPanel.add(value, c);

    JLabel nonceLabel = new JLabel("Nonce");
    nonceLabel.setFont(plain);
    c.gridx = 0;
    c.gridy = 3;
    c.insets = new Insets(0,0,0,0);
    transactionPanel.add(nonceLabel, c);

    JTextField nonce = new JTextField(ByteUtil.toHexString(transaction.getNonce()));
    highlightText(nonce);
    nonce.setEditable(false);
    nonce.setBorder(null);
    nonce.setFont(plain);
    c.gridx = 1;
    c.gridy = 3;
    c.insets = new Insets(0,10,0,0);
    transactionPanel.add(nonce, c);

    JButton data = new JButton("Data");
    data.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        if (transactionDataWindow == null)
          transactionDataWindow = new TransactionData(blockchainTable);
        transactionDataWindow.setData(transaction.getData());
        transactionDataWindow.setVisible(true);
        transactionDataWindow.highlightText(findText.getText(), painter);
      }

    });
    data.setFont(plain);
    if (findText.getText().length() > 0 && ByteUtil.toHexString(transaction.getData()).contains(findText.getText())) {
      data.setBackground(HILIT_COLOR);
    }
    c.gridx = 3;
    c.gridy = 3;
    c.insets = new Insets(0,0,10,0);
    transactionPanel.add(data, c);

    return transactionPanel;
  }

  private void highlightText(JTextField textField) {
    if (findText.getText().length() > 0 && textField.getText().contains(findText.getText())) {
      try {
        int end = textField.getText().indexOf(findText.getText())+findText.getText().length();
        textField.getHighlighter().addHighlight(textField.getText().indexOf(findText.getText()), end, painter);
      } catch (BadLocationException e) {
      }
    }
  }

}
