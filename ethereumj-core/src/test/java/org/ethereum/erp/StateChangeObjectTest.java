package org.ethereum.erp;

import org.ethereum.erp.RawStateChangeObject.RawStateChangeAction;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import static org.ethereum.crypto.HashUtil.EMPTY_DATA_HASH;
import static org.junit.Assert.*;

public class StateChangeObjectTest {

    private static String strAccount1 = "11113D9F938E13CD947EC05ABC7FE734DF8DD826";
    private static String strAccount2 = "22223D9F938E13CD947EC05ABC7FE734DF8DD826";
    private static byte[] account1 = Hex.decode(strAccount1);
    private static byte[] account2 = Hex.decode(strAccount2);

    @Test
    public void parseOk() {
        RawStateChangeObject raw = new RawStateChangeObject();
        raw.erpId = "eip-1234";
        raw.targetBlock = 1234L;
        RawStateChangeAction rawAction = new RawStateChangeAction();
        rawAction.fromAddress = strAccount1;
        rawAction.toAddress = strAccount2;
        rawAction.valueInWei = "123";
        rawAction.type = "weiTransfer";
        raw.actions = new RawStateChangeAction[]{ rawAction };

        final StateChangeObject sco = StateChangeObject.parse(raw);

        assertEquals(1234L, sco.targetBlock);
        assertEquals("eip-1234", sco.erpId);
        assertEquals(EMPTY_DATA_HASH, sco.actions[0].expectedCodeHash);
        assertArrayEquals(account1, sco.actions[0].fromAddress);
        assertArrayEquals(account2, sco.actions[0].toAddress);
        assertEquals("weiTransfer", sco.actions[0].type);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseFailsOnNegativeValue() {
        RawStateChangeObject raw = new RawStateChangeObject();
        RawStateChangeAction rawAction = new RawStateChangeAction();
        rawAction.valueInWei = "-1";
        raw.actions = new RawStateChangeAction[]{ rawAction };
        StateChangeObject.parse(raw);
    }
}