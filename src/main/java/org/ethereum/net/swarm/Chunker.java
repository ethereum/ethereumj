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

import java.util.Collection;

/**
 * Chunker is the interface to a component that is responsible for disassembling and assembling larger data
 * and intended to be the dependency of a DPA storage system with fixed maximum chunksize.
 * It relies on the underlying chunking model.
 * When calling Split, the caller provides a channel (chan *Chunk) on which it receives chunks to store.
 * The DPA delegates to storage layers (implementing ChunkStore interface). NewChunkstore(DB) is a convenience
 * wrapper with which all DBs (conforming to DB interface) can serve as ChunkStores. See chunkStore.go
 *
 * After getting notified that all the data has been split (the error channel is closed), the caller can safely
 * read or save the root key. Optionally it times out if not all chunks get stored or not the entire stream
 * of data has been processed. By inspecting the errc channel the caller can check if any explicit errors
 * (typically IO read/write failures) occurred during splitting.
 *
 * When calling Join with a root key, the caller gets returned a lazy reader. The caller again provides a channel
 * and receives an error channel. The chunk channel is the one on which the caller receives placeholder chunks with
 * missing data. The DPA is supposed to forward this to the chunk stores and notify the chunker if the data
 * has been delivered (i.e. retrieved from memory cache, disk-persisted db or cloud based swarm delivery).
 * The chunker then puts these together and notifies the DPA if data has been assembled by a closed error channel.
 * Once the DPA finds the data has been joined, it is free to deliver it back to swarm in full (if the original
 * request was via the bzz protocol) or save and serve if it it was a local client request.
 */
public interface Chunker {

    /**
     * When splitting, data is given as a SectionReader, and the key is a hashSize long byte slice (Key),
     * the root hash of the entire content will fill this once processing finishes.
     * New chunks to store are coming to caller via the chunk storage channel, which the caller provides.
     * wg is a Waitgroup (can be nil) that can be used to block until the local storage finishes
     * The caller gets returned an error channel, if an error is encountered during splitting, it is fed to errC error channel.
     * A closed error signals process completion at which point the key can be considered final if there were no errors.
     */
    Key split(SectionReader sectionReader, Collection<Chunk> consumer);

    /**
     * Join reconstructs original content based on a root key.
     * When joining, the caller gets returned a Lazy SectionReader
     * New chunks to retrieve are coming to caller via the Chunk channel, which the caller provides.
     * If an error is encountered during joining, it appears as a reader error.
     * The SectionReader provides on-demand fetching of chunks.
     */
    SectionReader join(ChunkStore chunkStore, Key key);

    /**
     * @return the key length
     */
    long keySize();
}
