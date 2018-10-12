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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

/**
 * Strategy to randomly generate the nodeId and the nodePrivateKey.
 *
 * @author Lucas Saldanha
 * @since 14.12.2017
 */
public class GenerateNodeIdRandomly implements GenerateNodeIdStrategy {

    private static Logger logger = LoggerFactory.getLogger("general");

    private String databaseDir;

    GenerateNodeIdRandomly(String databaseDir) {
        this.databaseDir = databaseDir;
    }

    @Override
    public String getNodePrivateKey() {
        ECKey key = new ECKey();
        Properties props = new Properties();
        props.setProperty("nodeIdPrivateKey", Hex.toHexString(key.getPrivKeyBytes()));
        props.setProperty("nodeId", Hex.toHexString(key.getNodeId()));

        File file = new File(databaseDir, "nodeId.properties");
        file.getParentFile().mkdirs();
        try (Writer writer = new FileWriter(file)) {
            props.store(writer, "Generated NodeID. To use your own nodeId please refer to 'peer.privateKey' config option.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        logger.info("New nodeID generated: " + props.getProperty("nodeId"));
        logger.info("Generated nodeID and its private key stored in " + file);

        return props.getProperty("nodeIdPrivateKey");
    }
}
