package link.botwmcs.fizzy.proxy.rule;

import link.botwmcs.fizzy.proxy.api.DefaultPhaseBridgePolicy;
import link.botwmcs.fizzy.proxy.api.InputDispatchPolicy;
import link.botwmcs.fizzy.proxy.api.KernelAttachSpec;
import link.botwmcs.fizzy.proxy.api.KernelUiSpec;
import link.botwmcs.fizzy.proxy.api.PhaseBridgePolicy;
import link.botwmcs.fizzy.proxy.api.TooltipPolicy;

import java.util.List;

public final class KernelSpecMerger {
    private KernelSpecMerger() {
    }

    public static KernelAttachSpec merge(List<KernelAttachSpec> patches) {
        if (patches == null || patches.isEmpty()) {
            return KernelAttachSpec.defaults();
        }

        KernelUiSpec mergedUi = KernelUiSpec.empty();
        PhaseBridgePolicy phasePolicy = null;
        TooltipPolicy tooltipPolicy = null;
        InputDispatchPolicy inputPolicy = null;

        for (KernelAttachSpec patch : patches) {
            if (patch == null || patch.isEmpty()) {
                continue;
            }
            mergedUi = mergeUi(mergedUi, patch.uiSpec());
            if (patch.phasePolicy() != null) {
                phasePolicy = patch.phasePolicy();
            }
            if (patch.tooltipPolicy() != null) {
                tooltipPolicy = patch.tooltipPolicy();
            }
            if (patch.inputPolicy() != null) {
                inputPolicy = patch.inputPolicy();
            }
        }

        if (phasePolicy == null) {
            phasePolicy = DefaultPhaseBridgePolicy.INSTANCE;
        }
        if (tooltipPolicy == null) {
            tooltipPolicy = TooltipPolicy.AUTO_SUPPRESS_SOURCE_WHEN_BLOCKING;
        }
        if (inputPolicy == null) {
            inputPolicy = InputDispatchPolicy.defaults();
        }

        return new KernelAttachSpec(mergedUi, phasePolicy, tooltipPolicy, inputPolicy);
    }

    private static KernelUiSpec mergeUi(KernelUiSpec base, KernelUiSpec overlay) {
        if (overlay == null || overlay.isEmpty()) {
            return base;
        }

        KernelUiSpec.Builder builder = KernelUiSpec.builder()
                .baseKernel(lastNonNull(base.baseKernel(), overlay.baseKernel()))
                .frame(lastNonNull(base.frame(), overlay.frame()))
                .background(lastNonNull(base.background(), overlay.background()))
                .behind(lastNonNull(base.behind(), overlay.behind()))
                .splitPainter(lastNonNull(base.splitPainter(), overlay.splitPainter()))
                .below(lastNonNull(base.below(), overlay.below()))
                .overrideSizePx(
                        lastNonNull(base.overrideWidthPx(), overlay.overrideWidthPx()),
                        lastNonNull(base.overrideHeightPx(), overlay.overrideHeightPx())
                )
                .addPads(base.pads())
                .addPads(overlay.pads())
                .addSplits(base.splits())
                .addSplits(overlay.splits());

        return builder.build();
    }

    private static <T> T lastNonNull(T left, T right) {
        return right != null ? right : left;
    }
}
