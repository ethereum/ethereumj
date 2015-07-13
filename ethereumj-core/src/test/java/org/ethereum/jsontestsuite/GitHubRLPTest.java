package org.ethereum.jsontestsuite;

import org.ethereum.util.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.*;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubRLPTest {

    private static Logger logger = LoggerFactory.getLogger("rlp");

    @Test
    public void rlpEncodeTest() throws Exception {
        logger.info("    Testing RLP encoding...");
        String json = JSONReader.loadJSON("RLPTests/rlptest.json");

        Assume.assumeFalse("Online test is not available", json.equals(""));

        JSONParser parser = new JSONParser();
        JSONObject testSuiteObj = (JSONObject) parser.parse(json);

        for (Object key : testSuiteObj.keySet()) {
            String testCase = (String) key;
            logger.info("    " + testCase);

            JSONObject testCaseJSON = (JSONObject) testSuiteObj.get(testCase);
            byte[] in = buildRLP(testCaseJSON.get("in"));
            String expected = testCaseJSON.get("out").toString().toLowerCase();
            String computed = Hex.toHexString(in);
            Assert.assertEquals(computed, expected);
        }
    }

    @Test
    public void rlpDecodeTest() throws Exception {
        logger.info("    Testing RLP decoding...");
        String json = JSONReader.loadJSON("RLPTests/rlptest.json");

        Assume.assumeFalse("Online test is not available", json.equals(""));

        Set<String> excluded = new HashSet<>();
        excluded.add("emptystring");
        excluded.add("mediumint4");
        excluded.add("mediumint5");
        excluded.add("bigint");

        JSONParser parser = new JSONParser();
        JSONObject testSuiteObj = (JSONObject) parser.parse(json);

        for (Object key : testSuiteObj.keySet()) {
            String testCase = (String) key;

            if ( excluded.contains(testCase))
                logger.info("[X] " + testCase);
            else
                logger.info("    " + testCase);

            if (excluded.contains(testCase)) continue;

            JSONObject testCaseJSON = (JSONObject) testSuiteObj.get(key);
            String out = testCaseJSON.get("out").toString().toLowerCase();
            Object in = testCaseJSON.get("in");
            RLPList list = RLP.decode2(Hex.decode(out));
            checkRLPAgainstJson(list.get(0), in);
        }
    }

    public byte[] buildRLP(Object in) {
        if (in instanceof JSONArray) {
            List<byte[]> elementList = new Vector<>();
            for (Object o : ((JSONArray) in).toArray()) {
                elementList.add(buildRLP(o));
            }
            byte[][] elements = elementList.toArray(new byte[elementList.size()][]);
            return RLP.encodeList(elements);
        } else {
            if (in instanceof String) {
                String s = in.toString();
                if (s.contains("#")) {
                    return RLP.encode(new BigInteger(s.substring(1)));
                }
            } else if (in instanceof Long) {
                return RLP.encodeInt(Integer.parseInt(in.toString()));
            }
            return RLP.encode(in);
        }
    }

    public void checkRLPAgainstJson(RLPElement element, Object in) {
        if (in instanceof JSONArray) {
            Object[] array = ((JSONArray) in).toArray();
            RLPList list = (RLPList) element;
            for (int i = 0; i < array.length; i++) {
                checkRLPAgainstJson(list.get(i), array[i]);
            }
        } else if (in instanceof Long) {
            int computed = ByteUtil.byteArrayToInt(element.getRLPData());
            Assert.assertEquals(computed, Integer.parseInt(in.toString()));
        } else if (in instanceof String) {
            String s = in.toString();
            if (s.contains("#")) {
                s = s.substring(1);
                BigInteger expected = new BigInteger(s);
                byte[] payload = Hex.decode(element.getRLPData());
                BigInteger computed = RLP.decodeBigInteger(payload, 0);
                Assert.assertEquals(computed, expected);
            } else {
                String expected = null;
                try {
                    expected = new String(element.getRLPData(), "UTF-8");
                } catch (Exception e) {}
                Assert.assertEquals(expected, s);
            }
        }
    }
}
