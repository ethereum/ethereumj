package org.ethereum.util;

/**
 * Created by Anton Nashatyrev on 06.10.2016.
 */
public class ByteArraySet extends SetAdapter<byte[]> {

    public ByteArraySet() {
        super(new ByteArrayMap<>());
    }
}
