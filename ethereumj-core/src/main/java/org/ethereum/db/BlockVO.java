package org.ethereum.db;

import java.math.BigInteger;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * @author Roman Mandeleil
 * @since 14.11.2014
 */
@Entity
@Table(name = "block")
public class BlockVO {

    @Id
    byte[] hash;

    Long number;

    @Lob
    byte[] rlp;

    BigInteger cummulativeDifficulty;

    public BlockVO() {
    }

    public BlockVO(Long number, byte[] hash, byte[] rlp, BigInteger cummulativeDifficulty) {
        this.number = number;
        this.hash = hash;
        this.rlp = rlp;
        this.cummulativeDifficulty = cummulativeDifficulty;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public Long getIndex() {
        return number;
    }

    public void setIndex(Long number) {
        this.number = number;
    }

    public byte[] getRlp() {
        return rlp;
    }

    public void setRlp(byte[] rlp) {
        this.rlp = rlp;
    }

    public BigInteger getCummulativeDifficulty() {
        return cummulativeDifficulty;
    }

    public void setCummulativeDifficulty(BigInteger cummulativeDifficulty) {
        this.cummulativeDifficulty = cummulativeDifficulty;
    }
}
