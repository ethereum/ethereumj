package org.ethereum;

import org.ethereum.cli.CLIInterface;
import org.ethereum.config.SystemProperties;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.net.rlpx.EncryptionHandshake;
import org.ethereum.net.rlpx.FrameCodec;
import org.ethereum.net.rlpx.Handshaker;

import java.io.IOException;

/**
 * @author Roman Mandeleil
 * @since 14.11.2014
 */
public class Start {

    public static void main(String args[]) throws IOException {
        CLIInterface.call(args);
        Ethereum ethereum = EthereumFactory.createEthereum();

        String host = "52.16.188.185";
        int port = 30303;
        String id = "09fbeec0d047e9a37e63f60f8618aa9df0e49271f3fadb2c070dc09e2099b95827b63a8b837c6fd01d0802d457dd83e3bd48bd3e6509f8209ed90dabbc30e3d3";


        Handshaker handshaker =  new Handshaker();
        handshaker.doHandshake(host, port, id);
        EncryptionHandshake.Secrets secrets = handshaker.getSecrets();

        FrameCodec frameCodec = new FrameCodec(secrets, null, null);
        ethereum.setFrameCode(frameCodec);


        ethereum.connect(host,
                port);

    }

}
