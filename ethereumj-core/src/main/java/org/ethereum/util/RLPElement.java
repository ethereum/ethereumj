package org.ethereum.util;

import java.io.Serializable;

/**
 * Wrapper class for decoded elements from an RLP encoded byte array.
 *
 * www.ethereumJ.com
 *
 * @author Roman Mandeleil
 * Created on: 01/04/2014 10:45
 */
public interface RLPElement extends Serializable {

    public byte[] getRLPData();
}
