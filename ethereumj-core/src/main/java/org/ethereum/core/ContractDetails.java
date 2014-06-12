package org.ethereum.core;

import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.ethereum.vm.DataWord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 09/06/2014 15:31
 */

public class ContractDetails {

    private byte[] rlpEncoded;

    List<DataWord> storageKeys;
    List<DataWord> storageValues;

    public ContractDetails(byte[] rlpEncoded) {

        RLPList data    = RLP.decode2(rlpEncoded);
        RLPList rlpList = (RLPList)data.get(0);

        RLPList keys   =  (RLPList)rlpList.get(0);
        RLPList values =  (RLPList)rlpList.get(1);

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
    }

    public ContractDetails(Map<DataWord, DataWord> storage) {

        storageKeys   = new ArrayList<DataWord>();
        storageValues = new ArrayList<DataWord>();

        for(DataWord key : storage.keySet()){

            DataWord value = storage.get(key);

            storageKeys.add(key);
            storageValues.add(value);
        }
    }

    public byte[] getEncoded() {
        if(rlpEncoded == null) {

            byte[][] keys  = new byte[storageKeys.size()][];
            byte[][] values = new byte[storageValues.size()][];

            int i = 0;
            for (DataWord key : storageKeys){
                keys[i] = RLP.encodeElement( key.getData());
                ++i;
            }

            i = 0;
            for (DataWord value : storageValues){
                values[i] = RLP.encodeElement( value.getData() );
                ++i;
            }

            byte[] rlpKeysList   = RLP.encodeList(keys);
            byte[] rlpValuesList = RLP.encodeList(values);

            this.rlpEncoded = RLP.encodeList(rlpKeysList, rlpValuesList);
        }
        return rlpEncoded;
    }


    public Map<DataWord, DataWord> getStorage(){

        Map<DataWord, DataWord> storage = new HashMap<>();

        for (int i = 0;
             storageKeys != null &&
             i < storageKeys.size(); ++i){
            storage.put(storageKeys.get(i), storageValues.get(i));
        }

        return storage;
    }
}
