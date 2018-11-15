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
package org.ethereum.sharding.processing.consensus;

import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.sharding.domain.Beacon;
import org.ethereum.sharding.processing.db.BeaconStore;
import org.ethereum.sharding.processing.state.ActiveState;
import org.ethereum.sharding.processing.state.AttestationRecord;
import org.ethereum.sharding.processing.state.BeaconState;
import org.ethereum.sharding.util.Bitfield;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Score uses IMD GHOST algorithm combined with finality
 * For more info check following pages:
 *  - https://ethresear.ch/t/immediate-message-driven-ghost-as-ffg-fork-choice-rule/2561
 *  - https://ethresear.ch/t/beacon-chain-casper-ffg-rpj-mini-spec/2760
 */
public class MaximumVotesAsScore implements ScoreFunction {

    BeaconStore store;

    public MaximumVotesAsScore(BeaconStore store) {
        this.store = store;
    }

    @Override
    public BigInteger apply(Beacon block, BeaconState state) {
        ActiveState activeState = state.getActiveState();

        // Assumes that data from block is already in active state
        List<AttestationRecord> pendingAttestations = activeState.getPendingAttestations();
        AttestationRecord first = block.getAttestations().get(0);
        boolean found = false;
        for (int i = pendingAttestations.size() - 1; i >= 0; --i) {
            if (pendingAttestations.get(i).equals(first)) {
                found = true;
            }
        }
        if (!found) {
            throw new RuntimeException("State should already include scored block");
        }

        long lastJustified = state.getCrystallizedState().getFinality().getLastJustifiedSlot();

        // Calculate per block votes
        Map<ByteArrayWrapper, List<AttestationRecord>> perBlockAttestations = new HashMap<>();
        pendingAttestations.forEach(at -> {
            ByteArrayWrapper blockHash = new ByteArrayWrapper(at.getShardBlockHash());
            if (perBlockAttestations.containsKey(blockHash)) {
                perBlockAttestations.get(blockHash).add(at);
            } else {
                perBlockAttestations.put(blockHash, new ArrayList<AttestationRecord>(){{add(at);}});
            }
        });

        int maxVotes = 0;
        Beacon current = block;
        while (current.getSlotNumber() != lastJustified) {
            int currentVotes = 0;
            List<AttestationRecord> blockAttestations = perBlockAttestations.get(new ByteArrayWrapper(current.getHash()));
            if (blockAttestations != null && !blockAttestations.isEmpty()) {
                currentVotes = Bitfield.orBitfield(
                        blockAttestations.stream()
                                .map(at -> at.getAttesterBitfield())
                                .collect(Collectors.toList())
                ).calcVotes();
            }
            if (currentVotes > maxVotes) {
                maxVotes = currentVotes;
            }
            current = store.getByHash(current.getParentHash());
        }

        return BigInteger.valueOf(maxVotes);
    }
}
