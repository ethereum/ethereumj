package org.ethereum.config.blockchain;

import org.ethereum.config.BlockchainConfig;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.Repository;
import org.ethereum.core.Transaction;
import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.db.RepositoryRoot;
import org.ethereum.erp.ErpExecutor;
import org.ethereum.erp.ErpLoader;
import org.ethereum.erp.RawStateChangeObject;
import org.ethereum.erp.StateChangeObject;
import org.ethereum.util.ByteUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;

import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ErpConfigTest
{

    private ErpConfig config;
    private Repository repo;
    private static byte[] account1 = Hex.decode("11113D9F938E13CD947EC05ABC7FE734DF8DD826");
    private static byte[] account2 = Hex.decode("22228AEE95F38490E9769C39B2773ED763D9CD5F");
    private static byte[] account3 = Hex.decode("33338AEE95F38490E9769C39B2773ED763D9CD5E");
    private static byte[] account4 = Hex.decode("44448AEE95F38490E9769C39B2773ED763D9CD5E");

    @Before
    public void setUp() throws Exception {
        this.config = new ErpConfig();
        this.repo = Mockito.spy(new RepositoryRoot(new HashMapDB<>()));
    }

    @Test
    public void getExtraData_nonTargetBlocks() {
        final byte[] minerData = Hex.decode("deadbeef");
        assertArrayEquals(minerData, this.config.getExtraData(minerData, 1));
        assertArrayEquals(minerData, this.config.getExtraData(minerData, 4_000_000));
        assertArrayEquals(minerData, this.config.getExtraData(minerData, 7_000_000));
    }

    @Test(expected = RuntimeException.class)
    public void failedInitThrows() throws IOException {
        ErpLoader failingLoader = mock(ErpLoader.class);

        when(failingLoader.loadErpMetadata()).thenThrow(new IOException("Test"));

        new ErpConfig(new HomesteadConfig(), failingLoader, new ErpExecutor());
    }

    @Test
    public void getExtraData_targetBlock() {
        final byte[] minerData = Hex.decode("deadbeef");
        final byte[] expected = ByteUtil.hexStringToBytes("6572702d383838");
        assertArrayEquals(expected, this.config.getExtraData(minerData, 6000000));
    }

    @Test
    public void hardForkTransfers_targetBlock() {
        repo.addBalance(account1, BigInteger.valueOf(1234L));
        repo.saveCode(account3, ByteUtil.hexStringToBytes("0xdada"));

        final Block block = mock(Block.class);
        when(block.getNumber()).thenReturn(6000000L);
        Repository spyRepo = Mockito.spy(repo);

        this.config.hardForkTransfers(block, spyRepo);

        assertEquals(BigInteger.ZERO, repo.getBalance(account1));
        assertEquals(BigInteger.valueOf(1234L), repo.getBalance(account2));

        assertArrayEquals(ByteUtil.hexStringToBytes("0xdeadbeef3"), repo.getCode(account3));
        assertArrayEquals(ByteUtil.hexStringToBytes("0xdeadbeef4"), repo.getCode(account4));
    }

    @Test
    public void hardForkTransfers_nonTargetBlock() {
        repo.addBalance(account1, BigInteger.valueOf(1234L));
        repo.saveCode(account3, ByteUtil.hexStringToBytes("0xdada"));

        final Block block = mock(Block.class);
        when(block.getNumber()).thenReturn(456L);

        this.config.hardForkTransfers(block, repo);

        assertEquals(BigInteger.valueOf(1234L), repo.getBalance(account1));
        assertEquals(BigInteger.ZERO, repo.getBalance(account2));

        assertArrayEquals(ByteUtil.hexStringToBytes("0xdada"), repo.getCode(account3));
        assertArrayEquals(EMPTY_BYTE_ARRAY, repo.getCode(account4));
    }

    @Test(expected = RuntimeException.class)
    public void hardForkTransfers_failingScoLoadThrows() throws IOException {
        final Block block = mock(Block.class);
        when(block.getNumber()).thenReturn(6000000L);

        ErpLoader mockLoader = mock(ErpLoader.class);
        ErpLoader actualLoader = new ErpLoader("/erps");
        when(mockLoader.loadErpMetadata()).thenReturn(actualLoader.loadErpMetadata());
        when(mockLoader.loadStateChangeObject(Mockito.any())).thenThrow(new IOException("Test"));

        ErpConfig config = new ErpConfig(new HomesteadConfig(), mockLoader, new ErpExecutor());
        config.hardForkTransfers(block, repo);
    }

    @Test
    public void doHardForkTransfers_failingAction() throws IOException, ErpExecutor.ErpExecutionException {
        ErpLoader mockLoader = mock(ErpLoader.class);
        ErpLoader.ErpMetadata metadata = mock(ErpLoader.ErpMetadata.class);
        ErpExecutor mockExecutor = mock(ErpExecutor.class);
        Repository mockRepo = mock(Repository.class);
        Repository track = mock(Repository.class);
        StateChangeObject sco = mock(StateChangeObject.class);

        when(mockLoader.loadErpMetadata()).thenReturn(Collections.emptyList());
        when(mockLoader.loadStateChangeObject(Mockito.eq(metadata))).thenReturn(sco);
        when(metadata.getId()).thenReturn("mockErp");
        when(metadata.getTargetBlock()).thenReturn(1L);

        doThrow(mock(ErpExecutor.ErpExecutionException.class))
                .when(mockExecutor)
                .applyStateChanges(Mockito.eq(sco), Mockito.eq(track));

        when(mockRepo.startTracking())
                .thenReturn(track)
                .thenThrow(new RuntimeException("Did not expect another call to startTracking"));

        ErpConfig config = new ErpConfig(new HomesteadConfig(), mockLoader, mockExecutor);
        config.doHardForkTransfers(metadata, mockRepo);

        // Verify the right interactions occurred
        Mockito.verify(mockExecutor, Mockito.times(1)).applyStateChanges(Mockito.eq(sco), Mockito.eq(track));

        Mockito.verify(track, Mockito.never()).commit();
        Mockito.verify(track, Mockito.times(1)).rollback();
        Mockito.verify(track, Mockito.times(1)).close();

        Mockito.verify(mockRepo, Mockito.never()).commit();
        Mockito.verify(mockRepo, Mockito.never()).rollback();
        Mockito.verify(mockRepo, Mockito.never()).close();
    }

    @Test
    public void doHardForkTransfers_rethrowsOtherExceptions() throws IOException, ErpExecutor.ErpExecutionException {
        ErpLoader mockLoader = mock(ErpLoader.class);
        ErpLoader.ErpMetadata metadata = mock(ErpLoader.ErpMetadata.class);
        ErpExecutor mockExecutor = mock(ErpExecutor.class);
        Repository mockRepo = mock(Repository.class);
        Repository track = mock(Repository.class);
        StateChangeObject sco = mock(StateChangeObject.class);

        when(mockLoader.loadErpMetadata()).thenReturn(Collections.emptyList());
        when(mockLoader.loadStateChangeObject(Mockito.eq(metadata))).thenReturn(sco);
        when(metadata.getId()).thenReturn("mockErp");
        when(metadata.getTargetBlock()).thenReturn(1L);

        doThrow(new RuntimeException("Something bad happened"))
                .when(mockExecutor)
                .applyStateChanges(Mockito.eq(sco), Mockito.eq(track));

        when(mockRepo.startTracking())
                .thenReturn(track)
                .thenThrow(new RuntimeException("Did not expect another call to startTracking"));

        ErpConfig config = new ErpConfig(new HomesteadConfig(), mockLoader, mockExecutor);
        try {
            config.doHardForkTransfers(metadata, mockRepo);
            throw new Exception("Unknown exceptions should be rethrown");
        } catch (Exception e) {
            // Expected
        }

        Mockito.verify(track, Mockito.never()).commit();
        Mockito.verify(track, Mockito.times(1)).rollback();
        Mockito.verify(track, Mockito.times(1)).close();

        Mockito.verify(mockRepo, Mockito.never()).commit();
        Mockito.verify(mockRepo, Mockito.never()).rollback();
        Mockito.verify(mockRepo, Mockito.never()).close();
    }

    /**
     * Not sure if these delegate methods are needed (or if more are needed).  However, adding this here
     * to indicate that whatever delegate method calls should be supported, they should be covered in the
     * test cases.
     * @throws IOException
     */
    @Test
    public void delegateMethodCalls() throws IOException {
        BlockchainConfig parent = mock(BlockchainConfig.class);
        ErpLoader mockLoader = mock(ErpLoader.class);
        ErpExecutor mockExecutor = mock(ErpExecutor.class);
        BlockHeader header1 = mock(BlockHeader.class);
        BlockHeader header2 = mock(BlockHeader.class);
        Transaction tx = mock(Transaction.class);

        when(mockLoader.loadErpMetadata()).thenReturn(Collections.emptyList());

        ErpConfig config = new ErpConfig(parent, mockLoader, mockExecutor);
        config.calcDifficulty(header1, header2);
        config.getTransactionCost(tx);
        config.acceptTransactionSignature(tx);

        Mockito.verify(parent, Mockito.times(1)).calcDifficulty(header1, header2);
        Mockito.verify(parent, Mockito.times(1)).getTransactionCost(tx);
        Mockito.verify(parent, Mockito.times(1)).acceptTransactionSignature(tx);
    }
}