package org.ethereum.core;

import org.ethereum.crypto.ECKey;
import org.ethereum.wallet.AddressState;
import org.spongycastle.util.encoders.Hex;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 17/05/14 15:53
 */
public class Wallet {

    // todo: a) the values I need to keep for address state is balance & nonce & ECKey
    // todo: b) keep it to be easy accessed by the toAddress()
//    private HashMap<Address, BigInteger> rows = new HashMap<>();

    // <address, info> table for a wallet
    private HashMap<String, AddressState> rows = new HashMap<String, AddressState>();
    private long high;

    private List<WalletListener> listeners = new ArrayList<WalletListener>();

    public void addNewKey(){

        AddressState addressState = new AddressState();
        String address = Hex.toHexString(addressState.getEcKey().getAddress());
        rows.put(address, addressState);
        for (WalletListener listener : listeners) listener.valueChanged();
    }

    public void importKey(byte[] privKey){

        AddressState addressState = new AddressState(ECKey.fromPrivate(privKey));
        String address = Hex.toHexString(addressState.getEcKey().getAddress());
        rows.put(address, addressState);
        notifyListeners();
    }

    public void addListener(WalletListener walletListener){
        this.listeners.add(walletListener);
    }

    public Collection<AddressState> getAddressStateCollection(){
        return rows.values();
    }

    public AddressState getAddressState(byte[] addressBytes){

        String address = Hex.toHexString(addressBytes);
        return rows.get(address);
    }


    public BigInteger getBalance(byte[] addressBytes){
        String address = Hex.toHexString(addressBytes);
        return rows.get(address).getBalance();
    }

    public BigInteger totalBalance(){

        BigInteger sum = BigInteger.ZERO;

        for (AddressState addressState : rows.values()){
            sum = sum.add(addressState.getBalance());
        }
        return sum;
    }


    public void processBlock(Block block){

        boolean walletUpdated = false;
        // todo: proceed coinbase when you are the miner that gets an award

        List<Transaction> transactions = block.getTransactionsList();

        for (Transaction tx : transactions){


            byte[] senderAddress = tx.getSender();
            AddressState senderState =  rows.get(Hex.toHexString(senderAddress));
            if (senderState != null){
                BigInteger value = new BigInteger(tx.getValue());

                senderState.addToBalance(value.negate());

                senderState.incrementTheNonce();
                walletUpdated = true;
            }

            byte[] receiveAddress = tx.getReceiveAddress();
            AddressState receiverState =  rows.get(Hex.toHexString(receiveAddress));
            if (receiverState != null){
                receiverState.addToBalance(new BigInteger(1, tx.getValue()));
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

            // todo: complete load func
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
        high.setValue(Long.toString( this.high ));
        walletElement.setAttributeNode(high);

        int i = 0;
        for (AddressState addressState :  getAddressStateCollection()){

            Element raw = doc.createElement("raw");
            Attr id = doc.createAttribute("id");
            id.setValue(Integer.toString(i++));
            raw.setAttributeNode(id);

            Element addressE = doc.createElement("address");
            addressE.setTextContent(Hex.toHexString(addressState.getEcKey().getAddress()));

            Attr nonce = doc.createAttribute("nonce");
            nonce.setValue("0");
            addressE.setAttributeNode(nonce);

            Element privKey = doc.createElement("privkey");
            privKey.setTextContent(Hex.toHexString(addressState.getEcKey().getPrivKeyBytes()));

            Element value   = doc.createElement("value");
            value.setTextContent(addressState.getBalance().toString());

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
