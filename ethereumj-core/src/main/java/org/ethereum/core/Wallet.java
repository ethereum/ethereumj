package org.ethereum.core;

import org.ethereum.crypto.ECKey;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.submit.WalletTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Wallet handles the management of accounts with addresses and private keys.
 * New accounts can be generated and added to the wallet and existing accounts can be queried.
 */
@Component
@DependsOn("worldManager")
public class Wallet {

    private Logger logger = LoggerFactory.getLogger("wallet");

    // TODO: a) the values I need to keep for address state is balance & nonce & ECKey
    // TODO: b) keep it to be easy accessed by the toAddress()
//    private HashMap<Address, BigInteger> rows = new HashMap<>();

    // This map of transaction designed
    // to approve the tx by external trusted peer
    private Map<String, WalletTransaction> walletTransactions = new ConcurrentHashMap<>();

    // <address, info> table for a wallet
    private Map<String, Account> rows = new HashMap<>();
    private long high;

    @Autowired
    private WorldManager worldManager;

    @Autowired
    private ApplicationContext context;

    private List<WalletListener> listeners = new ArrayList<>();

    public void setWorldManager(WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    public void addNewAccount() {
        Account account = new Account();
        account.init();
        String address = Hex.toHexString(account.getEcKey().getAddress());
        rows.put(address, account);
        for (WalletListener listener : listeners)
            listener.valueChanged();
    }

    public void importKey(byte[] privKey) {
        Account account = context.getBean(Account.class);
        account.init(ECKey.fromPrivate(privKey));
        String address = Hex.toHexString(account.getEcKey().getAddress());
        rows.put(address, account);
        notifyListeners();
    }

    public void addListener(WalletListener walletListener) {
        this.listeners.add(walletListener);
    }

    public Collection<Account> getAccountCollection() {
        return rows.values();
    }

    public BigInteger totalBalance() {
        BigInteger sum = BigInteger.ZERO;
        for (Account account : rows.values()) {
            sum = sum.add(account.getBalance());
        }
        return sum;
    }


    /**
     * The wallet will call this method once transaction been send to the network,
     * once the the GET_TRANSACTION will be answered with that particular transaction
     * it will be considered as received by the net.
     */
    public WalletTransaction addByWalletTransaction(Transaction transaction) {
        String hash = Hex.toHexString(transaction.getHash());
        WalletTransaction walletTransaction = new WalletTransaction(transaction);
        this.walletTransactions.put(hash, walletTransaction);

        return walletTransaction;
    }

    /**
     * <ol>
     * <li> the dialog put a pending transaction on the list
     * <li> the dialog send the transaction to a net
     * <li> wherever the transaction got in from the wire it will change to approve state
     * <li> only after the approve a) Wallet state changes
     * <li> after the block is received with that tx the pending been clean up
     * </ol>
     */
    public WalletTransaction addTransaction(Transaction transaction) {
        String hash = Hex.toHexString(transaction.getHash());
        logger.info("pending transaction placed hash: {}", hash);

        WalletTransaction walletTransaction = this.walletTransactions.get(hash);
        if (walletTransaction != null)
            walletTransaction.incApproved();
        else {
            walletTransaction = new WalletTransaction(transaction);
            this.walletTransactions.put(hash, walletTransaction);
        }

        this.applyTransaction(transaction);

        return walletTransaction;
    }

    public void addTransactions(List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            this.addTransaction(transaction);
        }
    }

    public void removeTransactions(List<Transaction> transactions) {
        for (Transaction tx : transactions) {
            if (logger.isDebugEnabled())
                logger.debug("pending cleanup: tx.hash: [{}]", Hex.toHexString(tx.getHash()));
            this.removeTransaction(tx);
        }
    }

    public void removeTransaction(Transaction transaction) {
        String hash = Hex.toHexString(transaction.getHash());
        logger.info("pending transaction removed with hash: {} ", hash);
        walletTransactions.remove(hash);
    }

    public void applyTransaction(Transaction transaction) {
        byte[] senderAddress = transaction.getSender();
        Account sender = rows.get(Hex.toHexString(senderAddress));
        if (sender != null) {
            sender.addPendingTransaction(transaction);

            logger.info("Pending transaction added to " +
                            "\n account: [{}], " +
                            "\n tx: [{}]",
                    Hex.toHexString(sender.getAddress()), Hex.toHexString(transaction.getHash()));
        }

        byte[] receiveAddress = transaction.getReceiveAddress();
        if (receiveAddress != null) {
            Account receiver = rows.get(Hex.toHexString(receiveAddress));
            if (receiver != null) {
                receiver.addPendingTransaction(transaction);

                logger.info("Pending transaction added to " +
                                "\n account: [{}], " +
                                "\n tx: [{}]",
                        Hex.toHexString(receiver.getAddress()), Hex.toHexString(transaction.getHash()));
            }
        }
        this.notifyListeners();
    }


