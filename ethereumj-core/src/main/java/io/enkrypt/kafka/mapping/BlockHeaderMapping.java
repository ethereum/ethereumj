package io.enkrypt.kafka.mapping;

import io.enkrypt.avro.capture.BlockHeaderRecord;
import io.enkrypt.avro.common.Data20;
import io.enkrypt.avro.common.Data256;
import io.enkrypt.avro.common.Data32;
import io.enkrypt.avro.common.Data8;
import org.ethereum.core.BlockHeader;

import java.math.BigInteger;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.ByteBuffer.wrap;

public class BlockHeaderMapping implements ObjectMapping {

  @Override
  public <A, B> B convert(Context ctx, Class<A> from, Class<B> to, A value) {

    checkArgument(BlockHeader.class == from);
    checkArgument(BlockHeaderRecord.class == to);

    final BlockHeader h = (BlockHeader) value;

    final BlockHeaderRecord.Builder builder = BlockHeaderRecord.newBuilder()
      .setNumber(wrap(BigInteger.valueOf(h.getNumber()).toByteArray()))
      .setHash(new Data32(h.getHash().clone()))
      .setParentHash(new Data32(h.getParentHash().clone()))
      .setNonce(new Data8(h.getNonce().clone()))
      .setSha3Uncles(new Data32(h.getUnclesHash().clone()))
      .setLogsBloom(new Data256(h.getLogsBloom().clone()))
      .setTransactionsRoot(new Data32(h.getTxTrieRoot().clone()))
      .setStateRoot(new Data32(h.getStateRoot().clone()))
      .setReceiptsRoot(new Data32(h.getReceiptsRoot().clone()))
      .setAuthor(new Data20(h.getCoinbase().clone()))
      .setDifficulty(wrap(h.getDifficulty().clone()))
      .setGasLimit(wrap(h.getGasLimit().clone()))
      .setGasUsed(wrap(BigInteger.valueOf(h.getGasUsed()).toByteArray()))
      .setTimestamp(h.getTimestamp())
      .setRaw(wrap(h.getEncoded().clone()));

    if(h.getExtraData() != null) builder.setExtraData(wrap(h.getExtraData().clone()));

    return to.cast(builder.build());
  }
}
