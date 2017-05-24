/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.net.swarm;

/**
 * Distributed Preimage Archive
 * Acts as a high-level API to the Swarm
 *
 * From Go implementation:
 * DPA provides the client API entrypoints Store and Retrieve to store and retrieve
 * It can store anything that has a byte slice representation, so files or serialised objects etc.
 * Storage: DPA calls the Chunker to segment the input datastream of any size to a merkle hashed tree of chunks.
 * The key of the root block is returned to the client.
 * Retrieval: given the key of the root block, the DPA retrieves the block chunks and reconstructs the original
 * data and passes it back as a lazy reader. A lazy reader is a reader with on-demand delayed processing,
 * i.e. the chunks needed to reconstruct a large file are only fetched and processed if that particular part
 * of the document is actually read.
 * As the chunker produces chunks, DPA dispatches them to the chunk store for storage or retrieval.
 *  The ChunkStore interface is implemented by :
 *  - memStore: a memory cache
 * - dbStore: local disk/db store
 * - localStore: a combination (sequence of) memStore and dbStore
 * - netStore: dht storage
 *
 * Created by Anton Nashatyrev on 18.06.2015.
 */
public class DPA {
    // this is now the default and the only possible Chunker implementation
    private Chunker chunker = new TreeChunker();
    private ChunkStore chunkStore;

    public DPA(ChunkStore chunkStore) {
        this.chunkStore = chunkStore;
    }

    /**
     * Main entry point for document storage directly. Used by the
     * FS-aware API and httpaccess
     *
     * @return key
     */
    public Key store(SectionReader reader) {
        return chunker.split(reader, new Util.ChunkConsumer(chunkStore));
    }

    /**
     * Main entry point for document retrieval directly. Used by the
     * FS-aware API and httpaccess
     * Chunk retrieval blocks on netStore requests with a timeout so reader will
     * report error if retrieval of chunks within requested range time out.
     *
     * @return key
     */
    public SectionReader retrieve(Key key) {
        return chunker.join(chunkStore, key);
    }
}
