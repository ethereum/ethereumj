package org.ethereum.vm;

import org.ethereum.db.StorageDictionaryDb;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

/**
 * Created by Anton Nashatyrev on 10.09.2015.
 */
public class StorageDictionaryHandlerTest {

    @Test
    public void test1() {
        StorageDictionaryHandler sk = new StorageDictionaryHandler(new DataWord(0xff));
        sk.vmStartPlayNotify();
        sk.vmSha3Notify(Hex.decode("0000000000000000000000000000000000000000000000000000000000000001" +
                        "0000000000000000000000000000000000000000000000000000000000000004"),
                new DataWord(Hex.decode("abd6e7cb50984ff9c2f3e18a2660c3353dadf4e3291deeb275dae2cd1e44fe05")));
        sk.vmSha3Notify(Hex.decode("abd6e7cb50984ff9c2f3e18a2660c3353dadf4e3291deeb275dae2cd1e44fe05"),
                new DataWord(Hex.decode("210afe6ebef982fa193bb4e17f9f236cdf09af7788627b5d54d9e3e4b100021b")));

        sk.vmSStoreNotify(new DataWord(Hex.decode("210afe6ebef982fa193bb4e17f9f236cdf09af7788627b5d54d9e3e4b100021b")),
                new DataWord(Hex.decode("3137326142713267314b38327174626745755876384b63443342453531346258")));
        sk.vmSStoreNotify(new DataWord(Hex.decode("210afe6ebef982fa193bb4e17f9f236cdf09af7788627b5d54d9e3e4b100021c")),
                new DataWord(Hex.decode("3772000000000000000000000000000000000000000000000000000000000000")));

        sk.vmSStoreNotify(new DataWord(Hex.decode("abd6e7cb50984ff9c2f3e18a2660c3353dadf4e3291deeb275dae2cd1e44fe05")),
                new DataWord(Hex.decode("0000000000000000000000000000000000000000000000000000000000000022")));
        sk.vmSStoreNotify(new DataWord(Hex.decode("0000000000000000000000000000000000000000000000000000000000000003")),
                new DataWord(Hex.decode("0000000000000000000000000000000000000000000000000000000000000002")));

        sk.vmSStoreNotify(new DataWord(Hex.decode("abd6e7cb50984ff9c2f3e18a2660c3353dadf4e3291deeb275dae2cd1e44fe06")),
                new DataWord(Hex.decode("000000000000000000000000000000000000000000000000016345785d8a0000")));
        sk.vmSStoreNotify(new DataWord(Hex.decode("abd6e7cb50984ff9c2f3e18a2660c3353dadf4e3291deeb275dae2cd1e44fe07")),
                new DataWord(Hex.decode("0000000000000000000000000d82cd113dc35ddda93f38166cd5cde8b88e36a1")));
        sk.vmEndPlayNotify(null);

//        System.out.println(sk.hashes.values());
//
//        System.out.println(sk.keysPath.dump());
    }

    @Test
    public void test2() {
        StorageDictionaryHandler sk = new StorageDictionaryHandler(new DataWord(0xff));
        sk.vmStartPlayNotify();
        sk.vmSha3Notify(Hex.decode("2153b9b2b56bb29c21cf47fe64fd2abdd7171b14cca48560f6f6cf294a1a5c52" +
                        "0000000000000000000000000000000000000000000000000000000000000103"),
                new DataWord(Hex.decode("ffd874e59055f4f3dfa2a72e56b6998ed34dc01ebcf35d9ab673308b9f41fc81")));

        sk.vmSStoreNotify(new DataWord(Hex.decode("ffd874e59055f4f3dfa2a72e56b6998ed34dc01ebcf35d9ab673308b9f41fc7f")),
                new DataWord(Hex.decode("3137326142713267314b38327174626745755876384b63443342453531346258")));
        sk.vmSStoreNotify(new DataWord(Hex.decode("ffd874e59055f4f3dfa2a72e56b6998ed34dc01ebcf35d9ab673308b9f41fc80")),
                new DataWord(Hex.decode("3137326142713267314b38327174626745755876384b63443342453531346258")));
        sk.vmSStoreNotify(new DataWord(Hex.decode("ffd874e59055f4f3dfa2a72e56b6998ed34dc01ebcf35d9ab673308b9f41fc81")),
                new DataWord(Hex.decode("3137326142713267314b38327174626745755876384b63443342453531346258")));
        sk.vmEndPlayNotify(null);

//        System.out.println(sk.keysPath.dump());
    }
}
