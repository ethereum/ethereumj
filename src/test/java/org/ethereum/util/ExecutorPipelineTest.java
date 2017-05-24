/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
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
