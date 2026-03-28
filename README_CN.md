# Fizzy

Fizzy 是一个面向 Minecraft 客户端模组的 UI 框架。  
它提供了：

- 声明式 GUI 构建器（`FizzyGuiBuilder`），可用于 `Screen` 与 `Menu`
- 完整的 kernel 渲染/运行时/状态系统
- layer 分层、modal、notification toast 系统
- 完整的 element 体系（按钮、文本、槽位、图标、图表、滚动容器、右键菜单、阻挡层等）
- 全局 formatting（含 emoji）系统
- proxy 代理系统：在不改原 GUI 类源码的情况下，对现有 Screen 做运行时注入

本 README_CN 会把上述主构造逐项展开，并给出可直接参考的 demo 类。

---

## 1. 核心架构总览

### 1.1 `ui.core`（GUI 组合层）

核心类型：

- `FizzyGuiBuilder`：构建 GUI
- `FizzyGui`：构建后的 GUI 定义对象
- `FizzyGuiSpec`：slot 维度 + host 类型
- `HostType`：`SCREEN` / `MENU`
- `UiUnit`：UI 基础常量（`SLOT_PX = 18`）

`FizzyGuiBuilder` 关键方法：

- `start()`
- `sizeSlots(int rows)`（固定 9 列）
- `@Deprecated sizeSlots(int cols, int rows)`
- `host(HostType)`
- `frame(FramePainter)`（必填）
- `background(BgPainter)`（默认：`new FizzyBg(BgType.STONE)`）
- `behind(BehindPainter)`（默认不设置）
- `below(ElementPainter)`（常用于 Menu 底部带）
- `overrideSizePx(int w, int h)`
- pad：`pad(...)`、`padAuto(...)`、`padByPx(...)`、`padByFrame()`
- split：`split(...)`、`splitByPx(...)`
- `build()`

补充：

- 如果定义了 split 且未显式指定 split painter，框架会自动使用 `new FizzySplit()`。
- `frame(...)` 不设置会在 `build()` 抛异常。

### 1.2 `ui.kernel`（渲染 / 运行时 / 状态 / overlay）

主要子系统：

- Render：`UiRenderPhase`、`UiRenderLayer`、`UiRenderTaskQueue`
- Runtime：`UiRuntime`、`UiMainThreadScheduler`
- 响应式状态：`StateKernel`（`mutableSignal`、`computedSignal`、`effect`、`batch`、`flush`）
- Overlay/Layers：`OverlayLayerStack`、`OverlayLayerKey`、布局引擎、focus/capture 状态
- Modal：`ModalSpec`、`ModalOverlay`、`ModalManager`
- Notification toast：`NotificationSpec`、`NotificationOverlay`、`NotificationManager`

### 1.3 `proxy`（对非 Fizzy 界面做补丁注入）

核心类型：

- `ProxyRule`（`matches` + `build`）
- `ProxyRuleRegistry`、`ProxyRuleResolver`、`KernelSpecMerger`
- `KernelUiSpec`、`KernelAttachSpec`
- 策略：`PhaseBridgePolicy`、`TooltipPolicy`、`InputDispatchPolicy`
- 运行时会话：`ScreenProxyRuntime`、`ScreenProxySession`、`ScreenProxyManager`
- Host 适配器：`ContainerScreenHostAdapter`、`GenericScreenHostAdapter`

---

## 2. 用 Fizzy 构建 Screen/Menu（含 background/behind/core/frame/host/pad/split）

### 2.1 最小构建示例

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

打开 Screen：

```java
Minecraft.getInstance().setScreen(new FizzyScreenHost(gui));
```

打开 Menu Screen：

```java
var screen = new FizzyMenuScreenHost<>(menu, inv, Component.literal("Title"), gui);
Minecraft.getInstance().setScreen(screen);
```

### 2.2 `host`、`frame`、`background`、`behind`、`below`

- `host(HostType.SCREEN)`：
  - 居中普通界面面板，按 Screen 路径渲染
- `host(HostType.MENU)`：
  - 菜单布局，带玩家背包区域和可选 below 带
