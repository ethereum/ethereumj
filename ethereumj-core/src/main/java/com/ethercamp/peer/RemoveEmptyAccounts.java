package com.ethercamp.peer;

import com.typesafe.config.ConfigFactory;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.AccountState;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.datasource.LevelDbDataSource;
import org.ethereum.db.RepositoryImpl;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.samples.BasicSample;
import org.ethereum.trie.TrieImpl;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.util.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class RemoveEmptyAccounts extends BasicSample {
    /**
     * Use that sender key to sign transactions
     */
    protected final byte[] senderPrivateKey = HashUtil.sha3("cow".getBytes());
    // sender address is derived from the private key aacc23ff079d96a5502b31fefcda87a6b3fbdcfb
    protected final byte[] senderAddress = ECKey.fromPrivate(senderPrivateKey).getAddress();

    static AtomicInteger dbReadCnt = new AtomicInteger();
    static AtomicLong dbReadTime = new AtomicLong();
    protected abstract static class MordenSampleConfig {
        private final String config =
                "peer.discovery.enabled = false \n" +
                "sync.enabled = false \n";
        public abstract RemoveEmptyAccounts sampleBean();

        @Bean
        public SystemProperties systemProperties() {
            SystemProperties props = new SystemProperties();
            props.overrideParams(ConfigFactory.parseString(config.replaceAll("'", "\"")));
            return props;
        }

        @Bean
        @Scope("prototype")
        @Primary
        public KeyValueDataSource keyValueDataSource() {
            return new LevelDbDataSource() {
                @Override
                public byte[] get(byte[] key) {
                    dbReadCnt.incrementAndGet();
                    long s = System.nanoTime();
                    byte[] ret = super.get(key);
                    dbReadTime.addAndGet(System.nanoTime() - s);
                    return ret;
                }
            };
        }
    }

    int nodeCnt = 0;
    int acctCnt = 0;
    int emptyAcctCnt = 0;

    @Override
    public void run() {
        RepositoryImpl repo = (RepositoryImpl) ethereum.getRepository();
        TrieImpl worldState = (TrieImpl) repo.getWorldState();
        final List<byte[]> nodesToDelete = new ArrayList<>();

        final long started = System.currentTimeMillis();

        worldState.scanTree(worldState.getRootHash(), new TrieImpl.ScanAction() {
            long lastStarted = System.currentTimeMillis();

            @Override
            public void doOnNode(byte[] hash, Value node) {
//                System.out.println(Hex.toHexString(hash) + " => " + node);
                nodeCnt++;
                Object[] data = (Object[]) node.asObj();
                if (data.length == 2 && ((byte[]) data[1]).length > 32) {
                    acctCnt++;
                    AccountState accountState = new AccountState((byte[]) data[1]);
                    if (accountState.getNonce().equals(BigInteger.ZERO) &&
                            accountState.getBalance().equals(BigInteger.ZERO) &&
                            FastByteComparisons.equal(accountState.getCodeHash(), HashUtil.EMPTY_DATA_HASH) &&
                            FastByteComparisons.equal(accountState.getStateRoot(), HashUtil.EMPTY_TRIE_HASH)) {

                        nodesToDelete.add(hash);
                        emptyAcctCnt++;
                    }
                }

                long l = System.currentTimeMillis();
                if (l > lastStarted + 10000) {
                    System.out.println((l - started) / 1000 + "s\t Nodes: " + nodeCnt + ", \tAccts: " + acctCnt + ", \tEmpty: " + emptyAcctCnt);
                    lastStarted = l;
                }
            }
        });

        System.out.println("Done!");
        System.out.println((System.currentTimeMillis() - started) / 1000 + "s\t Nodes: " + nodeCnt + ", \tAccts: " + acctCnt + ", \tEmpty: " + emptyAcctCnt);
    }

    public static void main(String[] args) throws Exception {

        class SampleConfig extends MordenSampleConfig {
            @Bean
            public RemoveEmptyAccounts sampleBean() {
                return new RemoveEmptyAccounts();
            }
        }

        Ethereum ethereum = EthereumFactory.createEthereum(SampleConfig.class);
    }
}
