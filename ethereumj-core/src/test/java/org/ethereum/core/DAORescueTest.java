package org.ethereum.core;

import org.ethereum.config.Constants;
import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.config.blockchain.HomesteadConfig;
import org.ethereum.config.blockchain.HomesteadDAOConfig;
import org.ethereum.config.net.AbstractNetConfig;
import org.ethereum.config.net.MainNetConfig;
import org.ethereum.util.blockchain.SolidityContract;
import org.ethereum.util.blockchain.StandaloneBlockchain;
import org.ethereum.vm.program.Program;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

/**
 * Created by Anton Nashatyrev on 20.06.2016.
 */
public class DAORescueTest {

    private static final Constants easyMiningConst = new FrontierConfig.FrontierConstants() {
        @Override
        public BigInteger getMINIMUM_DIFFICULTY() {
            return BigInteger.ONE;
        }
    };

    private static final String daoEmulator =
            "contract TestDAO {" +
                    "function withdraw() {" +
                    "  msg.sender.call.value(10)();" +
                    "}" +
                    "function deposit() {}" +
            "}" +
            "contract DAORobber {" +
                    "function robDao(address daoAddr) {" +
                    "  TestDAO(daoAddr).withdraw();" +
                    "}" +
            "}";

    @BeforeClass
    public static void setup() throws Exception {
        SystemProperties.getDefault().setBlockchainConfig(new FrontierConfig(easyMiningConst));

        StandaloneBlockchain bc = new StandaloneBlockchain().withAutoblock(true);
        SolidityContract dao = bc.submitNewContract(daoEmulator, "TestDAO");
        final byte[] codeHash = bc.getBlockchain().getRepository().getAccountState(dao.getAddress()).getCodeHash();

        SystemProperties.getDefault().setBlockchainConfig(new AbstractNetConfig() {
            {
                add(0, new HomesteadConfig(easyMiningConst));
                add(5, new HomesteadDAOConfig(easyMiningConst, 5, 0x1_000_000_001L, codeHash));
            }
        });
    }

    @AfterClass
    public static void cleanup() {
        SystemProperties.getDefault().setBlockchainConfig(MainNetConfig.INSTANCE);
    }

    @Test
    public void testForkAgreed() {
        StandaloneBlockchain bc = new StandaloneBlockchain()
                .withAutoblock(false);
        SolidityContract dao = bc.submitNewContract(daoEmulator, "TestDAO");
        bc.createBlock(); // #1
        SolidityContract daoRobber = bc.submitNewContract(daoEmulator, "DAORobber");
        bc.sendEther(dao.getAddress(), BigInteger.valueOf(1000));
        bc.createBlock(); // #2
        Assert.assertEquals(BigInteger.valueOf(1000),
                bc.getBlockchain().getRepository().getBalance(dao.getAddress()));

        dao.callFunction("withdraw");
        daoRobber.callFunction("robDao", Hex.toHexString(dao.getAddress()));
        bc.createBlock(); // #3
        Assert.assertEquals(BigInteger.valueOf(980),
                bc.getBlockchain().getRepository().getBalance(dao.getAddress()));

        dao.callFunction("withdraw");
        daoRobber.callFunction("robDao", Hex.toHexString(dao.getAddress()));
        bc.createBlock(); // #4
        Assert.assertEquals(BigInteger.valueOf(960),
                bc.getBlockchain().getRepository().getBalance(dao.getAddress()));

        dao.callFunction("withdraw");
        bc.createBlock(); // #5 invalid
        daoRobber.callFunction("robDao", Hex.toHexString(dao.getAddress()));
        bc.createBlock(); // #6 invalid

        long balance = bc.getBlockchain().getRepository().getBalance(dao.getAddress()).longValue();
        Assert.assertEquals(960, balance);

        for (int i = 0; i < 10; i++) {
            dao.callFunction(10, "deposit");
            bc.createBlock();
            balance += 10;
            Assert.assertEquals(BigInteger.valueOf(balance),
                    bc.getBlockchain().getRepository().getBalance(dao.getAddress()));

            dao.callFunction("withdraw");
            bc.createBlock();
            daoRobber.callFunction("robDao", Hex.toHexString(dao.getAddress()));
            bc.createBlock();
            Assert.assertEquals(BigInteger.valueOf(balance),
                    bc.getBlockchain().getRepository().getBalance(dao.getAddress()));
        }
    }

