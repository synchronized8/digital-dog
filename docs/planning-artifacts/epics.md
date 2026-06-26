---
stepsCompleted: [1, 2, 3, 4]
inputDocuments:
  - docs/planning-artifacts/prds/prd-digital-dog-2026-06-23/prd.md
  - docs/planning-artifacts/architecture.md
  - docs/planning-artifacts/ux-design-specification.md
  - docs/MVP-SPEC.md
  - docs/UI-SPEC.md
  - docs/LIPSYNC-SPEC.md
  - docs/planning-artifacts/platform-decision-ipad-display.md
  - docs/brainstorming/brainstorming-session-20260623-144420.md
workflowType: 'epics-and-stories'
lastStep: 4
status: 'complete'
completedAt: '2026-06-24'
lastOptimizedAt: '2026-06-25'
project_name: '数字狗语音口型 Demo'
user_name: 'kejincheng'
date: '2026-06-24'
extractionStatus: 'confirmed'
---

# 数字狗可爱讲话动画 MVP - Epic 与 Story

## 当前执行口径

以 2026-06-25 Sprint 范围重置为准：当前 MVP 不是“精确口型同步 Demo”，而是“可爱小狗讲话动画 UI”。

本 Sprint 只验收：

- Android 原生 Demo 能在 Redmi Pad SE 横竖屏核心视口运行。
- 首屏突出小狗舞台、文本输入、`让狗狗说话` 和 `播放示例`。
- 非空文本或示例能触发准备、讲话、完成、回待机。
- 讲话时小狗张口或轻微开合；非讲话状态闭口。
- 小狗有克制的俏皮动作、完成反馈和可恢复错误反馈。
- 空文本、忙碌状态、减少动态、基础 TalkBack 语义和布局不重叠有测试或手动验收保障。

本 Sprint 不验收：

- 7 类嘴型作为产品目标。
- `LipSyncTimeline` 新增能力、文本高亮、波形、嘴型时间轴、延迟校准。
- 上传音频、麦克风录音、音频分析、高精度口型同步。
- 真实 AI、云端 TTS、ASR、账号、历史记录、多宠物或多皮肤。

已实现过的 `LipSyncTimeline`、7 类嘴型和旧调试相关代码已从当前 production 路径移除或收敛为简单模型；当前 Sprint 不基于它们扩展，也不把它们作为 UI 验收入口。

## 实施顺序

1. 固定当前 MVP 默认 UI：小狗舞台、文本输入、主 CTA、示例入口、轻量状态摘要。
2. 打通讲话状态流：`idle -> preparing/thinking -> speaking -> done -> idle`，错误可回到 `idle`。
3. 实现最小嘴巴规则：`speaking` 张口或轻微开合，其他状态 `closed`。
4. 加入俏皮动作和完成反馈，减少动态时降低装饰动作但保留讲话开口。
5. 补齐测试边界：空文本、忙碌重复提交、开闭口规则、默认 UI 不出现 deferred 功能。

## Epic 概览

### Epic 1：Redmi Pad SE 宠物舞台基础

用户打开 Android 平板 Demo 后，能看到闭口待机、状态可感知、横竖屏可用的小狗舞台。

状态：已完成/保留。

当前 MVP 依赖：

- Android 原生 `DigitalDog` 工程、Kotlin、Jetpack Compose、基础测试。
- Redmi Pad SE 横竖屏布局基础。
- `idle`、`thinking/preparing`、`speaking`、`done`、`error` 等状态表达。
- 小狗基础嘴巴渲染、项圈状态光、待机生命感。
- 基础可访问性、减少动态和布局稳定性。

不再新增：

- 为当前 MVP 扩展 7 类嘴型验收。
- 为当前 MVP 增加上传、麦克风、波形或调试台布局。

### Epic 2：简单讲话动画与俏皮反馈

用户可以通过文本主入口或示例入口触发小狗讲话动画，看到准备、讲话、完成、回待机的完整循环。讲话时小狗张口或轻微开合，不要求与文本、音素或声音节奏同步。

状态：done，当前 MVP 已完成。

### Epic 3：Deferred - 口型同步调试与延迟校准

