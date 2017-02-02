package org.ethereum.net.par;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents supported Par versions
 */
public enum ParVersion {

    PAR1((byte) 1);

    public static final byte LOWER = PAR1.getCode();
    public static final byte UPPER = PAR1.getCode();

    private byte code;

    ParVersion(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static ParVersion fromCode(int code) {
        for (ParVersion v : values()) {
            if (v.code == code) {
                return v;
            }
        }

        return null;
    }

    public static boolean isSupported(byte code) {
        return code >= LOWER && code <= UPPER;
    }

    public static List<ParVersion> supported() {
        List<ParVersion> supported = new ArrayList<>();
        for (ParVersion v : values()) {
            if (isSupported(v.code)) {
                supported.add(v);
            }
        }

        return supported;
    }

    public boolean isCompatible(ParVersion version) {

        if (version.getCode() >= PAR1.getCode()) {
            return this.getCode() >= PAR1.getCode();
        } else {
            return this.getCode() < PAR1.getCode();
        }
    }

    @Override
    public String toString() {
        return "" + code;
    }
}
