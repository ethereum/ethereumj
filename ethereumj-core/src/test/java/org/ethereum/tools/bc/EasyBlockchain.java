package org.ethereum.tools.bc;

import org.ethereum.core.Blockchain;
import org.ethereum.crypto.ECKey;

import java.math.BigInteger;

/**
 * Created by Anton Nashatyrev on 23.03.2016.
 */
public interface EasyBlockchain {

    void setSender(ECKey senderPrivateKey);

    void sendEther(byte[] toAddress, BigInteger weis);

    SolidityContract submitNewContract(String soliditySrc);
    SolidityContract submitNewContract(String soliditySrc, String contractName);
    SolidityContract createExistingContractFromSrc(String soliditySrc, byte[] contractAddress);
    SolidityContract createExistingContractFromSrc(String soliditySrc, String contractName, byte[] contractAddress);
    SolidityContract createExistingContractFromABI(String ABI, byte[] contractAddress);

    Blockchain getBlockchain();
}
