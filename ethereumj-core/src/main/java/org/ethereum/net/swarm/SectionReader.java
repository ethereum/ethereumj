package org.ethereum.net.swarm;

/**
 * Created by Admin on 18.06.2015.
 */
public interface SectionReader extends Bounded {

    long seek(long offset, int whence /* ??? */);

    int read(byte[] dest, int destOff);

    int readAt(byte[] dest, int destOff, long readerOffset);
}
