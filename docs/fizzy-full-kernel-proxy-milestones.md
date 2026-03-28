# Fizzy Full Kernel Proxy Milestones

## 目标
让 Fizzy 以 **Full Kernel Proxy** 方式注入任意 `Screen` / `AbstractContainerScreen`，可精准叠加：
- `behind`
- `background`
- `frame`
- `pad`
- `split`
- `elements`
- `widgets`
- `tooltip`
- `overlay`

并保证统一 layer 顺序与输入阻断策略。

## 关键决策
- 规则采用“**多条合并**”，而不是“最高优先级单条”。
- 渲染接入采用“**Mixin 主导 + Event 辅助**”。
- 容器类 GUI 几何基准采用“**可见内容包围盒**”。

## 里程碑
### M1: 核心骨架
- 新增 `proxy/api`：阶段、策略、附加规格接口。
- 新增 `proxy/host`：Host 几何与能力抽象。
- 新增 `proxy/runtime`：Session 与 Manager 骨架。
- 目标：接口签名稳定并可编译。

### M2: 规则系统
- 新增 `proxy/rule`：规则注册、解析、合并器、调试摘要。
- 合并语义：
  - 单例域（frame/bg/behind/策略等）：后者覆盖前者。
  - 集合域（pads/splits）：稳定追加。
- 目标：可得到“最终生效 spec + 命中规则列表 + debug 文本”。

### M3: 生命周期接入（Event）
- 通过 `ScreenEvent` 建立/刷新/销毁 session。
- 统一鼠标输入转发与 `cancel`。

### M4: 渲染阶段桥接（Mixin）
- 建立 `SOURCE_BG / SOURCE_CONTENT / SOURCE_TOOLTIP` 细粒度阶段。
- 将 Fizzy `UiRenderPhase` 映射到 host 阶段。

### M5: HostAdapter 实装
- `GenericScreenAdapter`。
- `ContainerScreenAdapter`（可见包围盒，对齐配方书偏移等场景）。

### M6: 首批规则验证
- 原版工作台左侧外挂 `FizzyButtons`。
- 第三方 `AbstractContainerScreen` 示例（如 Create 风格界面）。

### M7: 调试与容错
- debug overlay / 日志：命中规则、阶段映射、层级排序、输入消费链。
- 单规则异常隔离与自动失效保护。

### M8: 回归验收
- 覆盖原版容器 + 第三方容器 + 非容器 screen。
- 验证层级、点击阻断、tooltip、关闭释放与性能。

