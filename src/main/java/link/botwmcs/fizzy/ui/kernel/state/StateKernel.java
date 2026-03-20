package link.botwmcs.fizzy.ui.kernel.state;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

public final class StateKernel {
    private final StateScheduler scheduler;
    private final Deque<Computation> trackingStack = new ArrayDeque<>();
    private final LinkedHashSet<ReactiveEffect> pendingEffects = new LinkedHashSet<>();

    private int batchDepth;
    private boolean flushScheduled;
    private boolean flushing;

    public StateKernel() {
        this(StateScheduler.immediate());
    }

    public StateKernel(StateScheduler scheduler) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
    }

    public Scope newScope() {
        return new Scope();
    }

    public <T> MutableSignal<T> mutableSignal(T initialValue) {
        return new MutableSignalImpl<>(initialValue);
    }

    public <T> Signal<T> computedSignal(Supplier<? extends T> supplier) {
        return new ComputedSignalImpl<>(supplier);
    }

    public <T> Signal<T> computedSignal(Scope scope, Supplier<? extends T> supplier) {
        ComputedSignalImpl<T> computed = new ComputedSignalImpl<>(supplier);
        scope.hold(computed);
        return computed;
    }

    public EffectHandle effect(Runnable body) {
        ReactiveEffect effect = new ReactiveEffect(body);
        queueEffect(effect);
        return effect;
    }

    public EffectHandle effect(Scope scope, Runnable body) {
        return scope.hold(effect(body));
    }

    public void batch(Runnable action) {
        Objects.requireNonNull(action, "action");
        batchDepth++;
        try {
            action.run();
        } finally {
            batchDepth--;
            if (batchDepth == 0) {
                requestFlush();
            }
        }
    }

    public void flush() {
        if (!scheduler.isOnSchedulerThread()) {
            requestFlush();
            return;
        }
        flushScheduled = false;
        if (flushing) {
            return;
        }
        flushing = true;
        try {
            while (!pendingEffects.isEmpty()) {
                ReactiveEffect next = pendingEffects.iterator().next();
                pendingEffects.remove(next);
                next.runIfNeeded();
            }
        } finally {
            flushing = false;
        }
    }

    private void requestFlush() {
        if (batchDepth > 0) {
            return;
        }
        if (scheduler.isOnSchedulerThread()) {
            flush();
            return;
        }
        if (flushScheduled) {
            return;
        }
        flushScheduled = true;
        scheduler.post(this::flush);
    }

    private void queueEffect(ReactiveEffect effect) {
        if (effect.isDisposed()) {
            return;
        }
        if (!pendingEffects.add(effect)) {
            return;
        }
        requestFlush();
    }

    private void trackRead(Source source) {
        Computation computation = trackingStack.peek();
        if (computation != null) {
            computation.track(source);
        }
    }

    private abstract class Source {
        private final LinkedHashSet<Computation> subscribers = new LinkedHashSet<>();

        void notifySubscribers() {
            if (subscribers.isEmpty()) {
                return;
            }
            List<Computation> snapshot = List.copyOf(subscribers);
            for (Computation subscriber : snapshot) {
                subscriber.markDirty();
            }
        }

        void clearSubscribers() {
            subscribers.clear();
        }
    }

    private abstract class Computation implements AutoCloseable {
        private final Set<Source> dependencies = new LinkedHashSet<>();
        private boolean disposed;

        final void track(Source source) {
            if (disposed || !dependencies.add(source)) {
                return;
            }
            source.subscribers.add(this);
        }

        final void markDirty() {
            if (disposed) {
                return;
            }
            onDirty();
        }

        protected abstract void onDirty();

        protected final <T> T evaluateTracked(Supplier<T> supplier) {
            clearDependencies();
            trackingStack.push(this);
            try {
                return supplier.get();
            } finally {
                if (!trackingStack.isEmpty() && trackingStack.peek() == this) {
                    trackingStack.pop();
                } else {
                    trackingStack.remove(this);
                }
            }
        }

        protected final void clearDependencies() {
            if (dependencies.isEmpty()) {
                return;
            }
            List<Source> snapshot = new ArrayList<>(dependencies);
            for (Source dependency : snapshot) {
                dependency.subscribers.remove(this);
            }
            dependencies.clear();
        }

        @Override
        public void close() {
            if (disposed) {
                return;
            }
            disposed = true;
            clearDependencies();
        }

        protected final boolean isDisposedInternal() {
            return disposed;
        }
    }

    private final class MutableSignalImpl<T> extends Source implements MutableSignal<T> {
        private T value;

        private MutableSignalImpl(T value) {
            this.value = value;
        }

        @Override
        public T get() {
            trackRead(this);
            return value;
        }

        @Override
        public void set(T value) {
            if (Objects.equals(this.value, value)) {
                return;
            }
            this.value = value;
            notifySubscribers();
            requestFlush();
        }
    }

    private final class ComputedSignalImpl<T> extends Source implements Signal<T>, AutoCloseable {
        private final Supplier<? extends T> supplier;
        private final ComputedTracker tracker = new ComputedTracker();

        private T value;
        private boolean dirty = true;

        private ComputedSignalImpl(Supplier<? extends T> supplier) {
            this.supplier = Objects.requireNonNull(supplier, "supplier");
        }

        @Override
        public T get() {
            trackRead(this);
            if (dirty) {
                value = tracker.evaluateTracked(supplier::get);
                dirty = false;
            }
            return value;
        }

        @Override
        public void close() {
            tracker.close();
            clearSubscribers();
        }

        private final class ComputedTracker extends Computation {
            @Override
            protected void onDirty() {
                if (dirty) {
                    return;
                }
                dirty = true;
                notifySubscribers();
                requestFlush();
            }
        }
    }

    private final class ReactiveEffect extends Computation implements EffectHandle {
        private final Runnable body;
        private boolean queued = true;

        private ReactiveEffect(Runnable body) {
            this.body = Objects.requireNonNull(body, "body");
        }

        private void runIfNeeded() {
            if (isDisposedInternal() || !queued) {
                return;
            }
            queued = false;
            evaluateTracked(() -> {
                body.run();
                return null;
            });
        }

        @Override
        protected void onDirty() {
            if (queued || isDisposedInternal()) {
                return;
            }
            queued = true;
            queueEffect(this);
        }

        @Override
        public boolean isDisposed() {
            return isDisposedInternal();
        }

        @Override
        public void close() {
            if (isDisposedInternal()) {
                return;
            }
            pendingEffects.remove(this);
            super.close();
        }
    }
}
