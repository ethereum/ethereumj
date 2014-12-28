package org.ethereum.gui;

import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;

/**
 * @author Roman Mandeleil
 * @since 01.09.2014
 */
public class UIEthereumManager {
    public static Ethereum ethereum = EthereumFactory.createEthereum();
}
