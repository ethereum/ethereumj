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
