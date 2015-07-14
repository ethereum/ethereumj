package org.ethereum.net.swarm;

/**
 * The interface for gathering statistical information.
 * Used similar to loggers and assumed to have minimal latency to
 * not affect the performance in production.
 * The implementation might be substituted to allow some advanced
 * information processing like streaming it to the database
 * or aggregating and displaying as graphics
 *
 * Created by Anton Nashatyrev on 01.07.2015.
 */
public abstract class Statter {

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

    /**
     * Used as a factory to create statters.
     * @param name Normally the name is assumed to be a hierarchical path with '.' delimiters
     *             similar to full Java class names.
     */
    public static Statter create(String name) {
        return new SimpleStatter(name);
    }


    public abstract void add(double value);
}
