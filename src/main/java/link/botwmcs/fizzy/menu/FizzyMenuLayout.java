package link.botwmcs.fizzy.menu;

public final class FizzyMenuLayout {
    public static final int SLOT_GRID_LEFT = 7;
    public static final int SLOT_GRID_TOP = 28;
    public static final int SLOT_INNER_OFFSET = 1;
    public static final int PLAYER_INV_GRID_TOP_FROM_MENU_BOTTOM = 1;
    public static final int PLAYER_INV_HOTBAR_GAP = 4;

    private FizzyMenuLayout() {
    }

    public static int containerSlotX(int col) {
        return SLOT_GRID_LEFT + SLOT_INNER_OFFSET + col * 18;
    }

    public static int containerSlotY(int row) {
        return SLOT_GRID_TOP + SLOT_INNER_OFFSET + row * 18;
    }

    public static int playerInvSlotX(int col) {
        return SLOT_GRID_LEFT + SLOT_INNER_OFFSET + col * 18;
    }

    public static int playerInvSlotY(int menuHeight, int row) {
        int top = menuHeight + PLAYER_INV_GRID_TOP_FROM_MENU_BOTTOM;
        return top + row * 18;
    }

    public static int hotbarSlotY(int menuHeight) {
        int playerInvTop = menuHeight + PLAYER_INV_GRID_TOP_FROM_MENU_BOTTOM;
        int hotbarTop = playerInvTop + 3 * 18 + PLAYER_INV_HOTBAR_GAP;
        return hotbarTop;
    }
}
