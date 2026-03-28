# Fizzy

Fizzy is a UI framework for Minecraft client mods.  
It provides:

- A declarative GUI builder (`FizzyGuiBuilder`) for `Screen` and `Menu` UIs
- A kernel render/runtime/state stack
- Overlay layer, modal, and notification-toast systems
- A full element system (buttons, text, slot, icon, charts, draggable, context menu, blockers, etc.)
- A text formatting + emoji pipeline
- A proxy system that can inject Fizzy UI patches into existing non-Fizzy vanilla/mod screens

This document explains the main architecture in detail and gives practical class-level demos.

---

## 1. Core Architecture

### 1.1 `ui.core` (GUI composition)

Main types:

- `FizzyGuiBuilder`: build GUI definition
- `FizzyGui`: final immutable GUI object
- `FizzyGuiSpec`: slot dimensions + host type
- `HostType`: `SCREEN` / `MENU`
- `UiUnit`: slot constants (`SLOT_PX = 18`)

`FizzyGuiBuilder` key methods:

- `start()`
- `sizeSlots(int rows)` (fixed 9 columns)
- `@Deprecated sizeSlots(int cols, int rows)`
- `host(HostType)`
- `frame(FramePainter)` (required)
- `background(BgPainter)` (default: `new FizzyBg(BgType.STONE)`)
- `behind(BehindPainter)` (default: none)
- `below(ElementPainter)` (for below-band content, typical on menus)
- `overrideSizePx(int w, int h)`
- Pads: `pad(...)`, `padAuto(...)`, `padByPx(...)`, `padByFrame()`
- Splits: `split(...)`, `splitByPx(...)`
- `build()`

Notes:

- If splits are defined and no split painter is provided internally, Fizzy uses `new FizzySplit()`.
- `frame(...)` is mandatory.

### 1.2 `ui.kernel` (render/runtime/state/overlay)

Main subsystems:

- Render: `UiRenderPhase`, `UiRenderLayer`, `UiRenderTaskQueue`
- Runtime: `UiRuntime`, `UiMainThreadScheduler`
- Reactive state: `StateKernel` (`mutableSignal`, `computedSignal`, `effect`, `batch`, `flush`)
- Overlay/layer stack: `OverlayLayerStack`, `OverlayLayerKey`, layout engine, focus/capture state
- Modal: `ModalSpec`, `ModalOverlay`, `ModalManager`
- Notification toast: `NotificationSpec`, `NotificationOverlay`, `NotificationManager`

### 1.3 `proxy` (patch non-Fizzy screens)

Main types:

- `ProxyRule` (`matches` + `build`)
- `ProxyRuleRegistry`, `ProxyRuleResolver`, `KernelSpecMerger`
- `KernelUiSpec`, `KernelAttachSpec`
- Policies: `PhaseBridgePolicy`, `TooltipPolicy`, `InputDispatchPolicy`
- Runtime/session: `ScreenProxyRuntime`, `ScreenProxySession`, `ScreenProxyManager`
- Host adapters: `ContainerScreenHostAdapter`, `GenericScreenHostAdapter`

---

## 2. Building UI (Screen/Menu, background/behind/frame/host/pad/split)

### 2.1 Minimal builder example

```java
import link.botwmcs.fizzy.ui.background.BgType;
import link.botwmcs.fizzy.ui.background.FizzyBg;
import link.botwmcs.fizzy.ui.behind.BlurBehind;
import link.botwmcs.fizzy.ui.core.FizzyGui;
import link.botwmcs.fizzy.ui.core.FizzyGuiBuilder;
import link.botwmcs.fizzy.ui.core.HostType;
import link.botwmcs.fizzy.ui.element.background.FizzyBackgroundElement;
import link.botwmcs.fizzy.ui.element.slot.SlotElement;
import link.botwmcs.fizzy.ui.frame.FizzyFrame;
import net.minecraft.network.chat.Component;

FizzyGui gui = FizzyGuiBuilder.start()
        .host(HostType.SCREEN)
        .sizeSlots(6) // 9x6
        .frame(new FizzyFrame(Component.literal("Demo")))
        .background(new FizzyBg(BgType.STONE))
        .behind(new BlurBehind())
        .padByFrame()
        .element(new FizzyBackgroundElement(BgType.BARRIER_BLUE))
        .done()
        .pad(1, 1, 6, 9)
        .element(new SlotElement())
        .done()
        .build();
```

