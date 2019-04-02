package org.ethereum.datasource;

import org.ethereum.core.Block;
import org.ethereum.core.Genesis;
import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.db.TransactionStore;
import org.ethereum.listener.BlockReplay;
import org.ethereum.listener.EthereumListener;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigInteger.ZERO;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author alexbraz
 * @since 29/03/2019
 */
public class BlockReplayTest {
    private static final Logger logger = LoggerFactory.getLogger("test");


    BlockReplay replay;
    EthereumListener listener;
    private List<Block> blocks = new ArrayList<>();
    private BigInteger totDifficulty = ZERO;
    Block genesis = Genesis.getInstance();

    @Before
    public void setup() throws URISyntaxException, IOException {
        URL scenario1 = ClassLoader
                .getSystemResource("blockstore/load.dmp");

        File file = new File(scenario1.toURI());
        List<String> strData = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

        IndexedBlockStore indexedBlockStore = indexedBlockStore = new IndexedBlockStore();
        indexedBlockStore.init(new HashMapDB<byte[]>(), new HashMapDB<byte[]>());

        TransactionStore txStore = new TransactionStore(new HashMapDB<>());

        listener = mock(EthereumListener.class);

        replay = new BlockReplay(indexedBlockStore, txStore,  listener, 0L);
        for (String blockRLP : strData) {

            Block block = new Block(
                    Hex.decode(blockRLP));

            if (block.getNumber() % 1000 == 0)
                logger.info("adding block.hash: [{}] block.number: [{}]",
                        block.getShortHash(),
                        block.getNumber());

            blocks.add(block);
            totDifficulty = totDifficulty.add(block.getDifficultyBI());
            indexedBlockStore.saveBlock(block, totDifficulty, true);
        }

    }

    @Test
    public void testReplayBlock() {
        IndexedBlockStore i = mock(IndexedBlockStore.class);
        when(i.getChainBlockByNumber(anyLong())).thenReturn(genesis);
        TransactionStore txStore = new TransactionStore(new HashMapDB<>());
        replay = new BlockReplay(i, txStore,  listener, 0L);
        replay.replay();

        verify(listener, times(1)).onBlock(any());
    }

    @Test
    public void testListenerNoConnection() {
        replay.onNoConnections();
        verify(listener, times(1)).onNoConnections();

        replay.onSyncDone(null);
        verify(listener, times(1)).onSyncDone(any());

        replay.onNodeDiscovered(any());
        verify(listener, times(1)).onNodeDiscovered(any());

        replay.onEthStatusUpdated(any(), any());
        verify(listener, times(1)).onEthStatusUpdated(any(), any());

        replay.onHandShakePeer(any(), any());
        verify(listener, times(1)).onHandShakePeer(any(), any());

        replay.onPeerAddedToSyncPool(any());
        verify(listener, times(1)).onPeerAddedToSyncPool(any());

        replay.onPeerDisconnect(anyString(), anyLong());
        verify(listener, times(1)).onPeerDisconnect(anyString(), anyLong());

        replay.onPendingStateChanged(any());
        verify(listener, times(1)).onPendingStateChanged(any());

        replay.onPendingTransactionUpdate(any(), any(), any());
        verify(listener, times(1)).onPendingTransactionUpdate(any(), any(), any());

        replay.onRecvMessage(any(), any());
        verify(listener, times(1)).onRecvMessage(any(), any());

        replay.onTransactionExecuted(any());
        verify(listener, times(1)).onTransactionExecuted(any());

        replay.onSendMessage(any(), any());
        verify(listener, times(1)).onSendMessage(any(), any());

        replay.onVMTraceCreated(anyString(), anyString());
        verify(listener, times(1)).onVMTraceCreated(anyString(), anyString());

    }



}
