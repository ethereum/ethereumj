package org.ethereum.manager;

import org.ethereum.core.Block;
import org.ethereum.core.Blockchain;
import org.ethereum.core.EventDispatchThread;
import org.ethereum.core.Genesis;
import org.ethereum.core.ImportResult;
import org.ethereum.db.DbFlushManager;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.validator.BlockHeaderRule;
import org.ethereum.validator.BlockHeaderValidator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static java.util.Objects.isNull;
import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = BlockLoaderTest.Config.class)
public class BlockLoaderTest {

    private static class Holder<T> {
        private T object;

        public Holder(T object) {
            this.object = object;
        }

        public T get() {
            return object;
        }

        public void set(T object) {
            this.object = object;
        }

        public boolean isEmpty() {
            return isNull(object);
        }
    }

    static class Config {

        private static final Genesis GENESIS_BLOCK = new Genesis(
                EMPTY_BYTE_ARRAY, EMPTY_BYTE_ARRAY, EMPTY_BYTE_ARRAY, EMPTY_BYTE_ARRAY, EMPTY_BYTE_ARRAY,
                0, 0, 0, 0, EMPTY_BYTE_ARRAY, EMPTY_BYTE_ARRAY, EMPTY_BYTE_ARRAY);

        @Bean
        public BlockHeaderValidator headerValidator() {
            BlockHeaderValidator validator = Mockito.mock(BlockHeaderValidator.class);
            when(validator.validate(any())).thenReturn(new BlockHeaderRule.ValidationResult(true, null));
            when(validator.validateAndLog(any(), any())).thenReturn(true);
            return validator;
        }

        @Bean
        public Blockchain blockchain(Holder<Block> lastBlockHolder) {
            Blockchain blockchain = Mockito.mock(Blockchain.class);
            when(blockchain.getBestBlock()).thenAnswer(invocation -> lastBlockHolder.get());
            when(blockchain.tryToConnect(any(Block.class))).thenAnswer(invocation -> {
                lastBlockHolder.set(invocation.getArgument(0));
                return ImportResult.IMPORTED_BEST;
            });
            return blockchain;
        }

        @Bean
        public Holder<Block> lastBlockHolder() {
            return new Holder<>(GENESIS_BLOCK);
        }

        @Bean
        public DbFlushManager dbFlushManager() {
            return Mockito.mock(DbFlushManager.class);
        }

        @Bean
        public CompositeEthereumListener ethereumListener() {
            return Mockito.mock(CompositeEthereumListener.class);
        }

        @Bean
        public EventDispatchThread dispatchThread() {
            return EventDispatchThread.getDefault();
        }

        @Bean
        public BlockLoader blockLoader(BlockHeaderValidator headerValidator, Blockchain blockchain, DbFlushManager dbFlushManager) {
            return new BlockLoader(headerValidator, blockchain, dbFlushManager);
        }
    }

    @Value("classpath:dmp")
    private Resource resource;
    @Autowired
    private BlockLoader blockLoader;

    @Test
    public void test() throws IOException {
        Path[] paths = Arrays.stream(resource.getFile().listFiles())
                .sorted()
                .map(dmp -> Paths.get(dmp.toURI()))
                .toArray(Path[]::new);

        Assert.assertTrue(blockLoader.loadBlocks(paths));
    }
}