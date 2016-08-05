package org.ethereum.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anton Nashatyrev on 21.07.2016.
 */
public class ExecutorPipelineTest {

    @Test
    public void joinTest() throws InterruptedException {
        ExecutorPipeline<Integer, Integer> exec1 = new ExecutorPipeline<>(8, 100, true, new Functional.Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer integer) {
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return integer;
            }
        }, new Functional.Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                throwable.printStackTrace();
            }
        });

        final List<Integer> consumed = new ArrayList<>();

        ExecutorPipeline<Integer, Void> exec2 = exec1.add(1, 100, new Functional.Consumer<Integer>() {
            @Override
            public void accept(Integer integer) {
                consumed.add(integer);
            }
        });

        int cnt = 1000;
        for (int i = 0; i < cnt; i++) {
            exec1.push(i);
        }
        exec1.join();

        Assert.assertEquals(cnt, consumed.size());
    }
}
