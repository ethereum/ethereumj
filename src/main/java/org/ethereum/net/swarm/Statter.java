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
