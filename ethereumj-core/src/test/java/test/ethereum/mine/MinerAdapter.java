package test.ethereum.mine;

import org.ethereum.core.Block;
import org.ethereum.mine.Miner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapts new Miner to older API the tests are written against.
 *
 * Miner would be a clean local variable, no sychronization or one-at-a-time semantics,
 * but for supporting the method stop(). alas.
 */
public class MinerAdapter {

    final static int CONCURRENCY = Runtime.getRuntime().availableProcessors();

    final static Logger logger = LoggerFactory.getLogger("miner");

    //MT: protected by this' lock
    Miner miner = null;

    private synchronized Miner getMiner()
    { return miner; }

    public synchronized void stop() {
	miner.cancel();
    }

    public boolean mine( Block newBlock, byte[] difficulty ) {
	
	Miner m;
	synchronized ( this )
	{
	    if ( miner != null ) throw new IllegalStateException("Only one block can be mined at a time by a MinerAdapter.");

	    newBlock.setDifficulty( difficulty );
	    m = new Miner( newBlock, CONCURRENCY );
	    miner = m;
	}
	try 
	{
	    Miner.State state = m.await();
	    if ( state == Miner.State.COMPLETED )
	    {
		Block good = m.getMined();
		
		//this API uses newBlock as an out param, so...
		newBlock.overwrite( good.getEncoded() );
		return true;
	    }
	    else
	    {
		logger.warn("Mining failed, miner state: " + state);
		return false;
	    }
	}
	catch (InterruptedException e)
	{
	    logger.warn("Mining failed with InterruptedException on waiting Thread, miner state: " + m.getState());
	    return false;
	}
	finally
	{ this.miner = null; }
    }
}
