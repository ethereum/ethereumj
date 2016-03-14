package org.ethereum.rpc;

import org.apache.commons.lang3.ArrayUtils;
import org.ethereum.config.Constants;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.crypto.SHA3Helper;
import org.ethereum.db.TransactionInfo;
import org.ethereum.facade.Ethereum;
import org.ethereum.manager.WorldManager;
// TODO Miner related
// import org.ethereum.mine.MinerServer;
// import org.ethereum.mine.MinerWork;
// import org.ethereum.mine.ProcessSPVProofIllegalStateException;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.rpc.DTO.TransactionReceiptDTO;
import org.ethereum.rpc.DTO.TransactionResultDTO;
import org.ethereum.sync.SyncManager;
import org.ethereum.util.RLP;
import org.ethereum.vm.GasCost;
import org.ethereum.vm.program.ProgramResult;
import org.spongycastle.util.encoders.Hex;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.math.BigInteger;
import java.util.*;

public class Web3Impl implements Web3 {

    public WorldManager worldManager;

    public org.ethereum.facade.Repository repository;

    public Ethereum eth;

    // TODO Miner related
    // public MinerServer minerServer;

    public String base_clientVersion = "RootstockJ";

    public Web3Impl(Ethereum eth){
        this.eth = eth;
        this.worldManager = eth.getWorldManager();
        this.repository = eth.getRepository();
        // TODO Miner related
        // this.minerServer = eth.getMinerServer();
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

    public String[] toJsonHexArray(Collection<String> c)
    {
        String[] arr = new String[c.size()];
        int i=0;
        for(String item : c) {
                arr[i++] = TypeConverter.toJsonHex(item);
        }
        return arr;
    }

    public String[] listObjectHashtoJsonHexArray(Collection<SerializableObject> c)
    {
        String[] arr = new String[c.size()];
        int i=0;
        for(SerializableObject item : c) {
            // Todo: Which hash is required? RawHash or Hash ?
            arr[i++] = TypeConverter.toJsonHex(item.getRawHash());
        }
        return arr;
    }

    public String web3_clientVersion() {
        return base_clientVersion + "/"+SystemProperties.CONFIG.projectVersion() + "/"+SystemProperties.CONFIG.projectVersionModifier();
    };

    public String  web3_sha3(String data) throws Exception {
        byte[] result = HashUtil.sha3(TypeConverter.StringHexToByteArray(data));
        return TypeConverter.toJsonHex(result);
    };

    public String net_version(){
            return "59"; // todo: unclear what to respond. Is it EthVersion.UPPER ?
    };

    public String net_peerCount(){
        int n = worldManager.getPeerDiscovery().getPeers().size();
        return TypeConverter.toJsonHex(n);
    };

    public boolean net_listening(){
        //return worldManager.getActivePeer()
        return eth.getPeerServer().getListening();
    };

    public String eth_protocolVersion(){
        return "54"; // Todo: fill with EthVersion?
    };

    public SyncingResult eth_syncing(){

        SyncManager manager = worldManager.getSyncManager();
        //manager.isSyncDone();
        // nothing in worldManager.getSyncManager() to see.
        // Todo: Currently this returns null, as blockchain connections are disabled.
        // Block bestBlock =worldManager.getBlockchain().getBestBlock();

        SyncingResult s = new SyncingResult();
        //s.startingBlock= TypeConverter.toJsonHex(bestBlock.getNumber());
        //s.currentBlock= TypeConverter.toJsonHex(bestBlock.getNumber());
        //s.highestBlock= TypeConverter.toJsonHex(bestBlock.getNumber());
        s.startingBlock= TypeConverter.toJsonHex(0);
        s.currentBlock= TypeConverter.toJsonHex(1);
        s.highestBlock= TypeConverter.toJsonHex(2);

        return s;
    };

    // TODO not implemented
    public String eth_coinbase(){
        throw new NotImplementedException();
        // TODO expose coin base address
        // return TypeConverter.toJsonHex(worldManager.getCoinBaseAddress());
    };

    public boolean eth_mining()
    {
        return false;
    };


    public String eth_hashrate(){
        // Todo: Wait for Osky code
        return TypeConverter.toJsonHex(0);
    };

    public String eth_gasPrice(){
        // Todo: There is no gasPrice estimator. Create it.
        // Go code has NewGasPriceOracle and SuggestPrice() that is used to suggest
        // a price here. Here we just send a fixed value
        BigInteger defaultGasPrice  = new BigInteger("10000000000000");
        return TypeConverter.toJsonHex(defaultGasPrice);
    };

    // TODO review implementation using getAccountCollection
    public String[] eth_accounts() {
        List<String> addresses = new ArrayList<>();

        for (Account account : worldManager.getWallet().getAccountCollection()) {
            addresses.add(Hex.toHexString(account.getAddress()));
        }

        return toJsonHexArray(addresses);
    }

    public String eth_blockNumber(){
        Block bestBlock =worldManager.getBlockchain().getBestBlock();
        long b = 0;
        if (bestBlock!=null) {
            b = bestBlock.getNumber();
        }
        return TypeConverter.toJsonHex(b);
    };


    public String eth_getBalance(String address, String block)
    throws Exception
    {
          /*HEX String  - an integer block number
        �  String "earliest"  for the earliest/genesis block
        �  String "latest"  - for the latest mined block
        �  String "pending"  - for the pending state/transactions
        */

        if (!block.toUpperCase().equals(("LATEST")))
            throw new Exception("Only LATEST option implemented. block number, earliest and pending available soon.");

        byte[] addressAsByteArray = TypeConverter.StringHexToByteArray(address);
        BigInteger balance = this.repository.getBalance(addressAsByteArray);
        return TypeConverter.toJsonHex(balance);
    }

    public String eth_getBalance(String address)
            throws Exception
    {
        byte[] addressAsByteArray = TypeConverter.StringHexToByteArray(address);
        BigInteger balance = this.repository.getBalance(addressAsByteArray);
        return TypeConverter.toJsonHex(balance);
    }

    public void eth_getStorageAt(){};

    public void eth_getTransactionCount(){};

    public Block getBlockByJSonHash(String blockHash) throws Exception  {
        byte[] bhash = TypeConverter.StringHexToByteArray(blockHash);
        return worldManager.getBlockchain().getBlockByHash(bhash);
    }

    public String eth_getBlockTransactionCountByHash(String blockHash) throws Exception {
        Block b = getBlockByJSonHash(blockHash);
        long n = b.getTransactionsList().size();
        return TypeConverter.toJsonHex(n);
    };

    public Block getBlockByNumberOrStr(String bnOrId) throws Exception {
        // Todo: interpret "earliest" or "pending",
        Block b;
        if (bnOrId.equals("latest"))
            b =worldManager.getBlockchain().getBestBlock();
        else
        if (bnOrId.equals("earliest") || bnOrId.equals("pending"))
            throw new Exception("Unsupported");
        else {
            long bn = JSonHexToLong(bnOrId);
            // Todo: here I'm returning blocks from the blockchain, but not unconfirmed blocks
            // this must be added.
            b = worldManager.getBlockchain().getBlockByNumber(bn);
        }
        return b;
    }

    public String eth_getBlockTransactionCountByNumber(String bnOrId) throws Exception {
        Block b = getBlockByNumberOrStr(bnOrId);
        long n = b.getTransactionsList().size();
        return TypeConverter.toJsonHex(n);
    };

    public String eth_getUncleCountByBlockHash(String blockHash) throws Exception {
        Block b = getBlockByJSonHash(blockHash);
        long n = b.getUncleList().size();
        return TypeConverter.toJsonHex(n);
    };

    public String eth_getUncleCountByBlockNumber(String bnOrId) throws Exception {
        Block b = getBlockByNumberOrStr(bnOrId);
        long n = b.getUncleList().size();
        return TypeConverter.toJsonHex(n);
    };

    public String eth_getCode(String addr, String bnOrId) throws Exception {
        if (!bnOrId.equals("latest"))
            throw new Exception("Unsupported");

        return TypeConverter.toJsonHex(worldManager.getRepository().getCode(TypeConverter.StringHexToByteArray(addr)));

    };

    public String eth_sign(String addr,String data) throws Exception {
        String ha = JSonHexToHex(addr);

        Account account = getAccountByAddress(ha);

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

        Account account = getAccountByAddress(JSonHexToHex(args.from));

        if (account == null)
            throw new Exception("From address private key could not be found in this node");

        ChannelManager channelManager = worldManager.getChannelManager();

        String toAddress = args.to != null ? Hex.toHexString(TypeConverter.StringHexToByteArray(args.to)) : null;
        BigInteger accountNonce = account.getNonce();
        BigInteger value = args.value != null ? TypeConverter.StringNumberAsBigInt(args.value) : BigInteger.ZERO;
        BigInteger gasPrice = args.gasPrice != null ? TypeConverter.StringNumberAsBigInt(args.gasPrice) : BigInteger.ZERO;
        BigInteger gasLimit = args.gasLimit != null ? TypeConverter.StringNumberAsBigInt(args.gasLimit) : BigInteger.valueOf(GasCost.TRANSACTION);

        if (args.data != null && args.data.startsWith("0x"))
            args.data = args.data.substring(2);

        Transaction tx = Transaction.create(toAddress, value, accountNonce, gasPrice, gasLimit, args.data);

        tx.sign(account.getEcKey().getPrivKeyBytes());

        List<Transaction> txs = new ArrayList<Transaction>();
        txs.add(tx);

        // TODO review if needed
        // worldManager.getBlockchain().addPendingTransactions(new HashSet<Transaction>(txs));

        // TODO review submit transaction to own peer
        eth.submitTransaction(tx);

        // TODO review channel argument as null
        channelManager.sendTransaction(txs, null);

        return TypeConverter.toJsonHex(tx.getHash());
    };

    // TODO: Remove, obsolete with this params
    public String eth_sendTransaction(String from,String to, String gas,
                                    String gasPrice, String value,String data,String nonce) throws Exception {

        Transaction tx;
        ChannelManager channelManager = worldManager.getChannelManager();
        tx = new Transaction(
                TypeConverter.StringHexToByteArray(nonce),
                TypeConverter.StringHexToByteArray(gasPrice),
                TypeConverter.StringHexToByteArray(gas),
                TypeConverter.StringHexToByteArray(to), /*receiveAddress*/
                TypeConverter.StringHexToByteArray(value),
                TypeConverter.StringHexToByteArray(data));

        // TODO review channel as null
        channelManager.sendTransaction(Arrays.asList(tx), null);

        return TypeConverter.toJsonHex(tx.getHash());
    };

    public String eth_sendRawTransaction(String rawData) throws Exception {

        ChannelManager channelManager = worldManager.getChannelManager();
        Transaction tx;
        tx = new Transaction(TypeConverter.StringHexToByteArray(rawData));

        // TODO review channel as null
        channelManager.sendTransaction(Arrays.asList(tx), null);

        return TypeConverter.toJsonHex(tx.getHash());
    };

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

            gasLimit = 100000000000000L;
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

    public ProgramResult createCallTxAndExecute(CallArguments args) throws Exception {
        BinaryCallArguments bca = new BinaryCallArguments();
        bca.setArguments(args);
        Transaction tx = CallTransaction.createRawTransaction(0,
                bca.gasPrice,
                bca.gasLimit,
                bca.toAddress,
                bca.value,
                bca.data);

        tx.sign(new byte[32]);

        Block block = worldManager.getBlockchain().getBestBlock();

        ProgramResult res = eth.callConstantCallTransaction(tx, block);
        return res;
    };


    public String eth_call(CallArguments args, String bnOrId) throws Exception {
        if (!bnOrId.equals("latest"))
            throw new Exception("Unsupported");

        ProgramResult res = createCallTxAndExecute(args);
        return TypeConverter.toJsonHex(res.getHReturn());

    };

    public String eth_estimateGas(CallArguments args) throws Exception {
        ProgramResult res = createCallTxAndExecute(args);
        return TypeConverter.toJsonHex(res.getGasUsed());
    }


    public BlockResult getBlockResult(Block b) {
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

        // Todo: Array of transaction objects not supported because object conversion is done by
        // jsonrpc4j and I don't know how if returning an Object[] will trigger dynamic type
        // selection or not. Currently the return type is String[]
        //: Array - Array of transaction objects, or 32 Bytes transaction hashes depending on the last given parameter.

        List<SerializableObject> tl = new ArrayList<SerializableObject>();
        tl.addAll(b.getTransactionsList());
        br.transactions = listObjectHashtoJsonHexArray(tl);

        List<SerializableObject> ul = new ArrayList<SerializableObject>();
        ul.addAll(b.getUncleList());

        br.uncles = listObjectHashtoJsonHexArray(ul);; //: Array - Array of uncle hashes.
        return br;
    }

    public BlockResult eth_getBlockByHash(String blockHash,Boolean fullTransactionObjects) throws Exception {
        Block b = getBlockByJSonHash(blockHash);
        // Todo: here I'm returning blocks from the blockchain, but not unconfirmed blocks
        // this must be added.
        return getBlockResult(b);
    }

    public BlockResult eth_getBlockByNumber(String bnOrId,Boolean fullTransactionObjects) throws Exception {
        Block b = getBlockByNumberOrStr(bnOrId);

        return getBlockResult(b);
    };

    // TODO complete implementation, blockchain.getTransactionInfo
    public TransactionResultDTO eth_getTransactionByHash(String transactionHash) throws Exception {
        throw new NotImplementedException();

        //TODO: return also unconfirmed transactions
/*
        Blockchain blockchain = worldManager.getBlockchain();
        TransactionInfo txInfo = blockchain.getTransactionInfo(TypeConverter.StringHexToByteArray(transactionHash));

        if (txInfo == null)
            return null;

        Block block = blockchain.getBlockByHash(txInfo.getBlockHash());

        TransactionResultDTO txDTO = new TransactionResultDTO(block, txInfo.getIndex(), txInfo.getReceipt().getTransaction());

        return txDTO;
  */
    }

    public TransactionResultDTO eth_getTransactionByBlockHashAndIndex(String blockHash,String index) throws Exception {
        Block b = getBlockByJSonHash(blockHash);
        int idx = JSonHexToInt(index);
        Transaction tx = b.getTransactionsList().get(idx);
        TransactionResultDTO tr = new TransactionResultDTO(b, idx, tx);
        return tr;
    };

    public TransactionResultDTO eth_getTransactionByBlockNumberAndIndex(String bnOrId,String index) throws Exception {
        Block b = getBlockByNumberOrStr(bnOrId);
        int idx = JSonHexToInt(index);
        Transaction tx = b.getTransactionsList().get(idx);
        TransactionResultDTO tr = new TransactionResultDTO(b, idx, tx);
        return tr;
    };

    // TODO complete implementation blockchain.getTransactionInfo
    public TransactionReceiptDTO eth_getTransactionReceipt(String transactionHash) throws Exception {

        Blockchain blockchain = worldManager.getBlockchain();
        byte[] hash = TypeConverter.StringHexToByteArray(transactionHash);
        TransactionInfo txInfo = blockchain.getTransactionInfo(hash);

        if (txInfo == null)
            return null;

        Block block = blockchain.getBlockByHash(txInfo.getBlockHash());

        return new TransactionReceiptDTO(block, txInfo);

    }

    public void eth_getUncleByBlockHashAndIndex(){};
    public void eth_getUncleByBlockNumberAndIndex(){};
    public void eth_getCompilers(){};
    public void eth_compileLLL(){};
    public void eth_compileSolidity(){};
    public void eth_compileSerpent(){};
    public void eth_newFilter(){};
    public void eth_newBlockFilter(){};
    public void eth_newPendingTransactionFilter(){};
    public void eth_uninstallFilter(){};
    public void eth_getFilterChanges(){};
    public void eth_getFilterLogs(){};
    public void eth_getLogs(){};
    //public void eth_getWork(){};
    public void db_putString(){};
    public void db_getString(){};

    public boolean eth_submitWork(String nonce, String header, String mince)
    {
        throw new NotImplementedException();
    }

    public boolean eth_submitHashrate(String Hashrate, String ID)
    {
        throw new NotImplementedException();
    }

    public void db_putHex(){};
    public void db_getHex(){};
    public void shh_post(){};
    public void shh_version(){};
    public void shh_newIdentity(){};
    public void shh_hasIdentity(){};
    public void shh_newGroup(){};
    public void shh_addToGroup(){};
    public void shh_newFilter(){};
    public void shh_uninstallFilter(){};
    public void shh_getFilterChanges(){};
    public void shh_getMessages(){};

    // TODO Miner related
    /*
    public MinerWork eth_getWork() {
        return minerServer.getWork();
    };

    public void eth_processSPVProof(String bitcoinBlockHex) throws ProcessSPVProofIllegalStateException {
        org.bitcoinj.core.NetworkParameters params = org.bitcoinj.params.RegTestParams.get();
        new org.bitcoinj.core.Context(params);
        byte[] bitcoinBlockByteArray = Hex.decode(bitcoinBlockHex);
        org.bitcoinj.core.Block bitcoinBlock = new org.bitcoinj.core.Block(params, bitcoinBlockByteArray);
        org.bitcoinj.core.Transaction coinbase = bitcoinBlock.getTransactions().get(0);
        byte[] coinbaseAsByteArray = coinbase.bitcoinSerialize();
        List<Byte> coinbaseAsByteList = java.util.Arrays.asList(ArrayUtils.toObject(coinbaseAsByteArray));

        List<Byte> rootstockTagAsByteList = java.util.Arrays.asList(ArrayUtils.toObject(Constants.ROOTSTOCK_TAG));

        int rootstockTagPosition = Collections.lastIndexOfSubList(coinbaseAsByteList, rootstockTagAsByteList);
        byte[] blockHashForMergedMining = new byte[SHA3Helper.Size.S256.getValue()/8];
        System.arraycopy(coinbaseAsByteArray, rootstockTagPosition+Constants.ROOTSTOCK_TAG.length, blockHashForMergedMining, 0, blockHashForMergedMining.length);



        minerServer.processSPVProof(blockHashForMergedMining, bitcoinBlock);
    }
*/

    // TODO review new method to get account by address
    private Account getAccountByAddress(String address) {
        Collection<Account> accounts = eth.getWallet().getAccountCollection();

        Account account = null;

        // TODO review search account by address
        for (Account acc : accounts) {
            String straddr = Hex.toHexString(acc.getAddress());
            if (address.equals(straddr)) {
                account = acc;
                break;
            }
        }
        return account;
    }}