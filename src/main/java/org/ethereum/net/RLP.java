package org.ethereum.net;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

import org.ethereum.net.rlp.RLPItem;
import org.ethereum.net.rlp.RLPList;

/**

 */
public class RLP {

	public static byte decodeOneByteItem(byte[] data, int index) {

		// null item
		if ((data[index] & 0xFF) == 0x80) {
			return (byte) (data[index] - 0x80);
		}
		// single byte item
		if ((data[index] & 0xFF) < 0x80) {
			return (byte) (data[index]);
		}
		// single byte item
		if ((data[index] & 0xFF) == 0x81) {
			return (byte) (data[index + 1]);
		}
		return 0;
	}

	public static String decodeStringItem(byte[] data, int index) {

		String value = null;

		if ((data[index] & 0xFF) >= 0xB7 && (data[index] & 0xFF) < 0xC0) {

			byte lenghtOfLenght = (byte) (data[index] - 0xB7);
			byte pow = (byte) (lenghtOfLenght - 1);

			int length = 0;

			for (int i = 1; i <= lenghtOfLenght; ++i) {

				length += data[index + i] << (8 * pow);
				pow--;
			}

			value = new String(data, index + lenghtOfLenght + 1, length);

		} else if ((data[index] & 0xFF) > 0x80 && (data[index] & 0xFF) < 0xB7) {

			byte length = (byte) ((data[index] & 0xFF) - 0x80);

			value = new String(data, index + 1, length);

		} else {
			throw new Error("wrong decode attempt");
		}

		return value;
	}

	public static int decodeInt(byte[] data, int index) {

		int value = 0;

		if ((data[index] & 0xFF) > 0x80 && (data[index] & 0xFF) < 0xB7) {

			byte length = (byte) (data[index] - 0x80);
			byte pow = (byte) (length - 1);

			for (int i = 1; i <= length; ++i) {
				value += data[index + i] << (8 * pow);
				pow--;
			}
		} else {
			throw new Error("wrong decode attempt");
		}
		return value;
	}

	public static short decodeShort(byte[] data, int index) {

		short value = 0;

		if ((data[index] & 0xFF) > 0x80 && (data[index] & 0xFF) < 0xB7) {
			byte length = (byte) (data[index] - 0x80);
			value = ByteBuffer.wrap(data, index, length).getShort();
		} else {
			value = data[index];
		}
		return value;
	}

	public static short decodeLong(byte[] data, int index) {

		short value = 0;

		if ((data[index] & 0xFF) > 0x80 && (data[index] & 0xFF) < 0xB7) {

			byte length = (byte) (data[index] - 0x80);
			byte pow = (byte) (length - 1);

			for (int i = 1; i <= length; ++i) {

				value += data[index + i] << (8 * pow);
				pow--;
			}
		} else {
			throw new Error("wrong decode attempt");
		}

		return value;
	}

	public static byte[] decodeItemBytes(byte[] data, int index) {

		byte[] value = null;

		if ((data[index] & 0xFF) >= 0xB7 && (data[index] & 0xFF) < 0xC0) {

			byte lenghtOfLenght = (byte) (data[index] - 0xB7);
			byte pow = (byte) (lenghtOfLenght - 1);

			int length = 0;

			for (int i = 1; i <= lenghtOfLenght; ++i) {

				length += data[index + i] << (8 * pow);
				pow--;
			}

			byte[] valueBytes = new byte[length];
			System.arraycopy(data, index, valueBytes, 0, length);

			value = valueBytes;

		} else if ((data[index] & 0xFF) > 0x80 && (data[index] & 0xFF) < 0xB7) {

			byte length = (byte) (data[index] - 0x80);

			byte[] valueBytes = new byte[length];
			System.arraycopy(data, index, valueBytes, 0, length);

			value = valueBytes;

		} else {

			throw new Error("wrong decode attempt");

		}

		return value;
	}