移出当前 MVP。只有后续重新确认要做“精确口型同步 Demo”时，才恢复调试快照、波形、嘴型时间轴、文本高亮和延迟校准。

状态：deferred。

### Epic 4：Deferred - 实验音频输入

移出当前 MVP。只有后续重新确认实验音频入口时，才恢复上传音频、麦克风录音、音频解码、音频分析和对应错误恢复。

状态：deferred。

## Story 明细

## Epic 1：Redmi Pad SE 宠物舞台基础

### Story 1.1：运行 Redmi Pad SE 原生 Android Demo 外壳

状态：done，保留。

As a 展示负责人,
I want 在 Android 平板上运行一个原生 Demo 外壳,
So that 后续宠物舞台和讲话动画可以在目标设备上验证。

详细说明：

- 工程使用 Android 原生技术栈，目标是 Redmi Pad SE 11 英寸 MIUI / Android 平板。
- Kotlin、Jetpack Compose、Gradle Wrapper 和基础测试作为第一版开发基线。
- 当前 MVP 不需要网页壳、iPadOS、SwiftUI 或任何真实 AI 服务。

验收：

- Given 开发者拉取工程，When 执行 Gradle 构建，Then `DigitalDog` app module 可以编译。
- Given 使用 Redmi Pad SE 或等价平板模拟视口，When 启动 App，Then 首屏显示 Demo 外壳而不是空白页面。
- Given 工程初始化完成，When 运行基础单元测试，Then 至少有可运行测试目标。
- Given 后续 Story 需要扩展 UI，When 新增 Compose 组件，Then 不需要重建项目结构。

### Story 1.2：首屏宠物舞台与 Redmi Pad SE 横竖屏布局

状态：done，保留。

As a 评审者,
I want 首屏直接看到小狗舞台和核心入口,
So that 我能在横屏和竖屏下快速理解 Demo 的主体验。

详细说明：

- 横屏优先展示宠物舞台、输入区和轻量状态摘要。
- 竖屏优先保证小狗舞台、文本输入和主 CTA 可见。
- 当前 MVP 不把调试台、波形、时间轴或实验入口作为首屏必需内容。

验收：

- Given App 首次启动，When 进入默认页面，Then 小狗舞台是视觉中心。
- Given 横屏或接近 1920 x 1200 的平板视口，When 页面布局完成，Then 小狗舞台、文本输入、主 CTA 和示例入口不重叠。
- Given 竖屏或接近 1200 x 1920 的平板视口，When 页面布局完成，Then 核心控件可达且标签不溢出。
- Given 动态状态文本发生变化，When 页面重新渲染，Then 主布局不跳动。

### Story 1.3：宠物状态机与项圈状态反馈

状态：done，保留。

As a 使用者,
I want 小狗用状态、项圈光和动作反馈表达当前进度,
So that 我知道它是在待机、准备、讲话、完成还是错误。

详细说明：

- 状态语义保留为 `idle`、`thinking/preparing`、`speaking`、`done`、`error`。
- `listening` 可作为未来 AI 或录音状态的预留，但不属于当前 MVP 默认入口。
- 状态反馈必须能通过文字或语义读取，不能只靠颜色。

验收：

- Given 小狗无活动 session，When 页面稳定，Then 宠物状态为 `idle`，嘴巴为 `closed`。
- Given 用户提交有效文本，When 讲话流程开始，Then 状态经过准备或思考后进入 `speaking`。
- Given 讲话结束，When 完成反馈结束，Then 状态回到 `idle`。
- Given 出现空文本或可恢复错误，When UI 展示错误，Then 小狗进入 `error` 且嘴巴保持 `closed`。
- Given TalkBack 或语义测试读取状态，When 状态变化，Then 当前状态有文字表达。

### Story 1.4：7 类嘴型渲染与手动测试

状态：done，历史完成；当前 MVP 已降级。

As a 开发者,
I want 明确历史 7 类嘴型探索不再驱动当前 MVP,
So that 后续实现不会继续沿精确口型同步方向扩展。

详细说明：

