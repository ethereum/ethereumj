package io.enkrypt.kafka.models;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TokenTransferTest {


  @Test
  public void testRLPEncoding(){

    final TokenTransfer empty = TokenTransfer.newBuilder()
      .build();

    final TokenTransfer decodedEmpty = TokenTransfer.newBuilder(empty.getEncoded()).build();
    assertEquals(empty, decodedEmpty);


  }

}
