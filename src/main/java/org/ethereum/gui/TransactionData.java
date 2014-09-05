package org.ethereum.gui;

import org.ethereum.util.ByteUtil;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import java.awt.*;

/**
 * www.ethereumJ.com
 *
 * @author: Adrian Benko
 * Created on: 27/08/14 18:22
 */
public class TransactionData extends JFrame {

  private JPanel topPanel;
  private JTextArea dataTextArea;

  public TransactionData(BlockChainTable blockChainTable) {
    setTitle("Data");
    setSize(400, 400);
    setLocation(350, 200);
    setBackground(Color.gray);

    java.net.URL url = ClassLoader.getSystemResource("ethereum-icon.png");
    Toolkit kit = Toolkit.getDefaultToolkit();
    Image img = kit.createImage(url);
    this.setIconImage(img);

    // Create a panel to hold all other components
    topPanel = new JPanel(new BorderLayout());
    getContentPane().add(topPanel);

    dataTextArea = new JTextArea();
    dataTextArea.setEditable(false);
    dataTextArea.setLineWrap(true);
    dataTextArea.setOpaque(false);
    dataTextArea.setWrapStyleWord(false);
    dataTextArea.setFont(BlockChainTable.plain);

    JScrollPane scrollPane = new JScrollPane(dataTextArea);
    topPanel.add(scrollPane, BorderLayout.CENTER);
  }

  public void setData(byte[] data) {
    dataTextArea.setText(ByteUtil.toHexString(data));
    topPanel.repaint();
  }

  public void highlightText(String findText, Highlighter.HighlightPainter painter) {
    if (findText.length() > 0 && dataTextArea.getText().contains(findText)) {
      int begin = dataTextArea.getText().indexOf(findText);
      int end = begin+findText.length();
      try {
        dataTextArea.getHighlighter().addHighlight(begin, end, painter);
      } catch (BadLocationException e) {
      }
      dataTextArea.setCaretPosition(end);
    }
  }

}