Open with host:

```java
Minecraft.getInstance().setScreen(new FizzyScreenHost(gui));
```

For menu screens:

```java
var screen = new FizzyMenuScreenHost<>(menu, inv, Component.literal("Title"), gui);
Minecraft.getInstance().setScreen(screen);
```

### 2.2 `host`, `frame`, `background`, `behind`, `below`

- `host(HostType.SCREEN)`:
  - centered GUI panel, screen-style frame rendering
- `host(HostType.MENU)`:
  - menu layout with player inventory region and optional below band
- `frame(FramePainter)`:
  - choose style (`FizzyFrame`, `MotiveFrame`)
- `background(BgPainter)`:
  - paints frame content area
- `behind(BehindPainter)`:
  - paints full-screen behind layer (blur/vanilla/image/color)
- `below(ElementPainter)`:
  - attaches one element to frame below-area (`FramePainter.currentBelowArea()`)

Available painters:

- Background: `FizzyBg(BgType)`, `SoildColorBg`
- Behind: `BlurBehind`, `VanillaBehind`, `ImageBehind`, `SoildColorBehind`
- Frame: `FizzyFrame`, `MotiveFrame` (`EasyFrame` is currently empty shell)

### 2.3 `pad` system (slot/pixel/frame/auto)

- `pad(rowStart, colStart, rowEnd, colEnd)` -> `SlotPadBuilder`
  - optional `.inner()` shrinks to slot inner area
- `padAuto(...)` -> `AutoPadBuilder`
  - auto-inset with slot borders + split collisions
- `padByPx(left, top, width, height)` -> `PixelPadBuilder`
- `padByFrame()` -> `FramePadBuilder`
- each builder supports:
  - `.element(...)`
  - `.elements(...)`
  - `.done()`

### 2.4 `split` system

- Slot split:
  - `split(rowStart, colStart, rowEnd, colEnd)`
  - must be same row or same column
- Pixel split:
  - `splitByPx(offsetX, offsetY, lengthPx, SplitType.HORIZONTAL|VERTICAL)`

### 2.5 Runtime element lookup (host APIs)

Both hosts provide:

```java
List<ElementPainter> atSlot = screen.elementsAtSlot(2, 5);
List<ElementPainter> atPx = screen.elementsAtPx(mouseX, mouseY);
```

### 2.6 Build Your Own `Element` / `Frame` / `Background` / `Behind`

#### Custom `ElementPainter`

```java
import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.element.ElementType;
import net.minecraft.client.gui.GuiGraphics;

public final class GuideLineElement implements ElementPainter {
    @Override
    public void render(GuiGraphics g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick) {
        int y = topPx + Math.max(0, heightPx / 2);
        g.fill(leftPx, y, leftPx + widthPx, y + 1, 0xFF57D7FF);
    }

    @Override
    public ElementType type() {
        return ElementType.CUSTOM;
    }
}
```

#### Custom `FramePainter`

```java
import link.botwmcs.fizzy.ui.frame.FizzyFrame;
import link.botwmcs.fizzy.ui.frame.FrameMetrics;
import link.botwmcs.fizzy.ui.frame.FramePainter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class AccentFrame implements FramePainter {
    private final FizzyFrame delegate;
    private Layout layout;

    public AccentFrame(Component title) {
        this.delegate = new FizzyFrame(title);
    }

    @Override
    public void paint(GuiGraphics g, int left, int top, int w, int h, boolean drawBottomEdge, boolean hasBelow) {
        delegate.paint(g, left, top, w, h, drawBottomEdge, hasBelow);
        g.fill(left + 2, top + 2, left + w - 2, top + 4, 0xFF57D7FF); // accent line
    }

    @Override
    public FrameMetrics metrics() {
        return delegate.metrics();
    }

    @Override
    public void setLayout(int left, int top, int w, int h, boolean drawBottomEdge, boolean hasBelow) {
        this.layout = new Layout(left, top, w, h, drawBottomEdge, hasBelow);
        delegate.setLayout(left, top, w, h, drawBottomEdge, hasBelow);
    }

    @Override
    public Layout layout() {
        return layout;
    }
}
```

#### Custom `BgPainter`

