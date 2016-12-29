package org.ethereum.solidity;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

import org.ethereum.solidity.Abi.Entry;
import org.ethereum.solidity.Abi.Entry.Type;
import org.junit.Test;

import java.io.IOException;

public class AbiTest {

    @Test
    public void simpleTest() throws IOException {
        String contractAbi = "[{"
                + "\"name\":\"simpleFunction\","
                + "\"constant\":true,"
                + "\"payable\":true,"
                + "\"type\":\"function\","
                + "\"inputs\": [{\"name\":\"_in\", \"type\":\"bytes32\"}],"
                + "\"outputs\":[{\"name\":\"_out\",\"type\":\"bytes32\"}]}]";

        Abi abi = Abi.fromJson(contractAbi);
        assertEquals(abi.size(), 1);

        Entry onlyFunc = abi.get(0);
        assertEquals(onlyFunc.type, Type.function);
        assertEquals(onlyFunc.inputs.size(), 1);
        assertEquals(onlyFunc.outputs.size(), 1);
        assertTrue(onlyFunc.payable);
        assertTrue(onlyFunc.constant);
    }

    public static void main(String[] args) throws Exception {
        new AbiTest().simpleTest();
    }
}