- 该 Story 曾用于验证 `closed`、`small`、`wide`、`round`、`smile`、`teeth`、`pant` 等视觉探索。
- 2026-06-25 范围重置后，当前 production 路径只保留 `closed` 和 `open/talking` 的简单模型。
- 历史嘴型探索只作为未来可能恢复精确口型时的参考，不作为当前 UI 验收入口。

验收：

- Given 当前 MVP 运行，When 小狗不讲话，Then 嘴巴为 `closed`。
- Given 当前 MVP 进入 `speaking`，When 小狗讲话，Then 嘴巴显示 `open/talking` 或简单开合。
- Given 后续 Story 开发，When 需要新增嘴型，Then 必须先有新的 scope change，而不是直接恢复 7 类嘴型。
- Given 默认 UI 渲染，When 用户查看页面，Then 不出现手动嘴型测试面板。

### Story 1.5：宠物基础动作、可访问性与减少动态

状态：done，保留并作为 Story 2.4 的基础。

As a 使用者,
I want 小狗在待机和状态变化时有轻微生命感,
So that 它看起来像一只可爱的小宠物。

详细说明：

- 待机生命感可以包括轻微呼吸、眨眼、耳朵或尾巴的低频动作。
- 减少动态开启时，需要降低装饰动作，但保留必要的讲话开口反馈。
- 可访问性优先表达当前状态、嘴巴状态、错误和输入来源。

验收：

- Given 小狗处于 `idle`，When 页面停留，Then 可以有低频呼吸或眨眼，但嘴巴保持 `closed`。
- Given 用户系统要求减少动态，When UI 渲染，Then 耳朵、头部、身体和尾巴等装饰动作减少。
- Given 小狗进入错误，When 显示错误反馈，Then 动作不遮挡嘴巴和错误文字。
- Given TalkBack 读取页面，When 焦点经过舞台或摘要，Then 当前状态可被读出。

## Epic 2：简单讲话动画与俏皮反馈

### Story 2.1：TTS 主入口提交与空文本处理

状态：done，保留并按当前 MVP 解释。

As a 使用者,
I want 输入一句话并点击“让狗狗说话”,
So that 我可以稳定触发小狗讲话动画。

详细说明：

- 文本主入口只负责创建一次讲话 session，不要求生成真实 TTS 音频或嘴型时间轴。
- 空文本必须是可恢复错误，不得触发 `speaking`。
- 忙碌状态下重复提交不得创建第二个并行 session。

验收：

- Given 输入框有非空文本，When 用户点击“让狗狗说话”，Then 系统创建一次讲话流程。
- Given 讲话流程刚创建，When 小狗处于准备阶段，Then 嘴巴保持 `closed`。
- Given 输入框为空或只有空白字符，When 用户点击主 CTA，Then 不创建讲话流程，并显示“先输入一句话”。
- Given 空文本错误出现，When UI 更新，Then 小狗进入可恢复 `error` 且嘴巴为 `closed`。
- Given 当前已有讲话流程，When 用户再次点击主 CTA，Then CTA 禁用或被保护，不创建第二个冲突流程。

### Story 2.2：TTS 文本到嘴型时间轴生成（历史完成，当前已移除 production 路径）

状态：done，superseded by 2026-06-25 scope change。

As a 开发者,
I want 明确历史 TTS 时间轴能力已不属于当前 production 路径,
So that 后续当前 MVP 不继续扩展精确同步方向。

详细说明：

- 旧 Story 2.2 的文本 token、停顿、`LipSyncTimeline` 和质量标记已不再是当前 MVP 的生产路径。
- 当前代码清理目标是避免 `LipSyncTimeline`、`TtsTimelineGenerator`、`TimelineQuality` 继续成为默认业务依赖。
- 该 Story 只保留历史追踪意义，不能作为继续开发同步调试功能的依据。

验收：

- Given 当前 production 代码，When 搜索核心模型，Then 不保留 `LipSyncTimeline`、`TtsTimelineGenerator`、`TimelineQuality` 的默认路径依赖。
- Given 后续 Story 2.3 到 2.6 开发，When 需要控制嘴巴，Then 只能基于讲话状态和开闭口规则。
- Given 当前 Sprint 验收，When 评审小狗讲话，Then 不要求文本、音素、音频节奏或时间轴对齐。
- Given 未来重新启用精确口型同步，When 团队决定恢复，Then 必须通过新的 scope change 重新拆 Story。

