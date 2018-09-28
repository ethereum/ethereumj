package io.enkrypt.kafka.models;

import java.math.BigInteger;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;

public class Account {

  private final byte[] address;
  private final BigInteger nonce;
  private final BigInteger balance;

  private byte[] rlpEncoded;

  public Account(byte[] address, BigInteger nonce, BigInteger balance) {
    this.address = address;
    this.nonce = nonce;
    this.balance = balance;
  }

  public Account(byte[] rlpData) {
    this.rlpEncoded = rlpData;

    RLPList items = (RLPList) RLP.decode2(rlpEncoded).get(0);
    this.address = items.get(0).getRLPData();
    this.nonce = ByteUtil.bytesToBigInteger(items.get(1).getRLPData());
    this.balance = ByteUtil.bytesToBigInteger(items.get(2).getRLPData());
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

  public byte[] getRLPEncoded() {
    if (rlpEncoded == null) {
      byte[] address = RLP.encodeElement(this.address);
      byte[] nonce = RLP.encodeBigInteger(this.nonce);
      byte[] balance = RLP.encodeBigInteger(this.balance);
      this.rlpEncoded = RLP.encodeList(address, nonce, balance);
    }
    return rlpEncoded;
  }

  public boolean isEmpty() {
    return BigInteger.ZERO.equals(balance) && BigInteger.ZERO.equals(nonce);
  }
}
