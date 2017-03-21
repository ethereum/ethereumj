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
package org.ethereum.datasource;

import org.ethereum.datasource.mapdb.MapDBFactoryImpl;
import org.ethereum.net.rlpx.Node;
import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by Anton Nashatyrev on 22.07.2015.
 */
public class MapDBTest {

    @Test
    public void simpleTest() {
//        MapDBFactoryImpl factory = new MapDBFactoryImpl();
//        DB db = factory.createDB("anton.test", true);
//        HTreeMap<Node, String> testMap = db.hashMap("tetsMap");
//        System.out.println(testMap);
//        Node node = new Node(new byte[]{1, 2}, "localhost", 333);
//        testMap.put(node, "aaa");
////        testMap.put("key1", "value1");
////        testMap.put("key2", "value2");
//        System.out.println(testMap);
////        db.commit();
        class A {
            public A(int a) {
                this.a = a;
            }

            int a;

            @Override
            public String toString() {
                return "A[" + a + "]";
            }
        }

//        PriorityBlockingQueue<A> q = new PriorityBlockingQueue<>(10, new Comparator<A>() {
//            @Override
//            public int compare(A o1, A o2) {
//                return o2.a - o1.a;
//            }
//        });
//        BlockingQueue<A> q = new PeerConnectionTester.MutablePriorityQueue<>(new Comparator<A>() {
//            @Override
//            public int compare(A o1, A o2) {
//                return o1.a - o2.a;
//            }
//        });
//
//        A a0 = new A(0);
//        q.add(a0);
//        q.add(new A(1));
//        A a2 = new A(2);
//        q.add(a2);
//        q.add(new A(3));
//        q.add(new A(4));
//
//        System.out.println(q.poll());
//        a0.a = 100;
//        System.out.println(q.poll());
//        System.out.println(q.poll());
//        System.out.println(q.poll());
//        System.out.println(q.poll());
    }


}
