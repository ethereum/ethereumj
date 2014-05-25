package org.ethereum.net.submit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 23/05/2014 19:07
 */

public class TransactionExecutor {

    static {instance = new TransactionExecutor();}
    public static TransactionExecutor instance;

    ExecutorService executor = Executors.newFixedThreadPool(1);

    public Future submitTransaction(TransactionTask task){
        return executor.submit(task);
    }

}
