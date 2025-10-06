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
                5,
                28,
                3,
                18,
                7,
                26,
                18,
                3
        );
    }
}