- `frame(FramePainter)`：
  - 面板风格（`FizzyFrame`、`MotiveFrame`）
- `background(BgPainter)`：
  - 画 frame 内容区域底图
- `behind(BehindPainter)`：
  - 画整屏背后层（模糊/渐变/图片/纯色）
- `below(ElementPainter)`：
  - 挂到 frame 的 below 区域（`currentBelowArea()`）

可用 painter：

- Background：`FizzyBg(BgType)`、`SoildColorBg`
- Behind：`BlurBehind`、`VanillaBehind`、`ImageBehind`、`SoildColorBehind`
- Frame：`FizzyFrame`、`MotiveFrame`（`EasyFrame` 当前为空壳）

### 2.3 pad 系统

- `pad(rowStart, colStart, rowEnd, colEnd)` -> `SlotPadBuilder`
  - 可选 `.inner()`，收缩到 slot 内边距区域
- `padAuto(...)` -> `AutoPadBuilder`
  - 自动考虑 slot 边框和 split 碰撞做 inset
- `padByPx(left, top, width, height)` -> `PixelPadBuilder`
- `padByFrame()` -> `FramePadBuilder`
- 每种 builder 都支持：
  - `.element(...)`
  - `.elements(...)`
  - `.done()`

### 2.4 split 系统

- Slot split：
  - `split(rowStart, colStart, rowEnd, colEnd)`
  - 只能同一行或同一列
- Pixel split：
  - `splitByPx(offsetX, offsetY, lengthPx, SplitType.HORIZONTAL|VERTICAL)`

### 2.5 运行时元素查询

`FizzyScreenHost` / `FizzyMenuScreenHost` 都支持：

```java
List<ElementPainter> atSlot = screen.elementsAtSlot(2, 5);
List<ElementPainter> atPx = screen.elementsAtPx(mouseX, mouseY);
```

### 2.6 按 API 自定义 `Element` / `Frame` / `Background` / `Behind`

#### 自定义 `ElementPainter`

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

#### 自定义 `FramePainter`

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
        g.fill(left + 2, top + 2, left + w - 2, top + 4, 0xFF57D7FF); // 顶部强调线
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

#### 自定义 `BgPainter`

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

#### 自定义 `BehindPainter`

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

### 2.7 按 API 自定义 `Pad` / `Split`

标准场景优先使用 builder 自带方法（`pad`、`padAuto`、`padByPx`、`split`、`splitByPx`）。  
如果需要复杂布局（例如槽位区域外侧栏），可以自己实现 spec。

#### 自定义 `PadSpec`（左侧栏）

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

#### 自定义 `SplitPainter`

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

注意：`FizzyGuiBuilder` 当前没有公开的 `splitPainter(...)`。  
需要挂自定义 `PadSpec` / `SplitPainter` 时，建议走 proxy 侧的 `KernelUiSpec` 注入。

接入示例（proxy 侧）：

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

### 2.8 slots 外部 pad 构造（大箱子 GUI 左侧 FCE 文本）

`padByPx(...)` 是相对 frame 原点定位，不受 slot 坐标约束，所以可以放在 slot 区域外。

示例：在 9x6 大箱子左侧外部放一块 FCE 说明文本：

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
        // slot 外部：X 为负数表示放到面板左侧外区域
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

这个模式可以直接用于：

- 侧边说明面板
- 统计信息条
- 网格外工具区

---

## 3. Kernel 细节（render / runtime / state）

### 3.1 Render phase 与 layer

`UiRenderPhase` 顺序：

1. `BEHIND`
2. `BACKGROUND`
3. `FRAME`
4. `ELEMENT`
5. `SPLIT`
6. `WIDGET`
7. `TOOLTIP`
8. `OVERLAY`

`UiRenderLayer = (phase, order)`。  
`UiRenderTaskQueue` 按 `phase -> order -> serial` 排序执行。

### 3.2 Runtime 与响应式状态

- `UiRuntime.frameTick()`：
  - `scheduler.drain()`
  - `stateKernel.flush()`
