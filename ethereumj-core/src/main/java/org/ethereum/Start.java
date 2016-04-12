package org.ethereum;

import org.ethereum.cli.CLIInterface;
import org.ethereum.config.SystemProperties;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;

import java.io.IOException;
import java.net.URISyntaxException;



/**
 * @author Roman Mandeleil
 * @since 14.11.2014
 */
public class Start {

    public static void main(String args[]) throws IOException, URISyntaxException {
        SystemProperties config = CLIInterface.call(args);

        if (!config.blocksLoader().equals("")) {
            config.setSyncEnabled(false);
            config.setDiscoveryEnabled(false);
        }

        Ethereum ethereum = EthereumFactory.createEthereum(config);

        if (!config.blocksLoader().equals(""))
            ethereum.getBlockLoader().loadBlocks();
    }

}
