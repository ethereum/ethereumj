package org.ethereum.jsontestsuite.runners;

import org.ethereum.core.Block;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionExecutor;
import org.ethereum.db.BlockStoreDummy;
import org.ethereum.core.Repository;
import org.ethereum.jsontestsuite.Env;
import org.ethereum.jsontestsuite.StateTestCase;
import org.ethereum.jsontestsuite.TestProgramInvokeFactory;
import org.ethereum.jsontestsuite.builder.*;
import org.ethereum.jsontestsuite.validators.LogsValidator;
import org.ethereum.jsontestsuite.validators.OutputValidator;
import org.ethereum.jsontestsuite.validators.RepositoryValidator;
import org.ethereum.vm.LogInfo;
import org.ethereum.vm.program.invoke.ProgramInvokeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

public class StateTestRunner {

    private static Logger logger = LoggerFactory.getLogger("TCK-Test");

    public static List<String> run(StateTestCase stateTestCase2) {

        logger.info("");
        Repository repository = RepositoryBuilder.build(stateTestCase2.getPre());
        logger.info("loaded repository");

        Transaction transaction = TransactionBuilder.build(stateTestCase2.getTransaction());
        logger.info("transaction: {}", transaction.toString());

        BlockchainImpl blockchain = new BlockchainImpl();
        blockchain.setRepository(repository);

        Env env = EnvBuilder.build(stateTestCase2.getEnv());
        ProgramInvokeFactory invokeFactory = new TestProgramInvokeFactory(env);

        Block block = BlockBuilder.build(env);

        blockchain.setBestBlock(block);
        blockchain.setProgramInvokeFactory(invokeFactory);
        blockchain.startTracking();

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
        repository.flushNoReconnect();

        List<LogInfo> origLogs = executor.getResult().getLogInfoList();
        List<LogInfo> postLogs = LogBuilder.build(stateTestCase2.getLogs());

        List<String> logsResult = LogsValidator.valid(origLogs, postLogs);

        Repository postRepository = RepositoryBuilder.build(stateTestCase2.getPost());
        List<String> repoResults = RepositoryValidator.valid(repository, postRepository);

        logger.info("--------- POST Validation---------");
        List<String> outputResults =
                OutputValidator.valid(Hex.toHexString(executor.getResult().getHReturn()), stateTestCase2.getOut());

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
