package org.ethereum.trie;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;

import static org.junit.Assert.*;

public class TrieTest {

	private static String LONG_STRING = "1234567890abcdefghijklmnopqrstuvwxxzABCEFGHIJKLMNOPQRSTUVWXYZ";
	private static String ROOT_HASH_EMPTY = "";
	
	private static String c = "c";
	private static String ca = "ca";
	private static String cat = "cat";
	private static String dog = "dog";
	private static String doge = "doge";
	private static String test = "test";
	private static String dude = "dude";
	
	private MockDB mockDb = new MockDB();
	
//		ROOT: [ '\x16', A ]
//		A: [ '', '', '', '', B, '', '', '', C, '', '', '', '', '', '', '', '' ]
//		B: [ '\x00\x6f', D ]
//		D: [ '', '', '', '', '', '', E, '', '', '', '', '', '', '', '', '', 'verb' ]
//		E: [ '\x17', F ]
//		F: [ '', '', '', '', '', '', G, '', '', '', '', '', '', '', '', '', 'puppy' ]
//		G: [ '\x35', 'coin' ]
//		C: [ '\x20\x6f\x72\x73\x65', 'stallion' ]
	
	@After
	public void closeMockDb() throws IOException {
		mockDb.close();
	}
	
	@Test
	public void testEmptyKey() {
		Trie trie = new Trie(mockDb);
		
		trie.update("", dog);
		assertEquals(dog, new String(trie.get("")));
	}
	
	@Test
	public void testInsertShortString() {
		Trie trie = new Trie(mockDb);
		
		trie.update(cat, dog);
		assertEquals(dog, new String(trie.get(cat)));
	}
	
	@Test
	public void testInsertLongString() {
		Trie trie = new Trie(mockDb);
		
		trie.update(cat, LONG_STRING);
		assertEquals(LONG_STRING, new String(trie.get(cat)));
	}
	
	@Test
	public void testInsertMultipleItems1() {
		Trie trie = new Trie(mockDb);
		trie.update(ca, dude);
		assertEquals(dude, new String(trie.get(ca)));
		
		trie.update(cat, dog);
		assertEquals(dog, new String(trie.get(cat)));
		
		trie.update(dog, test);
		assertEquals(test, new String(trie.get(dog)));
		
		trie.update(doge, LONG_STRING);
		assertEquals(LONG_STRING, new String(trie.get(doge)));
		
		trie.update(test, LONG_STRING);
		assertEquals(LONG_STRING, new String(trie.get(test)));

		// Test if everything is still there
		assertEquals(dude, new String(trie.get(ca)));
		assertEquals(dog, new String(trie.get(cat)));
		assertEquals(test, new String(trie.get(dog)));
		assertEquals(LONG_STRING, new String(trie.get(doge)));
		assertEquals(LONG_STRING, new String(trie.get(test)));
	}
	
	@Test
	public void testInsertMultipleItems2() {
		Trie trie = new Trie(mockDb);
		
		trie.update(cat, dog);
		assertEquals(dog, new String(trie.get(cat)));
		
		trie.update(ca, dude);
		assertEquals(dude, new String(trie.get(ca)));
		
		trie.update(doge, LONG_STRING);
		assertEquals(LONG_STRING, new String(trie.get(doge)));
		
		trie.update(dog, test);
		assertEquals(test, new String(trie.get(dog)));
		
		trie.update(test, LONG_STRING);
		assertEquals(LONG_STRING, new String(trie.get(test)));
		
		// Test if everything is still there
		assertEquals(dog, new String(trie.get(cat)));
		assertEquals(dude, new String(trie.get(ca)));
		assertEquals(LONG_STRING, new String(trie.get(doge)));
		assertEquals(test, new String(trie.get(dog)));
		assertEquals(LONG_STRING, new String(trie.get(test)));
	}
	
	@Test
	public void testUpdateShortToShortString() {
		Trie trie = new Trie(mockDb);
		
		trie.update(cat, dog);
		assertEquals(dog, new String(trie.get(cat)));
		
		trie.update(cat, dog+"1");
		assertEquals(dog+"1", new String(trie.get(cat)));
	}

