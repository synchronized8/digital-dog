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
project_name: '数字狗语音口型 Demo'
user_name: 'kejincheng'
date: '2026-06-24'
extractionStatus: 'confirmed'
---

# 数字狗语音口型 Demo - Epic 与 Story 拆分

## 2026-06-24 平台纠偏

第一版展示设备更正为 **小米 Redmi Pad SE 11 英寸 MIUI / Android 平板**。本文中旧的 iPadOS、iPad、Xcode、SwiftUI、AVFoundation、VoiceOver、Reduce Motion 表述均按 Android 原生平板方案覆盖：Kotlin + Jetpack Compose、Android TTS/Media/录音能力、系统文件选择器、TalkBack、48dp 触控目标，以及 1920 x 1200 / 16:10 横屏优先验收。

## 概览

本文档将把 PRD、UX 设计规格、架构决策、MVP/UI/LIPSYNC 专项规格中的需求拆解为可实施的 Epic 和 Story。当前已完成需求提取、FR 覆盖图、Epic 列表和 Story 草案，下一阶段将进行最终一致性验证。

## 需求清单

### 功能需求

FR1：用户启动 Redmi Pad SE / MIUI 展示端后，可以在首屏看到数字狗、TTS 主输入入口、示例音频入口、上传音频入口、麦克风录音入口和调试台入口；横屏展示完整舞台与调试台，竖屏优先展示数字狗和主入口，调试台可折叠。

FR2：数字狗在页面加载、未播放、讲话结束和错误恢复后必须处于 `idle`，嘴型必须为 `closed`，并保持轻微呼吸或低频眨眼等待机生命感。

FR3：系统必须支持 `idle`、`listening`、`thinking`、`speaking`、`done`、`error` 宠物状态，并通过数字狗动作、项圈状态光和调试台状态表达状态变化。

FR4：数字狗讲话行为必须形成“讲话前准备、讲话中同步、讲话后反馈、回到待机”的完整循环；讲话前耳朵竖起、眼睛看向用户且嘴型保持闭口，讲话中嘴型优先并伴随克制动作，讲话后闭口、眨眼或轻摇尾。

FR5：用户必须能输入非空文本并点击“让狗狗说话”触发 TTS 或模拟 TTS 讲话流程；空文本必须进入可恢复 `error`，不触发讲话。

FR6：用户必须能不输入文本、不授权麦克风，点击“播放示例”一键触发稳定演示；示例路径必须显示来源和 `stable` 解析质量。

FR7：用户必须能通过 Android 系统文件选择器上传本地音频；合法音频可播放并通过基础分析驱动嘴型，调试台显示输入来源和解析质量。

FR8：用户必须能通过 Redmi Pad SE 麦克风录制短音频，录制后回放并驱动基础嘴型；拒绝麦克风权限时进入 `error`，且 TTS 与示例路径仍可使用。

FR9：系统必须支持 7 类嘴型视觉状态：`closed`、`small`、`wide`、`round`、`smile`、`teeth`、`pant`；每种嘴型可被手动测试或播放流程触发，并具有明确视觉差异。

FR10：系统必须能根据 TTS 文本生成 `LipSyncTimeline`，并在播放时按时间轴驱动当前嘴型；TTS 演示至少可观察到 4 类以上嘴型，同时系统支持全部 7 类嘴型。

FR11：TTS 播放过程中必须显示与嘴型时间轴同步的文本高亮；标点、停顿和多段嘴型不得造成高亮错乱。

FR12：上传音频和麦克风录音必须能通过基础音频分析生成嘴型时间轴；静音段生成 `closed`，有声段可映射到 `small`、`wide`、`round`、`smile`、`teeth` 或降级为稳定嘴型。

FR13：系统必须执行口型平滑与闭口规则：相邻相同嘴型合并，短片段平滑处理，普通播放中嘴型切换不超过 5 次/秒，静音、待机、思考和错误状态下保持 `closed`。

FR14：调试台必须显示当前嘴型、当前宠物状态、输入来源和解析质量；输入来源至少包含 TTS、示例音频、上传音频、麦克风，解析质量至少包含稳定、一般、实验。

FR15：调试台必须显示音频波形或播放进度和嘴型时间轴；播放中当前片段高亮，时间轴片段显示嘴型短名。

FR16：调试台必须提供延迟校准控件，支持 `-150ms` 到 `+150ms` 范围、`25ms` 步进、默认 `0ms`，并显示当前偏移值。

FR17：系统必须对空文本、麦克风权限拒绝、上传不可解码、音频解析失败、播放失败等错误给出可见且可恢复反馈；错误时数字狗闭口、进入 `error`，且其他稳定入口仍可使用。

FR18：Demo 必须在 Redmi Pad SE 横屏和竖屏核心流程可用；验收基准包括 1920 x 1200 / 16:10 横屏视口、1200 x 1920 竖屏视口，以及 Android 系统缩放后的等价逻辑视口，核心控件不得重叠，按钮和标签不得溢出。

FR19：系统必须预留未来真实 AI 语音对话状态，不接真实 AI；`listening`、`thinking`、`speaking`、`done`、`error` 可由 mock 事件触发，未来接入 AI 时不需要重命名状态机。

### 非功能需求

NFR1：第一版必须是 Android 原生平板展示端，不是网页 UI；技术方向为 Android Studio 原生项目、Kotlin、Jetpack Compose、Android TTS/Media/AudioRecord 或 MediaRecorder。

NFR2：主体验必须是软萌宠物，调试台只能作为辅助；调试信息不得遮挡数字狗嘴巴区域，讲话时辅助动作不得干扰口型观察。

