package org.ethereum.db;

import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.datasource.LevelDbDataSource;

import org.iq80.leveldb.Options;

import org.junit.AfterClass;
import org.junit.Test;

import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.IOException;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;
import static org.junit.Assert.*;

/**
 * @author Roman Mandeleil
 * @since 11.06.2014
 */
public class TrackDatabaseTest {

    @Test
    public void test1() {

        KeyValueDataSource keyValueDataSource = new LevelDbDataSource(SystemProperties.getDefault(), "temp");
        keyValueDataSource.init();

        DatabaseImpl db1 = new DatabaseImpl(keyValueDataSource);
        TrackDatabase trackDatabase1 = new TrackDatabase(db1);

        trackDatabase1.put(Hex.decode("abcdef"), Hex.decode("abcdef"));
        byte[] value = trackDatabase1.get(Hex.decode("abcdef"));
        assertEquals("abcdef", Hex.toHexString(value));

        trackDatabase1.startTrack();
        trackDatabase1.put(Hex.decode("abcdef"), Hex.decode("ffffff"));
        value = trackDatabase1.get(Hex.decode("abcdef"));
        assertEquals("ffffff", Hex.toHexString(value));

        trackDatabase1.rollbackTrack();
        value = trackDatabase1.get(Hex.decode("abcdef"));
        assertEquals("abcdef", Hex.toHexString(value));

        trackDatabase1.startTrack();
        trackDatabase1.put(Hex.decode("abcdef"), Hex.decode("ffffff"));
        trackDatabase1.commitTrack();
        value = trackDatabase1.get(Hex.decode("abcdef"));
        assertEquals("ffffff", Hex.toHexString(value));

        db1.close();
    }

    @AfterClass
    public static void destroyDB() {
        try {
            Options options = new Options();
            factory.destroy(new File("temp"), options);
        } catch (IOException e) {
            fail("Destroying temp-db failed");
        }
    }
}