```java
import link.botwmcs.fizzy.ui.background.BgPainter;
import link.botwmcs.fizzy.ui.frame.FramePainter;
import net.minecraft.client.gui.GuiGraphics;

public final class GridBg implements BgPainter {
    @Override
    public void paint(GuiGraphics g, FramePainter frame) {
        var a = frame.currentBackgroundArea();
        g.fill(a.x(), a.y(), a.x() + a.w(), a.y() + a.h(), 0xCC111111);
        for (int x = a.x(); x < a.x() + a.w(); x += 9) {
            g.fill(x, a.y(), x + 1, a.y() + a.h(), 0x3314B8FF);
        }
    }
}
```

#### Custom `BehindPainter`

```java
import link.botwmcs.fizzy.ui.behind.BehindPainter;
import link.botwmcs.fizzy.ui.behind.BehindType;
import link.botwmcs.fizzy.ui.frame.FramePainter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class DimBehind implements BehindPainter {
    @Override
    public void paint(GuiGraphics g, FramePainter painter, float partialTick) {
        var mc = Minecraft.getInstance();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        g.fill(0, 0, sw, sh, 0x88000000);
    }

    @Override
    public BehindType type() {
        return BehindType.SOILD_COLOR;
    }
}
```

### 2.7 Build Your Own `Pad` / `Split`

For standard UI construction, use builder pads/splits (`pad`, `padAuto`, `padByPx`, `split`, `splitByPx`).  
For advanced layouts (including outside-slot regions), implement custom specs.

#### Custom `PadSpec` (sidebar-style)

```java
import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.frame.FramePainter;
import link.botwmcs.fizzy.ui.pad.PadSpec;

import java.util.List;

public record LeftSidebarPadSpec(int widthPx, int gapPx, List<ElementPainter> elements) implements PadSpec {
    @Override
    public PadBounds resolve(FramePainter frame, FramePainter.SlotArea slotArea) {
        var layout = frame.layout();
        int h = slotArea != null ? slotArea.h() : layout.h();
        int y = slotArea != null ? slotArea.y() : layout.top();
        int x = layout.left() - Math.max(0, gapPx) - Math.max(0, widthPx);
        return new PadBounds(x, y, Math.max(0, widthPx), Math.max(0, h));
    }
}
```

#### Custom `SplitPainter`

```java
import link.botwmcs.fizzy.ui.split.FizzySplitMetrics;
import link.botwmcs.fizzy.ui.split.SplitMetrics;
import link.botwmcs.fizzy.ui.split.SplitPainter;
import link.botwmcs.fizzy.ui.split.SplitType;
import net.minecraft.client.gui.GuiGraphics;

public final class DotSplitPainter implements SplitPainter {
    private static final SplitMetrics METRICS = FizzySplitMetrics.ofDefault();

    @Override
    public void paint(GuiGraphics g, int x, int y, int lengthPx, SplitType type) {
        if (type == SplitType.VERTICAL) {
            for (int i = 0; i < lengthPx; i += 2) {
                g.fill(x, y + i, x + 1, y + i + 1, 0xFF57D7FF);
            }
        } else {
            for (int i = 0; i < lengthPx; i += 2) {
                g.fill(x + i, y, x + i + 1, y + 1, 0xFF57D7FF);
            }
        }
    }

    @Override
    public SplitMetrics metrics() {
        return METRICS;
    }
}
```

Note: `FizzyGuiBuilder` currently has no public `splitPainter(...)` setter.  
When you need custom `PadSpec` / `SplitPainter`, apply them through proxy-side `KernelUiSpec`.

Example wiring (proxy side):

```java
KernelUiSpec uiSpec = KernelUiSpec.builder()
        .addPad(new LeftSidebarPadSpec(
                90,
                8,
                List.of(FizzyComponentElement.singleLine("Sidebar"))
        ))
        .splitPainter(new DotSplitPainter())
        .addSplit(new PixelSplitSpec(0, 0, 120, SplitType.VERTICAL))
        .build();
```

### 2.8 Pad Outside Slot Area (Large Chest Left-Side Text)

You can place pads outside slot grid with `padByPx(...)`, because `PixelPadSpec` is resolved from frame layout origin (not from slot coordinates).

Example: add `FCE` text to the left side of a 9x6 container UI:

```java
import link.botwmcs.fizzy.ui.core.FizzyGui;
import link.botwmcs.fizzy.ui.core.FizzyGuiBuilder;
import link.botwmcs.fizzy.ui.core.HostType;
import link.botwmcs.fizzy.ui.element.component.FizzyComponentElement;
import link.botwmcs.fizzy.ui.element.slot.SlotElement;
import link.botwmcs.fizzy.ui.frame.FizzyFrame;
import net.minecraft.network.chat.Component;

FizzyFrame frame = new FizzyFrame(Component.literal("Large Chest + Sidebar"));

FizzyGui gui = FizzyGuiBuilder.start()
        .host(HostType.MENU)
        .sizeSlots(6)
        .frame(frame)
        .pad(1, 1, 6, 9)
        .element(new SlotElement())
        .done()
        // Outside slots: negative X means "left of panel origin"
        .padByPx(-96, frame.metrics().slotStartTopPx(), 88, frame.metrics().slotSizePx() * 6)
        .element(FizzyComponentElement.builder()
                .addText(Component.literal("Tips"))
                .addText(Component.literal("- Shift-click to move stacks"))
                .addText(Component.literal("- Right-click splits stack"))
                .wrap(true)
                .lineSpacing(1.0f)
                .shadow(true)
                .build())
        .done()
        .build();
```

You can use the same pattern for:

- side help panels
- custom stat/summary strips
- out-of-grid interactive tools

---

## 3. Kernel Render / Runtime / State

### 3.1 Render phases and layers

`UiRenderPhase` order:

1. `BEHIND`
2. `BACKGROUND`
3. `FRAME`
4. `ELEMENT`
5. `SPLIT`
6. `WIDGET`
7. `TOOLTIP`
8. `OVERLAY`

`UiRenderLayer` is `(phase, order)`.  
`UiRenderTaskQueue` sorts by `phase -> order -> serial`.

### 3.2 Runtime and reactive state

- `UiRuntime.frameTick()` does:
  - `scheduler.drain()`
  - `stateKernel.flush()`
- `StateKernel` supports:
  - mutable signal: `mutableSignal`
  - computed signal: `computedSignal`
  - effects: `effect`
  - transaction batching: `batch`

Example:

```java
UiRuntime runtime = UiRuntime.createForCurrentThread();
var state = runtime.state();

var count = state.mutableSignal(0);
var text = state.computedSignal(() -> "Count = " + count.get());
state.effect(() -> System.out.println(text.get()));

state.batch(() -> {
    count.set(1);
    count.set(2);
});
runtime.frameTick(); // effect flush
```

---

## 4. Layer System / Modal / Notification Toast

### 4.1 Overlay layer hierarchy

`OverlayLayerKey` (priority low -> high):

- `HUD` (100)
- `NOTIFICATION` (200)
- `MODAL` (300)
- `ANNOUNCE` (900)
- `DEBUG` (1000)

Layout modes:

- `FIXED_ANCHOR`
- `PER_INSTANCE_ANCHOR`
- `MANUAL`

`OverlayLayerStack` provides:

- add/remove/clear/hide layer entries
- render with layout policy
- input dispatch (click/release/drag/scroll/move)
- focus state + pointer capture state

### 4.2 Modal system

`ModalSpec`:

- `title(Component)`
- `message(Component)`
- `widthPx(int)`
- `heightPx(int)`
- `anchor(Anchor)`

Usage:

```java
ModalManager.show(
        ModalSpec.builder()
                .title(Component.literal("Confirm"))
                .message(Component.literal("Apply this action?"))
                .widthPx(240)
                .heightPx(100)
                .build()
);
```

### 4.3 Notification toast system

`NotificationSpec`:

- `title(Component)`
- `message(Component)`
- `level(NotificationLevel)` -> `INFO/SUCCESS/WARNING/ERROR`
- `durationTicks(int)`
- `anchor(Anchor)`

Usage:

```java
NotificationManager.show(
        NotificationSpec.builder()
                .title(Component.literal("Saved"))
                .message(Component.literal("Profile updated"))
                .level(NotificationLevel.SUCCESS)
                .durationTicks(80)
                .build()
);
```

---

## 5. Elements (Full Catalog + How To Use)

## 5.1 Element base contracts

- `ElementPainter`
  - `init(...)`, `render(...)`
  - `type()`, `layer()`, `zIndex()`
  - `suppressesTooltips()`
  - `widgets()` (for interactive widgets owned by this element)
