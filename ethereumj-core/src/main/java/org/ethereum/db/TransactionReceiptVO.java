package org.ethereum.db;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.math.BigInteger;

/**
 * www.etherj.com
 *
 * @author: Roman Mandeleil
 * Created on: 14/11/2014 09:59
 */
@Entity
@Table(name = "transaction_receipt")
public class TransactionReceiptVO {

    @Id
    byte[] hash;

    @Lob
    byte[] rlp;

    public TransactionReceiptVO() {
    }

    public TransactionReceiptVO(byte[] hash, byte[] rlp) {
        this.hash = hash;
        this.rlp = rlp;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public byte[] getRlp() {
        return rlp;
    }

    public void setRlp(byte[] rlp) {
        this.rlp = rlp;
    }

}
