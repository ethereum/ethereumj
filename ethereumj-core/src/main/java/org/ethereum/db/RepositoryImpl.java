package org.ethereum.db;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.ethereum.config.CommonConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.Repository;
import org.ethereum.datasource.CachingDataSource;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.json.EtherObjectMapper;
import org.ethereum.json.JSONHelper;
import org.ethereum.trie.JournalPruneDataSource;
import org.ethereum.trie.SecureTrie;
import org.ethereum.trie.Trie;
import org.ethereum.trie.TrieImpl;
import org.ethereum.util.Value;
import org.ethereum.vm.DataWord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileSystemUtils;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.crypto.HashUtil.EMPTY_TRIE_HASH;
import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static org.ethereum.util.ByteUtil.wrap;

/**
 * @author Roman Mandeleil
 * @since 17.11.2014
 */
public class RepositoryImpl implements Repository , org.ethereum.facade.Repository{

    public final static String DETAILS_DB = "details";
    public final static String STATE_DB = "state";

    private static final Logger logger = LoggerFactory.getLogger("repository");
    private static final Logger gLogger = LoggerFactory.getLogger("general");

    @Autowired
    CommonConfig commonConfig = new CommonConfig();

    @Autowired
    SystemProperties config = SystemProperties.getDefault();

    @Autowired
    private DetailsDataStore dds = new DetailsDataStore();

    @Autowired
    private BlockStore blockStore;

    private Trie worldState;

    private DatabaseImpl detailsDB = null;

    @Autowired
    private KeyValueDataSource detailsDS;
    @Autowired
    private KeyValueDataSource stateDS;
    private CachingDataSource stateDSCache;
    private JournalPruneDataSource stateDSPrune;

    ReadWriteLock rwLock = new ReentrantReadWriteLock();

    private boolean isSnapshot = false;
    private long bestBlockNumber = 0;
    private long pruneBlockCount;
    private boolean pruneEnabled = true;

    public RepositoryImpl() {
    }

    public RepositoryImpl(KeyValueDataSource detailsDS, KeyValueDataSource stateDS) {
        this(detailsDS, stateDS, false);
    }

    public RepositoryImpl(KeyValueDataSource detailsDS, KeyValueDataSource stateDS, boolean pruneEnabled) {
        this.detailsDS = detailsDS;
        this.stateDS = stateDS;
        this.pruneEnabled = pruneEnabled;
        init();
    }

    public RepositoryImpl withBlockStore(BlockStore blockStore) {
        this.blockStore = blockStore;
        return this;
    }

    @PostConstruct
    void init() {
        detailsDS.setName(DETAILS_DB);
        detailsDS.init();

        stateDS.setName(STATE_DB);
        stateDS.init();
        stateDSCache = new CachingDataSource(stateDS);
        stateDSPrune = new JournalPruneDataSource(stateDSCache);

        detailsDB = new DatabaseImpl(detailsDS);
        dds.setDB(detailsDB);

        pruneBlockCount = pruneEnabled ? config.databasePruneDepth() : -1;

        worldState = createStateTrie();
    }

