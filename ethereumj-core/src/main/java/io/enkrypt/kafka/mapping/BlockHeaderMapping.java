package io.enkrypt.kafka.mapping;

import io.enkrypt.avro.capture.BlockHeaderRecord;
import io.enkrypt.avro.common.Address;
import io.enkrypt.avro.common.Hash;
import org.ethereum.core.BlockHeader;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.ByteBuffer.wrap;

public class BlockHeaderMapping implements ObjectMapping {

  @Override
  public <A, B> B convert(ObjectMapping mappers, Class<A> from, Class<B> to, A value) {
    checkArgument(BlockHeader.class == from);
    checkArgument(BlockHeaderRecord.class == to);

    final BlockHeader h = (BlockHeader) value;

    final BlockHeaderRecord.Builder builder = BlockHeaderRecord.newBuilder()
      .setHash(wrap(h.getHash()))
      .setParentHash(wrap(h.getParentHash()))
      .setUnclesHash(wrap(h.getUnclesHash()))
      .setCoinbase(wrap(h.getCoinbase()))
      .setStateRoot(wrap(h.getStateRoot()))
      .setTxTrieRoot(wrap(h.getTxTrieRoot()))
      .setReceiptTrieRoot(wrap(h.getReceiptsRoot()))
      .setLogsBloom(wrap(h.getLogsBloom()))
      .setDifficulty(wrap(h.getDifficulty()))
      .setTimestamp(h.getTimestamp())
      .setNumber(h.getNumber())
      .setGasLimit(wrap(h.getGasLimit()))
      .setGasUsed(h.getGasUsed())
      .setMixHash(wrap(h.getMixHash()))
      .setNonce(wrap(h.getNonce()));

    if(h.getExtraData() != null) builder.setExtraData(wrap(h.getExtraData()));

    return to.cast(builder.build());
  }
}
