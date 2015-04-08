package org.ethereum.net.rlpx;

import org.spongycastle.math.ec.ECPoint;

/**
 * ECIES encrypted message
 *
 * Created by devrandom on 2015-04-07.
 */
public class EncryptedMessage {
    ECPoint publicKey; // 65 bytes
    byte[] initialVector; // 16 bytes
    byte[] mac; // 32 bytes
}