	public static BigInteger decodeBigInteger(byte[] data, int index) {

		BigInteger value = null;

		if ((data[index] & 0xFF) >= 0xB7 && (data[index] & 0xFF) < 0xC0) {

			byte lenghtOfLenght = (byte) (data[index] - 0xB7);
			byte pow = (byte) (lenghtOfLenght - 1);

			int length = 0;

			for (int i = 1; i <= lenghtOfLenght; ++i) {

				length += data[index + i] << (8 * pow);
				pow--;
			}

			byte[] valueBytes = new byte[length];
			System.arraycopy(data, index, valueBytes, 0, length);

			value = new BigInteger(valueBytes);

		} else if ((data[index] & 0xFF) > 0x80 && (data[index] & 0xFF) < 0xB7) {

			byte length = (byte) (data[index] - 0x80);

			byte[] valueBytes = new byte[length];
			System.arraycopy(data, index, valueBytes, 0, length);

			value = new BigInteger(valueBytes);

		} else {

			throw new Error("wrong decode attempt");

		}

		return value;
	}

	public static byte[] decodeByteArray(byte[] data, int index) {

		byte[] value = null;

		if ((data[index] & 0xFF) >= 0xB7 && (data[index] & 0xFF) < 0xC0) {

			byte lenghtOfLenght = (byte) (data[index] - 0xB7);
			byte pow = (byte) (lenghtOfLenght - 1);

			int length = 0;

			for (int i = 1; i <= lenghtOfLenght; ++i) {

				length += data[index + i] << (8 * pow);
				pow--;
			}

			byte[] valueBytes = new byte[length];
			System.arraycopy(data, index, valueBytes, 0, length);

			value = valueBytes;

		} else if ((data[index] & 0xFF) > 0x80 && (data[index] & 0xFF) < 0xB7) {

			byte length = (byte) (data[index] - 0x80);

			byte[] valueBytes = new byte[length];
			System.arraycopy(data, index, valueBytes, 0, length);

			value = valueBytes;

		} else {

			throw new Error("wrong decode attempt");

		}

		return value;
	}

	public static int nextItemLength(byte[] data, int index) {

		if (index >= data.length)
			return -1;

		if ((data[index] & 0xFF) >= 0xF7) {
			byte lenghtOfLenght = (byte) (data[index] - 0xF7);
			byte pow = (byte) (lenghtOfLenght - 1);

			int length = 0;

			for (int i = 1; i <= lenghtOfLenght; ++i) {

				length += (data[index + i] & 0xFF) << (8 * pow);
				pow--;
			}

			return length;

		}

		if ((data[index] & 0xFF) >= 0xC0 && (data[index] & 0xFF) < 0xF7) {

			byte length = (byte) ((data[index] & 0xFF) - 0xC0);
			return length;
		}

		if ((data[index] & 0xFF) >= 0xB7 && (data[index] & 0xFF) < 0xC0) {

			byte lenghtOfLenght = (byte) (data[index] - 0xB7);
			byte pow = (byte) (lenghtOfLenght - 1);

			int length = 0;

			for (int i = 1; i <= lenghtOfLenght; ++i) {

				length += (data[index + i] & 0xFF) << (8 * pow);
				pow--;
			}

			return length;

		}

		if ((data[index] & 0xFF) > 0x80 && (data[index] & 0xFF) < 0xB7) {

			byte length = (byte) ((data[index] & 0xFF) - 0x80);
			return length;
		}

		if ((data[index] & 0xFF) == 0x80) {

			return 1;
		}

		if ((data[index] & 0xFF) < 0x80) {

			return 1;
		}

		return -1;
	}

	public static byte[] decodeIP4Bytes(byte[] data, int index) {

		int length = (data[index] & 0xFF) - 0xC0;
		int offset = 1;

		byte aByte = decodeOneByteItem(data, index + offset);

		if ((data[index + offset] & 0xFF) > 0x80)
			offset = offset + 2;
		else
			offset = offset + 1;
		byte bByte = decodeOneByteItem(data, index + offset);

		if ((data[index + offset] & 0xFF) > 0x80)
			offset = offset + 2;
		else
			offset = offset + 1;
		byte cByte = decodeOneByteItem(data, index + offset);

		if ((data[index + offset] & 0xFF) > 0x80)
			offset = offset + 2;
		else
			offset = offset + 1;
		byte dByte = decodeOneByteItem(data, index + offset);

		byte[] ip = new byte[4];
		ip[0] = aByte;
		ip[1] = bByte;
		ip[2] = cByte;
		ip[3] = dByte;

		return ip;
	}