### Story 2.3：讲话状态驱动开口与闭口规则

状态：done。

As a 评审者,
I want 点击“让狗狗说话”后看到小狗张口讲话，不讲话时闭口,
So that 我能快速判断这是一只会回应的小狗。

详细说明：

- 讲话与否是嘴巴状态的唯一当前 MVP 判定依据。
- 准备、思考、完成、错误和待机都必须闭口。
- `speaking` 可以是固定开口，也可以是轻微开合循环；不要求匹配文字或音频。

验收：

- Given 小狗处于 `idle`，When 页面稳定，Then 嘴巴为 `closed`。
- Given 小狗处于准备或思考，When 讲话流程尚未进入 `speaking`，Then 嘴巴为 `closed`。
- Given 小狗处于 `speaking`，When 舞台渲染，Then 嘴巴为 `open/talking` 或可观察的轻微开合。
- Given 小狗处于 `done` 或 `error`，When 舞台渲染，Then 嘴巴为 `closed`。
- Given 讲话结束事件发生，When 状态回到 `idle`，Then 嘴巴回到 `closed`。
- Given 运行范围边界测试，When 检查默认代码路径，Then 不依赖文本高亮、波形、嘴型时间轴或延迟校准。

### Story 2.4：俏皮动作编排与讲话结束反馈

状态：backlog。

As a 评审者,
I want 小狗在准备、讲话和结束时有俏皮但克制的动作,
So that 它像有生命的小宠物，而不是静态开合嘴。

详细说明：

- 准备阶段应让用户感到小狗正在回应，例如短暂看向用户、耳朵竖起或轻微歪头。
- 讲话中动作要服务于“可爱讲话”，不能抢过嘴巴区域。
- 完成反馈短而明确，随后回到待机。
- 减少动态模式下降低装饰动作，但讲话开口仍保留。

验收：

- Given 用户提交有效文本或点击示例，When 小狗进入准备阶段，Then 出现一次短暂准备动作且嘴巴仍为 `closed`。
- Given 小狗进入 `speaking`，When 讲话动画进行，Then 至少有一种低幅度俏皮动作，例如轻微点头、耳朵轻弹、尾巴小幅摆动或身体律动。
- Given 讲话动作播放，When 舞台渲染，Then 动作不得遮挡嘴巴区域，也不得造成布局跳动。
- Given 讲话结束，When 小狗进入完成反馈，Then 出现一次短反馈，例如眨眼、摇尾或开心表情。
- Given 完成反馈结束，When 状态恢复，Then 小狗回到 `idle` 且嘴巴为 `closed`。
- Given 用户开启减少动态，When 同样流程运行，Then 装饰动作降级或静态化，但讲话开口保留。

### Story 2.5：简化演示入口与 UI 清理

状态：backlog。

As a 使用者,
I want 首屏只保留小狗舞台、文本输入、让狗狗说话和示例入口,
So that 我能直接试小狗讲话动画，不被调试项干扰。

详细说明：

- 当前 MVP 默认 UI 应像一个简单可试的宠物讲话 Demo，而不是调试工具。
- 状态摘要保留必要信息：当前状态、嘴巴状态、输入来源。
- 上传、麦克风、波形、嘴型时间轴、文本高亮、延迟校准都不出现在默认首屏。

验收：

- Given App 默认启动，When 首屏渲染，Then 可见宠物舞台、TTS 输入、主 CTA 和示例入口。
- Given 用户未展开任何开发入口，When 检查默认 UI，Then 不展示波形、嘴型时间轴、文本高亮或延迟校准。
- Given 当前 MVP 范围，When 检查默认 UI，Then 上传音频和麦克风入口被移除，或隐藏到非默认开发入口。
- Given 状态摘要渲染，When 小狗状态变化，Then 摘要最多显示当前状态、嘴巴状态和输入来源等轻量信息。
- Given 横屏和竖屏视口，When 核心控件渲染，Then 按钮和标签不重叠、不溢出。
- Given TalkBack 读取默认页面，When 焦点经过主流程控件，Then 名称和值清晰可读。

