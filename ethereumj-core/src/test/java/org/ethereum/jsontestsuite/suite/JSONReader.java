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
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONReader {

    private static Logger logger = LoggerFactory.getLogger("TCK-Test");

    private static final String CACHE_DIR = SystemProperties.getDefault().githubTestsPath();
    private static final boolean USE_CACHE = SystemProperties.getDefault().githubTestsLoadLocal();
    private static final String CACHE_INDEX = "index.prop";
    private static final String CACHE_FILES_SUB_DIR = "files";

    static ExecutorService threadPool;

    private static int MAX_RETRIES = 3;

    public static List<String> loadJSONsFromCommit(List<String> filenames, final String shacommit) {

        int threads = 16;
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
                throw new RuntimeException(String.format("Failed to retrieve %d files from commit %s",
                        filenames.size(), shacommit), e);
            }
        }

        return ret;
    }

    public static String loadJSONFromCommit(String filename, String shacommit) throws IOException {
        String json = "";
        json = getFromUrl("https://raw.githubusercontent.com/ethereum/tests/" + shacommit + "/" + filename);
        if (!json.isEmpty()) json = json.replaceAll("//", "data");
        return json;
    }

    public static String getFromLocal(String filename) throws IOException {

        filename = SystemProperties.getDefault().githubTestsPath()
                + System.getProperty("file.separator") + filename.replaceAll("/", System.getProperty("file.separator"));

        logger.info("Loading local file: {}", filename);

        return new String(Files.readAllBytes(Paths.get(filename)));
    }

    public static String getFromUrl(String urlToRead) {
        String result = null;
        for (int i = 0; i < MAX_RETRIES; ++i) {
            try {
                if (USE_CACHE) {
                    result = getFromCacheImpl(urlToRead);
                    if (result == null) {
                        result = getFromUrlImpl(urlToRead);
                        recordCache(urlToRead, result);
                    }
                } else {
                    result = getFromUrlImpl(urlToRead);
                }
                break;
            } catch (Exception ex) {
                logger.debug(String.format("Failed to retrieve %s, retry %d/%d", urlToRead, (i + 1), MAX_RETRIES), ex);
                if (i < (MAX_RETRIES - 1)) {
                    try {
                        Thread.sleep(2000);  // adding delay after fail
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
        if (result == null) throw new RuntimeException(String.format("Failed to retrieve file from url %s", urlToRead));

        return result;
    }

    private static String getFromUrlImpl(String urlToRead) throws Exception {
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
            logger.debug("Failed to retrieve file.", e);
            throw e;
        }
        return result.toString();
    }

    private static String getFromCacheImpl(String urlToRead) {
        String result = null;
        String filename = null;
        try (InputStream input = new FileInputStream(CACHE_DIR + System.getProperty("file.separator") + CACHE_INDEX)) {
            Properties prop = new Properties();
            prop.load(input);
            filename = prop.getProperty(urlToRead);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (filename != null) {
            try {
                result = new String(Files.readAllBytes(new File(CACHE_DIR + System.getProperty("file.separator") +
                        CACHE_FILES_SUB_DIR + System.getProperty("file.separator") + filename).toPath()));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return result;
    }

    private synchronized static void recordCache(String urlToRead, String data) {
        String filename = UUID.randomUUID().toString();
        File targetFile = new File(CACHE_DIR + System.getProperty("file.separator") +
                CACHE_FILES_SUB_DIR + System.getProperty("file.separator") + filename);

        // Ensure we have directories created
        File parent = targetFile.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Couldn't create dir: " + parent);
        }

        // Load index
        Properties prop = new Properties();
        String propFile = CACHE_DIR + System.getProperty("file.separator") + CACHE_INDEX;
        try (InputStream input = new FileInputStream(propFile)) {
            prop.load(input);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Save with new entry
        prop.setProperty(urlToRead, filename);
        try (OutputStream output = new FileOutputStream(propFile)) {
            prop.store(output, null);
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        // Save data
        try (OutputStream output = new FileOutputStream(targetFile)) {
            output.write(data.getBytes());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static List<String> listJsonBlobsForTreeSha(String sha, String testRoot) throws IOException {

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
