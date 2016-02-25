package org.ethereum.rpc;

import org.ethereum.mine.MinerWork;
import org.ethereum.mine.ProcessSPVProofIllegalStateException;
import org.ethereum.rpc.DTO.TransactionReceiptDTO;
import org.ethereum.rpc.DTO.TransactionResultDTO;

public interface Web3 {

    public class SyncingResult {
        public String startingBlock;
        public String currentBlock;
        public String highestBlock;
    }

    public class CallArguments {
        public String from;
        public String to;
        public String gasLimit;
        public String gasPrice;
        public String value;
        public String data; // compiledCode
    }



    public class BlockResult {
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
        public String[] transactions; //: Array - Array of transaction objects, or 32 Bytes transaction hashes depending on the last given parameter.
        public String[] uncles; //: Array - Array of uncle hashes.
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
    void eth_getStorageAt();
    void eth_getTransactionCount();
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
    void eth_getUncleByBlockHashAndIndex();
    void eth_getUncleByBlockNumberAndIndex();
    void eth_getCompilers();
    void eth_compileLLL();
    void eth_compileSolidity();
    void eth_compileSerpent();
    void eth_newFilter();
    void eth_newBlockFilter();
    void eth_newPendingTransactionFilter();
    void eth_uninstallFilter();
    void eth_getFilterChanges();
    void eth_getFilterLogs();
    void eth_getLogs();
    //void eth_getWork();
    void db_putString();
    void db_getString();
    boolean eth_submitWork(String nonce, String header, String mince);
    boolean eth_submitHashrate(String hashrate, String id);
    void db_putHex();
    void db_getHex();
    void shh_post();
    void shh_version();
    void shh_newIdentity();
    void shh_hasIdentity();
    void shh_newGroup();
    void shh_addToGroup();
    void shh_newFilter();
    void shh_uninstallFilter();
    void shh_getFilterChanges();
    void shh_getMessages();

    MinerWork eth_getWork();
    void eth_processSPVProof(String bitcoinBlockHex) throws ProcessSPVProofIllegalStateException;


    }
