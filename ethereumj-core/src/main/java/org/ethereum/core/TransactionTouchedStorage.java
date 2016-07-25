package org.ethereum.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.keyvalue.AbstractKeyValue;
import org.ethereum.util.Functional;
import org.ethereum.vm.DataWord;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TransactionTouchedStorage {

    public static class Entry extends AbstractKeyValue<DataWord,DataWord> {

        private boolean changed;

        public Entry(DataWord key, DataWord value, boolean changed) {
            super(key, value);
            this.changed = changed;
        }

        public Entry() {
            super(null, null);
        }

        @Override
        protected DataWord setKey(DataWord key) {
            return super.setKey(key);
        }

        @Override
        protected DataWord setValue(DataWord value) {
            return super.setValue(value);
        }

        public boolean isChanged() {
            return changed;
        }

        public void setChanged(boolean changed) {
            this.changed = changed;
        }
    }

    private Map<DataWord, Entry> entries = new HashMap<>();

    public TransactionTouchedStorage() {
    }

    @JsonCreator
    public TransactionTouchedStorage(Collection<Entry> entries) {
        for (Entry entry : entries) {
            add(entry);
        }
    }

    @JsonValue
    public Collection<Entry> getEntries() {
        return entries.values();
    }

    public Entry add(Entry entry) {
        return entries.put(entry.getKey(), entry);
    }

    private Entry add(Map.Entry<DataWord, DataWord> entry, boolean changed) {
        return add(new Entry(entry.getKey(), entry.getValue(), changed));
    }

    void addReading(Map<DataWord, DataWord> entries) {
        if (MapUtils.isEmpty(entries)) return;

        for (Map.Entry<DataWord, DataWord> entry : entries.entrySet()) {
            if (!this.entries.containsKey(entry.getKey())) add(entry, false);
        }
    }

    void addWriting(Map<DataWord, DataWord> entries) {
        if (MapUtils.isEmpty(entries)) return;

        for (Map.Entry<DataWord, DataWord> entry : entries.entrySet()) {
            add(entry, true);
        }
    }

    private Map<DataWord, DataWord> keyValues(Functional.Function<Entry, Boolean> filter) {
        Map<DataWord, DataWord> result = new HashMap<>();
        for (Entry entry : getEntries()) {
            if (filter == null || filter.apply(entry)) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;

    }

    public Map<DataWord, DataWord> getChanged() {
        return keyValues(new Functional.Function<Entry, Boolean>() {
            @Override
            public Boolean apply(Entry entry) {
                return entry.isChanged();
            }
        });
    }

    public Map<DataWord, DataWord> getReadOnly() {
        return keyValues(new Functional.Function<Entry, Boolean>() {
            @Override
            public Boolean apply(Entry entry) {
                return !entry.isChanged();
            }
        });
    }

    public Map<DataWord, DataWord> getAll() {
        return keyValues(null);
    }

    public int size() {
        return entries.size();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }
}
