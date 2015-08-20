package org.ethereum.net.eth.handler;

import org.ethereum.net.eth.EthVersion;

/**
 * @author Mikhail Kalinin
 * @since 20.08.2015
 */
public interface EthHandlerFactory {

    EthHandler create(EthVersion version);

}
