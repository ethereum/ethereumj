package io.enkrypt.kafka.replay;

import io.enkrypt.kafka.Kafka;
import io.enkrypt.kafka.db.BlockSummaryStore;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.AccountState;
import org.ethereum.core.BlockSummary;
import org.ethereum.core.TransactionExecutionSummary;
import org.ethereum.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static java.lang.Long.parseLong;
import static org.ethereum.util.ByteUtil.intToBytes;
import static org.ethereum.util.ByteUtil.longToBytes;

public class BlockSummaryReplayer {

  @Autowired
  BlockSummaryStore store;

  @Autowired
  SystemProperties config;

  @Autowired
  Kafka kafka;

  private final Logger logger = LoggerFactory.getLogger("replay");

  public BlockSummaryReplayer() {
  }

  public void replay() {


//    BlockSummary blockSummary;
//    long number = parseLong(config.getProperty("replay.from", "0"));
//
//    logger.info("Attempting to replay from block number = {}", number);
//
//    do {
//      blockSummary = store.get(number);
//
//      if(blockSummary != null) {
//
//        byte[] numberAsBytes = longToBytes(number);
//        kafka.send(Kafka.Producer.BLOCKS, numberAsBytes, blockSummary.getEncoded());
//
//        for (TransactionExecutionSummary summary : blockSummary.getSummaries()) {
//          final Map<byte[], AccountState> touchedAccounts = summary.getTouchedAccounts();
//          for (Map.Entry<byte[], AccountState> entry : touchedAccounts.entrySet()) {
//            kafka.send(Kafka.Producer.ACCOUNT_STATE, entry.getKey(), entry.getValue().getEncoded());
//          }
//        }
//
//        logger.info("Replayed block number = {}", number);
//
//        number++;
//      }
//
//    } while(blockSummary != null);
  }
}

