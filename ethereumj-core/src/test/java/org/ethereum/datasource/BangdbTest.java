package org.ethereum.datasource;

import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.bangdb.BangdbDataSource;
import org.ethereum.util.ByteUtil;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by Anton Nashatyrev on 01.02.2017.
 */
public class BangdbTest {

    @Ignore
    @Test
    public void test1() {
        BangdbDataSource db = new BangdbDataSource("aaa");
        db.init();

        db.put(key(1), key(1));

        byte[] bytes = db.get(key(1));

        Assert.assertArrayEquals(key(1), bytes);
    }

    private static byte[] key(int i) {
        return HashUtil.sha3(ByteUtil.intToBytes(1));
    }
}
