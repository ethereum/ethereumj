package org.ethereum.trie;

import static org.junit.Assert.*;

import org.ethereum.trie.Trie;
import org.junit.Test;

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
	
	@Test
	public void testEmptyKey() {
		Trie trie = new Trie(mockDb);
		
		trie.update("", dog);
		assertEquals(dog, trie.get(""));
	}
	
	@Test
	public void testInsertShortString() {
		Trie trie = new Trie(mockDb);
		
		trie.update(cat, dog);
		assertEquals(dog, trie.get(cat));
	}
	
	@Test
	public void testInsertLongString() {
		Trie trie = new Trie(mockDb);
		
		trie.update(cat, LONG_STRING);
		assertEquals(LONG_STRING, trie.get(cat));
	}
	
	@Test
	public void testInsertMultipleItems1() {
		Trie trie = new Trie(mockDb);
		trie.update(ca, dude);
		assertEquals(dude, trie.get(ca));
		
		trie.update(cat, dog);
		assertEquals(dog, trie.get(cat));
		
		trie.update(dog, test);
		assertEquals(test, trie.get(dog));
		
		trie.update(doge, LONG_STRING);
		assertEquals(LONG_STRING, trie.get(doge));
		
		trie.update(test, LONG_STRING);
		assertEquals(LONG_STRING, trie.get(test));

		// Test if everything is still there
		assertEquals(dude, trie.get(ca));
		assertEquals(dog, trie.get(cat));
		assertEquals(test, trie.get(dog));
		assertEquals(LONG_STRING, trie.get(doge));
		assertEquals(LONG_STRING, trie.get(test));
	}
	
	@Test
	public void testInsertMultipleItems2() {
		Trie trie = new Trie(mockDb);
		
		trie.update(cat, dog);
		assertEquals(dog, trie.get(cat));
		
		trie.update(ca, dude);
		assertEquals(dude, trie.get(ca));
		
		trie.update(doge, LONG_STRING);
		assertEquals(LONG_STRING, trie.get(doge));
		
		trie.update(dog, test);
		assertEquals(test, trie.get(dog));
		
		trie.update(test, LONG_STRING);
		assertEquals(LONG_STRING, trie.get(test));
		
		// Test if everything is still there
		assertEquals(dog, trie.get(cat));
		assertEquals(dude, trie.get(ca));
		assertEquals(LONG_STRING, trie.get(doge));
		assertEquals(test, trie.get(dog));
		assertEquals(LONG_STRING, trie.get(test));
	}
	
	@Test
	public void testUpdateShortToShortString() {
		Trie trie = new Trie(mockDb);
		
		trie.update(cat, dog);
		assertEquals(dog, trie.get(cat));
		
		trie.update(cat, dog+"1");
		assertEquals(dog+"1", trie.get(cat));
	}

	@Test
	public void testUpdateLongToLongString() {
		Trie trie = new Trie(mockDb);
		trie.update(cat, LONG_STRING);
		assertEquals(LONG_STRING, trie.get(cat));
		trie.update(cat, LONG_STRING+"1");
		assertEquals(LONG_STRING+"1", trie.get(cat));
	}
	
	@Test
	public void testUpdateShortToLongString() {
		Trie trie = new Trie(mockDb);
		
		trie.update(cat, dog);
		assertEquals(dog, trie.get(cat));
		
		trie.update(cat, LONG_STRING+"1");
		assertEquals(LONG_STRING+"1", trie.get(cat));
	}

	@Test
	public void testUpdateLongToShortString() {
		Trie trie = new Trie(mockDb);
		
		trie.update(cat, LONG_STRING);
		assertEquals(LONG_STRING, trie.get(cat));
		
		trie.update(cat, dog+"1");
		assertEquals(dog+"1", trie.get(cat));
	}

	@Test
	public void testDeleteShortString1() {
		String ROOT_HASH_BEFORE = "a9539c810cc2e8fa20785bdd78ec36cc1dab4b41f0d531e80a5e5fd25c3037ee";
		String ROOT_HASH_AFTER = "fc5120b4a711bca1f5bb54769525b11b3fb9a8d6ac0b8bf08cbb248770521758";
		Trie trie = new Trie(mockDb);
		
		trie.update(cat, dog);
		assertEquals(dog, trie.get(cat));
		
		trie.update(ca, dude);
		assertEquals(dude, trie.get(ca));
		assertEquals(ROOT_HASH_BEFORE, trie.getRootHash());

		trie.delete(ca);
		assertEquals("", trie.get(ca));
		assertEquals(ROOT_HASH_AFTER, trie.getRootHash());
	}
	
	@Test
	public void testDeleteShortString2() {
		String ROOT_HASH_BEFORE = "a9539c810cc2e8fa20785bdd78ec36cc1dab4b41f0d531e80a5e5fd25c3037ee";
		String ROOT_HASH_AFTER = "b25e1b5be78dbadf6c4e817c6d170bbb47e9916f8f6cc4607c5f3819ce98497b";
		Trie trie = new Trie(mockDb);
		
		trie.update(ca, dude);
		assertEquals(dude, trie.get(ca));
		
		trie.update(cat, dog);
		assertEquals(dog, trie.get(cat));
		assertEquals(ROOT_HASH_BEFORE, trie.getRootHash());
		
		trie.delete(cat);
		assertEquals("", trie.get(cat));
		assertEquals(ROOT_HASH_AFTER, trie.getRootHash());
	}
	
	@Test
	public void testDeleteShortString3() {
		String ROOT_HASH_BEFORE = "778ab82a7e8236ea2ff7bb9cfa46688e7241c1fd445bf2941416881a6ee192eb";
		String ROOT_HASH_AFTER = "05875807b8f3e735188d2479add82f96dee4db5aff00dc63f07a7e27d0deab65";
		Trie trie = new Trie(mockDb);
		
		trie.update(cat, dude);
		assertEquals(dude, trie.get(cat));
		
		trie.update(dog, test);
		assertEquals(test, trie.get(dog));
		assertEquals(ROOT_HASH_BEFORE, trie.getRootHash());
		
		trie.delete(dog);
		assertEquals("", trie.get(dog));
		assertEquals(ROOT_HASH_AFTER, trie.getRootHash());
	}

	@Test
	public void testDeleteLongString1() {
		String ROOT_HASH_BEFORE = "318961a1c8f3724286e8e80d312352f01450bc4892c165cc7614e1c2e5a0012a";
		String ROOT_HASH_AFTER = "63356ecf33b083e244122fca7a9b128cc7620d438d5d62e4f8b5168f1fb0527b";
		Trie trie = new Trie(mockDb);

		trie.update(cat, LONG_STRING);
		assertEquals(LONG_STRING, trie.get(cat));
		
		trie.update(dog, LONG_STRING);
		assertEquals(LONG_STRING, trie.get(dog));
		assertEquals(ROOT_HASH_BEFORE, trie.getRootHash());
		
		trie.delete(dog);
		assertEquals("", trie.get(dog));
		assertEquals(ROOT_HASH_AFTER, trie.getRootHash());
	}
	
	@Test
	public void testDeleteLongString2() {
		String ROOT_HASH_BEFORE = "e020de34ca26f8d373ff2c0a8ac3a4cb9032bfa7a194c68330b7ac3584a1d388";
		String ROOT_HASH_AFTER = "334511f0c4897677b782d13a6fa1e58e18de6b24879d57ced430bad5ac831cb2";
		Trie trie = new Trie(mockDb);
		
		trie.update(ca, LONG_STRING);
		assertEquals(LONG_STRING, trie.get(ca));

		trie.update(cat, LONG_STRING);
		assertEquals(LONG_STRING, trie.get(cat));
		assertEquals(ROOT_HASH_BEFORE, trie.getRootHash());
		
		trie.delete(cat);
		assertEquals("", trie.get(cat));
		assertEquals(ROOT_HASH_AFTER, trie.getRootHash());
	}
	
	@Test
	public void testDeleteLongString3() {
		String ROOT_HASH_BEFORE = "e020de34ca26f8d373ff2c0a8ac3a4cb9032bfa7a194c68330b7ac3584a1d388";
		String ROOT_HASH_AFTER = "63356ecf33b083e244122fca7a9b128cc7620d438d5d62e4f8b5168f1fb0527b";
		Trie trie = new Trie(mockDb);
		
		trie.update(cat, LONG_STRING);
		assertEquals(LONG_STRING, trie.get(cat));
		
		trie.update(ca, LONG_STRING);
		assertEquals(LONG_STRING, trie.get(ca));
		assertEquals(ROOT_HASH_BEFORE, trie.getRootHash());
		
		trie.delete(ca);
		assertEquals("", trie.get(ca));
		assertEquals(ROOT_HASH_AFTER, trie.getRootHash());
	}	

	@Test
	public void testDeleteMultipleItems1() {
		String ROOT_HASH_BEFORE = "3a784eddf1936515f0313b073f99e3bd65c38689021d24855f62a9601ea41717";
		String ROOT_HASH_AFTER1 = "60a2e75cfa153c4af2783bd6cb48fd6bed84c6381bc2c8f02792c046b46c0653";
		String ROOT_HASH_AFTER2 = "a84739b4762ddf15e3acc4e6957e5ab2bbfaaef00fe9d436a7369c6f058ec90d";
		Trie trie = new Trie(mockDb);
		
		trie.update(cat, dog);
		assertEquals(dog, trie.get(cat));
		
		trie.update(ca, dude);
		assertEquals(dude, trie.get(ca));
		
		trie.update(doge, LONG_STRING);
		assertEquals(LONG_STRING, trie.get(doge));
		
		trie.update(dog, test);
		assertEquals(test, trie.get(dog));
		
		trie.update(test, LONG_STRING);
		assertEquals(LONG_STRING, trie.get(test));
		assertEquals(ROOT_HASH_BEFORE, trie.getRootHash());

		trie.delete(dog);
		assertEquals("", trie.get(dog));
		assertEquals(ROOT_HASH_AFTER1, trie.getRootHash());
		
		trie.delete(test);
		assertEquals("", trie.get(test));
		assertEquals(ROOT_HASH_AFTER2, trie.getRootHash());
	}	
	
	@Test
	public void testDeleteMultipleItems2() {
		String ROOT_HASH_BEFORE = "cf1ed2b6c4b6558f70ef0ecf76bfbee96af785cb5d5e7bfc37f9804ad8d0fb56";
		String ROOT_HASH_AFTER1 = "f586af4a476ba853fca8cea1fbde27cd17d537d18f64269fe09b02aa7fe55a9e";
		String ROOT_HASH_AFTER2 = "c59fdc16a80b11cc2f7a8b107bb0c954c0d8059e49c760ec3660eea64053ac91";
		
		Trie trie = new Trie(mockDb);
		trie.update(c, LONG_STRING);
		assertEquals(LONG_STRING, trie.get(c));
		
		trie.update(ca, LONG_STRING);
		assertEquals(LONG_STRING, trie.get(ca));
		
		trie.update(cat, LONG_STRING);
		assertEquals(LONG_STRING, trie.get(cat));
		assertEquals(ROOT_HASH_BEFORE, trie.getRootHash());
		
		trie.delete(ca);
		assertEquals("", trie.get(ca));
		assertEquals(ROOT_HASH_AFTER1, trie.getRootHash());
		
		trie.delete(cat);
		assertEquals("", trie.get(cat));
		assertEquals(ROOT_HASH_AFTER2, trie.getRootHash());
	}
	
	@Test
	public void testDeleteAll() {
		String ROOT_HASH_BEFORE = "a84739b4762ddf15e3acc4e6957e5ab2bbfaaef00fe9d436a7369c6f058ec90d";
		Trie trie = new Trie(mockDb);
		assertEquals(ROOT_HASH_EMPTY, trie.getRootHash());
		
		trie.update(ca, dude);
		trie.update(cat, dog);
		trie.update(doge, LONG_STRING);
		assertEquals(ROOT_HASH_BEFORE, trie.getRootHash());
		
		trie.delete(ca);
		trie.delete(cat);
		trie.delete(doge);
		assertEquals(ROOT_HASH_EMPTY, trie.getRootHash());
	}

	@Test
	public void testTrieCmp() {
		Trie trie1 = new Trie(mockDb);
		Trie trie2 = new Trie(mockDb);

		trie1.update(doge, LONG_STRING);
		trie2.update(doge, LONG_STRING);
		assertTrue("Expected tries to be equal", trie1.cmp(trie2));
		assertEquals(trie1.getRootHash(), trie2.getRootHash());
		
		trie1.update(dog, LONG_STRING);
		trie2.update(cat, LONG_STRING);
		assertFalse("Expected tries not to be equal", trie1.cmp(trie2));
		assertNotEquals(trie1.getRootHash(), trie2.getRootHash());
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
		fail("To be implemented");
	}
	
	@Test
	public void testTrieUndo() {
		fail("To be implemented");
	}
	
	// Using tests from: https://github.com/ethereum/tests/blob/master/trietest.json
	
	@Test
	public void testSingleItem() {
		Trie trie = new Trie(mockDb);
		trie.update("A", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		
	    assertEquals("d23786fb4a010da3ce639d66d5e904a11dbc02746d1ce25029e53290cabf28ab", trie.getRootHash());
	  }
	
	@Test
	public void testDogs() {
		Trie trie = new Trie(mockDb);
		trie.update("doe", "reindeer");
		assertEquals("11a0327cfcc5b7689b6b6d727e1f5f8846c1137caaa9fc871ba31b7cce1b703e", trie.getRootHash());
		
		trie.update("dog", "puppy");
		assertEquals("05ae693aac2107336a79309e0c60b24a7aac6aa3edecaef593921500d33c63c4", trie.getRootHash());
		
		trie.update("dogglesworth", "cat");
		assertEquals("8aad789dff2f538bca5d8ea56e8abe10f4c7ba3a5dea95fea4cd6e7c3a1168d3", trie.getRootHash());
	  }
	
	  @Test
	  public void testPuppy() {
		  Trie trie = new Trie(mockDb);
		  trie.update("do", "verb");
		  trie.update("horse", "stallion");
		  trie.update("doge", "coin");
		  trie.update("dog", "puppy");
		  
		  assertEquals("5991bb8c6514148a29db676a14ac506cd2cd5775ace63c30a4fe457715e9ac84", trie.getRootHash());
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
		  
		  assertEquals("5991bb8c6514148a29db676a14ac506cd2cd5775ace63c30a4fe457715e9ac84", trie.getRootHash());
	  }
	  
	  @Test
	  public void testFoo() {
		  Trie trie = new Trie(mockDb);
		  trie.update("foo", "bar");
		  trie.update("food", "bat");
		  trie.update("food", "bass");
		  
		  assertEquals("17beaa1648bafa633cda809c90c04af50fc8aed3cb40d16efbddee6fdf63c4c3", trie.getRootHash());
	  }
	  
	  @Test
	  public void testSmallValues() {
		  Trie trie = new Trie(mockDb);
		  
		  trie.update("be", "e");
		  trie.update("dog", "puppy");
		  trie.update("bed", "d");
		  assertEquals("3f67c7a47520f79faa29255d2d3c084a7a6df0453116ed7232ff10277a8be68b", trie.getRootHash());
	  }

	  @Test
	  public void testTesty() {
		Trie trie = new Trie(mockDb);

		trie.update("test", "test");
		assertEquals("85d106d4edff3b7a4889e91251d0a87d7c17a1dda648ebdba8c6060825be23b8", trie.getRootHash());
		
		trie.update("te", "testy");
		assertEquals("8452568af70d8d140f58d941338542f645fcca50094b20f3c3d8c3df49337928", trie.getRootHash());
	  }
}
