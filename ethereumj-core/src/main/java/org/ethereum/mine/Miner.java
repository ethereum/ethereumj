package org.ethereum.mine;

import org.ethereum.core.Block;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Set;
import java.util.HashSet;

public class Miner {

    public static enum State {
	UNINITIALIZED, // this miner hasn't been started
	MINING,        // this miner is in process of mining
	CANCELED,      // this miner has been canceled
	ERROR,         // this miner has aborted with an error ( available via getError() )
	EXHAUSTED,     // this miner has explored the full nonce space and not found a valid nonce
	COMPLETED      // this miner has successfully found a good block ( available via getMined() )
    }


    private static final Logger logger = LoggerFactory.getLogger("miner");

    private static final SecureRandom entropy = new java.security.SecureRandom();

    private static final int NONCE_LEN = 8;
    private static final int NONCE_LSB = NONCE_LEN - 1;

    private static final BigInteger NONCE_SPACE = BigInteger.ONE.shiftLeft( 8 * NONCE_LEN ); //1 << 64 == 2 ** 64

    private static synchronized byte[] randomNonce() {
	byte[] out = new byte[ NONCE_LEN ];
	entropy.nextBytes( out );
	return out;
    }

    final Block original;

    // MT: protected by this' lock
    State       state  = State.UNINITIALIZED;
    Block       mined  = null;
    Throwable   error  = null;
    Set<Thread> active = new HashSet<Thread>();

    /**
     * @param original is presumed to be a valid child of its parentHash,
     *        a valid new block across every dimensions except proof-of-work,
     *        which encompasses only mixHash and nonce. When mining terminates
     *        successfully ( state == COMPLETED ), mixHash and nonce will have 
     *        been updated to valid values for the mined block;
     *
     * @param concurrency how many Threads should concurrently mine.
     */
    public Miner( Block original, int concurrency ) {
	this.original = original;
	BigInteger nonceIncrement = NONCE_SPACE.divide( BigInteger.valueOf(concurrency) );

	BigInteger nonceBI = new BigInteger( 1, randomNonce() );
	BigInteger startNonce = nonceBI;
	for ( int i = 0; i < concurrency; ++i ) {
	    boolean last = (i == concurrency - 1);
	    BigInteger stopNonce = ( last ? nonceBI : startNonce.add( nonceIncrement ) );

	    Thread t = new WorkThread(original, startNonce.toByteArray(), stopNonce.toByteArray() );
	    active.add( t );
	    t.start();

	    startNonce = stopNonce;
	}

	this.start();
    }

    public synchronized State getState() {
	return state;
    }

    /**
     * @return the terminating Throwable, iff this miner has failed with an error
     *         (i.e. iff state == ERROR ), null otherwise
     */
    public synchronized Throwable getError() {
	return error;
    }

    /**
     * @return the correctly mined block, iff this miner has succeeded
     *         (i.e. iff state == COMPLETED ), null otherwise
     */
    public synchronized Block getMined() {
	return mined;
    }

    /**
     * Immediately cancels the miner.
     */
    public synchronized void cancel() {
	this.state = State.CANCELED;
	this.notifyAll();
    }

    public synchronized boolean isTerminated() {
	return 
	    state == State.CANCELED  ||
	    state == State.ERROR     ||
	    state == State.EXHAUSTED ||
	    state == State.COMPLETED;
    }

    /**
     * @return wait for this miner to terminate, that is enter into 
     *         one of the states CANCELED, ERROR, EXHAUSTED, COMPLETED
     */
    public synchronized State await() throws InterruptedException {
	while ( state == State.UNINITIALIZED || state == State.MINING ) this.wait();
	return state;
    }

    // let's autostart in the constructor
    private synchronized void start() {
	if ( this.state == State.UNINITIALIZED ) {
	    this.state = State.MINING;
	    this.notifyAll();
	} else {
	    throw new IllegalStateException("You can only start an uninitialized miner.");
	}
    }

    // grrr... 
    // we have to fully qualify references to Miner.State or getState(), otherwise the compiler goes to Thread.State
    private class WorkThread extends Thread {
	
	Block target;
	byte[] nonceBytes;
	byte[] stopNonce;

	WorkThread( Block original, byte[] startNonce /* inclusive */, byte[] stopNonce /* exclusive */) {
	    this.target = original.deepCopy();
	    this.nonceBytes = startNonce;
	    this.stopNonce = stopNonce;

	    this.setPriority( Thread.NORM_PRIORITY - 1 ); // hint to let other Threads have a go while we mine
	}
	
	public void run() {
	    try {
		synchronized ( Miner.this ) {
		    while ( state == Miner.State.UNINITIALIZED ) Miner.this.wait();
		}
		while ( Miner.this.getState() == Miner.State.MINING && !Arrays.equals( nonceBytes, stopNonce ) ) {
		    target.setNonce( nonceBytes );
		    if ( target.updateMixHashForGoodNonce() ) { // yay! we've mined the block
			synchronized( Miner.this ) {
			    if ( Miner.this.state == Miner.State.MINING ) {
				Miner.this.state = Miner.State.COMPLETED;
				Miner.this.mined = target;
				Miner.this.notifyAll();
			    }
			}
		    } else if ( this.isInterrupted() ) { 
			handleInterrupt();
		    } else {
			incrementNonce();
		    }
		}
	    } catch ( InterruptedException e ) { // may be thrown in wait() prior to the loop
		handleInterrupt();
	    } catch ( Throwable t ) {
		synchronized( Miner.this ) {
		    if ( Miner.this.state == Miner.State.MINING ) {
			Miner.this.state = Miner.State.ERROR;
			Miner.this.error = t;
			Miner.this.notifyAll();
		    } else {
			logger.debug( "An error occurred in a mining thread that was already about to terminate.", t );
		    }
		}
	    } finally {
		synchronized( Miner.this ) {
		    Miner.this.active.remove( this );
		    if ( Miner.this.active.isEmpty() && Miner.this.state == Miner.State.MINING ) {
			Miner.this.state = Miner.State.EXHAUSTED;
			Miner.this.notifyAll();
		    }
		}
	    }
	}

	private void handleInterrupt() {
	    synchronized ( Miner.this ) {
		if ( Miner.this.state == Miner.State.MINING ) {
		    logger.warn( "Mining worker thread " + this + " has been interrupt()ed. We interpret this as cancellation of the mining operation." );
		    Miner.this.state = Miner.State.CANCELED;
		    Miner.this.notifyAll();
		}
	    }
	}

	// this is the same as ByteUtils.increment(...) but it wraps around rather than terminating at all zero vals
	private void incrementNonce() {
	    int target = NONCE_LSB;
	    boolean done = false;
	    while (! done ) {
		byte b = (byte) (nonceBytes[target] + 1);
		nonceBytes[target] = b;
		if ( b == 0 ) {
		    target = ( target == 0 ? NONCE_LSB : target - 1 );
		} else {
		    done = true;
		}
	    }
	}
    }
}
