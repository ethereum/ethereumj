package org.ethereum.datasource;

/**
 * Converter from one type to another and vice versa
 *
 * Created by Anton Nashatyrev on 17.03.2016.
 */
public interface Serializer<T, S> {
    /**
     * Converts T ==> S
     * Should correctly handle null parameter
     */
    S serialize(T object);
    /**
     * Converts S ==> T
     * Should correctly handle null parameter
     */
    T deserialize(S stream);
}
