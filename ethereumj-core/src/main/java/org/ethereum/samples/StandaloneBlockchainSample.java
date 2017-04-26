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
public class StandaloneBlockchainSample {
    // Pretty simple (and probably the most expensive) Calculator
    private static final String contractSrc =
            "contract Calculator {" +
            "  int public result;" +  // public field can be accessed by calling 'result' function
            "  function add(int num) {" +
            "    result = result + num;" +
            "  }" +
            "  function sub(int num) {" +
            "    result = result - num;" +
            "  }" +
            "  function mul(int num) {" +
            "    result = result * num;" +
            "  }" +
            "  function div(int num) {" +
            "    result = result / num;" +
            "  }" +
            "  function clear() {" +
            "    result = 0;" +
            "  }" +
            "}";

    public static void main(String[] args) throws Exception {
        // Creating a blockchain which generates a new block for each transaction
        // just not to call createBlock() after each call transaction
        StandaloneBlockchain bc = new StandaloneBlockchain().withAutoblock(true);
        System.out.println("Creating first empty block (need some time to generate DAG)...");
        // warning up the block miner just to understand how long
        // the initial miner dataset is generated
        bc.createBlock();
        System.out.println("Creating a contract...");
        // This compiles our Solidity contract, submits it to the blockchain
        // internally generates the block with this transaction and returns the
        // contract interface
        SolidityContract calc = bc.submitNewContract(contractSrc);
        System.out.println("Calculating...");
        // Creates the contract call transaction, submits it to the blockchain
        // and generates a new block which includes this transaction
        // After new block is generated the contract state is changed
        calc.callFunction("add", 100);
        // Check the contract state with a constant call which returns result
        // but doesn't generate any transactions and remain the contract state unchanged
        assertEqual(BigInteger.valueOf(100), (BigInteger) calc.callConstFunction("result")[0]);
        calc.callFunction("add", 200);
        assertEqual(BigInteger.valueOf(300), (BigInteger) calc.callConstFunction("result")[0]);
        calc.callFunction("mul", 10);
        assertEqual(BigInteger.valueOf(3000), (BigInteger) calc.callConstFunction("result")[0]);
        calc.callFunction("div", 5);
        assertEqual(BigInteger.valueOf(600), (BigInteger) calc.callConstFunction("result")[0]);
        System.out.println("Clearing...");
        calc.callFunction("clear");
        assertEqual(BigInteger.valueOf(0), (BigInteger) calc.callConstFunction("result")[0]);
        // We are done - the Solidity contract worked as expected.
        System.out.println("Done.");
    }

    private static void assertEqual(BigInteger n1, BigInteger n2) {
        if (!n1.equals(n2)) {
            throw new RuntimeException("Assertion failed: " + n1 + " != " + n2);
        }
    }
}
