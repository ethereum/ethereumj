package org.ethereum.net;

import static org.junit.Assert.assertEquals;

import org.ethereum.net.message.HelloMessage;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

public class HelloMessageTest {

    /* HELLO_MESSAGE */

    @Test /* HelloMessage 1 */
    public void test_1() {

        String helloMessageRaw = "F8 77 80 0C 80 AD 45 74 " +
                "68 65 72 65 75 6D 28 2B 2B 29 2F 5A 65 72 6F 47 " +
                "6F 78 2F 76 30 2E 35 2E 31 2F 6E 63 75 72 73 65 " +
                "73 2F 4C 69 6E 75 78 2F 67 2B 2B 07 82 76 5F B8 " +
                "40 D8 83 3B 83 56 0E 0B 12 17 0E 91 69 DC 43 78 " +
                "42 23 A5 98 42 DE 23 59 E6 D0 3D B3 4C 30 A9 66 " +
                "C2 DE 3B 4B 25 52 FB 0D 75 95 A1 85 D5 58 F2 E6 " +
                "69 B5 95 67 4F 52 17 C9 96 EE 14 88 84 82 8B E0 FD";
        byte[] payload = Hex.decode(helloMessageRaw);
        RLPList rlpList = RLP.decode2(payload);

        HelloMessage helloMessage = new HelloMessage(rlpList);
        helloMessage.parseRLP();
        System.out.println(helloMessage);

        assertEquals(12, helloMessage.getP2PVersion());
        assertEquals("Ethereum(++)/ZeroGox/v0.5.1/ncurses/Linux/g++", helloMessage.getClientId());
        assertEquals(7, helloMessage.getCapabilities());
        assertEquals(30303, helloMessage.getListenPort());
        assertEquals(
            "D8833B83560E0B12170E9169DC43784223A59842DE2359E6D03DB34C30A966C2DE3B4B2552FB0D7595A185D558F2E669B595674F5217C996EE148884828BE0FD",
            Hex.toHexString(helloMessage.getPeerId()).toUpperCase() );
    }

    @Test /* HelloMessage 2 */
	public void test_2() {

		String helloMessageRaw = "F87F800B80B5457468657265756D282B2B292F76302E342E332F4554485F4255494C445F545950452F4554485F4255494C445F504C4154464F524D0782765FB840E02B18FBA6B887FB9258469C3AF8E445CC9AE2B5386CAC5F60C4170F822086224E3876555C745A7EC8AC181C7F9701776D94A779604EA12651DE5F4A748D29E1";
        byte[] payload = Hex.decode(helloMessageRaw);
        RLPList rlpList = RLP.decode2(payload);

        HelloMessage helloMessage = new HelloMessage(rlpList);
        helloMessage.parseRLP();
        System.out.println(helloMessage);

        assertEquals(11, helloMessage.getP2PVersion());
        assertEquals("Ethereum(++)/v0.4.3/ETH_BUILD_TYPE/ETH_BUILD_PLATFORM", helloMessage.getClientId());
        assertEquals(7, helloMessage.getCapabilities());
        assertEquals(30303, helloMessage.getListenPort());
        assertEquals(
                "E02B18FBA6B887FB9258469C3AF8E445CC9AE2B5386CAC5F60C4170F822086224E3876555C745A7EC8AC181C7F9701776D94A779604EA12651DE5F4A748D29E1",
                Hex.toHexString(helloMessage.getPeerId()).toUpperCase() );
    }
}