	@Test
	public void testUpdateLongToLongString() {
		Trie trie = new Trie(mockDb);
		trie.update(cat, LONG_STRING);
		assertEquals(LONG_STRING, new String(trie.get(cat)));
		trie.update(cat, LONG_STRING+"1");
		assertEquals(LONG_STRING+"1", new String(trie.get(cat)));
	}
	
	@Test
	public void testUpdateShortToLongString() {
		Trie trie = new Trie(mockDb);
		
		trie.update(cat, dog);
		assertEquals(dog, new String(trie.get(cat)));
		
		trie.update(cat, LONG_STRING+"1");
		assertEquals(LONG_STRING+"1", new String(trie.get(cat)));
	}

	@Test
	public void testUpdateLongToShortString() {
		Trie trie = new Trie(mockDb);
		
		trie.update(cat, LONG_STRING);
		assertEquals(LONG_STRING, new String(trie.get(cat)));
		
		trie.update(cat, dog+"1");
		assertEquals(dog+"1", new String(trie.get(cat)));
	}

	@Test
	public void testDeleteShortString1() {
		String ROOT_HASH_BEFORE = "a9539c810cc2e8fa20785bdd78ec36cc1dab4b41f0d531e80a5e5fd25c3037ee";
		String ROOT_HASH_AFTER = "fc5120b4a711bca1f5bb54769525b11b3fb9a8d6ac0b8bf08cbb248770521758";
		Trie trie = new Trie(mockDb);
		
		trie.update(cat, dog);
		assertEquals(dog, new String(trie.get(cat)));
		
		trie.update(ca, dude);
		assertEquals(dude, new String(trie.get(ca)));
		assertEquals(ROOT_HASH_BEFORE, Hex.toHexString(trie.getRootHash()));

		trie.delete(ca);
		assertEquals("", new String(trie.get(ca)));
		assertEquals(ROOT_HASH_AFTER, Hex.toHexString(trie.getRootHash()));
	}
	
	@Test
	public void testDeleteShortString2() {
		String ROOT_HASH_BEFORE = "a9539c810cc2e8fa20785bdd78ec36cc1dab4b41f0d531e80a5e5fd25c3037ee";
		String ROOT_HASH_AFTER = "b25e1b5be78dbadf6c4e817c6d170bbb47e9916f8f6cc4607c5f3819ce98497b";
		Trie trie = new Trie(mockDb);
		
		trie.update(ca, dude);
		assertEquals(dude, new String(trie.get(ca)));
		
		trie.update(cat, dog);
		assertEquals(dog, new String(trie.get(cat)));
		assertEquals(ROOT_HASH_BEFORE, Hex.toHexString(trie.getRootHash()));
		
		trie.delete(cat);
		assertEquals("", new String(trie.get(cat)));
		assertEquals(ROOT_HASH_AFTER, Hex.toHexString(trie.getRootHash()));
	}
	
	@Test
	public void testDeleteShortString3() {
		String ROOT_HASH_BEFORE = "778ab82a7e8236ea2ff7bb9cfa46688e7241c1fd445bf2941416881a6ee192eb";
		String ROOT_HASH_AFTER = "05875807b8f3e735188d2479add82f96dee4db5aff00dc63f07a7e27d0deab65";
		Trie trie = new Trie(mockDb);
		
		trie.update(cat, dude);
		assertEquals(dude, new String(trie.get(cat)));
		
		trie.update(dog, test);
		assertEquals(test, new String(trie.get(dog)));
		assertEquals(ROOT_HASH_BEFORE, Hex.toHexString(trie.getRootHash()));
		
		trie.delete(dog);
		assertEquals("", new String(trie.get(dog)));
		assertEquals(ROOT_HASH_AFTER, Hex.toHexString(trie.getRootHash()));
	}