### Story 2.6：简化讲话动画测试覆盖

状态：backlog。

As a 开发者,
I want 用测试覆盖讲话状态、开闭口规则和范围边界,
So that 后续 UI 调整不会破坏最小讲话体验。

详细说明：

- 测试优先覆盖当前 MVP 的真实业务边界，而不是已 deferred 的同步能力。
- 单元测试覆盖状态流、空文本、忙碌保护和开闭口规则。
- UI 或语义测试覆盖主 CTA、示例入口、状态摘要和默认 UI 不出现 deferred 功能。

验收：

- Given 运行逻辑测试，When 非空文本提交，Then 创建一次讲话流程并能进入 `speaking`。
- Given 运行逻辑测试，When 空文本提交，Then 状态进入可恢复 `error` 且嘴巴为 `closed`。
- Given 运行逻辑测试，When 忙碌状态重复提交，Then 不创建第二个讲话 session。
- Given 运行开闭口规则测试，When 状态为 `speaking`，Then 嘴巴为 `open/talking`。
- Given 运行开闭口规则测试，When 状态不是 `speaking`，Then 嘴巴为 `closed`。
- Given 运行 UI 或语义测试，When 点击主 CTA 或示例入口，Then 舞台进入讲话/张口状态。
- Given 运行边界测试或手动验收，When 检查默认 UI，Then 不新增波形、时间轴、文本高亮、延迟校准、上传、麦克风或新音频分析依赖。

## Epic 3：Deferred - 口型同步调试与延迟校准

说明：以下 Story 是历史规划，当前状态均为 deferred。它们只记录未来如果重新走“精确口型同步 Demo”时需要恢复的能力，不属于当前 Sprint 验收。

### Story 3.1：调试快照与当前同步状态展示

状态：deferred。

As a 开发者,
I want 调试台通过统一 `DebugSnapshot` 展示当前同步状态,
So that 我能确认当前嘴型、宠物状态、输入来源和解析质量是否一致。

Deferred 验收：

- Given 未来重新启用同步调试，When 生成调试快照，Then 快照包含宠物状态、当前嘴型、输入来源、解析质量、播放时间、总时长和延迟偏移。
- Given 调试台渲染，When 播放进行中，Then 调试台只读取快照，不反向驱动宠物表现或播放控制。
- Given 当前无播放 session，When 打开调试台，Then 显示空状态或待机摘要，嘴型为 `closed`。

### Story 3.2：波形进度展示

状态：deferred。

As a 开发者,
I want 在调试台看到音频波形或播放进度,
So that 我能把声音节奏和嘴型变化对应起来。

Deferred 验收：

- Given 未来 session 包含音频时长或模拟时长，When 播放开始，Then 波形或进度组件显示当前位置和总时长。
- Given 有真实音频分析数据，When 调试台渲染波形，Then 能显示基础能量变化或静音片段。
- Given 没有真实波形数据，When 调试台需要展示进度，Then 可使用模拟波形或进度条，并保持组件高度稳定。
- Given 竖屏调试台折叠，When 播放进行中，Then 摘要保留当前状态、嘴型、来源和进度信息。

### Story 3.3：嘴型时间轴可视化

状态：deferred。

As a 开发者,
I want 在调试台看到嘴型时间轴和当前片段,
So that 我能判断当前嘴型来自哪个时间段。

Deferred 验收：

- Given 未来 session 包含 `LipSyncTimeline`，When 调试台渲染时间轴，Then 每个片段显示嘴型短名并可识别当前片段。
- Given 播放指针进入新的 segment，When 调试台更新，Then 当前片段高亮同步移动。
- Given 当前片段质量较低或来源为实验路径，When 时间轴展示该片段，Then 显示质量标记或实验提示。
- Given 屏幕较窄，When 时间轴标签较多，Then 时间轴可折叠、压缩或横向滚动，标签不重叠。

### Story 3.4：TTS 文本高亮调试视图

状态：deferred。

As a 评审者,
I want 在调试台看到当前文本片段高亮,
So that 我能理解文字、声音和嘴型之间的节奏关系。

Deferred 验收：

