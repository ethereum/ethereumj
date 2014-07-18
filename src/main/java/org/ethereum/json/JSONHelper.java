package org.ethereum.json;

import org.ethereum.util.RLP;
import org.ethereum.vm.DataWord;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.*;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 26/06/2014 10:08
 */
public class JSONHelper {

    public static String dumpLine(byte[] address, byte[] nonce, byte[] balance, byte[] stateRoot,
                        byte[] codeHash, byte[] code, Map<DataWord, DataWord> storageMap) {

//            {address: x, nonce: n1, balance: b1, stateRoot: s1, codeHash: c1, code: c2, sotrage: [key: k1, value: v1, key:k2, value: v2 ] }

        List<DataWord> storageKeys = new ArrayList<DataWord>(storageMap.keySet());
		Collections.sort((List<DataWord>) storageKeys);

        Map<String, String> outMap = new LinkedHashMap<String, String>();

        for (DataWord key : storageKeys) {
            outMap.put(Hex.toHexString(key.getData()),
                       Hex.toHexString(
                               RLP.encodeElement( storageMap.get(key).getNoLeadZeroesData() )
                       ));
        }

        String mapString =  JSONValue.toJSONString(outMap);
        mapString = mapString.replace("\"", "");

        JSONArray orderFields  = new JSONArray();
        orderFields.add("address: "    + Hex.toHexString(address));
        orderFields.add(" nonce: "     + (nonce == null ? "00" : Hex.toHexString(nonce)));
        orderFields.add(" balance: "   + (balance == null ? "00" : Hex.toHexString(balance).toString()));
        orderFields.add(" stateRoot: " + (stateRoot == null ? "" : Hex.toHexString(stateRoot)));
        orderFields.add(" codeHash: "  + (codeHash == null ? "" : Hex.toHexString(codeHash)));
        orderFields.add(" code: "      + (code == null ? "" : Hex.toHexString(code)));
        orderFields.add(" storage: "   + mapString);

        String out = orderFields.toString();
        out = out.replace("\"", "");

        return out;
    }
}








