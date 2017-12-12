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
package org.ethereum.longrun;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.jsonrpc.JsonRpc;
import org.ethereum.jsonrpc.TransactionResultDTO;
import org.ethereum.listener.EthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.util.ByteArrayMap;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.context.annotation.Bean;

import java.net.URL;
import java.util.HashSet;

/**
 * Matches pending transactions from EthereumJ and any other JSON-RPC client
 *
 * Created by Anton Nashatyrev on 15.02.2017.
 */
@Ignore
public class PendingTxMonitor extends BasicNode {
    private static final Logger testLogger = LoggerFactory.getLogger("TestLogger");

    /**
     * Spring configuration class for the Regular peer
     */
    private static class RegularConfig {

        @Bean
        public PendingTxMonitor node() {
            return new PendingTxMonitor();
        }

        /**
         * Instead of supplying properties via config file for the peer
         * we are substituting the corresponding bean which returns required
         * config for this instance.
         */
        @Bean
        public SystemProperties systemProperties() {
            SystemProperties props = new SystemProperties();
            props.overrideParams(ConfigFactory.parseString(
                    "peer.discovery.enabled = true\n" +
                    "sync.enabled = true\n" +
                    "sync.fast.enabled = true\n" +
                    "database.dir = database-test-ptx\n" +
                    "database.reset = false\n"
            ));
            return props;
        }
    }

    public PendingTxMonitor() {
        super("sampleNode");
    }

    @Override
    public void run() {
        try {
            setupRemoteRpc();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        super.run();
    }

    private void setupRemoteRpc() throws Exception {
        System.out.println("Creating RPC interface...");
        JsonRpcHttpClient httpClient = new JsonRpcHttpClient(new URL("http://localhost:8545"));
        final JsonRpc jsonRpc = ProxyUtil.createClientProxy(getClass().getClassLoader(), JsonRpc.class, httpClient);
        System.out.println("Pinging remote RPC...");
        String protocolVersion = jsonRpc.eth_protocolVersion();
        System.out.println("Remote OK. Version: " + protocolVersion);

        final String pTxFilterId = jsonRpc.eth_newPendingTransactionFilter();

        new Thread(() -> {
            try {
                while (Boolean.TRUE) {
                    Object[] changes = jsonRpc.eth_getFilterChanges(pTxFilterId);
                    if (changes.length > 0) {
                        for (Object change : changes) {
                            TransactionResultDTO tx = jsonRpc.eth_getTransactionByHash((String) change);
                            newRemotePendingTx(tx);
                        }
                    }
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onSyncDoneImpl(EthereumListener.SyncState state) {
        super.onSyncDoneImpl(state);
        if (remoteTxs == null) {
            remoteTxs = new ByteArrayMap<>();
            System.out.println("Sync Done!!!");
            ethereum.addListener(new EthereumListenerAdapter() {
                @Override
                public void onPendingTransactionUpdate(TransactionReceipt txReceipt, PendingTransactionState state, Block block) {
                    PendingTxMonitor.this.onPendingTransactionUpdate(txReceipt, state, block);
                }
            });
        }
    }

    ByteArrayMap<Triple<Long, TransactionReceipt, EthereumListener.PendingTransactionState>> localTxs = new ByteArrayMap<>();
    ByteArrayMap<Pair<Long, TransactionResultDTO>> remoteTxs;

    private void checkUnmatched() {
        for (byte[] txHash : new HashSet<>(localTxs.keySet())) {
            Triple<Long, TransactionReceipt, EthereumListener.PendingTransactionState> tx = localTxs.get(txHash);
            if (System.currentTimeMillis() - tx.getLeft() > 60_000) {
                localTxs.remove(txHash);
                System.err.println("Local tx doesn't match: " + tx.getMiddle().getTransaction());
            }
        }

        for (byte[] txHash : new HashSet<>(remoteTxs.keySet())) {
            Pair<Long, TransactionResultDTO> tx = remoteTxs.get(txHash);
            if (System.currentTimeMillis() - tx.getLeft() > 60_000) {
                remoteTxs.remove(txHash);
                System.err.println("Remote tx doesn't match: " + tx.getRight());
            }
        }

    }

    private void onPendingTransactionUpdate(TransactionReceipt txReceipt, EthereumListener.PendingTransactionState state, Block block) {
        byte[] txHash = txReceipt.getTransaction().getHash();
        Pair<Long, TransactionResultDTO> removed = remoteTxs.remove(txHash);
        if (state == EthereumListener.PendingTransactionState.DROPPED) {
            if (localTxs.remove(txHash) != null) {
                System.out.println("Dropped due to timeout (matchned: " + (removed != null) + "): " + Hex.toHexString(txHash));
            } else {
                if (remoteTxs.containsKey(txHash)) {
                    System.err.println("Dropped but matching: "  + Hex.toHexString(txHash) + ": \n" + txReceipt);
                }
            }
        } else if (state == EthereumListener.PendingTransactionState.NEW_PENDING) {
            System.out.println("Local: " + Hex.toHexString(txHash));
            if (removed == null) {
                localTxs.put(txHash, Triple.of(System.currentTimeMillis(), txReceipt, state));
            } else {
                System.out.println("Tx matched: " + Hex.toHexString(txHash));
            }
        }
        checkUnmatched();
    }

    public void newRemotePendingTx(TransactionResultDTO tx) {
        byte[] txHash = Hex.decode(tx.hash.substring(2));
        if (remoteTxs == null) return;
        System.out.println("Remote: " + Hex.toHexString(txHash));
        Triple<Long, TransactionReceipt, EthereumListener.PendingTransactionState> removed = localTxs.remove(txHash);
        if (removed == null) {
            remoteTxs.put(txHash, Pair.of(System.currentTimeMillis(), tx));
        } else {
            System.out.println("Tx matched: " + Hex.toHexString(txHash));
        }
        checkUnmatched();
    }

    @Test
    public void test() throws Exception {
        testLogger.info("Starting EthereumJ regular instance!");
        EthereumFactory.createEthereum(RegularConfig.class);

        Thread.sleep(100000000000L);
    }
}
