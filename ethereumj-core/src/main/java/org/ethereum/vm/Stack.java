package org.ethereum.vm;

import org.ethereum.vmtrace.ProgramTraceListener;

public class Stack extends java.util.Stack<DataWord> implements ProgramTraceListenerAware {

    private ProgramTraceListener traceListener;

    @Override
    public void setTraceListener(ProgramTraceListener listener) {
        this.traceListener = listener;
    }

    @Override
    public synchronized DataWord pop() {
        if (traceListener != null) traceListener.onStackPop();
        return super.pop();
    }

    @Override
    public DataWord push(DataWord item) {
        if (traceListener != null) traceListener.onStackPush(item);
        return super.push(item);
    }

    public void swap(int from, int to) {
        if (isAccessible(from) && isAccessible(to) && (from != to)) {
            if (traceListener != null) traceListener.onStackSwap(from, to);
            DataWord tmp = get(from);
            set(from, set(to, tmp));
        }
    }

    private boolean isAccessible(int from) {
        return from >= 0 && from < size();
    }
}
