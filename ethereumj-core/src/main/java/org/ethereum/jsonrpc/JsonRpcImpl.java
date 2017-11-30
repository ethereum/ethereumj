/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.jsonrpc;

import org.apache.commons.collections4.map.LRUMap;
import org.ethereum.config.CommonConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.db.BlockStore;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.core.TransactionInfo;
import org.ethereum.db.TransactionStore;
import org.ethereum.facade.Ethereum;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.manager.WorldManager;
import org.ethereum.mine.BlockMiner;
import org.ethereum.net.client.Capability;
import org.ethereum.net.client.ConfigCapabilities;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.net.server.PeerServer;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.ethereum.sync.SyncManager;
import org.ethereum.util.BuildInfo;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.LogInfo;
import org.ethereum.vm.program.invoke.ProgramInvokeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.max;
import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.jsonrpc.TypeConverter.*;
import static org.ethereum.jsonrpc.TypeConverter.StringHexToByteArray;
import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static org.ethereum.util.ByteUtil.bigIntegerToBytes;

/**
 * Created by Anton Nashatyrev on 25.11.2015.
 */
@Component
@Lazy
public class JsonRpcImpl implements JsonRpc {
    private static final Logger logger = LoggerFactory.getLogger("jsonrpc");



    public class BinaryCallArguments {
        public long nonce;
        public long gasPrice;
        public long gasLimit;
        public String toAddress;
        public long value;
        public byte[] data;
        public void setArguments(CallArguments args) throws Exception {
            nonce = 0;
            if (args.nonce != null && args.nonce.length() != 0)
                nonce = JSonHexToLong(args.nonce);

            gasPrice = 0;
            if (args.gasPrice != null && args.gasPrice.length()!=0)
                gasPrice = JSonHexToLong(args.gasPrice);

            gasLimit = 4_000_000;
            if (args.gas != null && args.gas.length()!=0)
                gasLimit = JSonHexToLong(args.gas);

            toAddress = null;
            if (args.to != null && !args.to.isEmpty())
                toAddress = JSonHexToHex(args.to);

            value=0;
            if (args.value != null && args.value.length()!=0)
                value = JSonHexToLong(args.value);

            data = null;

            if (args.data != null && args.data.length()!=0)
                data = TypeConverter.StringHexToByteArray(args.data);
        }
    }

    @Autowired
    SystemProperties config;

    @Autowired
    ConfigCapabilities configCapabilities;

    @Autowired
    public WorldManager worldManager;

    @Autowired
    public Repository repository;

    @Autowired
    Ethereum eth;

    @Autowired
    PeerServer peerServer;

    @Autowired
    SyncManager syncManager;

    @Autowired
    TransactionStore txStore;

    @Autowired
    ChannelManager channelManager;

    @Autowired
    BlockMiner blockMiner;

    @Autowired
    TransactionStore transactionStore;

    @Autowired
    PendingStateImpl pendingState;

    @Autowired
    SolidityCompiler solidityCompiler;

    @Autowired
    ProgramInvokeFactory programInvokeFactory;

    @Autowired
    CommonConfig commonConfig = CommonConfig.getDefault();

    BlockchainImpl blockchain;

    CompositeEthereumListener compositeEthereumListener;


    long initialBlockNumber;

    Map<ByteArrayWrapper, Account> accounts = new HashMap<>();
    AtomicInteger filterCounter = new AtomicInteger(1);
    Map<Integer, Filter> installedFilters = new Hashtable<>();
    Map<ByteArrayWrapper, TransactionReceipt> pendingReceipts = Collections.synchronizedMap(new LRUMap<ByteArrayWrapper, TransactionReceipt>(1024));

    @Autowired
    public JsonRpcImpl(final BlockchainImpl blockchain, final CompositeEthereumListener compositeEthereumListener) {
        this.blockchain = blockchain;
        this.compositeEthereumListener = compositeEthereumListener;
        initialBlockNumber = blockchain.getBestBlock().getNumber();

        compositeEthereumListener.addListener(new EthereumListenerAdapter() {
            @Override
            public void onBlock(Block block, List<TransactionReceipt> receipts) {
                for (Filter filter : installedFilters.values()) {
                    filter.newBlockReceived(block);
                }
            }

            @Override
            public void onPendingTransactionsReceived(List<Transaction> transactions) {
                for (Filter filter : installedFilters.values()) {
                    for (Transaction tx : transactions) {
                        filter.newPendingTx(tx);
                    }
                }
            }

            @Override
            public void onPendingTransactionUpdate(TransactionReceipt txReceipt, PendingTransactionState state, Block block) {
                ByteArrayWrapper txHashW = new ByteArrayWrapper(txReceipt.getTransaction().getHash());
                if (state.isPending() || state == PendingTransactionState.DROPPED) {
                    pendingReceipts.put(txHashW, txReceipt);
                } else {
                    pendingReceipts.remove(txHashW);
                }
            }
        });

    }

    public long JSonHexToLong(String x) throws Exception {
        if (!x.startsWith("0x"))
            throw new Exception("Incorrect hex syntax");
        x = x.substring(2);
        return Long.parseLong(x, 16);
    }

    public int JSonHexToInt(String x) throws Exception {
        if (!x.startsWith("0x"))
            throw new Exception("Incorrect hex syntax");
        x = x.substring(2);
        return Integer.parseInt(x, 16);
    }

    public String JSonHexToHex(String x) throws Exception {
        if (!x.startsWith("0x"))
            throw new Exception("Incorrect hex syntax");
        x = x.substring(2);
        return x;
    }

    public Block getBlockByJSonHash(String blockHash) throws Exception  {
        byte[] bhash = TypeConverter.StringHexToByteArray(blockHash);
        return worldManager.getBlockchain().getBlockByHash(bhash);
    }

