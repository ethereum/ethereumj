package org.ethereum.api;

import org.ethereum.api.type.Address;
import org.ethereum.api.type.ByteArray;
import org.ethereum.api.type.EtherValue;
import org.ethereum.crypto.ECKey;

/**
 * Created by Anton Nashatyrev on 08.09.2016.
 */
public interface TransactionData {

    /*****  Builder methods  *****/

    TransactionData from(Address fromAddr);

    TransactionData from(ECKey senderPrivateKey);

    TransactionData to(Address toAddr);

    TransactionData value(EtherValue etherValue);

    TransactionData data(ByteArray data);

    TransactionData gasPrice(EtherValue gasPrice);

    TransactionData gasLimit(long gasLimit);

    TransactionData nonce(long nonce);

    /*****  Shortcut methods  ******/

    TransactionData sendEther(ECKey senderPrivateKey, Address toAddr, EtherValue amount);

    TransactionData createContract(ECKey senderPrivateKey, ByteArray contractBinary);

    TransactionData callContract(ECKey senderPrivateKey, Address contractAddr, ByteArray invocationData);

    TransactionData callContractConst(Address contractAddr, ByteArray invocationData);

    /*****  Getters  *****/

    Address getFromAddress();

    ECKey getFromSigner();

    Address getToAddress();

    EtherValue getValue();

    ByteArray getData();

    EtherValue getGasPrice();

    long getGasLimit();

    long getNonce();
}