- `AnimatableElement`
  - default `.animated(ElementAnimation...)`
- `AnimatedElement`
  - wraps any element with animations

Built-in animation vectors:

- `RotateAnimation`
- `ScaleAnimation`
- `TintAnimation`
- `FreeFallAnimation`

Example:

```java
ElementPainter spinningIcon = IconElement.builder(iconTex).build()
        .animated(new RotateAnimation(35.0f));
```

### 5.2 Element catalog

| Element | Category | Usage summary | Key APIs |
|---|---|---|---|
| `FizzyBackgroundElement` | Image/background | Tile a `BgType` texture inside a pad | `new FizzyBackgroundElement(BgType.STONE)` |
| `MapBackgroundElement` | Image/background | Vanilla map style 9-slice background | `new MapBackgroundElement()` |
| `SlotElement` | Slot | Draw slot grid visuals for pad area | `new SlotElement()` |
| `IconElement` | Icon | Draw texture fit/fill in pad | `IconElement.builder(tex).stretchToFit().allowUpscale().build()` |
| `FizzyComponentElement` | Text | Rich text lines rendered in a pad | `builder()`, `singleLine`, `multiLine`, `addText`, `wrap`, `align`, `lineSpacing`, full `TextRenderer` options |
| `FizzyTooltipElement` | Tooltip text | Custom tooltip renderer in `TOOLTIP` layer | `builder()`, `addText`, `maxWidthPx`, `tooltipColors`, `positioner` |
| `FizzyButtonElement` | Button | Fizzy-styled text/icon button | `builder(onPress)`, `text`, `icon`, `tooltip`, `pressSound`, `set*` runtime mutators |
| `ColoredButtonElement` | Button | Colored button theme | `builder(onPress).color(...)` + same text/icon/tooltip API |
| `VanillaLikeButtonElement` | Button | Vanilla-like button theme | `builder(onPress).colorTheme(...)` + same text/icon/tooltip API |
| `WidgetButtonElement` | Button | Arrow/utility widget button | `builder(message, onPress).type().color().direction().stretchToFit()` |
| `IconButtonElement` | Button | Texture icon button (`CustomIconButton`) | `builder(message, onPress, texture)`, tooltip/customize/applyToButton |
| `TransparentButtonElement` | Button | Transparent-base icon button | `builder(message, onPress)` |
| `LeftButtonBelow` | Below band | One left-aligned below-band button | constructors with text/onPress/customizer or existing `ColoredButtonElement` |
| `CenterButtonBelow` | Below band | One centered below-band button | same constructor style |
| `RightButtonBelow` | Below band | One right-side below-band button | same constructor style |
| `DoubleButtonBelow` | Below band | Two below-band buttons (left/right) | constructors with messages/handlers/customizers |
| `ProgressElement` | Custom visual | Boss-bar style progress bar | `builder().progress().color().autoNotches().addNotches()...` + runtime CRUD |
| `SimpleChartsElement` | Composite grid | Grid container with cell-level elements + clipping | `contentBuilder().grid(...).cell(...).element(...).done()` |
| `SimpleDraggableElement` | Composite scroll | Scrollable container with slot/pixel content pads | `contentBuilder().pad(...)/padByPx(...).contentHeightPx(...)` + scrollbar config |
| `ContextMenuElement` | Overlay menu | Right-click popup/submenu/separator/custom row element | `builder().item().submenu().separator().element().build()` |
| `SlotBlockerElement` | Interaction blocker | Animated glass blocker that disables underlying widgets | `new SlotBlockerElement(open)` + `setOpen(...)` |
| `TransparentBlockerElement` | Interaction blocker | Transparent click blocker | `new TransparentBlockerElement(open)` + `setOpen(...)` |

### 5.3 Text element details (`FCE` / `FTE`)

`FizzyComponentElement.Builder` supports:

- Content: `addText`, `addTextLines`
- Layout: `wrap`, `autoEllipsis`, `centerEllipsis`, `align`, `lineSpacing`
- Typo style: `textScale`, `letterSpacing`, `shadow`, `color`, `bold`, `underline`, `strikethrough`
- Dynamic style: `gradient`, `rainbow`, `floating`, `floatingPixelated`
- Targeted style map (`t2*`): `t2c`, `t2g`, `t2r`, `t2f`, `t2b`, `t2u`, `t2s`
- Range/index style: `styleRange`, `styleIndex`

