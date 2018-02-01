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
package org.ethereum.erp;

import org.ethereum.core.Repository;
import org.ethereum.erp.StateChangeObject.StateChangeAction;

import java.util.Arrays;

import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static org.ethereum.util.ByteUtil.toHexString;

/**
 * The ERP Executor applies State Change Actions to a Repository.
 */
public class ErpExecutor {
    public static final String WEI_TRANSFER = "weiTransfer";
    public static final String STORE_CODE = "storeCode";

    public static class ErpExecutionException extends Exception {
        ErpExecutionException(Throwable cause) {
            super(cause);
        }
    }

    public void applyStateChanges(StateChangeObject sco, Repository repo) throws ErpExecutionException {
        try {
            for (StateChangeAction action : sco.actions) {
                applyStateChangeAction(action, repo);
            }
        }
        catch (IllegalArgumentException | UnsupportedOperationException e) {
            throw new ErpExecutionException(e);
        }
    }

    void applyStateChangeAction(StateChangeAction action, Repository repo) {
        switch (action.type) {
            case WEI_TRANSFER:
                applyWeiTransfer(action, repo);
                break;
            case STORE_CODE:
                applyStoreCode(action, repo);
                break;
            default:
                throw new UnsupportedOperationException("ERP action type not supported. " + action.type);
        }
    }

    void applyStoreCode(StateChangeAction action, Repository repo) {
        if (action.toAddress == EMPTY_BYTE_ARRAY)
            throw new IllegalArgumentException("storeCode cannot store code at an empty address");

        if (!Arrays.equals(action.expectedCodeHash, repo.getCodeHash(action.toAddress)))
            throw new IllegalStateException(String.format("ERP storeCode did not find the expected hash.  Expected %s, found %s", toHexString(action.expectedCodeHash), toHexString(repo.getCodeHash(action.toAddress))));

        repo.saveCode(action.toAddress, action.code);
    }

    void applyWeiTransfer(StateChangeAction action, Repository repo) {
        if (action.toAddress == EMPTY_BYTE_ARRAY)
            throw new IllegalArgumentException("weiTransfer cannot transfer to the an empty address");

        // is this needed here?  Seems like this check should happen at a lower level (i.e. in the repo)
        if (repo.getBalance(action.fromAddress).compareTo(action.valueInWei) < 0)
            throw new IllegalStateException(String.format("ERP insufficient balance for weiTransfer.  Sender balance of %s less than transfer amount of %s", repo.getBalance(action.fromAddress).toString(), action.valueInWei.toString()));

        repo.addBalance(action.fromAddress, action.valueInWei.negate());
        repo.addBalance(action.toAddress, action.valueInWei);
    }
}
