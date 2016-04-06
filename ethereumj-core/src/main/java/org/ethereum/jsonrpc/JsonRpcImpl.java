package org.ethereum.jsonrpc;

/**
 * Created by Anton Nashatyrev on 25.11.2015.
 */
//@JsonRpcService("/jr")
//@Component
public class JsonRpcImpl implements JsonRpc {

    public String net_version() {
        return "666";
    }

    public String eth_getBalance(String account, String block) {
        System.out.println("JsonRpcImpl.eth_getBalance:" + "account = [" + account + "], block = [" + block + "]");
        return "0x555";
    }

    @Override
    public String eth_getStorageAt(String account, String block, String a) {
//        return "0x555";
        throw new UnsupportedOperationException("JSON RPC method eth_getStorageAt not implemented yet");
    }

    @Override
    public String web3_clientVersion() {
        throw new UnsupportedOperationException("JSON RPC method web3_clientVersion not implemented yet");
    }

    @Override
    public String web3_sha3() {
        throw new UnsupportedOperationException("JSON RPC method web3_sha3 not implemented yet");
    }

    @Override
    public String net_peerCount() {
        throw new UnsupportedOperationException("JSON RPC method net_peerCount not implemented yet");
    }

    @Override
    public String net_listening() {
        throw new UnsupportedOperationException("JSON RPC method net_listening not implemented yet");
    }

    @Override
    public String eth_protocolVersion() {
        throw new UnsupportedOperationException("JSON RPC method eth_protocolVersion not implemented yet");
    }

    @Override
    public String eth_syncing() {
        throw new UnsupportedOperationException("JSON RPC method eth_syncing not implemented yet");
    }

    @Override
    public String eth_coinbase() {
        throw new UnsupportedOperationException("JSON RPC method eth_coinbase not implemented yet");
    }

    @Override
    public String eth_mining() {
        throw new UnsupportedOperationException("JSON RPC method eth_mining not implemented yet");
    }

    @Override
    public String eth_hashrate() {
        throw new UnsupportedOperationException("JSON RPC method eth_hashrate not implemented yet");
    }

    @Override
    public String eth_gasPrice() {
        throw new UnsupportedOperationException("JSON RPC method eth_gasPrice not implemented yet");
    }

    @Override
    public String eth_accounts() {
        throw new UnsupportedOperationException("JSON RPC method eth_accounts not implemented yet");
    }

    @Override
    public String eth_blockNumber() {
        throw new UnsupportedOperationException("JSON RPC method eth_blockNumber not implemented yet");
    }

    @Override
    public String eth_getBalance() {
        throw new UnsupportedOperationException("JSON RPC method eth_getBalance not implemented yet");
    }

    @Override
    public String eth_getStorageAt() {
        throw new UnsupportedOperationException("JSON RPC method eth_getStorageAt not implemented yet");
    }

    @Override
    public String eth_getTransactionCount() {
        throw new UnsupportedOperationException("JSON RPC method eth_getTransactionCount not implemented yet");
    }

    @Override
    public String eth_getBlockTransactionCountByHash() {
        throw new UnsupportedOperationException("JSON RPC method eth_getBlockTransactionCountByHash not implemented yet");
    }

    @Override
    public String eth_getBlockTransactionCountByNumber() {
        throw new UnsupportedOperationException("JSON RPC method eth_getBlockTransactionCountByNumber not implemented yet");
    }

    @Override
    public String eth_getUncleCountByBlockHash() {
        throw new UnsupportedOperationException("JSON RPC method eth_getUncleCountByBlockHash not implemented yet");
    }

    @Override
    public String eth_getUncleCountByBlockNumber() {
        throw new UnsupportedOperationException("JSON RPC method eth_getUncleCountByBlockNumber not implemented yet");
    }

    @Override
    public String eth_getCode() {
        throw new UnsupportedOperationException("JSON RPC method eth_getCode not implemented yet");
    }

    @Override
    public String eth_sign() {
        throw new UnsupportedOperationException("JSON RPC method eth_sign not implemented yet");
    }

    @Override
    public String eth_sendTransaction() {
        throw new UnsupportedOperationException("JSON RPC method eth_sendTransaction not implemented yet");
    }

    @Override
    public String eth_sendRawTransaction() {
        throw new UnsupportedOperationException("JSON RPC method eth_sendRawTransaction not implemented yet");
    }

    @Override
    public String eth_call() {
        throw new UnsupportedOperationException("JSON RPC method eth_call not implemented yet");
    }

    @Override
    public String eth_estimateGas() {
        throw new UnsupportedOperationException("JSON RPC method eth_estimateGas not implemented yet");
    }

    @Override
    public String eth_getBlockByHash() {
        throw new UnsupportedOperationException("JSON RPC method eth_getBlockByHash not implemented yet");
    }

    @Override
    public String eth_getBlockByNumber() {
        throw new UnsupportedOperationException("JSON RPC method eth_getBlockByNumber not implemented yet");
    }

    @Override
    public String eth_getTransactionByHash() {
        throw new UnsupportedOperationException("JSON RPC method eth_getTransactionByHash not implemented yet");
    }

    @Override
    public String eth_getTransactionByBlockHashAndIndex() {
        throw new UnsupportedOperationException("JSON RPC method eth_getTransactionByBlockHashAndIndex not implemented yet");
    }

    @Override
    public String eth_getTransactionByBlockNumberAndIndex() {
        throw new UnsupportedOperationException("JSON RPC method eth_getTransactionByBlockNumberAndIndex not implemented yet");
    }

    @Override
    public String eth_getTransactionReceipt() {
        throw new UnsupportedOperationException("JSON RPC method eth_getTransactionReceipt not implemented yet");
    }

    @Override
    public String eth_getUncleByBlockHashAndIndex() {
        throw new UnsupportedOperationException("JSON RPC method eth_getUncleByBlockHashAndIndex not implemented yet");
    }

    @Override
    public String eth_getUncleByBlockNumberAndIndex() {
        throw new UnsupportedOperationException("JSON RPC method eth_getUncleByBlockNumberAndIndex not implemented yet");
    }

    @Override
    public String eth_getCompilers() {
        throw new UnsupportedOperationException("JSON RPC method eth_getCompilers not implemented yet");
    }

    @Override
    public String eth_compileLLL() {
        throw new UnsupportedOperationException("JSON RPC method eth_compileLLL not implemented yet");
    }

    @Override
    public String eth_compileSolidity() {
        throw new UnsupportedOperationException("JSON RPC method eth_compileSolidity not implemented yet");
    }

    @Override
    public String eth_compileSerpent() {
        throw new UnsupportedOperationException("JSON RPC method eth_compileSerpent not implemented yet");
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
    public String admin_addPeer() {
        throw new UnsupportedOperationException("JSON RPC method admin_addPeer not implemented yet");
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
    public String eth_resend() {
        throw new UnsupportedOperationException("JSON RPC method eth_resend not implemented yet");
    }

    @Override
    public String eth_pendingTransactions() {
        throw new UnsupportedOperationException("JSON RPC method eth_pendingTransactions not implemented yet");
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
    public String personal_newAccount() {
        throw new UnsupportedOperationException("JSON RPC method personal_newAccount not implemented yet");
    }

    @Override
    public String personal_unlockAccount() {
        throw new UnsupportedOperationException("JSON RPC method personal_unlockAccount not implemented yet");
    }

    @Override
    public String personal_listAccounts() {
        throw new UnsupportedOperationException("JSON RPC method personal_listAccounts not implemented yet");
    }
}
