package org.ethereum.jsonrpc;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.crypto.SHA3Helper;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.TransactionInfo;
import org.ethereum.db.TransactionStore;
import org.ethereum.facade.Ethereum;
import org.ethereum.manager.WorldManager;
import org.ethereum.mine.BlockMiner;
import org.ethereum.net.client.Capability;
import org.ethereum.net.client.ConfigCapabilities;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.net.server.PeerServer;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.ethereum.sync.SyncManager;
import org.ethereum.sync.listener.CompositeSyncListener;
import org.ethereum.sync.listener.SyncListenerAdapter;
import org.ethereum.util.RLP;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.program.ProgramResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.*;

import static java.lang.Math.max;
import static org.ethereum.jsonrpc.TypeConverter.*;
import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static org.ethereum.util.ByteUtil.bigIntegerToBytes;

/**
 * Created by Anton Nashatyrev on 25.11.2015.
 */
@Component
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
            nonce =0;
            gasPrice = 0;
            if (args.gasPrice != null && args.gasPrice.length()!=0)
                gasPrice = JSonHexToLong(args.gasPrice);

            gasLimit = 4_000_000;
            if (args.gasLimit != null && args.gasLimit.length()!=0)
                gasLimit = JSonHexToLong(args.gasLimit);

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
    BlockchainImpl blockchain;

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
    CompositeSyncListener compositeSyncListener;

    @Autowired
    BlockMiner blockMiner;

    @Autowired
    TransactionStore transactionStore;

    long initialBlockNumber;
    long maxBlockNumberSeen;

    Map<ByteArrayWrapper, Account> accounts = new HashMap<>();

    @PostConstruct
    private void init() {
        initialBlockNumber = blockchain.getBestBlock().getNumber();
        compositeSyncListener.add(new SyncListenerAdapter() {
            @Override
            public void onNewBlockNumber(long number) {
                maxBlockNumberSeen = max(maxBlockNumberSeen, number);
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
            // TODO
            throw new UnsupportedOperationException();
        } else {
            long blockNumber = StringHexToBigInteger(id).longValue();
            return blockchain.getBlockByNumber(blockNumber);
        }
    }

    protected Account getAccount(String address) throws Exception {
        return accounts.get(new ByteArrayWrapper(StringHexToByteArray(address)));
    }

    protected Account addAccount(String seed) {
        ECKey key = ECKey.fromPrivate(SHA3Helper.sha3(seed.getBytes()));
        Account account = new Account();
        account.init(key);
        accounts.put(new ByteArrayWrapper(account.getAddress()), account);
        return account;
    }

    public String web3_clientVersion() {
        String s = "EthereumJ" + "/" + SystemProperties.CONFIG.projectVersion() + "/" + SystemProperties.CONFIG.projectVersionModifier();
        if (logger.isDebugEnabled()) logger.debug("web3_clientVersion(): " + s);
        return s;
    };

    public String  web3_sha3(String data) throws Exception {
        byte[] result = HashUtil.sha3(TypeConverter.StringHexToByteArray(data));
        String s = TypeConverter.toJsonHex(result);
        if (logger.isDebugEnabled()) logger.debug("web3_sha3(" + data + "): " + s);
        return s;
    }

    public String net_version() {
        String s = eth_protocolVersion();
        if (logger.isDebugEnabled()) logger.debug("net_version(): " + s);
        return s;
    }

    public String net_peerCount(){
        int n = channelManager.getActivePeers().size();
        String s = TypeConverter.toJsonHex(n);
        if (logger.isDebugEnabled()) logger.debug("net_peerCount(): " + s);
        return s;
    }

    public boolean net_listening() {
        boolean b = peerServer.isListening();
        if (logger.isDebugEnabled()) logger.debug("net_listening(): " + b);
        return b;
    }

    public String eth_protocolVersion(){
        int version = 0;
        for (Capability capability : configCapabilities.getConfigCapabilities()) {
            if (capability.isEth()) {
                version = max(version, capability.getVersion());
            }
        }
        String s = Integer.toString(version);
        if (logger.isDebugEnabled()) logger.debug("eth_protocolVersion(): " + s);
        return s;
    }

    public SyncingResult eth_syncing(){
        SyncingResult s = new SyncingResult();
        s.startingBlock= TypeConverter.toJsonHex(initialBlockNumber);
        s.currentBlock= TypeConverter.toJsonHex(blockchain.getBestBlock().getNumber());
        s.highestBlock= TypeConverter.toJsonHex(maxBlockNumberSeen);

        if (logger.isDebugEnabled()) logger.debug("eth_syncing(): " + s);
        return s;
    };

    public String eth_coinbase() {
        return toJsonHex(blockchain.getMinerCoinbase());
    }

    public boolean eth_mining() {
        return blockMiner.isMining();
    }


    public String eth_hashrate() {
        // Todo: Wait for Osky code
        return TypeConverter.toJsonHex(0);
    }

    public String eth_gasPrice(){
        return TypeConverter.toJsonHex(eth.getGasPrice());
    };

    public String[] eth_accounts() {
        return personal_listAccounts();
    }

    public String eth_blockNumber(){
        Block bestBlock = blockchain.getBestBlock();
        long b = 0;
        if (bestBlock != null) {
            b = bestBlock.getNumber();
        }
        return TypeConverter.toJsonHex(b);
    };


    public String eth_getBalance(String address, String blockId) throws Exception {
        Block block = getByJsonBlockId(blockId);
        byte[] addressAsByteArray = TypeConverter.StringHexToByteArray(address);
        BigInteger balance = this.repository.getSnapshotTo(block.getStateRoot()).getBalance(addressAsByteArray);
        return TypeConverter.toJsonHex(balance);
    }

    public String eth_getBalance(String address) throws Exception {
        return eth_getBalance(address, "latest");
    }

    @Override
    public String eth_getStorageAt(String address, String storageIdx, String blockId) throws Exception {
        Block block = getByJsonBlockId(blockId);
        byte[] addressAsByteArray = StringHexToByteArray(address);
        DataWord storageValue = this.repository.getSnapshotTo(block.getStateRoot()).
                getStorageValue(addressAsByteArray, new DataWord(StringHexToByteArray(storageIdx)));
        return TypeConverter.toJsonHex(storageValue.getData());
    }

    @Override
    public String eth_getTransactionCount(String address, String blockId) throws Exception {
        Block block = getByJsonBlockId(blockId);
        byte[] addressAsByteArray = TypeConverter.StringHexToByteArray(address);
        BigInteger nonce = this.repository.getSnapshotTo(block.getStateRoot()).getNonce(addressAsByteArray);
        return TypeConverter.toJsonHex(nonce);
    }

    public String eth_getBlockTransactionCountByHash(String blockHash) throws Exception {
        Block b = getBlockByJSonHash(blockHash);
        if (b == null) return null;
        long n = b.getTransactionsList().size();
        return TypeConverter.toJsonHex(n);
    };

    public String eth_getBlockTransactionCountByNumber(String bnOrId) throws Exception {
        Block b = getByJsonBlockId(bnOrId);
        if (b == null) return null;
        long n = b.getTransactionsList().size();
        return TypeConverter.toJsonHex(n);
    };

    public String eth_getUncleCountByBlockHash(String blockHash) throws Exception {
        Block b = getBlockByJSonHash(blockHash);
        if (b == null) return null;
        long n = b.getUncleList().size();
        return TypeConverter.toJsonHex(n);
    };

    public String eth_getUncleCountByBlockNumber(String bnOrId) throws Exception {
        Block b = getByJsonBlockId(bnOrId);
        if (b == null) return null;
        long n = b.getUncleList().size();
        return TypeConverter.toJsonHex(n);
    };

    public String eth_getCode(String address, String blockId) throws Exception {
        Block block = getByJsonBlockId(blockId);
        if (block == null) return null;
        byte[] addressAsByteArray = TypeConverter.StringHexToByteArray(address);
        byte[] code = this.repository.getSnapshotTo(block.getStateRoot()).getCode(addressAsByteArray);
        return TypeConverter.toJsonHex(code);

    };

    public String eth_sign(String addr,String data) throws Exception {
        String ha = JSonHexToHex(addr);

        Account account = getAccount(ha);

        if (account==null)
            throw new Exception("Inexistent account");

        // Todo: is not clear from the spec what hash function must be used to sign
        // We assume sha3
        byte[] masgHash= HashUtil.sha3(TypeConverter.StringHexToByteArray(data));
        ECKey.ECDSASignature signature = account.getEcKey().sign(masgHash);
        // Todo: is not clear if result should be RlpEncoded or serialized by other means
        byte[] rlpSig = RLP.encode(signature);
        return TypeConverter.toJsonHex(rlpSig);
    }

    public String eth_sendTransaction(CallArguments args) throws Exception {

        Account account = getAccount(JSonHexToHex(args.from));

        if (account == null)
            throw new Exception("From address private key could not be found in this node");

        if (args.data != null && args.data.startsWith("0x"))
            args.data = args.data.substring(2);

        Transaction tx = new Transaction(
                bigIntegerToBytes(account.getNonce()),
                args.gasPrice != null ? StringNumberAsBytes(args.gasPrice) : EMPTY_BYTE_ARRAY,
                args.gasLimit != null ? StringNumberAsBytes(args.gasLimit) : EMPTY_BYTE_ARRAY,
                args.to != null ? StringHexToByteArray(args.to) : EMPTY_BYTE_ARRAY,
                args.value != null ? StringNumberAsBytes(args.value) : EMPTY_BYTE_ARRAY,
                Hex.decode(args.data));
        tx.sign(account.getEcKey().getPrivKeyBytes());

        eth.submitTransaction(tx);

        return TypeConverter.toJsonHex(tx.getHash());
    }

    // TODO: Remove, obsolete with this params
    public String eth_sendTransaction(String from,String to, String gas,
                                      String gasPrice, String value,String data,String nonce) throws Exception {
        Transaction tx = new Transaction(
                TypeConverter.StringHexToByteArray(nonce),
                TypeConverter.StringHexToByteArray(gasPrice),
                TypeConverter.StringHexToByteArray(gas),
                TypeConverter.StringHexToByteArray(to), /*receiveAddress*/
                TypeConverter.StringHexToByteArray(value),
                TypeConverter.StringHexToByteArray(data));

        eth.submitTransaction(tx);

        return TypeConverter.toJsonHex(tx.getHash());
    }

    public String eth_sendRawTransaction(String rawData) throws Exception {
        Transaction tx = new Transaction(StringHexToByteArray(rawData));

        eth.submitTransaction(tx);

        return TypeConverter.toJsonHex(tx.getHash());
    }

    public ProgramResult createCallTxAndExecute(CallArguments args, Block block) throws Exception {
        BinaryCallArguments bca = new BinaryCallArguments();
        bca.setArguments(args);
        Transaction tx = CallTransaction.createRawTransaction(0,
                bca.gasPrice,
                bca.gasLimit,
                bca.toAddress,
                bca.value,
                bca.data);

        ProgramResult res = eth.callConstant(tx, block);
        return res;
    }

    public String eth_call(CallArguments args, String bnOrId) throws Exception {

        if (logger.isDebugEnabled()) logger.debug("eth_call(" + args + ")");
        ProgramResult res = createCallTxAndExecute(args, getByJsonBlockId(bnOrId));
        String s = TypeConverter.toJsonHex(res.getHReturn());
        if (logger.isDebugEnabled()) logger.debug("eth_call(" + args + "): " + s);
        return s;
    }

    public String eth_estimateGas(CallArguments args) throws Exception {
        ProgramResult res = createCallTxAndExecute(args, blockchain.getBestBlock());
        return TypeConverter.toJsonHex(res.getGasUsed());
    }


    public BlockResult getBlockResult(Block b, boolean fullTx) {
        if (b==null)
            return null;
        BlockResult br = new BlockResult();
        br.number = TypeConverter.toJsonHex(b.getNumber());
        br.hash = TypeConverter.toJsonHex(b.getHash());
        br.parentHash = TypeConverter.toJsonHex(b.getParentHash());
        br.nonce = TypeConverter.toJsonHex(b.getNonce());
        br.sha3Uncles= TypeConverter.toJsonHex(b.getUnclesHash());
        br.logsBloom = TypeConverter.toJsonHex(b.getLogBloom());
        br.transactionsRoot =TypeConverter.toJsonHex(b.getTxTrieRoot());
        br.stateRoot = TypeConverter.toJsonHex(b.getStateRoot());
        br.receiptsRoot =TypeConverter.toJsonHex(b.getReceiptsRoot());
        br.miner = TypeConverter.toJsonHex(b.getCoinbase());
        br.difficulty = TypeConverter.toJsonHex(b.getDifficulty());
        br.totalDifficulty = TypeConverter.toJsonHex(b.getCumulativeDifficulty());
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
        Block b = getBlockByJSonHash(blockHash);
        return getBlockResult(b, fullTransactionObjects);
    }

    public BlockResult eth_getBlockByNumber(String bnOrId,Boolean fullTransactionObjects) throws Exception {
        Block b = getByJsonBlockId(bnOrId);

        return getBlockResult(b, fullTransactionObjects);
    }

    public TransactionResultDTO eth_getTransactionByHash(String transactionHash) throws Exception {
        TransactionInfo txInfo = transactionStore.get(StringHexToByteArray(transactionHash));
        Block block = blockchain.getBlockByHash(txInfo.getBlockHash());
        return new TransactionResultDTO(block, txInfo.getIndex(), block.getTransactionsList().get(txInfo.getIndex()));
    }

    public TransactionResultDTO eth_getTransactionByBlockHashAndIndex(String blockHash,String index) throws Exception {
        Block b = getBlockByJSonHash(blockHash);
        if (b == null) return null;
        int idx = JSonHexToInt(index);
        if (idx >= b.getTransactionsList().size()) return null;
        Transaction tx = b.getTransactionsList().get(idx);
        TransactionResultDTO tr = new TransactionResultDTO(b, idx, tx);
        return tr;
    }

    public TransactionResultDTO eth_getTransactionByBlockNumberAndIndex(String bnOrId, String index) throws Exception {
        Block b = getByJsonBlockId(bnOrId);
        if (b == null) return null;
        int idx = JSonHexToInt(index);
        if (idx >= b.getTransactionsList().size()) return null;
        Transaction tx = b.getTransactionsList().get(idx);
        TransactionResultDTO tr = new TransactionResultDTO(b, idx, tx);
        return tr;
    }

    public TransactionReceiptDTO eth_getTransactionReceipt(String transactionHash) throws Exception {
        byte[] hash = TypeConverter.StringHexToByteArray(transactionHash);
        TransactionInfo txInfo = txStore.get(hash);

        if (txInfo == null)
            return null;

        Block block = blockchain.getBlockByHash(txInfo.getBlockHash());

        return new TransactionReceiptDTO(block, txInfo);
    }

    @Override
    public BlockResult eth_getUncleByBlockHashAndIndex(String blockHash, String uncleIdx) throws Exception {
        Block block = blockchain.getBlockByHash(StringHexToByteArray(blockHash));
        if (block == null) return null;
        int idx = JSonHexToInt(uncleIdx);
        if (idx >= block.getUncleList().size()) return null;
        BlockHeader uncleHeader = block.getUncleList().get(idx);
        Block uncle = blockchain.getBlockByHash(uncleHeader.getHash());
        if (uncle == null) {
            uncle = new Block(uncleHeader, Collections.<Transaction>emptyList(), Collections.<BlockHeader>emptyList());
        }
        return getBlockResult(uncle, false);
    }

    @Override
    public BlockResult eth_getUncleByBlockNumberAndIndex(String blockId, String uncleIdx) throws Exception {
        return eth_getUncleByBlockHashAndIndex(
                toJsonHex(getByJsonBlockId(blockId).getHash()), uncleIdx);
    }

    @Override
    public String[] eth_getCompilers() {
        return new String[] {"solidity"};
    }

    @Override
    public CompilationResult eth_compileLLL(String contract) {
        throw new UnsupportedOperationException("LLL compiler not supported");
    }

    @Override
    public CompilationResult eth_compileSolidity(String contract) throws Exception {
        SolidityCompiler.Result res = SolidityCompiler.compile(
                contract.getBytes(), true, SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN);
        if (!res.errors.isEmpty()) {
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
        ret.info.abiDefinition = new CallTransaction.Contract(contractMetadata.abi);
        return ret;
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

    @Override
    public String eth_newFilter() {
        throw new UnsupportedOperationException("JSON RPC method eth_newFilter not implemented yet");
    }

    @Override
    public String eth_newBlockFilter() {
        throw new UnsupportedOperationException("JSON RPC method eth_newBlockFilter not implemented yet");
    }

    @Override
    public String eth_newPendingTransactionFilter() {
        throw new UnsupportedOperationException("JSON RPC method eth_newPendingTransactionFilter not implemented yet");
    }

    @Override
    public String eth_uninstallFilter() {
        throw new UnsupportedOperationException("JSON RPC method eth_uninstallFilter not implemented yet");
    }

    @Override
    public String eth_getFilterChanges() {
        throw new UnsupportedOperationException("JSON RPC method eth_getFilterChanges not implemented yet");
    }

    @Override
    public String eth_getFilterLogs() {
        throw new UnsupportedOperationException("JSON RPC method eth_getFilterLogs not implemented yet");
    }

    @Override
    public String eth_getLogs() {
        throw new UnsupportedOperationException("JSON RPC method eth_getLogs not implemented yet");
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
    public String miner_start() {
        throw new UnsupportedOperationException("JSON RPC method miner_start not implemented yet");
    }

    @Override
    public String miner_stop() {
        throw new UnsupportedOperationException("JSON RPC method miner_stop not implemented yet");
    }

    @Override
    public String miner_setEtherbase() {
        throw new UnsupportedOperationException("JSON RPC method miner_setEtherbase not implemented yet");
    }

    @Override
    public String miner_setExtra() {
        throw new UnsupportedOperationException("JSON RPC method miner_setExtra not implemented yet");
    }

    @Override
    public String miner_setGasPrice() {
        throw new UnsupportedOperationException("JSON RPC method miner_setGasPrice not implemented yet");
    }

    @Override
    public String miner_startAutoDAG() {
        throw new UnsupportedOperationException("JSON RPC method miner_startAutoDAG not implemented yet");
    }

    @Override
    public String miner_stopAutoDAG() {
        throw new UnsupportedOperationException("JSON RPC method miner_stopAutoDAG not implemented yet");
    }

    @Override
    public String miner_makeDAG() {
        throw new UnsupportedOperationException("JSON RPC method miner_makeDAG not implemented yet");
    }

    @Override
    public String miner_hashrate() {
        throw new UnsupportedOperationException("JSON RPC method miner_hashrate not implemented yet");
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
        Account account = addAccount(seed);
        return toJsonHex(account.getAddress());
    }

    @Override
    public boolean personal_unlockAccount(String addr, String pass, String duration) {
        return true;
    }

    @Override
    public String[] personal_listAccounts() {
        String[] ret = new String[accounts.size()];
        int i = 0;
        for (ByteArrayWrapper addr : accounts.keySet()) {
            ret[i++] = toJsonHex(addr.getData());
        }
        return ret;
    }
}
