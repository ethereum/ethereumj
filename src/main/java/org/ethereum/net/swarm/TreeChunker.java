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
package org.ethereum.net.swarm;

import org.ethereum.util.ByteUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * From Go implementation:
 *
 * The distributed storage implemented in this package requires fix sized chunks of content
 * Chunker is the interface to a component that is responsible for disassembling and assembling larger data.
 * TreeChunker implements a Chunker based on a tree structure defined as follows:
 * 1 each node in the tree including the root and other branching nodes are stored as a chunk.
 * 2 branching nodes encode data contents that includes the size of the dataslice covered by its
 *   entire subtree under the node as well as the hash keys of all its children
 *   data_{i} := size(subtree_{i}) || key_{j} || key_{j+1} .... || key_{j+n-1}
 * 3 Leaf nodes encode an actual subslice of the input data.
 * 4 if data size is not more than maximum chunksize, the data is stored in a single chunk
 *   key = sha256(int64(size) + data)
 * 2 if data size is more than chunksize*Branches^l, but no more than
 *   chunksize*Branches^l length (except the last one).
 *   key = sha256(int64(size) + key(slice0) + key(slice1) + ...)
 *   Tree chunker is a concrete implementation of data chunking.
 *   This chunker works in a simple way, it builds a tree out of the document so that each node either
 *   represents a chunk of real data or a chunk of data representing an branching non-leaf node of the tree.
 *   In particular each such non-leaf chunk will represent is a concatenation of the hash of its respective children.
 *   This scheme simultaneously guarantees data integrity as well as self addressing. Abstract nodes are
 *   transparent since their represented size component is strictly greater than their maximum data size,
 *   since they encode a subtree.
 *   If all is well it is possible to implement this by simply composing readers so that no extra allocation or
 *   buffering is necessary for the data splitting and joining. This means that in principle there
 *   can be direct IO between : memory, file system, network socket (bzz peers storage request is
 *   read from the socket ). In practice there may be need for several stages of internal buffering.
 *   Unfortunately the hashing itself does use extra copies and allocation though since it does need it.
 */
public class TreeChunker implements Chunker {

    public static final MessageDigest DEFAULT_HASHER;
    private static final int DEFAULT_BRANCHES = 128;

