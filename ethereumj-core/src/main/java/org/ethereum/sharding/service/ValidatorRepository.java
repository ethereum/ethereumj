package org.ethereum.sharding.service;

import org.ethereum.sharding.domain.Validator;

import java.util.List;

/**
 * Helper interface to look for deposited validators in the receipts.
 *
 * @author Mikhail Kalinin
 * @since 30.07.2018
 */
public interface ValidatorRepository {

    /**
     * Returns a list of validators deployed in an inclusive range {@code [fromBlock, toBlock]}.
     * An order of deposits is preserved, hence first deposited validator has the lowest index in returned list.
     */
    List<Validator> query(byte[] fromBlock, byte[] toBlock);
}
