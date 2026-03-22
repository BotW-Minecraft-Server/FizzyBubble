package link.botwmcs.fizzy.ui.kernel.render;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class UiRenderTaskQueue {
    private static final Comparator<RenderTask> TASK_ORDER = Comparator
            .comparing(RenderTask::layer)
            .thenComparingInt(RenderTask::serial);

    private final List<RenderTask> tasks = new ArrayList<>();
    private int nextSerial;

    public void add(UiRenderLayer layer, Runnable task) {
        tasks.add(new RenderTask(
                Objects.requireNonNull(layer, "layer"),
                Objects.requireNonNull(task, "task"),
                nextSerial++
        ));
    }

    public void renderAll() {
        renderMatching(phase -> true);
    }

    public void renderMatching(Predicate<UiRenderPhase> phasePredicate) {
        Objects.requireNonNull(phasePredicate, "phasePredicate");
        tasks.sort(TASK_ORDER);
        for (RenderTask task : tasks) {
            if (!phasePredicate.test(task.layer().phase())) {
                continue;
            }
            task.action().run();
        }
    }

    public void clear() {
        tasks.clear();
        nextSerial = 0;
    }

    private record RenderTask(UiRenderLayer layer, Runnable action, int serial) {
    }
}
