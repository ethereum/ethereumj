package io.enkrypt.kafka.contract;

import org.ethereum.core.*;
import org.ethereum.crypto.ECKey;
import org.ethereum.db.BlockStore;
import org.ethereum.solidity.Abi;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.LogInfo;
import org.ethereum.vm.program.invoke.ProgramInvokeFactory;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.ethereum.util.ByteUtil.longToBytesNoLeadZeroes;

public abstract class AbstractAbi {

  protected final Path path;

  protected Abi abi;

  protected Map<ByteBuffer, Abi.Function> functionMap;
  protected Map<ByteBuffer, Abi.Event> eventMap;

  protected AbstractAbi(Path path) {
    this.path = path;
  }

  public void init() {
    try {

      // load in the abi
      final String json = new String(Files.readAllBytes(path));
      this.abi = Abi.fromJson(json);

      // index the functions
      this.functionMap = Arrays
        .stream(functions())
        .map(name -> abi.findFunction(f -> f.name.equals(name)))
        .collect(Collectors.toMap(f -> ByteBuffer.wrap(Arrays.copyOfRange(f.encodeSignature(), 0, 4)), f -> f));

      // index the events
      this.eventMap = Arrays
        .stream(events())
        .map(name -> abi.findEvent(e -> e.name.equals(name)))
        .collect(Collectors.toMap(e -> ByteBuffer.wrap(e.encodeSignature()), e -> e));


    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  protected abstract String[] functions();

  protected abstract String[] events();

  public Optional<Abi.Function> matchFunction(byte[] data) {
    if (data == null || data.length < 4) return Optional.empty();

    final ByteBuffer key = ByteBuffer.wrap(data, 0, 4);
    return Optional.ofNullable(functionMap.get(key));
  }

  public Optional<Abi.Event> matchEvent(List<DataWord> topics) {
    checkNotNull(topics, "topics cannot be null");
    if (topics.isEmpty()) return Optional.empty();
    return matchEvent(topics.get(0));
  }

  public Optional<Abi.Event> matchEvent(DataWord word) {
    final ByteBuffer key = ByteBuffer.wrap(word.getData());
    return Optional.ofNullable(eventMap.get(key));
  }

  public List<?> invokeStatic(BlockchainImpl blockchain,
                              Block block,
                              byte[] address,
                              String methodName,
                              Object... args) {
    return invokeStatic(
      blockchain.getBlockStore(),
      blockchain.getRepository().getSnapshotTo(block.getStateRoot()),
      blockchain.getProgramInvokeFactory(),
      block,
      address,
      methodName,
      args
    );
  }

  public List<?> invokeStatic(BlockStore blockStore,
                              Repository repository,
                              ProgramInvokeFactory programInvokeFactory,
                              Block block,
                              byte[] address,
                              String methodName,
                              Object... args) {

    checkNotNull(blockStore, "blockStore cannot be null");
    checkNotNull(repository, "repository cannot be null");
    checkNotNull(programInvokeFactory, "programInvokeFactory cannot be null");
    checkNotNull(block, "block cannot be null");
    checkNotNull(address, "address cannot be null");
    checkNotNull(methodName, "methodName cannot be null");

    //

    final Abi.Function function = abi.findFunction(f -> f.name.equals(methodName));
    checkNotNull(function, "function not found: " + methodName);

    //

    final Transaction tx = new Transaction(longToBytesNoLeadZeroes(0),
      longToBytesNoLeadZeroes(0),
      longToBytesNoLeadZeroes(100000000000000L),
      address,
      longToBytesNoLeadZeroes(0),
      function.encode(args)
    );

    tx.sign(ECKey.DUMMY);

    TransactionExecutor executor = new org.ethereum.core.TransactionExecutor(
      tx,
      block.getCoinbase(),
      repository,
      blockStore,
      programInvokeFactory,
      block
    ).setLocalCall(true);

    executor.init();
    executor.execute();
    executor.go();
    executor.finalization();

    return function.decodeResult(executor.getResult().getHReturn());

  }

}
