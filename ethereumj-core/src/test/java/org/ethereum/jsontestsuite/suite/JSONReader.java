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
import org.ethereum.util.FileUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;

import java.net.HttpURLConnection;
import java.net.URL;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONReader {

    private static Logger logger = LoggerFactory.getLogger("TCK-Test");

    static ExecutorService threadPool;

    public static List<String> loadJSONsFromCommit(List<String> filenames, final String shacommit) {

        int threads = 64;
        if (threadPool == null) {
            threadPool = Executors.newFixedThreadPool(threads);
        }

        List<Future<String>> retF = new ArrayList<>();
        for (final String filename : filenames) {
            Future<String> f = threadPool.submit(() -> loadJSONFromCommit(filename, shacommit));
            retF.add(f);
        }

        List<String> ret = new ArrayList<>();
        for (Future<String> f : retF) {
            try {
                ret.add(f.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        return ret;
    }

    public static String loadJSONFromCommit(String filename, String shacommit) throws IOException {
        String json = "";
        if (!SystemProperties.getDefault().githubTestsLoadLocal())
            json = getFromUrl("https://raw.githubusercontent.com/ethereum/tests/" + shacommit + "/" + filename);
        if (!json.isEmpty()) json = json.replaceAll("//", "data");
        return json.isEmpty() ? getFromLocal(filename) : json;
    }

    public static String getFromLocal(String filename) throws IOException {

        filename = SystemProperties.getDefault().githubTestsPath()
                + System.getProperty("file.separator") + filename.replaceAll("/", System.getProperty("file.separator"));

        logger.info("Loading local file: {}", filename);

        return new String(Files.readAllBytes(Paths.get(filename)));
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

    public static List<String> listJsonBlobsForTreeSha(String sha, String testRoot) throws IOException {

        if (SystemProperties.getDefault().githubTestsLoadLocal()) {

            String path = SystemProperties.getDefault().githubTestsPath() +
                    System.getProperty("file.separator") + testRoot.replaceAll("/", "");

            List<String> files = FileUtil.recursiveList(path);

            List<String> jsons = new ArrayList<>();
            for (String f : files) {
                if (f.endsWith(".json"))
                    jsons.add(
                            f.replace(path + System.getProperty("file.separator"), "")
                             .replaceAll(System.getProperty("file.separator"), "/"));
            }

            return jsons;
        }

        String result = getFromUrl("https://api.github.com/repos/ethereum/tests/git/trees/" + sha + "?recursive=1");

        JSONParser parser = new JSONParser();
        JSONObject testSuiteObj = null;

        List<String> blobs = new ArrayList<>();
        try {
            testSuiteObj = (JSONObject) parser.parse(result);
            JSONArray tree = (JSONArray)testSuiteObj.get("tree");

            for (Object oEntry : tree) {
                JSONObject entry = (JSONObject) oEntry;
                String type = (String) entry.get("type");
                String path = (String) entry.get("path");

                if (!type.equals("blob")) continue;
                if (!path.endsWith(".json")) continue;

                blobs.add((String) entry.get("path"));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return blobs;
    }
}
