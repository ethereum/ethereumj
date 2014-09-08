package org.ethereum.facade;

import org.ethereum.core.AccountState;
import org.ethereum.db.ContractDetails;

import java.math.BigInteger;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 08/09/2014 10:25
 */

public interface Repository {

    public AccountState getAccountState(byte[] addr);
    public ContractDetails getContractDetails(byte[] addr);

}
