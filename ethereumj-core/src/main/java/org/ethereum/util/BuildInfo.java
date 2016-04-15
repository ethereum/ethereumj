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

                props.load(is);

                buildHash = props.getProperty("build.hash");
                buildTime = props.getProperty("build.time");
                buildBranch = props.getProperty("build.branch");
        } catch (IOException e) {
            logger.error("Error reading /build-info.properties", e);
        }
    }

    public static void printInfo(){
        logger.info("git.hash: [{}]", buildHash);
        logger.info("build.time: {}", buildTime);
    }
}
