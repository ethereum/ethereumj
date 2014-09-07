package org.ethereum.core;

import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.db.Repository;
import org.ethereum.manager.WorldManager;
import org.junit.*;
import org.junit.runners.MethodSorters;
import org.spongycastle.util.encoders.Hex;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import java.io.IOException;
import java.math.BigInteger;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 17/05/14 17:06
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WalletTest {

    @BeforeClass
    public static void setUp() {
        if (WorldManager.getInstance().getRepository().isClosed())
            WorldManager.getInstance().reset();
    }

    @AfterClass
    public static void tearDown() {
        WorldManager.getInstance().close();
    }

    @Test   // Testing account for simple balance set
    public void accountTest_1(){

        Repository repository = WorldManager.getInstance().getRepository();

        ECKey cowKey = ECKey.fromPrivate(HashUtil.sha3("cow".getBytes()));
        repository.createAccount(cowKey.getAddress());
        repository.addBalance(cowKey.getAddress(), BigInteger.TEN);

        Wallet wallet = new Wallet();
        wallet.importKey(cowKey.getPrivKeyBytes());

        BigInteger walletBalance = wallet.getBalance(cowKey.getAddress());
        Assert.assertEquals(BigInteger.TEN, walletBalance);

    }


    @Test  // test account balance with pending "unblocked" transaction
    public void accountTest_2(){

        Repository repository = WorldManager.getInstance().getRepository();

        ECKey cowKey = ECKey.fromPrivate(HashUtil.sha3("cow".getBytes()));
        repository.createAccount(cowKey.getAddress());
        repository.addBalance(cowKey.getAddress(), BigInteger.TEN);

        Wallet wallet = new Wallet();
        wallet.importKey(cowKey.getPrivKeyBytes());

        Transaction tx = new Transaction(
                new byte[]{},
                Hex.decode("09184E72A000"),
                Hex.decode("03E8"),
                cowKey.getAddress(),
                Hex.decode("0A"),
                new byte[]{}
        );

        ECKey catKey = ECKey.fromPrivate(HashUtil.sha3("cat".getBytes()));
        tx.sign(catKey.getPrivKeyBytes());

        wallet.applyTransaction(tx);

        BigInteger walletBalance = wallet.getBalance(cowKey.getAddress());
        Assert.assertEquals(BigInteger.valueOf(20), walletBalance);

    }


    @Test
    public void testSave1() throws TransformerException, ParserConfigurationException {

        Wallet wallet = new Wallet();
		ECKey cowKey = ECKey.fromPrivate(HashUtil.sha3("cow".getBytes()));
		ECKey catKey = ECKey.fromPrivate(HashUtil.sha3("cat".getBytes()));

        wallet.importKey(cowKey.getPrivKeyBytes());
        wallet.importKey(catKey.getPrivKeyBytes());

        wallet.setHigh(4354);

        wallet.save();
    }

    @Test
    @Ignore
	public void testLoad1() throws TransformerException,
			ParserConfigurationException, IOException, SAXException {
        Wallet wallet = new Wallet();
        wallet.load();
    }

}
