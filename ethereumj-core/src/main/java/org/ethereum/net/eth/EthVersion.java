package org.ethereum.net.eth;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents supported Eth versions
 *
 * @author Mikhail Kalinin
 * @since 14.08.2015
 */
public enum EthVersion {

    V60((byte) 60),
    V61((byte) 61);

    public static final byte LOWER = V60.getCode();
    public static final byte UPPER = V61.getCode();

    private byte code;

    EthVersion(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static EthVersion fromCode(int code) {
        for (EthVersion v : values()) {
            if (v.code == code) {
                return v;
            }
        }

        return null;
    }

    public static boolean isSupported(byte code) {
        return code >= LOWER && code <= UPPER;
    }

    public static List<EthVersion> supported() {
        List<EthVersion> supported = new ArrayList<>();
        for (EthVersion v : values()) {
            if (isSupported(v.code)) {
                supported.add(v);
            }
        }

        return supported;
    }

}
