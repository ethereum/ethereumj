package org.ethereum.vm;

import org.ethereum.core.AccountState;
import org.ethereum.crypto.HashUtil;
import org.ethereum.db.RepositoryImpl;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 16/06/2014 10:37
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VMComplexTest {

    @Test // contract call recursive
    public void test1() {

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

        ProgramInvokeMockImpl pi =  new ProgramInvokeMockImpl();
        pi.setOwnerAddress("77045e71a7a2c50903d88e564cd72fab11e82051");
        RepositoryImpl repository = pi.getRepository();

        repository.createAccount(callerAddrB);
        repository.addBalance(callerAddrB, new BigInteger("100000000000000000000"));

        repository.createAccount(contractAddrB);
        repository.saveCode(contractAddrB, codeB);
        repository.addStorageRow(contractAddrB, key1, value1);

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

        BigInteger balance = repository.getBalance(callerAddrB);

        System.out.println("*** Used gas: " + program.result.getGasUsed());
        System.out.println("*** Contract Balance: " + balance);

        // todo: assert caller balance after contract exec

        repository.close();
        assertEquals(expectedGas, program.result.getGasUsed());
    }

    @Test // contractB call contractA with data to storage
    public void test2() {

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

        ProgramInvokeMockImpl pi =  new ProgramInvokeMockImpl();
        pi.setOwnerAddress(contractB_addr);
        RepositoryImpl repository = pi.getRepository();

        byte[] contractB_addr_bytes = Hex.decode(contractB_addr);
        byte[] codeB = Hex.decode(code_b);

        repository.createAccount(contractA_addr_bytes);
        repository.saveCode(contractA_addr_bytes, codeA);

        repository.createAccount(contractB_addr_bytes);
        repository.saveCode(contractB_addr_bytes, codeB);

        repository.createAccount(caller_addr_bytes);
        repository.addBalance(caller_addr_bytes, new BigInteger("100000000000000000000"));


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


        System.out.println("*** Used gas: " + program.result.getGasUsed());


        DataWord value_1 = repository.getStorageValue(contractA_addr_bytes, new DataWord(00));
        DataWord value_2 = repository.getStorageValue(contractA_addr_bytes, new DataWord(01));


        repository.close();
        assertEquals(expectedVal_1, value_1.longValue());
        assertEquals(expectedVal_2, value_2.longValue());

        // TODO: check that the value pushed after exec is 1
    }

    @Test // contractB call contractA with return expectation
    public void test3() {

        /**
         *       #The code will run
         *       ------------------

         contract A: 77045e71a7a2c50903d88e564cd72fab11e82051
         ---------------

           a = 11
           b = 22
           c = 33
           d = 44
           e = 55
           f = 66

           [asm  192 0 RETURN asm]



         contract B: 83c5541a6c8d2dbad642f385d8d06ca9b6c731ee
         -----------
             a = msg((tx.gas / 10 * 8), 0x77045e71a7a2c50903d88e564cd72fab11e82051, 0, [11, 22, 33], 3, 6)

         */

        long expectedVal_1 = 11;
        long expectedVal_2 = 22;
        long expectedVal_3 = 33;
        long expectedVal_4 = 44;
        long expectedVal_5 = 55;
        long expectedVal_6 = 66;

        // Set contract into Database
        String callerAddr   = "cd2a3d9f938e13cd947ec05abc7fe734df8dd826";

        String contractA_addr = "77045e71a7a2c50903d88e564cd72fab11e82051";
        String contractB_addr = "83c5541a6c8d2dbad642f385d8d06ca9b6c731ee";

        String code_a = "600b60005460166020546021604054602c6060546037608054604260a05460c06000f2";
        String code_b = "6000601f5560e05b60e05b54600060c05b015560605b6020015b51602001600b5254516040016016525451606001602152546080525460007377045e71a7a2c50903d88e564cd72fab11e820516103e8f1602060000260a00160200153600054";

        byte[] caller_addr_bytes = Hex.decode(callerAddr);

        byte[] contractA_addr_bytes = Hex.decode(contractA_addr);
        byte[] codeA = Hex.decode(code_a);

        byte[] contractB_addr_bytes = Hex.decode(contractB_addr);
        byte[] codeB = Hex.decode(code_b);

        ProgramInvokeMockImpl pi =  new ProgramInvokeMockImpl();
        pi.setOwnerAddress(contractB_addr);
        RepositoryImpl repository  = pi.getRepository();
        repository.createAccount(contractA_addr_bytes);
        repository.saveCode(contractA_addr_bytes, codeA);

        repository.createAccount(contractB_addr_bytes);
        repository.saveCode(contractB_addr_bytes, codeB);

        repository.createAccount(caller_addr_bytes);
        repository.addBalance(caller_addr_bytes, new BigInteger("100000000000000000000"));

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
        System.out.println("*** Used gas: " + program.result.getGasUsed());

        DataWord value1 = program.memoryLoad(new DataWord(32));
        DataWord value2 = program.memoryLoad(new DataWord(64));
        DataWord value3 = program.memoryLoad(new DataWord(96));
        DataWord value4 = program.memoryLoad(new DataWord(128));
        DataWord value5 = program.memoryLoad(new DataWord(160));
        DataWord value6 = program.memoryLoad(new DataWord(192));

        repository.close();

        assertEquals(expectedVal_1, value1.longValue());
        assertEquals(expectedVal_2, value2.longValue());
        assertEquals(expectedVal_3, value3.longValue());
        assertEquals(expectedVal_4, value4.longValue());
        assertEquals(expectedVal_5, value5.longValue());
        assertEquals(expectedVal_6, value6.longValue());

        // TODO: check that the value pushed after exec is 1
    }

    @Test // CREATE magic
    public void test4() {

        /**
         *       #The code will run
         *       ------------------

         contract A: 77045e71a7a2c50903d88e564cd72fab11e82051
         -----------

             a = 0x7f60c860005461012c6020540000000000000000000000000000000000000000
             b = 0x0060005460206000f20000000000000000000000000000000000000000000000
             create(100, 0 41)


         contract B: (the contract to be created the addr will be defined to: 8e45367623a2865132d9bf875d5cfa31b9a0cd94)
         -----------
             a = 200
             b = 300

         */

        // Set contract into Database
        String callerAddr   = "cd2a3d9f938e13cd947ec05abc7fe734df8dd826";

        String contractA_addr = "77045e71a7a2c50903d88e564cd72fab11e82051";

        String code_a = "7f7f60c860005461012c602054000000000000" +
                        "00000000000000000000000000006000547e60" +
                        "005460206000f2000000000000000000000000" +
                        "0000000000000000000000602054602960006064f0";

        ProgramInvokeMockImpl pi =  new ProgramInvokeMockImpl();
        pi.setOwnerAddress(contractA_addr);

        RepositoryImpl repository = pi.getRepository();

        byte[] caller_addr_bytes = Hex.decode(callerAddr);

        byte[] contractA_addr_bytes = Hex.decode(contractA_addr);
        byte[] codeA = Hex.decode(code_a);

        repository.createAccount(contractA_addr_bytes);
        repository.saveCode(contractA_addr_bytes, codeA);

        repository.createAccount(caller_addr_bytes);

        // ****************** //
        //  Play the program  //
        // ****************** //
        VM vm = new VM();
        Program program = new Program(codeA, pi);

		try {
			while (!program.isStopped())
				vm.step(program);
		} catch (RuntimeException e) {
			program.setRuntimeFailure(e);
		}

        System.out.println();
        System.out.println("============ Results ============");

        System.out.println("*** Used gas: " + program.result.getGasUsed());
        // TODO: check that the value pushed after exec is the new address
        repository.close();
    }
    
    @Test // CALL contract with too much gas
    @Ignore
    public void test5() {
    	// TODO: CALL contract with gas > gasRemaining && gas > Long.MAX_VALUE
    }

}
