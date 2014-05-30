package org.ethereum.core;

import org.ethereum.crypto.ECKey;
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
        ECKey cowKey = ECKey.fromPrivate( HashUtil.sha3("cow".getBytes()) );
        ECKey catKey = ECKey.fromPrivate( HashUtil.sha3("cat".getBytes()) );

        wallet.importKey(cowKey.getPrivKeyBytes());
        wallet.importKey(catKey.getPrivKeyBytes());


        AccountState cowAddressState = (AccountState) wallet.getAddressState(cowKey.getAddress());
        AccountState catAddressState = (AccountState) wallet.getAddressState(catKey.getAddress());

        cowAddressState.addToBalance(new BigInteger("234234"));
        catAddressState.addToBalance(new BigInteger("84758"));

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
