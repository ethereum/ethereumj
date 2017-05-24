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
package org.ethereum.util.blockchain;

import org.ethereum.core.Block;

/**
 * Interface to Ethereum contract compiled with Solidity with
 * respect to language function signatures encoding and
 * storage layout
 *
 * Below is Java / Solidity types mapping:
 *
 *  Input arguments Java -&gt; Solidity mapping is the following:
 *    Number, BigInteger, String (hex) -> any integer type
 *    byte[], String (hex) -&gt; bytesN, byte[]
 *    String -&gt; string
 *    Java array of the above types -&gt; Solidity dynamic array of the corresponding type
 *
 *  Output arguments Solidity -&gt; Java mapping:
 *    any integer type -&gt; BigInteger
 *    string -&gt; String
 *    bytesN, byte[] -&gt; byte[]
 *    Solidity dynamic array -&gt; Java array
 *
 * Created by Anton Nashatyrev on 23.03.2016.
 */
public interface SolidityContract extends Contract {

    /**
     * Submits the transaction which invokes the specified contract function
     * with corresponding arguments
     *
     * TODO: either return pending transaction execution result
     * or return Future which is available upon block including trnasaction
     * or combine both approaches
     * @param functionName the function name
     * @param args the arguments
     * @return The call result
     */
    SolidityCallResult callFunction(String functionName, Object ... args);

    /**
     * Submits the transaction which invokes the specified contract function
     * with corresponding arguments and sends the specified value to the contract
     * @param value the value in wei
     * @param functionName the function name
     * @param args the arguments
     * @return The call result
     */
    SolidityCallResult callFunction(long value, String functionName, Object ... args);

    /**
     * Call the function without submitting a transaction and without
     * modifying the contract state.
     * Synchronously returns function execution result
     * (see output argument mapping in class doc)
     * @param functionName the function name
     * @param args the arguments
     * @return The call result
     */
    Object[] callConstFunction(String functionName, Object ... args);

    /**
     * Call the function without submitting a transaction and without
     * modifying the contract state. The function is executed with the
     * contract state actual after including the specified block.
     *
     * Synchronously returns function execution result
     * (see output argument mapping in class doc)
     * @param callBlock The block to call on
     * @param functionName the function name
     * @param args arguments
     * @return the converted results
     */
    Object[] callConstFunction(Block callBlock, String functionName, Object... args);

    /**
     * Gets the contract function. This object can be passed as a call argument for another
     * function with a function type parameter
     * @param name the function name
     * @return the function
     */
    SolidityFunction getFunction(String name);

    /**
     * Returns the Solidity JSON ABI (Application Binary Interface)
     * @return the ABI json
     */
    String getABI();
}
