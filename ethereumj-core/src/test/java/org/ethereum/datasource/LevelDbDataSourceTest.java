package org.ethereum.datasource;

import org.ethereum.config.SystemProperties;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.ethereum.TestUtils.randomBytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Ignore
public class LevelDbDataSourceTest {

    @Test
    public void testBatchUpdating() {
        LevelDbDataSource dataSource = new LevelDbDataSource(SystemProperties.getDefault(), "test");
        dataSource.init();

        final int batchSize = 100;
        Map<byte[], byte[]> batch = createBatch(batchSize);
        
        dataSource.updateBatch(batch);

        assertEquals(batchSize, dataSource.keys().size());
        
        dataSource.close();
    }

    @Test
    public void testPutting() {
        LevelDbDataSource dataSource = new LevelDbDataSource(SystemProperties.getDefault(), "test");
        dataSource.init();

        byte[] key = randomBytes(32);
        dataSource.put(key, randomBytes(32));

        assertNotNull(dataSource.get(key));
        assertEquals(1, dataSource.keys().size());
        
        dataSource.close();
    }

    private static Map<byte[], byte[]> createBatch(int batchSize) {
        HashMap<byte[], byte[]> result = new HashMap<>();
        for (int i = 0; i < batchSize; i++) {
            result.put(randomBytes(32), randomBytes(32));
        }
        return result;
    }

}
