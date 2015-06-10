package org.ethereum.db;

import java.math.BigInteger;

import javax.persistence.*;

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
    @Column(length=102400)
    byte[] rlp;

    BigInteger cumulativeDifficulty;

    public BlockVO() {
    }

    public BlockVO(Long number, byte[] hash, byte[] rlp, BigInteger cumulativeDifficulty) {
        this.number = number;
        this.hash = hash;
        this.rlp = rlp;
        this.cumulativeDifficulty = cumulativeDifficulty;
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

    public BigInteger getCumulativeDifficulty() {
        return cumulativeDifficulty;
    }

    public void setCumulativeDifficulty(BigInteger cumulativeDifficulty) {
        this.cumulativeDifficulty = cumulativeDifficulty;
    }
}
