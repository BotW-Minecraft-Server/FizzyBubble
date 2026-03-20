package link.botwmcs.fizzy.ui.kernel.state;

import java.util.Objects;

public interface StateScheduler {
    void post(Runnable task);

    default boolean isOnSchedulerThread() {
        return true;
    }

    static StateScheduler immediate() {
        return Immediate.INSTANCE;
    }

    final class Immediate implements StateScheduler {
        private static final Immediate INSTANCE = new Immediate();

        private Immediate() {
        }

        @Override
        public void post(Runnable task) {
            Objects.requireNonNull(task, "task").run();
        }
    }
}