	public static int getFirstListElement(byte[] payload, int pos) {

		if (pos >= payload.length)
			return -1;

		if ((payload[pos] & 0xFF) >= 0xF7) {

			byte lenghtOfLenght = (byte) (payload[pos] - 0xF7);

			return pos + lenghtOfLenght + 1;
		}

		if ((payload[pos] & 0xFF) >= 0xC0 && (payload[pos] & 0xFF) < 0xF7) {

			byte length = (byte) ((payload[pos] & 0xFF) - 0xC0);
			return pos + 1;
		}

		if ((payload[pos] & 0xFF) >= 0xB7 && (payload[pos] & 0xFF) < 0xC0) {

			byte lenghtOfLenght = (byte) (payload[pos] - 0xB7);
			byte pow = (byte) (lenghtOfLenght - 1);

			int length = 0;

			for (int i = 1; i <= lenghtOfLenght; ++i) {

				length += (payload[pos + i] & 0xFF) << (8 * pow);
				pow--;
			}

			return pos + lenghtOfLenght + 1;
		}

		return -1;
	}

	public static int getNextElementIndex(byte[] payload, int pos) {

		if (pos >= payload.length)
			return -1;

		if ((payload[pos] & 0xFF) >= 0xF7) {
			byte lenghtOfLength = (byte) (payload[pos] - 0xF7);
			byte pow = (byte) (lenghtOfLength - 1);

			int length = 0;

			for (int i = 1; i <= lenghtOfLength; ++i) {

				length += (payload[pos + i] & 0xFF) << (8 * pow);
				pow--;
			}

			return pos + lenghtOfLength + length + 1;
		}

		if ((payload[pos] & 0xFF) >= 0xC0 && (payload[pos] & 0xFF) < 0xF7) {

			byte length = (byte) ((payload[pos] & 0xFF) - 0xC0);
			return pos + 1 + length;
		}

		if ((payload[pos] & 0xFF) >= 0xB7 && (payload[pos] & 0xFF) < 0xC0) {

			byte lenghtOfLenght = (byte) (payload[pos] - 0xB7);
			byte pow = (byte) (lenghtOfLenght - 1);

			int length = 0;

			for (int i = 1; i <= lenghtOfLenght; ++i) {

				length += (payload[pos + i] & 0xFF) << (8 * pow);
				pow--;
			}

			return pos + lenghtOfLenght + length + 1;
		}

		if ((payload[pos] & 0xFF) > 0x80 && (payload[pos] & 0xFF) < 0xB7) {

			byte length = (byte) ((payload[pos] & 0xFF) - 0x80);
			return pos + 1 + length;
		}

		if ((payload[pos] & 0xFF) == 0x80) {

			return pos + 1;
		}

		if ((payload[pos] & 0xFF) < 0x80) {

			return pos + 1;
		}

		return -1;
	}

