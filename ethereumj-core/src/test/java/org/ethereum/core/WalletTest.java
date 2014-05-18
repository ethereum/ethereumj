package org.ethereum.core;

import org.junit.Test;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 17/05/14 17:06
 */
public class WalletTest {

    @Test
    public void SaveTest1() throws TransformerException, ParserConfigurationException {

        Wallet wallet = new Wallet();
        wallet.save();
    }
}
