package org.ethereum.util.blockchain;

import org.ethereum.core.Blockchain;
import org.ethereum.crypto.ECKey;

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
     * If the soliditySrc has more than one contract the {@link #submitNewContract(String, String)}
     * method should be used. This method will generate exception in this case
     */
    SolidityContract submitNewContract(String soliditySrc);

    /**
     * Creates and sends the transaction with the Solidity contract creation code
     * The contract name is specified when the soliditySrc has more than one contract
     */
    SolidityContract submitNewContract(String soliditySrc, String contractName);

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
