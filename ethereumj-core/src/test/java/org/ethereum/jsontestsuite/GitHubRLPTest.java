package org.ethereum.jsontestsuite;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.ethereum.jsontestsuite.suite.JSONReader;
import org.ethereum.jsontestsuite.suite.RLPTestCase;
import org.json.simple.parser.ParseException;
import org.junit.*;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubRLPTest {

    private static Logger logger = LoggerFactory.getLogger("rlp");
    private static HashMap<String , RLPTestCase> TEST_SUITE;

    @BeforeClass
    public static void init() throws ParseException, IOException {
        logger.info("    Initializing RLP tests...");
        String json = JSONReader.loadJSON("RLPTests/rlptest.json");

        Assume.assumeFalse("Online test is not available", json.equals(""));

        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().
                constructMapType(HashMap.class, String.class, RLPTestCase.class);

        TEST_SUITE = mapper.readValue(json, type);
    }

    @Test
    public void rlpEncodeTest() throws Exception {
        logger.info("    Testing RLP encoding...");

        for (String key : TEST_SUITE.keySet()) {
            logger.info("    " + key);
            RLPTestCase testCase = TEST_SUITE.get(key);
            testCase.doEncode();
            Assert.assertEquals(testCase.getExpected(), testCase.getComputed());
        }
    }

    @Test
    public void rlpDecodeTest() throws Exception {
        logger.info("    Testing RLP decoding...");

        Set<String> excluded = new HashSet<>();

        for (String key : TEST_SUITE.keySet()) {
            if ( excluded.contains(key)) {
                logger.info("[X] " + key);
                continue;
            }
            else {
                logger.info("    " + key);
            }

            RLPTestCase testCase = TEST_SUITE.get(key);
            testCase.doDecode();
            Assert.assertEquals(testCase.getExpected(), testCase.getComputed());
        }
    }
}
