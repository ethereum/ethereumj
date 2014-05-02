package samples;

import com.maxmind.geoip.LookupService;
import com.maxmind.geoip.Region;
import com.maxmind.geoip.regionName;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import org.ethereum.net.RLP;
import org.ethereum.util.Utils;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;

/**
 * www.openchain.info
 * User: Roman Mandeleil
 * Created on: 31/03/14 21:06
 */
public class Main1 {

    public static void main(String args[]) throws IOException, GeoIp2Exception, URISyntaxException {


        try {

            URL flagURL = ClassLoader.getSystemResource("GeoLiteCity.dat");
            File file = new File(flagURL.toURI());
            LookupService cl = new LookupService(file);
            System.out.println(cl.getLocation("110.77.217.185"));


        } catch (IOException e) {
            System.out.println("IO Exception");
        }


    }


    public static void main1(String args[]) throws IOException {

        //22400891000000088400000043414243

        String helloPacket = "22400891000000088400000043414243";
        String pingPacket = "224008910000000102";

        System.out.println(helloPacket);

        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", 20202));
        socketChannel.configureBlocking(false);

        String helloString = "22 40 08 91 00 00 00 79 F8 77 80 0A 80 AD 45 74 " +
                "68 65 72 65 75 6D 28 2B 2B 29 2F 5A 65 72 6F 47 " +
                "6F 78 2F 76 30 2E 34 2E 31 2F 6E 63 75 72 73 65 " +
                "73 2F 4C 69 6E 75 78 2F 67 2B 2B 07 82 76 5F B8 " +
                "40 D8 D6 0C 25 80 FA 79 5C FC 03 13 EF DE BA 86 " +
                "9D 21 94 E7 9E 7C B2 B5 22 F7 82 FF A0 39 2C BB " +
                "AB 8D 1B AC 30 12 08 B1 37 E0 DE 49 98 33 4F 3B " +
                "CF 73 FA 11 7E F2 13 F8 74 17 08 9F EA F8 4C 21 " +
                "B0 22 40 08 91 00 00 00 02 C1 02 22 40 08 91 00 " +
                "00 00 26 E5 14 A0 AB 6B 9A 56 13 97 0F AA 77 1B " +
                "12 D4 49 B2 E9 BB 92 5A B7 A3 69 F0 A4 B8 6B 28 " +
                "6E 9D 54 00 99 CF 82 01 00 22 40 08 91 00 00 00 " +
                "02 C1 16 22 40 08 91 00 00 00 02 C1 01 ";

        String getPeersString = "22 40 08 91 00 00 00 02 C1 10 ";


        byte[] helloBytes = hexStringToByteArr(helloString);

        // Sending
        ByteBuffer outBuffer = ByteBuffer.allocate(helloBytes.length);

        outBuffer.clear();
        outBuffer.put(helloBytes);
        outBuffer.flip();

        while (outBuffer.hasRemaining()) {
            socketChannel.write(outBuffer);
        }


        outBuffer.clear();
        byte[] getPeersBytes = hexStringToByteArr(getPeersString);

        // Sending
        outBuffer = ByteBuffer.allocate(getPeersBytes.length);

        outBuffer.clear();
        outBuffer.put(getPeersBytes);
        outBuffer.flip();

        while (outBuffer.hasRemaining()) {
            socketChannel.write(outBuffer);
        }


        ByteBuffer inBuffer = ByteBuffer.allocate(1);

        int bytesRead = socketChannel.read(inBuffer); //read into buffer.
        while (bytesRead != -1) {

            inBuffer.flip();  //make buffer ready for read

            while (inBuffer.hasRemaining()) {

                byte oneByte = inBuffer.get();

                System.out.print(Integer.toHexString((int) oneByte & 0x00FF)); // read 1 byte at a time
                System.out.print(" ");
            }


            inBuffer.clear(); //make buffer ready for writing
            bytesRead = socketChannel.read(inBuffer);
        }


        // read 4 bytes sync token 0x22400891
        // read 4 bytes packet size token, translate to size

        // read packet according the size


    }


    public static byte[] hexStringToByteArr(String hexString) {

        String hexSymbols = "0123456789ABCDEF";

        int arrSize = (int) (hexString.length() / 3);
        byte[] result = new byte[arrSize];

        for (int i = 0; i < arrSize; ++i) {

            int digit1 = hexSymbols.indexOf(hexString.charAt(i * 3));
            int digit2 = hexSymbols.indexOf(hexString.charAt(i * 3 + 1));

            result[i] = (byte) (digit1 * 16 + digit2);
        }


        return result;
    }


    public static void main2(String args[]) {

        String helloPacket =
                "F8 77 80 0B 80 AD 45 74 68 65 72 65 75 6D 28 2B 2B 29 " +
                        "2F 5A 65 72 6F 47 6F 78 2F 76 30 2E 34 2E 32 2F " +
                        "6E 63 75 72 73 65 73 2F 4C 69 6E 75 78 2F 67 2B " +
                        "2B 07 82 76 5F B8 40 80 7D 3E D5 E7 7C BA 05 8D " +
                        "C0 55 4A E0 90 98 9E FE EA 55 33 52 B3 1A DF DB " +
                        "80 5E 2A 1A 7D F7 9D 14 FE 8D 9D 2C CE AA D8 E9 " +
                        "4B 09 37 47 F1 33 C3 EE F3 98 83 96 20 1D 24 17 " +
                        "93 83 5D 38 70 FF D4";


        String peersPacket = "F8 4E 11 F8 4B C5 36 81 " +
                "CC 0A 29 82 76 5F B8 40 D8 D6 0C 25 80 FA 79 5C " +
                "FC 03 13 EF DE BA 86 9D 21 94 E7 9E 7C B2 B5 22 " +
                "F7 82 FF A0 39 2C BB AB 8D 1B AC 30 12 08 B1 37 " +
                "E0 DE 49 98 33 4F 3B CF 73 FA 11 7E F2 13 F8 74 " +
                "17 08 9F EA F8 4C 21 B0";


        byte[] payload = Utils.hexStringToByteArr(peersPacket);

        Utils.printHexStringForByteArray(payload);

        Queue<Integer> index = new LinkedList<Integer>();
        RLP.fullTraverse(payload, 0, 0, payload.length, 1, index);

//        for (Integer item : index) System.out.println("ind --> " + item);


//        Message newMessage = MessageFactory.createMessage(payload, index);
//        System.out.println(newMessage.toString());


    }
}
