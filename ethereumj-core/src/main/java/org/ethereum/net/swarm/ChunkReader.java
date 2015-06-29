package org.ethereum.net.swarm;

import java.io.Writer;

/**
 * Created by Admin on 18.06.2015.
 */
public class ChunkReader implements SectionReader, Sliced {

    public void writeTo(Writer writer) {} // ???

    @Override
    public long seek(long offset, int whence) {
        return 0;
    }

    @Override
    public int read(byte[] dest, int destOff) {
        return 0;
    }

    @Override
    public int readAt(byte[] dest, int destOff, long readerOffset) {
        return 0;
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public byte[] slice(long from, long to) {
        return new byte[0];
    }
}
