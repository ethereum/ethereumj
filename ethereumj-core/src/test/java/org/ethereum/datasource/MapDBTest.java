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