	/**
	 * Get exactly one message payload
	 */
	public static void fullTraverse(byte[] msgData, int level, int startPos,
			int endPos, int levelToIndex, Queue<Integer> index) {

		try {

			if (msgData == null || msgData.length == 0)
				return;
			int pos = startPos;

			while (pos < endPos) {

				if (level == levelToIndex)
					index.add(new Integer(pos));

				// It's a list with a payload more than 55 bytes
				// data[0] - 0xF7 = how many next bytes allocated
				// for the length of the list
				if ((msgData[pos] & 0xFF) >= 0xF7) {

					byte lenghtOfLenght = (byte) (msgData[pos] - 0xF7);
					byte pow = (byte) (lenghtOfLenght - 1);

					int length = 0;

					for (int i = 1; i <= lenghtOfLenght; ++i) {

						length += (msgData[pos + i] & 0xFF) << (8 * pow);
						pow--;
					}

					// now we can parse an item for data[1]..data[length]
					System.out.println("-- level: [" + level
							+ "] Found big list length: " + length);

					fullTraverse(msgData, level + 1, pos + lenghtOfLenght + 1,
							pos + lenghtOfLenght + length, levelToIndex, index);

					pos += lenghtOfLenght + length + 1;
					continue;
				}

				// It's a list with a payload less than 55 bytes
				if ((msgData[pos] & 0xFF) >= 0xC0
						&& (msgData[pos] & 0xFF) < 0xF7) {

					byte length = (byte) ((msgData[pos] & 0xFF) - 0xC0);

					System.out.println("-- level: [" + level
							+ "] Found small list length: " + length);

					fullTraverse(msgData, level + 1, pos + 1, pos + length + 1,
							levelToIndex, index);

					pos += 1 + length;
					continue;
				}

				// It's an item with a payload more than 55 bytes
				// data[0] - 0xB7 = how much next bytes allocated for
				// the length of the string
				if ((msgData[pos] & 0xFF) >= 0xB7
						&& (msgData[pos] & 0xFF) < 0xC0) {

					byte lenghtOfLenght = (byte) (msgData[pos] - 0xB7);
					byte pow = (byte) (lenghtOfLenght - 1);

					int length = 0;

					for (int i = 1; i <= lenghtOfLenght; ++i) {

						length += (msgData[pos + i] & 0xFF) << (8 * pow);
						pow--;
					}

					// now we can parse an item for data[1]..data[length]
					System.out.println("-- level: [" + level
							+ "] Found big item length: " + length);
					pos += lenghtOfLenght + length + 1;

					continue;
				}

				// It's an item less than 55 bytes long,
				// data[0] - 0x80 == lenght of the item
				if ((msgData[pos] & 0xFF) > 0x80
						&& (msgData[pos] & 0xFF) < 0xB7) {

					byte length = (byte) ((msgData[pos] & 0xFF) - 0x80);

					System.out.println("-- level: [" + level
							+ "] Found small item length: " + length);
					pos += 1 + length;

					continue;
				}

				// null item
				if ((msgData[pos] & 0xFF) == 0x80) {
					System.out.println("-- level: [" + level
							+ "] Found null item: ");
					pos += 1;
					continue;
				}

				// single byte item
				if ((msgData[pos] & 0xFF) < 0x80) {
					System.out.println("-- level: [" + level
							+ "] Found single item: ");
					pos += 1;
					continue;
				}
			}
		} catch (Throwable th) {
			throw new Error("wire packet not parsed correctly",
					th.fillInStackTrace());
		}
	}

	/**
	 * Parse wire byte[] message into RLP elements
	 * 
	 * @param msgData
	 *            - raw RLP data
	 * @param rlpList
	 *            - outcome of recursive RLP structure
	 */
	public static void parseObjects(byte[] msgData, RLPList rlpList) {
		RLP.fullTraverse2(msgData, 0, 0, msgData.length, 1, rlpList);
	}

