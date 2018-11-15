package org.ethereum.validator;

import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.BlockSummary;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListener;
import org.ethereum.mine.EthashValidationHelper;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.util.Collections;

import static org.ethereum.validator.BlockHeaderRule.Success;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Mikhail Kalinin
 * @since 12.07.2018
 */
public class EthashRuleTest {

  static class CompositeEthereumListenerMock extends CompositeEthereumListener {
    @Override
    public void onBlock(final BlockSummary blockSummary, final boolean best) {
      for (final EthereumListener listener : listeners) {
        listener.onBlock(blockSummary, best);
      }
    }

    @Override
    public void onSyncDone(final SyncState state) {
      for (final EthereumListener listener : listeners) {
        listener.onSyncDone(state);
      }
    }
  }

  static class EthashValidationHelperMock extends EthashValidationHelper {

    long preCacheCounter = 0;

    public EthashValidationHelperMock(CacheOrder cacheOrder) {
      super(cacheOrder);
    }

    @Override
    public void preCache(long blockNumber) {
      preCacheCounter += 1;
    }
  }

  BlockHeader validHeader = new BlockHeader(Hex.decode("f90211a0d4e56740f876aef8c010b86a40d5f56745a118d0906a34e69aec8c0db1cb8fa3a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d493479405a56e2d52c817161883f50c441c3228cfe54d9fa0d67e4d450343046425ae4271474353857ab860dbc0a1dde64b41b5cd3a532bf3a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008503ff80000001821388808455ba422499476574682f76312e302e302f6c696e75782f676f312e342e32a0969b900de27b6ac6a67742365dd65f55a0526c41fd18e1b16f1a1215c2e66f5988539bd4979fef1ec4"));
  BlockHeader partlyValidHeader = new BlockHeader(Hex.decode("f9020aa0548911f91c652dd110641a55f09fa4fa83d9c28d3afddce60a64256fb58468e9a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000a09c7460dbfd853c07a340e55bab456d4035190400246b731b193ec8c8044f41aea056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b9010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000830200000185100000000080845b471fc591457468657265756d4a20706f7765726564a03e2b0549a7219dd2a5c125aee28676a754272fea5d548f314521508635a8387888d3c67376b7804888"));
  BlockHeader invalidHeader = new BlockHeader(Hex.decode("f90211a0d4e56740f876aef8c010b86a40d5f56745a118d0906a34e69aec8c0db1cb8fa3a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d493479405a56e2d52c817161883f50c441c3228cfe54d9fa0d67e4d450343046425ae4271474353857ab860dbc0a1dde64b41b5cd3a532bf3a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008503ff80000001821388808455ba422499476574682f76312e302e302f6c696e75782f676f312e342e32a0969b900de27b6ac6a67742365dd65f55a0526c41fd18e1b16f1a1215c2e66f5a88539bd4979fef1ec4"));

  BlockSummary dummySummaryNum_1 = new BlockSummary(new Block(validHeader, Collections.emptyList(), Collections.emptyList()),
    Collections.emptyMap(), Collections.emptyList(), Collections.emptyList());

  @Test
  public void fake() {
    System.out.println(Hex.toHexString(new Block(Hex.decode("f9020ff9020aa0548911f91c652dd110641a55f09fa4fa83d9c28d3afddce60a64256fb58468e9a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000a09c7460dbfd853c07a340e55bab456d4035190400246b731b193ec8c8044f41aea056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b9010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000830200000185100000000080845b471fc591457468657265756d4a20706f7765726564a03e2b0549a7219dd2a5c125aee28676a754272fea5d548f314521508635a8387888d3c67376b7804888c0c0")).getHeader().getEncoded()));
  }

  @Test
  public void testFake() {
    CompositeEthereumListener listener = new CompositeEthereumListenerMock();
    EthashRule rule = new EthashRule(EthashRule.Mode.fake, EthashRule.ChainType.main, listener);

    assertEquals(Success, rule.validate(validHeader));
    assertEquals(Success, rule.validate(partlyValidHeader));
    assertNotEquals(Success, rule.validate(invalidHeader));
  }

