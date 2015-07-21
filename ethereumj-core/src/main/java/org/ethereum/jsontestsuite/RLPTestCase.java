package org.ethereum.jsontestsuite;


import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPList;
import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class RLPTestCase {
    private static Logger logger = LoggerFactory.getLogger("rlp");

    private Object in;
    private String out;

    private List<String> computed = new ArrayList<>();
    private List<String> expected = new ArrayList<>();

    public Object getIn() {
        return in;
    }

    public void setIn(Object in) {
        this.in = in;
    }

    public String getOut() {
        return out;
    }

    public void setOut(String out) {
        this.out = out;
    }

    public List<String> getComputed() {
        return computed;
    }

    public List<String> getExpected() {
        return expected;
    }

    public void doEncode() {
        byte[] in = buildRLP(this.in);
        String expected = this.out.toLowerCase();
        String computed = Hex.toHexString(in);
        this.computed.add(computed);
        this.expected.add(expected);
    }

    public void doDecode() {
        String out = this.out.toLowerCase();
        RLPList list = RLP.decode2(Hex.decode(out));
        checkRLPAgainstJson(list.get(0), in);
    }

    public byte[] buildRLP(Object in) {
        if (in instanceof ArrayList) {
            List<byte[]> elementList = new Vector<>();
            for (Object o : ((ArrayList) in).toArray()) {
                elementList.add(buildRLP(o));
            }
            byte[][] elements = elementList.toArray(new byte[elementList.size()][]);
            return RLP.encodeList(elements);
        } else {
            if (in instanceof String) {
                String s = in.toString();
                if (s.contains("#")) {
                    return RLP.encode(new BigInteger(s.substring(1)));
                }
            } else if (in instanceof Integer) {
                return RLP.encodeInt(Integer.parseInt(in.toString()));
            }
            return RLP.encode(in);
        }
    }

    public void checkRLPAgainstJson(RLPElement element, Object in) {
        if (in instanceof JSONArray) {
            Object[] array = ((JSONArray) in).toArray();
            RLPList list = (RLPList) element;
            for (int i = 0; i < array.length; i++) {
                checkRLPAgainstJson(list.get(i), array[i]);
            }
        } else if (in instanceof Long) {
            int computed = ByteUtil.byteArrayToInt(element.getRLPData());
            this.computed.add(Integer.toString(computed));
            this.expected.add(in.toString());
        } else if (in instanceof String) {
            String s = in.toString();
            if (s.contains("#")) {
                s = s.substring(1);
                BigInteger expected = new BigInteger(s);
                byte[] payload = Hex.decode(element.getRLPData());
                BigInteger computed = RLP.decodeBigInteger(payload, 0);
                this.computed.add(computed.toString());
                this.expected.add(expected.toString());
            } else {
                String expected = null;
                try {
                    expected = new String(element.getRLPData(), "UTF-8");
                } catch (Exception e) {}
                this.expected.add(expected);
                this.computed.add(s);
            }
        }
    }
}
