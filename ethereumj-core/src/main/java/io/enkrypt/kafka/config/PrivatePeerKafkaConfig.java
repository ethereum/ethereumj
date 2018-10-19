package io.enkrypt.kafka.config;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.Transaction;
import org.ethereum.crypto.ECKey;
import org.ethereum.samples.BasicSample;
import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(KafkaEthereumConfig.class)
public class PrivatePeerKafkaConfig {

  @Bean
  public SystemProperties systemProperties() {
    return KafkaSystemProperties.getKafkaPrivatePeerSystemProperties();
  }

  @Bean
  public RegularNode node() {
    return new RegularNode();
  }

  static class RegularNode extends BasicSample {
    public RegularNode() {
      // peers need different loggers
      super("sampleNode");
    }

    @Override
    public void onSyncDone() {
      new Thread(() -> {
        try {
          generateTransactions();
        } catch (Exception e) {
          logger.error("Error generating tx: ", e);
        }
      }).start();
    }

    /**
     * Generate one simple value transfer transaction each 7 seconds.
     * Thus blocks will include one, several and none transactions
     */
    private void generateTransactions() throws Exception{
      logger.info("Start generating transactions...");

      // the sender which some coins from the genesis
      ECKey senderKey = ECKey.fromPrivate(Hex.decode("6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec"));
      byte[] receiverAddr = Hex.decode("5db10750e8caff27f906b41c71b3471057dd2004");

      for (int i = ethereum.getRepository().getNonce(senderKey.getAddress()).intValue(), j = 0; j < 20000; i++, j++) {
        {
          Transaction tx = new Transaction(ByteUtil.intToBytesNoLeadZeroes(i),
            ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L), ByteUtil.longToBytesNoLeadZeroes(0xfffff),
            receiverAddr, new byte[]{77}, new byte[0], ethereum.getChainIdForNextBlock());
          tx.sign(senderKey);
          logger.info("<== Submitting tx: " + tx);
          ethereum.submitTransaction(tx);
        }
        Thread.sleep(7000);
      }
    }
  }

}
