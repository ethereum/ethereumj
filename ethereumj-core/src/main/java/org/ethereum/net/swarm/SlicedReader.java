package org.ethereum.net.swarm;

/**
 * Created by Admin on 19.06.2015.
 */
public class SlicedReader implements SectionReader {
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
