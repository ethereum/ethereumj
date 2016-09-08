package org.ethereum.api.type;

import org.ethereum.db.ByteArrayWrapper;

/**
 * Created by Anton Nashatyrev on 08.09.2016.
 */
public class ByteArray extends ByteArrayWrapper {
//    private static final long serialVersionUID = 8652327067626568720L;

    public ByteArray(byte[] data) {
        super(data);
    }
}
