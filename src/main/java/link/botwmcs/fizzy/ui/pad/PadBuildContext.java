package link.botwmcs.fizzy.ui.pad;

import link.botwmcs.fizzy.ui.split.SplitMetrics;
import link.botwmcs.fizzy.ui.split.SplitSpec;

import java.util.List;

public record PadBuildContext(
        int rows,
        int cols,
        List<SplitSpec> splits,
        SplitMetrics splitMetrics
) {
    public PadBuildContext {
        if (rows < 1) {
            throw new IllegalArgumentException("rows must be >= 1");
        }
        if (cols < 1) {
            throw new IllegalArgumentException("cols must be >= 1");
        }
        splits = List.copyOf(splits);
    }
}
