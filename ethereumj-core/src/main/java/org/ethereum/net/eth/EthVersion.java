package org.ethereum.net.eth;

/**
 * Represents supported Eth versions
 *
 * @author Mikhail Kalinin
 * @since 14.08.2015
 */
public enum EthVersion {

    V60((byte) 60);

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
        return fromCode(code) != null;
    }
}