- `StateKernel`：
  - 可变信号：`mutableSignal`
  - 计算信号：`computedSignal`
  - 副作用：`effect`
  - 批处理：`batch`

示例：

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
runtime.frameTick();
```

---

## 4. Layer 层级系统 / Modal / Notification Toast

### 4.1 Overlay layer 体系

`OverlayLayerKey`（priority 从低到高）：

- `HUD` (100)
- `NOTIFICATION` (200)
- `MODAL` (300)
- `ANNOUNCE` (900)
- `DEBUG` (1000)

布局模式：

- `FIXED_ANCHOR`
- `PER_INSTANCE_ANCHOR`
- `MANUAL`

`OverlayLayerStack` 提供：

- add/remove/clear/hide
- 按 layer policy 渲染
- 鼠标输入分发（click/release/drag/scroll/move）
- focus 与 pointer capture 管理

### 4.2 Modal 系统

`ModalSpec` 支持：

- `title(Component)`
- `message(Component)`
- `widthPx(int)`
- `heightPx(int)`
- `anchor(Anchor)`

调用：

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

### 4.3 Notification Toast 系统

`NotificationSpec`：

- `title(Component)`
- `message(Component)`
- `level(NotificationLevel)` -> `INFO/SUCCESS/WARNING/ERROR`
- `durationTicks(int)`
- `anchor(Anchor)`

调用：

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

## 5. Elements 全量说明（详细）

### 5.1 基础协议

- `ElementPainter`
  - `init(...)`、`render(...)`
  - `type()`、`layer()`、`zIndex()`
  - `suppressesTooltips()`
  - `widgets()`
- `AnimatableElement`
  - 默认提供 `.animated(ElementAnimation...)`
- `AnimatedElement`
  - 可包装任何 element 并叠加动画

内置动画向量：

- `RotateAnimation`
- `ScaleAnimation`
- `TintAnimation`
- `FreeFallAnimation`

示例：

```java
ElementPainter spinningIcon = IconElement.builder(iconTex).build()
        .animated(new RotateAnimation(35.0f));