	@Test
	public void testDeleteLongString1() {
		String ROOT_HASH_BEFORE = "318961a1c8f3724286e8e80d312352f01450bc4892c165cc7614e1c2e5a0012a";
		String ROOT_HASH_AFTER = "63356ecf33b083e244122fca7a9b128cc7620d438d5d62e4f8b5168f1fb0527b";
		Trie trie = new Trie(mockDb);

		trie.update(cat, LONG_STRING);
		assertEquals(LONG_STRING, new String(trie.get(cat)));
		
		trie.update(dog, LONG_STRING);
		assertEquals(LONG_STRING, new String(trie.get(dog)));
		assertEquals(ROOT_HASH_BEFORE, Hex.toHexString(trie.getRootHash()));
		
		trie.delete(dog);
		assertEquals("", new String(trie.get(dog)));
		assertEquals(ROOT_HASH_AFTER, Hex.toHexString(trie.getRootHash()));
	}
	
	@Test
	public void testDeleteLongString2() {
		String ROOT_HASH_BEFORE = "e020de34ca26f8d373ff2c0a8ac3a4cb9032bfa7a194c68330b7ac3584a1d388";
		String ROOT_HASH_AFTER = "334511f0c4897677b782d13a6fa1e58e18de6b24879d57ced430bad5ac831cb2";
		Trie trie = new Trie(mockDb);
		
		trie.update(ca, LONG_STRING);
		assertEquals(LONG_STRING, new String(trie.get(ca)));

		trie.update(cat, LONG_STRING);
		assertEquals(LONG_STRING, new String(trie.get(cat)));
		assertEquals(ROOT_HASH_BEFORE, Hex.toHexString(trie.getRootHash()));
		
		trie.delete(cat);
		assertEquals("", new String(trie.get(cat)));
		assertEquals(ROOT_HASH_AFTER, Hex.toHexString(trie.getRootHash()));
	}
	
	@Test
	public void testDeleteLongString3() {
		String ROOT_HASH_BEFORE = "e020de34ca26f8d373ff2c0a8ac3a4cb9032bfa7a194c68330b7ac3584a1d388";
		String ROOT_HASH_AFTER = "63356ecf33b083e244122fca7a9b128cc7620d438d5d62e4f8b5168f1fb0527b";
		Trie trie = new Trie(mockDb);
		
		trie.update(cat, LONG_STRING);
		assertEquals(LONG_STRING, new String(trie.get(cat)));
		
		trie.update(ca, LONG_STRING);
		assertEquals(LONG_STRING, new String(trie.get(ca)));
		assertEquals(ROOT_HASH_BEFORE, Hex.toHexString(trie.getRootHash()));
		
		trie.delete(ca);
		assertEquals("", new String(trie.get(ca)));
		assertEquals(ROOT_HASH_AFTER, Hex.toHexString(trie.getRootHash()));
	}	

	@Test
	public void testDeleteMultipleItems1() {
		String ROOT_HASH_BEFORE = "3a784eddf1936515f0313b073f99e3bd65c38689021d24855f62a9601ea41717";
		String ROOT_HASH_AFTER1 = "60a2e75cfa153c4af2783bd6cb48fd6bed84c6381bc2c8f02792c046b46c0653";
		String ROOT_HASH_AFTER2 = "a84739b4762ddf15e3acc4e6957e5ab2bbfaaef00fe9d436a7369c6f058ec90d";
		Trie trie = new Trie(mockDb);
		
		trie.update(cat, dog);
		assertEquals(dog, new String(trie.get(cat)));
		
		trie.update(ca, dude);
		assertEquals(dude, new String(trie.get(ca)));
		
		trie.update(doge, LONG_STRING);
		assertEquals(LONG_STRING, new String(trie.get(doge)));
		
		trie.update(dog, test);
		assertEquals(test, new String(trie.get(dog)));
		
		trie.update(test, LONG_STRING);
		assertEquals(LONG_STRING, new String(trie.get(test)));
		assertEquals(ROOT_HASH_BEFORE, Hex.toHexString(trie.getRootHash()));

		trie.delete(dog);
		assertEquals("", new String(trie.get(dog)));
		assertEquals(ROOT_HASH_AFTER1, Hex.toHexString(trie.getRootHash()));
		
		trie.delete(test);
		assertEquals("", new String(trie.get(test)));
		assertEquals(ROOT_HASH_AFTER2, Hex.toHexString(trie.getRootHash()));
	}	
	