NFR3：TTS 和内置示例必须作为稳定演示路径；上传和麦克风是实验入口，失败不得阻塞 TTS 与示例。

NFR4：上传音频和麦克风录音不得被包装成高精度音素同步；必须明确显示 `medium` 或 `experimental` 解析质量。

NFR5：所有按钮、文本输入、上传、录音、调试折叠控件和延迟滑杆必须支持触控、外接键盘和 TalkBack。

NFR6：状态颜色必须配合文字、标签、图标、位置或边框表达；调试图表不能只依赖颜色。

NFR7：正文对比度至少满足 4.5:1；按钮文本、状态标签、错误提示和调试摘要在浅色背景上必须清晰可读。

NFR8：触控目标不小于 `48dp`；Redmi Pad SE 竖屏紧凑布局下按钮文字、时间轴短名和调试摘要不得裁切或重叠。

NFR9：播放中波形、时间轴、按钮状态、标签和调试数值不得造成主布局跳动。

NFR10：口型切换、宠物状态变化和调试台更新不得出现明显卡顿；播放时钟、时间轴和嘴型计算必须保持一致。

NFR11：音频和麦克风数据默认本地处理，不上传服务器；麦克风录音默认只用于当前回放分析，不长期保存。

NFR12：第一版不接入真实 AI、云端 TTS、ASR、强制对齐、账号、历史记录、云存储、多宠物或多皮肤。

NFR13：Android 系统动画缩放关闭、降低或用户要求减少动态时，必须降低耳朵、头部、呼吸、尾巴等装饰动效，但保留必要嘴型切换。

NFR14：UI 不做营销落地页，不使用长段说明文案，不使用大面积单一色相、装饰性渐变球或过度工具化控制台视觉。

### 架构附加需求

- Epic 1 Story 1 必须创建 Android 原生项目 `DigitalDog`，使用 Kotlin、Jetpack Compose、Gradle，并启用基础测试；目标设备以 Redmi Pad SE 11 英寸 MIUI / Android 平板为主。
- 工具链基线为当前稳定 Android Studio、Android Gradle Plugin、Kotlin 与项目 Gradle Wrapper；具体版本在实施 Story 1.1 时以本机可构建环境记录。
- MVP 不引入数据库；核心数据使用 Kotlin `data class` / `enum class`，并保持可序列化和可比较。
- 本地持久化只用于低风险偏好项，例如延迟校准值和调试台展开状态，可使用 DataStore / SharedPreferences。
- 核心模型必须包括 `PetState`、`MouthShape`、`InputSource`、`TimelineQuality`、`LipSyncSegment`、`LipSyncTimeline`、`SpeechSession`、`DebugSnapshot`、`DigitalDogError`。
- 应使用单一主状态源 `SpeechDemoStore` 管理宠物状态、播放状态、当前嘴型、时间轴和调试信息。
- Compose UI 只能发送用户意图，例如 `submitText()`、`playSample()`、`startRecording()`、`stopPlayback()`，不得直接操作 Android 音频底层或直接修改嘴型。
- 所有音频入口必须统一输出 `LipSyncTimeline`，禁止 TTS、示例、上传、麦克风各自维护私有时间轴结构。
- 当前嘴型必须通过播放时间、`LipSyncTimeline` 和 `latencyOffsetMs` 计算，不允许绕过时间轴直接驱动嘴型。
- 调试台必须只读取 `DebugSnapshot`，不得反向驱动宠物表现、播放控制或音频分析。
- 代码应按模块组织：`app`、`designsystem`、`sharedmodel`、`state`、`petstage`、`dogrenderer`、`speechinput`、`lipsync`、`audioinput`、`playback`、`debugpanel`、`accessibility`、`resources`、`tests`。
- `SpeechInputProvider`、`TimelineGenerator`、`AudioPlaybackController`、`PetStateReducer`、`DebugSnapshotProvider` 等 interface 边界必须用于隔离输入、时间轴、播放、状态和调试。
- 音频底层能力集中在 `AudioInput` 和 `Playback`；TTS 使用 Android `TextToSpeech` 或预设 TTS 时间轴，上传和麦克风复用 Android 音频分析流程。
- 内置示例文本和时间轴放入 Android assets 或 raw resources 的 `samples`；宠物视觉资产放入 `pet_assets`；预览数据放入 `preview_data`。
- 纯逻辑测试必须覆盖状态机、`LipSyncTimeline` 生成、嘴型映射、延迟偏移、错误恢复和时间轴平滑。
- UI 和能力验证必须覆盖 Redmi Pad SE 横竖屏、麦克风权限授权/拒绝、音频播放、上传失败、TalkBack 和减少动态策略。
- 未来真实 AI API 只能通过独立 adapter 接入，输出本地 `SpeechSession`、`PetState` 和 `LipSyncTimeline`，不得直接驱动 Compose UI。

### UX 设计需求

UX-DR1：实现轻量自定义设计系统，优先使用 Jetpack Compose 原生组件、Material Icons 或 Compose Icons、项目自定义 tokens 和项目专属宠物/调试组件，不套用重型后台组件库。

UX-DR2：实现色彩 tokens：页面背景 `#F7FAF7`、主表面 `#FFFFFF`、次表面 `#EEF6F3`、主文字 `#24302F`、次文字 `#66736F`、宠物暖色 `#F2B6A0`、科技蓝 `#4C8DFF`、强调珊瑚 `#FF6F61`、成功绿 `#37A86B`、提示黄 `#E7A93F`、边框 `#DDE7E3`。

