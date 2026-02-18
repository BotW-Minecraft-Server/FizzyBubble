package link.botwmcs.fizzy.menu;

import link.botwmcs.fizzy.Fizzy;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class FizzyMenus {
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Fizzy.MODID);

    public static final Supplier<MenuType<FizzyTestMenu>> FIZZY_TEST_MENU = MENUS.register(
            "fizzy_test_menu",
            () -> IMenuTypeExtension.create((containerId, inventory, extraData) -> new FizzyTestMenu(containerId, inventory))
    );

    private FizzyMenus() {
    }

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
