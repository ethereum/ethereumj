package org.ethereum.samples;

import org.ethereum.core.CallTransaction;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.Utils;
import org.ethereum.vm.program.ProgramResult;
import org.spongycastle.util.encoders.Hex;
import org.springframework.context.annotation.Bean;

import java.math.BigInteger;
import java.util.Date;

/**
 * Created by Anton Nashatyrev on 05.02.2016.
 */
public class PriceFeedSample extends BasicSample {


    abstract class EthereumContract {
        private final static String zeroAddr = "0000000000000000000000000000000000000000";
        private final String contractAddr;

        protected EthereumContract(String contractAddr) {
            this.contractAddr = contractAddr;
        }

        protected Object[] callFunction(String name, String[] inParams, String[] outParams, Object ... args) {
            CallTransaction.Function function = CallTransaction.Function.fromSignature(name, inParams, outParams);
            ProgramResult result = ethereum.callConstantFunction(contractAddr, function, args);
            return function.decodeResult(result.getHReturn());
        }

        public boolean isExist() {
            return !contractAddr.equals(zeroAddr) && ethereum.getRepository().isExist(Hex.decode(contractAddr));
        }
    }

    // https://github.com/ether-camp/contracts/blob/master/name_reg.sol
    class NameRegContract extends EthereumContract {

        public NameRegContract() {
            super("985509582b2c38010bfaa3c8d2be60022d3d00da");
        }

        public byte[] addressOf(String name) {
            BigInteger bi = (BigInteger) callFunction("addressOf", new String[] {"bytes32"}, new String[] {"address"}, name)[0];
            return ByteUtil.bigIntegerToBytes(bi, 20);
        }

        public String nameOf(byte[] addr) {
            return (String) callFunction("nameOf", new String[]{"address"}, new String[]{"bytes32"}, addr)[0];
        }
    }

    // https://github.com/ether-camp/contracts/blob/master/PriceFeed.sol
    class PriceFeedContract extends EthereumContract {

        public PriceFeedContract(String contractAddr) {
            super(contractAddr);
        }

        public Date updateTime() {
            BigInteger ret = (BigInteger) callFunction("updateTime", new String[] {}, new String[] {"uint"})[0];
            return new Date(Utils.fromUnixTime(ret.longValue()));
        }

        public double getPrice(String ticker) {
            BigInteger ret = (BigInteger) callFunction("getPrice", new String[] {"bytes32"}, new String[] {"uint"}, ticker)[0];
            return ret.longValue() / 1_000_000d;
        }

        public Date getTimestamp(String ticker) {
            BigInteger ret = (BigInteger) callFunction("getTimestamp", new String[] {"bytes32"}, new String[] {"uint"}, ticker)[0];
            return new Date(Utils.fromUnixTime(ret.longValue()));
        }
    }


    @Override
    public void onSyncDone() {
        try {
            worker();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    @Override
//    protected void waitForFirstBlock() throws Exception {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    worker();
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }).start();
//        super.waitForFirstBlock();
//    }

    private void worker() throws Exception{
        NameRegContract nameRegContract = new NameRegContract();
        String priceFeedAddress = Hex.toHexString(nameRegContract.addressOf("ether-camp/price-feed_"));
        logger.info("Got PriceFeed address from name registry: " + priceFeedAddress);
        PriceFeedContract priceFeedContract = new PriceFeedContract(priceFeedAddress);
        Date updateTime = priceFeedContract.updateTime();
        logger.info("Got last PriceFeed update time: " + updateTime);

        logger.info("Polling cryptocurrency exchange rates once a minute (prices are normally updated each 10 mins)...");
        String[] tickers = {"BTC_ETH", "USDT_BTC", "USDT_ETH"};
        while(true) {
            if (priceFeedContract.isExist()) {
                String s = priceFeedContract.updateTime() + ": ";
                for (String ticker : tickers) {
                    s += ticker + " " + priceFeedContract.getPrice(ticker) + " (" + priceFeedContract.getTimestamp(ticker) + "), ";
                }
                logger.info(s);
            } else {
                logger.info("PriceFeed contract not exist. Likely it was not yet created until current block: " + bestBlock.getShortDescr());
            }
            Thread.sleep(60 * 1000);
        }
    }

    private static class Config {
        @Bean
        public PriceFeedSample priceFeedSample() {
            return new PriceFeedSample();
        }
    }


    public static void main(String[] args) throws Exception {
        sLogger.info("Starting EthereumJ!");

        // Based on Config class the BasicSample would be created by Spring
        // and its springInit() method would be called as an entry point
        EthereumFactory.createEthereum(Config.class);
    }
}
