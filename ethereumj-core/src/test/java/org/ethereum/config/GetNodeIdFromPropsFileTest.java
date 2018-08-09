/*
 * Copyright (c) [2017] [ <ether.camp> ]
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

import org.ethereum.crypto.ECKey;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.spongycastle.util.encoders.Hex;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.*;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Not thread safe - testGeneratedNodePrivateKey temporarily removes the nodeId.properties
 * file which may influence other tests.
 */
@SuppressWarnings("ConstantConditions")
@NotThreadSafe
public class GetNodeIdFromPropsFileTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private File nodeIdPropertiesFile;

    @Before
    public void init() {
        //Cleanup previous nodeId.properties file (if exists)
        nodeIdPropertiesFile = new File("database-test/nodeId.properties");
        //noinspection ResultOfMethodCallIgnored
        nodeIdPropertiesFile.delete();
    }

    @After
    public void teardown() {
        //Cleanup created nodeId.properties file
        //noinspection ResultOfMethodCallIgnored
        nodeIdPropertiesFile.delete();
    }

    @Test
    public void testGenerateNodeIdFromPropsFileReadsExistingFile() throws Exception {
        // Create temporary nodeId.properties file
        ECKey key = new ECKey();
        String expectedNodePrivateKey = Hex.toHexString(key.getPrivKeyBytes());
        String expectedNodeId = Hex.toHexString(key.getNodeId());
        createNodeIdPropertiesFile("database-test", key);

        new GetNodeIdFromPropsFile("database-test").getNodePrivateKey();

        assertTrue(nodeIdPropertiesFile.exists());
        String contents = FileCopyUtils.copyToString(new FileReader(nodeIdPropertiesFile));
        String[] lines = StringUtils.tokenizeToStringArray(contents, "\n");
        assertEquals(4, lines.length);
        assertTrue(lines[0].startsWith("#Generated NodeID."));
        assertTrue(lines[1].startsWith("#"));
        assertTrue(lines[2].startsWith("nodeIdPrivateKey=" + expectedNodePrivateKey));
        assertTrue(lines[3].startsWith("nodeId=" + expectedNodeId));
    }

    @Test
    public void testGenerateNodeIdFromPropsDoesntGenerateRandomWhenFileIsPresent() throws Exception {
        // Create temporary nodeId.properties file
        createNodeIdPropertiesFile("database-test", new ECKey());

        GenerateNodeIdRandomly randomNodeIdGeneratorStrategySpy = spy(new GenerateNodeIdRandomly("database-test"));
        GenerateNodeIdStrategy fileNodeIdGeneratorStrategy = new GetNodeIdFromPropsFile("database-test")
            .withFallback(randomNodeIdGeneratorStrategySpy);

        fileNodeIdGeneratorStrategy.getNodePrivateKey();

        verifyZeroInteractions(randomNodeIdGeneratorStrategySpy);
    }

    @Test
    public void testGenerateNodeIdFromPropsGeneratesRandomWhenFileIsNotPresent() {
        GenerateNodeIdRandomly randomNodeIdGeneratorStrategySpy = spy(new GenerateNodeIdRandomly("database-test"));
        GenerateNodeIdStrategy fileNodeIdGeneratorStrategy = new GetNodeIdFromPropsFile("database-test")
            .withFallback(randomNodeIdGeneratorStrategySpy);

        fileNodeIdGeneratorStrategy.getNodePrivateKey();

        verify(randomNodeIdGeneratorStrategySpy).getNodePrivateKey();
    }

    @Test
    public void testGenerateNodeIdFromPropsGeneratesRandomWhenFileIsNotPresentAndNoFallback() {
        exception.expect(RuntimeException.class);
        exception.expectMessage("Can't read 'nodeId.properties' and no fallback method has been set");

        GenerateNodeIdStrategy fileNodeIdGeneratorStrategy = new GetNodeIdFromPropsFile("database-test");

        fileNodeIdGeneratorStrategy.getNodePrivateKey();
    }

    private void createNodeIdPropertiesFile(String dir, ECKey key) throws IOException {
        Properties props = new Properties();
        props.setProperty("nodeIdPrivateKey", Hex.toHexString(key.getPrivKeyBytes()));
        props.setProperty("nodeId", Hex.toHexString(key.getNodeId()));

        File file = new File(dir, "nodeId.properties");
        file.getParentFile().mkdirs();
        try (Writer writer = new FileWriter(file)) {
            props.store(writer, "Generated NodeID. To use your own nodeId please refer to 'peer.privateKey' config option.");
        }
    }

}
