package org.ethereum.datasource;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @author alexbraz
 * @since 29/03/2019
 */
public class BatchSourceWriterTest {

    @Test
    public void testFlush() {
        BatchSource batchSource = mock(BatchSource.class);
        BatchSourceWriter<String, BigInteger> bsw = new BatchSourceWriter(batchSource);
        bsw.put("KEY", BigInteger.ONE);
        assertTrue(bsw.flushImpl());
    }

    @Test
    public void testValues() {
        BatchSource batchSource = mock(BatchSource.class);
        BatchSourceWriter<String, BigInteger> bsw = new BatchSourceWriter(batchSource);
        bsw.put("ONE", BigInteger.ONE);
        bsw.put("TEN", BigInteger.TEN);
        bsw.put("ZERO", BigInteger.ZERO);

        bsw.buf.forEach((K, v) -> {
            assertEquals(v, bsw.buf.get(K));
        });

    }
}