	/**
	 * Get exactly one message payload
	 */
	public static void fullTraverse2(byte[] msgData, int level, int startPos,
			int endPos, int levelToIndex, RLPList rlpList) {

		try {

			if (msgData == null || msgData.length == 0)
				return;
			int pos = startPos;

			while (pos < endPos) {

				// It's a list with a payload more than 55 bytes
				// data[0] - 0xF7 = how many next bytes allocated
				// for the length of the list
				if ((msgData[pos] & 0xFF) >= 0xF7) {

					byte lenghtOfLenght = (byte) (msgData[pos] - 0xF7);
					byte pow = (byte) (lenghtOfLenght - 1);

					int length = 0;

					for (int i = 1; i <= lenghtOfLenght; ++i) {

						length += (msgData[pos + i] & 0xFF) << (8 * pow);
						pow--;
					}

					byte[] rlpData = new byte[lenghtOfLenght + length + 1];
					System.arraycopy(msgData, pos, rlpData, 0, lenghtOfLenght
							+ length + 1);

					RLPList newLevelList = new RLPList();
					newLevelList.setRLPData(rlpData);

					// todo: this done to get some data for testing should be
					// delete
					// byte[] subList = Arrays.copyOfRange(msgData, pos, pos +
					// lenghtOfLenght + length + 1);
					// System.out.println(Utils.toHexString(subList));

					fullTraverse2(msgData, level + 1, pos + lenghtOfLenght + 1,
							pos + lenghtOfLenght + length + 1, levelToIndex,
							newLevelList);
					rlpList.addItem(newLevelList);

					pos += lenghtOfLenght + length + 1;
					continue;
				}

				// It's a list with a payload less than 55 bytes
				if ((msgData[pos] & 0xFF) >= 0xC0
						&& (msgData[pos] & 0xFF) < 0xF7) {

					byte length = (byte) ((msgData[pos] & 0xFF) - 0xC0);

					byte[] rlpData = new byte[length + 1];
					System.arraycopy(msgData, pos, rlpData, 0, length + 1);

					RLPList newLevelList = new RLPList();
					newLevelList.setRLPData(rlpData);

					if (length > 0)
						fullTraverse2(msgData, level + 1, pos + 1, pos + length
								+ 1, levelToIndex, newLevelList);
					rlpList.addItem(newLevelList);

					pos += 1 + length;
					continue;
				}

				// It's an item with a payload more than 55 bytes
				// data[0] - 0xB7 = how much next bytes allocated for
				// the length of the string
				if ((msgData[pos] & 0xFF) >= 0xB7
						&& (msgData[pos] & 0xFF) < 0xC0) {

					byte lenghtOfLenght = (byte) (msgData[pos] - 0xB7);
					byte pow = (byte) (lenghtOfLenght - 1);

					int length = 0;

					for (int i = 1; i <= lenghtOfLenght; ++i) {

						length += (msgData[pos + i] & 0xFF) << (8 * pow);
						pow--;
					}

					// now we can parse an item for data[1]..data[length]
					byte[] item = new byte[length];
					System.arraycopy(msgData, pos + lenghtOfLenght + 1, item,
							0, length);

					byte[] rlpPrefix = new byte[lenghtOfLenght + 1];
					System.arraycopy(msgData, pos, rlpPrefix, 0,
							lenghtOfLenght + 1);

					RLPItem rlpItem = new RLPItem(item);
					rlpList.addItem(rlpItem);
					pos += lenghtOfLenght + length + 1;

					continue;
				}

				// It's an item less than 55 bytes long,
				// data[0] - 0x80 == length of the item
				if ((msgData[pos] & 0xFF) > 0x80
						&& (msgData[pos] & 0xFF) < 0xB7) {

					byte length = (byte) ((msgData[pos] & 0xFF) - 0x80);

					byte[] item = new byte[length];
					System.arraycopy(msgData, pos + 1, item, 0, length);

					byte[] rlpPrefix = new byte[2];
					System.arraycopy(msgData, pos, rlpPrefix, 0, 2);

					RLPItem rlpItem = new RLPItem(item);
					rlpList.addItem(rlpItem);
					pos += 1 + length;

					continue;
				}

				// null item
				if ((msgData[pos] & 0xFF) == 0x80) {

					byte[] item = new byte[0];
					RLPItem rlpItem = new RLPItem(item);
					rlpList.addItem(rlpItem);
					pos += 1;
					continue;
				}

				// single byte item
				if ((msgData[pos] & 0xFF) < 0x80) {

					byte[] item = { (byte) (msgData[pos] & 0xFF) };

					RLPItem rlpItem = new RLPItem(item);
					rlpList.addItem(rlpItem);
					pos += 1;
					continue;
				}
			}
		} catch (Throwable th) {
			throw new Error("wire packet not parsed correctly",
					th.fillInStackTrace());
		}
	}

