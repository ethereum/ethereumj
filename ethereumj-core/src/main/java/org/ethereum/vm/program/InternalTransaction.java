package org.ethereum.vm.program;

import org.ethereum.core.Transaction;
import org.ethereum.crypto.ECKey;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.vm.DataWord;

import static org.apache.commons.lang3.ArrayUtils.getLength;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.ArrayUtils.nullToEmpty;

public class InternalTransaction extends Transaction {

    private byte[] parentHash;
    private int deep;
    private int index;
    private boolean rejected = false;
    private String note;

    public InternalTransaction(byte[] parentHash, int deep, int index, byte[] nonce, DataWord gasPrice, DataWord gasLimit,
                               byte[] sendAddress, byte[] receiveAddress, byte[] value, byte[] data, String note) {

        super(nonce, getData(gasPrice), getData(gasLimit), receiveAddress, nullToEmpty(value), nullToEmpty(data));

        this.parentHash = parentHash;
        this.deep = deep;
        this.index = index;
        this.sendAddress = nullToEmpty(sendAddress);
        this.note = note;
    }

    private static byte[] getData(DataWord gasPrice) {
        return (gasPrice == null) ? ByteUtil.EMPTY_BYTE_ARRAY : gasPrice.getData();
    }

    public void reject() {
        this.rejected = true;
    }


    public int getDeep() {
        return deep;
    }

    public int getIndex() {
        return index;
    }

    public boolean isRejected() {
        return rejected;
    }

    public String getNote() {
        return note;
    }

    @Override
    public byte[] getSender() {
        return sendAddress;
    }

    public byte[] getParentHash() {
        return parentHash;
    }

    @Override
    public byte[] getEncoded() {
        if (rlpEncoded != null) return rlpEncoded;

        byte[] nonce = getNonce();
        if (isEmpty(nonce) || getLength(nonce) == 1 && nonce[0] == 0) {
            nonce = RLP.encodeElement(null);
        } else {
            nonce = RLP.encodeElement(nonce);
        }
        byte[] senderAddress = RLP.encodeElement(getSender());
        byte[] receiveAddress = RLP.encodeElement(getReceiveAddress());
        byte[] value = RLP.encodeElement(getValue());
        byte[] gasPrice = RLP.encodeElement(getGasPrice());
        byte[] gasLimit = RLP.encodeElement(getGasLimit());
        byte[] data = RLP.encodeElement(getData());
        byte[] parentHash = RLP.encodeElement(this.parentHash);
        byte[] type = RLP.encodeString(this.note);
        byte[] deep = RLP.encodeInt(this.deep);
        byte[] index = RLP.encodeInt(this.index);
        byte[] rejected = RLP.encodeInt(this.rejected ? 1 : 0);

        this.rlpEncoded = RLP.encodeList(nonce, parentHash, senderAddress, receiveAddress, value,
                gasPrice, gasLimit, data, type, deep, index, rejected);

        return rlpEncoded;
    }

    @Override
    public byte[] getEncodedRaw() {
        return getEncoded();
    }

    @Override
    public void rlpParse() {
        // Internal transaction not uses as encoded data
    }

    @Override
    public ECKey getKey() {
        throw new UnsupportedOperationException("Cannot sign internal transaction.");
    }

    @Override
    public void sign(byte[] privKeyBytes) throws ECKey.MissingPrivateKeyException {
        throw new UnsupportedOperationException("Cannot sign internal transaction.");
    }
}
