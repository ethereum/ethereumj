package org.ethereum.core;

import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.ethereum.vm.program.InternalTransaction;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.ethereum.util.ByteUtil.bytesToBigInteger;
import static org.ethereum.util.RLP.*;

public class BlockStatistics {

  private int totalTxs = 0;
  private int numSuccessfulTxs = 0;
  private int numFailedTxs = 0;
  private int totalInternalTxs = 0;
  private BigInteger totalGasPrice = BigInteger.ZERO;
  private BigInteger avgGasPrice = BigInteger.ZERO;
  private BigInteger totalTxsFees = BigInteger.ZERO;
  private BigInteger avgTxsFees = BigInteger.ZERO;
  private BigInteger totalDifficulty = BigInteger.ZERO;

  public BlockStatistics() {
  }

  public BlockStatistics(byte[] bytes) {
    // TODO improve decoding

    final RLPList list = RLP.unwrapList(bytes);
    this.totalTxs = decodeInt(list.get(0).getRLPData(), 0);
    this.numSuccessfulTxs = decodeInt(list.get(1).getRLPData(), 0);
    this.numFailedTxs = decodeInt(list.get(2).getRLPData(), 0);
    this.totalInternalTxs = decodeInt(list.get(3).getRLPData(), 0);
    this.totalGasPrice = decodeBigInteger(list.get(4).getRLPData(), 0);
    this.avgGasPrice = decodeBigInteger(list.get(5).getRLPData(), 0);
    this.totalTxsFees = decodeBigInteger(list.get(6).getRLPData(), 0);
    this.avgTxsFees = decodeBigInteger(list.get(7).getRLPData(), 0);
    this.totalDifficulty = decodeBigInteger(list.get(8).getRLPData(), 0);
  }

  public int getTotalTxs() {
    return totalTxs;
  }

  public BlockStatistics setTotalTxs(int totalTxs) {
    this.totalTxs = totalTxs;
    return this;
  }

  public int getNumSuccessfulTxs() {
    return numSuccessfulTxs;
  }

  public BlockStatistics setNumSuccessfulTxs(int numSuccessfulTxs) {
    this.numSuccessfulTxs = numSuccessfulTxs;
    return this;
  }

  public int getNumFailedTxs() {
    return numFailedTxs;
  }

  public BlockStatistics setNumFailedTxs(int numFailedTxs) {
    this.numFailedTxs = numFailedTxs;
    return this;
  }

  public int getTotalInternalTxs() {
    return totalInternalTxs;
  }

  public BlockStatistics setTotalInternalTxs(int totalInternalTxs) {
    this.totalInternalTxs = totalInternalTxs;
    return this;
  }

  public BigInteger getTotalGasPrice() {
    return totalGasPrice;
  }

  public BlockStatistics setTotalGasPrice(BigInteger totalGasPrice) {
    this.totalGasPrice = totalGasPrice;
    return this;
  }

  public BigInteger getAvgGasPrice() {
    return avgGasPrice;
  }

  public BlockStatistics setAvgGasPrice(BigInteger avgGasPrice) {
    this.avgGasPrice = avgGasPrice;
    return this;
  }

  public BigInteger getTotalTxsFees() {
    return totalTxsFees;
  }

  public BlockStatistics setTotalTxsFees(BigInteger totalTxsFees) {
    this.totalTxsFees = totalTxsFees;
    return this;
  }

  public BigInteger getAvgTxsFees() {
    return avgTxsFees;
  }

  public BlockStatistics setAvgTxsFees(BigInteger avgTxsFees) {
    this.avgTxsFees = avgTxsFees;
    return this;
  }

  public BigInteger getTotalDifficulty() {
    return totalDifficulty;
  }

  public BlockStatistics setTotalDifficulty(BigInteger totalDifficulty) {
    this.totalDifficulty = totalDifficulty;
    return this;
  }

  public byte[] getEncoded() {
    return encodeList(
      encodeElement(encodeInt(totalTxs)),
      encodeElement(encodeInt(numSuccessfulTxs)),
      encodeElement(encodeInt(numFailedTxs)),
      encodeElement(encodeInt(totalInternalTxs)),
      encodeElement(encodeBigInteger(totalGasPrice)),
      encodeElement(encodeBigInteger(avgGasPrice)),
      encodeElement(encodeBigInteger(totalTxsFees)),
      encodeElement(encodeBigInteger(avgTxsFees)),
      encodeElement(encodeBigInteger(totalDifficulty))
    );
  }

  public static BlockStatistics forBlock(Block block, List<TransactionReceipt> receipts, List<TransactionExecutionSummary> summaries) {

    final int totalTxs = receipts.size();
    int numSuccessfulTxs = 0;
    int numFailedTxs = 0;
    int totalInternalTxs = 0;
    BigInteger totalGasPrice = BigInteger.ZERO;
    BigInteger avgGasPrice = BigInteger.ZERO;
    BigInteger totalTxsFees = BigInteger.ZERO;
    BigInteger avgTxsFees = BigInteger.ZERO;

    final Map<byte[], TransactionExecutionSummary> summariesByTxHash = summaries
      .stream()
      .collect(Collectors.toMap(TransactionExecutionSummary::getTransactionHash, x -> x));

    for (TransactionReceipt receipt : receipts) {

      final Transaction tx = receipt.getTransaction();
      final TransactionExecutionSummary summary = summariesByTxHash.get(tx.getHash());

      if (receipt.isSuccessful()) {
        numSuccessfulTxs += 1;
      } else {
        numFailedTxs += 1;
      }

      final BigInteger gasPrice = bytesToBigInteger(tx.getGasPrice());
      final BigInteger gasUsed = bytesToBigInteger(receipt.getGasUsed());

      totalGasPrice = totalGasPrice.add(gasPrice);

      final BigInteger txFee = gasUsed.multiply(gasPrice);
      totalTxsFees = totalTxsFees.add(txFee);

      final List<InternalTransaction> internalTxs = summary.getInternalTransactions();
      if (internalTxs != null) {
        totalInternalTxs += internalTxs.size();
      }

    }

    if (totalTxs > 0) {
      final BigInteger total = BigInteger.valueOf(totalTxs);
      avgGasPrice = totalGasPrice.divide(total);
      avgTxsFees = totalTxsFees.divide(total);
    }

    return new BlockStatistics()
      .setTotalTxs(totalTxs)
      .setNumSuccessfulTxs(numSuccessfulTxs)
      .setNumFailedTxs(numFailedTxs)
      .setTotalInternalTxs(totalInternalTxs)
      .setTotalGasPrice(totalGasPrice)
      .setAvgGasPrice(avgGasPrice)
      .setTotalTxsFees(totalTxsFees)
      .setAvgTxsFees(avgTxsFees);

  }

  @Override
  public String toString() {
    return "BlockStatistics{" +
      "totalTxs=" + totalTxs +
      ", numSuccessfulTxs=" + numSuccessfulTxs +
      ", numFailedTxs=" + numFailedTxs +
      ", totalInternalTxs=" + totalInternalTxs +
      ", totalGasPrice=" + totalGasPrice +
      ", avgGasPrice=" + avgGasPrice +
      ", totalTxsFees=" + totalTxsFees +
      ", avgTxsFees=" + avgTxsFees +
      '}';
  }
}
