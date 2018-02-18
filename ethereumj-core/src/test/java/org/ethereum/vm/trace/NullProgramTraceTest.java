package org.ethereum.vm.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.ethereum.vm.DataWord;
import org.junit.Test;

public class NullProgramTraceTest {

    /**
     * contract: {@link NullProgramTrace#getContractAddress()} returns an empty
     * string
     * 
     */
    @Test
    public void testContractAddress() {

        ProgramTrace trace = new NullProgramTrace();
        trace.setContractAddress("contact address");
        assertTrue(trace.getContractAddress().isEmpty());
    }

    /**
     * contract: {@link NullProgramTrace#getResult()} returns an empty String
     * 
     */
    @Test
    public void testResult() {
        ProgramTrace trace = new NullProgramTrace();
        byte[] result = { 0x41, 0x42, 0x43 };
        trace.result(result);
        assertTrue(trace.getResult().isEmpty());
        trace.setResult("result");
        assertTrue(trace.getResult().isEmpty());
    }

    /**
     * contract: {@link NullProgramTrace#result(byte[])} returns the same
     * NullProgramTrace instance
     * 
     * 
     */
    @Test
    public void testResultWithNull() {
        ProgramTrace trace = new NullProgramTrace();
        ProgramTrace trace2 = trace.result(null);
        assertSame(trace, trace2);
    }

    /**
     * contract: {@link NullProgramTrace#error(Exception)} returns the same
     * NullProgramTrace instance
     * 
     */
    @Test
    public void testError() {
        ProgramTrace trace = new NullProgramTrace();
        ProgramTrace trace2 = trace.error(null);
        assertSame(trace, trace2);
    }

    /**
     * contract: {@link NullProgramTrace#getError()} returns an empty String
     * 
     */
    @Test
    public void testErrorWithNull() {
        ProgramTrace trace = new NullProgramTrace();
        trace.error(new RuntimeException());
        assertTrue(trace.getError().isEmpty());
        trace.setError("Some initial error");
        assertTrue(trace.getError().isEmpty());
    }

    /**
     * contract:
     * {@link NullProgramTrace#addOp(byte, int, int, org.ethereum.vm.DataWord, OpActions)}
     * method don't return null
     * 
     */
    @Test
    public void testAddOp() {
        ProgramTrace trace = new NullProgramTrace();
        DataWord gas = new DataWord();
        OpActions actions = new OpActions();
        Op op = trace.addOp((byte) 'c', 1, 2, gas, actions);

        assertNotNull(op);
    }

    /**
     * contract: {@link NullProgramTrace#getOps()} always returns an empty list
     * 
     */
    @Test
    public void testMerge() {
        ProgramTrace trace = new NullProgramTrace();
        List<Op> ops1 = new ArrayList<>();
        ops1.add(new Op());
        ops1.add(new Op());
        ops1.add(new Op());
        trace.setOps(ops1);
        assertTrue(trace.getOps().isEmpty());

        DefaultProgramTrace trace2 = new DefaultProgramTrace();
        List<Op> ops2 = new ArrayList<>();
        ops2.add(new Op());
        ops2.add(new Op());
        ops2.add(new Op());
        trace2.setOps(ops2);
        trace.merge(trace2);
        assertTrue(trace.getOps().isEmpty());
    }

    /**
     * contract: {@link NullProgramTrace#asJsonString(boolean)} returns an empty
     * json string
     * 
     */
    @Test
    public void testAsJsonString() {
        ProgramTrace trace = new NullProgramTrace();
        trace.setError("Error");
        trace.setContractAddress("Contract Address");
        trace.setResult("Result");
        List<Op> ops = new ArrayList<>();
        ops.add(new Op());
        ops.add(new Op());
        ops.add(new Op());
        trace.setOps(ops);

        assertEquals("{}", trace.asJsonString(false));
        assertEquals("{}", trace.asJsonString(true));
    }

    /**
     * contract: {@link NullProgramTrace#toString()} returns an empty json string
     * 
     */
    @Test
    public void testToString() {
        ProgramTrace trace = new NullProgramTrace();
        trace.setError("Error");
        trace.setContractAddress("Contract Address");
        trace.setResult("Result");
        List<Op> ops = new ArrayList<>();
        ops.add(new Op());
        ops.add(new Op());
        ops.add(new Op());
        trace.setOps(ops);

        assertEquals("{}", trace.toString());
    }
}