```

### 5.2 内置 elements 总表

| Element | 类别 | 用法说明 | 关键 API |
|---|---|---|---|
| `FizzyBackgroundElement` | 图片/背景 | 在 pad 内平铺 `BgType` 贴图 | `new FizzyBackgroundElement(BgType.STONE)` |
| `MapBackgroundElement` | 图片/背景 | 原版地图风格 9-slice 背景 | `new MapBackgroundElement()` |
| `SlotElement` | 槽位 | 按 pad 区域绘制槽位网格 | `new SlotElement()` |
| `IconElement` | 图标 | 按区域适配绘制纹理 | `IconElement.builder(tex).stretchToFit().allowUpscale().build()` |
| `FizzyComponentElement` | 文本 | 富文本多行渲染 | `builder()/singleLine/multiLine`，`addText`，`wrap`，`align`，`lineSpacing`，全套 `TextRenderer` 配置 |
| `FizzyTooltipElement` | Tooltip 文本 | 自定义 tooltip 渲染（TOOLTIP 层） | `builder()`，`maxWidthPx`，`tooltipColors`，`positioner` |
| `FizzyButtonElement` | 按钮 | Fizzy 风格按钮（文+图） | `builder(onPress)`，`text`，`icon`，`tooltip`，`pressSound`，运行时 `set*` |
| `ColoredButtonElement` | 按钮 | 颜色主题按钮 | `builder(onPress).color(...)` + 同类文本/图标/tooltip 能力 |
| `VanillaLikeButtonElement` | 按钮 | 类原版按钮主题 | `builder(onPress).colorTheme(...)` + 同类文本/图标/tooltip 能力 |
| `WidgetButtonElement` | 按钮 | 箭头/控件型按钮 | `builder(message,onPress).type().color().direction().stretchToFit()` |
| `IconButtonElement` | 按钮 | 基于 `CustomIconButton` 的图标按钮 | `builder(message,onPress,texture)`，支持 tooltip/customize/applyToButton |
| `TransparentButtonElement` | 按钮 | 透明底 icon 按钮 | `builder(message,onPress)` |
| `LeftButtonBelow` | below 区 | 一个左侧 below 按钮 | 文本+回调构造，或传入现成 `ColoredButtonElement` |
| `CenterButtonBelow` | below 区 | 一个居中 below 按钮 | 同上 |
| `RightButtonBelow` | below 区 | 一个右侧 below 按钮 | 同上 |
| `DoubleButtonBelow` | below 区 | 两个 below 按钮 | 左右双按钮构造 |
| `ProgressElement` | 自定义可视化 | BossBar 风格进度条 | `builder().progress().color().autoNotches().addNotches()...` + 运行时 CRUD |
| `SimpleChartsElement` | 复合网格 | 网格 cell 容器，cell 级裁剪渲染 | `contentBuilder().grid(...).cell(...).element(...).done()` |
| `SimpleDraggableElement` | 复合滚动 | 可滚动内容容器（slot/pixel pad） | `contentBuilder().pad()/padByPx().contentHeightPx(...)` + scrollbar 配置 |
| `ContextMenuElement` | overlay 菜单 | 右键菜单/子菜单/分隔线/自定义项 | `builder().item().submenu().separator().element().build()` |
| `SlotBlockerElement` | 交互阻挡 | 玻璃动画阻挡层，禁用下层 widget | `new SlotBlockerElement(open)` + `setOpen(...)` |
| `TransparentBlockerElement` | 交互阻挡 | 透明阻挡层 | `new TransparentBlockerElement(open)` + `setOpen(...)` |

### 5.3 文本 element（FCE/FTE）详细配置

`FizzyComponentElement.Builder` 常用能力：

- 内容：`addText`、`addTextLines`
- 布局：`wrap`、`autoEllipsis`、`centerEllipsis`、`align`、`lineSpacing`
- 字体样式：`textScale`、`letterSpacing`、`shadow`、`color`、`bold`、`underline`、`strikethrough`
- 动态样式：`gradient`、`rainbow`、`floating`、`floatingPixelated`
- 文本映射样式（`t2*`）：`t2c`、`t2g`、`t2r`、`t2f`、`t2b`、`t2u`、`t2s`
- 按范围/索引样式：`styleRange`、`styleIndex`

`FizzyTooltipElement.Builder` 在以上基础上还支持：

- `maxWidthPx`
- `tooltipColors`
- `positioner`

### 5.4 按钮 element 详细能力

`FizzyButtonElement`、`ColoredButtonElement`、`VanillaLikeButtonElement` 共同支持：

- 文本来源：
  - `text(Component)` 或 `text(FizzyComponentElement)`
  - `textConfig(...)`
- 图标：
  - `icon(ResourceLocation)` / `icon(FizzyIcon)`
  - `iconFit(stretchToFit, allowUpscale)`
  - `iconSizePx(...)`
  - 排布：
    - `layout(TEXT_LEFT_ICON_RIGHT | ICON_LEFT_TEXT_RIGHT)`
    - `iconAlign(TOP | CENTER | BOTTOM)`
- tooltip：
  - `tooltip(Tooltip)`
  - `tooltip(Component)`
  - `tooltip(FizzyTooltipElement)`
- 交互：
  - `pressSound(...)`
  - `narration(...)`

并且都支持大量运行时 `set*` 动态修改。

### 5.5 复合元素详细说明

- `SimpleChartsElement`
  - 定义固定网格（rows x cols）
  - 通过 `cell(...)` 放置元素
  - cell 可 `inner()` 缩进
  - 子 widget 按 cell 做 scissor 裁剪
- `SimpleDraggableElement`
  - 内容 pad 可用 slot 或 pixel 坐标
  - 支持滚轮 + 拖动滚动条
  - 可调参数：
    - `wheelStepPx`
    - `scrollbarWidthPx`
    - `scrollbarGapPx`
    - `minThumbHeightPx`
- `ContextMenuElement`
  - 在触发区域右键打开
  - 渲染于 `OVERLAY` 层（`UiRenderLayer.overlay(300)`）
  - 菜单展开时可抑制 source tooltip
  - 支持多级 submenu 和自定义 row element

---

## 6. 示例菜单构造类

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

## 7. Formatting 系统（含 emoji）+ 表情包构造 demo

### 7.1 语法

`FizzyComponentParser` 支持：

- `&` 风格控制：
  - 兼容 `&0..&f`、样式位、`&r`
  - 十六进制：`&#RRGGBB`
  - 彩虹标记：`&h`