`FizzyTooltipElement.Builder` mirrors the same text style API, plus tooltip-specific:

- `maxWidthPx`
- `tooltipColors`
- `positioner`

### 5.4 Button element details

`FizzyButtonElement`, `ColoredButtonElement`, `VanillaLikeButtonElement` all support:

- text source:
  - `text(Component)` or `text(FizzyComponentElement)`
  - `textConfig(...)`
- icon:
  - `icon(ResourceLocation)` or `icon(FizzyIcon)`
  - `iconFit(stretchToFit, allowUpscale)`
  - `iconSizePx(...)`
  - layout/alignment:
    - `layout(TEXT_LEFT_ICON_RIGHT | ICON_LEFT_TEXT_RIGHT)`
    - `iconAlign(TOP | CENTER | BOTTOM)`
- tooltip:
  - `tooltip(Tooltip)`
  - `tooltip(Component)`
  - `tooltip(FizzyTooltipElement)`
- interaction:
  - `pressSound(...)`
  - `narration(...)`

Runtime mutators are available through `set*` methods.

### 5.5 Composite element details

- `SimpleChartsElement`
  - define exact grid (`rows x cols`)
  - place cells with `cell(...)`
  - optional `inner()` at cell level
  - all child widgets are clipped to their cells
- `SimpleDraggableElement`
  - content pads can be slot-based or pixel-based
  - supports wheel + drag scrollbar
  - configurable:
    - `wheelStepPx`
    - `scrollbarWidthPx`
    - `scrollbarGapPx`
    - `minThumbHeightPx`
- `ContextMenuElement`
  - opens on right-click inside its trigger region
  - renders in `OVERLAY` layer (`UiRenderLayer.overlay(300)`)
  - can suppress source tooltips while open
  - supports nested submenu and custom row elements

---

## 6. Example Menu Construction Class

```java
package demo;

import link.botwmcs.fizzy.menu.FizzyTestMenu;
import link.botwmcs.fizzy.ui.background.BgType;
import link.botwmcs.fizzy.ui.background.FizzyBg;
import link.botwmcs.fizzy.ui.behind.VanillaBehind;
import link.botwmcs.fizzy.ui.core.FizzyGui;
import link.botwmcs.fizzy.ui.core.FizzyGuiBuilder;
import link.botwmcs.fizzy.ui.core.HostType;
import link.botwmcs.fizzy.ui.element.below.DoubleButtonBelow;
import link.botwmcs.fizzy.ui.element.component.FizzyComponentElement;
import link.botwmcs.fizzy.ui.element.slot.SlotElement;
import link.botwmcs.fizzy.ui.frame.MotiveFrame;
import link.botwmcs.fizzy.ui.host.FizzyMenuScreenHost;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class DemoMenuScreen extends FizzyMenuScreenHost<FizzyTestMenu> {
    public DemoMenuScreen(FizzyTestMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, buildGui(title));
    }

    private static FizzyGui buildGui(Component title) {
        int rows = FizzyTestMenu.CONTAINER_ROWS;
        MotiveFrame frame = new MotiveFrame(title, false);

        return FizzyGuiBuilder.start()
                .host(HostType.MENU)
                .sizeSlots(rows)
                .frame(frame)
                .behind(new VanillaBehind())
                .background(new FizzyBg(BgType.BARRIER_BLUE))
                .pad(1, 1, rows, 9)
                .element(new SlotElement())
                .done()
                .padAuto(1, 1, 1, 9)
                .element(FizzyComponentElement.builder()
                        .addText(Component.literal("Demo Menu"))
                        .align(link.botwmcs.fizzy.client.util.TextRenderer.Align.CENTER)
                        .shadow(true)
                        .build())
                .done()
                .split(1, 4, rows, 4)
                .below(new DoubleButtonBelow(
                        Component.literal("Apply"), b -> {},
                        Component.literal("Close"), b -> {}
                ))
                .build();
    }
}
```

---

## 7. Formatting System (including emoji) + Emoji Pack Demo

### 7.1 Formatting syntax

`FizzyComponentParser` supports:

- Legacy code prefix: `&`
  - vanilla-like `&0..&f`, style flags, reset `&r`
  - hex color: `&#RRGGBB`
  - rainbow marker: `&h`
