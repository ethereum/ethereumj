package org.ethereum.jsontestsuite;

import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.util.ByteUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.*;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 28/06/2014 10:22
 */

public class TestCase {

    private String name = "";

    //            "env": { ... },
    private Env env;

    //            "exec": { ... },
    private Exec exec;

    //            "gas": { ... },
    private byte[] gas;

    //            "out": { ... },
    private byte[] out;

    //            "pre": { ... },
    private Map<ByteArrayWrapper, AccountState> pre = new HashMap<>();

    //            "post": { ... },
    private Map<ByteArrayWrapper, AccountState> post = new HashMap<>();

    //            "callcreates": { ... }
    private List<CallCreate> callCreateList = new ArrayList<>();

    public TestCase(String name, JSONObject testCaseJSONObj) throws ParseException{

        this(testCaseJSONObj);
        this.name = name;
    }


    public TestCase(JSONObject testCaseJSONObj) throws ParseException{

        try {

            JSONObject envJSON = (JSONObject)testCaseJSONObj.get("env");
            JSONObject execJSON = (JSONObject)testCaseJSONObj.get("exec");
            JSONObject preJSON = (JSONObject)testCaseJSONObj.get("pre");
            JSONObject postJSON = (JSONObject)testCaseJSONObj.get("post");
            JSONArray  callCreates = (JSONArray)testCaseJSONObj.get("callcreates");

            Long  gasNum = (Long)testCaseJSONObj.get("gas");
            this.gas      = ByteUtil.bigIntegerToBytes(BigInteger.valueOf(gasNum));

            this.out    = Helper.parseDataArray((JSONArray) testCaseJSONObj.get("out"));

            for (Object key : preJSON.keySet()){

                byte[] keyBytes = Hex.decode(key.toString());
                AccountState accountState =
                        new AccountState(keyBytes, (JSONObject)  preJSON.get(key));

                pre.put(new ByteArrayWrapper(keyBytes), accountState);
            }

            for (Object key : postJSON.keySet()){

                byte[] keyBytes = Hex.decode(key.toString());
                AccountState accountState =
                        new AccountState(keyBytes, (JSONObject)  postJSON.get(key));

                post.put(new ByteArrayWrapper(keyBytes), accountState);
            }

            for (int i = 0; i < callCreates.size(); ++i){

                CallCreate cc = new CallCreate((JSONObject)callCreates.get(i));
                this.callCreateList.add(cc);
            }

            this.env  = new Env(envJSON);
            this.exec = new Exec(execJSON);

        } catch (Throwable e) {
            throw  new ParseException(0, e);
        }
    }

    public Env getEnv() {
        return env;
    }

    public Exec getExec() {
        return exec;
    }

    public byte[] getGas() {
        return gas;
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

    public List<CallCreate> getCallCreateList() {
        return callCreateList;
    }

    public String getName(){
        return name;
    }

    @Override
    public String toString() {
        return "TestCase{" +
                "" + env +
                ", " + exec +
                ", gas=" + Hex.toHexString(gas) +
                ", out=" + Hex.toHexString(out) +
                ", pre=" + pre +
                ", post=" + post +
                ", callcreates=" + callCreateList +
                '}';
    }
}
