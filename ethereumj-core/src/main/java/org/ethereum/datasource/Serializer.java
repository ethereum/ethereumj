package org.ethereum.datasource;

/**
 * Created by Anton Nashatyrev on 17.03.2016.
 */
public interface Serializer<T, S> {
    S serialize(T object);
    T deserialize(S stream);
}
