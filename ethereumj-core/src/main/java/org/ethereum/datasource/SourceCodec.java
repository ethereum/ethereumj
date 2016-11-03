package org.ethereum.datasource;

/**
 * Created by Anton Nashatyrev on 03.11.2016.
 */
public class SourceCodec<Key, Value, SourceKey, SourceValue> implements Source<Key, Value>  {
    protected Source<SourceKey, SourceValue> src;

    protected Serializer<Key, SourceKey> keySerializer;
    protected Serializer<Value, SourceValue> valSerializer;

    public SourceCodec(Source<SourceKey, SourceValue> src, Serializer<Key, SourceKey> keySerializer, Serializer<Value, SourceValue> valSerializer) {
        this.src = src;
        this.keySerializer = keySerializer;
        this.valSerializer = valSerializer;
    }

    @Override
    public void put(Key key, Value val) {
        src.put(keySerializer.serialize(key), valSerializer.serialize(val));
    }

    @Override
    public Value get(Key key) {
        return valSerializer.deserialize(src.get(keySerializer.serialize(key)));
    }

    @Override
    public void delete(Key key) {
        src.delete(keySerializer.serialize(key));
    }

    @Override
    public boolean flush() {
        return src.flush();
    }

    public static class ValueOnly<Key, Value, SourceValue> extends SourceCodec<Key, Value, Key, SourceValue> {
        public ValueOnly(Source<Key, SourceValue> src, Serializer<Value, SourceValue> valSerializer) {
            super(src, new Serializer.IdentitySerializer<Key>(), valSerializer);
        }
    }

    public static class BytesKey<Value, SourceValue> extends ValueOnly<byte[], Value, SourceValue> {
        public BytesKey(Source<byte[], SourceValue> src, Serializer<Value, SourceValue> valSerializer) {
            super(src, valSerializer);
        }
    }
}
