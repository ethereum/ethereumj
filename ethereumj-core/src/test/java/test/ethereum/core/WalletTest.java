package test.ethereum.core;

import org.ethereum.config.SystemProperties;
import test.ethereum.TestContext;
import org.ethereum.core.Transaction;
import org.ethereum.core.Wallet;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.facade.Repository;
import org.ethereum.manager.WorldManager;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
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
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader=AnnotationConfigContextLoader.class)
public class WalletTest {

    private static final Logger logger = LoggerFactory.getLogger("test");

    @Configuration
    @ComponentScan(basePackages = "org.ethereum")
    static class ContextConfiguration extends TestContext {
        static {
            SystemProperties.CONFIG.setDataBaseDir("test_db/"+ WalletTest.class);
        }
    }

    @Autowired
    WorldManager worldManager;

    @After
    public void doReset(){
        worldManager.reset();
    }


    @Test   // Testing account for simple balance set
    public void accountTest_1(){

        Repository repository = worldManager.getRepository();

        ECKey cowKey = ECKey.fromPrivate(HashUtil.sha3("cow".getBytes()));
        repository.createAccount(cowKey.getAddress());
        repository.addBalance(cowKey.getAddress(), BigInteger.TEN);

        Wallet wallet = new Wallet();
        wallet.setWorldManager(worldManager);

        wallet.importKey(cowKey.getPrivKeyBytes());

        BigInteger walletBalance = wallet.getBalance(cowKey.getAddress());
        Assert.assertEquals(BigInteger.TEN, walletBalance);

    }


    @Test  // test account balance with pending "unblocked" transaction
    public void accountTest_2(){

        Repository repository = worldManager.getRepository();

        ECKey cowKey = ECKey.fromPrivate(HashUtil.sha3("cow".getBytes()));
        repository.createAccount(cowKey.getAddress());
        repository.addBalance(cowKey.getAddress(), BigInteger.TEN);

        Wallet wallet = new Wallet();
        wallet.setWorldManager(worldManager);

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
        wallet.setWorldManager(worldManager);

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
