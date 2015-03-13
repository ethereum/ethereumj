package test.ethereum.jsontestsuite;

import org.codehaus.jackson.map.ObjectMapper;
import org.ethereum.jsontestsuite.CryptoTestCase;
import org.ethereum.jsontestsuite.JSONReader;
import org.json.simple.parser.ParseException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubCryptoTest {


    @Test
    public void testAllInCryptoSute() throws ParseException, IOException {

        String json = JSONReader.loadJSON("BasicTests/crypto.json");

        ObjectMapper mapper = new ObjectMapper();
        CryptoTestCase[] testSuite =
                mapper.readValue(json, CryptoTestCase[].class);

        for (CryptoTestCase cryptoTestCase : testSuite){

            cryptoTestCase.execute();

        }
    }


}
