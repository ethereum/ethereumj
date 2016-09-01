package org.ethereum.jsontestsuite;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.ethereum.jsontestsuite.suite.CryptoTestCase;
import org.ethereum.jsontestsuite.suite.JSONReader;
import org.json.simple.parser.ParseException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.util.HashMap;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubCryptoTest {


    @Test
    public void testAllInCryptoSute() throws ParseException, IOException {

        String json = JSONReader.loadJSON("BasicTests/crypto.json");

        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().
                constructMapType(HashMap.class, String.class, CryptoTestCase.class);


        HashMap<String , CryptoTestCase> testSuite =
                mapper.readValue(json, type);

        for (String key : testSuite.keySet()){

            System.out.println("executing: " + key);
            testSuite.get(key).execute();

        }
    }


}
