package org.ethereum.core;

import org.ethereum.config.CommonConfig;
import org.ethereum.vm.LogInfo;
import org.ethereum.vm.program.ProgramResult;

import java.util.List;

/**
 * Executes transaction using Ethereum VM
 * Usual usage looks like this:
 *  1. init()
 *  2. execute()
 *  3. go()
 *  4. finalization() <- get Summary here
 *  After that all results of transaction execution
 *  could be obtained using all other methods
 */
public interface TransactionExecutor {
    TransactionExecutor withCommonConfig(CommonConfig commonConfig);

    /**
     * Do all the basic validation, if the executor
     * will be ready to run the transaction at the end
     * set readyToExecute = true
     */
    void init();

    /**
     * Opening steps of transaction
     * If transaction should create contract it creates,
     * if it's calling existing it's loaded into VM
     */
    void execute();

    /**
     * Main execution of transaction
     * If contract is involved program execution is done on this step
     */
    void go();

    /**
     * Called after execution is finished
     * Pays rewards, do state modifications required by specs etc.
     * Combines results from execution to Summary
     */
    TransactionExecutionSummary finalization();

    /**
     * Local execution does not spend gas on VM work
     * Usually executor with localCall turned on is
     * used for calling of constant methods
     */
    TransactionExecutor setLocalCall(boolean localCall);

    /**
     * @return {@link TransactionReceipt} filled with data from execution result
     */
    TransactionReceipt getReceipt();

    /**
     * @return list of {@link LogInfo}'s submitted during VM execution
     */
    List<LogInfo> getVMLogs();

    /**
     * Result of program execution
     */
    ProgramResult getResult();

    /**
     * @return amount of gas used for tx execution
     */
    long getGasUsed();
}