UX-DR3：实现字体 tokens：Android 系统字体、`Roboto`、`Noto Sans CJK`，必要时适配 MIUI 实机字体；页面标题、区块标题、面板标题、正文、标签、按钮、调试数值使用固定字号，不随视口宽度线性缩放，字间距为 `0`。

UX-DR4：实现间距 tokens：`xs=4px`、`sm=8px`、`md=16px`、`lg=24px`、`xl=32px`、`2xl=48px`，并用于按钮组、标签组、面板、主区域和双栏间距。

UX-DR5：实现 Redmi Pad SE 横屏布局：顶部状态栏 + 宠物舞台 / 调试台双栏 + 下方输入区；宠物舞台约 60%，调试台约 40%，输入区稳定可达。

UX-DR6：实现 Redmi Pad SE 竖屏布局：顶部状态、宠物舞台、TTS 输入、快速入口、调试摘要、展开调试详情；调试台默认折叠但必须显示当前状态、当前嘴型、输入来源和解析质量。

UX-DR7：实现顶部状态栏，显示 demo 名称、当前宠物状态、当前输入来源和状态点；状态点颜色必须配合文字。

UX-DR8：实现宠物舞台组件，包含数字狗角色、轻科技项圈、状态光、轻微地面或窝垫、当前状态摘要；舞台内不得放长文案，不得遮挡嘴巴区域。

UX-DR9：实现数字狗角色与嘴型渲染器，包含头部、耳朵、眼睛、鼻子、嘴巴、身体、项圈和尾巴；接收当前状态、当前嘴型、音量能量和完成反馈信号。

UX-DR10：实现语音输入中心，包含 TTS 文本框、主 CTA“让狗狗说话”、播放示例、上传音频、麦克风录音和入口说明标签；上传和麦克风必须标记为实验入口。

UX-DR11：实现口型同步调试台，包含当前嘴型、输入来源、解析质量、当前状态、波形、嘴型时间轴、文本高亮和延迟校准；横屏常显，竖屏折叠。

UX-DR12：实现波形进度组件，显示波形条、播放指针、静音片段、当前时间和总时长；横屏高度约 72pt，竖屏高度约 56pt，播放时高度固定。

UX-DR13：实现嘴型时间轴组件，显示时间片、嘴型标签、当前指针、质量标记和延迟偏移影响；当前片段必须用标签、边框或指针高亮，不能只靠颜色。

UX-DR14：实现 TTS 文本高亮组件，支持短中文句和多行文本；当前字、词或近似音节高亮，标点停顿推荐不高亮，上传和麦克风路径显示无文本时间轴或不显示文本高亮。

UX-DR15：实现解析质量标签，支持 `stable`、`medium`、`experimental`、`failed`，并用中文显示稳定、一般、实验、失败；等级必须有文字说明。

UX-DR16：实现延迟校准控件，包含滑杆、当前毫秒值、重置按钮和偏移方向说明；支持键盘调整，范围 `-150ms` 到 `+150ms`，步进 `25ms`。

UX-DR17：实现按钮层级：TTS 主按钮最高优先级，示例按钮次级，上传、麦克风、调试展开、重置延迟为低权重按钮或图标按钮；播放中主按钮变为停止或禁用。

UX-DR18：实现状态反馈模式：项圈状态光、数字狗动作和短文字共同表达准备、讲话、完成、错误和实验质量；完成反馈短于 1 秒。

UX-DR19：实现错误恢复模式：空文本、上传不可解码、麦克风权限拒绝、音频解析失败、播放失败时，数字狗闭口、疑惑或歪头、项圈提示黄，错误文案靠近对应入口并提供恢复动作。

UX-DR20：实现播放与口型同步模式：所有入口统一走时间轴；`speaking` 中嘴型变化是最高视觉优先级，静音、准备、思考、错误均为 `closed`。

UX-DR21：实现调试披露模式：普通用户无需理解调试项即可完成演示；开发者展开后可查看嘴型、来源、质量、波形、时间轴、文本高亮和延迟校准。

UX-DR22：实现可访问性标签和值：宠物图形可设为装饰，但当前状态必须可被 TalkBack 获取；当前嘴型、输入来源、解析质量、错误原因必须有文字表达。

UX-DR23：实现键盘操作顺序：状态摘要、宠物舞台状态、TTS 输入、主 CTA、示例、实验入口、调试台；主 CTA、上传、录音、示例、滑杆和折叠控件均可聚焦。

UX-DR24：实现减少动态策略：保留嘴型切换，降低耳朵、头部、身体呼吸和尾巴装饰动效。

UX-DR25：实现播放中布局稳定策略：波形、时间轴、标签、按钮、文本高亮和调试摘要使用固定或最小尺寸，动态内容不得挤压宠物舞台。

UX-DR26：实现 UI 文案：`Digital Dog Demo`、`输入一句想让狗狗说的话`、`让狗狗说话`、`讲话中`、`播放示例`、`上传音频`、`开始录音`、`停止录音`、`先输入一句话`、`麦克风权限未开启`、`音频解析失败，请换一段音频`、`同步调试`、`当前嘴型`、`解析质量`、`延迟校准`。

UX-DR27：实现动效契约：嘴型切换 80ms 到 140ms，眨眼 120ms 到 180ms，耳朵竖起 180ms 到 260ms，头部点动 180ms 到 320ms，呼吸 2400ms 到 3600ms 循环，尾巴完成反馈 500ms 到 900ms，错误歪头 300ms 到 450ms。

UX-DR28：实现嘴型视觉契约：`closed` 为柔和闭合嘴线，`small` 为小椭圆或小弧形开口，`wide` 为纵向大开口，`round` 为圆形开口，`smile` 为横向咧嘴，`teeth` 为轻微露齿或咬合，`pant` 为开心喘气嘴型。

