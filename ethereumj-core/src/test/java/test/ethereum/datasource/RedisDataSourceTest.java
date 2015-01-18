package test.ethereum.datasource;

import org.ethereum.datasource.RedisDataSource;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * @author: Roman Mandeleil
 * Created on: 18/01/2015 22:40
 */

public class RedisDataSourceTest {
    
   
    @Test
    public void testSet1(){

        try {
            RedisDataSource redis = new RedisDataSource();
            redis.setName("state");
            redis.init();

            byte[] key = Hex.decode("a1a2a3");
            byte[] val = Hex.decode("b1b2b3");

            redis.put(key, val);
            byte[] val2 =  redis.get(key);

            Assert.assertEquals(Hex.toHexString(val), Hex.toHexString(val2));
        } catch (JedisConnectionException e) {
            // no redis server consider test as pass
        }
    }

    @Test
    public void testSet2(){

        try {
            RedisDataSource redis1 = new RedisDataSource();
            redis1.setName("state");
            redis1.init();

            RedisDataSource redis2 = new RedisDataSource();
            redis2.setName("details");
            redis2.init();


            byte[] key = Hex.decode("a1a2a3");
            byte[] val1 = Hex.decode("b1b2b3");
            byte[] val2 = Hex.decode("c1c2c3");

            redis1.put(key, val1);
            redis2.put(key, val2);

            byte[] res1 = redis1.get(key);
            byte[] res2 = redis2.get(key);

            Assert.assertEquals(Hex.toHexString(val1), Hex.toHexString(res1));
            Assert.assertEquals(Hex.toHexString(val2), Hex.toHexString(res2));
        } catch (JedisConnectionException e) {
            // no redis server consider test as pass
        }
    }

}
