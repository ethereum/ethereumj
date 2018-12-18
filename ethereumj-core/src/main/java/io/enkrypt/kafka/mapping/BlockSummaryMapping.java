package io.enkrypt.kafka.mapping;

import io.enkrypt.avro.capture.*;
import io.enkrypt.avro.common.Data20;
import io.enkrypt.avro.common.Data256;
import io.enkrypt.avro.common.Data32;
import io.enkrypt.avro.common.Data8;
import org.ethereum.core.*;
import org.ethereum.core.BlockHeader;
import org.ethereum.vm.program.InternalTransaction;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.ByteBuffer.wrap;

public class BlockSummaryMapping implements ObjectMapping {

  @Override
  public <A, B> B convert(ObjectMapping mappers, Class<A> from, Class<B> to, A value) {

    checkArgument(BlockSummary.class == from);
    checkArgument(BlockRecord.Builder.class == to);

    final BlockSummary bs = (BlockSummary) value;
    final Block b = bs.getBlock();

    final BlockRecord.Builder builder = BlockRecord.newBuilder()
      .setHeader(mappers.convert(mappers, BlockHeader.class, BlockHeaderRecord.class, b.getHeader()))
      .setTotalDifficulty(wrap(b.getCumulativeDifficulty().toByteArray()))
      .setUnclesHash(new Data32(b.getUnclesHash().clone()))
      .setRaw(wrap(b.getEncoded().clone()));

    builder.setUncles(
      b.getUncleList()
        .stream()
        .map(u -> mappers.convert(mappers, BlockHeader.class, BlockHeaderRecord.class, u))
        .collect(Collectors.toList())
    );

    builder.setTransactions(
      b.getTransactionsList()
        .stream()
        .map(t -> mappers.convert(mappers, Transaction.class, TransactionRecord.class, t))
        .collect(Collectors.toList())
    );

    final Map<ByteBuffer, TransactionExecutionSummary> execSummariesByHash = bs.getSummaries()
      .stream()
      .collect(Collectors.toMap(s -> wrap(s.getTransactionHash()), s -> s));


    builder.setTransactionReceipts(
      bs.getReceipts()
        .stream()
        .map(t -> {
          final TransactionReceiptRecord record = mappers.convert(mappers, TransactionReceipt.class, TransactionReceiptRecord.class, t);
          final TransactionExecutionSummary executionSummary = execSummariesByHash.get(wrap(record.getTransactionHash().bytes()));

          if(executionSummary == null) return record;

          return TransactionReceiptRecord.newBuilder(record)
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
              .map(it -> mappers.convert(mappers, InternalTransaction.class, InternalTransactionRecord.class, it))
              .collect(Collectors.toList())
            ).build();

        })
        .collect(Collectors.toList())
    );

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
