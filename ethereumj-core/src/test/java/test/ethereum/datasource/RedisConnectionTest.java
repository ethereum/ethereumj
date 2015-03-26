package test.ethereum.datasource;

import org.ethereum.core.Transaction;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.redis.RedisConnection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import test.ethereum.TestContext;

import java.math.BigInteger;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class RedisConnectionTest {

    @Configuration
    @ComponentScan(basePackages = "org.ethereum")
    static class ContextConfiguration extends TestContext { }

    @Autowired
    private RedisConnection redisConnection;
    
    @Test
    public void test() {
        if (!redisConnection.isAvailable()) return;

        Set<Pojo> pojos = redisConnection.createSetFor(Pojo.class, "pojos");

        Pojo pojo = Pojo.create(1L, "test");
        pojos.add(pojo);
        assertTrue(pojos.contains(pojo));
        assertEquals(1, pojos.size());
        Pojo next = pojos.iterator().next();
        assertNotNull(next);
        assertEquals(pojo.getId(), next.getId());
        assertEquals(pojo.getName(), next.getName());
        assertTrue(pojos.remove(pojo));
        assertTrue(pojos.isEmpty());
    }

    @Test
    public void transactionStorageTest() {
        if (!redisConnection.isAvailable()) return;

        String namespace = "txnNamespace";
        Set<Transaction> transactions = redisConnection.createTransactionSet(namespace);
        transactions.add(createTransaction("09184e72a000", "4255", "1000000000000000000000", "cat"));
        transactions.add(createTransaction("09184e72a000", "4255", "1000000000000000000000", "dog"));
        transactions.add(createTransaction("09184e72a000", "4255", "1000000000000000000000", "rabbit"));

        Set<Transaction> transactions1 = redisConnection.createTransactionSet(namespace);
        transactions1.add(createTransaction("09184e72a000", "4255", "1000000000000000000000", "duck"));
        transactions1.add(createTransaction("09184e72a000", "4255", "1000000000000000000000", "chicken"));
        transactions1.add(createTransaction("09184e72a000", "4255", "1000000000000000000000", "cow"));
        
        assertEquals(6, transactions1.size());
        transactions.clear();
        assertTrue(transactions1.isEmpty());
    }

    private static class Pojo {
        private long id;
        private String name;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
        
        public static Pojo create(long id, String name) {
            Pojo result = new Pojo();
            result.setId(id);
            result.setName(name);
            return result;
        }
    }
    
    public static Transaction createTransaction(String gasPrice, String gas, String val, String secret) {

        ECKey ecKey = ECKey.fromPrivate(HashUtil.sha3(secret.getBytes()));

        // Tn (nonce); Tp(pgas); Tg(gaslimi); Tt(value); Tv(value); Ti(sender);  Tw; Tr; Ts
        return new Transaction(null, Hex.decode(gasPrice), Hex.decode(gas), ecKey.getAddress(),
                new BigInteger(val).toByteArray(),
                null);
    }
}