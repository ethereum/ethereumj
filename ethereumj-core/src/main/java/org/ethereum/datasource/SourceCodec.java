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
package org.ethereum.datasource;

/**
 * Source for converting between different key/value types
 * Has no own state and immediately propagate all changes
 * to the backing Source with key/value conversion
 *
 * Created by Anton Nashatyrev on 03.11.2016.
 */
public class SourceCodec<Key, Value, SourceKey, SourceValue>
        extends AbstractChainedSource<Key, Value, SourceKey, SourceValue>  {

    protected Serializer<Key, SourceKey> keySerializer;
    protected Serializer<Value, SourceValue> valSerializer;

    /**
     * Instantiates class
     * @param src  Backing Source
     * @param keySerializer  Key codec Key <=> SourceKey
     * @param valSerializer  Value codec Value <=> SourceValue
     */
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

    /**
     * Shortcut class when only value conversion is required
     */
    public static class ValueOnly<Key, Value, SourceValue> extends SourceCodec<Key, Value, Key, SourceValue> {
        public ValueOnly(Source<Key, SourceValue> src, Serializer<Value, SourceValue> valSerializer) {
            super(src, new Serializers.Identity<Key>(), valSerializer);
        }
    }

    /**
     * Shortcut class when only value conversion is required and keys are of byte[] type
     */
    public static class BytesKey<Value, SourceValue> extends ValueOnly<byte[], Value, SourceValue> {
        public BytesKey(Source<byte[], SourceValue> src, Serializer<Value, SourceValue> valSerializer) {
            super(src, valSerializer);
        }
    }
}
