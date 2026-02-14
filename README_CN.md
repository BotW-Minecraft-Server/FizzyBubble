# Fizzy

Fizzy 是一个提供轻量 UI 框架的 Minecraft Mod，用于快速构建自定义界面（Screen / Menu），支持 pad、元素、frame、背景和分割线等能力。

## 使用指南

### 1) 使用 `FizzyGuiBuilder` 构建 GUI

`FizzyGuiBuilder` 是主要入口，用于配置尺寸、frame、背景、元素和分割线。

```java
var frame = new FizzyFrame(Component.literal("Example"));

FizzyGui gui = FizzyGuiBuilder.start()
        .sizeSlots(9, 3) // 列, 行（不设置默认 9x3）
        .frame(frame)
        .background(new FizzyBg(BgType.STONE))
        .behind(new BlurBehind())
        .padByFrame()
        .element(new FizzyBackgroundElement(BgType.BARRIER)).done()
        .pad(1, 1, 1, 3)
        .element(ColoredButtonElement.builder(Component.literal("Button"), btn -> {
            // 点击事件
        }).build()).done()
        .build();
```

说明：
- `frame(...)` 必填。
- `background(...)` 默认是 `FizzyBg(BgType.STONE)`。
- `behind(...)` 默认是 `BlurBehind()`。
- Slot 单位为 18px（`UiUnit.SLOT_PX`）。

### 2) 打开 GUI

普通 Screen：
```java
Minecraft.getInstance().setScreen(new FizzyScreenHost(gui));
```

菜单类型：
```java
var screen = new FizzyMenuScreenHost<>(menu, playerInv, Component.literal("Title"), gui);
Minecraft.getInstance().setScreen(screen);
```

### 3) Pad 与元素放置

Pad 定义区域，元素按添加顺序叠放（后添加的在上层）。

```java
// Slot pad（行/列，1-based）
builder.pad(2, 5, 3, 8)
       .element(new SlotElement())
       .done();

// 像素 pad
builder.padByPx(10, 20, 64, 32)
       .element(new IconElement(ResourceLocation.withDefaultNamespace("textures/item/apple.png")))
       .done();

// Frame pad（覆盖整个 frame）
builder.padByFrame()
       .element(new FizzyBackgroundElement(BgType.BARRIER))
       .done();
```

### 4) 内置元素

常见元素：
- 按钮：`FizzyButtonElement`, `ColoredButtonElement`, `WidgetButtonElement`, `IconButtonElement`
- Slot：`SlotElement`
- 图标：`IconElement`
- 背景元素：`FizzyBackgroundElement`
- 底部按钮：`LeftButtonBelow`, `CenterButtonBelow`, `RightButtonBelow`, `DoubleButtonBelow`

示例：图标按钮
```java
var appleButton = IconButtonElement.builder(
        Component.empty(),
        btn -> player.sendSystemMessage(Component.literal("Apple clicked!")),
        ResourceLocation.withDefaultNamespace("textures/item/apple.png")
).build();
```

### 5) 分割线（Split）

```java
builder.splitByPx(UiUnit.SLOT_PX * 3 - 1, 0, UiUnit.SLOT_PX * 4, SplitType.VERTICAL);
```

### 6) 运行时查找元素

两个 Host 都提供查询方法：
```java
List<ElementPainter> elements = screen.elementsAtSlot(2, 5); // slot 行/列
List<ElementPainter> elementsPx = screen.elementsAtPx(mouseX, mouseY); // 像素
```

通过 `ElementType` 过滤类型，并通过 `widgets()` 访问真实控件：
```java
for (ElementPainter element : screen.elementsAtSlot(2, 5)) {
    if (element.type() == ElementType.BUTTON) {
        for (AbstractWidget widget : element.widgets()) {
            widget.active = false;
        }
    }
}
```

## 开发指南

### 创建自定义元素

实现 `ElementPainter`：

```java
public final class MyElement implements ElementPainter {
    @Override
    public void init(InitContext context, int leftPx, int topPx, int widthPx, int heightPx) {
        // 可选：使用 context.addRenderableWidget(...) 添加控件
    }

    @Override
    public void render(GuiGraphics g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick) {
        // 绘制内容
    }

    @Override
    public ElementType type() {
        return ElementType.CUSTOM;
    }
}
```

如果你的元素是按钮并希望被外部系统识别：
1. `type()` 返回 `ElementType.BUTTON`
2. 覆写 `widgets()` 返回所有按钮控件

```java
@Override
public ElementType type() { return ElementType.BUTTON; }

@Override
public List<AbstractWidget> widgets() { return List.of(myButtonWidget); }
```

### 构建器使用建议

Builder 是链式 API，Pad 叠加顺序为添加顺序。需要覆盖型元素（例如遮罩）请放在最后添加。

### 构建与运行（NeoForge）

需要 Java 21。

```bash
./gradlew runClient
```

## TODO
- 增加更多元素（Text, Image, TextField, Slider, Checkbox, RadioButton, Dropdown）
- 如有需要，调整 Menu 的渲染顺序
