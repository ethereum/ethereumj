package org.ethereum.erp;

import org.ethereum.erp.ErpLoader.ErpMetadata;
import org.ethereum.util.ByteUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ErpLoaderTest
{

    private ErpLoader loader;

    @Before
    public void setUp() throws Exception {
        this.loader = new ErpLoader("/erps");
    }


    @Test
    public void loadErpMetadata() throws IOException {
        final Collection<ErpMetadata> erpMetadata = loader.loadErpMetadata();
        assertEquals(2, erpMetadata.size());

        final Iterator<ErpMetadata> iterator = erpMetadata.iterator();
        ErpMetadata metadata1 = iterator.next();
        ErpMetadata metadata2 = iterator.next();

        assertEquals(6000000, metadata1.getTargetBlock());
        assertEquals("erp-888", metadata1.getId());
        assertArrayEquals(ByteUtil.hexStringToBytes("6572702d383838"), metadata1.getErpMarker());
    }

    @Test
    public void loadERPResourceFiles() throws IOException {
        final File[] files = loader.loadERPResourceFiles("/erps");
        assertEquals(2, files.length);
        assertEquals("erp888.sco.json", files[0].getName());
        assertEquals("erp999.sco.json", files[1].getName());
    }

    @Test(expected = IOException.class)
    public void loadERPResourceFiles_BadDirectory() throws IOException {
        loader.loadERPResourceFiles("/xxx");
    }

    @Test
    public void loadStateChangeObject_metadata() throws IOException {
        final File file = new File(getClass().getResource("/erps/erp999.sco.json").getPath());
        final StateChangeObject sco = loader.loadStateChangeObject(new ErpMetadata("ignored", 0L, file));

        assertEquals("erp-999", sco.erpId);
        assertEquals(5000000L, sco.targetBlock);
        assertEquals(2,sco.actions.length);
    }

    @Test
    public void loadStateChangeObject_file() throws IOException {
        final File file = new File(getClass().getResource("/erps/erp999.sco.json").getPath());
        final StateChangeObject sco = loader.loadStateChangeObject(file);

        assertEquals("erp-999", sco.erpId);
        assertEquals(5000000L, sco.targetBlock);
        assertEquals(2,sco.actions.length);
    }

    @Test
    public void loadRawStateChangeObject_file() throws IOException {
        final File file = new File(getClass().getResource("/erps/erp999.sco.json").getPath());
        final RawStateChangeObject sco = loader.loadRawStateChangeObject(file);

        assertEquals("erp-999", sco.erpId);
        assertEquals(5000000L, sco.targetBlock);
        assertEquals(2,sco.actions.length);
    }
}