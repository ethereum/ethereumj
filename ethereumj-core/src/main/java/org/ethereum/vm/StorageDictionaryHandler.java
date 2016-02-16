package org.ethereum.vm;

import org.ethereum.config.SystemProperties;
import org.ethereum.crypto.SHA3Helper;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.ContractDetails;
import org.ethereum.db.StorageDictionary;
import org.ethereum.db.StorageDictionaryDb;
import org.ethereum.util.Utils;
import org.ethereum.vm.program.Program;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.ethereum.crypto.HashUtil.sha3;

/**
 * Created by Anton Nashatyrev on 04.09.2015.
 */
public class StorageDictionaryHandler implements VMHook {
    private static final Logger logger = LoggerFactory.getLogger("VM");

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

    // hashes for storage indexes can be pre-calculated by the Solidity compiler
    private static final Map<ByteArrayWrapper, Entry> ConstHashes = new HashMap<>();

    static {
        DataWord storageIdx = new DataWord();
        // Let's take 5000 as the max storage index
        for (int i = 0; i < 5000; i++) {
            byte[] sha3 = SHA3Helper.sha3(storageIdx.getData());
            Entry entry = new Entry(new DataWord(sha3), storageIdx.clone().getData());
            ConstHashes.put(getMapKey(sha3), entry);
            storageIdx.add(new DataWord(1));
        }
    }

    byte[] contractAddress;

    Map<ByteArrayWrapper, Entry> hashes = new HashMap<>();
    Map<ByteArrayWrapper, DataWord> storeKeys = new HashMap<>();

    public StorageDictionaryHandler(DataWord ownerAddress) {
        try {
            contractAddress = ownerAddress.getNoLeadZeroesData();
        } catch (Throwable e) {
            logger.error("Unexpected exception: ", e);
            // ignore exception to not halt VM execution
        }
    }

    static ByteArrayWrapper getMapKey(byte[] hash) {
        return new ByteArrayWrapper(Arrays.copyOfRange(hash, 0, 20));
    }

    public void vmSha3Notify(byte[] in, DataWord out) {
        try {
            hashes.put(getMapKey(out.getData()), new Entry(out.clone(), in));
        } catch (Throwable e) {
            logger.error("Unexpected exception: ", e);
            // ignore exception to not halt VM execution
        }
    }

    public void vmSStoreNotify(DataWord key, DataWord value) {
        try {
            storeKeys.put(new ByteArrayWrapper(key.clone().getData()), value.clone());
        } catch (Throwable e) {
            logger.error("Unexpected exception: ", e);
            // ignore exception to not halt VM execution
        }
    }

    private Entry findHash(byte[] key) {
        ByteArrayWrapper mapKey = getMapKey(key);
        Entry entry = hashes.get(mapKey);
        if (entry == null) {
            entry = ConstHashes.get(mapKey);
        }
        return entry;
    }

    public StorageDictionary.PathElement[] getKeyOriginSerpent(byte[] key) {
        Entry entry = findHash(key);
        if (entry != null) {
            if (entry.input.length > 32 && entry.input.length % 32 == 0 &&
                    Arrays.equals(key, entry.hashValue.getData())) {

                int pathLength = entry.input.length / 32;
                StorageDictionary.PathElement[] ret = new StorageDictionary.PathElement[pathLength];
                for (int i = 0; i < ret.length; i++) {
                    ret[i] = guessPathElement(Arrays.copyOfRange(entry.input, i * 32, (i+1) * 32))[0];
                    ret[i].type = StorageDictionary.Type.MapKey;
                }
                return ret;
            } else {
                // not a Serenity contract
            }
        }
        StorageDictionary.PathElement[] storageIndex = guessPathElement(key);
        storageIndex[0].type = StorageDictionary.Type.StorageIndex;
        return  storageIndex;
    }

