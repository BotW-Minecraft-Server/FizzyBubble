package link.botwmcs.fizzy.ui.kernel.notification;

public enum NotificationLevel {
    INFO(0xFF57D7FF, 0xFF112734, 0xFFE8F6FF),
    SUCCESS(0xFF45B54A, 0xFF112A19, 0xFFE9FFE8),
    WARNING(0xFFFFB443, 0xFF2E2414, 0xFFFFF4E3),
    ERROR(0xFFFF5F6D, 0xFF31151A, 0xFFFFE8EC);

    private final int accentColor;
    private final int backgroundColor;
    private final int titleColor;

    NotificationLevel(int accentColor, int backgroundColor, int titleColor) {
        this.accentColor = accentColor;
        this.backgroundColor = backgroundColor;
        this.titleColor = titleColor;
    }

    public int accentColor() {
        return accentColor;
    }

    public int backgroundColor() {
        return backgroundColor;
    }

    public int titleColor() {
        return titleColor;
    }
}
