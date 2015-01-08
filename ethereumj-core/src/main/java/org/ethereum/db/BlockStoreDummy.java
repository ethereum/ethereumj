package org.ethereum.db;

import org.ethereum.crypto.HashUtil;
import org.ethereum.util.ByteUtil;

/**
 * @author: Roman Mandeleil
 * Created on: 08/01/2015 17:33
 */

public class BlockStoreDummy  implements BlockStore{
    
    @Override
    public byte[] getBlockHashByNumber(long blockNumber) {
        
        byte[] data = String.valueOf(blockNumber).getBytes();
        return HashUtil.sha3(data);
    }
}