	@Test
	public void testDeleteMultipleItems2() {
		String ROOT_HASH_BEFORE = "cf1ed2b6c4b6558f70ef0ecf76bfbee96af785cb5d5e7bfc37f9804ad8d0fb56";
		String ROOT_HASH_AFTER1 = "f586af4a476ba853fca8cea1fbde27cd17d537d18f64269fe09b02aa7fe55a9e";
		String ROOT_HASH_AFTER2 = "c59fdc16a80b11cc2f7a8b107bb0c954c0d8059e49c760ec3660eea64053ac91";
		
		Trie trie = new Trie(mockDb);
		trie.update(c, LONG_STRING);
		assertEquals(LONG_STRING, new String(trie.get(c)));
		
		trie.update(ca, LONG_STRING);
		assertEquals(LONG_STRING, new String(trie.get(ca)));
		
		trie.update(cat, LONG_STRING);
		assertEquals(LONG_STRING, new String(trie.get(cat)));
		assertEquals(ROOT_HASH_BEFORE, Hex.toHexString(trie.getRootHash()));
		
		trie.delete(ca);
		assertEquals("", new String(trie.get(ca)));
		assertEquals(ROOT_HASH_AFTER1, Hex.toHexString(trie.getRootHash()));
		
		trie.delete(cat);
		assertEquals("", new String(trie.get(cat)));
		assertEquals(ROOT_HASH_AFTER2, Hex.toHexString(trie.getRootHash()));
	}
	
	@Test
	public void testDeleteAll() {
		String ROOT_HASH_BEFORE = "a84739b4762ddf15e3acc4e6957e5ab2bbfaaef00fe9d436a7369c6f058ec90d";
		Trie trie = new Trie(mockDb);
		assertEquals(ROOT_HASH_EMPTY, Hex.toHexString(trie.getRootHash()));
		
		trie.update(ca, dude);
		trie.update(cat, dog);
		trie.update(doge, LONG_STRING);
		assertEquals(ROOT_HASH_BEFORE, Hex.toHexString(trie.getRootHash()));
		
		trie.delete(ca);
		trie.delete(cat);
		trie.delete(doge);
		assertEquals(ROOT_HASH_EMPTY, Hex.toHexString(trie.getRootHash()));
	}

	@Test
	public void testTrieCmp() {
		Trie trie1 = new Trie(mockDb);
		Trie trie2 = new Trie(mockDb);

		trie1.update(doge, LONG_STRING);
		trie2.update(doge, LONG_STRING);
		assertTrue("Expected tries to be equal", trie1.cmp(trie2));
		assertEquals(Hex.toHexString(trie1.getRootHash()), Hex.toHexString(trie2.getRootHash()));
		
		trie1.update(dog, LONG_STRING);
		trie2.update(cat, LONG_STRING);
		assertFalse("Expected tries not to be equal", trie1.cmp(trie2));
		assertNotEquals(Hex.toHexString(trie1.getRootHash()), Hex.toHexString(trie2.getRootHash()));
	}
	
	@Test
	public void testTrieSync() {
		Trie trie = new Trie(mockDb);

		trie.update(dog, LONG_STRING);
		assertEquals("Expected no data in database", mockDb.getAddedItems(), 0);

		trie.sync();
		assertNotEquals("Expected data to be persisted", mockDb.getAddedItems(), 0);
	}

	@Test
	public void TestTrieDirtyTracking() {
		Trie trie = new Trie(mockDb);
		trie.update(dog, LONG_STRING);
		assertTrue("Expected trie to be dirty", trie.getCache().isDirty());

		trie.sync();
		assertFalse("Expected trie not to be dirty", trie.getCache().isDirty());

		trie.update(test, LONG_STRING);
		trie.getCache().undo();
		assertFalse("Expected trie not to be dirty", trie.getCache().isDirty());
	}

	@Test
	public void TestTrieReset() {
		Trie trie = new Trie(mockDb);

		trie.update(cat, LONG_STRING);
		assertNotEquals("Expected cached nodes", 0, trie.getCache().getNodes().size());

		trie.getCache().undo();

		assertEquals("Expected no nodes after undo", 0, trie.getCache().getNodes().size());
	}
	
