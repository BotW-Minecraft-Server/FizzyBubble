package link.botwmcs.fizzy.ui.split;

public record FizzySplitMetrics(
        int texW,                    //
        int texH,                    //
        int horizontalSplitorStartX, //
        int horizontalSplitorStartY, //
        int horizontalSplitorWidth,  //
        int horizontalSplitorHeight, //
        int verticalSplitorStartX,   //
        int verticalSplitorStartY,   //
        int verticalSplitorWidth,    //
        int verticalSplitorHeight   //
) implements SplitMetrics {
    public static FizzySplitMetrics ofDefault() {
        return new FizzySplitMetrics(
                256,
                256,
                6,
                29,
                3,
                18,
                8,
                27,
                18,
                3
        );
    }
}
