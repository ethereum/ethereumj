/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.core;

import io.enkrypt.kafka.models.TokenTransfer;
import io.enkrypt.kafka.models.TokenTransferKey;
import org.ethereum.util.*;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.LogInfo;
import org.ethereum.vm.program.InternalTransaction;
import org.springframework.util.Assert;

import java.math.BigInteger;
import java.util.*;

import static java.util.Collections.*;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;
import static org.ethereum.util.BIUtil.toBI;

public class TransactionExecutionSummary {

  private Transaction tx;
  private BigInteger value = BigInteger.ZERO;
  private BigInteger gasPrice = BigInteger.ZERO;
  private BigInteger gasLimit = BigInteger.ZERO;
  private BigInteger gasUsed = BigInteger.ZERO;
  private BigInteger gasLeftover = BigInteger.ZERO;
  private BigInteger gasRefund = BigInteger.ZERO;

  private Set<DataWord> deletedAccounts = emptySet();
  private ByteArraySet touchedAccounts = new ByteArraySet();

  private List<InternalTransaction> internalTransactions = emptyList();
  private Map<DataWord, DataWord> storageDiff = emptyMap();
  private TransactionTouchedStorage touchedStorage = new TransactionTouchedStorage();

  private Map<TokenTransferKey, TokenTransfer> tokenTransfers;

  private byte[] result;
  private List<LogInfo> logs;

  private boolean failed;

  private byte[] rlpEncoded;
  private boolean parsed;

  public TransactionExecutionSummary(){
  }

  public TransactionExecutionSummary(Transaction transaction) {
    this.tx = transaction;
    this.gasLimit = toBI(transaction.getGasLimit());
    this.gasPrice = toBI(transaction.getGasPrice());
    this.value = toBI(transaction.getValue());
  }

  public TransactionExecutionSummary(byte[] rlpEncoded) {
    this.rlpEncoded = rlpEncoded;
    this.parsed = false;
  }

  public void rlpParse() {
    if (parsed) return;

    RLPList decodedTxList = RLP.decode2(rlpEncoded);
    RLPList summary = (RLPList) decodedTxList.get(0);

    this.tx = new Transaction(summary.get(0).getRLPData());
    this.value = decodeBigInteger(summary.get(1).getRLPData());
    this.gasPrice = decodeBigInteger(summary.get(2).getRLPData());
    this.gasLimit = decodeBigInteger(summary.get(3).getRLPData());
    this.gasUsed = decodeBigInteger(summary.get(4).getRLPData());
    this.gasLeftover = decodeBigInteger(summary.get(5).getRLPData());
    this.gasRefund = decodeBigInteger(summary.get(6).getRLPData());
    this.deletedAccounts = decodeDeletedAccounts((RLPList) summary.get(7));
    this.touchedAccounts = decodeTouchedAccounts((RLPList) summary.get(8));
    this.internalTransactions = decodeInternalTransactions((RLPList) summary.get(9));
    this.touchedStorage = decodeTouchedStorage(summary.get(10));
    this.tokenTransfers = decodeTokenTransfers((RLPList) summary.get(11));
    this.result = summary.get(12).getRLPData();
    this.logs = decodeLogs((RLPList) summary.get(13));

    byte[] failed = summary.get(14).getRLPData();
    this.failed = isNotEmpty(failed) && RLP.decodeInt(failed, 0) == 1;

  }

  private static BigInteger decodeBigInteger(byte[] encoded) {
    return ByteUtil.bytesToBigInteger(encoded);
  }

  public byte[] getEncoded() {
    if (rlpEncoded != null) return rlpEncoded;


    this.rlpEncoded = RLP.encodeList(
      this.tx.getEncoded(),
      RLP.encodeBigInteger(this.value),
      RLP.encodeBigInteger(this.gasPrice),
      RLP.encodeBigInteger(this.gasLimit),
      RLP.encodeBigInteger(this.gasUsed),
      RLP.encodeBigInteger(this.gasLeftover),
      RLP.encodeBigInteger(this.gasRefund),
      encodeDeletedAccounts(this.deletedAccounts),
      encodeTouchedAccounts(this.touchedAccounts),
      encodeInternalTransactions(this.internalTransactions),
      encodeTouchedStorage(this.touchedStorage),
      encodeTokenTransfers(this.tokenTransfers),
      RLP.encodeElement(this.result),
      encodeLogs(this.logs),
      RLP.encodeInt(this.failed ? 1 : 0)
    );

    return rlpEncoded;
  }

