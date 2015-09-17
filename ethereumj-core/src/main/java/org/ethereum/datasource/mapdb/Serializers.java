package org.ethereum.datasource.mapdb;

import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.BlockWrapper;
import org.ethereum.db.ByteArrayWrapper;
import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;

/**
 * @author Mikhail Kalinin
 * @since 09.07.2015
 */
public class Serializers {

    public static final Serializer<BlockHeader> BLOCK_HEADER_SERIALIZER = new Serializer<BlockHeader>() {

        @Override
        public void serialize(DataOutput out, BlockHeader header) throws IOException {
            byte[] bytes = header == null ? EMPTY_BYTE_ARRAY : header.getEncoded();
            BYTE_ARRAY.serialize(out, bytes);
        }

        @Override
        public BlockHeader deserialize(DataInput in, int available) throws IOException {
            byte[] bytes = BYTE_ARRAY.deserialize(in, available);
            return bytes.length > 0 ? new BlockHeader(bytes) : null;
        }
    };

    public static final Serializer<ByteArrayWrapper> BYTE_ARRAY_WRAPPER = new Serializer<ByteArrayWrapper>() {

        @Override
        public void serialize(DataOutput out, ByteArrayWrapper wrapper) throws IOException {
            byte[] bytes = getBytes(wrapper);
            BYTE_ARRAY.serialize(out, bytes);
        }

        @Override
        public ByteArrayWrapper deserialize(DataInput in, int available) throws IOException {
            byte[] bytes = BYTE_ARRAY.deserialize(in, available);
            return bytes.length > 0 ? new ByteArrayWrapper(bytes) : null;
        }
    };

    public static final Serializer<BlockWrapper> BLOCK_WRAPPER = new Serializer<BlockWrapper>()  {

        @Override
        public void serialize(DataOutput out, BlockWrapper wrapper) throws IOException {
            byte[] bytes = getBytes(wrapper);
            BYTE_ARRAY.serialize(out, bytes);
        }

        @Override
        public BlockWrapper deserialize(DataInput in, int available) throws IOException {
            byte[] bytes = BYTE_ARRAY.deserialize(in, available);
            return bytes.length > 0 ? new BlockWrapper(bytes) : null;
        }
    };

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

    private static byte[] getBytes(ByteArrayWrapper wrapper) {
        return wrapper == null ? EMPTY_BYTE_ARRAY : wrapper.getData();
    }

    private static byte[] getBytes(Block block) {
        return block == null ? EMPTY_BYTE_ARRAY : block.getEncoded();
    }

    private static byte[] getBytes(BlockWrapper wrapper) {
        return wrapper == null ? EMPTY_BYTE_ARRAY : wrapper.getBytes();
    }
}
