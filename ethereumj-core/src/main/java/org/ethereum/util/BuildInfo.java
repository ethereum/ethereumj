package org.ethereum.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BuildInfo {

    private static final Logger logger = LoggerFactory.getLogger("general");


    public static void printInfo(){
        try {
            Properties props = new Properties();
            InputStream is = ClassLoader.getSystemResourceAsStream("build-info.properties");

            if (is != null) {
                props.load(is);

                String hash = props.getProperty("build.hash");
                String time = props.getProperty("build.time");

                logger.info("git.hash: [{}]", hash);
                logger.info("build.time: {}", time);
                logger.info("");
            }
        } catch (IOException e) {}
    }
}
