package org.ethereum.listener;

import org.ethereum.core.*;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.message.Message;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.server.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Roman Mandeleil
 * @since 12.11.2014
 */
@Component(value = "EthereumListener")
public class CompositeEthereumListener implements EthereumListener {

    @Autowired
    EventDispatchThread eventDispatchThread = EventDispatchThread.getDefault();

    List<EthereumListener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(EthereumListener listener) {
        listeners.add(listener);
    }
    public void removeListener(EthereumListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void trace(final String output) {
        for (final EthereumListener listener : listeners) {
            eventDispatchThread.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.trace(output);
                }
            });
        }
    }

    @Override
    public void onBlock(final BlockSummary blockSummary, final boolean isBestBlock) {
        for (final EthereumListener listener : listeners) {
            eventDispatchThread.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.onBlock(blockSummary, isBestBlock);
                }
            });
        }
    }

    @Override
    public void onRecvMessage(final Channel channel, final Message message) {
        for (final EthereumListener listener : listeners) {
            eventDispatchThread.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.onRecvMessage(channel, message);
                }
            });
        }
    }

    @Override
    public void onSendMessage(final Channel channel, final Message message) {
        for (final EthereumListener listener : listeners) {
            eventDispatchThread.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.onSendMessage(channel, message);
                }
            });
        }
    }

    @Override
    public void onPeerDisconnect(final String host, final long port) {
        for (final EthereumListener listener : listeners) {
            eventDispatchThread.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.onPeerDisconnect(host, port);
                }
            });
        }
    }

    @Override
    public void onPendingTransactionsReceived(final List<Transaction> transactions) {
        for (final EthereumListener listener : listeners) {
            eventDispatchThread.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.onPendingTransactionsReceived(transactions);
                }
            });
        }
    }

    @Override
    public void onPendingStateChanged(final PendingState pendingState) {
        for (final EthereumListener listener : listeners) {
            eventDispatchThread.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.onPendingStateChanged(pendingState);
                }
            });
        }
    }

    @Override
    public void onSyncDone() {
        for (final EthereumListener listener : listeners) {
            eventDispatchThread.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.onSyncDone();
                }
            });
        }
    }

    @Override
    public void onNoConnections() {
        for (final EthereumListener listener : listeners) {
            eventDispatchThread.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.onNoConnections();
                }
            });
        }
    }

    @Override
    public void onHandShakePeer(final Channel channel, final HelloMessage helloMessage) {
        for (final EthereumListener listener : listeners) {
            eventDispatchThread.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.onHandShakePeer(channel, helloMessage);
                }
            });
        }
    }

    @Override
    public void onVMTraceCreated(final String transactionHash, final String trace) {
        for (final EthereumListener listener : listeners) {
            eventDispatchThread.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.onVMTraceCreated(transactionHash, trace);
                }
            });
        }
    }

    @Override
    public void onNodeDiscovered(final Node node) {
        for (final EthereumListener listener : listeners) {
            eventDispatchThread.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.onNodeDiscovered(node);
                }
            });
        }
    }

    @Override
    public void onEthStatusUpdated(final Channel channel, final StatusMessage status) {
        for (final EthereumListener listener : listeners) {
            eventDispatchThread.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.onEthStatusUpdated(channel, status);
                }
            });
        }
    }

    @Override
    public void onTransactionExecuted(final TransactionExecutionSummary summary) {
        for (final EthereumListener listener : listeners) {
            eventDispatchThread.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.onTransactionExecuted(summary);
                }
            });
        }
    }

    @Override
    public void onPeerAddedToSyncPool(final Channel peer) {
        for (final EthereumListener listener : listeners) {
            eventDispatchThread.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.onPeerAddedToSyncPool(peer);
                }
            });
        }
    }

    @Override
    public void onPendingTransactionUpdate(final TransactionReceipt txReceipt, final PendingTransactionState state,
                                           final Block block) {
        for (final EthereumListener listener : listeners) {
            eventDispatchThread.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.onPendingTransactionUpdate(txReceipt, state, block);
                }
            });
        }
    }
}
