package link.botwmcs.fizzy.ui.kernel.runtime;

import link.botwmcs.fizzy.ui.kernel.state.Scope;
import link.botwmcs.fizzy.ui.kernel.state.StateKernel;

import java.util.Objects;

public final class UiRuntime implements AutoCloseable {
    private final UiMainThreadScheduler scheduler;
    private final StateKernel stateKernel;
    private final Scope rootScope;

    private boolean closed;

    public static UiRuntime createForCurrentThread() {
        return new UiRuntime(new UiMainThreadScheduler(Thread.currentThread()));
    }

    public UiRuntime(UiMainThreadScheduler scheduler) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.stateKernel = new StateKernel(scheduler);
        this.rootScope = stateKernel.newScope();
    }

    public UiMainThreadScheduler scheduler() {
        return scheduler;
    }

    public StateKernel state() {
        return stateKernel;
    }

    public Scope rootScope() {
        return rootScope;
    }

    public boolean isClosed() {
        return closed;
    }

    public void frameTick() {
        if (closed) {
            return;
        }
        scheduler.drain();
        stateKernel.flush();
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        rootScope.close();
        scheduler.drain();
        stateKernel.flush();
    }
}
