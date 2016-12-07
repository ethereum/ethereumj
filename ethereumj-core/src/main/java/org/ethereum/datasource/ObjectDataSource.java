package org.ethereum.datasource;

/**
 * Created by Anton Nashatyrev on 06.12.2016.
 */
public class ObjectDataSource<V> extends SourceDelegateAdapter<byte[], V> {
    ReadCache<byte[], V> cache;
    SourceCodec<byte[], V, byte[], byte[]> codec;
    Source<byte[], byte[]> byteSource;

    public ObjectDataSource(Source<byte[], byte[]> byteSource, Serializer<V, byte[]> serializer, int readCacheEntries) {
        this.byteSource = byteSource;
        codec = new SourceCodec<>(byteSource, new Serializers.Identity<byte[]>(), serializer);
        cache = new ReadCache.BytesKey<V>(codec);
        cache.withMaxCapacity(readCacheEntries);
        setSource(cache);
    }

    public Source<byte[], byte[]> getByteSource() {
        return byteSource;
    }
}
