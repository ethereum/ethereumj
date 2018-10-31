package io.enkrypt.kafka.contract;

import io.enkrypt.kafka.models.TokenTransfer;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.ethereum.vm.DataWord;

import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.ethereum.util.ByteUtil.*;
import static org.ethereum.util.RLP.encodeElement;
import static org.ethereum.util.RLP.unwrapList;

public class ERC721Abi extends AbstractAbi {

  public static final String EVENT_TRANSFER = "Transfer";
  public static final String EVENT_APPROVAL = "Approval";

  private static final ERC721Abi INSTANCE;

  static {
    try {
      INSTANCE = new ERC721Abi();
      INSTANCE.init();
    } catch (URISyntaxException e) {
      throw new RuntimeException("Failed to instantiate", e);
    }
  }

  public static ERC721Abi getInstance() {
    return INSTANCE;
  }

  private ERC721Abi() throws URISyntaxException {
    super(Paths.get(ERC721Abi.class.getResource("/abi/erc721.json").toURI()));
  }

  @Override
  protected String[] functions() {
    return new String[]{
      "transfer"
    };
  }

  @Override
  protected String[] events() {
    return new String[]{
      EVENT_TRANSFER,
      EVENT_APPROVAL
    };
  }

  public Optional<TokenTransfer.Builder> decodeTransferEvent(byte[] data, List<DataWord> topics) {

    checkNotNull(data, "data cannot be null");
    checkNotNull(topics, "topics cannot be null");

    if (topics.size() != 4) return Optional.empty();

    return matchEvent(topics.get(0))
      .map(e -> e.decode(data, wordListToArray(topics)))
      .map(values -> TokenTransfer.newBuilder()
        .setFrom((byte[]) values.get(0))
        .setTo((byte[]) values.get(1))
        .setTokenId((BigInteger) values.get(2))
      );
  }


}
