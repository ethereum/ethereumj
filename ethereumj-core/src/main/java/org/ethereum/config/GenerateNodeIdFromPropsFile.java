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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

/**
 * Strategy to generate a random nodeId and the nodePrivateKey.
 * <p>
 * This strategy generates a nodeId and a nodePrivateKey from a new {@link org.ethereum.crypto.ECKey}
 * and save the values on nodeId.properties file (creates the file).
 *
 * @author Lucas Saldanha
 * @since 14.12.2017
 */
public class GenerateNodeIdFromPropsFile implements GenerateNodeIdStrategy {

    private String databaseDir;
    private GenerateNodeIdStrategy fallbackGenerateNodeIdStrategy;

    GenerateNodeIdFromPropsFile(String databaseDir) {
        this.databaseDir = databaseDir;
        this.fallbackGenerateNodeIdStrategy = new GenerateNodeIdRandomly(databaseDir);
    }

    @Override
    public String getNodePrivateKey() {
        Properties props = new Properties();
        File file = new File(databaseDir, "nodeId.properties");
        if (file.canRead()) {
            try (Reader r = new FileReader(file)) {
                props.load(r);
                return props.getProperty("nodeIdPrivateKey");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return fallbackGenerateNodeIdStrategy.getNodePrivateKey();
        }
    }

    /**
     * Used for testing
     *
     * @param generateNodeIdStrategy the fallback strategy to be used to generate the nodeId
     *                               and nodePrivateKey.
     */
    void setFallbackGenerateNodeIdStrategy(GenerateNodeIdStrategy generateNodeIdStrategy) {
        this.fallbackGenerateNodeIdStrategy = generateNodeIdStrategy;
    }
}
