package org.ethereum.jsontestsuite.runners;

import org.ethereum.core.*;
import org.ethereum.db.BlockStoreDummy;
import org.ethereum.jsontestsuite.Env;
import org.ethereum.jsontestsuite.StateTestCase;
import org.ethereum.jsontestsuite.TestProgramInvokeFactory;
import org.ethereum.jsontestsuite.builder.*;
import org.ethereum.jsontestsuite.validators.LogsValidator;
import org.ethereum.jsontestsuite.validators.OutputValidator;
import org.ethereum.jsontestsuite.validators.RepositoryValidator;
import org.ethereum.vm.LogInfo;
import org.ethereum.vm.program.ProgramResult;
import org.ethereum.vm.program.invoke.ProgramInvokeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

public class StateTestRunner {

    private static Logger logger = LoggerFactory.getLogger("TCK-Test");

    protected StateTestCase stateTestCase;
    protected Repository repository;
    protected Transaction transaction;
    protected BlockchainImpl blockchain;
    protected Env env;
    protected ProgramInvokeFactory invokeFactory;
    protected Block block;

    public StateTestRunner(StateTestCase stateTestCase) {
        this.stateTestCase = stateTestCase;
    }

    public static List<String> run(StateTestCase stateTestCase2) {
        return new StateTestRunner(stateTestCase2).runImpl();
    }

    protected ProgramResult executeTransaction(Transaction tx) {
        Repository track = repository.startTracking();

        TransactionExecutor executor =
                new TransactionExecutor(transaction, env.getCurrentCoinbase(), track, new BlockStoreDummy(),
                        invokeFactory, blockchain.getBestBlock());

        try{
            executor.init();
            executor.execute();
            executor.go();
            executor.finalization();
        } catch (StackOverflowError soe){
            logger.error(" !!! StackOverflowError: update your java run command with -Xss32M !!!");
            System.exit(-1);
        }

        track.commit();
        return executor.getResult();
    }

    public List<String> runImpl() {

        logger.info("");
        repository = RepositoryBuilder.build(stateTestCase.getPre());
        logger.info("loaded repository");

        transaction = TransactionBuilder.build(stateTestCase.getTransaction());
        logger.info("transaction: {}", transaction.toString());

        blockchain = new BlockchainImpl();
        blockchain.setRepository(repository);

        env = EnvBuilder.build(stateTestCase.getEnv());
        invokeFactory = new TestProgramInvokeFactory(env);

        block = BlockBuilder.build(env);

        blockchain.setBestBlock(block);
        blockchain.setProgramInvokeFactory(invokeFactory);
        blockchain.startTracking();

        ProgramResult programResult = executeTransaction(transaction);

        repository.flushNoReconnect();

        List<LogInfo> origLogs = programResult.getLogInfoList();
        List<LogInfo> postLogs = LogBuilder.build(stateTestCase.getLogs());

        List<String> logsResult = LogsValidator.valid(origLogs, postLogs);

        Repository postRepository = RepositoryBuilder.build(stateTestCase.getPost());
        List<String> repoResults = RepositoryValidator.valid(repository, postRepository);

        logger.info("--------- POST Validation---------");
        List<String> outputResults =
                OutputValidator.valid(Hex.toHexString(programResult.getHReturn()), stateTestCase.getOut());

        List<String> results = new ArrayList<>();
        results.addAll(repoResults);
        results.addAll(logsResult);
        results.addAll(outputResults);

        for (String result : results) {
            logger.error(result);
        }

        logger.info("\n\n");
        return results;
    }
}
