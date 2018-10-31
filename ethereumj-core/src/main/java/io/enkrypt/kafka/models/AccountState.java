package io.enkrypt.kafka.models;

import org.ethereum.util.RLPList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.ethereum.util.ByteUtil.*;
import static org.ethereum.util.RLP.*;

public class AccountState {

  private byte[] stateRoot;

  private byte[] code;

  private byte[] codeHash;

  private byte[] creator;

  private Boolean miner;

  private BigInteger nonce;

  private BigInteger balance;

  private AccountState() {
  }

  private AccountState(AccountState proto) {
    this.stateRoot = proto.stateRoot;
    this.code = proto.code;
    this.codeHash = proto.codeHash;
    this.creator = proto.creator;
    this.miner = proto.miner;
    this.nonce = proto.nonce;
    this.balance = proto.balance;
  }

  public boolean isEmpty() {
    return nonce == null && balance == null && stateRoot == null && code == null && codeHash == null && creator == null && miner == null;
  }

  public byte[] getStateRoot() {
    return stateRoot;
  }

  public byte[] getCode() {
    return code;
  }

  public byte[] getCodeHash() {
    return codeHash;
  }

  public byte[] getCreator() {
    return creator;
  }

  public BigInteger getNonce() {
    return nonce;
  }

  public BigInteger getBalance() {
    return balance;
  }

  public Boolean isMiner() {
    return miner;
  }

  public byte[] getEncoded() {
    return wrapList(
      stateRoot,
      code,
      codeHash,
      creator,
      miner == null ? null : encodeInt(miner ? 1 : 0),
      bigIntegerToBytes(nonce),
      bigIntegerToBytes(balance)
    );
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("AccountState{");
    sb.append("nonce=").append(nonce);
    sb.append(", balance=").append(balance);
    sb.append(", stateRoot=").append(toHexString(stateRoot));
    sb.append(", code=").append(toHexString(code));
    sb.append(", codeHash=").append(toHexString(codeHash));
    sb.append(", creator=").append(toHexString(creator));
    sb.append(", miner=").append(miner);
    sb.append('}');
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AccountState that = (AccountState) o;
    return Arrays.equals(stateRoot, that.stateRoot) &&
      Arrays.equals(code, that.code) &&
      Arrays.equals(codeHash, that.codeHash) &&
      Arrays.equals(creator, that.creator) &&
      Objects.equals(nonce, that.nonce) &&
      Objects.equals(balance, that.balance) &&
      Objects.equals(miner, that.miner);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(nonce, balance, miner);
    result = 31 * result + Arrays.hashCode(stateRoot);
    result = 31 * result + Arrays.hashCode(code);
    result = 31 * result + Arrays.hashCode(codeHash);
    result = 31 * result + Arrays.hashCode(creator);
    return result;
  }

  public static Builder newBuilder(byte[] rlpEncoded) {
    checkNotNull(rlpEncoded, "rlpEncoded cannot be null");

    final Builder builder = new Builder();
    final RLPList l = unwrapList(rlpEncoded);

    builder
      .setStateRoot(l.get(0).getRLPData())
      .setCode(l.get(1).getRLPData())
      .setCodeHash(l.get(2).getRLPData())
      .setCreator(l.get(3).getRLPData());

    final byte[] miner = l.get(4).getRLPData();
    if (miner != null) builder.setMiner(decodeInt(miner, 0) == 1);

    final byte[] nonce = l.get(5).getRLPData();
    if (nonce != null) builder.setNonce(bytesToBigInteger(nonce));

    final byte[] balance = l.get(6).getRLPData();
    if (balance != null) builder.setBalance(bytesToBigInteger(balance));


    return builder;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static Builder newBuilder(org.ethereum.core.AccountState state) {

    final Builder builder = new Builder();

    if (state != null) {
      builder.setNonce(state.getNonce())
        .setBalance(state.getBalance())
        .setStateRoot(state.getStateRoot())
        .setCodeHash(state.getCodeHash());
    }

    return builder;
  }

  public static final class Builder {

    private final AccountState proto;

    private Builder() {
      this.proto = new AccountState();
    }

    private Builder(AccountState other) {
      this();
      proto.nonce = other.nonce;
      proto.balance = other.balance;
      proto.stateRoot = other.stateRoot;
      proto.code = other.code;
      proto.codeHash = other.codeHash;
      proto.miner = other.miner;
    }

    public Builder setNonce(BigInteger nonce) {
      proto.nonce = nonce;
      return this;
    }

    public Builder setBalance(BigInteger balance) {
      proto.balance = balance;
      return this;
    }

    public Builder setStateRoot(byte[] stateRoot) {
      proto.stateRoot = stateRoot;
      return this;
    }

    public Builder setCode(byte[] code) {
      proto.code = code;
      return this;
    }

    public Builder setCodeHash(byte[] codeHash) {
      proto.codeHash = codeHash;
      return this;
    }

    public Builder setCreator(byte[] creator) {
      proto.creator = creator;
      return this;
    }

    public Builder setMiner(Boolean miner) {
      proto.miner = miner;
      return this;
    }

    public Builder merge(AccountState other) {
      if (other.nonce != null) setNonce(other.nonce);
      if (other.balance != null) setBalance(other.balance);
      if (other.stateRoot != null) setStateRoot(other.stateRoot);
      if (other.code != null) setCode(other.code);
      if (other.codeHash != null) setCodeHash(other.codeHash);
      if (other.creator != null) setCreator(other.creator);
      if (other.miner != null) setMiner(other.miner);

      return this;
    }

    public AccountState build() {
      return new AccountState(proto);
    }

  }

}
