package org.ethereum.vm;

import org.abego.treelayout.internal.util.Contract;
import org.ethereum.core.AccountState;
import org.ethereum.core.ContractDetails;
import org.ethereum.crypto.HashUtil;
import org.ethereum.db.TrackDatabase;
import org.ethereum.manager.WorldManager;
import org.ethereum.trie.TrackTrie;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 16/06/2014 10:37
 */

public class VMComplexTest {


    @Test // contract call recursive
    public void test1(){

/**
 *       #The code will run
 *       ------------------

         a = contract.storage[999]
         if a > 0:
             contract.storage[999] = a - 1

             # call to contract: 77045e71a7a2c50903d88e564cd72fab11e82051
             send((tx.gas / 10 * 8), 0x77045e71a7a2c50903d88e564cd72fab11e82051, 0)
         else:
             stop
 */

        int expectedGas = 438;

        DataWord key1 = new DataWord(999);
        DataWord value1 = new DataWord(3);

        HashMap<DataWord, DataWord> storage = new HashMap<>();
        storage.put(key1, value1);

        ContractDetails contractDetails = new ContractDetails(storage);


        // Set contract into Database
        String callerAddr   = "cd2a3d9f938e13cd947ec05abc7fe734df8dd826";
        String contractAddr = "77045e71a7a2c50903d88e564cd72fab11e82051";
        String code         = "6103e75660005460006000530b0f630000004b596001600053036103e757600060006000600060007377045e71a7a2c50903d88e564cd72fab11e820516008600a5c0402f1630000004c5800";

        byte[] contractAddrB = Hex.decode(contractAddr);
        byte[] callerAddrB = Hex.decode(callerAddr);
        byte[] codeB = Hex.decode(code);

        byte[] codeKey = HashUtil.sha3(codeB);
        AccountState accountState = new AccountState();
        accountState.setCodeHash(codeKey);

        AccountState callerAcountState = new AccountState();
        callerAcountState.addToBalance(new BigInteger("100000000000000000000"));

        WorldManager.instance.worldState.update(callerAddrB, callerAcountState.getEncoded());
        WorldManager.instance.worldState.update(contractAddrB, accountState.getEncoded());
        WorldManager.instance.chainDB.put(codeKey, codeB);
        WorldManager.instance.detaildDB.put(contractAddrB, contractDetails.getEncoded());

        TrackTrie     stateDB = new TrackTrie(WorldManager.instance.worldState);
        TrackDatabase chainDb   = new TrackDatabase(WorldManager.instance.chainDB);
        TrackDatabase detaildDB = new TrackDatabase(WorldManager.instance.detaildDB);

        ProgramInvokeMockImpl pi =  new ProgramInvokeMockImpl();
        pi.setDetaildDB(detaildDB);
        pi.setChainDb(chainDb);
        pi.setStateDB(stateDB);
        pi.setDetails(contractDetails);
        pi.setOwnerAddress("77045e71a7a2c50903d88e564cd72fab11e82051");


        // Play the program
        VM vm = new VM();
        Program program = new Program(codeB, pi);

        try {
            while(!program.isStopped())
                vm.step(program);
        } catch (RuntimeException e) {
            program.setRuntimeFailure(e);
        }


        System.out.println();
        System.out.println("============ Results ============");
        AccountState as =
                new AccountState(WorldManager.instance.worldState.get(
                        Hex.decode( contractAddr) ));

        System.out.println("*** Used gas: " + program.result.getGasUsed());
        System.out.println("*** Contract Balance: " + as.getBalance());

        // todo: assert caller balance after contract exec

        assertEquals(expectedGas, program.result.getGasUsed());
    }



    @Test // contract call recursive with data
    public void test2(){

        /**
         *       #The code will run
         *       ------------------

                 contract A: 77045e71a7a2c50903d88e564cd72fab11e82051
                 ---------------
                     a = msg.data[0]
                     b = msg.data[1]

                     contract.storage[a]
                     contract.storage[b]


                 contract B: 83c5541a6c8d2dbad642f385d8d06ca9b6c731ee
                 -----------
                     a = msg((tx.gas / 10 * 8), 0x77045e71a7a2c50903d88e564cd72fab11e82051, 0, [11, 22, 33], 3, 6)

         */


        long expectedVal_1 = 11;
        long expectedVal_2 = 22;

        // Set contract into Database
        String callerAddr   = "cd2a3d9f938e13cd947ec05abc7fe734df8dd826";

        String contractA_addr = "77045e71a7a2c50903d88e564cd72fab11e82051";
        String contractB_addr = "83c5541a6c8d2dbad642f385d8d06ca9b6c731ee";

        String code_a = "60006020023560005460016020023560205460005360005760205360015700";
        String code_b = "6000601f5560e05b60e05b54600060c05b015560605b6020015b51602001600b5254516040016016525451606001602152546080525460007377045e71a7a2c50903d88e564cd72fab11e820516103e8f1602060000260a00160200153600054";

        byte[] caller_addr_bytes = Hex.decode(callerAddr);

        byte[] contractA_addr_bytes = Hex.decode(contractA_addr);
        byte[] codeA = Hex.decode(code_a);
        byte[] codeA_Key = HashUtil.sha3(codeA);
        AccountState accountState_a = new AccountState();
        accountState_a.setCodeHash(codeA_Key);
        WorldManager.instance.worldState.update(contractA_addr_bytes, accountState_a.getEncoded());

        byte[] contractB_addr_bytes = Hex.decode(contractB_addr);
        byte[] codeB = Hex.decode(code_b);
        byte[] codeB_Key = HashUtil.sha3(codeB);
        AccountState accountState_b = new AccountState();
        accountState_b.setCodeHash(codeB_Key);
        WorldManager.instance.worldState.update(contractB_addr_bytes, accountState_a.getEncoded());

        AccountState callerAcountState = new AccountState();
        callerAcountState.addToBalance(new BigInteger("100000000000000000000"));
        WorldManager.instance.worldState.update(caller_addr_bytes, callerAcountState.getEncoded());

        WorldManager.instance.chainDB.put(codeA_Key, codeA);

        TrackTrie     stateDB   = new TrackTrie(WorldManager.instance.worldState);
        TrackDatabase chainDb   = new TrackDatabase(WorldManager.instance.chainDB);
        TrackDatabase detaildDB = new TrackDatabase(WorldManager.instance.detaildDB);

        ProgramInvokeMockImpl pi =  new ProgramInvokeMockImpl();
        pi.setDetaildDB(detaildDB);
        pi.setChainDb(chainDb);
        pi.setStateDB(stateDB);
        pi.setDetails(null);
        pi.setOwnerAddress(contractB_addr);

        // ****************** //
        //  Play the program  //
        // ****************** //
        VM vm = new VM();
        Program program = new Program(codeB, pi);

        try {
            while(!program.isStopped())
                vm.step(program);
        } catch (RuntimeException e) {
            program.setRuntimeFailure(e);
        }


        System.out.println();
        System.out.println("============ Results ============");
        AccountState as =
                new AccountState(WorldManager.instance.worldState.get(
                        Hex.decode( contractA_addr) ));


        System.out.println("*** Used gas: " + program.result.getGasUsed());


        byte[] rlpBytes =  WorldManager.instance.detaildDB.get(contractA_addr_bytes);

        ContractDetails details = new ContractDetails(rlpBytes);
        DataWord value_1 = details.getStorage().get(new DataWord(00));
        DataWord value_2 = details.getStorage().get(new DataWord(01));


        assertEquals(expectedVal_1, value_1.longValue());
        assertEquals(expectedVal_2, value_2.longValue());



    }



}
