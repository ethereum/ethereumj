package io.enkrypt.kafka.models;

import java.math.BigInteger;
import org.ethereum.config.BlockchainConfig;
import org.ethereum.core.AccountState;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;

import static org.ethereum.crypto.HashUtil.EMPTY_DATA_HASH;

public class Account {

  private final byte[] address;
  private final BigInteger nonce;
  private final BigInteger balance;
  private final byte[] stateRoot;
  private final byte[] codeHash;

  private byte[] rlpEncoded;

  public static Account fromAccountState(byte[] address, AccountState state) {
    return new Account(address, state.getNonce(), state.getBalance(), state.getStateRoot(), state.getCodeHash());
  }

  public Account(byte[] address, BigInteger nonce, BigInteger balance, byte[] stateRoot, byte[] codeHash) {
    this.address = address;
    this.nonce = nonce;
    this.balance = balance;
    this.stateRoot = stateRoot;
    this.codeHash = codeHash;
  }

  public Account(byte[] rlpData) {
    this.rlpEncoded = rlpData;

    RLPList items = (RLPList) RLP.decode2(rlpEncoded).get(0);
    this.address = items.get(0).getRLPData();
    this.nonce = ByteUtil.bytesToBigInteger(items.get(1).getRLPData());
    this.balance = ByteUtil.bytesToBigInteger(items.get(2).getRLPData());
    this.stateRoot = items.get(3).getRLPData();
    this.codeHash = items.get(4).getRLPData();
  }

  public byte[] getAddress() {
    return address;
  }

  public BigInteger getNonce() {
    return nonce;
  }

  public BigInteger getBalance() {
    return balance;
  }

  public byte[] getStateRoot() {
    return stateRoot;
  }

  public byte[] getCodeHash() {
    return codeHash;
  }

  public byte[] getRLPEncoded() {
    if (rlpEncoded == null) {
      byte[] address = RLP.encodeElement(this.address);
      byte[] nonce = RLP.encodeBigInteger(this.nonce);
      byte[] balance = RLP.encodeBigInteger(this.balance);
      byte[] stateRoot = RLP.encodeElement(this.stateRoot);
      byte[] codeHash = RLP.encodeElement(this.codeHash);
      this.rlpEncoded = RLP.encodeList(address, nonce, balance, stateRoot, codeHash);
    }
    return rlpEncoded;
  }

  public boolean isEmpty() {
    return FastByteComparisons.equal(codeHash, EMPTY_DATA_HASH) && BigInteger.ZERO.equals(balance) && BigInteger.ZERO.equals(nonce);
  }

  public boolean isContract() {
    return !FastByteComparisons.equal(codeHash, EMPTY_DATA_HASH);
  }
}
