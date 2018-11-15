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

import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.Repository;
import org.ethereum.core.Transaction;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.blockchain.SolidityCallResult;
import org.ethereum.util.blockchain.SolidityContract;
import org.ethereum.util.blockchain.StandaloneBlockchain;

import java.math.BigInteger;

/**
 * The class demonstrates usage of the StandaloneBlockchain helper class
 * which greatly simplifies Solidity contract testing on a locally created
 * blockchain
 *
 * Created by Anton Nashatyrev on 04.04.2016.
 */
public class SimpleStorageSmartContractSample {
    // Pretty simple (and probably the most expensive) Calculator
//    private static final String contractSrc =
//            "contract Calculator {" +
//            "  int public result;" +  // public field can be accessed by calling 'result' function
//            "  function add(int num) {" +
//            "    result = result + num;" +
//            "  }" +
//            "  function sub(int num) {" +
//            "    result = result - num;" +
//            "  }" +
//            "  function mul(int num) {" +
//            "    result = result * num;" +
//            "  }" +
//            "  function div(int num) {" +
//            "    result = result / num;" +
//            "  }" +
//            "  function clear() {" +
//            "    result = 0;" +
//            "  }" +
//            "}";

    private static final String simpleStorageContractSrc =
            "contract SimpleStorage {" +
            "    uint public storedData;" +
            "" +
            "    function set(uint x) public {" +
            "        storedData = x;" +
            "    }" +
            "" +
            "    function get() public view returns (uint) {" +
            "        return storedData;" +
            "    }" +
            "}";

    StandaloneBlockchain bc;

    public static void main(String[] args) throws Exception {
        SimpleStorageSmartContractSample main = new SimpleStorageSmartContractSample();
        main.simpleStorageSmartContract(args);
    }

    public void simpleStorageSmartContract(String[] args) throws Exception {
        // Creating a blockchain which generates a new block for each transaction
        // just not to call createBlock() after each call transaction
        bc = new StandaloneBlockchain().withAutoblock(true);
        System.out.println("Creating first empty block (need some time to generate DAG)...");
        // warning up the block miner just to understand how long
        // the initial miner dataset is generated
        bc.createBlock();

        System.out.println("Creating accounts: ownder, user");

        //@@ account{balance:10ether} owner;
        Account owner = new Account("smart contract owner");
        bc.sendEther(owner.getEckey().getAddress(), new BigInteger("250000000000000000"));


        //@@ account{balance:50ether} user1;
        Account user = new Account("account user1");
        bc.sendEther(user.getEckey().getAddress(), new BigInteger("500000000000000000"));

        System.out.println("Creating a contract...");
        // This compiles our Solidity contract, submits it to the blockchain
        // internally generates the block with this transaction and returns the
        // contract interface

        //@@ account{smart contract : simple_storage.sol, by:owner, balance: 0eth} simplestorage;
        bc.setSender(owner.getEckey());
        SolidityContract simpleStorageContract = bc.submitNewContract(simpleStorageContractSrc);


        bc.setSender(user.getEckey());

        System.out.println("set(123)");
        SolidityCallResult result_put = simpleStorageContract.callFunction("set", 123);

        System.out.println(result_put);


        bc.setSender(user.getEckey());

        System.out.println("get()");
        SolidityCallResult result_get = simpleStorageContract.callFunction("get");

        System.out.println(result_get);

        assertEqual(BigInteger.valueOf(123), (BigInteger)(result_get.getReturnValues()[0]));

        assertEqual(BigInteger.valueOf(123), (BigInteger) simpleStorageContract.callConstFunction("storedData")[0]);

        System.out.println("Done.");
    }

    private static void assertEqual(BigInteger n1, BigInteger n2) {
        if (!n1.equals(n2)) {
            throw new RuntimeException("Assertion failed: " + n1 + " != " + n2);
        }
    }
}

class Account {
    private String phrase;
    private byte[] senderPrivateKey;
    private ECKey eckey;

    public Account(String phrase) {
        this.phrase = phrase;
        this.senderPrivateKey = HashUtil.sha3(this.phrase.getBytes());
        this.eckey = ECKey.fromPrivate(this.senderPrivateKey);
    }

    public ECKey getEckey() {
        return eckey;
    }

    public byte[] getSenderPrivateKey() {
        return senderPrivateKey;
    }
}

