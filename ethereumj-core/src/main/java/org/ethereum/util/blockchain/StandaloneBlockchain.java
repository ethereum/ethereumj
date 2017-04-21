package org.ethereum.util.blockchain;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.core.genesis.GenesisLoader;
import org.ethereum.crypto.ECKey;
import org.ethereum.datasource.*;
import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.db.PruneManager;
import org.ethereum.db.RepositoryRoot;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.mine.Ethash;
import org.ethereum.solidity.compiler.CompilationResult;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.ethereum.sync.SyncManager;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.validator.DependentBlockHeaderRuleAdapter;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.LogInfo;
import org.ethereum.vm.program.invoke.ProgramInvokeFactoryImpl;
import org.iq80.leveldb.DBException;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Anton Nashatyrev on 23.03.2016.
 */
public class StandaloneBlockchain implements LocalBlockchain {

    Genesis genesis;
    byte[] coinbase;
    BlockchainImpl blockchain;
    PendingStateImpl pendingState;
    CompositeEthereumListener listener;
    ECKey txSender;
    long gasPrice;
    long gasLimit;
    boolean autoBlock;
    long dbDelay = 0;
    long totalDbHits = 0;
    int blockGasIncreasePercent = 0;

    long time = 0;
    long timeIncrement = 13;

    private HashMapDB<byte[]> stateDS;
    JournalSource<byte[]> pruningStateDS;
    PruneManager pruneManager;
    private BlockSummary lastSummary;

    List<PendingTx> submittedTxes = new CopyOnWriteArrayList<>();
//    SolidityContractImpl contract;

    public StandaloneBlockchain() {
        Genesis genesis = GenesisLoader.loadGenesis(
                getClass().getResourceAsStream("/genesis/genesis-light-sb.json"));

        withGenesis(genesis);
        withGasPrice(50_000_000_000L);
        withGasLimit(5_000_000L);
        withMinerCoinbase(Hex.decode("ffffffffffffffffffffffffffffffffffffffff"));
        setSender(ECKey.fromPrivate(Hex.decode("3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c")));
    }

    public StandaloneBlockchain withGenesis(Genesis genesis) {
        this.genesis = genesis;
        return this;
    }

    public StandaloneBlockchain withMinerCoinbase(byte[] coinbase) {
        this.coinbase = coinbase;
        return this;
    }

    public StandaloneBlockchain withAccountBalance(byte[] address, BigInteger weis) {
        AccountState state = new AccountState(BigInteger.ZERO, weis);
        genesis.getPremine().put(new ByteArrayWrapper(address), state);
        genesis.setStateRoot(GenesisLoader.generateRootHash(genesis.getPremine()));

        return this;
    }


    public StandaloneBlockchain withGasPrice(long gasPrice) {
        this.gasPrice = gasPrice;
        return this;
    }

    public StandaloneBlockchain withGasLimit(long gasLimit) {
        this.gasLimit = gasLimit;
        return this;
    }

    public StandaloneBlockchain withAutoblock(boolean autoblock) {
        this.autoBlock = autoblock;
        return this;
    }

    public StandaloneBlockchain withCurrentTime(Date date) {
        this.time = date.getTime() / 1000;
        return this;
    }

    /**
     * [-100, 100]
     * 0 - the same block gas limit as parent
     * 100 - max available increase from parent gas limit
     * -100 - max available decrease from parent gas limit
     */
    public StandaloneBlockchain withBlockGasIncrease(int blockGasIncreasePercent) {
        this.blockGasIncreasePercent = blockGasIncreasePercent;
        return this;
    }

    public StandaloneBlockchain withDbDelay(long dbDelay) {
        this.dbDelay = dbDelay;
        return this;
    }

    private Map<PendingTx, Transaction> createTransactions(Block parent) {
        Map<PendingTx, Transaction> txes = new LinkedHashMap<>();
        Map<ByteArrayWrapper, Long> nonces = new HashMap<>();
        Repository repoSnapshot = getBlockchain().getRepository().getSnapshotTo(parent.getStateRoot());
        for (PendingTx tx : submittedTxes) {
            Transaction transaction;
            if (tx.customTx == null) {
                ByteArrayWrapper senderW = new ByteArrayWrapper(tx.sender.getAddress());
                Long nonce = nonces.get(senderW);
                if (nonce == null) {
                    BigInteger bcNonce = repoSnapshot.getNonce(tx.sender.getAddress());
                    nonce = bcNonce.longValue();
                }
                nonces.put(senderW, nonce + 1);

                byte[] toAddress = tx.targetContract != null ? tx.targetContract.getAddress() : tx.toAddress;

                transaction = createTransaction(tx.sender, nonce, toAddress, tx.value, tx.data);

                if (tx.createdContract != null) {
                    tx.createdContract.setAddress(transaction.getContractAddress());
                }
            } else {
                transaction = tx.customTx;
            }

            txes.put(tx, transaction);
        }
        return txes;
    }

    public PendingStateImpl getPendingState() {
        return pendingState;
    }

    public void generatePendingTransactions() {
        pendingState.addPendingTransactions(new ArrayList<>(createTransactions(getBlockchain().getBestBlock()).values()));
    }

