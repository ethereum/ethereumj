package org.ethereum.net.swarm;

import com.google.common.util.concurrent.AtomicDouble;

import java.util.Map;

/**
 * Created by Admin on 01.07.2015.
 */
public abstract class Statter {
//    public static class SimpleStatter extends Statter {
//        public static Map<String, >
//        @Override
//        public void add(double value) {
//
//        }
//    }

    public static class SimpleStatter extends Statter {

        private final String name;
        private volatile double last;
        private volatile double sum;
        private volatile int count;

        public SimpleStatter(String name) {
            this.name = name;
        }

        @Override
        public void add(double value) {
            last = value;
            sum += value;
            count++;
        }

        public double getLast() {
            return last;
        }

        public int getCount() {
            return count;
        }

        public double getSum() {
            return sum;
        }

        public double getAvrg() {
            return getSum() / getCount();
        }

        public String getName() {
            return name;
        }

    }

    public static Statter create(String name) {
        return new SimpleStatter(name);
    }


    public abstract void add(double value);
}