### FR 覆盖图

FR1：Epic 1 - Redmi Pad SE 首屏展示宠物舞台、主入口、实验入口和调试入口。

FR2：Epic 1 - 待机、结束和错误恢复后的闭口状态。

FR3：Epic 1 - 宠物状态机与项圈/动作反馈。

FR4：Epic 1 - 宠物讲话行为循环的视觉基础和 mock 状态验证。

FR5：Epic 2 - TTS 主入口与空文本处理。

FR6：Epic 2 - 内置示例音频稳定演示。

FR7：Epic 4 - 上传音频入口。

FR8：Epic 4 - 麦克风录音入口。

FR9：Epic 1 - 7 类嘴型视觉状态与手动验证。

FR10：Epic 2 - TTS 嘴型时间轴生成与播放驱动。

FR11：Epic 2 - TTS 文本高亮。

FR12：Epic 4 - 上传和麦克风基础音频分析与时间轴生成。

FR13：Epic 2 - 口型平滑、闭口规则和播放中稳定性。

FR14：Epic 3 - 当前嘴型、状态、来源、质量展示。

FR15：Epic 3 - 波形和嘴型时间轴展示。

FR16：Epic 3 - 延迟校准。

FR17：Epic 4 - 可恢复错误状态。

FR18：Epic 1 - Redmi Pad SE 横竖屏布局基础。

FR19：Epic 1 - 未来 AI 对话状态预留。

## Epic 列表

### Epic 1：Redmi Pad SE 可互动宠物舞台基础

用户打开 Redmi Pad SE Demo 后，能立即看到一只闭口待机、状态可感知、嘴型可测试、横竖屏可用的数字狗，为后续讲话、调试和实验输入提供稳定舞台。

**FRs covered:** FR1, FR2, FR3, FR4, FR9, FR18, FR19

### Epic 2：稳定文字与示例讲话演示

用户可以通过 TTS 主入口和内置示例稳定触发数字狗讲话，看到准备、讲话、结束、回待机的完整循环，并观察到嘴型时间轴和文本高亮同步推进。

**FRs covered:** FR5, FR6, FR10, FR11, FR13

### Epic 3：口型同步调试与延迟校准

开发者和评审者可以通过调试台解释当前嘴型为什么出现，查看来源、质量、波形、时间轴、当前片段和延迟偏移，从而判断同步是否可信。

**FRs covered:** FR14, FR15, FR16

### Epic 4：实验音频输入与可恢复错误

用户可以上传音频或录制麦克风音频进行基础口型驱动；当权限拒绝、空输入、解析失败或播放失败时，系统给出可恢复错误，并保持 TTS 与示例路径可用。

**FRs covered:** FR7, FR8, FR12, FR17

## Epic 1：Redmi Pad SE 可互动宠物舞台基础

用户打开 Redmi Pad SE Demo 后，能立即看到一只闭口待机、状态可感知、嘴型可测试、横竖屏可用的数字狗，为后续讲话、调试和实验输入提供稳定舞台。

### Story 1.1：运行 Redmi Pad SE 原生 Android Demo 外壳

As a Android 平板客户端开发者,
I want 创建并运行 `DigitalDog` Kotlin / Jetpack Compose Android App 外壳,
So that 后续宠物舞台、嘴型、音频和调试能力都有一致的原生工程基础。

**Acceptance Criteria:**

**Given** 当前项目尚未创建 Android 工程
**When** 开发者创建 `DigitalDog` Android App 项目
**Then** 工程使用 Kotlin、Jetpack Compose、Gradle、启用基础 Tests，并能在 Redmi Pad SE 或 Android 平板模拟器启动
**And** 项目包含 `MainActivity`、Compose 根组件、基础目录结构和空白首屏容器
**And** 不引入网页框架、后端服务或数据库

**Given** App 在 Redmi Pad SE 或 Android 平板模拟器启动
**When** 用户进入首屏
**Then** 能看到 Demo 标题和占位宠物舞台
**And** App 不崩溃，不请求不必要权限

### Story 1.2：首屏宠物舞台与 Redmi Pad SE 横竖屏布局

As a 设计评审者,
I want 打开 Redmi Pad SE 后第一屏就看到数字狗舞台和主要入口位置,
So that 我能立刻理解这是一个可互动数字狗 Demo。

**Acceptance Criteria:**

**Given** App 在 Redmi Pad SE 横屏或接近 1920 x 1200 / 16:10 的平板模拟器运行
**When** 用户进入首屏
**Then** 页面展示顶部状态栏、宠物舞台、输入区占位、音频入口占位和调试台占位
**And** 宠物舞台是视觉中心，调试台不遮挡嘴巴区域

**Given** App 在 Redmi Pad SE 竖屏或接近 1200 x 1920 的平板模拟器运行
**When** 用户进入首屏
**Then** 宠物舞台优先展示，调试台以摘要或折叠形态出现
**And** 核心控件不重叠，按钮和标签不溢出

**Given** 用户首次打开 App
**When** 观察首屏 3 秒
**Then** 能判断这是数字狗 Demo、后续可让狗狗讲话、并可测试多种音频入口

### Story 1.3：宠物状态机与项圈状态反馈

As a 开发者,
I want 通过统一状态机驱动数字狗状态和项圈反馈,
So that 后续 TTS、示例、上传、麦克风和 AI 状态都能复用同一套宠物行为模型。

**Acceptance Criteria:**

**Given** App 已启动
**When** 系统处于默认状态
**Then** `PetState` 为 `idle`，当前嘴型为 `closed`
**And** 项圈低亮，数字狗保持待机姿态

