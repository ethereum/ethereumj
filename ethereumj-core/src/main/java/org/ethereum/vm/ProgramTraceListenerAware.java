package org.ethereum.vm;

import org.ethereum.vmtrace.ProgramTraceListener;

public interface ProgramTraceListenerAware {
    
    void setTraceListener(ProgramTraceListener listener);
}
