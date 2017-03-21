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

import org.ethereum.core.Blockchain;
import org.ethereum.crypto.ECKey;
import org.ethereum.solidity.compiler.CompilationResult.ContractMetadata;

import java.math.BigInteger;

/**
 * Interface for easy blockchain interaction
 *
 * Created by Anton Nashatyrev on 23.03.2016.
 */
public interface EasyBlockchain {

    /**
     *  Set the current sender key which all transactions (value transfer or
     *  contract creation/invocation) will be signed with
     *  The sender should have enough balance value
     */
    void setSender(ECKey senderPrivateKey);

    /**
     * Sends the value from the current sender to the specified recipient address
     */
    void sendEther(byte[] toAddress, BigInteger weis);

    /**
     * Creates and sends the transaction with the Solidity contract creation code
     * If the soliditySrc has more than one contract the {@link #submitNewContract(String, String, Object[])}
     * method should be used. This method will generate exception in this case
     */
    SolidityContract submitNewContract(String soliditySrc, Object... constructorArgs);

    /**
     * Creates and sends the transaction with the Solidity contract creation code
     * The contract name is specified when the soliditySrc has more than one contract
     */
    SolidityContract submitNewContract(String soliditySrc, String contractName, Object... constructorArgs);

    /**
     * Creates and sends the transaction with the Solidity contract creation code from a compiled json.
     * If the soliditySrc has more than one contract the {@link #submitNewContract(String, String, Object[])}
     * method should be used. This method will generate exception in this case
     */
    SolidityContract submitNewContractFromJson(String json, Object... constructorArgs);

    /**
     * Creates and sends the transaction with the Solidity contract creation code from a compiled json.
     * The contract name is specified when the soliditySrc has more than one contract
     */
    SolidityContract submitNewContractFromJson(String json, String contractName, Object... constructorArgs);

    /**
     * Creates and sends the transaction with the Solidity contract creation code from the contractMetaData.
     */
	SolidityContract submitNewContract(ContractMetadata contractMetaData, Object... constructorArgs);

    /**
     * Creates an interface to the Solidity contract already existing on the blockchain.
     * The contract source in that case is required only as an interface
     * @param soliditySrc  Source which describes the existing contract interface
     *                     This could be an abstract contract without function implementations
     * @param contractAddress The address of the existing contract
     */
    SolidityContract createExistingContractFromSrc(String soliditySrc, byte[] contractAddress);

    /**
     * The same as the previous method with specification of the exact contract
     * in the Solidity source
     */
    SolidityContract createExistingContractFromSrc(String soliditySrc, String contractName, byte[] contractAddress);

    /**
     * Creates an interface to the Solidity contract already existing on the blockchain.
     * @param ABI  Contract JSON ABI string
     * @param contractAddress The address of the existing contract
     */
    SolidityContract createExistingContractFromABI(String ABI, byte[] contractAddress);

    /**
     * Returns underlying Blockchain instance
     */
    Blockchain getBlockchain();
}
