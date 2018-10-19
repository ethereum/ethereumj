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
 * Strategy to generate the nodeId and the nodePrivateKey from a nodeId.properties file.
 * <p>
 * If the nodeId.properties file doesn't exist, it uses the
 * {@link GetNodeIdFromPropsFile#fallbackGenerateNodeIdStrategy} as a fallback strategy
 * to generate the nodeId and nodePrivateKey.
 *
 * @author Lucas Saldanha
 * @since 14.12.2017
 */
public class GetNodeIdFromPropsFile implements GenerateNodeIdStrategy {

    private String databaseDir;
    private GenerateNodeIdStrategy fallbackGenerateNodeIdStrategy;

    GetNodeIdFromPropsFile(String databaseDir) {
        this.databaseDir = databaseDir;
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
              throw new RuntimeException("Error reading 'nodeId.properties' file", e);
            }
        } else {
            if (fallbackGenerateNodeIdStrategy != null) {
                return fallbackGenerateNodeIdStrategy.getNodePrivateKey();
            } else {
                throw new RuntimeException("Can't read 'nodeId.properties' and no fallback method has been set");
            }
        }
    }

    public GenerateNodeIdStrategy withFallback(GenerateNodeIdStrategy generateNodeIdStrategy) {
        this.fallbackGenerateNodeIdStrategy = generateNodeIdStrategy;
        return this;
    }
}
