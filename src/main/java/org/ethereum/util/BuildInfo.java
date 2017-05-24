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
package org.ethereum.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BuildInfo {

    private static final Logger logger = LoggerFactory.getLogger("general");

    public static String buildHash;
    public static String buildTime;
    public static String buildBranch;

    static {
        try {
            Properties props = new Properties();
            InputStream is = BuildInfo.class.getResourceAsStream("/build-info.properties");

            if (is != null) {
                props.load(is);

                buildHash = props.getProperty("build.hash");
                buildTime = props.getProperty("build.time");
                buildBranch = props.getProperty("build.branch");
            } else {
                logger.warn("File not found `build-info.properties`. Run `gradle build` to generate it");
            }
        } catch (IOException e) {
            logger.error("Error reading /build-info.properties", e);
        }
    }

    public static void printInfo(){
        logger.info("git.hash: [{}]", buildHash);
        logger.info("build.time: {}", buildTime);
    }
}
