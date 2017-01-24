package org.ethereum.datasource;

/**
 * Abstract Source implementation with underlying backing Source
 * The class has control whether the backing Source should be flushed
 * in 'cascade' manner
 *
 * Created by Anton Nashatyrev on 06.12.2016.
 */
public abstract class AbstractChainedSource<Key, Value, SourceKey, SourceValue> implements Source<Key, Value> {

    private Source<SourceKey, SourceValue> source;
    private boolean flushSource;

    /**
     * Intended for subclasses which wishes to initialize the source
     * later via {@link #setSource(Source)} method
     */
    protected AbstractChainedSource() {
    }

    public AbstractChainedSource(Source<SourceKey, SourceValue> source) {
        this.source = source;
    }

    /**
     * Intended for subclasses which wishes to initialize the source later
     */
    protected void setSource(Source<SourceKey, SourceValue> src) {
        source = src;
    }

    public Source<SourceKey, SourceValue> getSource() {
        return source;
    }

    public void setFlushSource(boolean flushSource) {
        this.flushSource = flushSource;
    }

    /**
     * Invokes {@link #flushImpl()} and does backing Source flush if required
     * @return true if this or source flush did any changes
     */
    @Override
    public final synchronized boolean flush() {
        boolean ret = flushImpl();
        if (flushSource) {
            ret |= getSource().flush();
        }
        return ret;
    }

    /**
     * Should be overridden to do actual source flush
     */
    protected abstract boolean flushImpl();
}
