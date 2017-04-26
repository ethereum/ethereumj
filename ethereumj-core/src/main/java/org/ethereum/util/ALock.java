package org.ethereum.util;

import java.util.concurrent.locks.Lock;

/**
 * AutoClosable Lock wrapper. Use case:
 *
 * try (ALock l = wLock.lock()) {
 *     // do smth under lock
 * }
 *
 * Created by Anton Nashatyrev on 27.01.2017.
 */
public final class ALock implements AutoCloseable {
    private final Lock lock;

    public ALock(Lock l) {
        this.lock = l;
    }

    public final ALock lock() {
        this.lock.lock();
        return this;
    }

    public final void close() {
        this.lock.unlock();
    }
}
