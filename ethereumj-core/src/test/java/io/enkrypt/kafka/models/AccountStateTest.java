package io.enkrypt.kafka.models;

import org.junit.Test;

import java.math.BigInteger;
import java.util.Random;

import static org.junit.Assert.*;

public class AccountStateTest {

  private final Random random = new Random();

  @Test
  public void testRLPEncoding(){

    final AccountState empty = AccountState.newBuilder()
      .build();

    final AccountState decodedEmpty = AccountState.newBuilder(empty.getEncoded()).build();
    assertEquals(empty, decodedEmpty);
    assertTrue(empty.isEmpty());

    final AccountState normal = AccountState.newBuilder()
      .setNonce(BigInteger.ZERO)
      .setBalance(BigInteger.valueOf(5))
      .setCodeHash(new byte[]{1, 2, 3, 4, 5})
      .setStateRoot(new byte[]{2, 3, 4, 5, 6})
      .build();

    assertNotEquals(normal, empty);
    assertFalse(normal.isEmpty());
    assertEquals(normal, AccountState.newBuilder(normal.getEncoded()).build());

    final AccountState contract = AccountState.newBuilder()
      .setNonce(BigInteger.ZERO)
      .setBalance(BigInteger.valueOf(5))
      .setCodeHash(new byte[]{3, 4, 5, 6, 7})
      .setStateRoot(new byte[]{4, 5, 6, 7, 8})
      .setCode(new byte[]{10, 11, 12})
      .setCreator(new byte[]{13, 14, 15})
      .build();

    assertNotEquals(contract, empty);
    assertNotEquals(contract, normal);
    assertFalse(contract.isEmpty());
    assertEquals(contract, AccountState.newBuilder(contract.getEncoded()).build());

    final AccountState miner = AccountState.newBuilder()
      .setMiner(true)
      .build();

    assertEquals(miner, AccountState.newBuilder(miner.getEncoded()).build());
  }

}