	/*
	 * def rlp_encode(input): if isinstance(input,str): if len(input) == 1 and
	 * chr(input) < 128: return input else: return encode_length(len(input),128)
	 * + input elif isinstance(input,list): output = '' for item in input:
	 * output += rlp_encode(item) return encode_length(len(output),192) + output
	 * 
	 * def encode_length(L,offset): if L < 56: return chr(L + offset) elif L <
	 * 256**8: BL = to_binary(L) return chr(len(BL) + offset + 55) + BL else:
	 * raise Exception("input too long")
	 * 
	 * def to_binary(x): return '' if x == 0 else to_binary(int(x / 256)) +
	 * chr(x % 256)
	 */

	private static String rlpEncode(Object item) {

		if (item instanceof String) {

			String str = ((String) item);
			int length = str.length();
			if (length == 1 && str.charAt(0) < 128)
				return str;
			else
				return encodeLenght(str.length(), 128) + str;
		} else if (item instanceof List) {

			List itemList = (List) item;
			StringBuilder output = new StringBuilder();

			for (Object oneItem : itemList)
				output.append(rlpEncode(oneItem));
			return encodeLenght(output.toString().length(), 192)
					+ output.toString();
		}

		throw new Error("unsupported type" + item.getClass());
	}

	private static String encodeLenght(int L, int offset) {

		if (L < 56)
			return "" + (char) (L + offset);
		else if (L < (256 ^ 8)) {

			String BL = toBinary(L);
			return "" + (char) (BL.length() + offset + 55) + BL;
		} else
			throw new Error("input too long");

	}

	public static byte getCommandCode(byte[] data) {

		byte command = 0;

		int index = getFirstListElement(data, 0);

		command = data[index];

		command = ((int) (command & 0xFF) == 0x80) ? 0 : command;

		return command;
	}

	public static Object decode(char[] data) {

		if (data == null || data.length == 0)
			return null;

		if (data[0] >= 0xF7) { /*
								 * It's a list with a payload more than 55 bytes
								 * data[0] - 0xF7 = how many next bytes
								 * allocated for the length of the list
								 */
			;

			byte lenghtOfLenght = (byte) (data[0] - 0xF7);
			byte pow = (byte) (lenghtOfLenght - 1);

			long length = 0;

			for (int i = 1; i <= lenghtOfLenght; ++i) {

				length += data[i] << (8 * pow);
				pow--;
			}

			System.out.println(length);

			// now we can parse an item for data[1]..data[length]
		}

		if (data[0] >= 0xC0 && data[0] < 0xF7) /*
												 * It's a list with a payload
												 * less than 55 bytes
												 */
			;

		if (data[0] >= 0xB7 && data[0] < 0xC0) {/*
												 * It's an item with a payload
												 * more than 55 bytes data[0] -
												 * 0xB7 = how much next bytes
												 * allocated for the length of
												 * the string
												 */
			;

			byte lenghtOfLenght = (byte) (data[0] - 0xB7);
			byte pow = (byte) (lenghtOfLenght - 1);

			long length = 0;

			for (int i = 1; i <= lenghtOfLenght; ++i) {

				length += data[i] << (8 * pow);
				pow--;
			}
			// now we can parse an item for data[1]..data[length]
		}

		if (data[0] >= 0x80 && data[0] < 0xB7) {/*
												 * It's an item less than 55
												 * bytes long, data[0] - 0x80 ==
												 * lenght of the item
												 */
			;
		}

		if (data[0] == 0x80) /* null item */
			;
		if (data[0] < 0x80) /* single byte item */
			;

		return null;
	}

	private static String toBinary(int x) {

		if (x == 0)
			return "";
		else
			return toBinary(x >> 8) + ((char) (x & 0x00FF));

	}

	public static byte[] encodeByte(byte singleByte) {

		if ((singleByte & 0xFF) == 0) {

			return new byte[] { (byte) 0x80 };
		} else if ((singleByte & 0xFF) < 0x7F) {

			return new byte[] { singleByte };
		} else {

			return new byte[] { (byte) 0x81, singleByte };
		}
	}

