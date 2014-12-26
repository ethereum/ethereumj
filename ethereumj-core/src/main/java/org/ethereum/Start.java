package org.ethereum;

import org.ethereum.cli.CLIInterface;
import org.ethereum.config.SystemProperties;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;

/**
 * www.etherj.com
 *
 * @author: Roman Mandeleil
 * Created on: 14/11/2014 07:39
 */

public class Start {

    public static void main(String args[]) {
        CLIInterface.call(args);
        Ethereum ethereum = EthereumFactory.createEthereum();

        ethereum.connect(SystemProperties.CONFIG.activePeerIP(),
                SystemProperties.CONFIG.activePeerPort());

    }

}
