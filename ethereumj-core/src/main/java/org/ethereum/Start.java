package org.ethereum;

import org.ethereum.cli.CLIInterface;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.ImportResult;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;

/**
 * @author Roman Mandeleil
 * @since 14.11.2014
 */
public class Start {

    public static void main(String args[]) throws IOException, URISyntaxException {
        CLIInterface.call(args);

        if (!SystemProperties.getDefault().blocksLoader().equals("")) {
            SystemProperties.getDefault().setSyncEnabled(false);
            SystemProperties.getDefault().setDiscoveryEnabled(false);
        }

        final Ethereum ethereum = EthereumFactory.createEthereum();

        if (!SystemProperties.getDefault().blocksLoader().equals(""))
            ethereum.getBlockLoader().loadBlocks();
    }

}
