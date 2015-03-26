package org.ethereum.jsontestsuite.runners;

import org.ethereum.core.Block;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionExecutor;
import org.ethereum.db.BlockStoreDummy;
import org.ethereum.facade.Repository;
import org.ethereum.jsontestsuite.Env;
import org.ethereum.jsontestsuite.StateTestCase2;
import org.ethereum.jsontestsuite.TestProgramInvokeFactory;
import org.ethereum.jsontestsuite.builder.*;
import org.ethereum.jsontestsuite.validators.LogsValidator;
import org.ethereum.jsontestsuite.validators.RepositoryValidator;
import org.ethereum.vm.LogInfo;
import org.ethereum.vm.ProgramInvokeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class StateTestRunner {

    private static Logger logger = LoggerFactory.getLogger("TCK-Test");

    public static List<String> run(StateTestCase2 stateTestCase2) {

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
        executor.execute();
        track.commit();
        repository.flush();

        logger.info("--------- POST Validation---------");
        List<LogInfo> origLogs = executor.getResult().getLogInfoList();
        List<LogInfo> postLogs = LogBuilder.build(stateTestCase2.getLogs());
        LogsValidator.valid(origLogs, postLogs);

        Repository postRepository = RepositoryBuilder.build(stateTestCase2.getPost());
        List<String> results = RepositoryValidator.valid(repository, postRepository);

        for (String result : results) {
            logger.error(result);
        }

        logger.info("\n\n");
        return results;
    }
}
