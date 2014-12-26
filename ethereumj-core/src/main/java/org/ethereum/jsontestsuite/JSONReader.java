package org.ethereum.jsontestsuite;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;

import org.ethereum.config.SystemProperties;

public class JSONReader {

    public static String loadJSON(String filename) {
        String json = "";
        if(!SystemProperties.CONFIG.vmTestLoadLocal())
            json = getFromUrl("https://raw.githubusercontent.com/ethereum/tests/develop/" + filename);
        return json == "" ? json = getFromLocal(filename) : json;
    }

    public static String getFromLocal(String filename) {
        System.out.println("Loading local file: " + filename);
        try {
            if(System.getProperty("ETHEREUM_TEST_PATH") == null) {
                System.out.println("ETHEREUM_TEST_PATH is not passed as a VM argument, please make sure you pass it with the correct path");
                return "";
            }
            System.out.println("From: " + System.getProperty("ETHEREUM_TEST_PATH"));
            File vmTestFile = new File(System.getProperty("ETHEREUM_TEST_PATH") + filename);
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
        String line;
        String result = "";
        try {
            url = new URL(urlToRead);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoOutput(true);
            conn.connect();
            InputStream in = conn.getInputStream();
            rd = new BufferedReader(new InputStreamReader(in));
            System.out.println("Loading remote file: " + urlToRead);
            while ((line = rd.readLine()) != null) {
                result += line;
            }
            rd.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }
}