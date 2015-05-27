package org.ethereum.pow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.sc.v1.consuela.ethereum.pow.ethash23.Implementation;
import com.mchange.sc.v1.consuela.ethereum.pow.ethash23.Manager;
import com.mchange.sc.v1.consuela.ethereum.pow.ethash23.JavaHelpers;
import com.mchange.sc.v1.consuela.ethereum.pow.ethash23.JavaMonitorFactory;

/**
 * Utilities to perform proof-of-work according to the
 * Ethash specfication, version 23.
 *
 * See https://github.com/ethereum/wiki/wiki/Ethash
 */
public final class Ethash23 {

    private static final Logger logger = LoggerFactory.getLogger("ethash23");

    private static final double IN_MEM_COMPUTE_MULTIPLIER = 2.5d;

    private static final JavaMonitorFactory MONITOR_FACTORY = new JavaMonitorFactory() {
	public Implementation.Monitor create() {
	    return new LoggingMonitor();
	}
    };

    private static boolean shouldInMemCompute( long blockNumber ) {
	long mem = Runtime.getRuntime().totalMemory();
	long fullSize = JavaHelpers.getFullSizeForBlock( blockNumber );
	long needed = Math.round( fullSize * IN_MEM_COMPUTE_MULTIPLIER );
	boolean out = mem > needed;
 	logger.info(  "DAG size: " + fullSize + ", JVM memory: " + mem + ", In-memory compute? " + out + " (only if mem is at least " + IN_MEM_COMPUTE_MULTIPLIER + " times size)" );
	return out;
    }

    private static class LoggingMonitor implements Implementation.Monitor {
	String runID = Integer.toString( System.identityHashCode(this), 16 );

	long total;
	long percent;
	long count;

	public synchronized void start( long rowCount ) {
	    this.total = rowCount;
	    this.percent = total / 100;
	    this.count = 0;
	    logger.info( "Beginning dataset computation, " + total + " rows, runID = " + runID );
	}
	public synchronized void rowHandled( long rowIndex ) {
	    ++count;
	    if ( count % percent == 0 ) logger.info( "Computed " + count / percent +"% of " + total + " rows, runID = " + runID );
	}
	public synchronized void completed() {
	    logger.info( "Beginning dataset computation, " + count + " rows, runID = " + runID );
	}
    }

    public static boolean cacheDagForBlockNumber( long blockNumber ) {
	if ( shouldInMemCompute( blockNumber ) ) {
	    return JavaHelpers.precomputeCacheDatasetForBlockNumber( blockNumber, MONITOR_FACTORY );
	} else {
	    return JavaHelpers.streamDagFileForBlockNumber( blockNumber, MONITOR_FACTORY );
	}
    }

    private Ethash23() {}
}
