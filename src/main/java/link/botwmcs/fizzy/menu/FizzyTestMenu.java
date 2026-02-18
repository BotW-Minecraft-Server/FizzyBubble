package link.botwmcs.fizzy.menu;

import link.botwmcs.fizzy.network.s2c.FizzyMenuSyncS2CPayload;
import link.botwmcs.fizzy.ui.frame.FizzyFrameMetrics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FizzyTestMenu extends AbstractContainerMenu {
    public static final int CONTAINER_ROWS = 6;
    public static final int CONTAINER_COLS = 9;
    public static final int CONTAINER_SIZE = CONTAINER_ROWS * CONTAINER_COLS;
    public static final int DATA_PROGRESS = 0;
    public static final int DATA_MAX_PROGRESS = 1;
    public static final int DATA_COUNT = 2;
    private static final int CONTAINER_MENU_HEIGHT = FizzyFrameMetrics.ofDefault256x256()
            .totalHeightForRows(CONTAINER_ROWS, false, true);

    private final Container container;
    private final ContainerData data;
    private final Player owner;

    private int serverTick;
    private int clientPacketProgress;
    private String clientStatusText = "Waiting for server...";

    public FizzyTestMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(CONTAINER_SIZE), new SimpleContainerData(DATA_COUNT));
    }

    public FizzyTestMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(FizzyMenus.FIZZY_TEST_MENU.get(), containerId);
        checkContainerSize(container, CONTAINER_SIZE);
        checkContainerDataCount(data, DATA_COUNT);
        this.container = container;
        this.data = data;
        this.owner = playerInventory.player;
        this.container.startOpen(playerInventory.player);
        this.addDataSlots(data);

        addContainerSlots(container);
        addPlayerInventorySlots(playerInventory);
        addPlayerHotbarSlots(playerInventory);

        if (!playerInventory.player.level().isClientSide) {
            this.data.set(DATA_PROGRESS, 0);
            this.data.set(DATA_MAX_PROGRESS, 100);
        }
    }

    private void addContainerSlots(Container container) {
        for (int row = 0; row < CONTAINER_ROWS; row++) {
            for (int col = 0; col < CONTAINER_COLS; col++) {
                int slotIndex = col + row * CONTAINER_COLS;
                int x = FizzyMenuLayout.containerSlotX(col);
                int y = FizzyMenuLayout.containerSlotY(row);
                this.addSlot(new Slot(container, slotIndex, x, y));
            }
        }
    }

    private void addPlayerInventorySlots(Inventory playerInventory) {
        int menuHeight = containerMenuHeight();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int playerSlot = col + row * 9 + 9;
                int x = FizzyMenuLayout.playerInvSlotX(col);
                int y = FizzyMenuLayout.playerInvSlotY(menuHeight, row);
                this.addSlot(new Slot(playerInventory, playerSlot, x, y));
            }
        }
    }

    private void addPlayerHotbarSlots(Inventory playerInventory) {
        int menuHeight = containerMenuHeight();
        int hotbarY = FizzyMenuLayout.hotbarSlotY(menuHeight);
        for (int col = 0; col < 9; col++) {
            int x = FizzyMenuLayout.playerInvSlotX(col);
            this.addSlot(new Slot(playerInventory, col, x, hotbarY));
        }
    }

    private static int containerMenuHeight() {
        return CONTAINER_MENU_HEIGHT;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack empty = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot == null || !slot.hasItem()) {
            return empty;
        }

        ItemStack stackInSlot = slot.getItem();
        ItemStack copy = stackInSlot.copy();
        int playerInvStart = CONTAINER_SIZE;
        int playerInvEnd = this.slots.size();
        if (slotIndex < CONTAINER_SIZE) {
            if (!this.moveItemStackTo(stackInSlot, playerInvStart, playerInvEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stackInSlot, 0, CONTAINER_SIZE, false)) {
            return ItemStack.EMPTY;
        }

        if (stackInSlot.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        slot.onTake(player, stackInSlot);
        return copy;
    }

    @Override
    public void broadcastChanges() {
        if (this.owner instanceof ServerPlayer serverPlayer) {
            this.serverTick++;
            int progress = this.serverTick % 101;
            this.data.set(DATA_PROGRESS, progress);
            this.data.set(DATA_MAX_PROGRESS, 100);
            if (this.serverTick % 20 == 0) {
                sendSyncPacket(serverPlayer, progress, "Server tick: " + this.serverTick);
            }
        }
        super.broadcastChanges();
    }

    public void handleClientPing(ServerPlayer player) {
        if (player.containerMenu != this) {
            return;
        }
        int progress = this.data.get(DATA_PROGRESS);
        sendSyncPacket(player, progress, "Ping ack at tick " + this.serverTick);
    }

    private void sendSyncPacket(ServerPlayer player, int progress, String text) {
        player.connection.send(new ClientboundCustomPayloadPacket(
                new FizzyMenuSyncS2CPayload(this.containerId, progress, text)
        ));
    }

    public void applyClientSync(int progress, String text) {
        this.clientPacketProgress = progress;
        this.clientStatusText = text;
    }

    public int progress() {
        return Math.max(this.data.get(DATA_PROGRESS), this.clientPacketProgress);
    }

    public int maxProgress() {
        int max = this.data.get(DATA_MAX_PROGRESS);
        return Math.max(1, max);
    }

    public Component serverStatusText() {
        return Component.literal(this.clientStatusText);
    }
}
