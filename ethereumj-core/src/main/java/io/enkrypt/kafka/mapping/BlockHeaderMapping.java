package io.enkrypt.kafka.mapping;

import io.enkrypt.avro.capture.BlockHeaderRecord;
import io.enkrypt.avro.capture.BlockRecord;
import io.enkrypt.avro.capture.TransactionRecord;
import io.enkrypt.avro.common.Data20;
import io.enkrypt.avro.common.Data256;
import io.enkrypt.avro.common.Data32;
import io.enkrypt.avro.common.Data8;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.BlockSummary;
import org.ethereum.core.Transaction;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.ByteBuffer.wrap;

public class BlockHeaderMapping implements ObjectMapping {

  @Override
  public <A, B> B convert(ObjectMapping mappers, Class<A> from, Class<B> to, A value) {

    checkArgument(BlockHeader.class == from);
    checkArgument(BlockHeaderRecord.class == to);

    final BlockHeader h = (BlockHeader) value;

    final BlockHeaderRecord.Builder builder = BlockHeaderRecord.newBuilder()
      .setNumber(wrap(BigInteger.valueOf(h.getNumber()).toByteArray()))
      .setHash(new Data32(h.getHash()))
      .setParentHash(new Data32(h.getParentHash()))
      .setNonce(new Data8(h.getNonce()))
      .setSha3Uncles(new Data32(h.getUnclesHash()))
      .setLogsBloom(new Data256(h.getLogsBloom()))
      .setTransactionsRoot(new Data32(h.getTxTrieRoot()))
      .setStateRoot(new Data32(h.getStateRoot()))
      .setReceiptsRoot(new Data32(h.getReceiptsRoot()))
      .setAuthor(new Data20(h.getCoinbase()))
      .setDifficulty(wrap(h.getDifficulty()))
      .setGasLimit(wrap(h.getGasLimit()))
      .setGasUsed(wrap(BigInteger.valueOf(h.getGasUsed()).toByteArray()))
      .setTimestamp(h.getTimestamp())
      .setRaw(wrap(h.getEncoded()));

    if(h.getExtraData() != null) builder.setExtraData(wrap(h.getExtraData()));

    return to.cast(builder.build());
  }
}