    @Test
    public void testForkIgnored() {
        StandaloneBlockchain bc = new StandaloneBlockchain()
                .withAutoblock(false)
                .withBlockGasIncrease(100);
        SolidityContract dao = bc.submitNewContract(daoEmulator, "TestDAO");
        bc.createBlock(); // #1
        SolidityContract daoRobber = bc.submitNewContract(daoEmulator, "DAORobber");
        bc.sendEther(dao.getAddress(), BigInteger.valueOf(1000));
        bc.createBlock(); // #2
        Assert.assertEquals(BigInteger.valueOf(1000),
                bc.getBlockchain().getRepository().getBalance(dao.getAddress()));

        dao.callFunction("withdraw");
        daoRobber.callFunction("robDao", Hex.toHexString(dao.getAddress()));
        bc.createBlock(); // #3
        Assert.assertEquals(BigInteger.valueOf(980),
                bc.getBlockchain().getRepository().getBalance(dao.getAddress()));

        dao.callFunction("withdraw");
        daoRobber.callFunction("robDao", Hex.toHexString(dao.getAddress()));
        bc.createBlock(); // #4
        Assert.assertEquals(BigInteger.valueOf(960),
                bc.getBlockchain().getRepository().getBalance(dao.getAddress()));

        dao.callFunction("withdraw");
        daoRobber.callFunction("robDao", Hex.toHexString(dao.getAddress()));
        bc.createBlock(); // #5
        Assert.assertEquals(BigInteger.valueOf(940),
                bc.getBlockchain().getRepository().getBalance(dao.getAddress()));

        dao.callFunction("withdraw");
        daoRobber.callFunction("robDao", Hex.toHexString(dao.getAddress()));
        bc.createBlock(); // #6
        Assert.assertEquals(BigInteger.valueOf(920),
                bc.getBlockchain().getRepository().getBalance(dao.getAddress()));
    }

    @Test
    public void testForkUncertain() {
        StandaloneBlockchain bc = new StandaloneBlockchain()
                .withAutoblock(false);
        SolidityContract dao = bc.submitNewContract(daoEmulator, "TestDAO");
        bc.createBlock(); // #1
        SolidityContract daoRobber = bc.submitNewContract(daoEmulator, "DAORobber");
        bc.sendEther(dao.getAddress(), BigInteger.valueOf(1000));
        bc.createBlock(); // #2
        Assert.assertEquals(BigInteger.valueOf(1000),
                bc.getBlockchain().getRepository().getBalance(dao.getAddress()));
        bc.createBlock(); // #3
        Block b4 = bc.createBlock();// #4
        Block b5y = bc.createBlock(); // #5  (fork happened)
        bc.withBlockGasIncrease(100);
        Block b5n = bc.createForkBlock(b4); // #5' (fork rejected)
        bc.withBlockGasIncrease(0);

        dao.callFunction("withdraw");
        daoRobber.callFunction("robDao", Hex.toHexString(dao.getAddress()));
        bc.createForkBlock(b5y); // #6 invalid
        Assert.assertEquals(BigInteger.valueOf(1000),
                bc.getBlockchain().getRepository().getBalance(dao.getAddress()));

        dao.callFunction("withdraw");
        daoRobber.callFunction("robDao", Hex.toHexString(dao.getAddress()));
        Block b6n = bc.createForkBlock(b5n);// #6'
        Block b7n = bc.createForkBlock(b6n); // #7'
        Assert.assertEquals(BigInteger.valueOf(980),
                bc.getBlockchain().getRepository().getBalance(dao.getAddress()));


        Block b6 = bc.createForkBlock(b5y);// #6
        Block b7 = bc.createForkBlock(b6);// #7 (main)
        Block b8 = bc.createForkBlock(b7);// #7 (main)

        Assert.assertEquals(BigInteger.valueOf(1000),
                bc.getBlockchain().getRepository().getBalance(dao.getAddress()));
    }
}