- 占位符语法：
  - `:id:`
  - `:id(payload):`
- emoji token：
  - 同样走 `:token:` 语法，由 `EmojiRegistry` 解析

### 7.2 全局接入路径

入口：

- `FizzyComponentService`

当 `Config.ENABLE_FIZZY_COMPONENT = true` 时，mixin 会接管：

- 文本宽度 / split / visual order
- drawInBatch 绘制链路
- inline glyph 宽度与渲染（PUA codepoint）
- 聊天中 interactive emoji 点击
- 聊天框 `:emoji:` 自动建议

### 7.3 Emoji 注册方式

- 静态图：
  - `EmojiRegistry.registerStatic(...)`
- 动画图：
  - `EmojiRegistry.registerAnimated(...)`
- 可点击：
  - `EmojiRegistry.registerStaticInteractive(...)`
  - `EmojiRegistry.registerAnimatedInteractive(...)`
- pack：
  - 实现 `EmojiPack`
  - `EmojiRegistry.registerPack(...)`

### 7.4 表情包构造类 demo

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

## 8. Proxy 代理系统（原理、能力、边界、demo）

### 8.1 Proxy 是什么

Proxy 的目标是：对“非 Fizzy 原生 host”的 GUI 做运行时补丁注入，而不是直接改原类源码。

能做的事情：

- 给原版/第三方 Screen 增加 Fizzy element/widget
- 增加 overlay、slot 高亮、快捷按钮、自定义 tooltip
- 通过 render stage 做精细时机插入（`SCREEN_PRE`、`SOURCE_CONTENT_POST` 等）
- 自定义 tooltip 策略、输入分发策略
- 多规则自动合并（pads/splits 追加，单值策略后者覆盖）

注意：

- `FizzyScreenHost` / `FizzyMenuScreenHost` 会被 proxy runtime 排除（它们本身已是 Fizzy host）

### 8.2 规则生命周期

1. 在 `ScreenProxyRuntime.instance().ruleRegistry()` 注册 `ProxyRule`
2. 屏幕初始化时构建 `ProxyBuildContext`
3. Resolver 匹配规则并合并为 `KernelAttachSpec`
4. Session 按映射后的 stage 渲染任务
5. 鼠标输入依据策略转发与拦截

### 8.3 策略层

- `PhaseBridgePolicy`
  - 将 Fizzy phase 映射到 host stage
- `TooltipPolicy`
  - `SOURCE_ONLY`、`FIZZY_ONLY`、`BOTH`、`AUTO_SUPPRESS_SOURCE_WHEN_BLOCKING`
- `InputDispatchPolicy`
  - `overlayFirst`
  - `cancelSourceWhenHandled`
  - `blockSourceWhenHitBlockingElement`

### 8.4 Proxy demo：修改 `TitleScreen` 与工作台 `CraftingScreen`

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
            // 工作台界面（原版类名通常是 CraftingScreen）
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
                    // 用 slot 坐标高亮第一行区域
                    .addPad(new SlotPadSpec(1, 1, 1, 3, true, List.of(new FizzyBackgroundElement(BgType.PURE_GRAY))))
                    .build();

            return new KernelAttachSpec(uiSpec, null, null, null);
        }
    }
}
```

客户端初始化时注册：

```java
DemoProxyRules.registerAll();
```

---

## 9. 实战建议

- 规则尽量“小而单一”，依赖合并器组合行为。
- 只有确实存在冲突时才提高 `priority()`。
- 通用屏幕优先 `PixelPadSpec`，容器类屏幕优先 `SlotPadSpec`。
- 需要严格渲染阶段控制时再实现自定义 `PhaseBridgePolicy`。