    private Trie createStateTrie() {
        return new SecureTrie(stateDSPrune).withPruningEnabled(pruneBlockCount >= 0);
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        rwLock.writeLock().lock();
        try {
            if (detailsDB != null) {
                detailsDB.close();
                detailsDB = null;
            }


            if (stateDS != null) {
                stateDS.close();
                stateDS = null;
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public boolean isClosed() {
        return stateDS == null;
    }

    @Override
    public synchronized void updateBatch(HashMap<ByteArrayWrapper, AccountState> stateCache,
                            HashMap<ByteArrayWrapper, ContractDetails> detailsCache) {

        logger.trace("updatingBatch: detailsCache.size: {}", detailsCache.size());

        for (ByteArrayWrapper hash : stateCache.keySet()) {

            AccountState accountState = stateCache.get(hash);
            ContractDetails contractDetails = detailsCache.get(hash);

            if (accountState.isDeleted()) {
                delete(hash.getData());
                logger.debug("delete: [{}]",
                        Hex.toHexString(hash.getData()));

            } else {

                if (!contractDetails.isDirty()) continue;

                ContractDetailsCacheImpl contractDetailsCache = (ContractDetailsCacheImpl) contractDetails;
                if (contractDetailsCache.origContract == null) {
                    contractDetailsCache.origContract = commonConfig.contractDetailsImpl();
                    contractDetailsCache.origContract.setAddress(hash.getData());
                    contractDetailsCache.commit();
                }

                contractDetails = contractDetailsCache.origContract;

                byte[] data = hash.getData();
                updateContractDetails(data, contractDetails);

                if ( !Arrays.equals(accountState.getCodeHash(), EMPTY_TRIE_HASH) )
                    accountState.setStateRoot(contractDetails.getStorageHash());

                updateAccountState(hash.getData(), accountState);

                if (logger.isTraceEnabled()) {
                    logger.trace("update: [{}],nonce: [{}] balance: [{}] [{}]",
                            Hex.toHexString(hash.getData()),
                            accountState.getNonce(),
                            accountState.getBalance(),
                            contractDetails.getStorage());
                }
            }
        }


        logger.debug("updated: detailsCache.size: {}", detailsCache.size());

        stateCache.clear();
        detailsCache.clear();
    }

    private synchronized void updateContractDetails(final byte[] address, final ContractDetails contractDetails) {
        rwLock.readLock().lock();
        try {
            dds.update(address, contractDetails);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public void flushNoReconnect() {
        rwLock.writeLock().lock();
        try {
                gLogger.debug("flushing to disk");
                long s = System.currentTimeMillis();

                dds.flush();
                worldState.sync();
                gLogger.info("RepositoryImpl.flushNoReconnect took " + (System.currentTimeMillis() - s) + " ms");
        } finally {
            rwLock.writeLock().unlock();
        }
    }


    @Override
    public synchronized void flush() {
        rwLock.writeLock().lock();
        try {
                gLogger.debug("flushing to disk");
                long s = System.currentTimeMillis();

                dds.flush();
                worldState.sync();
                stateDSCache.flush();

                gLogger.info("RepositoryImpl.flush took " + (System.currentTimeMillis() - s) + " ms");
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void rollback() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void commit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized void syncToRoot(final byte[] root) {
        rwLock.readLock().lock();
        try {
                worldState.setRoot(root);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public synchronized Repository startTracking() {
        return commonConfig.repositoryTrack(this);
    }

    protected SystemProperties config() {
        return config;
    }

    @Override
    public synchronized void dumpState(Block block, long gasUsed, int txNumber, byte[] txHash) {
        dumpTrie(block);

        if (!(config().dumpFull() || config().dumpBlock() == block.getNumber()))
            return;

        // todo: dump block header and the relevant tx

        if (block.getNumber() == 0 && txNumber == 0)
            if (config().dumpCleanOnRestart()) {
                FileSystemUtils.deleteRecursively(new File(config().dumpDir()));
            }

        String dir = config().dumpDir() + "/";

        String fileName = "";
        if (txHash != null)
            fileName = String.format("%07d_%d_%s.dmp", block.getNumber(), txNumber,
                    Hex.toHexString(txHash).substring(0, 8));
        else {
            fileName = String.format("%07d_c.dmp", block.getNumber());
        }

        File dumpFile = new File(System.getProperty("user.dir") + "/" + dir + fileName);
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {

            dumpFile.getParentFile().mkdirs();
            dumpFile.createNewFile();

            fw = new FileWriter(dumpFile.getAbsoluteFile());
            bw = new BufferedWriter(fw);

            List<ByteArrayWrapper> keys = this.detailsDB.dumpKeys();

            JsonNodeFactory jsonFactory = new JsonNodeFactory(false);
            ObjectNode blockNode = jsonFactory.objectNode();

            JSONHelper.dumpBlock(blockNode, block, gasUsed,
                    this.getRoot(),
                    keys, this);

            EtherObjectMapper mapper = new EtherObjectMapper();
            bw.write(mapper.writeValueAsString(blockNode));

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (bw != null) bw.close();
                if (fw != null) fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized String getTrieDump() {
        rwLock.readLock().lock();
        try {
                return worldState.getTrieDump();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public synchronized void dumpTrie(Block block) {

        if (!(config().dumpFull() || config().dumpBlock() == block.getNumber()))
            return;

        String fileName = String.format("%07d_trie.dmp", block.getNumber());
        String dir = config().dumpDir() + "/";
        File dumpFile = new File(System.getProperty("user.dir") + "/" + dir + fileName);
        FileWriter fw = null;
        BufferedWriter bw = null;

        String dump = getTrieDump();

        try {

            dumpFile.getParentFile().mkdirs();
            dumpFile.createNewFile();

            fw = new FileWriter(dumpFile.getAbsoluteFile());
            bw = new BufferedWriter(fw);

            bw.write(dump);

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (bw != null) bw.close();
                if (fw != null) fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public synchronized Set<byte[]> getAccountsKeys() {
        rwLock.readLock().lock();
        try {
                Set<byte[]> result = new HashSet<>();
                for (ByteArrayWrapper key : dds.keys()) {

                    if (isExist(key.getData()))
                        result.add(key.getData());
                }

                return result;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public synchronized BigInteger addBalance(byte[] addr, BigInteger value) {

        AccountState account = getAccountStateOrCreateNew(addr);

        BigInteger result = account.addToBalance(value);
        updateAccountState(addr, account);

        return result;
    }

    @Override
    public synchronized BigInteger getBalance(byte[] addr) {
        if (!isExist(addr)) return BigInteger.ZERO;
        AccountState account = getAccountState(addr);
        return (account == null) ? BigInteger.ZERO : account.getBalance();
    }

    @Override
    public synchronized DataWord getStorageValue(byte[] addr, DataWord key) {
        ContractDetails details = getContractDetails(addr);
        return (details == null) ? null : details.get(key);
    }

    @Override
    public synchronized int getStorageSize(byte[] addr) {
        ContractDetails details = getContractDetails(addr);
        return (details == null) ? 0 : details.getStorageSize();
    }

    @Override
    public synchronized Set<DataWord> getStorageKeys(byte[] addr) {
        ContractDetails details = getContractDetails(addr);
        return (details == null) ? Collections.EMPTY_SET : details.getStorageKeys();
    }

    @Override
    public synchronized Map<DataWord, DataWord> getStorage(byte[] addr, Collection<DataWord> keys) {
        ContractDetails details = getContractDetails(addr);
        return (details == null) ? Collections.EMPTY_MAP : details.getStorage(keys);
    }

    @Override
    public synchronized void addStorageRow(byte[] addr, DataWord key, DataWord value) {
        ContractDetails details = getContractDetails(addr);
        if (details == null) {
            createAccount(addr);
            details = getContractDetails(addr);
        }

        details.put(key, value);

        updateContractDetails(addr, details);
    }

    @Override
    public synchronized byte[] getCode(byte[] addr) {

        if (!isExist(addr))
            return EMPTY_BYTE_ARRAY;

        byte[] codeHash = getAccountState(addr).getCodeHash();

        ContractDetails details = getContractDetails(addr);
        return (details == null) ? null : details.getCode(codeHash);
    }

    @Override
    public synchronized void saveCode(byte[] addr, byte[] code) {
        ContractDetails details = getContractDetails(addr);

        if (details == null) {
            createAccount(addr);
            details = getContractDetails(addr);
        }

        details.setCode(code);
        AccountState accountState = getAccountState(addr);
        accountState.setCodeHash(sha3(code));

        updateContractDetails(addr, details);
        updateAccountState(addr, accountState);
    }


    @Override
    public synchronized BigInteger getNonce(byte[] addr) {
        AccountState accountState = getAccountState(addr);
        return accountState == null ? config().getBlockchainConfig().getCommonConstants().getInitialNonce() :
                accountState.getNonce();
    }

    @Nonnull
    private synchronized AccountState getAccountStateOrCreateNew(byte[] addr) {
        AccountState account = getAccountState(addr);
        return (account == null) ? createAccount(addr) : account;
    }

    @Override
    public synchronized BigInteger increaseNonce(byte[] addr) {
        AccountState account = getAccountStateOrCreateNew(addr);

        account.incrementNonce();
        updateAccountState(addr, account);

        return account.getNonce();
    }

    private synchronized void updateAccountState(final byte[] addr, final AccountState accountState) {
        rwLock.readLock().lock();
        try {
                worldState.update(addr, accountState.getEncoded());
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public synchronized BigInteger setNonce(final byte[] addr, final BigInteger nonce) {
        AccountState account = getAccountStateOrCreateNew(addr);

        account.setNonce(nonce);
        updateAccountState(addr, account);

        return account.getNonce();
    }

    @Override
    public synchronized void delete(final byte[] addr) {
        rwLock.readLock().lock();
        try {
                worldState.delete(addr);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public synchronized ContractDetails getContractDetails(final byte[] addr) {
        rwLock.readLock().lock();
        try {
                // That part is important cause if we have
                // to sync details storage according the trie root
                // saved in the account
                AccountState accountState = getAccountState(addr);
                byte[] storageRoot = EMPTY_TRIE_HASH;
                if (accountState != null)
                    storageRoot = getAccountState(addr).getStateRoot();
                ContractDetails details =  dds.get(addr);

                if (details != null)
                    details = details.getSnapshotTo(storageRoot);

                return  details;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public boolean hasContractDetails(byte[] addr) {
        return dds.get(addr) != null;
    }

    @Override
    public synchronized AccountState getAccountState(final byte[] addr) {
        rwLock.readLock().lock();
        try {
                AccountState result = null;
                byte[] accountData = worldState.get(addr);

                if (accountData.length != 0)
                    result = new AccountState(accountData);

                return result;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public synchronized AccountState createAccount(final byte[] addr) {
        AccountState accountState = new AccountState(
                config().getBlockchainConfig().getCommonConstants().getInitialNonce(), BigInteger.ZERO);

        updateAccountState(addr, accountState);
        updateContractDetails(addr, commonConfig.contractDetailsImpl());

        return accountState;
    }

    @Override
    public boolean isExist(byte[] addr) {
        return getAccountState(addr) != null;
    }

    @Override
    public synchronized void loadAccount(byte[] addr,
                            HashMap<ByteArrayWrapper, AccountState> cacheAccounts,
                            HashMap<ByteArrayWrapper, ContractDetails> cacheDetails) {

        AccountState account = getAccountState(addr);
        ContractDetails details = getContractDetails(addr);

        account = (account == null) ? new AccountState(config().getBlockchainConfig().getCommonConstants().
                getInitialNonce(), BigInteger.ZERO) : account.clone();
        details = new ContractDetailsCacheImpl(details);
//        details.setAddress(addr);

        ByteArrayWrapper wrappedAddress = wrap(addr);
        cacheAccounts.put(wrappedAddress, account);
        cacheDetails.put(wrappedAddress, details);
    }

    @Override
    public synchronized byte[] getRoot() {
        return worldState.getRootHash();
    }

    public synchronized void setRoot(byte[] root) {
        worldState.setRoot(root);
    }

    public void setPruneBlockCount(long pruneBlockCount) {
        this.pruneBlockCount = pruneBlockCount;
    }

    public synchronized void commitBlock(BlockHeader blockHeader) {
        worldState.sync();

        if (pruneBlockCount >= 0) {
            stateDSPrune.storeBlockChanges(blockHeader);
            pruneBlocks(blockHeader);
        }
    }

    private void pruneBlocks(BlockHeader curBlock) {
        if (curBlock.getNumber() > bestBlockNumber) { // pruning only on increasing blocks
            long pruneBlockNumber = curBlock.getNumber() - pruneBlockCount;
            if (pruneBlockNumber >= 0) {
                byte[] pruneBlockHash = blockStore.getBlockHashByNumber(pruneBlockNumber);
                if (pruneBlockHash != null) {
                    stateDSPrune.prune(blockStore.getBlockByHash(pruneBlockHash).getHeader());
                }
            }
        }
        bestBlockNumber = curBlock.getNumber();
    }

    public Trie getWorldState() {
        return worldState;
    }

    @Override
    public synchronized Repository getSnapshotTo(byte[] root){

        RepositoryImpl repo = new RepositoryImpl();
        repo.commonConfig = commonConfig;
        repo.blockStore = blockStore;
        repo.config = config;
        repo.stateDS = this.stateDS;
        repo.stateDSCache = this.stateDSCache;
        repo.stateDSPrune = this.stateDSPrune;
        repo.pruneBlockCount = this.pruneBlockCount;
        repo.detailsDB = this.detailsDB;
        repo.detailsDS = this.detailsDS;
        repo.dds = this.dds;
        repo.isSnapshot = true;

        repo.worldState = repo.createStateTrie();
        repo.worldState.setRoot(root);


        return repo;
    }
}
