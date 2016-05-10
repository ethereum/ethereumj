package org.ethereum.db;

import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.config.net.MainNetConfig;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionInfo;
import org.ethereum.datasource.CachingDataSource;
import org.ethereum.datasource.HashMapDB;
import org.ethereum.util.blockchain.SolidityContract;
import org.ethereum.util.blockchain.StandaloneBlockchain;
import org.ethereum.vm.DataWord;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;

/**
 * Created by Anton Nashatyrev on 08.04.2016.
 */
public class TransactionStoreTest {

    private static SystemProperties config;

    @BeforeClass
    public static void setup() {
        config = SystemProperties.getDefault();
        config.setBlockchainConfig(new FrontierConfig(new FrontierConfig.FrontierConstants() {
            @Override
            public BigInteger getMINIMUM_DIFFICULTY() {
                return BigInteger.ONE;
            }
        }));
    }

    @AfterClass
    public static void cleanup() {
        config.setBlockchainConfig(MainNetConfig.INSTANCE);
    }

    @Test
    public void simpleTest() {
        String contractSrc =
                "contract Adder {" +
                "  function add(int a, int b) returns (int) {return a + b;}" +
                "}";
        HashMapDB txDb = new HashMapDB();

        StandaloneBlockchain bc = new StandaloneBlockchain(config);
        bc.getBlockchain().withTransactionStore(new TransactionStore(new CachingDataSource(txDb)));
        SolidityContract contract = bc.submitNewContract(contractSrc);
        bc.createBlock();
        contract.callFunction("add", 555, 222);
        Block b2 = bc.createBlock();
        contract.callFunction("add", 333, 333);
        Block b3 = bc.createBlock();
        Transaction tx1 = b2.getTransactionsList().get(0);
        TransactionInfo tx1Info = bc.getBlockchain().getTransactionInfo(tx1.getHash());
        byte[] executionResult = tx1Info.getReceipt().getExecutionResult();
        Assert.assertArrayEquals(new DataWord(777).getData(), executionResult);

        System.out.println(txDb.keys().size());
        bc.getBlockchain().flush();
        System.out.println(txDb.keys().size());

        TransactionStore txStore = new TransactionStore(txDb);
        TransactionInfo tx1Info_ = txStore.get(tx1.getHash());
        executionResult = tx1Info_.getReceipt().getExecutionResult();
        Assert.assertArrayEquals(new DataWord(777).getData(), executionResult);

        TransactionInfo highIndex = new TransactionInfo(tx1Info.getReceipt(), tx1Info.getBlockHash(), 255);
        TransactionInfo highIndexCopy = new TransactionInfo(highIndex.getEncoded());
        Assert.assertArrayEquals(highIndex.getBlockHash(), highIndexCopy.getBlockHash());
        Assert.assertEquals(highIndex.getIndex(), highIndexCopy.getIndex());
    }
}