  public static byte[] encodeTouchedStorage(TransactionTouchedStorage touchedStorage) {
    Collection<TransactionTouchedStorage.Entry> entries = touchedStorage.getEntries();
    byte[][] result = new byte[entries.size()][];

    int i = 0;
    for (TransactionTouchedStorage.Entry entry : entries) {
      byte[] key = RLP.encodeElement(entry.getKey().getData());
      byte[] value = RLP.encodeElement(entry.getValue().getData());
      byte[] changed = RLP.encodeInt(entry.isChanged() ? 1 : 0);

      result[i++] = RLP.encodeList(key, value, changed);
    }

    return RLP.encodeList(result);
  }

  public static TransactionTouchedStorage decodeTouchedStorage(RLPElement encoded) {
    TransactionTouchedStorage result = new TransactionTouchedStorage();

    for (RLPElement entry : (RLPList) encoded) {
      RLPList asList = (RLPList) entry;

      DataWord key = DataWord.of(asList.get(0).getRLPData());
      DataWord value = DataWord.of(asList.get(1).getRLPData());
      byte[] changedBytes = asList.get(2).getRLPData();
      boolean changed = isNotEmpty(changedBytes) && RLP.decodeInt(changedBytes, 0) == 1;

      result.add(new TransactionTouchedStorage.Entry(key, value, changed));
    }

    return result;
  }

  private static List<LogInfo> decodeLogs(RLPList logs) {
    ArrayList<LogInfo> result = new ArrayList<>();
    for (RLPElement log : logs) {
      result.add(new LogInfo(log.getRLPData()));
    }
    return result;
  }

  private static byte[] encodeLogs(List<LogInfo> logs) {
    byte[][] result = new byte[logs.size()][];
    for (int i = 0; i < logs.size(); i++) {
      LogInfo log = logs.get(i);
      result[i] = log.getEncoded();
    }

    return RLP.encodeList(result);
  }

  private static byte[] encodeStorageDiff(Map<DataWord, DataWord> storageDiff) {
    byte[][] result = new byte[storageDiff.size()][];
    int i = 0;
    for (Map.Entry<DataWord, DataWord> entry : storageDiff.entrySet()) {
      byte[] key = RLP.encodeElement(entry.getKey().getData());
      byte[] value = RLP.encodeElement(entry.getValue().getData());
      result[i++] = RLP.encodeList(key, value);
    }
    return RLP.encodeList(result);
  }

  private static Map<DataWord, DataWord> decodeStorageDiff(RLPList storageDiff) {
    Map<DataWord, DataWord> result = new HashMap<>();
    for (RLPElement entry : storageDiff) {
      DataWord key = DataWord.of(((RLPList) entry).get(0).getRLPData());
      DataWord value = DataWord.of(((RLPList) entry).get(1).getRLPData());
      result.put(key, value);
    }
    return result;
  }

  private static byte[] encodeTokenTransfers(Map<TokenTransferKey, TokenTransfer> tokenTransfers) {

    if(tokenTransfers == null) return RLP.encodeList();  // empty list

    byte[][] result = new byte[tokenTransfers.size()][];
    int i = 0;
    for (Map.Entry<TokenTransferKey, TokenTransfer> entry : tokenTransfers.entrySet()) {
      byte[] key = RLP.encodeElement(entry.getKey().getEncoded());
      byte[] value = RLP.encodeElement(entry.getValue().getEncoded());
      result[i++] = RLP.encodeList(key, value);
    }
    return RLP.encodeList(result);
  }

