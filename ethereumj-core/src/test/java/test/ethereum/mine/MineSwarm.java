package test.ethereum.mine;

import org.ethereum.core.Block;
import org.ethereum.net.peerdiscovery.RejectionLogger;

import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Roman Mandeleil
 * @since 08.11.2014
 */
public class MineSwarm {

    public AtomicBoolean started = new AtomicBoolean(false);

    private ThreadFactory threadFactory;
    private ThreadPoolExecutor executorPool;
    private RejectedExecutionHandler rejectionHandler;

    private AtomicBoolean found = new AtomicBoolean(false);
    List<MinerThread> workers = new ArrayList<>();

    private Queue<Block> blockAppearQueue = new ConcurrentLinkedQueue<>();

    public void start() {

        started.set(true);
        // RejectedExecutionHandler implementation
        rejectionHandler = new RejectionLogger();

        // Get the ThreadFactory implementation to use
        threadFactory = Executors.defaultThreadFactory();

        MinerThread mt1 = new MinerThread("miner1", this, Hex.decode("2bd26d8f796719923ff13d295644f9b45db1f730"));
        Thread miner1 = new Thread(mt1);
        miner1.start();
        workers.add(mt1);

        MinerThread mt2 = new MinerThread("miner2", this, Hex.decode("f92c0f3e4825f09490ca264dc0cdacffeb566f06"));
        Thread miner2 = new Thread(mt2);
        miner2.start();
        workers.add(mt2);

        MinerThread mt3 = new MinerThread("miner3", this, Hex.decode("407d73d8a49eeb85d32cf465507dd71d507100c1"));
        Thread miner3 = new Thread(mt3);
        miner3.start();
        workers.add(mt3);

        while (!mt1.isDone() || !mt2.isDone() || !mt3.isDone()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        while (!blockAppearQueue.isEmpty()) {
            System.out.println(
                    Hex.toHexString(blockAppearQueue.poll().getEncoded())
            );
        }

        System.out.println("miner-1: TD: " + mt1.getChain().getTotalDifficulty() +
                " chain.size: " + mt1.getChain().getSize());

        System.out.println("miner-2: TD: " + mt2.getChain().getTotalDifficulty() +
                " chain.size: " + mt2.getChain().getSize());

        System.out.println("miner-3: TD: " + mt3.getChain().getTotalDifficulty() +
                " chain.size: " + mt3.getChain().getSize());

    }


    public void announceBlock(Block block) {
        for (MinerThread mt : workers) {
            mt.onNewBlock(block);
        }
    }

    public void addToQueue(Block block) {
        synchronized (blockAppearQueue) {
            blockAppearQueue.add(block);
        }
    }
}
