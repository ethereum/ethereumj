package org.ethereum.api;

import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionInfo;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.listener.EthereumListener;
import org.ethereum.api.type.*;
import org.ethereum.net.rlpx.Node;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by Anton Nashatyrev on 07.09.2016.
 */
public interface EthereumJ {

    /*****  General Callback  ******/

    void addEthereumListener(EthereumListener listener);

    void removeEthereumListener(EthereumListener listener);

    /*****  Data access  ******/

    Block getBlock(BlockId blockId);

    TransactionInfo getTransaction(Hash256 txHash);

    long getNonce(Address address, BlockId blockId);

    EtherValue getBalance(Address address, BlockId blockId);

    Word getStorage(Address address, Word storageIdx, BlockId blockId);

    ByteArray getCode(Address address, BlockId blockId);

    EtherValue getGasPrice();

    /******  Transactions processing  ******/

    TransactionObserver sendTransaction(TransactionData tx);

    TransactionReceipt callTransaction(TransactionData tx, BlockId blockId);

    TransactionObserver getPendingTransactions();

    /******  Log filters  ******/

    LogObserver addLogFilter(BlockId fromBlock, BlockId toBlock,
                             @Nullable Address contractAddr, LogTopic ... topics);

    /******* Additional Info  ********/

    List<Block> getForkBlocks(long blockNum);

    TransactionObserver trackTransaction(Hash256 txHash);

    /******* Network  ********/

    List<Node> getConnectedNodes();

    void connect(Node node);

    void disconnect(Node node);

    void setDiscoveryState(boolean enabled);

    void setAllowInboundConnections(boolean enabled);
}
