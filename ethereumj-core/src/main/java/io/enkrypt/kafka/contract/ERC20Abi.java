package io.enkrypt.kafka.contract;

import io.enkrypt.kafka.models.TokenTransfer;
import org.ethereum.core.Block;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.Repository;
import org.ethereum.db.BlockStore;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.program.invoke.ProgramInvokeFactory;

import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.ethereum.util.ByteUtil.*;
import static org.ethereum.util.RLP.encodeElement;
import static org.ethereum.util.RLP.unwrapList;

public class ERC20Abi extends AbstractAbi {

  public static final String FUNCTION_TOTAL_SUPPLY = "totalSupply";
  public static final String FUNCTION_BALANCE_OF = "balanceOf";

  public static final String EVENT_TRANSFER = "Transfer";
  public static final String EVENT_APPROVAL = "Approval";

  private static final ERC20Abi INSTANCE;

  static {
    try {
      INSTANCE = new ERC20Abi();
      INSTANCE.init();
    } catch (URISyntaxException e) {
      throw new RuntimeException("Failed to instantiate", e);
    }
  }

  public static ERC20Abi getInstance() {
    return INSTANCE;
  }

  private ERC20Abi() throws URISyntaxException {
    super(Paths.get(ERC20Abi.class.getResource("/abi/erc20.json").toURI()));
  }

  @Override
  protected String[] functions() {
    return new String[]{FUNCTION_BALANCE_OF, FUNCTION_TOTAL_SUPPLY};
  }

  @Override
  protected String[] events() {
    return new String[]{EVENT_APPROVAL, EVENT_TRANSFER};
  }

  public Optional<TokenTransfer.Builder> decodeTransferEvent(byte[] data, List<DataWord> topics) {

    checkNotNull(data, "data cannot be null");
    checkNotNull(topics, "topics cannot be null");

    eventMap.get(ByteBuffer.wrap(topics.get(0).getData()));

    if (topics.size() != 3) return Optional.empty();

    return matchEvent(topics.get(0))
      .map(e -> e.decode(data, wordListToArray(topics)))
      .map(values -> TokenTransfer.newBuilder()
        .setFrom((byte[]) values.get(0))
        .setTo((byte[]) values.get(1))
        .setValue((BigInteger) values.get(2))
      );
  }

  public BigInteger balanceOf(BlockStore blockStore, Repository repository, ProgramInvokeFactory programInvokeFactory, Block block, byte[] contractAddress, byte[] address) {

    final List<?> results = this.invokeStatic(blockStore, repository, programInvokeFactory, block, contractAddress, FUNCTION_BALANCE_OF, new Object[]{address});
    checkState(results.size() == 1, "Expected 1 result, received: " + results.size());

    return (BigInteger) results.get(0);
  }

  public BigInteger balanceOf(BlockchainImpl blockchain, Block block, byte[] contractAddress, byte[] address) {

    final List<?> results = this.invokeStatic(blockchain, block, contractAddress, FUNCTION_BALANCE_OF, new Object[]{address});
    checkState(results.size() == 1, "Expected 1 result, received: " + results.size());

    return (BigInteger) results.get(0);
  }

}
