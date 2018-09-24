package io.enkrypt.kafka.config;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.enkrypt.kafka.Kafka;
import io.enkrypt.kafka.KafkaImpl;
import io.enkrypt.kafka.NullKafka;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.ethereum.config.CommonConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Repository;
import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.*;
import org.ethereum.db.*;
import io.enkrypt.kafka.db.KafkaIndexedBlockStore;
import io.enkrypt.kafka.db.KafkaTransactionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

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
    return new XorDataSource<>(src, HashUtil.sha3("state".getBytes()));
  }

  @Bean
  public Repository defaultRepository() {
    return new RepositoryRoot(commonConfig.stateSource(), null, kafka());
  }


  @Bean
  public Kafka kafka() {
    final boolean enabled = ((KafkaSystemProperties) config).isKafkaEnabled();
    final String bootstrapServers = ((KafkaSystemProperties) config).getKafkaBootstrapServers();
    final String schemaRegistryUrl = ((KafkaSystemProperties) config).getSchemaRegistryUrl();

    if (!enabled) {
      return new NullKafka();
    }

    final Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "ethj");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
    props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 2000000000);

    return new KafkaImpl(new KafkaProducer<>(props));
  }
}
