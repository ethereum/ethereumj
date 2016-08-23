package org.ethereum.vm.program;

import org.ethereum.core.Transaction;
import org.ethereum.crypto.ECKey;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.ethereum.vm.DataWord;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import static org.apache.commons.lang3.ArrayUtils.*;
import static org.ethereum.util.ByteUtil.toHexString;

public class InternalTransaction extends Transaction {

    private byte[] parentHash;
    private int deep;
    private int index;
    private boolean rejected = false;
    private String note;

    public InternalTransaction(byte[] rawData) {
        super(rawData);
    }

    public InternalTransaction(byte[] parentHash, int deep, int index, byte[] nonce, DataWord gasPrice, DataWord gasLimit,
                               byte[] sendAddress, byte[] receiveAddress, byte[] value, byte[] data, String note) {

        super(nonce, getData(gasPrice), getData(gasLimit), receiveAddress, nullToEmpty(value), nullToEmpty(data));

        this.parentHash = parentHash;
        this.deep = deep;
        this.index = index;
        this.sendAddress = nullToEmpty(sendAddress);
        this.note = note;
        this.parsed = true;
    }

    private static byte[] getData(DataWord gasPrice) {
        return (gasPrice == null) ? ByteUtil.EMPTY_BYTE_ARRAY : gasPrice.getData();
    }

    public void reject() {
        this.rejected = true;
    }


    public int getDeep() {
        if (!parsed) rlpParse();
        return deep;
    }

    public int getIndex() {
        if (!parsed) rlpParse();
        return index;
    }

    public boolean isRejected() {
        if (!parsed) rlpParse();
        return rejected;
    }

    public String getNote() {
        if (!parsed) rlpParse();
        return note;
    }

    @Override
    public byte[] getSender() {
        if (!parsed) rlpParse();
        return sendAddress;
    }

    public byte[] getParentHash() {
        if (!parsed) rlpParse();
        return parentHash;
    }

    @Override
    public byte[] getEncoded() {
        if (rlpEncoded == null) {

            byte[] nonce = getNonce();
            boolean isEmptyNonce = isEmpty(nonce) || (getLength(nonce) == 1 && nonce[0] == 0);

            this.rlpEncoded = RLP.encodeList(
                    RLP.encodeElement(isEmptyNonce ? null : nonce),
                    RLP.encodeElement(this.parentHash),
                    RLP.encodeElement(getSender()),
                    RLP.encodeElement(getReceiveAddress()),
                    RLP.encodeElement(getValue()),
                    RLP.encodeElement(getGasPrice()),
                    RLP.encodeElement(getGasLimit()),
                    RLP.encodeElement(getData()),
                    RLP.encodeString(this.note),
                    encodeInt(this.deep),
                    encodeInt(this.index),
                    encodeInt(this.rejected ? 1 : 0)
            );
        }

        return rlpEncoded;
    }

    @Override
    public byte[] getEncodedRaw() {
        return getEncoded();
    }

    @Override
    public void rlpParse() {
        RLPList decodedTxList = RLP.decode2(rlpEncoded);
        RLPList transaction = (RLPList) decodedTxList.get(0);

        this.nonce = transaction.get(0).getRLPData();
        this.parentHash = transaction.get(1).getRLPData();
        this.sendAddress = transaction.get(2).getRLPData();
        this.receiveAddress = transaction.get(3).getRLPData();
        this.value = transaction.get(4).getRLPData();
        this.gasPrice = transaction.get(5).getRLPData();
        this.gasLimit = transaction.get(6).getRLPData();
        this.data = transaction.get(7).getRLPData();
        this.note = new String(transaction.get(8).getRLPData());
        this.deep = decodeInt(transaction.get(9).getRLPData());
        this.index = decodeInt(transaction.get(10).getRLPData());
        this.rejected = decodeInt(transaction.get(11).getRLPData()) == 1;

        this.parsed = true;
    }

    private static byte[] encodeInt(int value) {
        return RLP.encodeElement(ByteBuffer.allocate(4).putInt(value).array());
    }

    private static int decodeInt(byte[] encoded) {
        return isEmpty(encoded) ? 0 : new BigInteger(encoded).intValue();
    }

    @Override
    public ECKey getKey() {
        throw new UnsupportedOperationException("Cannot sign internal transaction.");
    }

    @Override
    public void sign(byte[] privKeyBytes) throws ECKey.MissingPrivateKeyException {
        throw new UnsupportedOperationException("Cannot sign internal transaction.");
    }

    @Override
    public String toString() {
        return "TransactionData [" +
                "  parentHash=" + toHexString(getParentHash()) +
                ", hash=" + toHexString(getHash()) +
                ", nonce=" + toHexString(getNonce()) +
                ", gasPrice=" + toHexString(getGasPrice()) +
                ", gas=" + toHexString(getGasLimit()) +
                ", receiveAddress=" + toHexString(getSender()) +
                ", receiveAddress=" + toHexString(getReceiveAddress()) +
                ", value=" + toHexString(getValue()) +
                ", data=" + toHexString(getData()) +
                ", note=" + getNote() +
                ", deep=" + getDeep() +
                ", index=" + getIndex() +
                ", rejected=" + isRejected() +
                "]";
    }
}
