/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
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
 * This sample demonstrates how constant calls works (that is transactions which are
 * not broadcasted to network, executed locally, don't change the blockchain state and can
 * report function return values).
 * Constant calls can actually invoke contract functions which are not formally 'const'
 * i.e. which change the contract storage state, but after such calls the contract
 * storage will remain unmodified.
 *
 * As a side effect this sample shows how Java wrappers for Ethereum contracts can be
 * created and then manipulated as regular Java objects
 *
 * Created by Anton Nashatyrev on 05.02.2016.
 */
public class PriceFeedSample extends BasicSample {

    /**
     * Base class for a Ethereum Contract wrapper
     * It can be used by two ways:
     * 1. for each function specify its name and input/output formal parameters
     * 2. Pass the contract JSON ABI to the constructor and then refer the function by name only
     */
    abstract class EthereumContract {
        private final static String zeroAddr = "0000000000000000000000000000000000000000";
        private final String contractAddr;

        private CallTransaction.Contract contractFromABI = null;

        /**
         * @param contractAddr address of the target contract as a hex String
         */
        protected EthereumContract(String contractAddr) {
            this.contractAddr = contractAddr;
        }

        /**
         *  Use this variant if you have the contract ABI then you call the functions
         *  by their names only
         */
        public EthereumContract(String contractAddr, String contractABI) {
            this.contractAddr = contractAddr;
            this.contractFromABI = new CallTransaction.Contract(contractABI);
        }

        /**
         *  The main method of this demo which illustrates how to call a constant function.
         *  To identify the Solidity contract function (calculate its signature) we need :
         *  - function name
         *  - a list of function formal params how they are declared in declaration
         *  Output parameter types are required only for decoding the return values.
         *
         *  Input arguments Java -> Solidity mapping is the following:
         *    Number, BigInteger, String (hex) -> any integer type
         *    byte[], String (hex) -> bytesN, byte[]
         *    String -> string
         *    Java array of the above types -> Solidity dynamic array of the corresponding type
         *
         *  Output arguments Solidity -> Java mapping:
         *    any integer type -> BigInteger
         *    string -> String
         *    bytesN, byte[] -> byte[]
         *    Solidity dynamic array -> Java array
         */
        protected Object[] callFunction(String name, String[] inParamTypes, String[] outParamTypes, Object ... args) {
            CallTransaction.Function function = CallTransaction.Function.fromSignature(name, inParamTypes, outParamTypes);
            ProgramResult result = ethereum.callConstantFunction(contractAddr, function, args);
            return function.decodeResult(result.getHReturn());
        }

        /**
         *  Use this method if the contract ABI was passed
         */
        protected Object[] callFunction(String functionName, Object ... args) {
            if (contractFromABI == null) {
                throw new RuntimeException("The contract JSON ABI should be passed to constructor to use this method");
            }
            CallTransaction.Function function = contractFromABI.getByName(functionName);
            ProgramResult result = ethereum.callConstantFunction(contractAddr, function, args);
            return function.decodeResult(result.getHReturn());
        }

        /**
         * Checks if the contract exist in the repository
         */
        public boolean isExist() {
            return !contractAddr.equals(zeroAddr) && ethereum.getRepository().isExist(Hex.decode(contractAddr));
        }
    }

    /**
     * NameReg contract which manages a registry of contracts which can be accessed by name
     *
     * Here we resolve contract functions by specifying their name and input/output types
     *
     * Contract sources, live state and many more here:
     * https://live.ether.camp/account/985509582b2c38010bfaa3c8d2be60022d3d00da
     */
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

    /**
     * PriceFeed contract where prices for several securities are stored and updated periodically
     *
     * This contract is created using its JSON ABI representation
     *
     * Contract sources, live state and many more here:
     * https://live.ether.camp/account/1194e966965418c7d73a42cceeb254d875860356
     */
    class PriceFeedContract extends EthereumContract {

        private static final String contractABI =
                "[{" +
                "  'constant': true," +
                "  'inputs': [{" +
                "    'name': 'symbol'," +
                "    'type': 'bytes32'" +
                "  }]," +
                "  'name': 'getPrice'," +
                "  'outputs': [{" +
                "    'name': 'currPrice'," +
                "    'type': 'uint256'" +
                "  }]," +
                "  'type': 'function'" +
                "}, {" +
                "  'constant': true," +
                "  'inputs': [{" +
                "    'name': 'symbol'," +
                "    'type': 'bytes32'" +
                "  }]," +
                "  'name': 'getTimestamp'," +
                "  'outputs': [{" +
                "    'name': 'timestamp'," +
                "    'type': 'uint256'" +
                "  }]," +
                "  'type': 'function'" +
                "}, {" +
                "  'constant': true," +
                "  'inputs': []," +
                "  'name': 'updateTime'," +
                "  'outputs': [{" +
                "    'name': ''," +
                "    'type': 'uint256'" +
                "  }]," +
                "  'type': 'function'" +
                "}, {" +
                "  'inputs': []," +
                "  'type': 'constructor'" +
                "}]";

        public PriceFeedContract(String contractAddr) {
            super(contractAddr, contractABI.replace("'", "\"")); //JSON parser doesn't like single quotes :(
        }

        public Date updateTime() {
            BigInteger ret = (BigInteger) callFunction("updateTime")[0];
            // All times in Ethereum are Unix times
            return new Date(Utils.fromUnixTime(ret.longValue()));
        }

        public double getPrice(String ticker) {
            BigInteger ret = (BigInteger) callFunction("getPrice", ticker)[0];
            // since Ethereum has no decimal numbers we are storing prices with
            // virtual fixed point
            return ret.longValue() / 1_000_000d;
        }

        public Date getTimestamp(String ticker) {
            BigInteger ret = (BigInteger) callFunction("getTimestamp", ticker)[0];
            return new Date(Utils.fromUnixTime(ret.longValue()));
        }
    }


    @Override
    public void onSyncDone() {
        try {
            // after all blocks are synced perform the work
//            worker();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void waitForDiscovery() throws Exception {
        super.waitForDiscovery();
        worker();
    }

    /**
     * The method retrieves the information from the PriceFeed contract once in a minute and prints
     * the result in log.
     */
    private void worker() throws Exception{
        NameRegContract nameRegContract = new NameRegContract();
        if (!nameRegContract.isExist()) {
            throw new RuntimeException("Namereg contract not exist on the blockchain");
        }
        String priceFeedAddress = Hex.toHexString(nameRegContract.addressOf("ether-camp/price-feed"));
        logger.info("Got PriceFeed address from name registry: " + priceFeedAddress);
        PriceFeedContract priceFeedContract = new PriceFeedContract(priceFeedAddress);

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
                logger.info("PriceFeed contract not exist. Likely it was not yet created until current block");
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

        // Based on Config class the sample would be created by Spring
        // and its springInit() method would be called as an entry point
        EthereumFactory.createEthereum(Config.class);
    }
}
