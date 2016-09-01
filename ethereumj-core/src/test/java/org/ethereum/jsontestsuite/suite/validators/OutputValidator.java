package org.ethereum.jsontestsuite.suite.validators;

import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

import static org.ethereum.jsontestsuite.suite.Utils.parseData;

public class OutputValidator {

    public static List<String> valid(String origOutput, String postOutput){

        List<String> results = new ArrayList<>();

        String postOutputFormated = Hex.toHexString(parseData(postOutput));

        if (!origOutput.equals(postOutputFormated)){
            String formattedString = String.format("HReturn: wrong expected: %s, current: %s",
                    postOutputFormated, origOutput);
            results.add(formattedString);
        }

        return results;
    }

}
