package org.ethereum.net.par.handler;

import org.ethereum.net.par.ParVersion;

public interface ParHandlerFactory {

    /**
     * Creates EthHandler by requested Par version
     *
     * @param version Par version
     * @return created handler
     *
     * @throws IllegalArgumentException if provided Par version is not supported
     */
    ParHandler create(ParVersion version);

}
