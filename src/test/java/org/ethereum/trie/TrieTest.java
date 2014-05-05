package org.ethereum.trie;

import static org.junit.Assert.*;

import org.ethereum.trie.Trie;
import org.junit.Test;

import com.cedarsoftware.util.DeepEquals;

public class TrieTest {

	private static String LONG_STRING = "1234567890abcdefghijklmnopqrstuvwxxzABCEFGHIJKLMNOPQRSTUVWXYZ";
	
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
		String result = trie.get("");
		assertEquals(dog, result);
	}
	
	@Test
	public void testInsertShortString() {
		Trie trie = new Trie(mockDb);
		trie.update(cat, dog);
		String result = trie.get(cat);
		assertEquals(dog, result);
	}
	
	@Test
	public void testInsertLongString() {
		Trie trie = new Trie(mockDb);
		trie.update(cat, LONG_STRING);
		String result = trie.get(cat);
		assertEquals(LONG_STRING, result);
	}
	
	@Test
	public void testInsertMultipleItems1() {
		Trie trie = new Trie(mockDb);
		trie.update(ca, dude);
		trie.update(cat, dog);
		trie.update(dog, test);
		trie.update(doge, LONG_STRING);
		trie.update(test, LONG_STRING);
		String result1 = trie.get(ca);
		String result2 = trie.get(cat);
		String result3 = trie.get(dog);
		String result4 = trie.get(doge);
		String result5 = trie.get(test);
		assertEquals(dude, result1);
		assertEquals(dog, result2);
		assertEquals(test, result3);
		assertEquals(LONG_STRING, result4);
		assertEquals(LONG_STRING, result5);
	}
	
	@Test
	public void testInsertMultipleItems2() {
		Trie trie = new Trie(mockDb);
		trie.update(cat, dog);
		trie.update(ca, dude);
		trie.update(doge, LONG_STRING);
		trie.update(dog, test);
		trie.update(test, LONG_STRING);
		String result1 = trie.get(cat);
		String result2 = trie.get(ca);
		String result3 = trie.get(doge);
		String result4 = trie.get(dog);
		String result5 = trie.get(test);
		assertEquals(dog, result1);
		assertEquals(dude, result2);
		assertEquals(LONG_STRING, result3);
		assertEquals(test, result4);
		assertEquals(LONG_STRING, result5);
	}
	
	@Test
	public void testUpdateShortToShortString() {
		Trie trie = new Trie(mockDb);
		trie.update(cat, dog);
		trie.update(cat, dog+"1");
		String result = trie.get(cat);
		assertEquals(dog+"1", result);
	}

	@Test
	public void testUpdateLongToLongString() {
		Trie trie = new Trie(mockDb);
		trie.update(cat, LONG_STRING);
		trie.update(cat, LONG_STRING+"1");
		String result = trie.get(cat);
		assertEquals(LONG_STRING+"1", result);
	}
	
	@Test
	public void testUpdateShortToLongString() {
		Trie trie = new Trie(mockDb);
		trie.update(cat, dog);
		trie.update(cat, LONG_STRING+"1");
		String result = trie.get(cat);
		assertEquals(LONG_STRING+"1", result);
	}

	@Test
	public void testUpdateLongToShortString() {
		Trie trie = new Trie(mockDb);
		trie.update(cat, LONG_STRING);
		trie.update(cat, dog+"1");
		String result = trie.get(cat);
		assertEquals(dog+"1", result);
	}

	@Test
	public void testDeleteShortString1() {
		Trie trie = new Trie(mockDb);
		trie.update(cat, dog);
		Object expected = trie.getRoot();
		trie.update(ca, dude);
		trie.delete(ca);
		Object result = trie.getRoot();
		assertTrue("Tries are not equal", DeepEquals.deepEquals(expected, result));
	}
	
	@Test
	public void testDeleteShortString2() {
		Trie trie = new Trie(mockDb);
		trie.update(ca, dude);
		Object expected = trie.getRoot();
		trie.update(cat, dog);
		trie.delete(cat);
		Object result = trie.getRoot();
		assertTrue("Tries are not equal", DeepEquals.deepEquals(expected, result));
	}
	
	@Test
	public void testDeleteShortString3() {
		Trie trie = new Trie(mockDb);
		trie.update(cat, dude);
		Object expected = trie.getRoot();
		trie.update(dog, test);
		trie.delete(dog);
		Object result = trie.getRoot();
		assertTrue("Tries are not equal", DeepEquals.deepEquals(expected, result));
	}

	@Test
	public void testDeleteLongString1() {
		Trie trie = new Trie(mockDb);
		trie.update(cat, LONG_STRING);
		Object expected = trie.getRoot();
		trie.update(dog, LONG_STRING);
		trie.delete(dog);
		Object result = trie.getRoot();
		assertTrue("Tries are not equal", DeepEquals.deepEquals(expected, result));
	}
	
	@Test
	public void testDeleteLongString2() {
		Trie trie = new Trie(mockDb);
		trie.update(ca, LONG_STRING);
		Object expected = trie.getRoot();
		trie.update(cat, LONG_STRING);
		trie.delete(cat);
		Object result = trie.getRoot();
		assertTrue("Tries are not equal", DeepEquals.deepEquals(expected, result));
	}
	
	@Test
	public void testDeleteLongString3() {
		Trie trie = new Trie(mockDb);
		trie.update(cat, LONG_STRING);
		Object expected = trie.getRoot();
		trie.update(ca, LONG_STRING);
		trie.delete(ca);
		Object result = trie.getRoot();
		assertTrue("Tries are not equal", DeepEquals.deepEquals(expected, result));
	}	

	@Test
	public void testDeleteMultipleItems1() {
		Trie trie = new Trie(mockDb);
		trie.update(cat, dog);
		trie.update(ca, dude);
		trie.update(doge, LONG_STRING);
		Object expected = trie.getRoot();
		trie.update(dog, test);
		trie.update(test, LONG_STRING);

		trie.delete(dog);
		trie.delete(test);
		Object result = trie.getRoot();
		assertTrue("Tries are not equal", DeepEquals.deepEquals(expected, result));
	}	
	
	@Test
	public void testDeleteMultipleItems2() {
		Trie trie = new Trie(mockDb);
		trie.update(c, LONG_STRING);
		Object expected = trie.getRoot();
		trie.update(ca, LONG_STRING);
		trie.update(cat, LONG_STRING);

		trie.delete(ca);
		trie.delete(cat);
		Object result = trie.getRoot();
		assertTrue("Tries are not equal", DeepEquals.deepEquals(expected, result));

	}
	
	@Test
	public void testDeleteAll() {
		Trie trie = new Trie(mockDb);
		Object expected = trie.getRoot();
		trie.update(ca, dude);
		trie.update(cat, dog);
		trie.update(doge, LONG_STRING);
		trie.delete(ca);
		trie.delete(cat);
		trie.delete(doge);
		Object result = trie.getRoot();
		assertTrue("Tries are not equal", DeepEquals.deepEquals(expected, result));
	}


	@Test
	public void testTrieCmp() {
		Trie trie1 = new Trie(mockDb);
		Trie trie2 = new Trie(mockDb);

		trie1.update(doge, LONG_STRING);
		trie2.update(doge, LONG_STRING);
		assertTrue("Expected tries to be equal", trie1.cmp(trie2));

		trie1.update(dog, LONG_STRING);
		trie2.update(cat, LONG_STRING);
		assertFalse("Expected tries not to be equal", trie1.cmp(trie2));
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
}
