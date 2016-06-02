package org.ethereum.util.blockchain;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.CommonConfig;
import org.ethereum.core.*;
import org.ethereum.core.genesis.GenesisLoader;
import org.ethereum.crypto.ECKey;
import org.ethereum.datasource.HashMapDB;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.db.RepositoryImpl;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.manager.AdminInfo;
import org.ethereum.mine.Ethash;
import org.ethereum.solidity.compiler.CompilationResult;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.ethereum.util.ByteUtil;
import org.ethereum.validator.DependentBlockHeaderRuleAdapter;
import org.ethereum.vm.DataWord;
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
    ECKey txSender;
    long gasPrice;
    long gasLimit;
    boolean autoBlock;
    List<Pair<byte[], BigInteger>> initialBallances = new ArrayList<>();

    class PendingTx {
        ECKey sender;
        byte[] toAddress;
        BigInteger value;
        byte[] data;

        SolidityContractImpl createdContract;
        SolidityContractImpl targetContract;

        public PendingTx(byte[] toAddress, BigInteger value, byte[] data) {
            this.sender = txSender;
            this.toAddress = toAddress;
            this.value = value;
            this.data = data;
        }

        public PendingTx(byte[] toAddress, BigInteger value, byte[] data,
                         SolidityContractImpl createdContract, SolidityContractImpl targetContract) {
            this.sender = txSender;
            this.toAddress = toAddress;
            this.value = value;
            this.data = data;
            this.createdContract = createdContract;
            this.targetContract = targetContract;
        }
    }

    List<PendingTx> submittedTxes = new ArrayList<>();

    public StandaloneBlockchain() {
        withGenesis(GenesisLoader.loadGenesis(
                getClass().getResourceAsStream("/genesis/genesis-light.json")));
        withGasPrice(50_000_000_000L);
        withGasLimit(5_000_000L);
        withMinerCoinbase(Hex.decode("ffffffffffffffffffffffffffffffffffffffff"));
        setSender(ECKey.fromPrivate(Hex.decode("3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c")));
//        withAccountBalance(txSender.getAddress(), new BigInteger("100000000000000000000000000"));
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

    @Override
    public Block createBlock() {
        return createForkBlock(getBlockchain().getBestBlock());
    }

    @Override
    public Block createForkBlock(Block parent) {
        try {
            List<Transaction> txes = new ArrayList<>();
            Map<ByteArrayWrapper, Long> nonces = new HashMap<>();
            Repository repoSnapshot = getBlockchain().getRepository().getSnapshotTo(parent.getStateRoot());
            for (PendingTx tx : submittedTxes) {
                ByteArrayWrapper senderW = new ByteArrayWrapper(tx.sender.getAddress());
                Long nonce = nonces.get(senderW);
                if (nonce == null) {
                    BigInteger bcNonce = repoSnapshot.getNonce(tx.sender.getAddress());
                    nonce = bcNonce.longValue();
                }
                nonces.put(senderW, nonce + 1);

                byte[] toAddress = tx.targetContract != null ? tx.targetContract.getAddress() : tx.toAddress;

                Transaction transaction = new Transaction(ByteUtil.longToBytesNoLeadZeroes(nonce),
                        ByteUtil.longToBytesNoLeadZeroes(gasPrice),
                        ByteUtil.longToBytesNoLeadZeroes(gasLimit),
                        toAddress, ByteUtil.bigIntegerToBytes(tx.value), tx.data);
                transaction.sign(tx.sender.getPrivKeyBytes());
                if (tx.createdContract != null) {
                    tx.createdContract.setAddress(transaction.getContractAddress());
                }
                txes.add(transaction);
            }
            Block b = getBlockchain().createNewBlock(parent, txes, Collections.EMPTY_LIST);
            Ethash.getForBlock(b.getNumber()).mineLight(b).get();
            ImportResult importResult = getBlockchain().tryToConnect(b);
            if (importResult != ImportResult.IMPORTED_BEST && importResult != ImportResult.IMPORTED_NOT_BEST) {
                throw new RuntimeException("Invalid block import result " + importResult + " for block " + b);
            }
            submittedTxes.clear();
            return b;
        } catch (InterruptedException|ExecutionException e) {
            throw new RuntimeException(e);
        }
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

    @Override
    public SolidityContract submitNewContract(String soliditySrc) {
        return submitNewContract(soliditySrc, null);
    }

    @Override
    public SolidityContract submitNewContract(String soliditySrc, String contractName) {
        SolidityContractImpl contract = createContract(soliditySrc, contractName);
        submitNewTx(new PendingTx(new byte[0], BigInteger.ZERO,
                Hex.decode(contract.getBinary()), contract, null));
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

    private void submitNewTx(PendingTx tx) {
        submittedTxes.add(tx);
        if (autoBlock) {
            createBlock();
        }
    }

    private BlockchainImpl createBlockchain(Genesis genesis) {
        IndexedBlockStore blockStore = new IndexedBlockStore();
        blockStore.init(new HashMapDB(), new HashMapDB());

        Repository repository = new RepositoryImpl(new HashMapDB(), new HashMapDB());

        ProgramInvokeFactoryImpl programInvokeFactory = new ProgramInvokeFactoryImpl();
        EthereumListenerAdapter listener = new EthereumListenerAdapter();

        BlockchainImpl blockchain = new BlockchainImpl(blockStore, repository);
        blockchain.setParentHeaderValidator(new DependentBlockHeaderRuleAdapter());
        blockchain.setProgramInvokeFactory(programInvokeFactory);
        programInvokeFactory.setBlockchain(blockchain);

        blockchain.byTest = true;

        PendingStateImpl pendingState = new PendingStateImpl(listener, blockchain);

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

        blockStore.saveBlock(genesis, genesis.getCumulativeDifficulty(), true);

        blockchain.setBestBlock(genesis);
        blockchain.setTotalDifficulty(genesis.getCumulativeDifficulty());

        return blockchain;
    }

    class SolidityContractImpl implements SolidityContract {
        byte[] address;
        CompilationResult.ContractMetadata compiled;
        CallTransaction.Contract contract;

        public SolidityContractImpl(String abi) {
            contract = new CallTransaction.Contract(abi);
        }
        public SolidityContractImpl(CompilationResult.ContractMetadata result) {
            compiled = result;
            contract = new CallTransaction.Contract(compiled.abi);
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
        public Object[] callFunction(String functionName, Object... args) {
            return callFunction(0, functionName, args);
        }

        @Override
        public Object[] callFunction(long value, String functionName, Object... args) {
            byte[] data = contract.getByName(functionName).encode(args);
            submitNewTx(new PendingTx(null, BigInteger.valueOf(value), data, null, this));
            return null; // TODO return either Future or pending state
        }

        @Override
        public Object[] callConstFunction(String functionName, Object... args) {
            return callConstFunction(getBlockchain().getBestBlock(), functionName, args);
        }

        @Override
        public Object[] callConstFunction(Block callBlock, String functionName, Object... args) {

            Transaction tx = CallTransaction.createCallTransaction(0, 0, 100000000000000L,
                    Hex.toHexString(getAddress()), 0, contract.getByName(functionName), args);
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

                return contract.getByName(functionName).decodeResult(executor.getResult().getHReturn());
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