**Given** 开发测试入口或 mock 事件可触发状态
**When** 依次触发 `listening`、`thinking`、`speaking`、`done`、`error`
**Then** 顶部状态栏、项圈状态光和宠物姿态同步变化
**And** `listening`、`thinking`、`error` 状态下嘴型保持 `closed`

**Given** 状态进入 `done` 或 `error`
**When** 恢复动作结束
**Then** 状态可回到 `idle`
**And** 嘴型回到 `closed`

### Story 1.4：7 类嘴型渲染与手动测试

As a 开发者,
I want 手动触发并观察 7 类数字狗嘴型,
So that 我能在接入音频前确认每种嘴型视觉差异明确。

**Acceptance Criteria:**

**Given** App 已加载宠物舞台
**When** 开发者触发 `closed`、`small`、`wide`、`round`、`smile`、`teeth`、`pant` 中任一嘴型
**Then** 数字狗嘴巴切换到对应视觉状态
**And** 每种嘴型与其他嘴型有可观察差异

**Given** 当前处于手动嘴型测试
**When** 切换任一嘴型
**Then** 当前嘴型标签同步更新
**And** 来源标记为 `manual` 或开发测试状态

**Given** 手动测试结束
**When** 用户重置到待机
**Then** 嘴型回到 `closed`
**And** 不影响宠物状态机的默认 `idle` 行为

### Story 1.5：宠物基础动作、可访问性与减少动态

As a 评审者,
I want 数字狗在待机、准备、讲话、完成和错误时有自然克制的动作反馈,
So that 它看起来像软萌宠物，而不是静态图或过度表演的玩具。

**Acceptance Criteria:**

**Given** 数字狗处于 `idle`
**When** 用户观察宠物舞台
**Then** 数字狗闭口、轻微呼吸、偶尔眨眼
**And** 动作不造成布局跳动

**Given** mock 状态进入准备、讲话、完成或错误
**When** 状态变化发生
**Then** 耳朵、眼睛、头部、身体、尾巴或项圈至少一项给出对应反馈
**And** 讲话中的辅助动作不遮挡嘴巴区域

**Given** Android 系统动画缩放关闭、降低或用户要求减少动态
**When** 用户触发状态变化
**Then** 装饰性耳朵、头部、身体和尾巴动效降低
**And** 必要的嘴型状态仍可清楚观察

**Given** TalkBack 用户访问宠物舞台
**When** 焦点到达宠物状态摘要
**Then** 可读出当前宠物状态和当前嘴型
**And** 宠物图形本身可以作为装饰，不重复播报复杂图形细节

## Epic 2：稳定文字与示例讲话演示

用户可以通过 TTS 主入口和内置示例稳定触发数字狗讲话，看到准备、讲话、结束、回待机的完整循环，并观察到嘴型时间轴和文本高亮同步推进。

### Story 2.1：TTS 主入口提交与空文本处理

As a 设计评审者,
I want 输入一句话并点击“让狗狗说话”,
So that 我可以用最稳定的方式触发数字狗讲话演示。

**Acceptance Criteria:**

**Given** App 已完成宠物舞台和状态机基础
**When** 用户在 TTS 文本框输入非空文本并点击“让狗狗说话”
**Then** 系统接收文本并创建 TTS 播放意图
**And** 数字狗进入 `thinking` 或准备状态，嘴型保持 `closed`

**Given** TTS 文本框为空
**When** 用户点击“让狗狗说话”
**Then** 不创建讲话 session
**And** 数字狗进入 `error`，嘴型保持 `closed`
**And** 输入区显示“先输入一句话”
**And** 用户可继续编辑文本并重试

**Given** 当前已有播放 session 进行中
**When** 用户再次触发主 CTA
**Then** 主 CTA 处于禁用或停止状态
**And** 不会创建互相冲突的第二个讲话 session

### Story 2.2：TTS 文本到嘴型时间轴生成

As a 开发者,
I want 从用户输入文本生成统一的 `LipSyncTimeline`,
So that 数字狗嘴型、文本高亮和后续调试台能共享同一套时序数据。

**Acceptance Criteria:**

**Given** 用户提交非空中文或英文文本
**When** 系统生成 TTS 时间轴
**Then** 输出 `LipSyncTimeline`，包含 `source=tts`、`quality=stable`、`durationMs`、`segments` 和 `latencyOffsetMs`
**And** 每个 segment 使用 `startMs`、`endMs`、`mouth`、`source`、`confidence` 和可选 `textRange`

**Given** 文本包含中文字符
**When** 系统映射嘴型
**Then** 可按拼音或降级规则生成 `small`、`wide`、`round`、`smile`、`teeth`、`closed` 等嘴型
**And** 无法判断时降级到 `small`，不生成未知嘴型

**Given** 文本包含英文单词或常见字母组合
**When** 系统映射嘴型
**Then** 可按启发式规则识别闭唇、齿音、圆嘴、大开口和咧嘴倾向
**And** 其他有声段降级到 `small`

**Given** 文本包含逗号、句号、问号、感叹号或换行
**When** 系统生成时间轴
**Then** 标点和停顿生成 `closed` 片段
**And** 停顿片段可被文本高亮逻辑识别为不高亮或停顿

### Story 2.3：时间轴平滑、闭口规则与当前嘴型计算

As a 用户,
I want 数字狗讲话时嘴型稳定、自然、不乱跳,
So that 我能相信它是在跟着声音讲话，而不是随机开合嘴。

**Acceptance Criteria:**