- Placeholder tokens:
  - `:id:`
  - `:id(payload):`
- Emoji token:
  - same token syntax (`:my_emoji:`), resolved through `EmojiRegistry`

### 7.2 Pipeline integration

Global formatting entry:

- `FizzyComponentService`

When `Config.ENABLE_FIZZY_COMPONENT` is true, mixins patch:

- text width/split/visual-order
- drawInBatch render path
- inline glyph width + rendering (private-use codepoints)
- chat click for interactive emoji
- chat command suggestion popup for `:emoji:` completion

### 7.3 Emoji registry usage

- static:
  - `EmojiRegistry.registerStatic(...)`
- animated:
  - `EmojiRegistry.registerAnimated(...)`
- interactive:
  - `EmojiRegistry.registerStaticInteractive(...)`
  - `EmojiRegistry.registerAnimatedInteractive(...)`
- packs:
  - implement `EmojiPack`
  - `EmojiRegistry.registerPack(...)`

### 7.4 Emoji pack class demo

```java
package demo;

import link.botwmcs.fizzy.client.formatting.emoji.EmojiClickContext;
import link.botwmcs.fizzy.client.formatting.emoji.EmojiPack;
import link.botwmcs.fizzy.client.formatting.emoji.EmojiRegistry;
import link.botwmcs.fizzy.client.formatting.inline.AnimatedInlineImageSource;
import link.botwmcs.fizzy.client.formatting.inline.StaticInlineImageSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class DemoEmojiPack implements EmojiPack {
    public static final String ID = "demo_pack";

    public static void register() {
        EmojiRegistry.registerPack(new DemoEmojiPack());
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void register(Registrar registrar) {
        registrar.token(
                "spark",
                new StaticInlineImageSource(
                        ResourceLocation.fromNamespaceAndPath("fizzy", "textures/gui/components/icon/star_4x3.png"),
                        16.0f,
                        16.0f
                )
        );

        registrar.token(
                "pulse",
                new AnimatedInlineImageSource(
                        List.of(
                                ResourceLocation.fromNamespaceAndPath("fizzy", "textures/gui/components/icon/heart.png"),
                                ResourceLocation.fromNamespaceAndPath("fizzy", "textures/gui/components/icon/warning.png")
                        ),
                        120L,
                        16.0f,
                        16.0f
                )
        );

        registrar.tokenInteractive(
                "help",
                new StaticInlineImageSource(
                        ResourceLocation.fromNamespaceAndPath("fizzy", "textures/gui/components/icon/light-bulb.png"),
                        16.0f,
                        16.0f
                ),
                DemoEmojiPack::onHelpClicked
        );
    }

    private static void onHelpClicked(EmojiClickContext ctx) {
        if (ctx.minecraft().player != null) {
            ctx.minecraft().player.sendSystemMessage(Component.literal("Clicked :" + ctx.token() + ":"));
        }
    }
}
```

---

## 8. Proxy System (Design, Capabilities, and Demos)

### 8.1 What proxy is

Proxy allows you to patch non-Fizzy screens at runtime, without editing source GUI classes directly:

- Add Fizzy elements/widgets to vanilla or mod screens
- Add overlays, slot highlights, custom buttons, tooltips
- Inject by stage (`SCREEN_PRE`, `SOURCE_CONTENT_POST`, etc.)
- Control tooltip strategy and input dispatch strategy
- Merge multiple rules into one final patch spec

Eligibility note:

- `FizzyScreenHost` and `FizzyMenuScreenHost` are excluded from proxy runtime (already native Fizzy hosts)

### 8.2 Rule lifecycle

1. Register `ProxyRule` into `ScreenProxyRuntime.instance().ruleRegistry()`
2. On screen init, runtime builds `ProxyBuildContext`
3. Resolver matches rules and merges `KernelAttachSpec`
4. Session renders patched tasks by mapped stages
5. Mouse events are dispatched through policy

### 8.3 Policies

- `PhaseBridgePolicy`
  - maps Fizzy phase to host render stage
- `TooltipPolicy`
  - `SOURCE_ONLY`, `FIZZY_ONLY`, `BOTH`, `AUTO_SUPPRESS_SOURCE_WHEN_BLOCKING`
- `InputDispatchPolicy`
  - `overlayFirst`
  - `cancelSourceWhenHandled`
  - `blockSourceWhenHitBlockingElement`

