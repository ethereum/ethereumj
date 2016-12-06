package org.ethereum.datasource;

/**
 * Created by Anton Nashatyrev on 03.11.2016.
 */
public class SourceCodec<Key, Value, SourceKey, SourceValue>
        extends AbstractChainedSource<Key, Value, SourceKey, SourceValue>  {

    protected Serializer<Key, SourceKey> keySerializer;
    protected Serializer<Value, SourceValue> valSerializer;

    public SourceCodec(Source<SourceKey, SourceValue> src, Serializer<Key, SourceKey> keySerializer, Serializer<Value, SourceValue> valSerializer) {
        super(src);
        this.keySerializer = keySerializer;
        this.valSerializer = valSerializer;
        setFlushSource(true);
    }

    @Override
    public void put(Key key, Value val) {
        getSource().put(keySerializer.serialize(key), valSerializer.serialize(val));
    }

    @Override
    public Value get(Key key) {
        return valSerializer.deserialize(getSource().get(keySerializer.serialize(key)));
    }

    @Override
    public void delete(Key key) {
        getSource().delete(keySerializer.serialize(key));
    }

    @Override
    public boolean flushImpl() {
        return false;
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