**Given** 已生成初始 `LipSyncTimeline`
**When** 系统执行平滑处理
**Then** 相邻相同嘴型被合并
**And** 小于最短保持时间的片段被并入合适相邻片段或按规则保留

**Given** 普通播放正在进行
**When** 系统计算当前嘴型
**Then** 使用 `effectiveTimeMs = audioCurrentTimeMs + latencyOffsetMs` 查找当前 segment
**And** 嘴型切换不超过 5 次/秒

**Given** 当前处于待机、准备、思考、错误或静音段
**When** 系统解析当前嘴型
**Then** 当前嘴型为 `closed`

**Given** 播放结束
**When** 时间轴到达末尾
**Then** 数字狗进入 `done`，嘴型回到 `closed`
**And** 可短暂触发 `pant` 作为宠物反馈，但不作为普通语音默认嘴型

### Story 2.4：TTS 播放同步与文本高亮

As a 评审者,
I want 听到狗狗讲话并看到嘴型和文本同步推进,
So that 我能判断 TTS 主演示路径是否可信。

**Acceptance Criteria:**

**Given** 已生成 TTS `LipSyncTimeline`
**When** 播放开始
**Then** 数字狗进入 `speaking`
**And** 当前嘴型随播放时间和时间轴变化
**And** 头部和身体动作保持克制，不遮挡嘴巴

**Given** 时间轴 segment 包含 `textRange`
**When** 当前播放时间进入该 segment
**Then** 对应文本片段高亮
**And** 多个 segment 对应同一 token 时，该 token 持续高亮

**Given** 当前 segment 是标点或停顿
**When** 播放指针进入该片段
**Then** 嘴型为 `closed`
**And** 文本高亮不应跳到错误字符

**Given** TTS 播放完成
**When** 音频或模拟播放结束
**Then** 数字狗进入 `done`，随后回到 `idle`
**And** 讲话结束 1 秒内嘴型保持或回到 `closed`

### Story 2.5：内置示例稳定演示

As a 业务决策者,
I want 不输入文字、不授权麦克风也能一键播放示例,
So that 我可以稳定看到数字狗讲话效果。

**Acceptance Criteria:**

**Given** 用户未输入文本且未授权麦克风
**When** 用户点击“播放示例”
**Then** 系统加载内置示例文本和预设 `LipSyncTimeline`
**And** 数字狗完成准备、讲话、完成、回待机循环

**Given** 示例时间轴加载成功
**When** 示例播放中
**Then** 调试状态显示 `source=sample`
**And** 解析质量为 `stable`
**And** 时间轴至少覆盖 `closed`、`small`、`wide`、`round`、`smile` 5 类嘴型

**Given** 示例播放结束
**When** 数字狗进入完成反馈
**Then** 嘴型回到 `closed`
**And** 可出现一次短暂眨眼、尾巴轻摇或 `pant` 反馈
**And** 用户可以再次播放示例或改用 TTS 主入口

### Story 2.6：TTS 与示例路径的逻辑测试覆盖

As a 开发者,
I want 用测试覆盖 TTS 时间轴、平滑规则和示例时间轴,
So that 后续改 UI 或音频实现时不会破坏主演示路径。

**Acceptance Criteria:**

**Given** 项目包含测试 target
**When** 运行核心逻辑测试
**Then** 测试覆盖 `LipSyncTimeline` 数据结构、TTS token 映射、标点停顿、相邻片段合并、最短保持时间和当前嘴型解析

**Given** 示例时间轴作为 Bundle 资源或代码预设存在
**When** 运行示例时间轴测试
**Then** 示例时间轴可被加载
**And** `quality=stable`
**And** 覆盖至少 5 类嘴型
**And** 不包含未知嘴型或负时长片段

**Given** 文本为空、极短、包含中英文混排或包含标点
**When** 运行 TTS 时间轴生成测试
**Then** 系统返回可预期结果
**And** 不崩溃，不生成无效 segment

## Epic 3：口型同步调试与延迟校准

开发者和评审者可以通过调试台解释当前嘴型为什么出现，查看来源、质量、波形、时间轴、当前片段和延迟偏移，从而判断同步是否可信。

### Story 3.1：调试快照与当前同步状态展示

As a 开发者,
I want 调试台通过统一 `DebugSnapshot` 展示当前同步状态,
So that 我能确认当前嘴型、宠物状态、输入来源和解析质量是否一致。

**Acceptance Criteria:**

**Given** App 已有宠物状态和播放 session
**When** 系统生成 `DebugSnapshot`
**Then** 快照包含当前宠物状态、当前嘴型、输入来源、解析质量、播放时间、总时长和延迟偏移
**And** 调试台只读取快照，不直接修改宠物状态或播放状态

**Given** 当前播放来源为 TTS 或示例
**When** 播放进行中
**Then** 调试台显示当前嘴型标签、当前状态、输入来源和解析质量
**And** TTS 和示例显示为 `stable`

**Given** 当前无播放 session
**When** 用户进入首屏
**Then** 调试台显示空状态或待机摘要
**And** 当前嘴型显示为 `closed`

### Story 3.2：波形进度展示

As a 开发者,
I want 在调试台看到音频波形或播放进度,
So that 我能把声音节奏和嘴型变化对应起来。

**Acceptance Criteria:**

**Given** 播放 session 包含音频时长或模拟时长
**When** 播放开始
**Then** 波形或进度组件显示当前播放位置和总时长
**And** 播放指针随时间推进

**Given** 当前路径有真实音频分析数据
**When** 调试台渲染波形
**Then** 能显示基础能量变化或静音片段
**And** 当前进度不只依赖颜色表达