	@Test
	public void testTrieCopy() {
		Trie trie = new Trie(mockDb);
		trie.update("doe", "reindeer");
		Trie trie2 = trie.copy();
		assertFalse(trie.equals(trie2)); // avoid possibility that its just a reference copy
		assertEquals(Hex.toHexString(trie.getRootHash()), Hex.toHexString(trie2.getRootHash()));
		assertTrue(trie.cmp(trie2));
	}
	
	@Test
	public void testTrieUndo() {
		Trie trie = new Trie(mockDb);
		trie.update("doe", "reindeer");
		assertEquals("11a0327cfcc5b7689b6b6d727e1f5f8846c1137caaa9fc871ba31b7cce1b703e", Hex.toHexString(trie.getRootHash()));
		trie.sync();
		
		trie.update("dog", "puppy");
		assertEquals("05ae693aac2107336a79309e0c60b24a7aac6aa3edecaef593921500d33c63c4", Hex.toHexString(trie.getRootHash()));
		
		trie.undo();
		assertEquals("11a0327cfcc5b7689b6b6d727e1f5f8846c1137caaa9fc871ba31b7cce1b703e", Hex.toHexString(trie.getRootHash()));
	}
	
	// Using tests from: https://github.com/ethereum/tests/blob/master/trietest.json
	
