package org.ethereum.trie;

import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.util.ByteUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 11/06/2014 19:47
 */
public class TrackTrie implements TrieFacade {

	private TrieFacade trie;

	private boolean trackingChanges = false;
	private Map<ByteArrayWrapper, byte[]> changes;
	private List<ByteArrayWrapper> deletes;

	public TrackTrie(TrieFacade trie) {
		this.trie = trie;
	}

	public void startTrack() {
		changes = new HashMap<>();
		deletes = new ArrayList<>();
		trackingChanges = true;
	}

	public void commitTrack() {
		for (ByteArrayWrapper key : changes.keySet()) {
			trie.update(key.getData(), changes.get(key));
		}
		for (ByteArrayWrapper key : deletes) {
			trie.update(key.getData(), ByteUtil.EMPTY_BYTE_ARRAY);
		}
		changes = null;
		trackingChanges = false;
	}

	public void rollbackTrack() {
		changes = new HashMap<>();
		deletes = new ArrayList<>();
		changes = null;
		trackingChanges = false;
	}

	@Override
	public void update(byte[] key, byte[] value) {
		if (trackingChanges) {
			changes.put(new ByteArrayWrapper(key), value);
		} else {
			trie.update(key, value);
		}
	}

	@Override
	public byte[] get(byte[] key) {
		if (trackingChanges) {
			ByteArrayWrapper wKey = new ByteArrayWrapper(key);
			if (deletes.contains(wKey))
				return null;
			if (changes.get(wKey) != null)
				return changes.get(wKey);
			return trie.get(key);
		}
		return trie.get(key);
	}

	@Override
	public void delete(byte[] key) {
		if (trackingChanges) {
			ByteArrayWrapper wKey = new ByteArrayWrapper(key);
			deletes.add(wKey);
		} else {
			trie.delete(key);
		}
	}

	@Override
	public byte[] getRootHash() {
		return trie.getRootHash();
	}

	@Override
	public String getTrieDump() {
		return trie.getTrieDump();
	}
}