**Given** 当前路径暂时没有真实波形数据
**When** 调试台需要展示播放进度
**Then** 可显示模拟波形或进度条
**And** 必须保持组件高度稳定，不造成布局跳动

**Given** Redmi Pad SE 竖屏调试台折叠
**When** 播放进行中
**Then** 摘要中至少保留当前嘴型、状态、来源和进度信息

### Story 3.3：嘴型时间轴可视化

As a 开发者,
I want 在调试台看到嘴型时间轴和当前片段,
So that 我能判断当前嘴型来自哪个时间段。

**Acceptance Criteria:**

**Given** 当前 session 包含 `LipSyncTimeline`
**When** 调试台渲染时间轴
**Then** 每个片段显示嘴型短名，例如 `closed`、`small`、`wide`、`round`、`smile`、`teeth`、`pant`
**And** 当前片段高亮

**Given** 播放指针进入新的 segment
**When** 调试台更新
**Then** 当前片段高亮同步移动
**And** 当前嘴型标签与高亮片段一致

**Given** 当前片段质量较低或来源为实验路径
**When** 时间轴展示该片段
**Then** 可显示质量标记或实验提示
**And** 不把实验路径伪装成稳定质量

**Given** 时间轴标签较多或屏幕较窄
**When** Redmi Pad SE 竖屏展示
**Then** 时间轴可折叠、压缩或横向滚动
**And** 标签不得重叠或裁切到不可读

### Story 3.4：TTS 文本高亮调试视图

As a 评审者,
I want 在调试台看到当前文本片段高亮,
So that 我能理解文字、声音和嘴型之间的节奏关系。

**Acceptance Criteria:**

**Given** 当前播放来源为 TTS 且 segment 包含 `textRange`
**When** 播放指针进入该 segment
**Then** 调试台高亮对应文本片段
**And** 高亮与当前嘴型和时间轴片段同步

**Given** 当前播放来源为示例且包含预设文本
**When** 示例播放中
**Then** 可显示示例文本高亮或示例文本进度
**And** 来源仍显示为 `sample`

**Given** 当前播放来源为上传或麦克风
**When** 调试台渲染文本区域
**Then** 显示无文本时间轴、不显示文本高亮，或以空状态表达
**And** 不显示错误的 TTS 文本

**Given** 高亮仅靠颜色可能不可访问
**When** 文本片段高亮
**Then** 同时使用背景、下划线、边框或其他非颜色提示

### Story 3.5：延迟校准控件

As a 开发者,
I want 调整嘴型相对音频的延迟偏移,
So that 我可以快速判断口型偏早或偏晚并校准演示效果。

**Acceptance Criteria:**

**Given** 调试台处于可校准状态
**When** 用户调整延迟滑杆
**Then** `latencyOffsetMs` 在 `-150ms` 到 `+150ms` 范围内变化
**And** 步进为 `25ms`
**And** UI 显示当前偏移值，例如 `+50ms`

**Given** 延迟偏移发生变化
**When** 系统计算当前嘴型或时间轴指针
**Then** 使用 `effectiveTimeMs = audioCurrentTimeMs + latencyOffsetMs`
**And** 当前嘴型或时间轴指针反映新的偏移

**Given** 用户点击重置
**When** 重置动作完成
**Then** `latencyOffsetMs` 回到 `0ms`
**And** UI 同步显示 `0ms`

**Given** 用户使用外接键盘或 TalkBack
**When** 焦点到达延迟校准控件
**Then** 控件可被调整
**And** 可读出当前毫秒值和调整方向

### Story 3.6：调试台横竖屏与可访问性验收

As a 开发者,
I want 调试台在 Redmi Pad SE 横屏和竖屏都可用且不压过宠物舞台,
So that 评审者看效果、开发者看依据时不会互相干扰。

**Acceptance Criteria:**

**Given** Redmi Pad SE 横屏或接近 1920 x 1200 / 16:10 的平板模拟器
**When** 调试台展示完整模式
**Then** 当前嘴型、来源、质量、波形、时间轴、文本高亮和延迟校准均可见或可达
**And** 调试台不遮挡数字狗嘴巴区域

**Given** Redmi Pad SE 竖屏或接近 1200 x 1920 的平板模拟器
**When** 调试台默认折叠
**Then** 摘要显示当前状态、当前嘴型、输入来源和解析质量
**And** 展开后按波形、时间轴、文本、校准顺序展示详情

**Given** 用户开启 TalkBack
**When** 焦点进入调试台
**Then** 当前嘴型、来源、质量、播放时间和错误状态可被读出
**And** 图表信息有文字替代表达

**Given** 播放正在进行
**When** 调试台持续更新
**Then** 波形、时间轴、标签和数值区域尺寸稳定
**And** 不造成主布局跳动

## Epic 4：实验音频输入与可恢复错误

用户可以上传音频或录制麦克风音频进行基础口型驱动；当权限拒绝、空输入、解析失败或播放失败时，系统给出可恢复错误，并保持 TTS 与示例路径可用。

### Story 4.1：上传音频文件选择与播放入口

As a 内部测试用户,
I want 上传本地音频并播放,
So that 我可以验证非 TTS 音频也能驱动数字狗基础嘴型。

**Acceptance Criteria:**

**Given** App 已完成 TTS/示例播放基础
**When** 用户点击“上传音频”
**Then** 系统打开 Android 系统文件选择能力
**And** 用户可以选择本地音频文件

**Given** 用户选择合法音频文件
**When** 系统完成读取
**Then** 创建上传音频 session
**And** 输入来源显示为上传音频
**And** 解析质量默认显示为 `medium` 或 `experimental`