	public static byte[] encodeShort(short singleShort) {

		if (singleShort <= 0xFF)

			return encodeByte((byte) singleShort);
		else {

			return new byte[] { (byte) 0x82, (byte) (singleShort >> 8 & 0xFF),
					(byte) (singleShort >> 0 & 0xFF) };

		}
	}

	public static byte[] encodeString(String srcString) {

		return encodeElement(srcString.getBytes());
	}

	public static byte[] encodeBigInteger(BigInteger srcBigInteger) {

		return encodeElement(srcBigInteger.toByteArray());
	}

	public static byte[] encodeElement(byte[] srcData) {

		if (srcData.length <= 0x37) {

			// length = 8X
			byte length = (byte) (0x80 + srcData.length);

			byte[] data = Arrays.copyOf(srcData, srcData.length + 1);
			System.arraycopy(data, 0, data, 1, srcData.length);
			data[0] = length;

			return data;

		} else {

			// length of length = BX
			// prefix = [BX, [length]]
			int tmpLenght = srcData.length;
			byte byteNum = 0;
			while (tmpLenght != 0) {

				++byteNum;
				tmpLenght = tmpLenght >> 8;
			}

			byte[] lenBytes = new byte[byteNum];
			for (int i = 0; i < byteNum; ++i) {
				lenBytes[0] = (byte) ((srcData.length >> (8 * i)) & 0xFF);
			}

			// first byte = F7 + bytes.length
			byte[] data = Arrays.copyOf(srcData, srcData.length + 1 + byteNum);
			System.arraycopy(data, 0, data, 1 + byteNum, srcData.length);
			data[0] = (byte) (0xB7 + byteNum);
			System.arraycopy(lenBytes, 0, data, 1, lenBytes.length);

			return data;
		}
	}

	public static byte[] encodeList(byte[]... elements) {

		int totalLength = 0;
		for (int i = 0; i < elements.length; ++i) {

			totalLength += elements[i].length;
		}

		byte[] data;
		int copyPos = 0;
		if (totalLength <= 0x37) {

			data = new byte[1 + totalLength];
			data[0] = (byte) (0xC0 + totalLength);
			copyPos = 1;

		} else {

			// length of length = BX
			// prefix = [BX, [length]]
			int tmpLenght = totalLength;
			byte byteNum = 0;
			while (tmpLenght != 0) {

				++byteNum;
				tmpLenght = tmpLenght >> 8;
			}

			tmpLenght = totalLength;
			byte[] lenBytes = new byte[byteNum];
			for (int i = 0; i < byteNum; ++i) {
				lenBytes[i] = (byte) ((tmpLenght >> (8 * i)) & 0xFF);
			}

			// first byte = F7 + bytes.length
			data = new byte[1 + lenBytes.length + totalLength];
			data[0] = (byte) (0xF7 + byteNum);
			System.arraycopy(lenBytes, 0, data, 1, lenBytes.length);

			copyPos = lenBytes.length + 1;
		}

		for (int i = 0; i < elements.length; ++i) {

			System.arraycopy(elements[i], 0, data, copyPos, elements[i].length);
			copyPos += elements[i].length;
		}

		return data;
	}

	public static void main(String args[]) {

		char[] data = { 0xF9, 20, 100 };

		decode(data);
	}

	public static void main2(String args[]) throws UnsupportedEncodingException {

		List<Object> moreInnerList = new ArrayList();
		moreInnerList.add("aa");

		List<Object> innerList = new ArrayList();
		innerList.add(moreInnerList);

		List<Object> list = new ArrayList<Object>();
		list.add("dogy");
		// list.add("dogy");
		// list.add("dogy");
		// list.add(innerList);
		list.add("cat");

		String result = rlpEncode(list);

		byte[] bytes = result.getBytes();

		for (char oneChar : result.toCharArray()) {

			byte oneByte = (byte) oneChar;

			System.out.print(Integer.toHexString((int) oneByte & 0x00FF) + " ");
		}
		System.out.println();

		System.out.println(result);

		// System.out.println('\u0080');
		// System.out.println(toBinary(252));
	}

}