  @Test
  public void testStrict() {
    CompositeEthereumListener listener = new CompositeEthereumListenerMock();
    EthashRule rule = new EthashRule(EthashRule.Mode.strict, EthashRule.ChainType.main, listener);

    // cache is empty, fallback into fake rule
    assertEquals(Success, rule.validate(partlyValidHeader));

    // trigger ethash cache
    listener.onBlock(dummySummaryNum_1, true);

    assertEquals(Success, rule.validate(validHeader));
    assertNotEquals(Success, rule.validate(partlyValidHeader));
    assertNotEquals(Success, rule.validate(invalidHeader));

    // check that full verification is done on each run in strict mode
    for (int i = 0; i < 100; i++) {
      assertNotEquals(Success, rule.validate(partlyValidHeader));
    }
  }

  @Test
  public void testMixed() {
    CompositeEthereumListener listener = new CompositeEthereumListenerMock();
    EthashRule rule = new EthashRule(EthashRule.Mode.mixed, EthashRule.ChainType.main, listener);

    // trigger ethash cache
    listener.onBlock(dummySummaryNum_1, true);

    // check mixed mode randomness
    boolean fullCheckTriggered = false;
    boolean partialCheckTriggered = false;
    for (int i = 0; i < 100; i++) {
      if (Success != rule.validate(partlyValidHeader)) {
        fullCheckTriggered = true;
      } else {
        partialCheckTriggered = true;
      }

      if (partialCheckTriggered & fullCheckTriggered) break;
    }
    assertTrue(fullCheckTriggered);
    assertTrue(partialCheckTriggered);

    // trigger onSyncDone
    listener.onSyncDone(EthereumListener.SyncState.COMPLETE);

    // check that full verification is done on each run in strict mode
    for (int i = 0; i < 100; i++) {
      assertNotEquals(Success, rule.validate(partlyValidHeader));
    }
  }

  @Test
  public void testCacheMain() {
    EthashValidationHelperMock helper = new EthashValidationHelperMock(EthashValidationHelper.CacheOrder.direct);
    CompositeEthereumListener listener = new CompositeEthereumListenerMock();
    EthashRule rule = new EthashRule(EthashRule.Mode.mixed, EthashRule.ChainType.main, listener);
    rule.ethashHelper = helper;

    // trigger cache
    for (int i = 0; i < 100; i++) {
      listener.onBlock(dummySummaryNum_1, false);
      listener.onBlock(dummySummaryNum_1, true);
    }

    // must be triggered on best block only
    assertEquals(100, helper.preCacheCounter);
  }

  @Test
  public void testCacheDirect() {
    EthashValidationHelperMock helper = new EthashValidationHelperMock(EthashValidationHelper.CacheOrder.direct);
    CompositeEthereumListener listener = new CompositeEthereumListenerMock();
    EthashRule rule = new EthashRule(EthashRule.Mode.mixed, EthashRule.ChainType.direct, listener);
    rule.ethashHelper = helper;

    // trigger cache
    for (int i = 0; i < 100; i++) {
      rule.validate(validHeader);
    }

    // must be triggered each verification attempt, in spite of mixed mode
    assertEquals(100, helper.preCacheCounter);
  }
  @Test
  public void testCacheReverse() {
    EthashValidationHelperMock helper = new EthashValidationHelperMock(EthashValidationHelper.CacheOrder.direct);
    CompositeEthereumListener listener = new CompositeEthereumListenerMock();
    EthashRule rule = new EthashRule(EthashRule.Mode.mixed, EthashRule.ChainType.reverse, listener);
    rule.ethashHelper = helper;

    // trigger cache
    for (int i = 0; i < 100; i++) {
      rule.validate(validHeader);
    }

    // must be triggered each verification attempt, in spite of mixed mode
    assertEquals(100, helper.preCacheCounter);
  }
}
