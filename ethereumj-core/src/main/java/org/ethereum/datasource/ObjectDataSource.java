package org.ethereum.datasource;

/**
 * Just a convenient class to store arbitrary Objects into byte[] value backing
 * Source.
 * Includes ReadCache for caching deserialized objects and object Serializer
 *
 * Created by Anton Nashatyrev on 06.12.2016.
 */
public class ObjectDataSource<V> extends SourceChainBox<byte[], V, byte[], byte[]> {
    ReadCache<byte[], V> cache;
    SourceCodec<byte[], V, byte[], byte[]> codec;
    Source<byte[], byte[]> byteSource;

    /**
     * Creates new instance
     * @param byteSource baking store
     * @param serializer for encode/decode byte[] <=> V
     * @param readCacheEntries number of entries to cache
     */
    public ObjectDataSource(Source<byte[], byte[]> byteSource, Serializer<V, byte[]> serializer, int readCacheEntries) {
        super(byteSource);
        this.byteSource = byteSource;
        add(codec = new SourceCodec<>(byteSource, new Serializers.Identity<byte[]>(), serializer));
        add(cache = new ReadCache.BytesKey<>(codec).withMaxCapacity(readCacheEntries));
    }
}
