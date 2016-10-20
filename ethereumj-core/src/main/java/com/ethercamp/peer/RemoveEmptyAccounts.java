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
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private KeyValueDataSource stateDS;

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
    int emptyAcctRemoved = 0;

    @Override
    public void run() {
        final RepositoryImpl repo = (RepositoryImpl) ethereum.getRepository();
        final TrieImpl worldState = (TrieImpl) repo.getWorldState();
        final List<byte[]> nodesToDelete = new ArrayList<>();

        final TrieImpl deleteTrie = new TrieImpl(repo.stateDSCache, repo.getRoot());

        final long started = System.currentTimeMillis();

        worldState.scanTree(worldState.getRootHash(), new TrieImpl.ScanAction() {
            long lastStarted = System.currentTimeMillis();

            @Override
            public void doOnValue(byte[] key, byte[] value) {
                acctCnt++;
                AccountState accountState = new AccountState(value);
                if (accountState.getNonce().equals(BigInteger.ZERO) &&
                        accountState.getBalance().equals(BigInteger.ZERO) &&
                        FastByteComparisons.equal(accountState.getCodeHash(), HashUtil.EMPTY_DATA_HASH) &&
                        FastByteComparisons.equal(accountState.getStateRoot(), HashUtil.EMPTY_TRIE_HASH)) {

                    nodesToDelete.add(key);
                    emptyAcctCnt++;
                }

                if (emptyAcctCnt == 10000) {
                    long s = System.currentTimeMillis();
                    for (byte[] deleteKey : nodesToDelete) {
                        deleteTrie.delete(deleteKey);
                    }
                    repo.stateDSCache.flush();

                    // cleaning cache
                    worldState.getCache().setDirty(true);
                    worldState.getCache().commit();

                    worldState.setRoot(deleteTrie.getRootHash());
                    System.out.println("Removed 10000 accounts in " + (System.currentTimeMillis() - s) + "ms");

                    emptyAcctRemoved += emptyAcctCnt;
                    emptyAcctCnt = 0;
                }
            }

            @Override
            public void doOnNode(byte[] hash, Value node) {
                nodeCnt++;

                long l = System.currentTimeMillis();
                if (l > lastStarted + 10000) {
                    System.out.println((l - started) / 1000 + "s\t Nodes: " + nodeCnt + "\tAccts: " + acctCnt + "\tEmpty: " + emptyAcctCnt + "\tRemoved: " + emptyAcctRemoved);
                    lastStarted = l;
                }
            }

        });

        System.out.println("Done!");
        System.out.println((System.currentTimeMillis() - started) / 1000 + "s\tNodes: " + nodeCnt + "\tAccts: " + acctCnt + "\tEmpty: " + emptyAcctCnt + "\tRemoved: " + emptyAcctRemoved);

        System.out.println(Hex.toHexString(repo.stateDSCache.get(nodesToDelete.get(0))));

//        new TrieImpl()
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