    public StorageDictionary.PathElement[] getKeyOriginSolidity(byte[] key) {
        Entry entry = findHash(key);
        if (entry == null) {
            StorageDictionary.PathElement[] storageIndex = guessPathElement(key);
            storageIndex[0].type = StorageDictionary.Type.StorageIndex;
            return  storageIndex;
        } else {
            byte[] subKey = Arrays.copyOfRange(entry.input, 0, entry.input.length - 32); // subkey.length == 0 for dyn arrays
            long offset = new BigInteger(key).subtract(new BigInteger(entry.hashValue.clone().getData())).longValue();
            return Utils.mergeArrays(
                    getKeyOriginSolidity(Arrays.copyOfRange(entry.input, entry.input.length - 32, entry.input.length)),
                    guessPathElement(subKey),
                    new StorageDictionary.PathElement[] {new StorageDictionary.PathElement
                            (subKey.length == 0 ? StorageDictionary.Type.ArrayIndex : StorageDictionary.Type.Offset, (int) offset)});
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

    private static Map<ByteArrayWrapper, StorageDictionary> seContracts = new HashMap<>();

    public StorageDictionary testDump(StorageDictionaryDb.Layout layout) {
        StorageDictionary dict = new StorageDictionary();
        for (ByteArrayWrapper key : storeKeys.keySet()) {

            dict.addPath(new DataWord(key.getData()), getKeyOriginSolidity(key.getData()));
        }
        return dict;
    }

    public void dumpKeys(ContractDetails storage) {

        StorageDictionary solidityDict = StorageDictionaryDb.INST.getOrCreate(StorageDictionaryDb.Layout.Solidity,
                contractAddress);
        StorageDictionary serpentDict = StorageDictionaryDb.INST.getOrCreate(StorageDictionaryDb.Layout.Serpent,
                contractAddress);

        for (ByteArrayWrapper key : storeKeys.keySet()) {
            solidityDict.addPath(new DataWord(key.getData()), getKeyOriginSolidity(key.getData()));
            serpentDict.addPath(new DataWord(key.getData()), getKeyOriginSerpent(key.getData()));
        }

        if (SystemProperties.CONFIG.getConfig().hasPath("vm.structured.storage.dictionary.dump")) {
            // for debug purposes only
            if (!solidityDict.isValid()) {
                File f = new File("json");
                f.mkdirs();
                f = new File(f, Hex.toHexString(contractAddress) + ".sol.txt");
                try {
                    BufferedWriter w = new BufferedWriter(new FileWriter(f));
                    String s = solidityDict.compactAndFilter(null).dump(storage);
                    w.write(s);
                    w.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (!serpentDict.isValid()) {
                File f = new File("json", Hex.toHexString(contractAddress) + ".se.txt");
                f.getParentFile().mkdirs();
                try {
                    BufferedWriter w = new BufferedWriter(new FileWriter(f));
                    String s = serpentDict.dump(storage);
                    w.write(s);
                    w.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            File f = new File("json", Hex.toHexString(contractAddress) + ".hash.txt");
            f.getParentFile().mkdirs();
            try {
                BufferedWriter w = new BufferedWriter(new FileWriter(f, true));
                w.write("\nHashes:\n");
                for (Entry entry : hashes.values()) {
                    w.write(entry + "\n");
                }
                w.write("\nSSTORE:\n");
                for (Map.Entry<ByteArrayWrapper, DataWord> entry : storeKeys.entrySet()) {
                    w.write(entry + "\n");
                }

                w.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        StorageDictionaryDb.INST.put(StorageDictionaryDb.Layout.Solidity, contractAddress, solidityDict);
        StorageDictionaryDb.INST.put(StorageDictionaryDb.Layout.Serpent, contractAddress, serpentDict);
    }

    public void vmStartPlayNotify() {
    }

    public void vmEndPlayNotify(ContractDetails contractDetails) {
        try {
            dumpKeys(contractDetails);
        } catch (Throwable e) {
            logger.error("Unexpected exception: ", e);
            // ignore exception to not halt VM execution
        }
    }

    @Override
    public void startPlay(Program program) {
        vmStartPlayNotify();
    }

    @Override
    public void stopPlay(Program program) {
        vmEndPlayNotify(program.getStorage().getContractDetails(program.getOwnerAddress().getLast20Bytes()));
    }

    public void step(Program program, OpCode opcode) {
        switch (opcode) {
            case SSTORE:
                DataWord addr = program.getStack().get(0);
                DataWord value = program.getStack().get(1);
                vmSStoreNotify(addr, value);
                break;
            case SHA3:
                DataWord memOffsetData = program.getStack().get(0);
                DataWord lengthData = program.getStack().get(1);
                byte[] buffer = program.memoryChunk(memOffsetData.intValue(), lengthData.intValue());
                byte[] encoded = sha3(buffer);
                DataWord word = new DataWord(encoded);
                vmSha3Notify(buffer, word);
                break;
        }
    }

}
