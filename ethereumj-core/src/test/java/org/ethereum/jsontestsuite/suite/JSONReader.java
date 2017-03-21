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
package org.ethereum.jsontestsuite.suite;

import org.ethereum.config.SystemProperties;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONReader {

    private static Logger logger = LoggerFactory.getLogger("TCK-Test");

    public static String loadJSON(String filename) {
        String json = "";
        if (!SystemProperties.getDefault().vmTestLoadLocal())
            json = getFromUrl("https://raw.githubusercontent.com/ethereum/tests/develop/" + filename);
        return json.isEmpty() ? getFromLocal(filename) : json;
    }

    public static String loadJSONFromCommit(String filename, String shacommit) {
        String json = "";
        if (!SystemProperties.getDefault().vmTestLoadLocal())
            json = getFromUrl("https://raw.githubusercontent.com/ethereum/tests/" + shacommit + "/" + filename);
        if (!json.isEmpty()) json = json.replaceAll("//", "data");
        return json.isEmpty() ? getFromLocal(filename) : json;
    }

    public static String getFromLocal(String filename) {
        System.out.println("Loading local file: " + filename);
        try {
            File vmTestFile = new File(filename);
            if (!vmTestFile.exists()){
                System.out.println(" Error: no file: " +filename);
                System.exit(1);
            }
            return new String(Files.readAllBytes(vmTestFile.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getFromUrl(String urlToRead) {
        URL url;
        HttpURLConnection conn;
        BufferedReader rd;
        StringBuilder result = new StringBuilder();
        String line;
        try {
            url = new URL(urlToRead);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoOutput(true);
            conn.connect();
            InputStream in = conn.getInputStream();
            rd = new BufferedReader(new InputStreamReader(in), 819200);

            logger.info("Loading remote file: " + urlToRead);
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    public static String getTestBlobForTreeSha(String shacommit, String testcase){

        String result = getFromUrl("https://api.github.com/repos/ethereum/tests/git/trees/" + shacommit);

        JSONParser parser = new JSONParser();
        JSONObject testSuiteObj = null;

        List<String> fileNames = new ArrayList<String>();
        try {
            testSuiteObj = (JSONObject) parser.parse(result);
            JSONArray tree = (JSONArray)testSuiteObj.get("tree");

            for (Object oEntry : tree) {
                JSONObject entry = (JSONObject) oEntry;
                String testName = (String) entry.get("path");
                if ( testName.equals(testcase) ) {
                    String blobresult = getFromUrl( (String) entry.get("url") );

                    testSuiteObj = (JSONObject) parser.parse(blobresult);
                    String blob  = (String) testSuiteObj.get("content");
                    byte[] valueDecoded= Base64.decodeBase64(blob.getBytes() );
                    //System.out.println("Decoded value is " + new String(valueDecoded));
                    return new String(valueDecoded);
                }
            }
        } catch (ParseException e) {e.printStackTrace();}

        return "";
    }

    public static List<String> getFileNamesForTreeSha(String sha){

        String result = getFromUrl("https://api.github.com/repos/ethereum/tests/git/trees/" + sha + "?recursive=1");

        JSONParser parser = new JSONParser();
        JSONObject testSuiteObj = null;

        List<String> fileNames = new ArrayList<String>();
        try {
            testSuiteObj = (JSONObject) parser.parse(result);
            JSONArray tree = (JSONArray)testSuiteObj.get("tree");

            for (Object oEntry : tree) {
                JSONObject entry = (JSONObject) oEntry;
                String testName = (String) entry.get("path");
                fileNames.add(testName);
            }
        } catch (ParseException e) {e.printStackTrace();}

        return fileNames;
    }
}
