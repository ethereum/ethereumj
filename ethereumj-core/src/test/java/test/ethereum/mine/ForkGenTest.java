package test.ethereum.mine;


import org.ethereum.core.Block;
import org.ethereum.mine.Miner;
import org.junit.Ignore;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

/**
 * @author Roman Mandeleil
 * @since 09.11.2014
 */
public class ForkGenTest {


    @Test
    @Ignore
    public void mineOnBlock() {

        byte[] blockRaw = Hex.decode("f90139f90134a077702830ce2f66cdbf82cabd600ddb760a68b73c93739bdd439309989b22f93ba01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d4934794907a0f5f767664ac77c8e431b99e74abc9288a40a0a2deb803ea8704997ae17efd0adf038df2833505da8776f095e32174dcb8e4aba056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b840000000000000000000000000000000000000000000002000000000004000000000000020000000000000000000000000000000000000000000000000000000008301f2363c8609184e72a000830e63bb80845461623980a05643fd40385c6520e3320109adc71917fdbbcdffe61f0b476ccb3b34111af194c0c0");
        byte[] coinbase = Hex.decode("cd2a3d9f938e13cd947ec05abc7fe734df8dd826");

        Block block = new Block(blockRaw);
        Block newBlock = MinerThread.createBlock(block, coinbase);
        newBlock.setStateRoot(Hex.decode("43bb67bea1931eca8f9e06f9cca66a9f9914cc3e3d4e9ceb2e08e58ab9f92bab"));

        Miner miner = new Miner();
        miner.mine(newBlock, newBlock.getDifficulty());

        System.out.println(newBlock);

        //f8f9f8f5a0a02852f3f5e7d06936bd5f39e7cc65a9f11e37656255f92c7eb32cb878a70213a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d4934794cd2a3d9f938e13cd947ec05abc7fe734df8dd826a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421808301ef4e518609184e72a000830e1c69808601499c9bf5dd80a0000000000000000000000000000000000000000000000000000000000000f599c0c0

    }


    @Test
    @Ignore
    public void makeFork() {


        MineSwarm swarm = new MineSwarm();
        swarm.start();


        while (swarm.started.get()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }
}
