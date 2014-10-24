package org.ethereum.jsontestsuite;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;

public class JSONReader {

    public static String loadJSON(String filename) {
//    	return getFromLocal(filename);
    	String json = getFromUrl("https://raw.githubusercontent.com/ethereum/tests/develop/" + filename);
    	return json == "" ? json = getFromLocal(filename) : json;
    }
    
    public static String getFromLocal(String filename) {
    	System.out.println("Loading local file: " + filename);
    	try {
			URL vmtest = ClassLoader.getSystemResource("jsontestsuite/" + filename);
			File vmTestFile = new File(vmtest.toURI());
			return new String(Files.readAllBytes(vmTestFile.toPath()));
		} catch (URISyntaxException | IOException e) {
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
            conn.setConnectTimeout(3000);
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
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
