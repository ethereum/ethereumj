package org.ethereum.jsontestsuite;

import org.ethereum.db.ByteArrayWrapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * www.etherj.com
 *
 * @author Roman Mandeleil
 * @since 15.12.2014
 */

public class StateTestCase {

    private String name = "";

    private Env env;
    private Logs logs;
    private byte[] out;

    //            "pre": { ... },
    private Map<ByteArrayWrapper, AccountState> pre = new HashMap<>();

    //            "post": { ... },
    private Map<ByteArrayWrapper, AccountState> post = new HashMap<>();

    private Transaction transaction;

    public StateTestCase(String name, JSONObject testCaseJSONObj) throws ParseException {

        this(testCaseJSONObj);
        this.name = name;
    }

    public StateTestCase(JSONObject testCaseJSONObj) throws ParseException {

        try {

            JSONObject envJSON = (JSONObject) testCaseJSONObj.get("env");
            JSONArray logsJSON = (JSONArray) testCaseJSONObj.get("logs");
            String outStr = testCaseJSONObj.get("out").toString();
            JSONObject txJSON = (JSONObject) testCaseJSONObj.get("transaction");

            JSONObject preJSON = (JSONObject) testCaseJSONObj.get("pre");
            JSONObject postJSON = (JSONObject) testCaseJSONObj.get("post");

            this.env = new Env(envJSON);
            this.logs = new Logs(logsJSON);
            this.out = Utils.parseData(outStr);
            this.transaction = new Transaction(txJSON);

            for (Object key : preJSON.keySet()) {

                byte[] keyBytes = Hex.decode(key.toString());
                AccountState accountState =
                        new AccountState(keyBytes, (JSONObject) preJSON.get(key));

                pre.put(new ByteArrayWrapper(keyBytes), accountState);
            }

            for (Object key : postJSON.keySet()) {

                byte[] keyBytes = Hex.decode(key.toString());
                AccountState accountState =
                        new AccountState(keyBytes, (JSONObject) postJSON.get(key));

                post.put(new ByteArrayWrapper(keyBytes), accountState);
            }


        } catch (Throwable e) {
            throw new ParseException(0, e);
        }
    }


    public String getName() {
        return name;
    }

    public Env getEnv() {
        return env;
    }

    public Logs getLogs() {
        return logs;
    }

    public byte[] getOut() {
        return out;
    }

    public Map<ByteArrayWrapper, AccountState> getPre() {
        return pre;
    }

    public Map<ByteArrayWrapper, AccountState> getPost() {
        return post;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    @Override
    public String toString() {
        return "StateTestCase{" +
                "name='" + name + '\'' +
                ", env=" + env +
                ", logs=" + logs +
                ", out=" + Arrays.toString(out) +
                ", pre=" + pre +
                ", post=" + post +
                ", transaction=" + transaction +
                '}';
    }
}
