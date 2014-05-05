package org.ethereum.net;

/**
 * www.openchain.info
 * User: Roman Mandeleil
 * Created on: 04/04/14 00:51
 */
public class MessageDeserializer {
    /**
     * Get exactly one message payload
     */
    public static void deserialize(byte [] msgData, int level, int startPos, int endPos){

        if (msgData == null || msgData.length == 0) return ;
        int pos = startPos;

        while(pos < endPos){
            // It's a list with a payload more than 55 bytes
            // data[0] - 0xF7 = how many next bytes allocated
            // for  the length of the list
        	if ((msgData[pos] & 0xFF) >= 0xF7){
                byte lengthOfLength = (byte) (msgData[pos] -  0xF7);
                int length = calcLength(lengthOfLength, msgData, pos);
                // now we can parse an item for data[1]..data[length]
                System.out.println("-- level: [" + level + "] Found big list length: " + length);
                deserialize(msgData, level + 1, pos + lengthOfLength + 1, pos + lengthOfLength +  length);
                pos += lengthOfLength + length + 1 ;
                continue;
            }
            // It's a list with a payload less than 55 bytes
            if ((msgData[pos] & 0xFF) >= 0xC0 && (msgData[pos] & 0xFF) < 0xF7){
                byte length = (byte) (msgData[pos] -  0xC0);
                System.out.println("-- level: [" + level + "] Found small list length: " + length);
                deserialize(msgData, level + 1, pos + 1, pos + length + 1);
                pos += 1 + length;
                continue;
            }
            //  It's an item  with a payload more than 55 bytes
            //  data[0] - 0xB7 = how much next bytes allocated for
            //  the length of the string
            if ((msgData[pos] & 0xFF) >= 0xB7 && (msgData[pos] & 0xFF) < 0xC0) {
                byte lengthOfLength = (byte) (msgData[pos] -  0xB7);
                int length = calcLength(lengthOfLength, msgData, pos);
                // now we can parse an item for data[1]..data[length]
                System.out.println("-- level: [" + level + "] Found big item length: " + length);
                pos += lengthOfLength + length + 1 ;
                continue;
            }
            // It's an item less than 55 bytes long,
            // data[0] - 0x80 == lenght of the item
            if ((msgData[pos] & 0xFF) > 0x80 && (msgData[pos] & 0xFF) < 0xB7) {
                byte length = (byte) (msgData[pos] -  0x80);
                System.out.println("-- level: [" + level + "] Found small item length: " + length);
                pos += 1 + length;
                continue;
            }
            //  null item
            if ((msgData[pos] & 0xFF) == 0x80){
                System.out.println("-- level: [" + level + "] Found null item: ");
                pos += 1;
                continue;
            }
            //  single byte item
            if ((msgData[pos] & 0xFF) < 0x80)  {
                System.out.println("-- level: [" + level + "] Found single item: ");
                pos += 1;
                continue;
            }
        }
    }
    
    private static int calcLength(int lengthOfLength, byte[] msgData, int pos) {
    	byte pow = (byte) (lengthOfLength - 1);
        int length = 0;
        for (int i = 1; i <= lengthOfLength; ++i){
            length += msgData[pos + i] << (8 *  pow);
            pow--;
        }
        return length;
    }
}
