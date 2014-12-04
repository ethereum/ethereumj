package org.ethereum.jsontestsuite;

import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.ContractDetails;
import org.ethereum.db.RepositoryImpl;
import org.ethereum.facade.Repository;
import org.ethereum.util.ByteUtil;
import org.ethereum.vm.*;
import org.ethereum.vmtrace.ProgramTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.*;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 02/07/2014 13:03
 */

public class TestRunner {

    private Logger logger = LoggerFactory.getLogger("JSONTest");
    private ProgramTrace trace = null;

    public List<String> runTestSuite(TestSuite testSuite) {
    	
        Iterator<TestCase> testIterator = testSuite.iterator();
        List<String> resultCollector = new ArrayList<>();

        while (testIterator.hasNext()) {

            TestCase testCase = testIterator.next();

            TestRunner runner = new TestRunner();
            List<String> result = runner.runTestCase(testCase);
            resultCollector.addAll(result);
        }

        return resultCollector;
    }


    public List<String> runTestCase(TestCase testCase) {

    	Repository repository = new RepositoryImpl();
    	
    	try {
	    	System.out.println("\nRunning test case: " + testCase.getName());
	        List<String> results = new ArrayList<>();
	        
	        System.out.println("--------- PRE ---------");
	        /* 1. Store pre-exist accounts - Pre */
	        for (ByteArrayWrapper key : testCase.getPre().keySet()) {
	
	            AccountState accountState = testCase.getPre().get(key);
	
	            repository.createAccount(key.getData());
	            repository.saveCode(key.getData(), accountState.getCode());
	            repository.addBalance(key.getData(), new BigInteger(accountState.getBalance()));
	
	            for (long i = 0; i < accountState.getNonceLong(); ++i)
	                repository.increaseNonce(key.getData());
	        }
	
	        /* 2. Create ProgramInvoke - Env/Exec */
	        Env  env  = testCase.getEnv();
	        Exec exec = testCase.getExec();
	        Logs logs = testCase.getLogs();
	
	        byte[] address     = exec.getAddress();
	        byte[] origin      = exec.getOrigin();
	        byte[] caller      = exec.getCaller();
	        byte[] balance     = ByteUtil.bigIntegerToBytes(repository.getBalance(exec.getAddress()));
	        byte[] gasPrice    = exec.getGasPrice();
	        byte[] gas         = exec.getGas();
	        byte[] callValue   = exec.getValue();
	        byte[] msgData     = exec.getData();
	        byte[] lastHash    = env.getPreviousHash();
	        byte[] coinbase    = env.getCurrentCoinbase();
	        long timestamp     = new BigInteger(env.getCurrentTimestamp()).longValue();
	        long number        = new BigInteger(env.getCurrentNumber()).longValue();
	        byte[] difficulty  = env.getCurrentDifficlty();
	        long gaslimit      = new BigInteger(env.getCurrentGasLimit()).longValue();
	
	        // Origin and caller need to exist in order to be able to execute
	        if(repository.getAccountState(origin) == null)
	        	repository.createAccount(origin);
	        if(repository.getAccountState(caller) == null)
	        	repository.createAccount(caller);
	        
	        ProgramInvoke programInvoke = new ProgramInvokeImpl(address, origin, caller, balance,
	                gasPrice, gas, callValue, msgData, lastHash, coinbase,
	                timestamp, number, difficulty, gaslimit, repository, true);
	
	        /* 3. Create Program - exec.code */
	        /* 4. run VM */
	        VM vm = new VM();
	        Program program = new Program(exec.getCode(), programInvoke);
	        boolean vmDidThrowAnEception = false;
	        RuntimeException e = null;
	        try {
	        	while(!program.isStopped())
	        		vm.step(program);
	        }
	        catch (RuntimeException ex) {
	        	vmDidThrowAnEception = true;
	        	e = ex;
	        }
            program.saveProgramTraceToFile(testCase.getName());
            
            if(testCase.getPost().size() == 0) {
            	if(vmDidThrowAnEception != true) {
            		String output =
            				String.format("VM was expected to throw an exception");
            		logger.info(output);
            		results.add(output);
            	}
            	else
            		logger.info("VM did throw an exception: " + e.toString());
            }
            else {
            	if(vmDidThrowAnEception) {
            		String output =
            				String.format("VM threw an unexpected exception: " + e.toString());
            		logger.info(output);
            		results.add(output);
            		return results;
            	}
            	
            	this.trace = program.getProgramTrace();
            	
    	        System.out.println("--------- POST --------");
    	        /* 5. Assert Post values */
    	        for (ByteArrayWrapper key : testCase.getPost().keySet()) {
    	
    	            AccountState accountState = testCase.getPost().get(key);
    	
    	            long       expectedNonce     = accountState.getNonceLong();
    	            BigInteger expectedBalance   = accountState.getBigIntegerBalance();
    	            byte[]     expectedCode      = accountState.getCode();
    	
    	            boolean accountExist = (null != repository.getAccountState(key.getData()));
    	            if (!accountExist) {
    	
    	                String output =
    	                        String.format("The expected account does not exist. key: [ %s ]",
    	                                Hex.toHexString(key.getData()));
    	                logger.info(output);
    	                results.add(output);
    	                continue;
    	            }
    	
    	            long       actualNonce   = repository.getNonce(key.getData()).longValue();
    	            BigInteger actualBalance = repository.getBalance(key.getData());
    	            byte[]     actualCode    = repository.getCode(key.getData());
    	            if (actualCode == null) actualCode = "".getBytes();
    	
    	            if (expectedNonce != actualNonce) {
    	
    	                String output =
    	                        String.format("The nonce result is different. key: [ %s ],  expectedNonce: [ %d ] is actualNonce: [ %d ] ",
    	                                Hex.toHexString(key.getData()), expectedNonce, actualNonce);
    	                logger.info(output);
    	                results.add(output);
    	            }
    	
    	            if (!expectedBalance.equals(actualBalance)) {
    	
    	                String output =
    	                        String.format("The balance result is different. key: [ %s ],  expectedBalance: [ %s ] is actualBalance: [ %s ] ",
    	                                Hex.toHexString(key.getData()), expectedBalance.toString(), actualBalance.toString());
    	                logger.info(output);
    	                results.add(output);
    	            }
    	
    	            if (!Arrays.equals(expectedCode, actualCode)) {
    	
    	                String output =
    	                        String.format("The code result is different. account: [ %s ],  expectedCode: [ %s ] is actualCode: [ %s ] ",
    	                                Hex.toHexString(key.getData()),
    	                                Hex.toHexString(expectedCode),
    	                                Hex.toHexString(actualCode));
    	                logger.info(output);
    	                results.add(output);
    	            }
    	
    	            // assert storage
    	            Map<ByteArrayWrapper, ByteArrayWrapper> storage = accountState.getStorage();
    	            for (ByteArrayWrapper storageKey : storage.keySet()) {
    	
    	                byte[] expectedStValue = storage.get(storageKey).getData();
    	
    	                ContractDetails contractDetails =
    	                        program.getResult().getRepository().getContractDetails(accountState.getAddress());
    	
    	                if (contractDetails == null) {
    	
    	                    String output =
    	                            String.format("Storage raw doesn't exist: key [ %s ], expectedValue: [ %s ]",
    	                                    Hex.toHexString(storageKey.getData()),
    	                                    Hex.toHexString(expectedStValue)
    	                            );
    	                    logger.info(output);
    	                    results.add(output);
    	                    continue;
    	                }
    	
    	                Map<DataWord, DataWord>  testStorage = contractDetails.getStorage();
    	                DataWord actualValue = testStorage.get(new DataWord(storageKey.getData()));

    	                if (actualValue == null ||
                            !Arrays.equals(expectedStValue, actualValue.getNoLeadZeroesData())) {
    	
    	                    String output =
    	                            String.format("Storage value different: key [ %s ], expectedValue: [ %s ], actualValue: [ %s ]",
    	                                    Hex.toHexString(storageKey.getData()),
    	                                    Hex.toHexString(expectedStValue),
    	                                    actualValue == null ? "" :   Hex.toHexString(actualValue.getNoLeadZeroesData()));
    	                    logger.info(output);
    	                    results.add(output);
    	                }
    	            }
    	            
    	            /* asset logs */
    	            List<LogInfo> logResult = program.getResult().getLogInfoList();
    	            Iterator<byte[]> itr = logs.getLogsBloomKeyIterator();
    	            while(itr.hasNext()) {
    	            	byte[] expectedLogKey = itr.next();
    	            	System.out.println("Expected key " + Hex.toHexString(expectedLogKey));
    	            	LogInfo expectedLogInfo = logs.getLogBloom(expectedLogKey);
						LogInfo foundLogInfo = null;
    	            	boolean found = false;
    	            	for(LogInfo resultLogInfo:logResult) {
        	            	byte[] resultKey = resultLogInfo.getBloom().getData();
        	            	System.out.println("returned key " + Hex.toHexString(resultKey));
        	            	if(Arrays.equals(expectedLogKey, resultKey)) {
        	            		found = true;
								foundLogInfo = resultLogInfo;
								break;
        	            	}        	            	
        	            }
    	            	
    	            	if(!found) {
    	            		String output =
    	                            String.format("Expected log [ %s ]", expectedLogInfo.toString());
    	                    logger.info(output);
    	                    results.add(output);
    	            	}
						else {
							if(!Arrays.equals(expectedLogInfo.getAddress(), foundLogInfo.getAddress())) {
								String output =
										String.format("Expected address [ %s ], found [ %s ]", Hex.toHexString(expectedLogInfo.getAddress()), Hex.toHexString(foundLogInfo.getAddress()));
								logger.info(output);
								results.add(output);
							}

							if(!Arrays.equals(expectedLogInfo.getData(), foundLogInfo.getData())) {
								String output =
										String.format("Expected data [ %s ], found [ %s ]", Hex.toHexString(expectedLogInfo.getData()), Hex.toHexString(foundLogInfo.getData()));
								logger.info(output);
								results.add(output);
							}

							if(expectedLogInfo.getTopics().size() != foundLogInfo.getTopics().size()) {
								String output =
										String.format("Expected number of topics [ %d ], found [ %d ]", expectedLogInfo.getTopics().size(), foundLogInfo.getTopics().size());
								logger.info(output);
								results.add(output);
							}
							else {
								int i=0;
								for(DataWord topic: expectedLogInfo.getTopics()) {
									byte[] foundTopic = foundLogInfo.getTopics().get(i).getData();

									if(!Arrays.equals(topic.getData(), foundTopic)) {
										String output =
												String.format("Expected topic [ %s ], found [ %s ]", Hex.toHexString(topic.getData()), Hex.toHexString(foundTopic));
										logger.info(output);
										results.add(output);
									}

									i++;
								}
							}
						}
    	            }
    	        }
    	
    	        // TODO: assert that you have no extra accounts in the repository
    	        // TODO:  -> basically the deleted by suicide should be deleted
    	        // TODO:  -> and no unexpected created
    	
    	        List<org.ethereum.vm.CallCreate> resultCallCreates  =
    	                program.getResult().getCallCreateList();
    	
    	        // assert call creates
    	        for (int i = 0; i < testCase.getCallCreateList().size(); ++i) {
    	
    	            org.ethereum.vm.CallCreate resultCallCreate = null;
    	            if (resultCallCreates != null && resultCallCreates.size() > i) {
    	                resultCallCreate = resultCallCreates.get(i);
    	            }
    	
    	            CallCreate expectedCallCreate = testCase.getCallCreateList().get(i);
    	
    	            if (resultCallCreate == null && expectedCallCreate != null) {
    	
    	                String output =
    	                        String.format("Missing call/create invoke: to: [ %s ], data: [ %s ], gas: [ %s ], value: [ %s ]",
    	                                Hex.toHexString(expectedCallCreate.getDestination()),
    	                                Hex.toHexString(expectedCallCreate.getData()),
    	                                Hex.toHexString(expectedCallCreate.getGasLimit()),
    	                                Hex.toHexString(expectedCallCreate.getValue()));
    	                logger.info(output);
    	                results.add(output);
    	
    	                continue;
    	            }
    	
    	            boolean assertDestination = Arrays.equals(
    	            		expectedCallCreate.getDestination(),
    	                    resultCallCreate.getDestination());
    	            if (!assertDestination) {
    	
    	                String output =
    	                        String.format("Call/Create destination is different. Expected: [ %s ], result: [ %s ]",
    	                                Hex.toHexString(expectedCallCreate.getDestination()),
    	                                Hex.toHexString(resultCallCreate.getDestination()));
    	                logger.info(output);
    	                results.add(output);
    	            }
    	
    	            boolean assertData = Arrays.equals(
    	            		expectedCallCreate.getData(),
    	                    resultCallCreate.getData());
    	            if (!assertData) {
    	
    	                String output =
    	                        String.format("Call/Create data is different. Expected: [ %s ], result: [ %s ]",
    	                                Hex.toHexString(expectedCallCreate.getData()),
    	                                Hex.toHexString(resultCallCreate.getData()));
    	                logger.info(output);
    	                results.add(output);
    	            }
    	
    	            boolean assertGasLimit = Arrays.equals(
    	            		expectedCallCreate.getGasLimit(),
    	                    resultCallCreate.getGasLimit());
    	            if (!assertGasLimit) {
    	                String output =
    	                        String.format("Call/Create gasLimit is different. Expected: [ %s ], result: [ %s ]",
    	                                Hex.toHexString(expectedCallCreate.getGasLimit()),
    	                                Hex.toHexString(resultCallCreate.getGasLimit()));
    	                logger.info(output);
    	                results.add(output);
    	            }
    	
    	            boolean assertValue = Arrays.equals(
    	            		expectedCallCreate.getValue(),
    	                    resultCallCreate.getValue());
    	            if (!assertValue) {
    	                String output =
    	                        String.format("Call/Create value is different. Expected: [ %s ], result: [ %s ]",
    	                                Hex.toHexString(expectedCallCreate.getValue()),
    	                                Hex.toHexString(resultCallCreate.getValue()));
    	                logger.info(output);
    	                results.add(output);
    	            }
    	        }
    	
    	        // assert out
    	        byte[] expectedHReturn = testCase.getOut();
    	        byte[] actualHReturn = ByteUtil.EMPTY_BYTE_ARRAY;
    	        if (program.getResult().getHReturn() != null) {
    	            actualHReturn = program.getResult().getHReturn().array();
    	        }
    	
    	        if (!Arrays.equals(expectedHReturn, actualHReturn)) {
    	
    	            String output =
    	                    String.format("HReturn is different. Expected hReturn: [ %s ], actual hReturn: [ %s ]",
    	                            Hex.toHexString(expectedHReturn),
    	                            Hex.toHexString(actualHReturn));
    	            logger.info(output);
    	            results.add(output);
    	        }
    	
    	        // assert gas
    	        BigInteger expectedGas = new BigInteger(testCase.getGas());
    	        BigInteger actualGas = new BigInteger(gas).subtract(BigInteger.valueOf(program.getResult().getGasUsed()));
    	
    	        if (!expectedGas.equals(actualGas)) {
    	
    	            String output =
    	                    String.format("Gas remaining is different. Expected gas remaining: [ %s ], actual gas remaining: [ %s ]",
    	                            expectedGas.toString() ,
    	                            actualGas.toString());
    	            logger.info(output);
    	            results.add(output);
    	        }
    	        /*
    	         * end of if(testCase.getPost().size() == 0)
    	         */
            }
            
	        return results;
    	} finally {
    		repository.close();
    	}
    }

    public ProgramTrace getTrace() {
        return trace;
    }
}
