package org.ethereum.erp;

import org.ethereum.erp.ErpLoader.ErpMetadata;
import org.ethereum.util.ByteUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.*;

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

        final Map<String, ErpMetadata> erpsById = erpMetadata.stream()
                .collect(toMap(ErpMetadata::getId, Function.identity()));

        ErpMetadata erp888 = erpsById.get("erp-888");
        ErpMetadata erp999 = erpsById.get("erp-999");
        assertNotNull("Did not find erp888", erp888);
        assertNotNull("Did not find erp999", erp999);

        assertEquals(6000000, erp888.getTargetBlock());
        assertArrayEquals(ByteUtil.hexStringToBytes("6572702d383838"), erp888.getErpMarker());
    }

    @Test
    public void loadERPResourceFiles() throws IOException {
        final File[] files = loader.loadERPResourceFiles("/erps");
        assertEquals(2, files.length);
        List<String> fileNames = Arrays.stream(files).map(File::getName).collect(Collectors.toList());
        assertTrue(fileNames.contains("erp888.sco.json"));
        assertTrue(fileNames.contains("erp999.sco.json"));
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