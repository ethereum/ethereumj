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

import com.google.common.base.Function;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomGenerator<T> {

    private final Random random;
    private final List<Function<Random, T>> genFunctions = new ArrayList<>();


    public RandomGenerator(Random random) {
        this.random = random;
    }

    public RandomGenerator<T> addGenFunction(Function<Random, T> function) {
        genFunctions.add(function);
        return this;
    }

    public <E> E randomFrom(List<E> list) {
        return list.get(random.nextInt(list.size()));
    }

    public <E> E randomFrom(E[] array) {
        return array[random.nextInt(array.length)];
    }

    public T genNext() {
        if (genFunctions.isEmpty()) {
            throw new IllegalStateException();
        }

        return randomFrom(genFunctions).apply(random);
    }
}
