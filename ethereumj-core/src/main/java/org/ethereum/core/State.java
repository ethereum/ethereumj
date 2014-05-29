package org.ethereum.core;

import java.util.Map;

import org.ethereum.trie.Trie;

public class State {

	Trie trie;
	Map<String, State> states;
	
}