  private Map<TokenTransferKey, TokenTransfer> decodeTokenTransfers(RLPList list) {
    Map<TokenTransferKey, TokenTransfer> result = new HashMap<>();
    for (RLPElement entry : list) {
      final TokenTransferKey key = new TokenTransferKey(((RLPList) entry).get(0).getRLPData());
      final TokenTransfer value = TokenTransfer.newBuilder(((RLPList) entry).get(1).getRLPData()).build();
      result.put(key, value);
    }
    return result;
  }



  private static byte[] encodeInternalTransactions(List<InternalTransaction> internalTransactions) {
    byte[][] result = new byte[internalTransactions.size()][];
    for (int i = 0; i < internalTransactions.size(); i++) {
      InternalTransaction transaction = internalTransactions.get(i);
      result[i] = transaction.getEncoded();
    }

    return RLP.encodeList(result);
  }

  private static List<InternalTransaction> decodeInternalTransactions(RLPList internalTransactions) {
    List<InternalTransaction> result = new ArrayList<>();
    for (RLPElement internalTransaction : internalTransactions) {
      result.add(new InternalTransaction(internalTransaction.getRLPData()));
    }
    return result;
  }

  private static byte[] encodeTouchedAccounts(Set<byte[]> touchedAccounts) {
    byte[][] result = new byte[touchedAccounts.size()][];
    int idx = 0;
    for (byte[] account : touchedAccounts) {
      result[idx++] = RLP.encodeElement(account);
    }
    return RLP.encodeList(result);
  }

  private static ByteArraySet decodeTouchedAccounts(RLPList touchedAccounts) {
    ByteArraySet result = new ByteArraySet();
    for (RLPElement deletedAccount : touchedAccounts) {
      result.add(deletedAccount.getRLPData());
    }
    return result;
  }

  private static byte[] encodeDeletedAccounts(Set<DataWord> deletedAccounts) {
    byte[][] result = new byte[deletedAccounts.size()][];
    int idx = 0;
    for (DataWord account : deletedAccounts) {
      result[idx++] = RLP.encodeElement(account.getData());
    }
    return RLP.encodeList(result);
  }

  private static Set<DataWord> decodeDeletedAccounts(RLPList deletedAccounts) {
    Set<DataWord> result = new HashSet<>();
    for (RLPElement deletedAccount : deletedAccounts) {
      result.add(DataWord.of(deletedAccount.getRLPData()));
    }
    return result;
  }

  public Transaction getTransaction() {
    if (!parsed) rlpParse();
    return tx;
  }

  public byte[] getTransactionHash() {
    return getTransaction().getHash();
  }

  private BigInteger calcCost(BigInteger gas) {
    return gasPrice.multiply(gas);
  }

  public BigInteger getFee() {
    if (!parsed) rlpParse();
    return calcCost(gasLimit.subtract(gasLeftover.add(gasRefund)));
  }

  public BigInteger getRefund() {
    if (!parsed) rlpParse();
    return calcCost(gasRefund);
  }

  public BigInteger getLeftover() {
    if (!parsed) rlpParse();
    return calcCost(gasLeftover);
  }

  public BigInteger getGasPrice() {
    if (!parsed) rlpParse();
    return gasPrice;
  }

  public BigInteger getGasLimit() {
    if (!parsed) rlpParse();
    return gasLimit;
  }

  public BigInteger getGasUsed() {
    if (!parsed) rlpParse();
    return gasUsed;
  }

  public BigInteger getGasLeftover() {
    if (!parsed) rlpParse();
    return gasLeftover;
  }

  public BigInteger getValue() {
    if (!parsed) rlpParse();
    return value;
  }

  public ByteArraySet getTouchedAccounts() {
    if (!parsed) rlpParse();
    return touchedAccounts;
  }

  public Set<DataWord> getDeletedAccounts() {
    if (!parsed) rlpParse();
    return deletedAccounts;
  }

  public List<InternalTransaction> getInternalTransactions() {
    if (!parsed) rlpParse();
    return internalTransactions;
  }