**Given** 当前已有播放 session
**When** 用户尝试上传新音频
**Then** 上传入口处于禁用，或要求先停止当前播放
**And** 不会同时运行两个播放 session

### Story 4.2：上传音频解码、分析与基础嘴型时间轴

As a 开发者,
I want 对上传音频做基础音频分析并生成 `LipSyncTimeline`,
So that 上传路径能复用同一套嘴型播放与调试能力。

**Acceptance Criteria:**

**Given** 上传音频可被 Android Media API 解码
**When** 系统进行音频分析
**Then** 音频被转换为可分析 PCM 数据
**And** 多声道音频可转为 mono 或等价分析格式

**Given** 音频分析开始
**When** 系统按固定窗口计算帧特征
**Then** 每帧至少包含时间、RMS 或能量、峰值、静音判断
**And** 可选包含低/中/高频能量、spectral centroid 或 zero-crossing rate

**Given** 分析帧已生成
**When** 系统生成上传音频时间轴
**Then** 输出 `LipSyncTimeline`，`source=upload`
**And** 静音段映射为 `closed`
**And** 有声段可映射到 `small`、`wide`、`round`、`smile`、`teeth` 或稳定降级

**Given** 上传音频噪声明显、音量过低或频谱不可区分
**When** 系统评估质量
**Then** `quality=experimental`
**And** 调试台不显示为 `stable`

### Story 4.3：麦克风权限、录制与回放入口

As a 内部测试用户,
I want 用 Redmi Pad SE 麦克风录制短音频并回放,
So that 我可以验证真实录音也能驱动数字狗基础嘴型。

**Acceptance Criteria:**

**Given** 用户点击“开始录音”
**When** 麦克风权限尚未授权
**Then** 系统请求 Android 麦克风运行时权限
**And** 数字狗进入 `listening` 或权限等待状态，嘴型保持 `closed`

**Given** 用户授权麦克风
**When** 用户点击“开始录音”
**Then** 系统开始录制短音频
**And** 录音按钮切换为“停止录音”
**And** 当前输入来源显示为麦克风

**Given** 用户点击“停止录音”
**When** 录音有效
**Then** 系统创建麦克风 session
**And** 录音可回放
**And** 回放时进入 `speaking`

**Given** 录音时长少于有效阈值或音量过低
**When** 系统处理录音
**Then** 进入可恢复错误或显示 `experimental` 质量
**And** TTS 和示例路径仍可使用

### Story 4.4：麦克风录音分析复用上传分析流程

As a 开发者,
I want 麦克风录音复用上传音频的分析和时间轴生成能力,
So that 系统不会产生第二套音频口型逻辑。

**Acceptance Criteria:**

**Given** 麦克风录音文件或 buffer 可用于分析
**When** 系统处理录音
**Then** 使用与上传音频相同或等价的 `AudioFeatureExtractor` 和 `AudioFeatureTimelineGenerator`
**And** 输出 `source=microphone` 的 `LipSyncTimeline`

**Given** 麦克风录音分析完成
**When** 系统评估质量
**Then** 默认质量为 `experimental`
**And** 不显示为 `stable`

**Given** 麦克风回放进行中
**When** 播放时间推进
**Then** 当前嘴型由 `LipSyncTimeline` 和播放时钟计算
**And** 调试台显示当前嘴型、来源、质量、波形或进度、时间轴和延迟偏移

**Given** 第一版范围限制
**When** 用户录音
**Then** 系统采用录制后回放分析
**And** 不实现实时流式麦克风口型

### Story 4.5：可恢复错误与稳定路径保护

As a 评审者,
I want 上传、录音或输入失败时 Demo 仍然可继续演示,
So that 失败不会破坏整体展示。

**Acceptance Criteria:**

**Given** 用户提交空文本
**When** 系统检测到无有效输入
**Then** 数字狗进入 `error`，嘴型为 `closed`
**And** 输入区显示“先输入一句话”
**And** 用户可以继续使用 TTS 或示例

**Given** 用户拒绝麦克风权限
**When** 权限结果返回拒绝
**Then** 数字狗进入 `error`，嘴型为 `closed`
**And** UI 显示“麦克风权限未开启”
**And** TTS 和示例入口仍可点击

**Given** 上传音频不可解码或解析失败
**When** 系统捕获失败
**Then** 数字狗进入 `error`，嘴型为 `closed`
**And** UI 显示“音频解析失败，请换一段音频”
**And** 调试台显示失败来源和实验/失败质量

**Given** 播放失败或 session 中断
**When** 系统进入错误恢复
**Then** 停止当前 session
**And** 状态可回到 `idle`
**And** 不影响下一次 TTS 或示例播放

### Story 4.6：实验入口质量标识与验收测试

As a 开发者,
I want 用清晰的质量标识和测试覆盖实验入口,
So that 上传和麦克风能力可用但不会误导评审者。

**Acceptance Criteria:**

**Given** 当前 session 来源为上传音频
**When** 调试台展示解析质量
**Then** 质量只能是 `medium`、`experimental` 或 `failed`
**And** 不显示为 `stable`

**Given** 当前 session 来源为麦克风
**When** 调试台展示解析质量
**Then** 质量默认显示为 `experimental`
**And** UI 可通过文字说明这是实验入口

**Given** 运行逻辑测试
**When** 测试上传和麦克风时间轴生成
**Then** 验证静音段生成 `closed`
**And** 有声段生成合法嘴型
**And** 时间轴不包含未知嘴型、负时长或重叠片段

**Given** 运行错误恢复测试或手动验收
**When** 模拟权限拒绝、解析失败、播放失败
**Then** 系统进入可恢复错误
**And** TTS 与示例路径仍能继续使用
