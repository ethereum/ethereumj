package io.enkrypt.kafka.mapping;

import io.enkrypt.avro.capture.*;
import io.enkrypt.avro.common.Data20;
import io.enkrypt.avro.common.Data32;
import org.ethereum.core.*;
import org.ethereum.core.BlockHeader;
import org.ethereum.util.ByteUtil;
import org.ethereum.vm.program.InternalTransaction;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.ByteBuffer.wrap;
import static org.ethereum.util.ByteUtil.bigIntegerToBytes;

public class BlockSummaryMapping implements ObjectMapping {

  @Override
  public <A, B> B convert(Context ctx, Class<A> from, Class<B> to, A value) {

    checkArgument(BlockSummary.class == from);
    checkArgument(BlockRecord.Builder.class == to);

    final ObjectMapping mappers = ctx.mappers();

    final BlockSummary bs = (BlockSummary) value;
    final Block b = bs.getBlock();

    final BlockRecord.Builder builder = BlockRecord.newBuilder()
      .setHeader(mappers.convert(ctx, BlockHeader.class, BlockHeaderRecord.class, b.getHeader()))
      .setTotalDifficulty(wrap(b.getCumulativeDifficulty().toByteArray()))
      .setUnclesHash(new Data32(b.getUnclesHash().clone()))
      .setRaw(wrap(b.getEncoded().clone()));

    builder.setUncles(
      b.getUncleList()
        .stream()
        .map(u -> mappers.convert(ctx, BlockHeader.class, BlockHeaderRecord.class, u))
        .collect(Collectors.toList())
    );


    final List<Transaction> txs = b.getTransactionsList();
    final List<TransactionRecord> txRecords = new ArrayList<>(txs.size());

    int transactionIndex = 0;

    for (Transaction tx : txs) {

      final Context txCtx = ctx.copy();
      txCtx.set("timestamp", builder.getHeader().getTimestamp());
      txCtx.set("blockHash", builder.getHeader().getHash());
      txCtx.set("index", transactionIndex++);

      final TransactionRecord record = mappers.convert(txCtx, Transaction.class, TransactionRecord.class, tx);
      txRecords.add(record);
    }

    builder.setTransactions(txRecords);

    final Map<ByteBuffer, TransactionExecutionSummary> execSummariesByHash = bs.getSummaries()
      .stream()
      .collect(Collectors.toMap(s -> wrap(s.getTransactionHash()), s -> s));

    final List<TransactionReceipt> txReceipts = bs.getReceipts();
    final List<TransactionReceiptRecord> txReceiptRecords = new ArrayList<>(txReceipts.size());

    int receiptIndex = 0;

    for (TransactionReceipt txReceipt : txReceipts) {

      final Context receiptCtx = ctx.copy();
      receiptCtx.set("blockHash", builder.getHeader().getHash());
      receiptCtx.set("index", receiptIndex++);

      TransactionReceiptRecord record = mappers.convert(receiptCtx, TransactionReceipt.class, TransactionReceiptRecord.class, txReceipt);
      final TransactionExecutionSummary executionSummary = execSummariesByHash.get(wrap(record.getTransactionHash().bytes()));

      if (executionSummary != null) {

        record = TransactionReceiptRecord.newBuilder(record)
          // deleted accounts
          .setDeletedAccounts(
            executionSummary.getDeletedAccounts()
              .stream()
              .map(a -> new Data20(a.getLast20Bytes()))
              .collect(Collectors.toList())
          )
          // internal transactions
          .setInternalTxs(
            executionSummary
              .getInternalTransactions()
              .stream()
              .map(it -> mappers.convert(ctx, InternalTransaction.class, InternalTransactionRecord.class, it))
              .collect(Collectors.toList())
          ).build();
      }

      txReceiptRecords.add(record);
    }

    builder.setTransactionReceipts(txReceiptRecords);

    builder.setRewards(
      bs.getRewards()
        .entrySet()
        .stream()
        .map(e -> BlockRewardRecord
          .newBuilder()
          .setAddress(new Data20(e.getKey().clone()))
          .setReward(wrap(e.getValue().toByteArray()))
          .build()
        )
        .collect(Collectors.toList())
    );

    return to.cast(builder);
  }
}