    public void processBlock(Block block) {

        for (Account account : getAccountCollection()) {
            account.clearAllPendingTransactions();
        }

        notifyListeners();
    }

    /**
     * Load wallet file from the disk
     */
    public void load() throws IOException, SAXException, ParserConfigurationException {

        /**

         <wallet high="8933">
             <row id=1>
                 <address nonce="1" >7c63d6d8b6a4c1ec67766ae123637ca93c199935<address/>
                 <privkey>roman<privkey/>
                 <value>20000000<value/>
             </row>
             <row id=2>
                 <address nonce="6" >b5da3e0ba57da04f94793d1c334e476e7ce7b873<address/>
                 <privkey>cow<privkey/>
                 <value>900099909<value/>
             </row>
         </wallet>

         */

        String dir = System.getProperty("user.dir");
        String fileName = dir + "/wallet.xml";

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fileName);

        Node walletNode = doc.getElementsByTagName("wallet").item(0);
        String high = walletNode.getAttributes().getNamedItem("high").getTextContent();
        this.setHigh(Long.parseLong(high));

        NodeList rowNodes = walletNode.getChildNodes();

        for (int i = 0; i < rowNodes.getLength(); ++i) {

            Node rowNode = rowNodes.item(i);
            Node addrNode = rowNode.getChildNodes().item(0);
            Node privNode = rowNode.getChildNodes().item(1);
            Node valueNode = rowNode.getChildNodes().item(2);

            // TODO: complete load func
//            byte[] privKey = Hex.decode(privNode.getTextContent());
//            Address address = new Address(privKey);
//            BigInteger value = new BigInteger(valueNode.getTextContent());

//            this.importKey(privKey);
//            this.setBalance(address, value);
        }
    }

    /**
     * Save wallet file to the disk
     */
    public void save() throws ParserConfigurationException, ParserConfigurationException, TransformerException {

        /**

         <wallet high="8933">
           <row id=1>
                      <address nonce="1" >7c63d6d8b6a4c1ec67766ae123637ca93c199935<address/>
                      <privkey>roman<privkey/>
                      <value>20000000<value/>
           </row>
           <row id=2>
                      <address nonce="6" >b5da3e0ba57da04f94793d1c334e476e7ce7b873<address/>
                      <privkey>cow<privkey/>
                      <value>900099909<value/>
           </row>
         </wallet>

         */

        String dir = System.getProperty("user.dir");
        String fileName = dir + "/wallet.xml";

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.newDocument();
        Element walletElement = doc.createElement("wallet");
        doc.appendChild(walletElement);

        Attr high = doc.createAttribute("high");
        high.setValue(Long.toString(this.high));
        walletElement.setAttributeNode(high);

        int i = 0;
        for (Account account : getAccountCollection()) {

            Element raw = doc.createElement("raw");
            Attr id = doc.createAttribute("id");
            id.setValue(Integer.toString(i++));
            raw.setAttributeNode(id);

            Element addressE = doc.createElement("address");
            addressE.setTextContent(Hex.toHexString(account.getEcKey().getAddress()));

            Attr nonce = doc.createAttribute("nonce");
            nonce.setValue("0");
            addressE.setAttributeNode(nonce);

            Element privKey = doc.createElement("privkey");
            privKey.setTextContent(Hex.toHexString(account.getEcKey().getPrivKeyBytes()));

            Element value = doc.createElement("value");
            value.setTextContent(account.getBalance().toString());

            raw.appendChild(addressE);
            raw.appendChild(privKey);
            raw.appendChild(value);

            walletElement.appendChild(raw);
        }
        // write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(fileName));

        // Output to console for testing
        // StreamResult result = new StreamResult(System.out);

        transformer.transform(source, result);
    }

    private void notifyListeners() {
        for (WalletListener listener : listeners)
            listener.valueChanged();
    }

    public interface WalletListener {
        public void valueChanged();
    }

    public BigInteger getBalance(byte[] addressBytes) {
        String address = Hex.toHexString(addressBytes);
        return rows.get(address).getBalance();
    }

    public long getHigh() {
        return high;
    }

    public void setHigh(long high) {
        this.high = high;
    }
}
