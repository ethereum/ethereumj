package org.ethereum.util;


public interface Functional {

    /**
     * Represents an operation that accepts a single input argument and returns no
     * result. Unlike most other functional interfaces, {@code Consumer} is expected
     * to operate via side-effects.
     *
     * @param <T> the type of the input to the operation
     */
    public static interface Consumer<T> {

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
    public interface BiConsumer<T, U> {

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
    public static interface Function<T, R> {

        /**
         * Applies this function to the given argument.
         *
         * @param t the function argument
         * @return the function result
         */
        R apply(T t);
    }
    
    public static interface InvokeWrapper {
        
        void invoke();
    }

    public static interface InvokeWrapperWithResult<R> {

        R invoke();
    }

    public static interface Predicate<T> {
        boolean test(T t);
    }

}
