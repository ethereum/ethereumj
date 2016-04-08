package org.ethereum.jsonrpc;

import org.ethereum.core.Block;
import org.ethereum.core.CallTransaction;

/**
 * Created by Anton Nashatyrev on 25.11.2015.
 */
public interface JsonRpc {

    class SyncingResult {
        public String startingBlock;
        public String currentBlock;
        public String highestBlock;
    }

    class CallArguments {
        public String from;
        public String to;
        public String gasLimit;
        public String gasPrice;
        public String value;
        public String data; // compiledCode
    }

    class BlockResult {
        public String number; // QUANTITY - the block number. null when its pending block.
        public String hash; // DATA, 32 Bytes - hash of the block. null when its pending block.
        public String parentHash; // DATA, 32 Bytes - hash of the parent block.
        public String nonce; // DATA, 8 Bytes - hash of the generated proof-of-work. null when its pending block.
        public String sha3Uncles; // DATA, 32 Bytes - SHA3 of the uncles data in the block.
        public String logsBloom; // DATA, 256 Bytes - the bloom filter for the logs of the block. null when its pending block.
        public String transactionsRoot; // DATA, 32 Bytes - the root of the transaction trie of the block.
        public String stateRoot; // DATA, 32 Bytes - the root of the final state trie of the block.
        public String receiptsRoot; // DATA, 32 Bytes - the root of the receipts trie of the block.
        public String miner; // DATA, 20 Bytes - the address of the beneficiary to whom the mining rewards were given.
        public String difficulty; // QUANTITY - integer of the difficulty for this block.
        public String totalDifficulty; // QUANTITY - integer of the total difficulty of the chain until this block.
        public String extraData; // DATA - the "extra data" field of this block
        public String size;//QUANTITY - integer the size of this block in bytes.
        public String gasLimit;//: QUANTITY - the maximum gas allowed in this block.
        public String gasUsed; // QUANTITY - the total used gas by all transactions in this block.
        public String timestamp; //: QUANTITY - the unix timestamp for when the block was collated.
        public Object[] transactions; //: Array - Array of transaction objects, or 32 Bytes transaction hashes depending on the last given parameter.
        public String[] uncles; //: Array - Array of uncle hashes.
    }

    class CompilationResult {
        String code;
        CompilationInfo info;
    }

    class CompilationInfo {
        String source;
        String language;
        String languageVersion;
        String compilerVersion;
        CallTransaction.Contract abiDefinition;
        String userDoc;
        String developerDoc;
    }

    String web3_clientVersion();
    String web3_sha3(String data) throws Exception;
    String net_version();
    String net_peerCount();
    boolean net_listening();
    String eth_protocolVersion();
    SyncingResult eth_syncing();
    String eth_coinbase();
    boolean eth_mining();
    String eth_hashrate();
    String eth_gasPrice();
    String[] eth_accounts();
    String eth_blockNumber();
    String eth_getBalance(String address, String block) throws Exception;
    String eth_getBalance(String address) throws Exception;

    String eth_getStorageAt(String address, String storageIdx, String blockId) throws Exception;

    String eth_getTransactionCount(String address, String blockId) throws Exception;

    String eth_getBlockTransactionCountByHash(String blockHash)throws Exception;
    String eth_getBlockTransactionCountByNumber(String bnOrId)throws Exception;
    String eth_getUncleCountByBlockHash(String blockHash)throws Exception;
    String eth_getUncleCountByBlockNumber(String bnOrId)throws Exception;
    String eth_getCode(String addr, String bnOrId)throws Exception;
    String eth_sign(String addr,String data) throws Exception;
    String eth_sendTransaction(CallArguments transactionArgs) throws Exception;
    // TODO: Remove, obsolete with this params
    String eth_sendTransaction(String from,String to, String gas,
                               String gasPrice, String value,String data,String nonce) throws Exception;
    String eth_sendRawTransaction(String rawData) throws Exception;
    String eth_call(CallArguments args, String bnOrId) throws Exception;
    String eth_estimateGas(CallArguments args) throws Exception;
    BlockResult eth_getBlockByHash(String blockHash,Boolean fullTransactionObjects) throws Exception;
    BlockResult eth_getBlockByNumber(String bnOrId,Boolean fullTransactionObjects) throws Exception;
    TransactionResultDTO eth_getTransactionByHash(String transactionHash) throws Exception;
    TransactionResultDTO eth_getTransactionByBlockHashAndIndex(String blockHash,String index) throws Exception;
    TransactionResultDTO eth_getTransactionByBlockNumberAndIndex(String bnOrId,String index) throws Exception;
    TransactionReceiptDTO eth_getTransactionReceipt(String transactionHash) throws Exception;

    BlockResult eth_getUncleByBlockHashAndIndex(String blockHash, String uncleIdx) throws Exception;

    BlockResult eth_getUncleByBlockNumberAndIndex(String blockId, String uncleIdx) throws Exception;

    String[] eth_getCompilers();
    CompilationResult eth_compileLLL(String contract);
    CompilationResult eth_compileSolidity(String contract) throws Exception;
    CompilationResult eth_compileSerpent(String contract);
    String eth_resend();
    String eth_pendingTransactions();

    String eth_newFilter();
    String eth_newBlockFilter();
    String eth_newPendingTransactionFilter();
    String eth_uninstallFilter();
    String eth_getFilterChanges();
    String eth_getFilterLogs();
    String eth_getLogs();
    String eth_getWork();
    String eth_submitWork();
    String eth_submitHashrate();
    String db_putString();
    String db_getString();
    String db_putHex();
    String db_getHex();
    String shh_post();
    String shh_version();
    String shh_newIdentity();
    String shh_hasIdentity();
    String shh_newGroup();
    String shh_addToGroup();
    String shh_newFilter();
    String shh_uninstallFilter();
    String shh_getFilterChanges();
    String shh_getMessages();


    boolean admin_addPeer(String s);

    String admin_exportChain();
    String admin_importChain();
    String admin_sleepBlocks();
    String admin_verbosity();
    String admin_setSolc();
    String admin_startRPC();
    String admin_stopRPC();
    String admin_setGlobalRegistrar();
    String admin_setHashReg();
    String admin_setUrlHint();
    String admin_saveInfo();
    String admin_register();
    String admin_registerUrl();
    String admin_startNatSpec();
    String admin_stopNatSpec();
    String admin_getContractInfo();
    String admin_httpGet();
    String admin_nodeInfo();
    String admin_peers();
    String admin_datadir();
    String net_addPeer();
    String miner_start();
    String miner_stop();
    String miner_setEtherbase();
    String miner_setExtra();
    String miner_setGasPrice();
    String miner_startAutoDAG();
    String miner_stopAutoDAG();
    String miner_makeDAG();
    String miner_hashrate();
    String debug_printBlock();
    String debug_getBlockRlp();
    String debug_setHead();
    String debug_processBlock();
    String debug_seedHash();
    String debug_dumpBlock();
    String debug_metrics();

    String personal_newAccount(String seed);

    boolean personal_unlockAccount(String addr, String pass, String duration);

    String[] personal_listAccounts();
}
