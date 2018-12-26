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
package org.ethereum.jsontestsuite.suite;

import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.ContractDetails;
import org.ethereum.vm.DataWord;
import org.spongycastle.util.encoders.Hex;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.crypto.HashUtil.EMPTY_DATA_HASH;
import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;

/**
 * Common functionality for ContractDetails implementations
 *
 * Created by Anton Nashatyrev on 24.03.2016.
 */
public abstract class AbstractContractDetails implements ContractDetails {

    private boolean dirty = false;
    private boolean deleted = false;

    private Map<ByteArrayWrapper, byte[]> codes = new HashMap<>();

    @Override
    public byte[] getCode() {
        return codes.size() == 0 ? EMPTY_BYTE_ARRAY : codes.values().iterator().next();
    }

    @Override
    public byte[] getCode(byte[] codeHash) {
        if (java.util.Arrays.equals(codeHash, EMPTY_DATA_HASH))
            return EMPTY_BYTE_ARRAY;
        byte[] code = codes.get(new ByteArrayWrapper(codeHash));
        return code == null ? EMPTY_BYTE_ARRAY : code;
    }

    @Override
    public void setCode(byte[] code) {
        if (code == null) return;
        codes.put(new ByteArrayWrapper(sha3(code)), code);
        setDirty(true);
    }

    protected Map<ByteArrayWrapper, byte[]> getCodes() {
        return codes;
    }

    protected void setCodes(Map<ByteArrayWrapper, byte[]> codes) {
        this.codes = new HashMap<>(codes);
    }

    protected void appendCodes(Map<ByteArrayWrapper, byte[]> codes) {
        this.codes.putAll(codes);
    }

    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }

    public abstract ContractDetails clone();

    @Override
    public void deleteStorage() {
        Set<DataWord> keys = getStorageKeys();
        for (DataWord key: keys) {
            put(key, DataWord.ZERO);
        }
    }

    @Override
    public String toString() {
        String ret = "  Code: " + (codes.size() < 2 ? Hex.toHexString(getCode()) : codes.size() + " versions" ) + "\n";
        ret += "  Storage: " + getStorage().toString();
        return ret;
    }
}
