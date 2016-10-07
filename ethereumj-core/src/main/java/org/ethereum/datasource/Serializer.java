package org.ethereum.datasource;

/**
 * Created by Anton Nashatyrev on 17.03.2016.
 */
public interface Serializer<T, S> {
    S serialize(T object);
    T deserialize(S stream);

    class IdentitySerializer<T> implements Serializer<T, T> {
        @Override
        public T serialize(T object) {
            return object;
        }
        @Override
        public T deserialize(T stream) {
            return stream;
        }
    }
}