- Given 未来播放来源为 TTS 且 segment 包含 `textRange`，When 播放指针进入该 segment，Then 对应文本片段高亮。
- Given 当前播放来源为示例且包含预设文本，When 示例播放中，Then 可显示示例文本进度，来源仍显示为 `sample`。
- Given 当前播放来源为上传或麦克风，When 调试台渲染文本区域，Then 显示无文本时间轴或空状态，不显示错误 TTS 文本。
- Given 用户不能只依赖颜色，When 文本片段高亮，Then 同时使用背景、下划线、边框或其他非颜色提示。

### Story 3.5：延迟校准控件

状态：deferred。

As a 开发者,
I want 调整嘴型相对音频的延迟偏移,
So that 我可以快速判断口型偏早或偏晚并校准演示效果。

Deferred 验收：

- Given 未来调试台处于可校准状态，When 用户调整延迟滑杆，Then `latencyOffsetMs` 在 `-150ms` 到 `+150ms` 范围内变化。
- Given 延迟偏移发生变化，When 系统计算当前嘴型或时间轴指针，Then 使用 `effectiveTimeMs = audioCurrentTimeMs + latencyOffsetMs`。
- Given 用户点击重置，When 重置完成，Then `latencyOffsetMs` 回到 `0ms`。
- Given 用户使用外接键盘或 TalkBack，When 焦点到达延迟控件，Then 控件可调整且可读出当前毫秒值。

### Story 3.6：调试台横竖屏与可访问性验收

状态：deferred。

As a 开发者,
I want 调试台在 Redmi Pad SE 横屏和竖屏都可用且不压过宠物舞台,
So that 评审者看效果、开发者看依据时不会互相干扰。

Deferred 验收：

- Given 横屏平板视口，When 调试台展示完整模式，Then 当前嘴型、来源、质量、波形、时间轴、文本高亮和延迟校准均可见或可达。
- Given 竖屏平板视口，When 调试台默认折叠，Then 摘要显示当前状态、当前嘴型、输入来源和解析质量。
- Given 用户开启 TalkBack，When 焦点进入调试台，Then 当前嘴型、来源、质量、播放时间和错误状态可被读出。
- Given 播放正在进行，When 调试台持续更新，Then 波形、时间轴、标签和数值区域尺寸稳定。

## Epic 4：Deferred - 实验音频输入

说明：以下 Story 是历史规划，当前状态均为 deferred。它们只记录未来如果重新启用上传和麦克风实验入口时需要恢复的能力，不属于当前 Sprint 验收。

### Story 4.1：上传音频文件选择与播放入口

状态：deferred。

As a 内部测试用户,
I want 上传本地音频并播放,
So that 我可以验证非 TTS 音频也能驱动数字狗基础嘴型。

Deferred 验收：

- Given 未来 App 恢复实验上传入口，When 用户点击“上传音频”，Then 系统打开 Android 系统文件选择器。
- Given 用户选择合法音频文件，When 系统完成读取，Then 创建上传音频 session，并显示输入来源为上传音频。
- Given 当前已有播放 session，When 用户尝试上传新音频，Then 上传入口被禁用或要求先停止当前播放。

### Story 4.2：上传音频解码、分析与基础嘴型时间轴

状态：deferred。

As a 开发者,
I want 对上传音频做基础音频分析并生成嘴型时间轴,
So that 上传路径能复用同一套嘴型播放与调试能力。

Deferred 验收：

- Given 上传音频可被 Android Media API 解码，When 系统进行音频分析，Then 音频被转换为可分析 PCM 或等价数据。
- Given 分析开始，When 系统按固定窗口计算帧特征，Then 每帧至少包含时间、能量或静音判断。
- Given 分析帧已生成，When 生成上传音频时间轴，Then 静音段映射为 `closed`，有声段映射为合法嘴型。
- Given 上传音频噪声明显或音量过低，When 系统评估质量，Then 质量显示为 `experimental` 或 `failed`，不显示为 `stable`。

### Story 4.3：麦克风权限、录制与回放入口

状态：deferred。

As a 内部测试用户,
I want 用 Redmi Pad SE 麦克风录制短音频并回放,
So that 我可以验证真实录音也能驱动数字狗基础嘴型。

Deferred 验收：

