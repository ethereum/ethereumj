package org.ethereum.jsontestsuite;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;

import static org.ethereum.jsontestsuite.GitHubJSONTestSuite.runABITest;

/**
 * @author Mikhail Kalinin
 * @since 28.09.2017
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GithubABITest {

    String commitSHA = "2d28e1f654bb4b6fe6aca2808b4b5c37e3fccdcc";

    @Test
    public void basicAbiTests() throws IOException {
        runABITest("ABITests/basic_abi_tests.json", commitSHA);
    }
}
