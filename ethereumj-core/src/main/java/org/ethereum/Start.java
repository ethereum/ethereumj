package org.ethereum;

import org.ethereum.cli.CLIInterface;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.net.rlpx.Node;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * @author Roman Mandeleil
 * @since 14.11.2014
 */
public class Start {

    public static void main(String args[]) throws IOException, URISyntaxException {
        CLIInterface.call(args);

        if (!CONFIG.blocksLoader().equals("")) {
            CONFIG.setSyncEnabled(false);
            CONFIG.setDiscoveryEnabled(false);
        }

        Ethereum ethereum = EthereumFactory.createEthereum();

        if (!CONFIG.blocksLoader().equals(""))
            ethereum.getBlockLoader().loadBlocks();
    }

}
