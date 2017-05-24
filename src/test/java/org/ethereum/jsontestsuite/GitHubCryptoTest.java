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
