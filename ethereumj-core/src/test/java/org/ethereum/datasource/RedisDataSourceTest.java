package org.ethereum.datasource;

import org.ethereum.datasource.redis.RedisDataSource;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

/**
 * @author Roman Mandeleil
 */
public class RedisDataSourceTest extends AbstractRedisTest {

    
    
    @Test
    public void testSet1() {
        if (!isConnected()) return;

        KeyValueDataSource dataSource = createDataSource("test-state");
        try {
            byte[] key = Hex.decode("a1a2a3");
            byte[] val = Hex.decode("b1b2b3");

            dataSource.put(key, val);
            byte[] val2 = dataSource.get(key);

            Assert.assertEquals(Hex.toHexString(val), Hex.toHexString(val2));
        } finally {
            clear(dataSource);
        }
    }

    @Test
    public void testSet2() {
        if (!isConnected()) return;

        KeyValueDataSource states = createDataSource("test-state");
        KeyValueDataSource details = createDataSource("test-details");

        try {
            byte[] key = Hex.decode("a1a2a3");
            byte[] val1 = Hex.decode("b1b2b3");
            byte[] val2 = Hex.decode("c1c2c3");

            states.put(key, val1);
            details.put(key, val2);

            byte[] res1 = states.get(key);
            byte[] res2 = details.get(key);

            Assert.assertEquals(Hex.toHexString(val1), Hex.toHexString(res1));
            Assert.assertEquals(Hex.toHexString(val2), Hex.toHexString(res2));
        } finally {
            clear(states);
            clear(details);
        }
    }

    private KeyValueDataSource createDataSource(String name) {
        KeyValueDataSource result = getRedisConnection().createDataSource(name);
        result.setName(name);
        result.init();
        return result;
    }

    private void clear(KeyValueDataSource dataSource) {
        ((RedisDataSource) dataSource).clear();
    }

}
