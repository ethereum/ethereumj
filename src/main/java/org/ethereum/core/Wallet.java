package org.ethereum.core;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 17/05/14 15:53
 */
public class Wallet {

    HashMap<Address, BigInteger> rows = new HashMap<>();
    List<WalletListener> listeners = new ArrayList();
    long high;

    public void addNewKey(){
        Address address = new Address();
        rows.put(address, BigInteger.ZERO);

        for (WalletListener listener : listeners) listener.valueChanged();
    }

    public void importKey(byte[] privKey){
        Address address = new Address(privKey);
        rows.put(address, BigInteger.ZERO);
        notifyListeners();
    }

    public void addListener(WalletListener walletListener){
        this.listeners.add(walletListener);
    }

    public Set<Address> getAddressSet(){
        return rows.keySet();
    }


    public BigInteger setBalance(Address address, BigInteger balance){
        return rows.put(address, balance);
    }

    public BigInteger getBalance(Address address){
        return rows.get(address);
    }

    public BigInteger totalBalance(){

        BigInteger sum = BigInteger.ZERO;

        for (BigInteger value : rows.values()){
            sum = sum.add(value);
        }
        return sum;
    }


    public void processBlock(Block block){

        boolean walletUpdated = false;
        // todo: proceed coinbase when you are the miner that gets an award

        List<Transaction> transactions = block.getTransactionsList();

        for (Transaction tx : transactions){

            byte[] pubKey = tx.getReceiveAddress();
            Address receiveAddress = new Address(null, pubKey);
            BigInteger balance = getBalance(receiveAddress);

            if (balance != null){

                // todo: validate the transaction and decrypt the sender
                setBalance(receiveAddress, balance.add(new BigInteger(tx.getValue())));
                walletUpdated = true;
            }
        }

        this.high = block.getNumber();
        if (walletUpdated) notifyListeners();
    }

    /**
     * Load wallet file from the disk
     */
    public void load() {

    }

    /**
     * Save wallet file to the disk
     */
    public void save() throws ParserConfigurationException, ParserConfigurationException, TransformerException {

        /**

         <wallet high="8933">
           <row id=1>
                      <address>7c63d6d8b6a4c1ec67766ae123637ca93c199935<address/>
                      <privkey>roman<privkey/>
                      <value>20000000<value/>
           </row>
           <row id=2>
                      <address>b5da3e0ba57da04f94793d1c334e476e7ce7b873<address/>
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
        high.setValue("2345");
        walletElement.setAttributeNode(high);

        // staff elements
        Element raw = doc.createElement("raw");
        Attr id = doc.createAttribute("id");
        id.setValue("1");
        raw.setAttributeNode(id);

        Element address = doc.createElement("address");
        address.setTextContent("732f3b4b6cf31f5d14fed3a5f24f6e90ae6db2cc");

        Element privKey = doc.createElement("privkey");
        privKey.setTextContent("caw");

        Element value   = doc.createElement("value");
        value.setTextContent("200000000000000");

        raw.appendChild(address);
        raw.appendChild(privKey);
        raw.appendChild(value);

        walletElement.appendChild(raw);

        // write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(fileName));

        // Output to console for testing
        // StreamResult result = new StreamResult(System.out);

        transformer.transform(source, result);
    }

    private void notifyListeners(){
        for (WalletListener listener : listeners) listener.valueChanged();
    }

    public interface WalletListener{
        public void valueChanged();
    }
}
