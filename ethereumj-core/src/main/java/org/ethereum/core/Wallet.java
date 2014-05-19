package org.ethereum.core;

import org.spongycastle.util.encoders.Hex;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

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
import java.io.IOException;
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

    private HashMap<Address, BigInteger> rows = new HashMap<>();
    private List<WalletListener> listeners = new ArrayList();
    private long high;

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

        for (int i = 0; i <  rowNodes.getLength(); ++i ){

            Node rowNode   = rowNodes.item(i);
            Node addrNode  = rowNode.getChildNodes().item(0);
            Node privNode  = rowNode.getChildNodes().item(1);
            Node valueNode = rowNode.getChildNodes().item(2);

            byte[] privKey = Hex.decode(privNode.getTextContent());
            Address address = new Address(privKey);
            BigInteger value = new BigInteger(valueNode.getTextContent());

            this.importKey(privKey);
            this.setBalance(address, value);
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
        high.setValue(Long.toString( this.high ));
        walletElement.setAttributeNode(high);

        int i = 0;
        for (Address address :  getAddressSet()){

            Element raw = doc.createElement("raw");
            Attr id = doc.createAttribute("id");
            id.setValue(Integer.toString(i++));
            raw.setAttributeNode(id);

            Element addressE = doc.createElement("address");
            addressE.setTextContent(Hex.toHexString(address.getPubKey()));

            Attr nonce = doc.createAttribute("nonce");
            nonce.setValue("0");
            addressE.setAttributeNode(nonce);

            Element privKey = doc.createElement("privkey");
            privKey.setTextContent(Hex.toHexString(address.getPrivKey()));

            Element value   = doc.createElement("value");
            value.setTextContent(getBalance(address).toString());

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

    private void notifyListeners(){
        for (WalletListener listener : listeners) listener.valueChanged();
    }

    public interface WalletListener{
        public void valueChanged();
    }


    public long getHigh() {
        return high;
    }

    public void setHigh(long high) {
        this.high = high;
    }
}
