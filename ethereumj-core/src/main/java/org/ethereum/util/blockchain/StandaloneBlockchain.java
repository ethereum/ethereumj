package org.ethereum.util.blockchain;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.CommonConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.core.genesis.GenesisLoader;
import org.ethereum.crypto.ECKey;
import org.ethereum.datasource.HashMapDB;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.db.RepositoryImpl;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.mine.Ethash;
import org.ethereum.solidity.compiler.CompilationResult;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.ethereum.util.ByteUtil;
import org.ethereum.validator.DependentBlockHeaderRuleAdapter;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.LogInfo;
import org.ethereum.vm.program.invoke.ProgramInvokeFactoryImpl;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
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
    List<Pair<byte[], BigInteger>> initialBallances = new ArrayList<>();
    int blockGasIncreasePercent = 0;
    private HashMapDB detailsDS;
    private HashMapDB stateDS;
    private BlockSummary lastSummary;

    class PendingTx {
        ECKey sender;
        byte[] toAddress;
        BigInteger value;
        byte[] data;

        SolidityContractImpl createdContract;
        SolidityContractImpl targetContract;

        Transaction customTx;

        TransactionResult txResult = new TransactionResult();

        public PendingTx(byte[] toAddress, BigInteger value, byte[] data) {
            this.sender = txSender;
            this.toAddress = toAddress;
            this.value = value;
            this.data = data;
        }

        public PendingTx(byte[] toAddress, BigInteger value, byte[] data,
                         SolidityContractImpl createdContract, SolidityContractImpl targetContract, TransactionResult res) {
            this.sender = txSender;
            this.toAddress = toAddress;
            this.value = value;
            this.data = data;
            this.createdContract = createdContract;
            this.targetContract = targetContract;
            this.txResult = res;
        }

        public PendingTx(Transaction customTx) {
            this.customTx = customTx;
        }
    }

    List<PendingTx> submittedTxes = new ArrayList<>();

    public StandaloneBlockchain() {
        withGenesis(GenesisLoader.loadGenesis(
                getClass().getResourceAsStream("/genesis/genesis-light-sb.json")));
        withGasPrice(50_000_000_000L);
        withGasLimit(5_000_000L);
        withMinerCoinbase(Hex.decode("ffffffffffffffffffffffffffffffffffffffff"));
        setSender(ECKey.fromPrivate(Hex.decode("3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c")));
//        withAccountBalance(txSender.getAddress(), new BigInteger("100000000000000000000000000"));
        addEthereumListener(new EthereumListenerAdapter() {
            @Override
            public void onBlock(BlockSummary blockSummary) {
                lastSummary = blockSummary;
            }
        });
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
        initialBallances.add(Pair.of(address, weis));
        return this;
//        Repository repository = blockchain.getRepository();
//        Repository track = repository.startTracking();
//        if (!blockchain.getRepository().isExist(address)) {
//            track.createAccount(address);
//        }
//        track.addBalance(address, weis);
//        track.commit();
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

            Block b = getBlockchain().createNewBlock(parent, new ArrayList<>(txes.values()), Collections.EMPTY_LIST);

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
                pendingTxes.get(i).txResult.executionSummary = lastSummary.getSummaries().get(i);
            }

            submittedTxes.clear();
            return b;
        } catch (InterruptedException|ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public Transaction createTransaction(long nonce, byte[] toAddress, long value, byte[] data) {
        return createTransaction(getSender(), nonce, toAddress, BigInteger.valueOf(value), data);
    }
    public Transaction createTransaction(ECKey sender, long nonce, byte[] toAddress, BigInteger value, byte[] data) {
        Transaction transaction = new Transaction(ByteUtil.longToBytesNoLeadZeroes(nonce),
                ByteUtil.longToBytesNoLeadZeroes(gasPrice),
                ByteUtil.longToBytesNoLeadZeroes(gasLimit),
                toAddress, ByteUtil.bigIntegerToBytes(value), data);
        transaction.sign(sender);
        return transaction;
    }

    public void resetSubmittedTransactions() {
        submittedTxes.clear();
    }

    @Override
    public void setSender(ECKey senderPrivateKey) {
        txSender = senderPrivateKey;
        if (!getBlockchain().getRepository().isExist(senderPrivateKey.getAddress())) {
            Repository repository = getBlockchain().getRepository();
            Repository track = repository.startTracking();
            track.createAccount(senderPrivateKey.getAddress());
            track.commit();
        }
    }

    public ECKey getSender() {
        return txSender;
    }

    @Override
    public void sendEther(byte[] toAddress, BigInteger weis) {
        submitNewTx(new PendingTx(toAddress, weis, new byte[0]));
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
        submitNewTx(new PendingTx(new byte[0], BigInteger.ZERO,
                ByteUtil.merge(Hex.decode(contract.getBinary()), argsEncoded), contract, null, new TransactionResult()));
        return contract;
    }

    private SolidityContractImpl createContract(String soliditySrc, String contractName) {
        try {
            SolidityCompiler.Result compileRes = SolidityCompiler.compile(soliditySrc.getBytes(), true, SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN);
            if (!compileRes.errors.isEmpty()) throw new RuntimeException("Compile error: " + compileRes.errors);
            CompilationResult result = CompilationResult.parse(compileRes.output);
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        }
        return blockchain;
    }

    public void addEthereumListener(EthereumListener listener) {
        this.listener.addListener(listener);
    }

    private void submitNewTx(PendingTx tx) {
        submittedTxes.add(tx);
        if (autoBlock) {
            createBlock();
        }
    }

    public HashMapDB getStateDS() {
        return stateDS;
    }

    public HashMapDB getDetailsDS() {
        return detailsDS;
    }

    private BlockchainImpl createBlockchain(Genesis genesis) {
        IndexedBlockStore blockStore = new IndexedBlockStore();
        blockStore.init(new HashMapDB(), new HashMapDB());

        detailsDS = new HashMapDB();
        stateDS = new HashMapDB();
        RepositoryImpl repository = new RepositoryImpl(detailsDS, stateDS, true)
                .withBlockStore(blockStore);

        ProgramInvokeFactoryImpl programInvokeFactory = new ProgramInvokeFactoryImpl();
        listener = new CompositeEthereumListener();

        BlockchainImpl blockchain = new BlockchainImpl(blockStore, repository)
                .withEthereumListener(listener);
        blockchain.setParentHeaderValidator(new DependentBlockHeaderRuleAdapter());
        blockchain.setProgramInvokeFactory(programInvokeFactory);
        programInvokeFactory.setBlockchain(blockchain);

        blockchain.byTest = true;

        pendingState = new PendingStateImpl(listener, blockchain);

        pendingState.init();

        pendingState.setBlockchain(blockchain);
        blockchain.setPendingState(pendingState);

        Repository track = repository.startTracking();
        for (ByteArrayWrapper key : genesis.getPremine().keySet()) {
            track.createAccount(key.getData());
            track.addBalance(key.getData(), genesis.getPremine().get(key).getBalance());
        }
        for (Pair<byte[], BigInteger> acc : initialBallances) {
            track.createAccount(acc.getLeft());
            track.addBalance(acc.getLeft(), acc.getRight());
        }

        track.commit();
        repository.commitBlock(genesis.getHeader());

        blockStore.saveBlock(genesis, genesis.getCumulativeDifficulty(), true);

        blockchain.setBestBlock(genesis);
        blockchain.setTotalDifficulty(genesis.getCumulativeDifficulty());

        return blockchain;
    }

    class SolidityContractImpl implements SolidityContract {
        byte[] address;
        CompilationResult.ContractMetadata compiled;
        CallTransaction.Contract contract;
        List<CallTransaction.Contract> relatedContracts = new ArrayList<>();

        public SolidityContractImpl(String abi) {
            contract = new CallTransaction.Contract(abi);
        }
        public SolidityContractImpl(CompilationResult.ContractMetadata result) {
            this(result.abi);
            compiled = result;
        }

        public void addRelatedContract(String abi) {
            CallTransaction.Contract c = new CallTransaction.Contract(abi);
            relatedContracts.add(c);
        }

        void setAddress(byte[] address) {
            this.address = address;
        }

        @Override
        public byte[] getAddress() {
            if (address == null) {
                throw new RuntimeException("Contract address will be assigned only after block inclusion. Call createBlock() first.");
            }
            return address;
        }

        @Override
        public SolidityCallResult callFunction(String functionName, Object... args) {
            return callFunction(0, functionName, args);
        }

        @Override
        public SolidityCallResult callFunction(long value, String functionName, Object... args) {
            CallTransaction.Function function = contract.getByName(functionName);
            byte[] data = function.encode(args);
            SolidityCallResult res = new SolidityCallResultImpl(this, function);
            submitNewTx(new PendingTx(null, BigInteger.valueOf(value), data, null, this, res));
            return res;
        }

        @Override
        public Object[] callConstFunction(String functionName, Object... args) {
            return callConstFunction(getBlockchain().getBestBlock(), functionName, args);
        }

        @Override
        public Object[] callConstFunction(Block callBlock, String functionName, Object... args) {

            CallTransaction.Function func = contract.getByName(functionName);
            if (func == null) throw new RuntimeException("No function with name '" + functionName + "'");
            Transaction tx = CallTransaction.createCallTransaction(0, 0, 100000000000000L,
                    Hex.toHexString(getAddress()), 0, func, args);
            tx.sign(new byte[32]);

            Repository repository = getBlockchain().getRepository().getSnapshotTo(callBlock.getStateRoot()).startTracking();

            try {
                org.ethereum.core.TransactionExecutor executor = new org.ethereum.core.TransactionExecutor
                        (tx, callBlock.getCoinbase(), repository, getBlockchain().getBlockStore(),
                                getBlockchain().getProgramInvokeFactory(), callBlock)
                        .setLocalCall(true);

                executor.init();
                executor.execute();
                executor.go();
                executor.finalization();

                return func.decodeResult(executor.getResult().getHReturn());
            } finally {
                repository.rollback();
            }
        }

        @Override
        public SolidityStorage getStorage() {
            return new SolidityStorageImpl(getAddress());
        }

        @Override
        public String getABI() {
            return compiled.abi;
        }

        @Override
        public String getBinary() {
            return compiled.bin;
        }

        @Override
        public void call(byte[] callData) {
            // for this we need cleaner separation of EasyBlockchain to
            // Abstract and Solidity specific
            throw new UnsupportedOperationException();
        }
    }

    public class SolidityCallResultImpl extends SolidityCallResult {
        SolidityContractImpl contract;
        CallTransaction.Function function;

        SolidityCallResultImpl(SolidityContractImpl contract, CallTransaction.Function function) {
            this.contract = contract;
            this.function = function;
        }

        @Override
        public CallTransaction.Function getFunction() {
            return function;
        }

        public List<CallTransaction.Invocation> getEvents() {
            List<CallTransaction.Invocation> ret = new ArrayList<>();
            for (LogInfo logInfo : getReceipt().getLogInfoList()) {
                for (CallTransaction.Contract c : contract.relatedContracts) {
                    CallTransaction.Invocation event = c.parseEvent(logInfo);
                    if (event != null) ret.add(event);
                }
            }
            return ret;
        }

        @Override
        public String toString() {
            String ret = "SolidityCallResult{" +
                    function + ": " +
                    (isIncluded() ? "EXECUTED" : "PENDING") + ", ";
            if (isIncluded()) {
                ret += isSuccessful() ? "SUCCESS" : ("ERR (" + getReceipt().getError() + ")");
                ret += ", ";
                if (isSuccessful()) {
                    ret += "Ret: " + Arrays.toString(getReturnValues()) + ", ";
                    ret += "Events: " + getEvents() + ", ";
                }
            }
            return ret + "}";
        }
    }


    class SolidityStorageImpl implements SolidityStorage {
        byte[] contractAddr;

        public SolidityStorageImpl(byte[] contractAddr) {
            this.contractAddr = contractAddr;
        }

        @Override
        public byte[] getStorageSlot(long slot) {
            return getStorageSlot(new DataWord(slot).getData());
        }

        @Override
        public byte[] getStorageSlot(byte[] slot) {
            DataWord ret = getBlockchain().getRepository().getContractDetails(contractAddr).get(new DataWord(slot));
            return ret.getData();
        }
    }
}