    private Block getByJsonBlockId(String id) {
        if ("earliest".equalsIgnoreCase(id)) {
            return blockchain.getBlockByNumber(0);
        } else if ("latest".equalsIgnoreCase(id)) {
            return blockchain.getBestBlock();
        } else if ("pending".equalsIgnoreCase(id)) {
            return null;
        } else {
            long blockNumber = StringHexToBigInteger(id).longValue();
            return blockchain.getBlockByNumber(blockNumber);
        }
    }

    private Repository getRepoByJsonBlockId(String id) {
        if ("pending".equalsIgnoreCase(id)) {
            return pendingState.getRepository();
        } else {
            Block block = getByJsonBlockId(id);
            return this.repository.getSnapshotTo(block.getStateRoot());
        }
    }

    private List<Transaction> getTransactionsByJsonBlockId(String id) {
        if ("pending".equalsIgnoreCase(id)) {
            return pendingState.getPendingTransactions();
        } else {
            Block block = getByJsonBlockId(id);
            return block != null ? block.getTransactionsList() : null;
        }
    }

    protected Account getAccount(String address) throws Exception {
        return accounts.get(new ByteArrayWrapper(StringHexToByteArray(address)));
    }

    protected Account addAccount(String seed) {
        return addAccount(ECKey.fromPrivate(sha3(seed.getBytes())));
    }

    protected Account addAccount(ECKey key) {
        Account account = new Account();
        account.init(key);
        accounts.put(new ByteArrayWrapper(account.getAddress()), account);
        return account;
    }

    public String web3_clientVersion() {

        String s = "EthereumJ" + "/v" + config.projectVersion() + "/" +
                System.getProperty("os.name") + "/Java1.8/" + config.projectVersionModifier() + "-" + BuildInfo.buildHash;
        if (logger.isDebugEnabled()) logger.debug("web3_clientVersion(): " + s);
        return s;
    };

    public String  web3_sha3(String data) throws Exception {
        String s = null;
        try {
            byte[] result = HashUtil.sha3(TypeConverter.StringHexToByteArray(data));
            return s = TypeConverter.toJsonHex(result);
        } finally {
            if (logger.isDebugEnabled()) logger.debug("web3_sha3(" + data + "): " + s);
        }
    }

    public String net_version() {
        String s = null;
        try {
            return s = eth_protocolVersion();
        } finally {
            if (logger.isDebugEnabled()) logger.debug("net_version(): " + s);
        }
    }

    public String net_peerCount(){
        String s = null;
        try {
            int n = channelManager.getActivePeers().size();
            return s = TypeConverter.toJsonHex(n);
        } finally {
            if (logger.isDebugEnabled()) logger.debug("net_peerCount(): " + s);
        }
    }

    public boolean net_listening() {
        Boolean s = null;
        try {
            return s = peerServer.isListening();
        }finally {
            if (logger.isDebugEnabled()) logger.debug("net_listening(): " + s);
        }
    }

    public String eth_protocolVersion(){
        String s = null;
        try {
            int version = 0;
            for (Capability capability : configCapabilities.getConfigCapabilities()) {
                if (capability.isEth()) {
                    version = max(version, capability.getVersion());
                }
            }
            return s = Integer.toString(version);
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_protocolVersion(): " + s);
        }
    }

    public SyncingResult eth_syncing(){
        SyncingResult s = new SyncingResult();
        try {
            s.startingBlock = TypeConverter.toJsonHex(initialBlockNumber);
            s.currentBlock = TypeConverter.toJsonHex(blockchain.getBestBlock().getNumber());
            s.highestBlock = TypeConverter.toJsonHex(syncManager.getLastKnownBlockNumber());

            return s;
        }finally {
            if (logger.isDebugEnabled()) logger.debug("eth_syncing(): " + s);
        }
    };

    public String eth_coinbase() {
        String s = null;
        try {
            return s = toJsonHex(blockchain.getMinerCoinbase());
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_coinbase(): " + s);
        }
    }

    public boolean eth_mining() {
        Boolean s = null;
        try {
            return s = blockMiner.isMining();
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_mining(): " + s);
        }
    }


    public String eth_hashrate() {
        String s = null;
        try {
            return s = null;
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_hashrate(): " + s);
        }
    }

    public String eth_gasPrice(){
        String s = null;
        try {
            return s = TypeConverter.toJsonHex(eth.getGasPrice());
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_gasPrice(): " + s);
        }
    }

    public String[] eth_accounts() {
        String[] s = null;
        try {
            return s = personal_listAccounts();
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_accounts(): " + Arrays.toString(s));
        }
    }

    public String eth_blockNumber(){
        String s = null;
        try {
            Block bestBlock = blockchain.getBestBlock();
            long b = 0;
            if (bestBlock != null) {
                b = bestBlock.getNumber();
            }
            return s = TypeConverter.toJsonHex(b);
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_blockNumber(): " + s);
        }
    }


    public String eth_getBalance(String address, String blockId) throws Exception {
        String s = null;
        try {
            byte[] addressAsByteArray = TypeConverter.StringHexToByteArray(address);
            BigInteger balance = getRepoByJsonBlockId(blockId).getBalance(addressAsByteArray);
            return s = TypeConverter.toJsonHex(balance);
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_getBalance(" + address + ", " + blockId + "): " + s);
        }
    }

    public String eth_getBalance(String address) throws Exception {
        String s = null;
        try {
            return s = eth_getBalance(address, "latest");
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_getBalance(" + address + "): " + s);
        }
    }

    @Override
    public String eth_getStorageAt(String address, String storageIdx, String blockId) throws Exception {
        String s = null;
        try {
            byte[] addressAsByteArray = StringHexToByteArray(address);
            DataWord storageValue = getRepoByJsonBlockId(blockId).
                    getStorageValue(addressAsByteArray, new DataWord(StringHexToByteArray(storageIdx)));
            return s = TypeConverter.toJsonHex(storageValue.getData());
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_getStorageAt(" + address + ", " + storageIdx + ", " + blockId + "): " + s);
        }
    }

