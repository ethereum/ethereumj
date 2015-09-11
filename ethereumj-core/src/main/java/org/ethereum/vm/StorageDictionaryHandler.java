package org.ethereum.vm;

import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.ContractDetails;
import org.ethereum.db.StorageDictionary;
import org.ethereum.db.StorageDictionaryDb;
import org.ethereum.util.Utils;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Anton Nashatyrev on 04.09.2015.
 */
public class StorageDictionaryHandler {

    static class Entry {
        final DataWord hashValue;
        final byte[] input;

        public Entry(DataWord hashValue, byte[] input) {
            this.hashValue = hashValue.clone();
            this.input = input;
        }

        @Override
        public String toString() {
            return "sha3(" + Hex.toHexString(input) + ") = " + hashValue;
        }
    }

    byte[] contractAddress;
    StorageDictionary keysPath;
    Map<ByteArrayWrapper, Entry> hashes = new HashMap<>();
    Map<ByteArrayWrapper, DataWord> storeKeys = new HashMap<>();

    public StorageDictionaryHandler(DataWord ownerAddress) {
        try {
            contractAddress = ownerAddress.getNoLeadZeroesData();
            keysPath = StorageDictionaryDb.INST.getOrCreate(contractAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ByteArrayWrapper getMapKey(byte[] hash) {
        return new ByteArrayWrapper(Arrays.copyOfRange(hash, 0, 20));
    }

    public void vmSha3Notify(byte[] in, DataWord out) {
        try {
            hashes.put(getMapKey(out.getData()), new Entry(out.clone(), in));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void vmSStoreNotify(DataWord key, DataWord value) {
        try {
            storeKeys.put(new ByteArrayWrapper(key.clone().getData()), value.clone());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public StorageDictionary.PathElement[] getKeyOrigin(byte[] key) {
        Entry entry = hashes.get(getMapKey(key));
        if (entry == null) {
            StorageDictionary.PathElement[] storageIndex = guessPathElement(key);
            storageIndex[0].type = StorageDictionary.Type.StorageIndex;
            return  storageIndex;
        } else {
            byte[] subKey = Arrays.copyOfRange(entry.input, 0, entry.input.length - 32);
            long offset = new BigInteger(key).subtract(new BigInteger(entry.hashValue.clone().getData())).longValue();
            return Utils.mergeArrays(
                    getKeyOrigin(Arrays.copyOfRange(entry.input, entry.input.length - 32, entry.input.length)),
                    guessPathElement(subKey),
                    new StorageDictionary.PathElement[] {new StorageDictionary.PathElement(StorageDictionary.Type.Offset, (int) offset)});
        }
    }

    public StorageDictionary.PathElement[] guessPathElement(byte[] bytes) {
        if (bytes.length == 0) return new StorageDictionary.PathElement[0];
        Object value = guessValue(bytes);
        StorageDictionary.PathElement el = null;
        if (value instanceof String) {
            el = new StorageDictionary.PathElement((String) value);
        } else if (value instanceof BigInteger) {
            BigInteger bi = (BigInteger) value;
            if (bi.bitLength() < 32) el = new StorageDictionary.PathElement(StorageDictionary.Type.MapKey, bi.intValue());
            else el = new StorageDictionary.PathElement("0x" + bi.toString(16));
        }
        return new StorageDictionary.PathElement[] {el};
    }

    public static Object guessValue(byte[] bytes) {
        int startZeroCnt = 0, startNonZeroCnt = 0;
        boolean asciiOnly = true;
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] != 0) {
                if (startNonZeroCnt > 0 || i == 0) startNonZeroCnt++;
                else break;
            } else {
                if (startZeroCnt > 0 || i == 0) startZeroCnt++;
                else break;
            }
            asciiOnly &= bytes[i] > 0x1F && bytes[i] <= 0x7E;
        }
        int endZeroCnt = 0, endNonZeroCnt = 0;
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[bytes.length - i - 1] != 0) {
                if (endNonZeroCnt > 0 || i == 0) endNonZeroCnt++;
                else break;
            } else {
                if (endZeroCnt > 0 || i == 0) endZeroCnt++;
                else break;
            }
        }
        if (startZeroCnt > 16) return new BigInteger(bytes);
        if (asciiOnly) return new String(bytes, 0, startNonZeroCnt);
        return Hex.toHexString(bytes);
    }

    public void dumpKeys(ContractDetails storage) {
        for (ByteArrayWrapper key : storeKeys.keySet()) {
            keysPath.addPath(new DataWord(key.getData()), getKeyOrigin(key.getData()));
        }
        StorageDictionaryDb.INST.put(contractAddress, keysPath);

        // Uncomment to dump human readable storage with values
//        File f = new File("json");
//        f.mkdirs();
//        f = new File(f, Hex.toHexString(contractAddress) + ".txt");
//        try {
//            BufferedWriter w = new BufferedWriter(new FileWriter(f));
//            String s = keysPath.dump(storage);
//            w.write(s);
//            w.write("\nHashaes:\n");
//            for (Entry entry : hashes.values()) {
//                w.write(entry + "\n");
//            }
//            w.write("\nSSTORE:\n");
//            for (Map.Entry<Key, DataWord> entry : storeKeys.entrySet()) {
//                w.write(entry + "\n");
//            }
//
//            w.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public void vmStartPlayNotify() {
    }

    public void vmEndPlayNotify(ContractDetails contractDetails) {
        try {
            dumpKeys(contractDetails);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
