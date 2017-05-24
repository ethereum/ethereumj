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


public interface Functional {

    /**
     * Represents an operation that accepts a single input argument and returns no
     * result. Unlike most other functional interfaces, {@code Consumer} is expected
     * to operate via side-effects.
     *
     * @param <T> the type of the input to the operation
     */
    interface Consumer<T> {

        /**
         * Performs this operation on the given argument.
         *
         * @param t the input argument
         */
        void accept(T t);
    }

    /**
     * Represents an operation that accepts two input arguments and returns no
     * result.  This is the two-arity specialization of {@link java.util.function.Consumer}.
     * Unlike most other functional interfaces, {@code BiConsumer} is expected
     * to operate via side-effects.
     *
     * @param <T> the type of the first argument to the operation
     * @param <U> the type of the second argument to the operation
     *
     * @see org.ethereum.util.Functional.Consumer
     */
    interface BiConsumer<T, U> {

        /**
         * Performs this operation on the given arguments.
         *
         * @param t the first input argument
         * @param u the second input argument
         */
        void accept(T t, U u);
    }


    /**
     * Represents a function that accepts one argument and produces a result.
     *
     * @param <T> the type of the input to the function
     * @param <R> the type of the result of the function
     */
    interface Function<T, R> {

        /**
         * Applies this function to the given argument.
         *
         * @param t the function argument
         * @return the function result
         */
        R apply(T t);
    }

    interface Supplier<T> {

        /**
         * Gets a result.
         *
         * @return a result
         */
        T get();
    }

    interface InvokeWrapper {
        
        void invoke();
    }

    interface InvokeWrapperWithResult<R> {

        R invoke();
    }

    interface Predicate<T> {
        boolean test(T t);
    }

}