    @Override
    public String eth_getTransactionCount(String address, String blockId) throws Exception {
        String s = null;
        try {
            byte[] addressAsByteArray = TypeConverter.StringHexToByteArray(address);
            BigInteger nonce = getRepoByJsonBlockId(blockId).getNonce(addressAsByteArray);
            return s = TypeConverter.toJsonHex(nonce);
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_getTransactionCount(" + address + ", " + blockId + "): " + s);
        }
    }

    public String eth_getBlockTransactionCountByHash(String blockHash) throws Exception {
        String s = null;
        try {
            Block b = getBlockByJSonHash(blockHash);
            if (b == null) return null;
            long n = b.getTransactionsList().size();
            return s = TypeConverter.toJsonHex(n);
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_getBlockTransactionCountByHash(" + blockHash + "): " + s);
        }
    }

    public String eth_getBlockTransactionCountByNumber(String bnOrId) throws Exception {
        String s = null;
        try {
            List<Transaction> list = getTransactionsByJsonBlockId(bnOrId);
            if (list == null) return null;
            long n = list.size();
            return s = TypeConverter.toJsonHex(n);
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_getBlockTransactionCountByNumber(" + bnOrId + "): " + s);
        }
    }

    public String eth_getUncleCountByBlockHash(String blockHash) throws Exception {
        String s = null;
        try {
            Block b = getBlockByJSonHash(blockHash);
            if (b == null) return null;
            long n = b.getUncleList().size();
            return s = TypeConverter.toJsonHex(n);
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_getUncleCountByBlockHash(" + blockHash + "): " + s);
        }
    }

    public String eth_getUncleCountByBlockNumber(String bnOrId) throws Exception {
        String s = null;
        try {
            Block b = getByJsonBlockId(bnOrId);
            if (b == null) return null;
            long n = b.getUncleList().size();
            return s = TypeConverter.toJsonHex(n);
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_getUncleCountByBlockNumber(" + bnOrId + "): " + s);
        }
    }

    public String eth_getCode(String address, String blockId) throws Exception {
        String s = null;
        try {
            byte[] addressAsByteArray = TypeConverter.StringHexToByteArray(address);
            byte[] code = getRepoByJsonBlockId(blockId).getCode(addressAsByteArray);
            return s = TypeConverter.toJsonHex(code);
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_getCode(" + address + ", " + blockId + "): " + s);
        }
    }

    public String eth_sign(String addr,String data) throws Exception {
        String s = null;
        try {
            String ha = JSonHexToHex(addr);
            Account account = getAccount(ha);

            if (account==null)
                throw new Exception("Inexistent account");

            // Todo: is not clear from the spec what hash function must be used to sign
            byte[] masgHash= HashUtil.sha3(TypeConverter.StringHexToByteArray(data));
            ECKey.ECDSASignature signature = account.getEcKey().sign(masgHash);
            // Todo: is not clear if result should be RlpEncoded or serialized by other means
            byte[] rlpSig = RLP.encode(signature);
            return s = TypeConverter.toJsonHex(rlpSig);
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_sign(" + addr + ", " + data + "): " + s);
        }
    }

    public String eth_sendTransaction(CallArguments args) throws Exception {

        String s = null;
        try {
            Account account = getAccount(JSonHexToHex(args.from));

            if (account == null)
                throw new Exception("From address private key could not be found in this node");

            if (args.data != null && args.data.startsWith("0x"))
                args.data = args.data.substring(2);

            Transaction tx = new Transaction(
                    args.nonce != null ? StringHexToByteArray(args.nonce) : bigIntegerToBytes(pendingState.getRepository().getNonce(account.getAddress())),
                    args.gasPrice != null ? StringHexToByteArray(args.gasPrice) : ByteUtil.longToBytesNoLeadZeroes(eth.getGasPrice()),
                    args.gas != null ? StringHexToByteArray(args.gas) : ByteUtil.longToBytes(90_000),
                    args.to != null ? StringHexToByteArray(args.to) : EMPTY_BYTE_ARRAY,
                    args.value != null ? StringHexToByteArray(args.value) : EMPTY_BYTE_ARRAY,
                    args.data != null ? StringHexToByteArray(args.data) : EMPTY_BYTE_ARRAY,
                    eth.getChainIdForNextBlock());
            tx.sign(account.getEcKey().getPrivKeyBytes());

            eth.submitTransaction(tx);

            return s = TypeConverter.toJsonHex(tx.getHash());
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_sendTransaction(" + args + "): " + s);
        }
    }

    public String eth_sendTransaction(String from, String to, String gas,
                                      String gasPrice, String value,String data,String nonce) throws Exception {
        String s = null;
        try {
            Transaction tx = new Transaction(
                    TypeConverter.StringHexToByteArray(nonce),
                    TypeConverter.StringHexToByteArray(gasPrice),
                    TypeConverter.StringHexToByteArray(gas),
                    TypeConverter.StringHexToByteArray(to), /*receiveAddress*/
                    TypeConverter.StringHexToByteArray(value),
                    TypeConverter.StringHexToByteArray(data),
                    eth.getChainIdForNextBlock());

            Account account = getAccount(from);
            if (account == null) throw new RuntimeException("No account " + from);

            tx.sign(account.getEcKey());

            eth.submitTransaction(tx);

            return s = TypeConverter.toJsonHex(tx.getHash());
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_sendTransaction(" +
                    "from = [" + from + "], to = [" + to + "], gas = [" + gas + "], gasPrice = [" + gasPrice +
                    "], value = [" + value + "], data = [" + data + "], nonce = [" + nonce + "]" + "): " + s);
        }
    }

    public String eth_sendRawTransaction(String rawData) throws Exception {
        String s = null;
        try {
            Transaction tx = new Transaction(StringHexToByteArray(rawData));
            tx.verify();

            eth.submitTransaction(tx);

            return s = TypeConverter.toJsonHex(tx.getHash());
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_sendRawTransaction(" + rawData + "): " + s);
        }
    }

