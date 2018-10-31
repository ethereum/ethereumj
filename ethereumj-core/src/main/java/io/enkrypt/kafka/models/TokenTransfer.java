package io.enkrypt.kafka.models;

import org.ethereum.util.RLPList;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

import static org.ethereum.util.ByteUtil.*;
import static org.ethereum.util.RLP.*;

public class TokenTransfer {

  private byte[] address;

  private byte[] from;

  private byte[] to;

  private BigInteger fromBalance;

  private BigInteger toBalance;

  private BigInteger value;

  private BigInteger tokenId;

  public TokenTransfer() {
  }

  private TokenTransfer(TokenTransfer proto) {
    this.address = proto.address;
    this.from = proto.from;
    this.to = proto.to;
    this.fromBalance = proto.fromBalance;
    this.toBalance = proto.toBalance;
    this.value = proto.value;
    this.tokenId = proto.tokenId;
  }

  public byte[] getAddress() { return address; }

  public byte[] getFrom() {
    return from;
  }

  public byte[] getTo() {
    return to;
  }

  public BigInteger getFromBalance() {
    return fromBalance;
  }

  public BigInteger getToBalance() {
    return toBalance;
  }

  public BigInteger getValue() {
    return value;
  }

  public BigInteger getTokenId() {
    return tokenId;
  }

  public byte[] getEncoded() {

    return encodeList(
      encodeElement(address),
      encodeElement(from),
      encodeElement(to),
      encodeElement(bigIntegerToBytes(fromBalance)),
      encodeElement(bigIntegerToBytes(toBalance)),
      encodeElement(bigIntegerToBytes(value)),
      encodeElement(bigIntegerToBytes(tokenId))
    );

  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("TokenTransfer{");
    sb.append("address=").append(toHexString(address));
    sb.append(", from=").append(toHexString(from));
    sb.append(", to=").append(toHexString(to));
    sb.append(", fromBalance=").append(fromBalance);
    sb.append(", toBalance=").append(toBalance);
    sb.append(", value=").append(value);
    sb.append(", tokenId=").append(tokenId);
    sb.append('}');
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TokenTransfer that = (TokenTransfer) o;
    return Arrays.equals(address, that.address) &&
      Arrays.equals(from, that.from) &&
      Objects.equals(fromBalance, that.fromBalance) &&
      Arrays.equals(to, that.to) &&
      Objects.equals(toBalance, that.toBalance) &&
      Objects.equals(value, that.value) &&
      Objects.equals(tokenId, that.tokenId);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(fromBalance, toBalance, value, tokenId);
    result = 31 * result + Arrays.hashCode(address);
    result = 31 * result + Arrays.hashCode(from);
    result = 31 * result + Arrays.hashCode(to);
    return result;
  }

  public static Builder newBuilder(){
    return new Builder();
  }

  public static Builder newBuilder(byte[] rlpEncoded) {
    final Builder builder = newBuilder();

    final RLPList list = unwrapList(rlpEncoded);

    builder
      .setAddress(list.get(0).getRLPData())
      .setFrom(list.get(1).getRLPData())
      .setTo(list.get(2).getRLPData());

    final byte[] fromBalance = list.get(3).getRLPData();
    if(fromBalance != null) builder.setFromBalance(bytesToBigInteger(fromBalance));

    final byte[] toBalance = list.get(4).getRLPData();
    if(toBalance != null) builder.setToBalance(bytesToBigInteger(toBalance));

    final byte[] value = list.get(5).getRLPData();
    if(value != null) builder.setValue(bytesToBigInteger(value));

    final byte[] tokenId = list.get(6).getRLPData();
    if(tokenId != null) builder.setTokenId(bytesToBigInteger(tokenId));

    return builder;
  }

  public static final class Builder {

    private final TokenTransfer proto;

    private Builder() {
      this.proto = new TokenTransfer();
    }

    public byte[] getAddress() {
      return proto.getAddress();
    }

    public Builder setAddress(byte[] address) {
      proto.address = address;
      return this;
    }

    public byte[] getFrom(){
      return proto.from;
    }

    public Builder setFrom(byte[] from) {
      proto.from = from;
      return this;
    }

    public byte[] getTo(){
      return proto.to;
    }

    public Builder setTo(byte[] to) {
      proto.to = to;
      return this;
    }

    public BigInteger getFromBalance() {
      return proto.getFromBalance();
    }

    public Builder setFromBalance(BigInteger balance){
      proto.fromBalance = balance;
      return this;
    }

    public BigInteger getToBalance() {
      return proto.getToBalance();
    }

    public Builder setToBalance(BigInteger balance) {
      proto.toBalance = balance;
      return this;
    }

    public BigInteger getValue(){
      return proto.value;
    }

    public Builder setValue(BigInteger value) {
      proto.value = value;
      return this;
    }

    public BigInteger getTokenId() {
      return proto.tokenId;
    }

    public Builder setTokenId(BigInteger tokenId) {
      proto.tokenId = tokenId;
      return this;
    }

    public TokenTransfer build(){
      return new TokenTransfer(proto);
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("Builder{");
      sb.append("proto=").append(proto);
      sb.append('}');
      return sb.toString();
    }
  }
}
