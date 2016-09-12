package org.ethereum.config;

import com.google.common.io.Files;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import org.ethereum.util.FileUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

/**
 * Created by Stan Reshetnyk on 11.09.16.
 */
public class InitializerTest {

    final Initializer.CheckDatabaseVersionWithReset resetHelper = new Initializer.CheckDatabaseVersionWithReset();

    File tempFile;
    String databaseDir;
    File versionFile;

    final static boolean RESET = true;
    final static boolean NOT_RESET = false;

    @Before
    public void before() {
        tempFile = Files.createTempDir();
        databaseDir = tempFile.getAbsolutePath() + "/database";
        versionFile = new File(databaseDir + "/version.properties");
    }

    @After
    public void after() {
        FileUtil.recursiveDelete(tempFile.getAbsolutePath());
    }

    // RESET

    @Test
    public void reset_shouldCreateVersionFile() {
        SystemProperties props = withConfig(1, true);

        // state without database
        assertEquals(new Integer(-1), resetHelper.getDatabaseVersion(versionFile));
        assertTrue(!resetHelper.isDatabaseDirectoryExists(props));

        // create database version file
        resetHelper.validateDatabaseVersion(props);

        // state with just created database
        assertEquals(new Integer(1), resetHelper.getDatabaseVersion(versionFile));
        assertTrue(resetHelper.isDatabaseDirectoryExists(props));

        // running validate for a second time should change nothing
        resetHelper.validateDatabaseVersion(props);
        assertEquals(new Integer(1), resetHelper.getDatabaseVersion(versionFile));
        assertTrue(resetHelper.isDatabaseDirectoryExists(props));
    }

    @Test(expected = RuntimeException.class)
    public void reset_shouldStop_whenNoVersionFileAndNotFirstVersion() throws IOException {
        SystemProperties props = withConfig(2, NOT_RESET);
        resetHelper.validateDatabaseVersion(props);

        // database is assumed to exist if dir is not empty
        versionFile.renameTo(new File(versionFile.getAbsoluteFile() + ".renamed"));

        resetHelper.validateDatabaseVersion(props);
    }


    @Test
    public void reset_shouldReset_whenDifferentVersionAndFlag() {
        SystemProperties props1 = withConfig(1, true);
        resetHelper.validateDatabaseVersion(props1);

        SystemProperties props2 = withConfig(2, RESET);
        resetHelper.validateDatabaseVersion(props2);
        assertTrue(!resetHelper.isDatabaseDirectoryExists(props2));
    }

    @Test(expected = RuntimeException.class)
    public void reset_shouldNotReset_whenDifferentVersionAndNoFlag() {
        final SystemProperties props1 = withConfig(1, true);
        resetHelper.validateDatabaseVersion(props1);

        final SystemProperties props2 = withConfig(2, NOT_RESET);
        resetHelper.validateDatabaseVersion(props2);
        assertTrue(resetHelper.isDatabaseDirectoryExists(props2));
    }


    // HELPERS

    private SystemProperties withConfig(int databaseVersion, boolean autoResetOldVersion) {
        Config config = ConfigFactory.empty()
                .withValue("database.autoResetOldVersion", ConfigValueFactory.fromAnyRef(autoResetOldVersion));

        SPO systemProperties = new SPO(config);
        systemProperties.setDataBaseDir(databaseDir);
        systemProperties.setDatabaseVersion(databaseVersion);
        return systemProperties;
    }

    public static class SPO extends SystemProperties {

        public SPO(Config config) {
            super(config);
        }

        public void setDatabaseVersion(Integer databaseVersion) {
            this.databaseVersion = databaseVersion;
        }
    }
}