    public TransactionReceipt createCallTxAndExecute(CallArguments args, Block block) throws Exception {
        Repository repository = ((Repository) worldManager.getRepository())
                .getSnapshotTo(block.getStateRoot())
                .startTracking();

        return createCallTxAndExecute(args, block, repository, worldManager.getBlockStore());
    }

    public TransactionReceipt createCallTxAndExecute(CallArguments args, Block block, Repository repository, BlockStore blockStore) throws Exception {
        BinaryCallArguments bca = new BinaryCallArguments();
        bca.setArguments(args);
        Transaction tx = CallTransaction.createRawTransaction(0,
                bca.gasPrice,
                bca.gasLimit,
                bca.toAddress,
                bca.value,
                bca.data);

        // put mock signature if not present
        if (tx.getSignature() == null) {
            tx.sign(ECKey.fromPrivate(new byte[32]));
        }

        try {
            TransactionExecutor executor = new TransactionExecutor
                    (tx, block.getCoinbase(), repository, blockStore,
                            programInvokeFactory, block, new EthereumListenerAdapter(), 0)
                    .withCommonConfig(commonConfig)
                    .setLocalCall(true);

            executor.init();
            executor.execute();
            executor.go();
            executor.finalization();

            return executor.getReceipt();
        } finally {
            repository.rollback();
        }
    }

    public String eth_call(CallArguments args, String bnOrId) throws Exception {

        String s = null;
        try {
            TransactionReceipt res;
            if ("pending".equals(bnOrId)) {
                Block pendingBlock = blockchain.createNewBlock(blockchain.getBestBlock(), pendingState.getPendingTransactions(), Collections.<BlockHeader>emptyList());
                res = createCallTxAndExecute(args, pendingBlock, pendingState.getRepository(), worldManager.getBlockStore());
            } else {
                res = createCallTxAndExecute(args, getByJsonBlockId(bnOrId));
            }
            return s = TypeConverter.toJsonHex(res.getExecutionResult());
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_call(" + args + "): " + s);
        }
    }

    public String eth_estimateGas(CallArguments args) throws Exception {
        String s = null;
        try {
            TransactionReceipt res = createCallTxAndExecute(args, blockchain.getBestBlock());
            return s = TypeConverter.toJsonHex(res.getGasUsed());
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_estimateGas(" + args + "): " + s);
        }
    }


    public BlockResult getBlockResult(Block b, boolean fullTx) {
        if (b==null)
            return null;
        boolean isPending = ByteUtil.byteArrayToLong(b.getNonce()) == 0;
        BlockResult br = new BlockResult();
        br.number = isPending ? null : TypeConverter.toJsonHex(b.getNumber());
        br.hash = isPending ? null : TypeConverter.toJsonHex(b.getHash());
        br.parentHash = TypeConverter.toJsonHex(b.getParentHash());
        br.nonce = isPending ? null : TypeConverter.toJsonHex(b.getNonce());
        br.sha3Uncles= TypeConverter.toJsonHex(b.getUnclesHash());
        br.logsBloom = isPending ? null : TypeConverter.toJsonHex(b.getLogBloom());
        br.transactionsRoot =TypeConverter.toJsonHex(b.getTxTrieRoot());
        br.stateRoot = TypeConverter.toJsonHex(b.getStateRoot());
        br.receiptsRoot =TypeConverter.toJsonHex(b.getReceiptsRoot());
        br.miner = isPending ? null : TypeConverter.toJsonHex(b.getCoinbase());
        br.difficulty = TypeConverter.toJsonHex(b.getDifficulty());
        br.totalDifficulty = TypeConverter.toJsonHex(blockchain.getTotalDifficulty());
        if (b.getExtraData() != null)
            br.extraData =TypeConverter.toJsonHex(b.getExtraData());
        br.size = TypeConverter.toJsonHex(b.getEncoded().length);
        br.gasLimit =TypeConverter.toJsonHex(b.getGasLimit());
        br.gasUsed =TypeConverter.toJsonHex(b.getGasUsed());
        br.timestamp =TypeConverter.toJsonHex(b.getTimestamp());

        List<Object> txes = new ArrayList<>();
        if (fullTx) {
            for (int i = 0; i < b.getTransactionsList().size(); i++) {
                txes.add(new TransactionResultDTO(b, i, b.getTransactionsList().get(i)));
            }
        } else {
            for (Transaction tx : b.getTransactionsList()) {
                txes.add(toJsonHex(tx.getHash()));
            }
        }
        br.transactions = txes.toArray();

        List<String> ul = new ArrayList<>();
        for (BlockHeader header : b.getUncleList()) {
            ul.add(toJsonHex(header.getHash()));
        }
        br.uncles = ul.toArray(new String[ul.size()]);

        return br;
    }

    public BlockResult eth_getBlockByHash(String blockHash,Boolean fullTransactionObjects) throws Exception {
        BlockResult s = null;
        try {
            Block b = getBlockByJSonHash(blockHash);
            return getBlockResult(b, fullTransactionObjects);
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_getBlockByHash(" +  blockHash + ", " + fullTransactionObjects + "): " + s);
        }
    }

    public BlockResult eth_getBlockByNumber(String bnOrId,Boolean fullTransactionObjects) throws Exception {
        BlockResult s = null;
        try {
            Block b;
            if ("pending".equalsIgnoreCase(bnOrId)) {
                b = blockchain.createNewBlock(blockchain.getBestBlock(), pendingState.getPendingTransactions(), Collections.<BlockHeader>emptyList());
            } else {
                b = getByJsonBlockId(bnOrId);
            }
            return s = (b == null ? null : getBlockResult(b, fullTransactionObjects));
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_getBlockByNumber(" +  bnOrId + ", " + fullTransactionObjects + "): " + s);
        }
    }

