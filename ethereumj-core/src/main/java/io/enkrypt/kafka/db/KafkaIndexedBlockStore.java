package io.enkrypt.kafka.db;

import io.enkrypt.avro.Bytes20;
import io.enkrypt.avro.Bytes32;
import io.enkrypt.kafka.Kafka;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.Transaction;
import org.ethereum.crypto.ECKey;
import org.ethereum.datasource.Source;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.util.ByteUtil;

public class KafkaIndexedBlockStore extends IndexedBlockStore {

  private Kafka kafka;

  public void init(Source<byte[], byte[]> index, Source<byte[], byte[]> blocks, Kafka kafka) {
    init(index, blocks);
    this.kafka = kafka;
  }

  protected void addInternalBlock(Block block, BigInteger totalDifficulty, boolean mainChain) {
    List<BlockInfo> blockInfos = block.getNumber() >= index.size() ? null : index.get((int) block.getNumber());
    blockInfos = blockInfos == null ? new ArrayList<>() : blockInfos;

    BlockInfo blockInfo = new BlockInfo();
    blockInfo.setTotalDifficulty(totalDifficulty);
    blockInfo.setHash(block.getHash());
    blockInfo.setMainChain(mainChain); // FIXME:maybe here I should force reset main chain for all uncles on that level

    putBlockInfo(blockInfos, blockInfo);

    // TODO use kafka transactions

    blockInfos.forEach(bi -> {
      final byte[] numberBytes = ByteUtil.longToBytes(block.getNumber());
      kafka.sendSync(Kafka.Producer.BLOCKS_INFO, numberBytes, toAvro(bi));
    });
    kafka.sendSync(Kafka.Producer.BLOCKS, block.getHash(), toAvro(block));

    index.set((int) block.getNumber(), blockInfos);
    blocks.put(block.getHash(), block);
  }

  protected synchronized void setBlockInfoForLevel(long level, List<BlockInfo> infos) {
    infos.forEach(bi -> {
      final byte[] numberBytes = ByteUtil.longToBytes(level);
      kafka.sendSync(Kafka.Producer.BLOCKS_INFO, numberBytes, toAvro(bi));
    });

    index.set((int) level, infos);
  }

  private io.enkrypt.avro.BlockInfo toAvro(BlockInfo b) {
    if (b == null) {
      return null;
    }

    return io.enkrypt.avro.BlockInfo.newBuilder()
        .setHash(ByteBuffer.wrap(b.getHash()))
        .setDifficulty(ByteBuffer.wrap(b.getTotalDifficulty().toByteArray()))
        .setMainChain(b.isMainChain())
        .build();
  }

  private io.enkrypt.avro.Block toAvro(Block b) {
    if (b == null) {
      return null;
    }

    return io.enkrypt.avro.Block.newBuilder()
        .setHash(new Bytes32(b.getHash()))
        .setHeader(toAvro(b.getHeader()))
        .setTransactions(b.getTransactionsList().stream().map(this::toAvro).collect(Collectors.toList()))
        .setUncleList(b.getUncleList().stream().map(this::toAvro).collect(Collectors.toList()))
        .setBlockStats(null)
        .setBlockInfo(null)
        .build();
  }

  private io.enkrypt.avro.BlockHeader toAvro(BlockHeader h) {
    if (h == null) {
      return null;
    }

    return io.enkrypt.avro.BlockHeader.newBuilder()
        .setParentHash(new Bytes32(h.getParentHash()))
        .setUnclesHash(new Bytes32(h.getUnclesHash()))
        .setCoinbase(new Bytes20(h.getCoinbase()))
        .setStateRoot(new Bytes32(h.getStateRoot()))
        .setTxTrieRoot(new Bytes32(h.getTxTrieRoot()))
        .setReceiptTrieRoot(new Bytes32(h.getReceiptsRoot()))
        .setLogsBloom(ByteBuffer.wrap(h.getLogsBloom()))
        .setDifficulty(ByteBuffer.wrap(h.getDifficulty()))
        .setTimestamp(h.getTimestamp())
        .setNumber(h.getNumber())
        .setGasLimit(ByteBuffer.wrap(h.getGasLimit()))
        .setGasUsed(h.getGasUsed())
        .setMixHash(ByteBuffer.wrap(h.getMixHash()))
        .setExtraData(h.getExtraData() != null ? ByteBuffer.wrap(h.getExtraData()) : ByteBuffer.wrap(new byte[0]))
        .setNonce(ByteBuffer.wrap(h.getNonce()))
        .build();
  }

  private io.enkrypt.avro.Transaction toAvro(Transaction t) {
    if (t == null) {
      return null;
    }

    final ECKey.ECDSASignature signature = t.getSignature();

    return io.enkrypt.avro.Transaction.newBuilder()
        .setHash(new Bytes32(t.getHash()))
        .setNonce(ByteBuffer.wrap(t.getNonce()))
        .setTransactionIndex(null)
        .setFrom(t.getSender() != null ? new Bytes20(t.getSender()) : null)
        .setTo(t.getReceiveAddress() != null ? new Bytes20(t.getReceiveAddress()) : null)
        .setValue(ByteBuffer.wrap(t.getValue()))
        .setGasPrice(ByteBuffer.wrap(t.getGasPrice()))
        .setGasLimit(ByteBuffer.wrap(t.getGasLimit()))
        .setData(t.getData() != null ? ByteBuffer.wrap(t.getData()) : null)
        .setReceipt(null)
        .setV(ByteBuffer.wrap(new byte[] {signature.v}))
        .setR(ByteBuffer.wrap(signature.r.toByteArray()))
        .setS(ByteBuffer.wrap(signature.s.toByteArray()))
        .build();
  }
}
