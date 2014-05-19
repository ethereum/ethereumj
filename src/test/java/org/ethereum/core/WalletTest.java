package org.ethereum.core;

import org.ethereum.crypto.HashUtil;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.math.BigInteger;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 17/05/14 17:06
 */
public class WalletTest {

    @Test
    public void SaveTest1() throws TransformerException, ParserConfigurationException {

        Wallet wallet = new Wallet();
        wallet.importKey(HashUtil.sha3("cow".getBytes()));
        wallet.importKey(HashUtil.sha3("cat".getBytes()));

        Address addr1 = (Address) wallet.getAddressSet().toArray()[0];
        Address addr2 = (Address) wallet.getAddressSet().toArray()[1];

        wallet.setBalance(addr1, new BigInteger("234234"));
        wallet.setBalance(addr2, new BigInteger("84758"));

        wallet.setHigh(4354);

        wallet.save();
    }

    @Test
    public void LoadTest1() throws TransformerException, ParserConfigurationException, IOException, SAXException {

        Wallet wallet = new Wallet();
        wallet.load();

        System.out.println();
    }

}