    public TransactionResultDTO eth_getTransactionByHash(String transactionHash) throws Exception {
        TransactionResultDTO s = null;
        try {
            byte[] txHash = StringHexToByteArray(transactionHash);
            Block block = null;

            TransactionInfo txInfo = blockchain.getTransactionInfo(txHash);

            if (txInfo == null) {
                TransactionReceipt receipt = pendingReceipts.get(new ByteArrayWrapper(txHash));

                if (receipt == null) {
                    return null;
                }
                txInfo = new TransactionInfo(receipt);
            } else {
                block = blockchain.getBlockByHash(txInfo.getBlockHash());
                // need to return txes only from main chain
                Block mainBlock = blockchain.getBlockByNumber(block.getNumber());
                if (!Arrays.equals(block.getHash(), mainBlock.getHash())) {
                    return null;
                }
                txInfo.setTransaction(block.getTransactionsList().get(txInfo.getIndex()));
            }

            return s = new TransactionResultDTO(block, txInfo.getIndex(), txInfo.getReceipt().getTransaction());
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_getTransactionByHash(" + transactionHash + "): " + s);
        }
    }

    public TransactionResultDTO eth_getTransactionByBlockHashAndIndex(String blockHash,String index) throws Exception {
        TransactionResultDTO s = null;
        try {
            Block b = getBlockByJSonHash(blockHash);
            if (b == null) return null;
            int idx = JSonHexToInt(index);
            if (idx >= b.getTransactionsList().size()) return null;
            Transaction tx = b.getTransactionsList().get(idx);
            return s = new TransactionResultDTO(b, idx, tx);
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_getTransactionByBlockHashAndIndex(" + blockHash + ", " + index + "): " + s);
        }
    }

    public TransactionResultDTO eth_getTransactionByBlockNumberAndIndex(String bnOrId, String index) throws Exception {
        TransactionResultDTO s = null;
        try {
            Block b = getByJsonBlockId(bnOrId);
            List<Transaction> txs = getTransactionsByJsonBlockId(bnOrId);
            if (txs == null) return null;
            int idx = JSonHexToInt(index);
            if (idx >= txs.size()) return null;
            Transaction tx = txs.get(idx);
            return s = new TransactionResultDTO(b, idx, tx);
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_getTransactionByBlockNumberAndIndex(" + bnOrId + ", " + index + "): " + s);
        }
    }

    public TransactionReceiptDTO eth_getTransactionReceipt(String transactionHash) throws Exception {
        TransactionReceiptDTO s = null;
        try {
            byte[] hash = TypeConverter.StringHexToByteArray(transactionHash);

            TransactionReceipt pendingReceipt = pendingReceipts.get(new ByteArrayWrapper(hash));

            TransactionInfo txInfo;
            Block block;

            if (pendingReceipt != null) {
                txInfo = new TransactionInfo(pendingReceipt);
                block = null;
            } else {
                txInfo = blockchain.getTransactionInfo(hash);

                if (txInfo == null)
                    return null;

                block = blockchain.getBlockByHash(txInfo.getBlockHash());

                // need to return txes only from main chain
                Block mainBlock = blockchain.getBlockByNumber(block.getNumber());
                if (!Arrays.equals(block.getHash(), mainBlock.getHash())) {
                    return null;
                }
            }

            return s = new TransactionReceiptDTO(block, txInfo);
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_getTransactionReceipt(" + transactionHash + "): " + s);
        }
    }

    @Override
    public TransactionReceiptDTOExt ethj_getTransactionReceipt(String transactionHash) throws Exception {
        TransactionReceiptDTOExt s = null;
        try {
            byte[] hash = TypeConverter.StringHexToByteArray(transactionHash);

            TransactionReceipt pendingReceipt = pendingReceipts.get(new ByteArrayWrapper(hash));

            TransactionInfo txInfo;
            Block block;

            if (pendingReceipt != null) {
                txInfo = new TransactionInfo(pendingReceipt);
                block = null;
            } else {
                txInfo = blockchain.getTransactionInfo(hash);

                if (txInfo == null)
                    return null;

                block = blockchain.getBlockByHash(txInfo.getBlockHash());

                // need to return txes only from main chain
                Block mainBlock = blockchain.getBlockByNumber(block.getNumber());
                if (!Arrays.equals(block.getHash(), mainBlock.getHash())) {
                    return null;
                }
            }

            return s = new TransactionReceiptDTOExt(block, txInfo);
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_getTransactionReceipt(" + transactionHash + "): " + s);
        }
    }

    @Override
    public BlockResult eth_getUncleByBlockHashAndIndex(String blockHash, String uncleIdx) throws Exception {
        BlockResult s = null;
        try {
            Block block = blockchain.getBlockByHash(StringHexToByteArray(blockHash));
            if (block == null) return null;
            int idx = JSonHexToInt(uncleIdx);
            if (idx >= block.getUncleList().size()) return null;
            BlockHeader uncleHeader = block.getUncleList().get(idx);
            Block uncle = blockchain.getBlockByHash(uncleHeader.getHash());
            if (uncle == null) {
                uncle = new Block(uncleHeader, Collections.<Transaction>emptyList(), Collections.<BlockHeader>emptyList());
            }
            return s = getBlockResult(uncle, false);
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_getUncleByBlockHashAndIndex(" + blockHash + ", " + uncleIdx + "): " + s);
        }
    }

    @Override
    public BlockResult eth_getUncleByBlockNumberAndIndex(String blockId, String uncleIdx) throws Exception {
        BlockResult s = null;
        try {
            Block block = getByJsonBlockId(blockId);
            return s = block == null ? null :
                    eth_getUncleByBlockHashAndIndex(toJsonHex(block.getHash()), uncleIdx);
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_getUncleByBlockNumberAndIndex(" + blockId + ", " + uncleIdx + "): " + s);
        }
    }

    @Override
    public String[] eth_getCompilers() {
        String[] s = null;
        try {
            return s = new String[] {"solidity"};
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_getCompilers(): " + Arrays.toString(s));
        }
    }

    @Override
    public CompilationResult eth_compileLLL(String contract) {
        throw new UnsupportedOperationException("LLL compiler not supported");
    }