    static {
        try {
            DEFAULT_HASHER = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);  // Can't happen.
        }
    }

    public class TreeChunk extends Chunk {
        private static final int DATA_OFFSET = 8;

        public TreeChunk(int dataSize) {
            super(null, new byte[DATA_OFFSET + dataSize]);
            setSubtreeSize(dataSize);
        }

        public TreeChunk(Chunk chunk) {
            super(chunk.getKey(), chunk.getData());
        }

        public void setSubtreeSize(long size) {
            ByteBuffer.wrap(getData()).order(ByteOrder.LITTLE_ENDIAN).putLong(0, size);
        }

        public long getSubtreeSize() {
            return ByteBuffer.wrap(getData()).order(ByteOrder.LITTLE_ENDIAN).getLong(0);
        }

        public int getDataOffset() {
            return DATA_OFFSET;
        }

        public Key getKey() {
            if (key == null) {
                key = new Key(hasher.digest(getData()));
            }
            return key;
        }

        @Override
        public String toString() {
            String dataString = ByteUtil.toHexString(
                    Arrays.copyOfRange(getData(), getDataOffset(), getDataOffset() + 16)) + "...";
            return "TreeChunk[" + getSubtreeSize() + ", " + getKey() + ", " + dataString + "]";
        }
    }

    public class HashesChunk extends TreeChunk {

        public HashesChunk(long subtreeSize) {
            super(branches * hashSize);
            setSubtreeSize(subtreeSize);
        }

        public HashesChunk(Chunk chunk) {
            super(chunk);
        }

        public int getKeyCount() {
            return branches;
        }

        public Key getKey(int idx) {
            int off = getDataOffset() + idx * hashSize;
            return new Key(Arrays.copyOfRange(getData(), off, off + hashSize));
        }

        public void setKey(int idx, Key key) {
            int off = getDataOffset() + idx * hashSize;
            System.arraycopy(key.getBytes(), 0, getData(), off, hashSize);
        }

        @Override
        public String toString() {
            String hashes = "{";
            for (int i = 0; i < getKeyCount(); i++) {
                hashes += (i == 0 ? "" : ", ") + getKey(i);
            }
            hashes += "}";
            return "HashesChunk[" + getSubtreeSize() + ", " + getKey() + ", " + hashes + "]";
        }
    }

    private class TreeSize {
        int depth;
        long treeSize;

        public TreeSize(long dataSize) {
            treeSize = chunkSize;
            for (; treeSize < dataSize; treeSize *= branches) {
                depth++;
            }
        }
    }

    private int branches;
    private MessageDigest hasher;

    private int hashSize;
    private long chunkSize;

    public TreeChunker() {
        this(DEFAULT_BRANCHES, DEFAULT_HASHER);
    }

    public TreeChunker(int branches, MessageDigest hasher) {
        this.branches = branches;
        this.hasher = hasher;

        hashSize = hasher.getDigestLength();
        chunkSize = hashSize * branches;
    }

    public long getChunkSize() {
        return chunkSize;
    }

    @Override
    public Key split(SectionReader sectionReader, Collection<Chunk> consumer) {
        TreeSize ts = new TreeSize(sectionReader.getSize());
        return splitImpl(ts.depth, ts.treeSize/branches, sectionReader, consumer);
    }

    private Key splitImpl(int depth, long treeSize, SectionReader data, Collection<Chunk> consumer) {
        long size = data.getSize();
        TreeChunk newChunk;

        while (depth > 0 && size < treeSize) {
            treeSize /= branches;
            depth--;
        }

        if (depth == 0) {
            newChunk = new TreeChunk((int) size); // safe to cast since leaf chunk size < 2Gb
            data.read(newChunk.getData(), newChunk.getDataOffset());
        } else {
            // intermediate chunk containing child nodes hashes
            int branchCnt = (int) ((size + treeSize - 1) / treeSize);

            HashesChunk hChunk = new HashesChunk(size);

            long pos = 0;
            long secSize;

            // TODO the loop can be parallelized
            for (int i = 0; i < branchCnt; i++) {
                // the last item can have shorter data
                if (size-pos < treeSize) {
                    secSize = size - pos;
                } else {
                    secSize = treeSize;
                }
                // take the section of the data corresponding encoded in the subTree
                SectionReader subTreeData = new SlicedReader(data, pos, secSize);
                // the hash of that data
                Key subTreeKey = splitImpl(depth-1, treeSize/branches, subTreeData, consumer);

                hChunk.setKey(i, subTreeKey);

                pos += treeSize;
            }
            // now we got the hashes in the chunk, then hash the chunk
            newChunk = hChunk;
        }

        consumer.add(newChunk);
        // report hash of this chunk one level up (keys corresponds to the proper subslice of the parent chunk)x
        return newChunk.getKey();

    }

    @Override
    public SectionReader join(ChunkStore chunkStore, Key key) {
        return new LazyChunkReader(chunkStore, key);
    }

    @Override
    public long keySize() {
        return hashSize;
    }

    private class LazyChunkReader implements SectionReader {
        Key key;
        ChunkStore chunkStore;
        final long size;

        final Chunk root;

        public LazyChunkReader(ChunkStore chunkStore, Key key) {
            this.chunkStore = chunkStore;
            this.key = key;
            root = chunkStore.get(key);
            this.size = new TreeChunk(root).getSubtreeSize();
        }

        @Override
        public int readAt(byte[] dest, int destOff, long readerOffset) {
            int size = dest.length - destOff;
            TreeSize ts = new TreeSize(this.size);
            return readImpl(dest, destOff, root, ts.treeSize, 0, readerOffset,
                    readerOffset + min(size, this.size - readerOffset));
        }

        private int readImpl(byte[] dest, int destOff, Chunk chunk, long chunkWidth, long chunkStart,
                              long readStart, long readEnd) {
            long chunkReadStart = max(readStart - chunkStart, 0);
            long chunkReadEnd = min(chunkWidth, readEnd - chunkStart);

            int ret = 0;
            if (chunkWidth > chunkSize) {
                long subChunkWidth = chunkWidth / branches;
                if (chunkReadStart >= chunkWidth || chunkReadEnd <= 0) {
                    throw new RuntimeException("Not expected.");
                }

                int startSubChunk = (int) (chunkReadStart / subChunkWidth);
                int lastSubChunk = (int) ((chunkReadEnd - 1) / subChunkWidth);

                // TODO the loop can be parallelized
                for (int i = startSubChunk; i <= lastSubChunk; i++) {
                    HashesChunk hChunk = new HashesChunk(chunk);
                    Chunk subChunk = chunkStore.get(hChunk.getKey(i));
                    ret += readImpl(dest, (int) (destOff + (i - startSubChunk) * subChunkWidth),
                            subChunk, subChunkWidth, chunkStart + i * subChunkWidth, readStart, readEnd);
                }
            } else {
                TreeChunk dataChunk = new TreeChunk(chunk);
                ret = (int) (chunkReadEnd - chunkReadStart);
                System.arraycopy(dataChunk.getData(), (int) (dataChunk.getDataOffset() + chunkReadStart),
                        dest, destOff, ret);
            }
            return ret;
        }

        @Override
        public long seek(long offset, int whence) {
            throw new RuntimeException("Not implemented");
        }


        @Override
        public long getSize() {
            return size;
        }

        @Override
        public int read(byte[] dest, int destOff) {
            return readAt(dest, destOff, 0);
        }
    }

    /**
     * A 'subReader'
     */
    public static class SlicedReader implements SectionReader {
        SectionReader delegate;
        long offset;
        long len;

        public SlicedReader(SectionReader delegate, long offset, long len) {
            this.delegate = delegate;
            this.offset = offset;
            this.len = len;
        }

        @Override
        public long seek(long offset, int whence) {
            return delegate.seek(this.offset + offset, whence);
        }

        @Override
        public int read(byte[] dest, int destOff) {
            return delegate.readAt(dest, destOff, offset);
        }

        @Override
        public int readAt(byte[] dest, int destOff, long readerOffset) {
            return delegate.readAt(dest, destOff, offset + readerOffset);
        }

        @Override
        public long getSize() {
            return len;
        }
    }
}
