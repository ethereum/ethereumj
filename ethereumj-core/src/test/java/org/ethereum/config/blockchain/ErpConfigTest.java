package org.ethereum.config.blockchain;

import org.ethereum.core.Block;
import org.ethereum.core.Repository;
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

import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

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
        this.repo = new RepositoryRoot(new HashMapDB<>());
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
        ErpLoader failingLoader = Mockito.mock(ErpLoader.class);

        Mockito.when(failingLoader.loadErpMetadata()).thenThrow(new IOException("Test"));

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

        final Block block = Mockito.mock(Block.class);
        Mockito.when(block.getNumber()).thenReturn(6000000L);
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

        final Block block = Mockito.mock(Block.class);
        Mockito.when(block.getNumber()).thenReturn(456L);

        Repository spyRepo = Mockito.spy(repo);

        this.config.hardForkTransfers(block, spyRepo);

        assertEquals(BigInteger.valueOf(1234L), repo.getBalance(account1));
        assertEquals(BigInteger.ZERO, repo.getBalance(account2));

        assertArrayEquals(ByteUtil.hexStringToBytes("0xdada"), repo.getCode(account3));
        assertArrayEquals(EMPTY_BYTE_ARRAY, repo.getCode(account4));
    }

    @Test(expected = RuntimeException.class)
    public void hardForkTransfers_failingScoLoadThrows() throws IOException {
        final Block block = Mockito.mock(Block.class);
        Mockito.when(block.getNumber()).thenReturn(6000000L);

        ErpLoader mockLoader = Mockito.mock(ErpLoader.class);
        ErpLoader actualLoader = new ErpLoader("/erps");
        Mockito.when(mockLoader.loadErpMetadata()).thenReturn(actualLoader.loadErpMetadata());
        Mockito.when(mockLoader.loadStateChangeObject(Mockito.any())).thenThrow(new IOException("Test"));

        ErpConfig config = new ErpConfig(new HomesteadConfig(), mockLoader, new ErpExecutor());
        config.hardForkTransfers(block, repo);
    }

    @Test(expected = RuntimeException.class)
    public void hardForkTransfers_failingAction() throws IOException {
        final Block block = Mockito.mock(Block.class);
        Mockito.when(block.getNumber()).thenReturn(6000000L);

        StateChangeObject badStageChangeObject = new StateChangeObject();
        badStageChangeObject.erpId = "eip-123";
        badStageChangeObject.targetBlock = 6000000L;
        RawStateChangeObject.RawStateChangeAction badAction = new RawStateChangeObject.RawStateChangeAction();
        badAction.type = "nonsense";
        badStageChangeObject.actions = new StateChangeObject.StateChangeAction[]{StateChangeObject.StateChangeAction.parse(badAction)};

        ErpLoader mockLoader = Mockito.mock(ErpLoader.class);
        ErpLoader actualLoader = new ErpLoader("/erps");
        Mockito.when(mockLoader.loadErpMetadata()).thenReturn(actualLoader.loadErpMetadata());
        Mockito.when(mockLoader.loadStateChangeObject(Mockito.any())).thenReturn(badStageChangeObject);

        ErpConfig config = new ErpConfig(new HomesteadConfig(), mockLoader, new ErpExecutor());
        config.hardForkTransfers(block, repo);
    }
}