    @Override
    public Block createBlock() {
        return createForkBlock(getBlockchain().getBestBlock());
    }

    @Override
    public Block createForkBlock(Block parent) {
        try {
            Map<PendingTx, Transaction> txes = createTransactions(parent);

            time += timeIncrement;
            Block b = getBlockchain().createNewBlock(parent, new ArrayList<>(txes.values()), Collections.EMPTY_LIST, time);

            int GAS_LIMIT_BOUND_DIVISOR = SystemProperties.getDefault().getBlockchainConfig().
                    getCommonConstants().getGAS_LIMIT_BOUND_DIVISOR();
            BigInteger newGas = ByteUtil.bytesToBigInteger(parent.getGasLimit())
                    .multiply(BigInteger.valueOf(GAS_LIMIT_BOUND_DIVISOR * 100 + blockGasIncreasePercent))
                    .divide(BigInteger.valueOf(GAS_LIMIT_BOUND_DIVISOR * 100));
            b.getHeader().setGasLimit(ByteUtil.bigIntegerToBytes(newGas));

            Ethash.getForBlock(SystemProperties.getDefault(), b.getNumber()).mineLight(b).get();
            ImportResult importResult = getBlockchain().tryToConnect(b);
            if (importResult != ImportResult.IMPORTED_BEST && importResult != ImportResult.IMPORTED_NOT_BEST) {
                throw new RuntimeException("Invalid block import result " + importResult + " for block " + b);
            }

            List<PendingTx> pendingTxes = new ArrayList<>(txes.keySet());
            for (int i = 0; i < lastSummary.getReceipts().size(); i++) {
                pendingTxes.get(i).txResult.receipt = lastSummary.getReceipts().get(i);
                pendingTxes.get(i).txResult.executionSummary = getTxSummary(lastSummary, i);
            }
            //Clear all the transactions in the block with every new block (just a safety check)
            submittedTxes.clear();
            return b;
        } catch (InterruptedException|ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private TransactionExecutionSummary getTxSummary(BlockSummary bs, int idx) {
        TransactionReceipt txReceipt = bs.getReceipts().get(idx);
        for (TransactionExecutionSummary summary : bs.getSummaries()) {
            if (FastByteComparisons.equal(txReceipt.getTransaction().getHash(), summary.getTransaction().getHash())) {
                return summary;
            }
        }
        return null;
    }

    public Transaction createTransaction(long nonce, byte[] toAddress, long value, byte[] data) {
        return createTransaction(getSender(), nonce, toAddress, BigInteger.valueOf(value), data);
    }
    public Transaction createTransaction(ECKey sender, long nonce, byte[] toAddress, BigInteger value, byte[] data) {
        Transaction transaction = new Transaction(ByteUtil.longToBytesNoLeadZeroes(nonce),
                ByteUtil.longToBytesNoLeadZeroes(gasPrice),
                ByteUtil.longToBytesNoLeadZeroes(gasLimit),
                toAddress, ByteUtil.bigIntegerToBytes(value),
                data,
                null);
        transaction.sign(sender);
        return transaction;
    }

    public void resetSubmittedTransactions() {
        submittedTxes.clear();
    }

    @Override
    public void setSender(ECKey senderPrivateKey) {
        txSender = senderPrivateKey;
//        if (!getBlockchain().getRepository().isExist(senderPrivateKey.getAddress())) {
//            Repository repository = getBlockchain().getRepository();
//            Repository track = repository.startTracking();
//            track.createAccount(senderPrivateKey.getAddress());
//            track.commit();
//        }
    }

    public ECKey getSender() {
        return txSender;
    }

    @Override
    public void sendEther(byte[] toAddress, BigInteger weis) {
        submitNewTx(new PendingTx(txSender, toAddress, weis, new byte[0]));
    }

    public void submitTransaction(Transaction tx) {
        submitNewTx(new PendingTx(tx));
    }

    @Override
    public SolidityContract submitNewContract(String soliditySrc, Object... constructorArgs) {
        return submitNewContract(soliditySrc, null, constructorArgs);
    }

    @Override
    public SolidityContract submitNewContract(String soliditySrc, String contractName, Object... constructorArgs) {
        SolidityContractImpl contract = createContract(soliditySrc, contractName);
        CallTransaction.Function constructor = contract.contract.getConstructor();
        if (constructor == null && constructorArgs.length > 0) {
            throw new RuntimeException("No constructor with params found");
        }
        byte[] argsEncoded = constructor == null ? new byte[0] : constructor.encodeArguments(constructorArgs);
        submitNewTx(new PendingTx(txSender, new byte[0], BigInteger.ZERO,
                ByteUtil.merge(Hex.decode(contract.getBinary()), argsEncoded), contract, null, new TransactionResult()));
        return contract;
    }

    @Override
    public SolidityContract submitNewContractFromJson(String json, Object... constructorArgs) {
        return submitNewContractFromJson(json, null, constructorArgs);
    }

    @Override
    public SolidityContract submitNewContractFromJson(String json, String contractName, Object... constructorArgs) {
		SolidityContractImpl contract;
		try {
			contract = createContractFromJson(contractName, json);
			CallTransaction.Function constructor = contract.contract.getConstructor();
			if (constructor == null && constructorArgs.length > 0) {
				throw new RuntimeException("No constructor with params found");
			}
			byte[] argsEncoded = constructor == null ? new byte[0] : constructor.encodeArguments(constructorArgs);
			submitNewTx(new PendingTx(txSender, new byte[0], BigInteger.ZERO,
					ByteUtil.merge(Hex.decode(contract.getBinary()), argsEncoded), contract, null,
					new TransactionResult()));
			return contract;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }

    private SolidityContractImpl createContract(String soliditySrc, String contractName) {
        try {
            SolidityCompiler.Result compileRes = SolidityCompiler.compile(soliditySrc.getBytes(), true, SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN);
            if (compileRes.isFailed()) throw new RuntimeException("Compile result: " + compileRes.errors);
			return createContractFromJson(contractName, compileRes.output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

	private SolidityContractImpl createContractFromJson(String contractName, String json) throws IOException {
		CompilationResult result = CompilationResult.parse(json);
		if (contractName == null) {
		    if (result.contracts.size() > 1) {
		        throw new RuntimeException("Source contains more than 1 contact (" + result.contracts.keySet() + "). Please specify the contract name");
		    } else {
		        contractName = result.contracts.keySet().iterator().next();
		    }
		}

		SolidityContractImpl contract = new SolidityContractImpl(result.contracts.get(contractName));

		for (CompilationResult.ContractMetadata metadata : result.contracts.values()) {
		    contract.addRelatedContract(metadata.abi);
		}
		return contract;
	}

    @Override
    public SolidityContract createExistingContractFromSrc(String soliditySrc, String contractName, byte[] contractAddress) {
        SolidityContractImpl contract = createContract(soliditySrc, contractName);
        contract.setAddress(contractAddress);
        return contract;
    }

    @Override
    public SolidityContract createExistingContractFromSrc(String soliditySrc, byte[] contractAddress) {
        return createExistingContractFromSrc(soliditySrc, null, contractAddress);
    }

    @Override
    public SolidityContract createExistingContractFromABI(String ABI, byte[] contractAddress) {
        SolidityContractImpl contract = new SolidityContractImpl(ABI);
        contract.setAddress(contractAddress);
        return contract;
    }

    @Override
    public BlockchainImpl getBlockchain() {
        if (blockchain == null) {
            blockchain = createBlockchain(genesis);
            blockchain.setMinerCoinbase(coinbase);
            addEthereumListener(new EthereumListenerAdapter() {
                @Override
                public void onBlock(BlockSummary blockSummary) {
                    lastSummary = blockSummary;
                }
            });
        }
        return blockchain;
    }

    public void addEthereumListener(EthereumListener listener) {
        getBlockchain();
        this.listener.addListener(listener);
    }

    protected void submitNewTx(PendingTx tx) {
        submittedTxes.add(tx);
        if (autoBlock) {
            createBlock();
        }
    }

    public HashMapDB<byte[]> getStateDS() {
        return stateDS;
    }

    public Source<byte[], byte[]> getPruningStateDS() {
        return pruningStateDS;
    }

    public long getTotalDbHits() {
        return totalDbHits;
    }

    private BlockchainImpl createBlockchain(Genesis genesis) {
        //initialize blockchain
        IndexedBlockStore blockStore = new IndexedBlockStore();
        blockStore.init(new HashMapDB<byte[]>(), new HashMapDB<byte[]>());

        stateDS = new HashMapDB<>();
        pruningStateDS = new JournalSource<>(new CountingBytesSource(stateDS));
        pruneManager = new PruneManager(blockStore, pruningStateDS, SystemProperties.getDefault().databasePruneDepth());

        RepositoryRoot repository = new RepositoryRoot(pruningStateDS);

        ProgramInvokeFactoryImpl programInvokeFactory = new ProgramInvokeFactoryImpl();
        listener = new CompositeEthereumListener();

        BlockchainImpl blockchain = new BlockchainImpl(blockStore, repository)
                .withEthereumListener(listener)
                .withSyncManager(new SyncManager());
        blockchain.setParentHeaderValidator(new DependentBlockHeaderRuleAdapter());
        blockchain.setProgramInvokeFactory(programInvokeFactory);
        blockchain.setPruneManager(pruneManager);

        blockchain.byTest = true;

        pendingState = new PendingStateImpl(listener, blockchain);

        pendingState.setBlockchain(blockchain);
        blockchain.setPendingState(pendingState);

        Repository track = repository.startTracking();
        for (ByteArrayWrapper key : genesis.getPremine().keySet()) {
            track.createAccount(key.getData());
            track.addBalance(key.getData(), genesis.getPremine().get(key).getBalance());
        }

        track.commit();
        repository.commit();

        blockStore.saveBlock(genesis, genesis.getCumulativeDifficulty(), true);

        blockchain.setBestBlock(genesis);
        blockchain.setTotalDifficulty(genesis.getCumulativeDifficulty());

        pruneManager.blockCommitted(genesis.getHeader());

        return blockchain;
    }
}