    @Override
    public CompilationResult eth_compileSolidity(String contract) throws Exception {
        CompilationResult s = null;
        try {
            SolidityCompiler.Result res = solidityCompiler.compileSrc(
                    contract.getBytes(), true, true, SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN);
            if (res.isFailed()) {
                throw new RuntimeException("Compilation error: " + res.errors);
            }
            org.ethereum.solidity.compiler.CompilationResult result = org.ethereum.solidity.compiler.CompilationResult.parse(res.output);
            CompilationResult ret = new CompilationResult();
            org.ethereum.solidity.compiler.CompilationResult.ContractMetadata contractMetadata = result.contracts.values().iterator().next();
            ret.code = toJsonHex(contractMetadata.bin);
            ret.info = new CompilationInfo();
            ret.info.source = contract;
            ret.info.language = "Solidity";
            ret.info.languageVersion = "0";
            ret.info.compilerVersion = result.version;
            ret.info.abiDefinition = new CallTransaction.Contract(contractMetadata.abi).functions;
            return s = ret;
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_compileSolidity(" + contract + ")" + s);
        }
    }

    @Override
    public CompilationResult eth_compileSerpent(String contract){
        throw new UnsupportedOperationException("Serpent compiler not supported");
    }

    @Override
    public String eth_resend() {
        throw new UnsupportedOperationException("JSON RPC method eth_resend not implemented yet");
    }

    @Override
    public String eth_pendingTransactions() {
        throw new UnsupportedOperationException("JSON RPC method eth_pendingTransactions not implemented yet");
    }

    static class Filter {
        static final int MAX_EVENT_COUNT = 1024; // prevent OOM when Filers are forgotten
        static abstract class FilterEvent {
            public abstract Object getJsonEventObject();
        }
        List<FilterEvent> events = new LinkedList<>();

        public synchronized boolean hasNew() { return !events.isEmpty();}

        public synchronized Object[] poll() {
            Object[] ret = new Object[events.size()];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = events.get(i).getJsonEventObject();
            }
            this.events.clear();
            return ret;
        }

        protected synchronized void add(FilterEvent evt) {
            events.add(evt);
            if (events.size() > MAX_EVENT_COUNT) events.remove(0);
        }

