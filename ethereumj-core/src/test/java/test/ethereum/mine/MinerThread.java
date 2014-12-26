package test.ethereum.mine;

import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.Chain;
import org.ethereum.core.Genesis;
import org.ethereum.mine.Miner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Roman Mandeleil 
 * Created on: 22/05/2014 09:26
 */
public class MinerThread implements Runnable {


    private final static Logger logger = LoggerFactory.getLogger("miner");
    private final byte[] coinbase;

    private MineSwarm   mineSwarm;
    private String      name;
    private boolean     done = false;
    private Miner miner = new Miner();

    private Chain mainChain  = new Chain();
    private List<Chain> altChains  = new ArrayList<>();
    private Block       tmpBlock   = null;
    private List<Block> uncles     = new ArrayList<>();

    private Block announcedBlock = null;

    public MinerThread(String name,  MineSwarm mineSwarm, byte[] coinbase) {
        this.name = name;
        this.mineSwarm = mineSwarm;
        this.coinbase = coinbase;

        Block genesis = Genesis.getInstance();
        mainChain.add(genesis);

    }

    @Override
    public void run() {
        logger.debug("{} start", name);
        doRun();
        logger.debug("{} end", name);
    }

    public void onNewBlock(Block foundBlock){

        if (mainChain.getLast().isEqual(foundBlock)){
            // That is our announcement, do nothing.
            return;
        }

        logger.info("{}: Ohh.. I heard the block was found already: {}: {} ^ {}", name,
                foundBlock.getNumber(), Hex.toHexString(foundBlock.getHash()).substring(0, 6),
                Hex.toHexString(foundBlock.getParentHash()).substring(0, 6));


        if ( mainChain.getLast().isParentOf(foundBlock) ){
            logger.info("{}: adding by announce to main chain. hash:{} ", name,
                    Hex.toHexString(foundBlock.getHash()).substring(0, 6));
            // add it as main block
            announcedBlock = foundBlock;
            miner.stop();
        } else{

            if (mainChain.isParentOnTheChain(foundBlock)){

                logger.info("{} found an uncle. on index: {}", name, foundBlock.getNumber());
                // add it as a future uncle
                uncles.add(foundBlock);
            } else{
                logger.info("{}: nothing to do, maybe alt chain: {}: {} ^ {}", name,
                        foundBlock.getNumber(), Hex.toHexString(foundBlock.getHash()).substring(0, 6),
                        Hex.toHexString(foundBlock.getParentHash()).substring(0, 6));
            }
        }
    }

    public void announceBlock(Block block){
        mineSwarm.announceBlock(block);
    }

    private void doRun() {

        Block genesis = mainChain.getLast();
        tmpBlock = createBlock(genesis, coinbase);
        while (!done){

            this.announcedBlock = null;
            logger.info("{}: before mining: chain.size: [{}], chain.TD: [{}]", name,
                    mainChain.getSize(), mainChain.getTotalDifficulty());
            boolean found = miner.mine(tmpBlock, tmpBlock.getDifficulty());
            logger.info("{}: finished mining, found: [{}]", name, found);

            if (!found && announcedBlock != null){
                mainChain.add(announcedBlock);
                tmpBlock = createBlock(announcedBlock, coinbase);
            }

            if (found) {
                mineSwarm.addToQueue(tmpBlock);
                logger.info("{}: mined block: {} --> {} ^ {}", name, tmpBlock.getNumber(),
                        Hex.toHexString(tmpBlock.getHash()).substring(0, 6),
                        Hex.toHexString(tmpBlock.getParentHash()).substring(0, 6));
                if (announcedBlock != null)
                    logger.info("{}: forked on: {}",name, tmpBlock.getNumber());

                logger.info("{}: adding to main chain. hash:{} ", name,
                        Hex.toHexString(tmpBlock.getHash()).substring(0, 6));
                mainChain.add(tmpBlock);
                sleep();
                announceBlock(tmpBlock);
                tmpBlock = createBlock(tmpBlock, coinbase);
            }

            if (!uncles.isEmpty()){
                for (Block uncle : uncles){
                    BlockHeader uncleHeader = uncle.getHeader();
                    tmpBlock.addUncle(uncleHeader);
                    logger.info("{} adding {} uncles to block: {}", name, uncles.size(), tmpBlock.getNumber());
                }
                uncles.clear();
            }

            if (mainChain.getSize() == 100) done = true;
        }

    }


    public static Block createBlock(Block lastBlock, byte[] coinbase) {

       long timeDiff = System.currentTimeMillis() - lastBlock.getTimestamp();

       byte[] difficulty = lastBlock.getDifficulty();
//       if (timeDiff < 5000){
//           System.out.println("increase");
//           BigInteger diff = (new BigInteger(1, lastBlock.getDifficulty()).add(new BigInteger("FFF", 16)));
//           difficulty = diff.toByteArray();
//       }

        Block newBlock = new Block(lastBlock.getHash(), lastBlock.getUnclesHash(), coinbase, lastBlock.getLogBloom(),
                difficulty , lastBlock.getNumber() + 1,
                lastBlock.getGasLimit(), lastBlock.getGasUsed(), System.currentTimeMillis() / 1000,
                null, null,  null, null);

        return newBlock;
    }

    public boolean isDone() {
        return done;
    }

    public Chain getChain() {
        return mainChain;
    }

    private void sleep(){
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
