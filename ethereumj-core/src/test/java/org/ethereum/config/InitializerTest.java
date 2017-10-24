/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.google.common.io.Files;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import org.ethereum.util.FileUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.ethereum.config.Initializer.DatabaseVersionHandler.Behavior;
import static org.ethereum.config.Initializer.DatabaseVersionHandler.Behavior.*;
import static org.junit.Assert.*;

/**
 * Created by Stan Reshetnyk on 11.09.16.
 */
public class InitializerTest {

    private final Initializer.DatabaseVersionHandler resetHelper = new Initializer.DatabaseVersionHandler();

    private File tempFile;
    private String databaseDir;
    private File versionFile;

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

    @Test
    public void helper_shouldAllowCleanWorkspace() {
        SystemProperties props = withConfig(2, null);

        resetHelper.process(props);
        assertEquals(new Integer(2), resetHelper.getDatabaseVersion(versionFile));
        resetHelper.process(props);
    }

    @Test
    public void helper_shouldCreateVersionFile() {
        SystemProperties props = withConfig(1, null);

        // state without database
        assertEquals(new Integer(-1), resetHelper.getDatabaseVersion(versionFile));
        assertTrue(!resetHelper.isDatabaseDirectoryExists(props));

        // create database version file
        resetHelper.process(props);

        // state with just created database
        assertEquals(new Integer(1), resetHelper.getDatabaseVersion(versionFile));
        assertTrue(resetHelper.isDatabaseDirectoryExists(props));

        // running process for a second time should change nothing
        resetHelper.process(props);
        assertEquals(new Integer(1), resetHelper.getDatabaseVersion(versionFile));
        assertTrue(resetHelper.isDatabaseDirectoryExists(props));
    }

    @Test
    public void helper_shouldCreateVersionFile_whenOldVersion() {
        // create database without version
        SystemProperties props1 = withConfig(1, null);
        resetHelper.process(props1);
        //noinspection ResultOfMethodCallIgnored
        versionFile.renameTo(new File(versionFile.getAbsoluteFile() + ".renamed"));

        SystemProperties props2 = withConfig(2, IGNORE);
        resetHelper.process(props2);

        assertEquals(new Integer(1), resetHelper.getDatabaseVersion(versionFile));
        assertTrue(resetHelper.isDatabaseDirectoryExists(props2));
    }

    @Test(expected = RuntimeException.class)
    public void helper_shouldStop_whenNoVersionFileAndNotFirstVersion() throws IOException {
        SystemProperties props = withConfig(2, EXIT);
        resetHelper.process(props);

        // database is assumed to exist if dir is not empty
        //noinspection ResultOfMethodCallIgnored
        versionFile.renameTo(new File(versionFile.getAbsoluteFile() + ".renamed"));

        resetHelper.process(props);
    }

    @Test
    public void helper_shouldReset_whenDifferentVersionAndFlag() {
        SystemProperties props1 = withConfig(1, null);
        resetHelper.process(props1);

        final File testFile = createFile();
        SystemProperties props2 = withConfig(2, RESET);
        resetHelper.process(props2);
        assertFalse(testFile.exists());
        assertEquals(new Integer(2), resetHelper.getDatabaseVersion(versionFile));
    }

    @Test(expected = RuntimeException.class)
    public void helper_shouldExit_whenDifferentVersionAndFlag() {
        final SystemProperties props1 = withConfig(1, null);
        resetHelper.process(props1);

        final SystemProperties props2 = withConfig(2, EXIT);
        resetHelper.process(props2);
    }

    @Test(expected = RuntimeException.class)
    public void helper_shouldExit_byDefault() {
        final SystemProperties props1 = withConfig(1, null);
        resetHelper.process(props1);

        final SystemProperties props2 = withConfig(2, null);
        resetHelper.process(props2);
    }

    @Test
    public void helper_shouldIgnore_whenDifferentVersionAndFlag() {
        final SystemProperties props1 = withConfig(1, EXIT);
        resetHelper.process(props1);
        final File testFile = createFile();

        final SystemProperties props2 = withConfig(2, IGNORE);
        resetHelper.process(props2);
        assertTrue(testFile.exists());
        assertEquals(new Integer(1), resetHelper.getDatabaseVersion(versionFile));
    }

    @Test
    public void helper_shouldPutVersion_afterDatabaseReset() throws IOException {
        Config config = ConfigFactory.empty()
                .withValue("database.reset", ConfigValueFactory.fromAnyRef(true));

        SPO systemProperties = new SPO(config);
        systemProperties.setDataBaseDir(databaseDir);
        systemProperties.setDatabaseVersion(33);
        final File testFile = createFile();

        assertTrue(testFile.exists());
        resetHelper.process(systemProperties);
        assertEquals(new Integer(33), resetHelper.getDatabaseVersion(versionFile));

        assertFalse(testFile.exists()); // reset should have cleared file
    }

    @Test
    public void helper_shouldPrintCapabilityEthVersion_whenInfoEnabled() {
        SystemProperties system = new SystemProperties();
        Initializer initializer = new Initializer();

        ListAppender<ILoggingEvent> inMemoryAppender = new ListAppender<>();
        inMemoryAppender.start();

        Logger logger = (Logger) LoggerFactory.getLogger("general");
        try {
            logger.setLevel(Level.DEBUG);
            logger.addAppender(inMemoryAppender);

            initializer.postProcessBeforeInitialization(system, "initializerBean");
            assertContainsLogLine(inMemoryAppender.list, "capability eth version: [62, 63]");
            assertContainsLogLine(inMemoryAppender.list, "capability shh version: [3]");
            assertContainsLogLine(inMemoryAppender.list, "capability bzz version: [0]");
        } finally {
            inMemoryAppender.stop();
            logger.detachAppender(inMemoryAppender);
        }
    }

    private void assertContainsLogLine(List<ILoggingEvent> lines, final String line) {
        for (ILoggingEvent loggingEvent : lines) {
            if (loggingEvent.getFormattedMessage().equals(line)) {
                return;
            }
        }
        fail("Could not find log line that matches: " + line);
    }


    // HELPERS

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File createFile() {
        final File testFile = new File(databaseDir + "/empty.file");
        testFile.getParentFile().mkdirs();
        try {
            testFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException("Can't create file in database dir");
        }
        return testFile;
    }

    private SystemProperties withConfig(int databaseVersion, Behavior behavior) {
        Config config = ConfigFactory.empty()
                // reset is true for tests
                .withValue("database.reset", ConfigValueFactory.fromAnyRef(false));

        if (behavior != null) {
            config = config.withValue("database.incompatibleDatabaseBehavior",
                    ConfigValueFactory.fromAnyRef(behavior.toString().toLowerCase()));
        }


        SPO systemProperties = new SPO(config);
        systemProperties.setDataBaseDir(databaseDir);
        systemProperties.setDatabaseVersion(databaseVersion);
        return systemProperties;
    }

    public static class SPO extends SystemProperties {

        SPO(Config config) {
            super(config);
        }

        void setDatabaseVersion(Integer databaseVersion) {
            this.databaseVersion = databaseVersion;
        }
    }
}
