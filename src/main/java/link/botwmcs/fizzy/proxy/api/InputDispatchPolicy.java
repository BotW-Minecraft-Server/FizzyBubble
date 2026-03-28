package link.botwmcs.fizzy.proxy.api;

public record InputDispatchPolicy(
        boolean overlayFirst,
        boolean cancelSourceWhenHandled,
        boolean blockSourceWhenHitBlockingElement
) {
    public static InputDispatchPolicy defaults() {
        return new InputDispatchPolicy(true, true, true);
    }
}

