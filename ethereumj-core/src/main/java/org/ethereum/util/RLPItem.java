package org.ethereum.util;

/**
 * @author Roman Mandeleil
 * @since 21.04.14
 */
public class RLPItem implements RLPElement {

    private final byte[] rlpData;

    public RLPItem(byte[] rlpData) {
        this.rlpData = rlpData;
    }

    public byte[] getRLPData() {
        if (rlpData.length == 0)
            return null;
        return rlpData;
    }
}
