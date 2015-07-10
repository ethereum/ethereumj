package org.ethereum.datasource.mapdb;

import org.ethereum.core.Block;
import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Mikhail Kalinin
 * @since 09.07.2015
 */
public class Serializers {

    public static final Serializer<Block> BLOCK = new Serializer<Block>()  {

        @Override
        public void serialize(DataOutput out, Block block) throws IOException {
            byte[] bytes = getBytes(block);
            BYTE_ARRAY.serialize(out, bytes);
        }

        @Override
        public Block deserialize(DataInput in, int available) throws IOException {
            byte[] bytes = BYTE_ARRAY.deserialize(in, available);
            return bytes.length > 0 ? new Block(bytes) : null;
        }
    };

    private static byte[] getBytes(Block block) {
        return block == null ? new byte[0] : block.getEncoded();
    }
}
