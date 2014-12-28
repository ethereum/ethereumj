package org.ethereum.util;

import java.io.Serializable;

/**
 * Wrapper class for decoded elements from an RLP encoded byte array.
 *
 * @author Roman Mandeleil
 * @since 01.04.2014
 */
public interface RLPElement extends Serializable {

    public byte[] getRLPData();
}
