package org.ethereum.jsontestsuite;

import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.ContractDetails;
import org.ethereum.db.RepositoryImpl;
import org.ethereum.util.ByteUtil;
import org.ethereum.vm.*;
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

    public List<String> runTestSuite(TestSuite testSuite){

        Iterator<TestCase> testIterator = testSuite.iterator();
        List<String> resultCollector = new ArrayList<>();

        while (testIterator.hasNext()){

            TestCase testCase = testIterator.next();

            logger.info("Running: [ {} ]", testCase.getName());
            TestRunner runner = new TestRunner();
            List<String> result = runner.runTestCase(testCase);
            resultCollector.addAll(result);
        }

        return resultCollector;
    }


    public List<String> runTestCase(TestCase testCase){

        List<String> results = new ArrayList<>();

        RepositoryImpl repository = new RepositoryImpl();

        /* 1. Store pre-exist accounts - Pre */
        for (ByteArrayWrapper key : testCase.getPre().keySet()){

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

        byte[] address     = exec.getAddress();
        byte[] origin      = exec.getOrigin();
        byte[] caller      = exec.getCaller();
        byte[] balance     =  ByteUtil.bigIntegerToBytes(repository.getBalance(exec.getAddress()));
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

        ProgramInvoke programInvoke = new ProgramInvokeImpl(address, origin, caller, balance,
                gasPrice, gas, callValue, msgData, lastHash, coinbase,
                timestamp, number, difficulty, gaslimit, repository, true);

        /* 3. Create Program - exec.code */
        /* 4. run VM */
        VM vm = new VM();
        Program program = new Program(exec.getCode(), programInvoke);

        try {
            while(!program.isStopped())
                vm.step(program);

            System.out.println();
        } catch (RuntimeException e) {
            program.setRuntimeFailure(e);
        }

        /* 5. Assert Post values */
        for (ByteArrayWrapper key : testCase.getPost().keySet()){

            AccountState accountState = testCase.getPost().get(key);

            long       expectedNonce     = accountState.getNonceLong();
            BigInteger expectedBalance   = accountState.getBigIntegerBalance();
            byte[]     expectedCode      = accountState.getCode();

            boolean accountExist = (null != repository.getAccountState(key.getData()));
            if (!accountExist){

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

            if (expectedNonce != actualNonce){

                String output =
                        String.format("The nonce result is different. key: [ %s ],  expectedNonce: [ %d ] is actualNonce: [ %d ] ",
                                Hex.toHexString(key.getData()), expectedNonce, actualNonce);
                logger.info(output);
                results.add(output);
            }

            if (!expectedBalance.equals(actualBalance)){

                String output =
                        String.format("The balance result is different. key: [ %s ],  expectedBalance: [ %s ] is actualBalance: [ %s ] ",
                                Hex.toHexString(key.getData()), expectedBalance.toString(), actualBalance.toString());
                logger.info(output);
                results.add(output);
            }

            if (!Arrays.equals(expectedCode, actualCode)){

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
            for (ByteArrayWrapper storageKey  :  storage.keySet()  ){

                byte[] expectedStValue = storage.get(storageKey).getData();

                ContractDetails contractDetails =
                        program.getResult().getRepository().getContractDetails(accountState.getAddress());

                if (contractDetails == null){

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

                if (!Arrays.equals(expectedStValue, actualValue.getNoLeadZeroesData())){

                    String output =
                            String.format("Storage value different: key [ %s ], expectedValue: [ %s ], actualValue: [ %s ]",
                                    Hex.toHexString(storageKey.getData()),
                                    Hex.toHexString(actualValue.getData()),
                                    Hex.toHexString(expectedStValue)
                                    );
                    logger.info(output);
                    results.add(output);
                }
            }
        }

        // TODO: assert that you have no extra accounts in the repository
        // TODO:  -> basically the deleted by suicide should be deleted
        // TODO:  -> and no unexpected created

        List<org.ethereum.vm.CallCreate> resultCallCreates  =
                program.getResult().getCallCreateList();

        // assert call creates
        for (int i = 0; i < testCase.getCallCreateList().size(); ++i){

            org.ethereum.vm.CallCreate resultCallCreate = null;
            if (resultCallCreates != null && resultCallCreates.size() > i){
                resultCallCreate = resultCallCreates.get(i);
            }

            CallCreate expectedCallCreate =
                    testCase.getCallCreateList().get(i);

            if (resultCallCreate == null && expectedCallCreate != null){

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

            boolean assertDestination = Arrays.equals(expectedCallCreate.getDestination(),
                    resultCallCreate.getDestination());
            if (!assertDestination){

                String output =
                        String.format("Call/Create destination is different expected: [ %s ], result: [ %s ]",
                                Hex.toHexString(expectedCallCreate.getDestination()),
                                Hex.toHexString(resultCallCreate.getDestination()));
                logger.info(output);
                results.add(output);
            }

            boolean assertData = Arrays.equals(expectedCallCreate.getData(),
                    resultCallCreate.getData());
            if (!assertData){

                String output =
                        String.format("Call/Create data is different expected: [ %s ], result: [ %s ]",
                                Hex.toHexString(  expectedCallCreate.getData() ),
                                Hex.toHexString(resultCallCreate.getData()) );
                logger.info(output);
                results.add(output);
            }

            boolean assertGasLimit = Arrays.equals(expectedCallCreate.getGasLimit(),
                    resultCallCreate.getGasLimit());
            if (!assertGasLimit){

                String output =
                        String.format("Call/Create gasLimit is different expected: [ %s ], result: [ %s ]",
                                Hex.toHexString( expectedCallCreate.getGasLimit() ),
                                Hex.toHexString( resultCallCreate.getGasLimit()) );
                logger.info(output);
                results.add(output);
            }

            boolean assertValue = Arrays.equals(expectedCallCreate.getValue(),
                    resultCallCreate.getValue());
            if (!assertValue){

                String output =
                        String.format("Call/Create value is different expected: [ %s ], result: [ %s ]",
                                Hex.toHexString( expectedCallCreate.getValue() ),
                                Hex.toHexString( resultCallCreate.getValue() ));
                logger.info(output);
                results.add(output);
            }
        }

        // assert out
        byte[] expectedHReturn = testCase.getOut();
        byte[] actualHReturn = new byte[0];
        if (program.getResult().getHReturn() != null){
            actualHReturn = program.getResult().getHReturn().array();
        }

        if (!Arrays.equals(expectedHReturn, actualHReturn)){

            String output =
                    String.format("HReturn is differnt expected hReturn: [ %s ], actual hReturn: [ %s ]",
                            Hex.toHexString( expectedHReturn ),
                            Hex.toHexString( actualHReturn ));
            logger.info(output);
            results.add(output);
        }

        // assert gas
        BigInteger expectedGas = new BigInteger(testCase.getGas());
        BigInteger actualGas = new BigInteger(gas).subtract(BigInteger.valueOf(program.getResult().getGasUsed()));

        if (!expectedGas.equals(actualGas)){

            String output =
                    String.format("Gas usage is differnt expected gas usage: [ %s ], actual gas usage: [ %s ]",
                            expectedGas.toString() ,
                            actualGas.toString());
            logger.info(output);
            results.add(output);
        }
        program.getResult().getRepository().close();

        return results;
    }
}
