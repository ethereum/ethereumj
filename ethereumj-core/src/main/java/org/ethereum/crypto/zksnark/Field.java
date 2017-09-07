package org.ethereum.crypto.zksnark;

/**
 * Interface of abstract finite field
 *
 * @author Mikhail Kalinin
 * @since 05.09.2017
 */
interface Field<T> {

    T add(T o);
    T mul(T o);
    T sub(T o);
    T squared();
    T dbl();
    T inverse();
    T negate();
    boolean isZero();
    boolean isValid();
}
