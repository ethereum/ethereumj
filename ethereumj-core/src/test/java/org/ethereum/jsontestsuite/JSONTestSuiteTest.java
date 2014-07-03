package org.ethereum.jsontestsuite;

import org.ethereum.db.ByteArrayWrapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.List;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 29/06/2014 10:46
 */

public class JSONTestSuiteTest {



    @Test // AccountState parsing  //
    public void test1() throws ParseException {

        String expectedNonce  = "01";
        String expectedBalance = "0de0b6b3a763ff6c";
        String expectedCode    = "6000600060006000604a3360c85c03f1";

        ByteArrayWrapper expectedKey1 = new ByteArrayWrapper( Hex.decode("ffaa") );
        ByteArrayWrapper expectedKey2 = new ByteArrayWrapper( Hex.decode("ffab") );

        ByteArrayWrapper expectedVal1 = new ByteArrayWrapper( Hex.decode("c8") );
        ByteArrayWrapper expectedVal2 = new ByteArrayWrapper( Hex.decode("b2b2b2") );

        JSONParser parser = new JSONParser();
        String accountString = "{'balance':999999999999999852,'nonce':1," +
                "'code':[96,0,96,0,96,0,96,0,96,74,51,96,200,92,3,241]," +
                "'storage':{'0xffaa' : [200], '0xffab' : ['0xb2b2b2']}}";
        accountString = accountString.replace("'", "\"");

        JSONObject accountJSONObj = (JSONObject)parser.parse(accountString);

        AccountState state =
                new AccountState(Hex.decode("0f572e5295c57f15886f9b263e2f6d2d6c7b5ec6"),
                        accountJSONObj);

        Assert.assertEquals(expectedNonce, Hex.toHexString(state.nonce));
        Assert.assertEquals(expectedBalance, Hex.toHexString(state.balance));
        Assert.assertEquals(expectedCode, Hex.toHexString(state.code));

        Assert.assertTrue( state.storage.keySet().contains(expectedKey1) );
        Assert.assertTrue(state.storage.keySet().contains(expectedKey2));

        Assert.assertTrue( state.storage.values().contains(expectedVal1) );
        Assert.assertTrue(state.storage.values().contains(expectedVal2));
    }



    @Test // Exec parsing  //
    public void test2() throws ParseException {

        String expectedAddress = "0f572e5295c57f15886f9b263e2f6d2d6c7b5ec6";
        String expectedCaller   = "cd1722f3947def4cf144679da39c4c32bdc35681";
        String expectedData     = "ffaabb";
        String expectedCode     = "6000600060006000604a3360c85c03f1";
        String expectedGas      = "2710";
        String expectedGasPrice = "5af3107a4000";
        String expectedOrigin   = "cd1722f3947def4cf144679da39c4c32bdc35681";
        String expectedValue    = "0de0b6b3a7640000";

        JSONParser parser = new JSONParser();
        String execString = "{'address' : '0f572e5295c57f15886f9b263e2f6d2d6c7b5ec6'," +
                "             'caller' : 'cd1722f3947def4cf144679da39c4c32bdc35681'," +
                "             'data' : ['0xffaabb']," +
                "             'code' : [96,0,96,0,96,0,96,0,96,74,51,96,200,92,3,241]," +
                "             'gas' : 10000," +
                "             'gasPrice' : 100000000000000," +
                "             'origin' : 'cd1722f3947def4cf144679da39c4c32bdc35681'," +
                "             'value' : 1000000000000000000}";
        execString = execString.replace("'", "\"");

        JSONObject execJSONObj = (JSONObject)parser.parse(execString);
        Exec exec = new Exec(execJSONObj);

        Assert.assertEquals(expectedAddress, Hex.toHexString(exec.getAddress()));
        Assert.assertEquals(expectedCaller, Hex.toHexString(exec.getCaller()));
        Assert.assertEquals(expectedData, Hex.toHexString(exec.getData()));
        Assert.assertEquals(expectedCode, Hex.toHexString(exec.getCode()));
        Assert.assertEquals(expectedGas, Hex.toHexString(exec.getGas()));
        Assert.assertEquals(expectedGasPrice, Hex.toHexString(exec.getGasPrice()));
        Assert.assertEquals(expectedOrigin, Hex.toHexString(exec.getOrigin()));
        Assert.assertEquals(expectedValue, Hex.toHexString(exec.getValue()));
    }



    @Test // Env parsing  //
    public void test3() throws ParseException {

        String expectedCurrentCoinbase   =   "2adc25665018aa1fe0e6bc666dac8fc2697ff9ba";
        String expectedCurrentDifficulty =   "256";
        String expectedCurrentGasLimit   =   "1000000";
        String expectedCurrentNumber     =   "0";
        String expectedCurrentTimestamp  =    "1";
        String expectedPreviousHash      =    "5e20a0453cecd065ea59c37ac63e079ee08998b6045136a8ce6635c7912ec0b6";

        JSONParser parser = new JSONParser();
        String envString = "{'currentCoinbase' : '2adc25665018aa1fe0e6bc666dac8fc2697ff9ba'," +
                "'currentDifficulty' : '256'," +
                "'currentGasLimit' : '1000000'," +
                "'currentNumber' : '0'," +
                "'currentTimestamp' : 1," +
                "'previousHash' : '5e20a0453cecd065ea59c37ac63e079ee08998b6045136a8ce6635c7912ec0b6'}";
        envString = envString.replace("'", "\"");

        JSONObject envJSONObj = (JSONObject)parser.parse(envString);

        Env env = new Env(envJSONObj);

        Assert.assertEquals(expectedCurrentCoinbase,    Hex.toHexString(env.getCurrentCoinbase()));
        Assert.assertEquals(expectedCurrentDifficulty, new BigInteger( env.getCurrentDifficlty()).toString());
        Assert.assertEquals(expectedCurrentGasLimit,   new BigInteger( env.getCurrentGasLimit()).toString());
        Assert.assertEquals(expectedCurrentNumber,     new BigInteger( env.getCurrentNumber()).toString());
        Assert.assertEquals(expectedCurrentTimestamp,  new BigInteger( env.getCurrentTimestamp()).toString());
        Assert.assertEquals(expectedPreviousHash,      Hex.toHexString(env.getPreviousHash()));

    }