### 8.4 Proxy demo: patch `TitleScreen` and workbench `CraftingScreen`

```java
package demo;

import link.botwmcs.fizzy.Fizzy;
import link.botwmcs.fizzy.proxy.api.KernelAttachSpec;
import link.botwmcs.fizzy.proxy.api.KernelUiSpec;
import link.botwmcs.fizzy.proxy.api.TooltipPolicy;
import link.botwmcs.fizzy.proxy.rule.ProxyBuildContext;
import link.botwmcs.fizzy.proxy.rule.ProxyRule;
import link.botwmcs.fizzy.proxy.runtime.ScreenProxyRuntime;
import link.botwmcs.fizzy.ui.background.BgType;
import link.botwmcs.fizzy.ui.element.background.FizzyBackgroundElement;
import link.botwmcs.fizzy.ui.element.button.VanillaLikeButtonElement;
import link.botwmcs.fizzy.ui.pad.PixelPadSpec;
import link.botwmcs.fizzy.ui.pad.SlotPadSpec;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class DemoProxyRules {
    private DemoProxyRules() {}

    public static void registerAll() {
        var registry = ScreenProxyRuntime.instance().ruleRegistry();
        registry.register(new TitleScreenRule());
        registry.register(new CraftingScreenRule());
    }

    private static final class TitleScreenRule implements ProxyRule {
        private static final ResourceLocation ID =
                ResourceLocation.fromNamespaceAndPath(Fizzy.MODID, "demo/title_buttons");

        @Override
        public ResourceLocation id() {
            return ID;
        }

        @Override
        public int priority() {
            return 100;
        }

        @Override
        public boolean matches(ProxyBuildContext context) {
            return context.screen() instanceof TitleScreen;
        }

        @Override
        public KernelAttachSpec build(ProxyBuildContext context) {
            int w = context.geometry().rootWidth();
            int x = Math.max(8, w - 140);

            var docsBtn = VanillaLikeButtonElement.builder(btn -> {
            }).text(Component.literal("Docs")).build();
            var newsBtn = VanillaLikeButtonElement.builder(btn -> {
            }).text(Component.literal("News")).build();

            KernelUiSpec uiSpec = KernelUiSpec.builder()
                    .addPad(new PixelPadSpec(x, 40, 120, 20, List.of(docsBtn)))
                    .addPad(new PixelPadSpec(x, 66, 120, 20, List.of(newsBtn)))
                    .build();

            return new KernelAttachSpec(uiSpec, null, TooltipPolicy.BOTH, null);
        }
    }

    private static final class CraftingScreenRule implements ProxyRule {
        private static final ResourceLocation ID =
                ResourceLocation.fromNamespaceAndPath(Fizzy.MODID, "demo/crafting_overlay");

        @Override
        public ResourceLocation id() {
            return ID;
        }

        @Override
        public int priority() {
            return 200;
        }

        @Override
        public boolean matches(ProxyBuildContext context) {
            if (!(context.screen() instanceof AbstractContainerScreen<?> screen)) {
                return false;
            }
            // Workbench screen class in vanilla client:
            return screen.getClass().getName().endsWith(".CraftingScreen");
        }

        @Override
        public KernelAttachSpec build(ProxyBuildContext context) {
            int rootW = context.geometry().rootWidth();

            var helpButton = VanillaLikeButtonElement.builder(btn -> {
            })
                    .text(Component.literal("Recipe+"))
                    .build();

            KernelUiSpec uiSpec = KernelUiSpec.builder()
                    .addPad(new PixelPadSpec(Math.max(4, rootW - 86), 6, 80, 16, List.of(helpButton)))
                    // Highlight first row region in slot coordinates.
                    .addPad(new SlotPadSpec(1, 1, 1, 3, true, List.of(new FizzyBackgroundElement(BgType.PURE_GRAY))))
                    .build();

            return new KernelAttachSpec(uiSpec, null, null, null);
        }
    }
}
```

Register once during client setup:

```java
DemoProxyRules.registerAll();
```

---

## 9. Practical Notes

- Keep rules focused and small; rely on merger to compose behavior.
- Use high `priority()` only when ordering truly matters.
- Prefer `PixelPadSpec` for generic screens and `SlotPadSpec` for container-aware patches.
- If you need strict stage placement, provide a custom `PhaseBridgePolicy`.
