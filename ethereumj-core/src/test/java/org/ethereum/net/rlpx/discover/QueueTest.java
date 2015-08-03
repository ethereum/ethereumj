package org.ethereum.net.rlpx.discover;

import org.ethereum.net.rlpx.discover.PeerConnectionTester;
import org.junit.Test;

import java.util.Comparator;

/**
 * Created by Anton Nashatyrev on 03.08.2015.
 */
public class QueueTest {

    @Test
    public void simple() throws Exception {
        final PeerConnectionTester.MutablePriorityQueue<String, String> queue =
                new PeerConnectionTester.MutablePriorityQueue<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        final int threadCnt = 8;
        final int elemCnt = 100000;

        Runnable adder = new Runnable() {
            @Override
            public void run() {
                System.out.println("Adding...");
                for (int i = 0; i < elemCnt; i++) {
                    queue.add("aaa");
                }
                System.out.println("Done.");
            }
        };

        ThreadGroup tg = new ThreadGroup("test");

        Thread t1[] = new Thread[threadCnt];

        for (int i = 0; i < t1.length; i++) {
            t1[i] = new Thread(tg, adder);
            t1[i].start();
        }

        Runnable taker = new Runnable() {
            @Override
            public void run() {
                System.out.println("Taking...");
                for (int i = 0; i < elemCnt; i++) {
                    try {
                        queue.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("OK: " + queue.size());
            }
        };
        Thread t2[] = new Thread[threadCnt];

        for (int i = 0; i < t2.length; i++) {
            t2[i] = new Thread(tg, taker);
            t2[i].start();
        }

        for (Thread thread : t1) {
            thread.join();
        }
        for (Thread thread : t1) {
            thread.join();
        }
    }
}
