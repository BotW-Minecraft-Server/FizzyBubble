package link.botwmcs.fizzy.ui.split;

public interface SplitMetrics {
    int texW();
    int texH();
    int horizontalSplitorStartX();
    int horizontalSplitorStartY();
    int horizontalSplitorWidth();
    int horizontalSplitorHeight();
    int verticalSplitorStartX();
    int verticalSplitorStartY();
    int verticalSplitorWidth();
    int verticalSplitorHeight();

    /** @return 纹理中对应竖向分割线的起始 U 坐标 */
    default int splitorStartU(SplitType type) {
        return switch (type) {
            case VERTICAL -> horizontalSplitorStartX();
            case HORIZONTAL -> verticalSplitorStartX();
        };
    }

    /** @return 纹理中对应分割线的起始 V 坐标 */
    default int splitorStartV(SplitType type) {
        return switch (type) {
            case VERTICAL -> horizontalSplitorStartY();
            case HORIZONTAL -> verticalSplitorStartY();
        };
    }

    /**
     * @return 绘制当前分割线时使用的贴图宽度
     * <p>对于竖线而言是贴图的像素宽度；对于横线而言是贴图的像素长度。</p>
     */
    default int splitorWidth(SplitType type) {
        return switch (type) {
            case VERTICAL -> horizontalSplitorWidth();
            case HORIZONTAL -> verticalSplitorWidth();
        };
    }

    /**
     * @return 绘制当前分割线时使用的贴图高度
     * <p>对于竖线而言是贴图的像素高度；对于横线而言是贴图的像素厚度。</p>
     */
    default int splitorHeight(SplitType type) {
        return switch (type) {
            case VERTICAL -> horizontalSplitorHeight();
            case HORIZONTAL -> verticalSplitorHeight();
        };
    }

    /**
     * @return 当前分割线在绘制方向上的步进长度（像素）
     */
    default int axisStepLength(SplitType type) {
        return switch (type) {
            case VERTICAL -> horizontalSplitorHeight();
            case HORIZONTAL -> verticalSplitorWidth();
        };
    }

    /**
     * @return 当前分割线在垂直于绘制方向上的像素厚度
     */
    default int perpendicularThickness(SplitType type) {
        return switch (type) {
            case VERTICAL -> horizontalSplitorWidth();
            case HORIZONTAL -> verticalSplitorHeight();
        };
    }

    /**
     * @return 以分割线中心为锚点时需要向左/向上偏移的像素数
     */
    default int anchorOffset(SplitType type) {
        return perpendicularThickness(type) / 2;
    }
}