        public void newBlockReceived(Block b) {}
        public void newPendingTx(Transaction tx) {}
    }

    static class NewBlockFilter extends Filter {
        class NewBlockFilterEvent extends FilterEvent {
            public final Block b;
            NewBlockFilterEvent(Block b) {this.b = b;}

            @Override
            public String getJsonEventObject() {
                return toJsonHex(b.getHash());
            }
        }

        public void newBlockReceived(Block b) {
            add(new NewBlockFilterEvent(b));
        }
    }

    static class PendingTransactionFilter extends Filter {
        class PendingTransactionFilterEvent extends FilterEvent {
            private final Transaction tx;

            PendingTransactionFilterEvent(Transaction tx) {this.tx = tx;}

            @Override
            public String getJsonEventObject() {
                return toJsonHex(tx.getHash());
            }
        }

        public void newPendingTx(Transaction tx) {
            add(new PendingTransactionFilterEvent(tx));
        }
    }

    class JsonLogFilter extends Filter {
        class LogFilterEvent extends FilterEvent {
            private final LogFilterElement el;

            LogFilterEvent(LogFilterElement el) {
                this.el = el;
            }

            @Override
            public LogFilterElement getJsonEventObject() {
                return el;
            }
        }

        LogFilter logFilter;
        boolean onNewBlock;
        boolean onPendingTx;

        public JsonLogFilter(LogFilter logFilter) {
            this.logFilter = logFilter;
        }

        void onLogMatch(LogInfo logInfo, Block b, int txIndex, Transaction tx, int logIdx) {
            add(new LogFilterEvent(new LogFilterElement(logInfo, b, txIndex, tx, logIdx)));
        }

        void onTransactionReceipt(TransactionReceipt receipt, Block b, int txIndex) {
            if (logFilter.matchBloom(receipt.getBloomFilter())) {
                int logIdx = 0;
                for (LogInfo logInfo : receipt.getLogInfoList()) {
                    if (logFilter.matchBloom(logInfo.getBloom()) && logFilter.matchesExactly(logInfo)) {
                        onLogMatch(logInfo, b, txIndex, receipt.getTransaction(), logIdx);
                    }
                    logIdx++;
                }
            }
        }

        void onTransaction(Transaction tx, Block b, int txIndex) {
            if (logFilter.matchesContractAddress(tx.getReceiveAddress())) {
                TransactionInfo txInfo = blockchain.getTransactionInfo(tx.getHash());
                onTransactionReceipt(txInfo.getReceipt(), b, txIndex);
            }
        }

        void onBlock(Block b) {
            if (logFilter.matchBloom(new Bloom(b.getLogBloom()))) {
                int txIdx = 0;
                for (Transaction tx : b.getTransactionsList()) {
                    onTransaction(tx, b, txIdx);
                    txIdx++;
                }
            }
        }

        @Override
        public void newBlockReceived(Block b) {
            if (onNewBlock) onBlock(b);
        }

        @Override
        public void newPendingTx(Transaction tx) {
            // TODO add TransactionReceipt for PendingTx
//            if (onPendingTx)
        }
    }

    @Override
    public String eth_newFilter(FilterRequest fr) throws Exception {
        String str = null;
        try {
            LogFilter logFilter = new LogFilter();

            if (fr.address instanceof String) {
                logFilter.withContractAddress(StringHexToByteArray((String) fr.address));
            } else if (fr.address instanceof String[]) {
                List<byte[]> addr = new ArrayList<>();
                for (String s : ((String[]) fr.address)) {
                    addr.add(StringHexToByteArray(s));
                }
                logFilter.withContractAddress(addr.toArray(new byte[0][]));
            }

            if (fr.topics != null) {
                for (Object topic : fr.topics) {
                    if (topic == null) {
                        logFilter.withTopic(null);
                    } else if (topic instanceof String) {
                        logFilter.withTopic(new DataWord(StringHexToByteArray((String) topic)).getData());
                    } else if (topic instanceof String[]) {
                        List<byte[]> t = new ArrayList<>();
                        for (String s : ((String[]) topic)) {
                            t.add(new DataWord(StringHexToByteArray(s)).getData());
                        }
                        logFilter.withTopic(t.toArray(new byte[0][]));
                    }
                }
            }

            JsonLogFilter filter = new JsonLogFilter(logFilter);
            int id = filterCounter.getAndIncrement();
            installedFilters.put(id, filter);

            Block blockFrom = fr.fromBlock == null ? null : getByJsonBlockId(fr.fromBlock);
            Block blockTo = fr.toBlock == null ? null : getByJsonBlockId(fr.toBlock);

            if (blockFrom != null) {
                // need to add historical data
                blockTo = blockTo == null ? blockchain.getBestBlock() : blockTo;
                for (long blockNum = blockFrom.getNumber(); blockNum <= blockTo.getNumber(); blockNum++) {
                    filter.onBlock(blockchain.getBlockByNumber(blockNum));
                }
            }

            // the following is not precisely documented
            if ("pending".equalsIgnoreCase(fr.fromBlock) || "pending".equalsIgnoreCase(fr.toBlock)) {
                filter.onPendingTx = true;
            } else if ("latest".equalsIgnoreCase(fr.fromBlock) || "latest".equalsIgnoreCase(fr.toBlock)) {
                filter.onNewBlock = true;
            }

            return str = toJsonHex(id);
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_newFilter(" + fr + "): " + str);
        }
    }

    @Override
    public String eth_newBlockFilter() {
        String s = null;
        try {
            int id = filterCounter.getAndIncrement();
            installedFilters.put(id, new NewBlockFilter());
            return s = toJsonHex(id);
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_newBlockFilter(): " + s);
        }
    }

    @Override
    public String eth_newPendingTransactionFilter() {
        String s = null;
        try {
            int id = filterCounter.getAndIncrement();
            installedFilters.put(id, new PendingTransactionFilter());
            return s = toJsonHex(id);
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_newPendingTransactionFilter(): " + s);
        }
    }

    @Override
    public boolean eth_uninstallFilter(String id) {
        Boolean s = null;
        try {
            if (id == null) return false;
            return s = installedFilters.remove(StringHexToBigInteger(id).intValue()) != null;
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_uninstallFilter(" + id + "): " + s);
        }
    }

    @Override
    public Object[] eth_getFilterChanges(String id) {
        Object[] s = null;
        try {
            Filter filter = installedFilters.get(StringHexToBigInteger(id).intValue());
            if (filter == null) return null;
            return s = filter.poll();
        } finally {
            if (logger.isDebugEnabled()) logger.debug("eth_getFilterChanges(" + id + "): " + Arrays.toString(s));
        }
    }

    @Override
    public Object[] eth_getFilterLogs(String id) {
        logger.debug("eth_getFilterLogs ...");
        return eth_getFilterChanges(id);
    }

    @Override
    public Object[] eth_getLogs(FilterRequest fr) throws Exception {
        logger.debug("eth_getLogs ...");
        String id = eth_newFilter(fr);
        Object[] ret = eth_getFilterChanges(id);
        eth_uninstallFilter(id);
        return ret;
    }

    @Override
    public String eth_getWork() {
        throw new UnsupportedOperationException("JSON RPC method eth_getWork not implemented yet");
    }

    @Override
    public String eth_submitWork() {
        throw new UnsupportedOperationException("JSON RPC method eth_submitWork not implemented yet");
    }

    @Override
    public String eth_submitHashrate() {
        throw new UnsupportedOperationException("JSON RPC method eth_submitHashrate not implemented yet");
    }

    @Override
    public String db_putString() {
        throw new UnsupportedOperationException("JSON RPC method db_putString not implemented yet");
    }

    @Override
    public String db_getString() {
        throw new UnsupportedOperationException("JSON RPC method db_getString not implemented yet");
    }

    @Override
    public String db_putHex() {
        throw new UnsupportedOperationException("JSON RPC method db_putHex not implemented yet");
    }

    @Override
    public String db_getHex() {
        throw new UnsupportedOperationException("JSON RPC method db_getHex not implemented yet");
    }

    @Override
    public String shh_post() {
        throw new UnsupportedOperationException("JSON RPC method shh_post not implemented yet");
    }

    @Override
    public String shh_version() {
        throw new UnsupportedOperationException("JSON RPC method shh_version not implemented yet");
    }

    @Override
    public String shh_newIdentity() {
        throw new UnsupportedOperationException("JSON RPC method shh_newIdentity not implemented yet");
    }

    @Override
    public String shh_hasIdentity() {
        throw new UnsupportedOperationException("JSON RPC method shh_hasIdentity not implemented yet");
    }

    @Override
    public String shh_newGroup() {
        throw new UnsupportedOperationException("JSON RPC method shh_newGroup not implemented yet");
    }

    @Override
    public String shh_addToGroup() {
        throw new UnsupportedOperationException("JSON RPC method shh_addToGroup not implemented yet");
    }

    @Override
    public String shh_newFilter() {
        throw new UnsupportedOperationException("JSON RPC method shh_newFilter not implemented yet");
    }

    @Override
    public String shh_uninstallFilter() {
        throw new UnsupportedOperationException("JSON RPC method shh_uninstallFilter not implemented yet");
    }

    @Override
    public String shh_getFilterChanges() {
        throw new UnsupportedOperationException("JSON RPC method shh_getFilterChanges not implemented yet");
    }

    @Override
    public String shh_getMessages() {
        throw new UnsupportedOperationException("JSON RPC method shh_getMessages not implemented yet");
    }

    @Override
    public boolean admin_addPeer(String s) {
        eth.connect(new Node(s));
        return true;
    }

    @Override
    public String admin_exportChain() {
        throw new UnsupportedOperationException("JSON RPC method admin_exportChain not implemented yet");
    }

    @Override
    public String admin_importChain() {
        throw new UnsupportedOperationException("JSON RPC method admin_importChain not implemented yet");
    }

    @Override
    public String admin_sleepBlocks() {
        throw new UnsupportedOperationException("JSON RPC method admin_sleepBlocks not implemented yet");
    }

    @Override
    public String admin_verbosity() {
        throw new UnsupportedOperationException("JSON RPC method admin_verbosity not implemented yet");
    }

    @Override
    public String admin_setSolc() {
        throw new UnsupportedOperationException("JSON RPC method admin_setSolc not implemented yet");
    }

    @Override
    public String admin_startRPC() {
        throw new UnsupportedOperationException("JSON RPC method admin_startRPC not implemented yet");
    }

    @Override
    public String admin_stopRPC() {
        throw new UnsupportedOperationException("JSON RPC method admin_stopRPC not implemented yet");
    }

    @Override
    public String admin_setGlobalRegistrar() {
        throw new UnsupportedOperationException("JSON RPC method admin_setGlobalRegistrar not implemented yet");
    }

    @Override
    public String admin_setHashReg() {
        throw new UnsupportedOperationException("JSON RPC method admin_setHashReg not implemented yet");
    }

    @Override
    public String admin_setUrlHint() {
        throw new UnsupportedOperationException("JSON RPC method admin_setUrlHint not implemented yet");
    }

    @Override
    public String admin_saveInfo() {
        throw new UnsupportedOperationException("JSON RPC method admin_saveInfo not implemented yet");
    }

    @Override
    public String admin_register() {
        throw new UnsupportedOperationException("JSON RPC method admin_register not implemented yet");
    }

    @Override
    public String admin_registerUrl() {
        throw new UnsupportedOperationException("JSON RPC method admin_registerUrl not implemented yet");
    }

    @Override
    public String admin_startNatSpec() {
        throw new UnsupportedOperationException("JSON RPC method admin_startNatSpec not implemented yet");
    }

    @Override
    public String admin_stopNatSpec() {
        throw new UnsupportedOperationException("JSON RPC method admin_stopNatSpec not implemented yet");
    }

    @Override
    public String admin_getContractInfo() {
        throw new UnsupportedOperationException("JSON RPC method admin_getContractInfo not implemented yet");
    }

    @Override
    public String admin_httpGet() {
        throw new UnsupportedOperationException("JSON RPC method admin_httpGet not implemented yet");
    }

    @Override
    public String admin_nodeInfo() {
        throw new UnsupportedOperationException("JSON RPC method admin_nodeInfo not implemented yet");
    }

    @Override
    public String admin_peers() {
        throw new UnsupportedOperationException("JSON RPC method admin_peers not implemented yet");
    }

    @Override
    public String admin_datadir() {
        throw new UnsupportedOperationException("JSON RPC method admin_datadir not implemented yet");
    }

    @Override
    public String net_addPeer() {
        throw new UnsupportedOperationException("JSON RPC method net_addPeer not implemented yet");
    }

    @Override
    public boolean miner_start() {
        blockMiner.startMining();
        return true;
    }

    @Override
    public boolean miner_stop() {
        blockMiner.stopMining();
        return true;
    }

    @Override
    public boolean miner_setEtherbase(String coinBase) throws Exception {
        blockchain.setMinerCoinbase(TypeConverter.StringHexToByteArray(coinBase));
        return true;
    }

    @Override
    public boolean miner_setExtra(String data) throws Exception {
        blockchain.setMinerExtraData(TypeConverter.StringHexToByteArray(data));
        return true;
    }

    @Override
    public boolean miner_setGasPrice(String newMinGasPrice) {
        blockMiner.setMinGasPrice(TypeConverter.StringHexToBigInteger(newMinGasPrice));
        return true;
    }

    @Override
    public boolean miner_startAutoDAG() {
        return false;
    }

    @Override
    public boolean miner_stopAutoDAG() {
        return false;
    }

    @Override
    public boolean miner_makeDAG() {
        return false;
    }

    @Override
    public String miner_hashrate() {
        return "0x01";
    }

    @Override
    public String debug_printBlock() {
        throw new UnsupportedOperationException("JSON RPC method debug_printBlock not implemented yet");
    }

    @Override
    public String debug_getBlockRlp() {
        throw new UnsupportedOperationException("JSON RPC method debug_getBlockRlp not implemented yet");
    }

    @Override
    public String debug_setHead() {
        throw new UnsupportedOperationException("JSON RPC method debug_setHead not implemented yet");
    }

    @Override
    public String debug_processBlock() {
        throw new UnsupportedOperationException("JSON RPC method debug_processBlock not implemented yet");
    }

    @Override
    public String debug_seedHash() {
        throw new UnsupportedOperationException("JSON RPC method debug_seedHash not implemented yet");
    }

    @Override
    public String debug_dumpBlock() {
        throw new UnsupportedOperationException("JSON RPC method debug_dumpBlock not implemented yet");
    }

    @Override
    public String debug_metrics() {
        throw new UnsupportedOperationException("JSON RPC method debug_metrics not implemented yet");
    }

    @Override
    public String personal_newAccount(String seed) {
        String s = null;
        try {
            Account account = addAccount(seed);
            return s = toJsonHex(account.getAddress());
        } finally {
            if (logger.isDebugEnabled()) logger.debug("personal_newAccount(*****): " + s);
        }
    }

    @Override
    public boolean personal_unlockAccount(String addr, String pass, String duration) {
        String s = null;
        try {
            return true;
        } finally {
            if (logger.isDebugEnabled()) logger.debug("personal_unlockAccount(" + addr + ", ***, " + duration + "): " + s);
        }
    }

    @Override
    public String[] personal_listAccounts() {
        String[] ret = new String[accounts.size()];
        try {
            int i = 0;
            for (ByteArrayWrapper addr : accounts.keySet()) {
                ret[i++] = toJsonHex(addr.getData());
            }
            return ret;
        } finally {
            if (logger.isDebugEnabled()) logger.debug("personal_listAccounts(): " + Arrays.toString(ret));
        }
    }
}