  @Deprecated
  /* Use getTouchedStorage().getAll() instead */
  public Map<DataWord, DataWord> getStorageDiff() {
    if (!parsed) rlpParse();
    return storageDiff;
  }

  public BigInteger getGasRefund() {
    if (!parsed) rlpParse();
    return gasRefund;
  }

  public boolean isFailed() {
    if (!parsed) rlpParse();
    return failed;
  }

  public byte[] getResult() {
    if (!parsed) rlpParse();
    return result;
  }

  public List<LogInfo> getLogs() {
    if (!parsed) rlpParse();
    return logs;
  }

  public Map<TokenTransferKey, TokenTransfer> getTokenTransfers() {
    return tokenTransfers;
  }

  public TransactionTouchedStorage getTouchedStorage() {
    return touchedStorage;
  }

  public static Builder builderFor(Transaction transaction) {
    return new Builder(transaction);
  }

  public static Builder builderFor(TransactionExecutionSummary summary){ return new Builder(summary); }

  public static class Builder {

    private final TransactionExecutionSummary summary;

    Builder(Transaction transaction) {
      Assert.notNull(transaction, "Cannot build TransactionExecutionSummary for null transaction.");
      summary = new TransactionExecutionSummary(transaction);
    }

    Builder(TransactionExecutionSummary proto) {
      summary = new TransactionExecutionSummary();

      // shallow copy

      gasUsed(proto.gasUsed)
        .gasLeftover(proto.gasLeftover)
        .gasRefund(proto.gasRefund)
        .internalTransactions(proto.internalTransactions)
        .touchedAccounts(proto.touchedAccounts)
        .deletedAccounts(proto.deletedAccounts)
        .storageDiff(proto.storageDiff)
        .logs(proto.logs)
        .result(proto.result)
        .tokenTransfers(proto.tokenTransfers);

      summary.touchedStorage = proto.touchedStorage;
      summary.failed = proto.failed;

    }


    public Builder gasUsed(BigInteger gasUsed) {
      summary.gasUsed = gasUsed;
      return this;
    }

    public Builder gasLeftover(BigInteger gasLeftover) {
      summary.gasLeftover = gasLeftover;
      return this;
    }

    public Builder gasRefund(BigInteger gasRefund) {
      summary.gasRefund = gasRefund;
      return this;
    }

    public Builder internalTransactions(List<InternalTransaction> internalTransactions) {
      summary.internalTransactions = unmodifiableList(internalTransactions);
      return this;
    }

    public Builder touchedAccounts(ByteArraySet touchedAccounts) {
      summary.touchedAccounts = new ByteArraySet();
      summary.touchedAccounts.addAll(touchedAccounts);
      return this;
    }

    public Builder deletedAccounts(Set<DataWord> deletedAccounts) {
      summary.deletedAccounts = new HashSet<>();
      summary.deletedAccounts.addAll(deletedAccounts);
      return this;
    }

    public Builder storageDiff(Map<DataWord, DataWord> storageDiff) {
      summary.storageDiff = unmodifiableMap(storageDiff);
      return this;
    }

    public Builder touchedStorage(Map<DataWord, DataWord> touched, Map<DataWord, DataWord> changed) {
      summary.touchedStorage.addReading(touched);
      summary.touchedStorage.addWriting(changed);
      return this;
    }

    public Builder markAsFailed() {
      summary.failed = true;
      return this;
    }

    public Builder logs(List<LogInfo> logs) {
      summary.logs = logs;
      return this;
    }

    public Builder result(byte[] result) {
      summary.result = result;
      return this;
    }

    public Builder tokenTransfers(Map<TokenTransferKey, TokenTransfer> transfersMap) {
      summary.tokenTransfers = transfersMap;
      return this;
    }

    public TransactionExecutionSummary build() {
      summary.parsed = true;
      if (summary.failed) {
        for (InternalTransaction transaction : summary.internalTransactions) {
          transaction.reject();
        }
      }
      return summary;
    }
  }
}