    @Test // CallCreate parsing  //
    public void test4() throws ParseException {

        String expectedData         =   "";
        String expectedDestination =   "cd1722f3947def4cf144679da39c4c32bdc35681";
        String expectedGasLimit    =   "9792";
        String expectedValue       =   "74";

        JSONParser parser = new JSONParser();
        String callCreateString = "{'data' : [],'destination' : 'cd1722f3947def4cf144679da39c4c32bdc35681','gasLimit' : 9792,'value' : 74}";
        callCreateString = callCreateString.replace("'", "\"");

        JSONObject callCreateJSONObj = (JSONObject)parser.parse(callCreateString);

        CallCreate callCreate = new CallCreate(callCreateJSONObj);

        Assert.assertEquals(expectedData,         Hex.toHexString(callCreate.getData()));
        Assert.assertEquals(expectedDestination,  Hex.toHexString(callCreate.getDestination()));
        Assert.assertEquals(expectedGasLimit,     new BigInteger( callCreate.getGasLimit()).toString());
        Assert.assertEquals(expectedValue,        new BigInteger( callCreate.getValue()).toString());
    }

    @Test // TestCase parsing  //
    public void test5() throws ParseException {

        JSONParser parser = new JSONParser();
//        String testCaseString = "{'callcreates':[{'data':[],'destination':'cd1722f3947def4cf144679da39c4c32bdc35681','gasLimit':9792,'value':74}],'env':{'currentCoinbase':'2adc25665018aa1fe0e6bc666dac8fc2697ff9ba','currentDifficulty':'256','currentGasLimit':'1000000','currentNumber':'0','currentTimestamp':1,'previousHash':'5e20a0453cecd065ea59c37ac63e079ee08998b6045136a8ce6635c7912ec0b6'},'exec':{'address':'0f572e5295c57f15886f9b263e2f6d2d6c7b5ec6','caller':'cd1722f3947def4cf144679da39c4c32bdc35681','code':[96,0,96,0,96,0,96,0,96,74,51,96,200,92,3,241],'data':[],'gas':10000,'gasPrice':100000000000000,'origin':'cd1722f3947def4cf144679da39c4c32bdc35681','value':1000000000000000000},'gas':9971,'out':[],'post':{'0f572e5295c57f15886f9b263e2f6d2d6c7b5ec6':{'balance':999999999999999926,'code':[96,0,96,0,96,0,96,0,96,74,51,96,200,92,3,241],'nonce':0,'storage':{}}},'pre':{'0f572e5295c57f15886f9b263e2f6d2d6c7b5ec6':{'balance':1000000000000000000,'code':[96,0,96,0,96,0,96,0,96,74,51,96,200,92,3,241],'nonce':0,'storage':{}}}}";
        String testCaseString = "{'callcreates':[{'data':[],'destination':'cd1722f3947def4cf144679da39c4c32bdc35681','gasLimit':200,'value':74}],'env':{'currentCoinbase':'2adc25665018aa1fe0e6bc666dac8fc2697ff9ba','currentDifficulty':'256','currentGasLimit':'1000000','currentNumber':'0','currentTimestamp':1,'previousHash':'5e20a0453cecd065ea59c37ac63e079ee08998b6045136a8ce6635c7912ec0b6'},'exec':{'address':'0f572e5295c57f15886f9b263e2f6d2d6c7b5ec6','caller':'cd1722f3947def4cf144679da39c4c32bdc35681','code':['0x6000600060006000604a3360c8f1'],'data':[],'gas':10000,'gasPrice':100000000000000,'origin':'cd1722f3947def4cf144679da39c4c32bdc35681','value':1000000000000000000},'gas':9773,'out':[],'post':{'0f572e5295c57f15886f9b263e2f6d2d6c7b5ec6':{'balance':999999999999999926,'code':[],'nonce':0,'storage':{}}},'pre':{'0f572e5295c57f15886f9b263e2f6d2d6c7b5ec6':{'balance':1000000000000000000,'code':[],'nonce':0,'storage':{}}}}";
        testCaseString = testCaseString.replace("'", "\"");

        JSONObject testCaseJSONObj = (JSONObject)parser.parse(testCaseString);

        TestCase testCase = new TestCase(testCaseJSONObj);
        int ccList = testCase.getCallCreateList().size();

        Assert.assertEquals(1, ccList);

        TestRunner runner = new TestRunner();
        List<String> result = runner.runTestCase(testCase);

        Assert.assertTrue(result.size() == 0);

    }


}
