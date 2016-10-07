package org.ethereum.db;

import org.ethereum.core.AccountState;
import org.ethereum.datasource.KeyValueDataSource;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.HashMap;
import java.util.Map;

import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.crypto.HashUtil.EMPTY_DATA_HASH;
import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;

/**
 * Common functionality for ContractDetails implementations
 *
 * Created by Anton Nashatyrev on 24.03.2016.
 */
public abstract class AbstractContractDetails implements ContractDetails {

    @Autowired
    @Qualifier("stateDS")
    KeyValueDataSource dataSource;

    AccountState accountState;

    private boolean dirty = false;
    private boolean deleted = false;

//    private Map<ByteArrayWrapper, byte[]> codes = new HashMap<>();

    @Override
    public byte[] getCode() {
//        return codes.size() == 0 ? EMPTY_BYTE_ARRAY : codes.values().iterator().next();
        return getCode(accountState.getCodeHash());
    }

    @Override
    public byte[] getCode(byte[] codeHash) {
//        if (java.util.Arrays.equals(codeHash, EMPTY_DATA_HASH))
//            return EMPTY_BYTE_ARRAY;
//        byte[] code = codes.get(new ByteArrayWrapper(codeHash));

        byte[] code = dataSource.get(codeHash);
        return code == null ? EMPTY_BYTE_ARRAY : code;
    }

    @Override
    public void setCode(byte[] code) {
        if (code == null) return;
        dataSource.put(sha3(code), code);
        setDirty(true);
    }

//    protected Map<ByteArrayWrapper, byte[]> getCodes() {
//        return codes;
//    }

//    protected void setCodes(Map<ByteArrayWrapper, byte[]> codes) {
//        this.codes = new HashMap<>(codes);
//    }

//    protected void appendCodes(Map<ByteArrayWrapper, byte[]> codes) {
//        this.codes.putAll(codes);
//    }

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
    public String toString() {
        String ret = ""; //"  Code: " + (codes.size() < 2 ? Hex.toHexString(getCode()) : codes.size() + " versions" ) + "\n";
        ret += "  Storage: " + getStorage().toString();
        return ret;
    }
}
