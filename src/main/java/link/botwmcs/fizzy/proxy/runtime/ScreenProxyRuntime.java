package link.botwmcs.fizzy.proxy.runtime;

import link.botwmcs.fizzy.ui.host.FizzyMenuScreenHost;
import link.botwmcs.fizzy.ui.host.FizzyScreenHost;
import link.botwmcs.fizzy.proxy.api.HostRenderStage;
import link.botwmcs.fizzy.proxy.api.InputDispatchPolicy;
import link.botwmcs.fizzy.proxy.api.KernelAttachSpec;
import link.botwmcs.fizzy.proxy.host.ContainerScreenHostAdapter;
import link.botwmcs.fizzy.proxy.host.GenericScreenHostAdapter;
import link.botwmcs.fizzy.proxy.host.HostAdapter;
import link.botwmcs.fizzy.proxy.host.HostAdapterRegistry;
import link.botwmcs.fizzy.proxy.host.HostGeometry;
import link.botwmcs.fizzy.proxy.rule.ProxyBuildContext;
import link.botwmcs.fizzy.proxy.rule.ProxyResolutionResult;
import link.botwmcs.fizzy.proxy.rule.ProxyRuleRegistry;
import link.botwmcs.fizzy.proxy.rule.ProxyRuleResolver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;
import java.util.Optional;

public final class ScreenProxyRuntime {
    private static final ScreenProxyRuntime INSTANCE = new ScreenProxyRuntime();

    private final ProxyRuleRegistry ruleRegistry = new ProxyRuleRegistry();
    private final ProxyRuleResolver ruleResolver = new ProxyRuleResolver(ruleRegistry);
    private final HostAdapterRegistry adapterRegistry = new HostAdapterRegistry();
    private final ScreenProxyManager sessionManager = new ScreenProxyManager();

    private ScreenProxyRuntime() {
        adapterRegistry.register(new ContainerScreenHostAdapter());
        adapterRegistry.register(new GenericScreenHostAdapter());
    }

    public static ScreenProxyRuntime instance() {
        return INSTANCE;
    }

    public ProxyRuleRegistry ruleRegistry() {
        return ruleRegistry;
    }

    public HostAdapterRegistry adapterRegistry() {
        return adapterRegistry;
    }

    public ScreenProxyManager sessionManager() {
        return sessionManager;
    }

    public void onScreenInit(@Nullable Screen screen) {
        if (!isProxyEligible(screen)) {
            if (screen != null) {
                sessionManager.remove(screen);
            }
            return;
        }

        ProxyResolutionResult resolved = resolveFor(screen);
        KernelAttachSpec mergedSpec = resolved.mergedSpec();
        if (resolved.appliedRuleIds().isEmpty() && mergedSpec.uiSpec().isEmpty()) {
            sessionManager.remove(screen);
            return;
        }

        Optional<HostAdapter> adapterOpt = adapterRegistry.resolve(screen);
        if (adapterOpt.isEmpty()) {
            sessionManager.remove(screen);
            return;
        }

        sessionManager.remove(screen);
        ScreenProxySession session = sessionManager.ensure(screen, adapterOpt.get(), mergedSpec);
        session.init();
    }

    public void onScreenClosing(@Nullable Screen screen) {
        if (screen == null) {
            return;
        }
        sessionManager.remove(screen);
    }

    public void onRenderStage(
            @Nullable Screen screen,
            HostRenderStage stage,
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        if (!isProxyEligible(screen)) {
            return;
        }
        ScreenProxySession session = sessionManager.get(screen);
        if (session == null || session.isClosed()) {
            return;
        }
        if (!session.isInitialized()) {
            session.init();
        }
        session.renderStage(stage, graphics, mouseX, mouseY, partialTick);
    }

    public boolean onMouseClicked(@Nullable Screen screen, double x, double y, int button) {
        return dispatchInput(screen, session -> session.mouseClicked(x, y, button));
    }

    public boolean onMouseReleased(@Nullable Screen screen, double x, double y, int button) {
        return dispatchInput(screen, session -> session.mouseReleased(x, y, button));
    }

    public boolean onMouseDragged(@Nullable Screen screen, double x, double y, int button, double dragX, double dragY) {
        return dispatchInput(screen, session -> session.mouseDragged(x, y, button, dragX, dragY));
    }

    public boolean onMouseScrolled(@Nullable Screen screen, double x, double y, double scrollX, double scrollY) {
        return dispatchInput(screen, session -> session.mouseScrolled(x, y, scrollX, scrollY));
    }

    public boolean shouldCancelSourceTooltip(@Nullable Screen screen, double x, double y) {
        if (!isProxyEligible(screen)) {
            return false;
        }
        ScreenProxySession session = sessionManager.get(screen);
        if (session == null || session.isClosed()) {
            return false;
        }
        if (!session.isInitialized()) {
            session.init();
        }
        return session.shouldCancelSourceTooltip(x, y);
    }

    private boolean dispatchInput(@Nullable Screen screen, SessionInputDispatcher dispatcher) {
        if (!isProxyEligible(screen)) {
            return false;
        }
        ScreenProxySession session = sessionManager.get(screen);
        if (session == null || session.isClosed()) {
            return false;
        }
        if (!session.isInitialized()) {
            session.init();
        }

        boolean handled = dispatcher.dispatch(session);
        if (!handled) {
            return false;
        }

        InputDispatchPolicy policy = session.spec().inputPolicy();
        return policy == null || policy.cancelSourceWhenHandled();
    }

    private ProxyResolutionResult resolveFor(Screen screen) {
        Optional<HostAdapter> adapterOpt = adapterRegistry.resolve(screen);
        if (adapterOpt.isEmpty()) {
            return new ProxyResolutionResult(KernelAttachSpec.empty(), java.util.List.of(), "No host adapter matched");
        }

        HostAdapter adapter = adapterOpt.get();
        HostGeometry geometry = adapter.resolveGeometry(screen);
        ProxyBuildContext context = new ProxyBuildContext(
                Minecraft.getInstance(),
                screen,
                adapter,
                geometry,
                ScreenSourceModIdResolver.resolve(screen)
        );
        return ruleResolver.resolve(context);
    }

    private static boolean isProxyEligible(@Nullable Screen screen) {
        if (screen == null) {
            return false;
        }
        if (screen instanceof FizzyScreenHost) {
            return false;
        }
        if (screen instanceof FizzyMenuScreenHost<?>) {
            return false;
        }
        return true;
    }

    @FunctionalInterface
    private interface SessionInputDispatcher {
        boolean dispatch(ScreenProxySession session);
    }
}
