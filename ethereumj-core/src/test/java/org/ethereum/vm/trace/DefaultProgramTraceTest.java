package org.ethereum.vm.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ethereum.config.SystemProperties;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.OpCode;
import org.ethereum.vm.program.invoke.ProgramInvoke;
import org.ethereum.vm.program.invoke.ProgramInvokeMockImpl;
import org.junit.Test;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class DefaultProgramTraceTest {

    /**
     * contract:
     * {@link DefaultProgramTrace#ProgramTrace(org.ethereum.config.SystemProperties, org.ethereum.vm.program.invoke.ProgramInvoke)}
     * constructor set the contract address value if program invoke was not null and
     * vmTrace was true
     * 
     */
    @Test
    public void testContractAddress() {

	ProgramInvoke programInvoke = new ProgramInvokeMockImpl();
	SystemProperties systemProperties = SystemProperties.getDefault();

	DefaultProgramTrace trace = new DefaultProgramTrace(systemProperties, programInvoke);
	assertNull(trace.getContractAddress());

	Map<String, Boolean> cliOptions = new HashMap<>();
	cliOptions.put("vm.structured.trace", true);
	Config config = ConfigFactory.parseMap(cliOptions);
	systemProperties = new SystemProperties(config);

	trace = new DefaultProgramTrace(systemProperties, programInvoke);
	assertEquals("cd2a3d9f938e13cd947ec05abc7fe734df8dd826", trace.getContractAddress());

    }

    /**
     * contract: {@link DefaultProgramTrace#result(byte[])} method converts the byte array
     * to a HEX string and set it in the "result" instance variable
     * 
     */
    @Test
    public void testResult() {
	DefaultProgramTrace trace = new DefaultProgramTrace();
	byte[] result = { 0x41, 0x42, 0x43 };
	trace.result(result);
	assertEquals("414243", trace.getResult());
    }

    /**
     * contract: {@link DefaultProgramTrace#result(byte[])} method sets the result to an
     * empty string if the byte array is null
     * 
     * 
     */
    @Test
    public void testResultWithNull() {
	DefaultProgramTrace trace = new DefaultProgramTrace();
	trace.setResult("414243");
	trace.result(null);
	assertTrue(trace.getResult().isEmpty());
    }

    /**
     * contract: {@link DefaultProgramTrace#error(Exception)} method sets the instance
     * variable "error" to "ExceptionClass: Message"
     * 
     */
    @Test
    public void testError() {
	DefaultProgramTrace trace = new DefaultProgramTrace();
	Exception error = new RuntimeException("error occured");
	trace.error(error);
	assertEquals("class java.lang.RuntimeException: error occured", trace.getError());
    }

    /**
     * contract: {@link DefaultProgramTrace#error(Exception)} method sets the instance
     * variable "error" to empty string if the exception passed was null
     * 
     */
    @Test
    public void testErrorWithNull() {
	DefaultProgramTrace trace = new DefaultProgramTrace();
	trace.setError("Some initial error");
	trace.error(null);
	assertTrue(trace.getError().isEmpty());
    }

    /**
     * contract:
     * {@link DefaultProgramTrace#addOp(byte, int, int, org.ethereum.vm.DataWord, OpActions)}
     * method creates a new {@link Op} object and add it to the "ops" list
     * 
     */
    @Test
    public void testAddOp() {
	DefaultProgramTrace trace = new DefaultProgramTrace();
	assertEquals(0, trace.getOps().size());
	DataWord gas = new DataWord();
	OpActions actions = new OpActions();
	Op addedOp = trace.addOp((byte) 'c', 1, 2, gas, actions);

	assertEquals(OpCode.code((byte) 'c'), addedOp.getCode());
	assertEquals(1, addedOp.getPc());
	assertEquals(2, addedOp.getDeep());
	assertEquals(gas.value(), addedOp.getGas());
	assertSame(actions, addedOp.getActions());
	assertSame(addedOp, trace.getOps().get(0));
    }

    /**
     * contract: {@link DefaultProgramTrace#merge(DefaultProgramTrace)} method the items in "ops"
     * list of the send DefaultProgramTrace object to the "ops" list of this object
     * 
     */
    @Test
    public void testMerge() {
	DefaultProgramTrace trace1 = new DefaultProgramTrace();
	List<Op> ops1 = new ArrayList<>();
	ops1.add(new Op());
	ops1.add(new Op());
	ops1.add(new Op());
	trace1.setOps(ops1);

	DefaultProgramTrace trace2 = new DefaultProgramTrace();
	List<Op> ops2 = new ArrayList<>();
	ops2.add(new Op());
	ops2.add(new Op());
	ops2.add(new Op());
	trace2.setOps(ops2);

	trace1.merge(trace2);
	assertEquals(6, trace1.getOps().size());
	assertSame(ops2.get(0), trace1.getOps().get(3));
	assertSame(ops2.get(1), trace1.getOps().get(4));
	assertSame(ops2.get(2), trace1.getOps().get(5));
    }

    /**
     * contract: {@link DefaultProgramTrace#asJsonString(boolean)} method converts the
     * DefaultProgramTrace to a json string
     * 
     */
    @Test
    public void testAsJsonString() {
	DefaultProgramTrace trace = new DefaultProgramTrace();
	trace.setError("Error");
	trace.setContractAddress("Contract Address");
	trace.setResult("Result");
	List<Op> ops = new ArrayList<>();
	ops.add(new Op());
	ops.add(new Op());
	ops.add(new Op());
	trace.setOps(ops);

	assertEquals(
		"{\"ops\":[{\"code\":null,\"deep\":0,\"pc\":0,\"gas\":null,\"actions\":null},{\"code\":null,\"deep\":0,\"pc\":0,\"gas\":null,\"actions\":null},{\"code\":null,\"deep\":0,\"pc\":0,\"gas\":null,\"actions\":null}],\"result\":\"Result\",\"error\":\"Error\",\"contractAddress\":\"Contract Address\"}",
		trace.asJsonString(false));
	assertEquals("{\n" + "  \"ops\" : [ {\n" + "    \"code\" : null,\n" + "    \"deep\" : 0,\n"
		+ "    \"pc\" : 0,\n" + "    \"gas\" : null,\n" + "    \"actions\" : null\n" + "  }, {\n"
		+ "    \"code\" : null,\n" + "    \"deep\" : 0,\n" + "    \"pc\" : 0,\n" + "    \"gas\" : null,\n"
		+ "    \"actions\" : null\n" + "  }, {\n" + "    \"code\" : null,\n" + "    \"deep\" : 0,\n"
		+ "    \"pc\" : 0,\n" + "    \"gas\" : null,\n" + "    \"actions\" : null\n" + "  } ],\n"
		+ "  \"result\" : \"Result\",\n" + "  \"error\" : \"Error\",\n"
		+ "  \"contractAddress\" : \"Contract Address\"\n" + "}", trace.asJsonString(true));
    }

    /**
     * contract: {@link DefaultProgramTrace#toString()} method return a formatted json
     * string
     * 
     */
    @Test
    public void testToString() {
	DefaultProgramTrace trace = new DefaultProgramTrace();
	trace.setError("Error");
	trace.setContractAddress("Contract Address");
	trace.setResult("Result");
	List<Op> ops = new ArrayList<>();
	ops.add(new Op());
	ops.add(new Op());
	ops.add(new Op());
	trace.setOps(ops);

	assertEquals("{\n" + "  \"ops\" : [ {\n" + "    \"code\" : null,\n" + "    \"deep\" : 0,\n"
		+ "    \"pc\" : 0,\n" + "    \"gas\" : null,\n" + "    \"actions\" : null\n" + "  }, {\n"
		+ "    \"code\" : null,\n" + "    \"deep\" : 0,\n" + "    \"pc\" : 0,\n" + "    \"gas\" : null,\n"
		+ "    \"actions\" : null\n" + "  }, {\n" + "    \"code\" : null,\n" + "    \"deep\" : 0,\n"
		+ "    \"pc\" : 0,\n" + "    \"gas\" : null,\n" + "    \"actions\" : null\n" + "  } ],\n"
		+ "  \"result\" : \"Result\",\n" + "  \"error\" : \"Error\",\n"
		+ "  \"contractAddress\" : \"Contract Address\"\n" + "}", trace.toString());
    }
}
