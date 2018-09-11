package org.ethereum.vm.hook;

import org.ethereum.vm.OpCode;
import org.ethereum.vm.program.Program;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

@Primary
@Component
public class RootVmHook implements VMHook {

    private static final Logger logger = LoggerFactory.getLogger("VM");

    private final List<VMHook> hooks;

    @Autowired
    public RootVmHook(List<VMHook> hooks) {
        this.hooks = hooks;
    }

    private void safeProxyToAll(Consumer<VMHook> action) {
        this.hooks.forEach(hook -> {
            try {
                action.accept(hook);
            } catch (Throwable t) {
                logger.error("VM hook execution error: ", t);
            }
        });
    }

    @Override
    public void startPlay(Program program) {
        safeProxyToAll(hook -> hook.startPlay(program));
    }

    @Override
    public void step(Program program, OpCode opcode) {
        safeProxyToAll(hook -> hook.startPlay(program));
    }

    @Override
    public void stopPlay(Program program) {
        safeProxyToAll(hook -> hook.startPlay(program));
    }
}