	@Test
	public void testSingleItem() {
		Trie trie = new Trie(mockDb);
		trie.update("A", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		
	    assertEquals("d23786fb4a010da3ce639d66d5e904a11dbc02746d1ce25029e53290cabf28ab", Hex.toHexString(trie.getRootHash()));
	  }
	
	@Test
	public void testDogs() {
		Trie trie = new Trie(mockDb);
		trie.update("doe", "reindeer");
		assertEquals("11a0327cfcc5b7689b6b6d727e1f5f8846c1137caaa9fc871ba31b7cce1b703e", Hex.toHexString(trie.getRootHash()));
		
		trie.update("dog", "puppy");
		assertEquals("05ae693aac2107336a79309e0c60b24a7aac6aa3edecaef593921500d33c63c4", Hex.toHexString(trie.getRootHash()));
		
		trie.update("dogglesworth", "cat");
		assertEquals("8aad789dff2f538bca5d8ea56e8abe10f4c7ba3a5dea95fea4cd6e7c3a1168d3", Hex.toHexString(trie.getRootHash()));
	  }
	
	  @Test
	  public void testPuppy() {
		  Trie trie = new Trie(mockDb);
		  trie.update("do", "verb");
		  trie.update("horse", "stallion");
		  trie.update("doge", "coin");
		  trie.update("dog", "puppy");
		  
		  assertEquals("5991bb8c6514148a29db676a14ac506cd2cd5775ace63c30a4fe457715e9ac84", Hex.toHexString(trie.getRootHash()));
	  }
	  
	  @Test
	  public void testEmptyValues() {
		  Trie trie = new Trie(mockDb);
		  trie.update("do", "verb");
		  trie.update("ether", "wookiedoo");
		  trie.update("horse", "stallion");
		  trie.update("shaman", "horse");
		  trie.update("doge", "coin");
		  trie.update("ether", "");
		  trie.update("dog", "puppy");
		  trie.update("shaman", "");
		  
		  assertEquals("5991bb8c6514148a29db676a14ac506cd2cd5775ace63c30a4fe457715e9ac84", Hex.toHexString(trie.getRootHash()));
	  }
	  
	  @Test
	  public void testFoo() {
		  Trie trie = new Trie(mockDb);
		  trie.update("foo", "bar");
		  trie.update("food", "bat");
		  trie.update("food", "bass");
		  
		  assertEquals("17beaa1648bafa633cda809c90c04af50fc8aed3cb40d16efbddee6fdf63c4c3", Hex.toHexString(trie.getRootHash()));
	  }
	  
	  @Test
	  public void testSmallValues() {
		  Trie trie = new Trie(mockDb);
		  
		  trie.update("be", "e");
		  trie.update("dog", "puppy");
		  trie.update("bed", "d");
		  assertEquals("3f67c7a47520f79faa29255d2d3c084a7a6df0453116ed7232ff10277a8be68b", Hex.toHexString(trie.getRootHash()));
	  }

	  @Test
	  public void testTesty() {
		Trie trie = new Trie(mockDb);

		trie.update("test", "test");
		assertEquals("85d106d4edff3b7a4889e91251d0a87d7c17a1dda648ebdba8c6060825be23b8", Hex.toHexString(trie.getRootHash()));
		
		trie.update("te", "testy");
		assertEquals("8452568af70d8d140f58d941338542f645fcca50094b20f3c3d8c3df49337928", Hex.toHexString(trie.getRootHash()));
	  }

	  @Test
	  public void testGetFromRootNode() {
			Trie trie1 = new Trie(mockDb);
			trie1.update(cat, LONG_STRING);
			trie1.sync();
			Trie trie2 = new Trie(mockDb, trie1.getRootHash());
			assertEquals(LONG_STRING, new String(trie2.get(cat)));
	  }


/*
        0x7645b9fbf1b51e6b980801fafe6bbc22d2ebe218 0x517eaccda568f3fa24915fed8add49d3b743b3764c0bc495b19a47c54dbc3d62 0x 0x1
        0x0000000000000000000000000000000000000000000000000000000000000010 0x947e70f9460402290a3e487dae01f610a1a8218fda
        0x0000000000000000000000000000000000000000000000000000000000000014 0x40
        0x0000000000000000000000000000000000000000000000000000000000000016 0x94412e0c4f0102f3f0ac63f0a125bce36ca75d4e0d
        0x0000000000000000000000000000000000000000000000000000000000000017 0x01
*/

    @Test
    public void storageHashCalc_1(){

        byte[] key1 = Hex.decode("0000000000000000000000000000000000000000000000000000000000000010");
        byte[] key2 = Hex.decode("0000000000000000000000000000000000000000000000000000000000000014");
        byte[] key3 = Hex.decode("0000000000000000000000000000000000000000000000000000000000000016");
        byte[] key4 = Hex.decode("0000000000000000000000000000000000000000000000000000000000000017");

        byte[] val1 = Hex.decode("947e70f9460402290a3e487dae01f610a1a8218fda");
        byte[] val2 = Hex.decode("40");
        byte[] val3 = Hex.decode("94412e0c4f0102f3f0ac63f0a125bce36ca75d4e0d");
        byte[] val4 = Hex.decode("01");

        Trie storage = new Trie(new org.ethereum.trie.MockDB());
        storage.update(key1, val1);
        storage.update(key2, val2);
        storage.update(key3, val3);
        storage.update(key4, val4);

        String hash = Hex.toHexString(storage.getRootHash());

        System.out.println(hash);
        Assert.assertEquals("517eaccda568f3fa24915fed8add49d3b743b3764c0bc495b19a47c54dbc3d62", hash);
    }

    @Test
    public void storageHashCalc_2(){

        byte[] key1 = Hex.decode("0000000000000000000000000000000000000000000000000000000000000010");
        byte[] key2 = Hex.decode("0000000000000000000000000000000000000000000000000000000000000014");
        byte[] key3 = Hex.decode("0000000000000000000000000000000000000000000000000000000000000017");
        byte[] key4 = Hex.decode("0000000000000000000000000000000000000000000000000000000000000016");

        byte[] val1 = Hex.decode("947e70f9460402290a3e487dae01f610a1a8218fda");
        byte[] val2 = Hex.decode("40");
        byte[] val3 = Hex.decode("01");
        byte[] val4 = Hex.decode("94412e0c4f0102f3f0ac63f0a125bce36ca75d4e0d");

        Trie storage = new Trie(new org.ethereum.trie.MockDB());
        storage.update(key1, val1);
        storage.update(key2, val2);
        storage.update(key3, val3);
        storage.update(key4, val4);

        String hash = Hex.toHexString(storage.getRootHash());

        System.out.println(hash);
        Assert.assertEquals("255b5df6f1ba5963cb21535d59ee7b65532e6b071065587c5b52fcc4e55207a2", hash);
    }

}
