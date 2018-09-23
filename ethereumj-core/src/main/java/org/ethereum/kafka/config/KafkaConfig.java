package org.ethereum.kafka.config;

import com.github.jcustenborder.kafka.serialization.jackson.JacksonSerializer;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.ethereum.config.CommonConfig;
import org.ethereum.config.DefaultConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.DbSource;
import org.ethereum.datasource.NodeKeyCompositor;
import org.ethereum.datasource.PrefixLookupSource;
import org.ethereum.datasource.Source;
import org.ethereum.datasource.XorDataSource;
import org.ethereum.db.BlockStore;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.db.PruneManager;
import org.ethereum.db.TransactionStore;
import org.ethereum.kafka.Kafka;
import org.ethereum.kafka.db.KafkaIndexedBlockStore;
import org.ethereum.kafka.db.KafkaListeningDataSource;
import org.ethereum.kafka.db.KafkaTransactionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
public class KafkaConfig {

  private static Logger logger = LoggerFactory.getLogger("general");

  @Autowired
  ApplicationContext appCtx;

  @Autowired
  CommonConfig commonConfig;

  @Autowired
  SystemProperties config;

  public KafkaConfig() {
    // TODO: We can intercept KafkaException to stop completely the app in case of a bad crash
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.error("Uncaught exception", e));
  }

  @Bean
  public BlockStore blockStore() {
    commonConfig.fastSyncCleanUp();

    Source<byte[], byte[]> block = commonConfig.cachedDbSource("block");
    Source<byte[], byte[]> index = commonConfig.cachedDbSource("index");

    KafkaIndexedBlockStore indexedBlockStore = new KafkaIndexedBlockStore();
    indexedBlockStore.init(index, block, kafka());
    return indexedBlockStore;
  }

  @Bean
  public TransactionStore transactionStore() {
    commonConfig.fastSyncCleanUp();
    return new KafkaTransactionStore(
        commonConfig.cachedDbSource("transactions"),
        kafka()
    );
  }

  @Bean
  public PruneManager pruneManager() {
    if (config.databasePruneDepth() >= 0) {
      return new PruneManager(
          (IndexedBlockStore) blockStore(),
          commonConfig.stateSource().getJournalSource(),
          commonConfig.stateSource().getNoJournalSource(),
          config.databasePruneDepth()
      );
    } else {
      return new PruneManager(null, null, null, -1); // dummy
    }
  }

  @Bean
  public Source<byte[], byte[]> trieNodeSource() {
    DbSource<byte[]> db = commonConfig.blockchainDB();
    Source<byte[], byte[]> src = new PrefixLookupSource<>(db, NodeKeyCompositor.PREFIX_BYTES);
    final XorDataSource<byte[]> xorDataSource = new XorDataSource<>(src, HashUtil.sha3("state".getBytes()));
    return new KafkaListeningDataSource(kafka(), xorDataSource);
  }

  @Bean
  public Kafka kafka() {
    final String bootstrapServers = ((KafkaSystemProperties) config).getKafkaBootstrapServers();

    // Long - Object Producer
    final Properties props1 = new Properties();
    props1.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props1.put(ProducerConfig.CLIENT_ID_CONFIG, "ethj-1");
    props1.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
    props1.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonSerializer.class.getName());
    props1.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 2000000000);
    final KafkaProducer<Long, Object> longObjectKafkaProducer = new KafkaProducer<>(props1);

    // String - Object Producer
    final Properties props2 = new Properties();
    props2.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props2.put(ProducerConfig.CLIENT_ID_CONFIG, "ethj-2");
    props2.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props2.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonSerializer.class.getName());
    props2.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 2000000000);
    final KafkaProducer<String, Object> stringObjectProducer = new KafkaProducer<>(props2);

    // String - Byte Producer
    final Properties props3 = new Properties();
    props3.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props3.put(ProducerConfig.CLIENT_ID_CONFIG, "ethj-state-producer");
    props3.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props3.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, BytesSerializer.class.getName());
    props3.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 2000000000);
    final KafkaProducer<String, byte[]> stateProducer = new KafkaProducer<>(props3);

    return new Kafka(longObjectKafkaProducer, stringObjectProducer, stateProducer);
  }
}
