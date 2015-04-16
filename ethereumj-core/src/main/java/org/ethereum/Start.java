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

        String host = "192.168.1.146";
        int port = 10101;
        String id = "b8425bd5941c72b68890bdaeea228d65a1316e9aeed8c824683a4504e6d8e5cfd3e6d15c8c4b507009abc51fb1251336ebd78ce5e92dd1b952c7dc6b4f868469";

        ethereum.connect(host,
                port, id);

    }

}
