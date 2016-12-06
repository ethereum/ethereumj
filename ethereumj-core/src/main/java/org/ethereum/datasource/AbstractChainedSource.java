package org.ethereum.datasource;

/**
 * Created by Anton Nashatyrev on 06.12.2016.
 */
public abstract class AbstractChainedSource<Key, Value, SourceKey, SourceValue> implements Source<Key, Value> {

    protected Source<SourceKey, SourceValue> source;
    protected boolean flushSource;

    protected AbstractChainedSource() {
    }

    public AbstractChainedSource(Source<SourceKey, SourceValue> source) {
        this.source = source;
    }

    protected void setSource(Source<SourceKey, SourceValue> src) {
        source = src;
    }

    public Source<SourceKey, SourceValue> getSource() {
        return source;
    }

    public void setFlushSource(boolean flushSource) {
        this.flushSource = flushSource;
    }

    @Override
    public final synchronized boolean flush() {
        if (flushSource) {
            getSource().flush();
        }
        return flushImpl();
    }

    protected abstract boolean flushImpl();
}
