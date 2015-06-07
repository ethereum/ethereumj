package org.ethereum.db;

import junit.framework.Assert;
import org.ethereum.vm.DataWord;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.assertEquals;

public class ContractDetailsTest {

    @Test
    public void test_1(){

        byte[] code = Hex.decode("60016002");

        byte[] key_1 = Hex.decode("111111");
        byte[] val_1 = Hex.decode("aaaaaa");

        byte[] key_2 = Hex.decode("222222");
        byte[] val_2 = Hex.decode("bbbbbb");

        ContractDetailsImpl contractDetails = new ContractDetailsImpl();
        contractDetails.setCode(code);
        contractDetails.put(new DataWord(key_1), new DataWord(val_1));
        contractDetails.put(new DataWord(key_2), new DataWord(val_2));

        byte[] data = contractDetails.getEncoded();

        ContractDetailsImpl contractDetails_ = new ContractDetailsImpl(data);

        assertEquals(Hex.toHexString(code),
            Hex.toHexString(contractDetails_.getCode()));

        assertEquals(Hex.toHexString(val_1),
                Hex.toHexString(contractDetails_.get(new DataWord(key_1)).getNoLeadZeroesData()));

        assertEquals(Hex.toHexString(val_2),
                Hex.toHexString(contractDetails_.get(new DataWord(key_2)).getNoLeadZeroesData()));

    }
}
