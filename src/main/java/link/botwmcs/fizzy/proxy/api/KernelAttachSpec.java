package link.botwmcs.fizzy.proxy.api;

import javax.annotation.Nullable;
import java.util.Objects;

public final class KernelAttachSpec {
    private static final KernelAttachSpec EMPTY = new KernelAttachSpec(
            KernelUiSpec.empty(),
            null,
            null,
            null
    );
    private static final KernelAttachSpec DEFAULTS = new KernelAttachSpec(
            KernelUiSpec.empty(),
            DefaultPhaseBridgePolicy.INSTANCE,
            TooltipPolicy.AUTO_SUPPRESS_SOURCE_WHEN_BLOCKING,
            InputDispatchPolicy.defaults()
    );

    private final KernelUiSpec uiSpec;
    private final @Nullable PhaseBridgePolicy phasePolicy;
    private final @Nullable TooltipPolicy tooltipPolicy;
    private final @Nullable InputDispatchPolicy inputPolicy;

    public KernelAttachSpec(
            KernelUiSpec uiSpec,
            @Nullable PhaseBridgePolicy phasePolicy,
            @Nullable TooltipPolicy tooltipPolicy,
            @Nullable InputDispatchPolicy inputPolicy
    ) {
        this.uiSpec = Objects.requireNonNull(uiSpec, "uiSpec");
        this.phasePolicy = phasePolicy;
        this.tooltipPolicy = tooltipPolicy;
        this.inputPolicy = inputPolicy;
    }

    public static KernelAttachSpec empty() {
        return EMPTY;
    }

    public static KernelAttachSpec defaults() {
        return DEFAULTS;
    }

    public KernelUiSpec uiSpec() {
        return uiSpec;
    }

    public @Nullable PhaseBridgePolicy phasePolicy() {
        return phasePolicy;
    }

    public @Nullable TooltipPolicy tooltipPolicy() {
        return tooltipPolicy;
    }

    public @Nullable InputDispatchPolicy inputPolicy() {
        return inputPolicy;
    }

    public boolean isEmpty() {
        return uiSpec.isEmpty() && phasePolicy == null && tooltipPolicy == null && inputPolicy == null;
    }
}

