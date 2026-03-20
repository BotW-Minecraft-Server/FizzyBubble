package link.botwmcs.fizzy.ui.kernel.runtime;

import link.botwmcs.fizzy.ui.kernel.state.StateScheduler;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class UiMainThreadScheduler implements StateScheduler {
    private final Thread ownerThread;
    private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();

    public UiMainThreadScheduler(Thread ownerThread) {
        this.ownerThread = Objects.requireNonNull(ownerThread, "ownerThread");
    }

    @Override
    public void post(Runnable task) {
        queue.add(Objects.requireNonNull(task, "task"));
    }

    @Override
    public boolean isOnSchedulerThread() {
        return Thread.currentThread() == ownerThread;
    }

    public void drain() {
        Runnable task;
        while ((task = queue.poll()) != null) {
            task.run();
        }
    }
}