- Given 未来恢复麦克风入口，When 用户点击“开始录音”且权限未授权，Then 系统请求 Android 麦克风运行时权限。
- Given 用户授权麦克风，When 用户点击“开始录音”，Then 系统开始录制短音频，并显示当前输入来源为麦克风。
- Given 用户点击“停止录音”，When 录音有效，Then 系统创建麦克风 session 并可回放。
- Given 用户拒绝权限或录音无效，When 系统处理结果，Then 进入可恢复错误，TTS 和示例路径仍可使用。

### Story 4.4：麦克风录音分析复用上传分析流程

状态：deferred。

As a 开发者,
I want 麦克风录音复用上传音频的分析和时间轴生成能力,
So that 系统不会产生第二套音频口型逻辑。

Deferred 验收：

- Given 麦克风录音文件或 buffer 可用于分析，When 系统处理录音，Then 使用与上传音频相同或等价的分析流程。
- Given 麦克风录音分析完成，When 系统评估质量，Then 默认质量为 `experimental`，不显示为 `stable`。
- Given 麦克风回放进行中，When 播放时间推进，Then 当前嘴型由未来恢复的时间轴和播放时钟计算。
- Given 第一版实验范围限制，When 用户录音，Then 采用录制后回放分析，不实现实时流式麦克风口型。

### Story 4.5：可恢复错误与稳定路径保护

状态：deferred。

As a 评审者,
I want 上传、录音或输入失败时 Demo 仍然可继续演示,
So that 失败不会破坏整体展示。

Deferred 验收：

- Given 用户提交空文本，When 系统检测到无有效输入，Then 小狗进入 `error`，嘴巴为 `closed`，且用户可以继续使用 TTS 或示例。
- Given 用户拒绝麦克风权限，When 权限结果返回拒绝，Then UI 显示“麦克风权限未开启”，稳定入口仍可点击。
- Given 上传音频不可解码或解析失败，When 系统捕获失败，Then UI 显示可恢复错误，不影响下一次文本或示例播放。
- Given 播放失败或 session 中断，When 系统进入错误恢复，Then 停止当前 session，并允许回到 `idle`。

### Story 4.6：实验入口质量标识与验收测试

状态：deferred。

As a 开发者,
I want 用清晰的质量标识和测试覆盖实验入口,
So that 上传和麦克风能力可用但不会误导评审者。

Deferred 验收：

- Given 当前 session 来源为上传音频，When 调试台展示解析质量，Then 质量只能是 `medium`、`experimental` 或 `failed`。
- Given 当前 session 来源为麦克风，When 调试台展示解析质量，Then 质量默认显示为 `experimental`。
- Given 运行实验入口逻辑测试，When 测试上传和麦克风时间轴生成，Then 验证静音段、合法嘴型、无负时长和无重叠片段。
- Given 运行错误恢复测试或手动验收，When 模拟权限拒绝、解析失败、播放失败，Then 系统进入可恢复错误且稳定路径仍能继续使用。

## 范围映射

当前保留：

- FR2、FR3、FR4、FR5、FR6、FR17、FR18 的简化版本。
- NFR 中与 Android 原生、可访问性、触控目标、对比度、布局稳定、减少动态、本地处理和不接真实 AI 相关的约束。
- Epic 1 已完成能力作为当前 MVP 的舞台、状态和可访问性基础。
- Epic 2 的 2.1 到 2.6 已作为当前 Sprint 的实施主线完成。

当前冻结或移出：

- FR7、FR8、FR10、FR11、FR12、FR13、FR14、FR15、FR16。
- FR9 仅保留为历史探索，不作为当前 MVP 产品验收目标。
- 旧架构中强制所有入口统一输出 `LipSyncTimeline`、调试台只读 `DebugSnapshot`、上传/麦克风复用音频分析等要求，全部归入 Deferred Story。

## 与 Sprint Status 对齐

- Epic 1：`done`。
- Epic 2：`done`，其中 2.1、2.2、2.3、2.4、2.5、2.6 均已完成。
- Epic 3：`deferred`，所有 Story 保留为未来可选范围。
- Epic 4：`deferred`，所有 Story 保留为未来可选范围。
