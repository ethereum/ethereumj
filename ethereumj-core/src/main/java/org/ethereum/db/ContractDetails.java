package org.ethereum.db;

import org.ethereum.trie.Trie;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.ethereum.vm.DataWord;

import javax.enterprise.inject.New;
import java.util.*;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 24/06/2014 00:12
 */

public class ContractDetails {

    private byte[] rlpEncoded;

    List<DataWord> storageKeys   = new ArrayList<DataWord>();
    List<DataWord> storageValues = new ArrayList<DataWord>();

    byte[] code;

    Trie storageTrie = new Trie(null);


    public ContractDetails(){}
    public ContractDetails(byte[] rlpCode) {
        decode(rlpCode);
    }

    public ContractDetails(Map<DataWord, DataWord> storage, byte[] code) {}

    public void put(DataWord key, DataWord value){

        storageTrie.update(key.getData(), value.getData());

        int index = storageKeys.indexOf(key);

        if (index != -1){
            storageKeys.remove(index);
            storageValues.remove(index);
        }

        storageKeys.add(key);
        storageValues.add(value);

        this.rlpEncoded = null;
    }

    public DataWord get(DataWord key){

        if (storageKeys.size() == 0)
            return null;

        int foundIndex = -1;
        for (int i = 0; i < storageKeys.size(); ++i){
            if (storageKeys.get(i).equals(key) ){
                foundIndex = i;
                break;
            }
        }

        if (foundIndex != -1)
            return storageValues.get(foundIndex);
        else
            return null;
    }

    public byte[] getCode() {
        return code;
    }

    public void setCode(byte[] code) {
        this.code = code;
        this.rlpEncoded = null;
    }

    public byte[] getStorageHash(){
        return storageTrie.getRootHash();
    }

    public void decode(byte[] rlpCode){
        RLPList data    = RLP.decode2(rlpCode);
        RLPList rlpList = (RLPList)data.get(0);

        RLPList keys   =  (RLPList)rlpList.get(0);
        RLPList values =  (RLPList)rlpList.get(1);
        RLPElement code =  (RLPElement)rlpList.get(2);

        if (keys.size() > 0){
            storageKeys = new ArrayList<>();
            storageValues = new ArrayList<>();
        }

        for (int i = 0; i < keys.size(); ++i){

            RLPItem rlpItem = (RLPItem)keys.get(i);
            storageKeys.add(new DataWord(rlpItem.getRLPData()));
        }

        for (int i = 0; i < values.size(); ++i){
            RLPItem rlpItem = (RLPItem)values.get(i);
            storageValues.add(new DataWord(rlpItem.getRLPData()));
        }

        for (int i = 0; i < keys.size(); ++i){

            DataWord key = storageKeys.get(i);
            DataWord value = storageValues.get(i);
            storageTrie.update(key.getData(), value.getData());
        }

        this.code = code.getRLPData();

        this.rlpEncoded = rlpCode;
    }

    public byte[] getEncoded(){

        if(rlpEncoded == null) {

            int size = storageKeys == null ? 0 : storageKeys.size();

            byte[][] keys  = new byte[size][];
            byte[][] values = new byte[size][];


            for (int i = 0; i < size; ++i){
                DataWord key  =  storageKeys.get(i);
                keys[i] = RLP.encodeElement(key.getData());
            }

            for (int i = 0; i < size; ++i){
                DataWord value = storageValues.get(i);
                values[i] = RLP.encodeElement( value.getData() );
            }

            byte[] rlpKeysList   = RLP.encodeList(keys);
            byte[] rlpValuesList = RLP.encodeList(values);
            byte[] rlpCode       = RLP.encodeElement(code);

            this.rlpEncoded = RLP.encodeList(rlpKeysList, rlpValuesList, rlpCode);
        }
        return rlpEncoded;

    }


    public Map<DataWord, DataWord> getStorage(){

        Map<DataWord, DataWord> storage = new HashMap<DataWord, DataWord>();

        for (int i = 0;
             storageKeys != null &&
                     i < storageKeys.size(); ++i){
            storage.put(storageKeys.get(i), storageValues.get(i));
        }

        return Collections.unmodifiableMap(storage);
    }

}

