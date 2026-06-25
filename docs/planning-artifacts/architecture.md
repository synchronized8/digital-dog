---
stepsCompleted: [1, 2, 3, 4, 5, 6, 7, 8]
inputDocuments:
  - docs/planning-artifacts/prds/prd-digital-dog-2026-06-23/prd.md
  - docs/planning-artifacts/prds/prd-digital-dog-2026-06-23/addendum.md
  - docs/planning-artifacts/prds/prd-digital-dog-2026-06-23/review-rubric.md
  - docs/planning-artifacts/prds/prd-digital-dog-2026-06-23/reconcile-sources.md
  - docs/planning-artifacts/briefs/brief-digital-dog-2026-06-23/brief.md
  - docs/planning-artifacts/briefs/brief-digital-dog-2026-06-23/addendum.md
  - docs/planning-artifacts/ux-design-specification.md
  - docs/planning-artifacts/ux-design-directions.html
  - docs/planning-artifacts/platform-decision-ipad-display.md
  - docs/MVP-SPEC.md
  - docs/UI-SPEC.md
  - docs/LIPSYNC-SPEC.md
  - docs/brainstorming/brainstorming-session-20260623-144420.md
workflowType: 'architecture'
lastStep: 8
status: 'complete'
completedAt: '2026-06-24'
project_name: '数字狗语音口型 Demo'
user_name: 'kejincheng'
date: '2026-06-23'
---

# 架构决策文档：数字狗语音口型 Demo

本文档将按 BMAD 架构流程逐步生成。每一步会先基于已确认的产品、UX、口型同步和 iPad 平台决策资料形成建议，再经过确认后写入正式架构结论。

## 2026-06-24 平台纠偏：Redmi Pad SE / MIUI Android 平板

第一版展示设备已从 iPadOS / iPad 更正为 **小米 Redmi Pad SE 11 英寸 MIUI / Android 平板**。本文后续历史段落中的 iPadOS、Xcode、SwiftUI、AVFoundation、Swift Testing、VoiceOver、Reduce Motion 等表述全部被本节覆盖。

新的架构基线如下：

- 平台：Android 原生平板应用，目标展示设备为 Redmi Pad SE 11 英寸。
- 屏幕：1920 x 1200、16:10、最高 90Hz；横屏优先，竖屏完整可用。
- 系统：MIUI Pad 14 / Android 13 作为设备基线。
- 语言与 UI：Kotlin + Jetpack Compose。
- 音频与语音：Android `TextToSpeech`、Android Media APIs、`AudioRecord` 或 `MediaRecorder`，必要时使用 Media3。
- 上传：Android Storage Access Framework / 系统文件选择器。
- 可访问性：TalkBack、字体缩放、触控目标不小于 48dp、系统权限提示和 Android 动画缩放设置。
- 测试：Kotlin/JVM 单元测试、Compose UI Test、Android 仪器化测试。

不变的架构契约：`PetState`、`MouthShape`、`InputSource`、`TimelineQuality`、`LipSyncTimeline`、单一 `SpeechDemoStore`、所有输入统一输出时间轴、调试台只读调试快照、未来 AI 通过 adapter 接入。

## 项目上下文分析

### 需求概览

**功能需求：**

项目包含 19 个功能需求，主要分为 5 组：

1. 宠物舞台与状态机：首屏展示数字狗，支持 `idle`、`listening`、`thinking`、`speaking`、`done`、`error` 状态，并在待机、思考、错误时保持闭口。
2. 音频输入与播放入口：支持文字转语音、内置示例音频、上传音频、麦克风录音四种入口，其中 TTS 是主演示路径，示例音频是稳定兜底，上传和麦克风是实验入口。
3. 口型同步：支持 `closed`、`small`、`wide`、`round`、`smile`、`teeth`、`pant` 7 类嘴型；TTS 需要生成嘴型时间轴和文本高亮；上传和麦克风使用基础音频分析驱动嘴型。
4. 调试台：展示当前状态、当前嘴型、输入来源、解析质量、波形、嘴型时间轴、文本高亮和延迟校准。
5. 错误、布局与 AI 预留：错误必须可恢复；iPad 横屏优先、竖屏可用；第一版不接真实 AI，只保留未来 AI 对话状态。

**非功能需求：**

关键非功能需求包括：

- iPad 横屏与竖屏核心流程都必须可用，控件不得重叠，文字不得溢出。
- 触控目标、按钮、输入、上传、录音、滑杆和折叠控件需要支持 iPadOS 可访问性能力。
- 状态和调试信息不能只依赖颜色表达，需要文字、标签、位置或形状辅助。
- 口型切换、状态变化、波形和时间轴更新不能造成明显卡顿。
- 宠物动作必须克制，不能干扰嘴型观察。
- 上传和麦克风路径必须诚实标记为实验质量，不能伪装成高精度音素同步。
- 麦克风权限拒绝、上传失败、解析失败不能阻断 TTS 和内置示例路径。

**规模与复杂度：**

- 主要技术领域：iPadOS 原生互动音频、动画与调试可视化。
- 复杂度级别：中等偏高。
- 复杂度来源不是业务流程数量，而是音频播放、口型时间轴、宠物状态机、动画表现、iPad 自适应布局和调试可解释性需要同步工作。
- 预估核心架构组件包括：应用入口与布局层、宠物状态机、数字狗渲染器、口型渲染器、语音输入中心、TTS 时间轴生成器、示例时间轴模块、上传音频分析模块、麦克风录音分析模块、播放同步控制器、调试台、错误恢复与权限处理。

### 技术约束与依赖

已确认的技术约束包括：

- 第一版展示目标是 iPadOS 原生应用，不再按网页 UI 架构处理。
- 已确认推荐技术方向为 `SwiftUI + AVFoundation`。
- TTS 主路径可使用 `AVSpeechSynthesizer` 或预设 TTS 时间轴。
- 音频播放、解码和基础分析依赖 iPadOS 原生音频能力。
- 上传音频使用 iPadOS 文件选择能力。
- 麦克风录音使用 iPadOS 麦克风权限、录制和回放分析能力。
- 数字狗资产需要采用 iPadOS 可控方案，例如 SwiftUI vector/Canvas、Lottie 或 Rive。
- 第一版不依赖云端 AI、服务端 ASR、强制对齐或云端音素时间戳。
- 口型同步数据结构、7 类嘴型和宠物状态机需要作为后续 AI 对话接入的稳定契约。

### 已识别的跨领域关注点

- 状态一致性：宠物状态、嘴型、音频播放、文本高亮、波形进度和调试台必须由统一状态源驱动。
- 时间同步：音频播放进度、嘴型时间轴和延迟校准需要明确的时钟基准。
- 输入路径统一：TTS、示例、上传和麦克风最终都应输出统一的 `LipSyncTimeline`。
- 质量分级：不同输入路径的解析质量需要进入数据模型和 UI，避免用户误解。
- 可恢复错误：空文本、上传失败、权限拒绝、解析失败和播放失败都需要回到可操作状态。
- iPad 布局适配：横屏双栏和竖屏折叠调试台需要共享同一信息架构。
- 可访问性：动画、颜色、图表、频繁嘴型变化都需要可访问替代表达。
- 后续 AI 扩展：第一版不能接入真实 AI，但状态机和模块边界要允许未来接入语音对话链路。

## 启动模板评估

### 主要技术领域

本项目属于 iPadOS 原生应用，而不是 Web、跨端移动框架或后端服务。基础架构应从 Xcode 官方 iOS App 模板开始，目标设备以 iPad 为主，界面层使用 SwiftUI，音频与语音能力使用 AVFoundation / AVFAudio。

### 已评估的启动方案

1. Xcode iOS App 模板
   - 优点：Apple 官方维护，直接支持 iPadOS、SwiftUI、预览、模拟器、测试 target、权限配置和 AVFoundation 接入。
   - 缺点：不是命令行一键 scaffold，项目初始化主要通过 Xcode GUI 完成。
   - 结论：最适合作为 MVP 基础。

2. Xcode Multiplatform App 模板
   - 优点：适合未来扩展到 macOS 或其他 Apple 平台。
   - 缺点：第一版只验证 iPad 展示体验，多平台结构会提前增加复杂度。
   - 结论：暂不采用。

3. Tuist / XcodeGen 等项目生成器
   - 优点：适合大型团队维护可复现工程结构。
   - 缺点：MVP 阶段会引入额外工具链和维护成本。
   - 结论：暂不作为第一版基础，可在工程复杂度上升后再评估。

4. React Native / Flutter / WebView 路线
   - 优点：跨平台复用潜力高。
   - 缺点：本项目已确认是 iPadOS 原生展示，且依赖原生 TTS、录音、文件选择、音频分析和细腻动画同步。
   - 结论：不采用。

### 选定启动方案：Xcode iOS App 模板

**选择理由：**

第一版目标是快速验证 iPad 上的数字狗讲话体验。官方 Xcode iOS App 模板能直接提供 SwiftUI 应用入口、iPad 模拟器运行、测试 target、资源管理和权限配置基础，避免把时间花在跨端框架或工程生成器上。音频播放、TTS、录音权限、上传音频解码和基础分析都可以自然落在 AVFoundation / AVFAudio 体系内。

**初始化方式：**

```bash
open -a Xcode
```

在 Xcode 中创建项目：

- 模板：iOS > App
- Product Name：DigitalDog
- Interface：SwiftUI
- Language：Swift
- Storage：None
- Tests：启用
- 目标设备：iPad 优先，保留必要的 iOS 兼容能力
- 最低系统版本：后续架构决策中确认，建议不低于项目实际演示设备支持版本

**启动模板提供的架构决策：**

**语言与运行时：**
使用 Swift。优先采用当前 Xcode 稳定版本支持的 Swift 语言模式，业务模型、状态机、口型时间轴和音频分析逻辑都用 Swift 实现。

**界面方案：**
使用 SwiftUI。主界面按 iPad 横屏双栏和竖屏上下结构实现，数字狗舞台、输入中心、调试台和状态反馈拆成独立 View。

**音频与语音基础：**
使用 AVFoundation / AVFAudio。TTS 路径使用 AVSpeechSynthesizer 或预设时间轴；上传和麦克风路径使用原生音频解码、录音权限和 PCM 分析能力。

**构建工具：**
使用 Xcode 原生工程与 xcodebuild。第一版不引入额外工程生成器。

**测试框架：**
优先使用 Swift Testing 或 XCTest。重点覆盖状态机、LipSyncTimeline 生成、嘴型映射、延迟偏移和错误恢复逻辑；UI 验证通过 Xcode 预览、模拟器和必要的 UI 测试补充。

**代码组织：**
建议从一开始按模块分组：App、DesignSystem、PetStage、LipSync、AudioInput、Playback、DebugPanel、SharedModels、Tests。

**开发体验：**
使用 SwiftUI Preview 验证宠物舞台和组件状态；使用 iPad 模拟器验证横竖屏布局；使用真实 iPad 验证麦克风权限、音频播放和触控体验。

**备注：**
项目初始化应作为第一条实施任务。第一版不使用网页 starter，不使用 React Native / Flutter starter，也不引入 Tuist/XcodeGen，除非后续工程复杂度明显上升。

## 核心架构决策

### 决策优先级分析

**关键决策：**

- 平台：iPadOS 原生应用，使用 Xcode iOS App 模板、Swift、SwiftUI。
- 最低系统：MVP 建议 iPadOS 17.0+；如演示设备更老，实施前再降级评估。
- 数据：MVP 不使用数据库，使用内存状态 + Bundle 内置示例数据 + 少量本地偏好设置。
- 状态：使用单一主状态源管理宠物状态、播放状态、当前嘴型、时间轴和调试信息。
- 口型：所有入口统一输出 `LipSyncTimeline`，宠物渲染器只消费当前嘴型和宠物状态。
- 音频：TTS、示例、上传、麦克风统一进入播放与时间轴同步层。
- 渲染：MVP 优先 SwiftUI vector / Canvas；Rive 或 Lottie 作为后续可替换资产层。
- 网络：第一版无后端、无真实 AI、无云端 TTS/ASR。

**重要决策：**

- 测试：核心逻辑用 Swift Testing 或 XCTest 覆盖；UI 和音频权限用模拟器 + 真机验证。
- 安全：本地处理音频，不默认上传、不默认持久化麦克风录音。
- 调试：调试台读取统一状态，不反向驱动宠物表现。
- AI 预留：后续通过协议适配真实 AI 对话链路，不改变宠物状态机和口型时间轴契约。

**延后决策：**

- 真实 AI 对话、云端 TTS、ASR、音素级对齐、实时麦克风流式口型。
- Rive / Lottie 正式资产管线。
- Xcode Cloud、TestFlight、App Store 分发。
- 多平台扩展。

### 数据架构

MVP 不引入 Core Data、SwiftData 或 SQLite。核心数据全部使用 Swift struct / enum 表达，并遵循 `Codable`、`Equatable`，便于调试、测试和后续保存。

核心模型包括：

- `PetState`
- `MouthShape`
- `InputSource`
- `TimelineQuality`
- `LipSyncSegment`
- `LipSyncTimeline`
- `SpeechSession`
- `DebugSnapshot`
- `DigitalDogError`

本地持久化只用于低风险偏好项，例如延迟校准值、调试台展开状态。可使用 `AppStorage` / `UserDefaults`。内置示例时间轴放在 App Bundle 中。

### 认证与安全

MVP 不做登录、鉴权和用户体系。

安全重点放在 iPadOS 权限与隐私：

- 麦克风权限通过系统能力申请，并在拒绝时进入可恢复错误状态。
- 上传音频只在本地解析，不上传服务器。
- 麦克风录音默认仅用于当前回放分析，不长期保存。
- 未来接入真实 AI 时，不在客户端硬编码服务密钥；需要通过后端或受控代理管理凭据。

### API 与通信模式

MVP 不定义网络 API。

内部模块通过协议解耦：

- `SpeechInputProvider`：统一 TTS、示例、上传、麦克风入口。
- `TimelineGenerating`：从文本或音频特征生成 `LipSyncTimeline`。
- `AudioPlaybackControlling`：管理播放、停止、当前时间和播放完成。
- `PetStateReducing`：把用户输入、播放事件、错误事件转换为宠物状态。
- `DebugSnapshotProviding`：为调试台提供只读状态快照。

错误统一使用 `DigitalDogError`，映射到宠物 `error` 状态和入口附近错误文案。

### 前端架构

使用 SwiftUI 单屏应用结构，不引入路由框架。

推荐模块划分：

- `App`
- `DesignSystem`
- `PetStage`
- `DogRenderer`
- `SpeechInputCenter`
- `LipSync`
- `AudioInput`
- `Playback`
- `DebugPanel`
- `SharedModels`

状态管理采用主线程上的单一 Store，例如 `SpeechDemoStore`。SwiftUI View 只负责展示和触发意图，不直接持有音频分析逻辑。

布局策略：

- iPad 横屏：宠物舞台 + 调试台双栏，输入区稳定展示。
- iPad 竖屏：宠物舞台优先，调试台摘要默认展示，详情折叠。
- 播放中不改变主要组件尺寸，避免嘴型、波形和时间轴造成布局跳动。
- 支持 Reduce Motion：保留必要嘴型切换，降低耳朵、头部、身体、尾巴装饰动效。

### 基础设施与部署

MVP 使用 Xcode 本地开发、模拟器和真机验证。

- 构建：Xcode / `xcodebuild`
- 依赖：默认零第三方依赖；如后续引入资产动画库，优先使用 Swift Package Manager。
- 配置：Debug / Release 分离；MVP 无服务端环境变量。
- 日志：使用调试台 + `OSLog`。
- CI：MVP 可暂缓；进入多人协作或 TestFlight 阶段后再评估 Xcode Cloud 或 GitHub Actions macOS runner。

### 决策影响分析

**实施顺序：**

1. 创建 Xcode iOS App 项目。
2. 定义核心模型和状态机。
3. 实现 `LipSyncTimeline` 与 TTS / 示例时间轴。
4. 实现 SwiftUI 宠物舞台和 7 类嘴型渲染。
5. 接入 TTS / 示例播放同步。
6. 实现调试台只读快照。
7. 接入上传音频分析。
8. 接入麦克风录音与回放分析。
9. 补齐错误恢复、可访问性和 iPad 横竖屏验收。

**跨组件依赖：**

- 宠物舞台依赖统一状态源和当前嘴型，不依赖具体输入入口。
- 调试台依赖 `DebugSnapshot`，不直接控制音频和宠物。
- 所有输入入口必须输出统一时间轴，否则后续 AI 接入会变复杂。
- 渲染资产可以替换，但不能改变 7 类嘴型、状态机和时间轴数据结构。

## 实施模式与一致性规则

### 已识别的关键冲突点

本项目至少有 10 类容易出现实现分歧的点：模型命名、文件命名、模块边界、状态更新、时间轴格式、错误结构、调试快照、音频入口输出、测试位置、资源组织。

### 命名模式

**数据库命名约定：**

MVP 无数据库，不定义表、字段、迁移和索引命名。若后续引入本地持久化，只允许先写入架构变更文档，再决定 SwiftData / SQLite / 文件存储路线。

**API 命名约定：**

MVP 无网络 API。内部协议命名使用能力名 + 后缀：

- 输入入口协议：`SpeechInputProvider`
- 时间轴生成协议：`TimelineGenerating`
- 播放控制协议：`AudioPlaybackControlling`
- 调试快照协议：`DebugSnapshotProviding`

未来真实 AI 接入时，外部 API 不直接进入 SwiftUI View，必须通过独立 adapter 转为本地 `SpeechSession` 和 `LipSyncTimeline`。

**代码命名约定：**

- Swift 类型使用 `PascalCase`，例如 `SpeechDemoStore`、`LipSyncTimeline`。
- 属性、方法、局部变量使用 `camelCase`，例如 `currentMouthShape`、`startSpeaking()`。
- enum case 使用 `lowerCamelCase`，例如 `idle`、`speaking`、`experimental`。
- 文件名与主类型同名，例如 `SpeechDemoStore.swift`、`DogRendererView.swift`。
- SwiftUI View 统一以 `View` 结尾；非 View 不使用 `View` 后缀。

### 结构模式

**项目组织：**

代码按功能模块组织，不按 SwiftUI 控件类型堆叠：

- `App`
- `DesignSystem`
- `SharedModels`
- `PetStage`
- `DogRenderer`
- `SpeechInputCenter`
- `LipSync`
- `AudioInput`
- `Playback`
- `DebugPanel`
- `Resources`
- `Tests`

**测试位置：**

核心纯逻辑测试放在测试 target 中，文件名与被测对象对应：

- `LipSyncTimelineTests.swift`
- `PetStateReducerTests.swift`
- `TimelineGeneratorTests.swift`
- `DigitalDogErrorTests.swift`

SwiftUI Preview 只用于视觉状态验证，不替代逻辑测试。

**资源组织：**

- 内置示例文本和时间轴放入 `Resources/Samples`。
- 宠物视觉资产放入 `Resources/PetAssets`。
- 调试用 mock 数据放入 `Resources/PreviewData`。
- 真实用户上传和麦克风录音不放入 Bundle。

### 格式模式

**时间轴数据格式：**

所有输入入口必须输出统一的 `LipSyncTimeline`。时间单位统一为毫秒，字段使用 Swift 语义命名：

- `startMs`
- `endMs`
- `mouth`
- `source`
- `quality`
- `textRange`

禁止让 TTS、示例、上传、麦克风各自维护不同时间轴结构。

**错误格式：**

错误统一收敛为 `DigitalDogError`，并提供：

- 面向开发的 `code`
- 面向用户的 `message`
- 可恢复动作 `recoveryAction`
- 来源入口 `source`

SwiftUI View 不直接判断底层音频错误类型，只消费 `DigitalDogError`。

### 通信模式

**状态管理：**

使用单一主状态源 `SpeechDemoStore`。View 只能发送用户意图，例如 `submitText()`、`playSample()`、`startRecording()`、`stopPlayback()`，不能直接修改宠物状态或嘴型。

**状态更新规则：**

- 宠物状态由事件和 reducer 推导，不在多个 View 中分别赋值。
- 当前嘴型由播放时间 + `LipSyncTimeline` + `latencyOffsetMs` 计算。
- 调试台只读取 `DebugSnapshot`，不直接驱动播放或宠物动画。

**事件命名：**

内部事件使用过去式或意图式命名：

- 用户意图：`textSubmitted`、`sampleRequested`
- 系统事件：`timelineGenerated`、`playbackStarted`、`playbackCompleted`
- 错误事件：`audioDecodeFailed`、`microphonePermissionDenied`

### 流程模式

**加载状态：**

入口级 loading 必须归属到当前 `SpeechSession`，不得使用多个互相冲突的布尔值。播放中禁止触发新的上传、录音或 TTS 播放，除非先停止当前 session。

**错误恢复：**

任何错误都必须满足：

- 数字狗嘴型回到 `closed`
- 宠物状态进入 `error`
- TTS 和示例入口仍可继续使用
- 错误文案靠近对应入口
- 调试台显示错误来源和解析质量

**音频入口输出：**

四种入口统一遵守：

1. 生成或加载音频来源。
2. 生成 `LipSyncTimeline`。
3. 创建 `SpeechSession`。
4. 交给播放同步层。
5. 由 Store 更新宠物状态和调试快照。

### 强制执行规则

所有 AI Agent 必须：

- 不新增第二套宠物状态机。
- 不新增第二套嘴型枚举。
- 不让 SwiftUI View 直接操作 AVFoundation 细节。
- 不让上传或麦克风路径伪装成 `stable`。
- 不绕过 `LipSyncTimeline` 直接驱动嘴型。
- 不在播放中改变主布局尺寸。
- 不把真实 AI、云端 TTS、ASR 或实时麦克风流式分析塞进 MVP。

### 正例

- `TTSInputView` 触发 `store.submitText(text)`。
- `SpeechDemoStore` 生成 `SpeechSession`。
- `TimelinePlaybackController` 根据播放时间计算当前嘴型。
- `DogRendererView` 只接收 `petState`、`mouthShape`、`energyLevel`。
- `DebugPanelView` 只接收 `DebugSnapshot`。

### 反模式

- 在 `DogRendererView` 内直接读取麦克风。
- 在上传音频模块里定义一套 `UploadMouthSegment`。
- 在调试台按钮里直接修改 `currentMouthShape`。
- TTS 用秒，上传用毫秒，麦克风用采样帧。
- 错误时只弹系统 alert，不更新宠物状态。

## 项目结构与边界

### 完整项目目录结构

```text
digital-dog/
├── README.md
├── docs/
│   ├── MVP-SPEC.md
│   ├── UI-SPEC.md
│   ├── LIPSYNC-SPEC.md
│   └── planning-artifacts/
│       └── architecture.md
├── DigitalDog/
│   ├── DigitalDog.xcodeproj
│   ├── DigitalDog/
│   │   ├── App/
│   │   │   ├── DigitalDogApp.swift
│   │   │   └── AppRootView.swift
│   │   ├── DesignSystem/
│   │   │   ├── DesignTokens.swift
│   │   │   ├── DogColors.swift
│   │   │   ├── DogTypography.swift
│   │   │   └── ControlStyles.swift
│   │   ├── SharedModels/
│   │   │   ├── PetState.swift
│   │   │   ├── MouthShape.swift
│   │   │   ├── InputSource.swift
│   │   │   ├── TimelineQuality.swift
│   │   │   ├── LipSyncSegment.swift
│   │   │   ├── LipSyncTimeline.swift
│   │   │   ├── SpeechSession.swift
│   │   │   ├── DebugSnapshot.swift
│   │   │   └── DigitalDogError.swift
│   │   ├── State/
│   │   │   ├── SpeechDemoStore.swift
│   │   │   ├── PetStateReducer.swift
│   │   │   └── SpeechDemoEvent.swift
│   │   ├── PetStage/
│   │   │   ├── PetStageView.swift
│   │   │   ├── PetStatusBadgeView.swift
│   │   │   └── CollarStatusView.swift
│   │   ├── DogRenderer/
│   │   │   ├── DogRendererView.swift
│   │   │   ├── DogFaceView.swift
│   │   │   ├── DogMouthView.swift
│   │   │   ├── DogMotionModel.swift
│   │   │   └── MouthShapePreviewData.swift
│   │   ├── SpeechInputCenter/
│   │   │   ├── SpeechInputCenterView.swift
│   │   │   ├── TTSInputView.swift
│   │   │   ├── SampleAudioButtonView.swift
│   │   │   ├── UploadAudioButtonView.swift
│   │   │   └── MicrophoneRecordButtonView.swift
│   │   ├── LipSync/
│   │   │   ├── TimelineGenerating.swift
│   │   │   ├── TTSTimelineGenerator.swift
│   │   │   ├── SampleTimelineProvider.swift
│   │   │   ├── AudioFeatureTimelineGenerator.swift
│   │   │   ├── TimelineSmoother.swift
│   │   │   └── CurrentMouthResolver.swift
│   │   ├── AudioInput/
│   │   │   ├── SpeechInputProvider.swift
│   │   │   ├── TTSSpeechProvider.swift
│   │   │   ├── SampleSpeechProvider.swift
│   │   │   ├── UploadedAudioProvider.swift
│   │   │   ├── MicrophoneRecordingProvider.swift
│   │   │   ├── AudioFeatureExtractor.swift
│   │   │   └── MicrophonePermissionClient.swift
│   │   ├── Playback/
│   │   │   ├── AudioPlaybackControlling.swift
│   │   │   ├── TimelinePlaybackController.swift
│   │   │   ├── AVSpeechPlaybackController.swift
│   │   │   ├── FileAudioPlaybackController.swift
│   │   │   └── PlaybackClock.swift
│   │   ├── DebugPanel/
│   │   │   ├── DebugPanelView.swift
│   │   │   ├── DebugSummaryView.swift
│   │   │   ├── WaveformView.swift
│   │   │   ├── MouthTimelineView.swift
│   │   │   ├── TTSTextHighlightView.swift
│   │   │   ├── QualityBadgeView.swift
│   │   │   └── LatencyCalibrationView.swift
│   │   ├── Accessibility/
│   │   │   ├── AccessibilityLabels.swift
│   │   │   └── ReduceMotionPolicy.swift
│   │   └── Resources/
│   │       ├── Samples/
│   │       │   ├── sample-tts-text.json
│   │       │   └── sample-lipsync-timeline.json
│   │       ├── PetAssets/
│   │       └── PreviewData/
│   │           ├── preview-debug-snapshot.json
│   │           └── preview-timeline.json
│   ├── DigitalDogTests/
│   │   ├── LipSyncTimelineTests.swift
│   │   ├── TTSTimelineGeneratorTests.swift
│   │   ├── CurrentMouthResolverTests.swift
│   │   ├── PetStateReducerTests.swift
│   │   ├── TimelineSmootherTests.swift
│   │   └── DigitalDogErrorTests.swift
│   └── DigitalDogUITests/
│       ├── DigitalDogLayoutUITests.swift
│       ├── SpeechFlowUITests.swift
│       └── AccessibilitySmokeTests.swift
└── scripts/
    └── README.md
```

### 架构边界

**API 边界：**

MVP 无网络 API。未来 AI API 只能通过独立 adapter 接入，输出本地 `SpeechSession`、`PetState` 和 `LipSyncTimeline`，不得直接驱动 SwiftUI View。

**组件边界：**

- `SpeechInputCenter` 只负责收集用户输入和触发意图。
- `State` 负责统一状态转换。
- `LipSync` 负责生成和解析嘴型时间轴。
- `AudioInput` 负责 TTS、示例、上传、麦克风来源。
- `Playback` 负责播放时钟和当前时间。
- `DogRenderer` 只负责视觉渲染。
- `DebugPanel` 只读 `DebugSnapshot`。

**服务边界：**

音频底层能力集中在 `AudioInput` 和 `Playback`。SwiftUI View 不直接调用 AVFoundation / AVFAudio。

**数据边界：**

所有输入路径最终都转为 `LipSyncTimeline`。调试台、宠物舞台和文本高亮共享同一个 session 状态，不各自维护数据副本。

### 需求到结构映射

**宠物舞台与状态机：**

- `PetStage/`
- `DogRenderer/`
- `State/`
- `SharedModels/PetState.swift`
- `SharedModels/MouthShape.swift`

**音频输入与播放入口：**

- `SpeechInputCenter/`
- `AudioInput/`
- `Playback/`

**口型同步：**

- `LipSync/`
- `SharedModels/LipSyncTimeline.swift`
- `SharedModels/LipSyncSegment.swift`
- `DogRenderer/DogMouthView.swift`

**调试台：**

- `DebugPanel/`
- `SharedModels/DebugSnapshot.swift`
- `SharedModels/TimelineQuality.swift`

**错误、iPad 布局和 AI 预留：**

- `SharedModels/DigitalDogError.swift`
- `Accessibility/`
- `App/AppRootView.swift`
- 未来 AI adapter 暂不创建目录，避免 MVP 范围膨胀。

### 集成点

**内部通信：**

用户操作进入 `SpeechDemoStore`，Store 调用输入 provider 和时间轴生成器，创建 `SpeechSession`，播放控制器推进时钟，Store 输出宠物状态、当前嘴型和调试快照。

**外部集成：**

MVP 只集成 iPadOS 原生能力：TTS、音频播放、文件选择、麦克风权限、录音、可访问性。

**数据流：**

`用户输入 / 示例 / 上传 / 麦克风` -> `SpeechInputProvider` -> `LipSyncTimeline` -> `SpeechSession` -> `PlaybackClock` -> `CurrentMouthResolver` -> `DogRendererView` + `DebugPanelView`

### 文件组织模式

**配置文件：**

Xcode 工程配置由 `DigitalDog.xcodeproj` 管理。MVP 无 `.env`。权限文案放在 Xcode target 的 Info 配置中。

**源码组织：**

源码按功能模块组织。共享 enum、struct、protocol 放入 `SharedModels` 或对应模块，不跨模块重复定义。

**测试组织：**

纯逻辑测试放入 `DigitalDogTests`。UI、布局和可访问性烟测放入 `DigitalDogUITests`。

**资源组织：**

内置示例和预览数据放入 Bundle。用户上传和麦克风录音不进入资源目录。

### 开发工作流集成

**开发结构：**

开发者从 `DigitalDog.xcodeproj` 打开工程，使用 SwiftUI Preview 和 iPad 模拟器验证主要界面状态。

**构建结构：**

构建通过 Xcode 或 `xcodebuild` 执行。MVP 不依赖额外代码生成工具。

**部署结构：**

MVP 以本地真机演示为主。TestFlight、Xcode Cloud 和 App Store 分发延后。

## 架构验证结果

### 一致性验证 ✅

**决策兼容性：**

当前决策彼此兼容：iPadOS 原生应用、Xcode iOS App 模板、SwiftUI、AVFoundation / AVFAudio、无后端、无数据库、统一 `LipSyncTimeline` 和单一 `SpeechDemoStore` 可以共同支撑 MVP。  
版本基线补充为：MVP 使用 Xcode 26.5 稳定线与 Swift 6.3；Xcode 27 beta 不作为 MVP 基线。

**模式一致性：**

命名、目录、状态更新、错误处理和时间轴格式都围绕 Swift/iPadOS 原生架构定义。实施规则明确禁止第二套状态机、第二套嘴型枚举和入口私有时间轴，能降低多 Agent 实现冲突。

**结构对齐：**

项目结构覆盖 App、DesignSystem、SharedModels、State、PetStage、DogRenderer、SpeechInputCenter、LipSync、AudioInput、Playback、DebugPanel、Accessibility、Resources 和 Tests。目录边界与核心架构决策一致。

### 需求覆盖验证 ✅

**功能覆盖：**

PRD 中 19 个功能需求均有架构支撑：

- FR-1 到 FR-4：由 `PetStage`、`DogRenderer`、`State`、`SharedModels` 覆盖。
- FR-5 到 FR-8：由 `SpeechInputCenter`、`AudioInput`、`Playback` 覆盖。
- FR-9 到 FR-13：由 `LipSync`、`DogMouthView`、`CurrentMouthResolver`、`TimelineSmoother` 覆盖。
- FR-14 到 FR-16：由 `DebugPanel`、`DebugSnapshot`、`WaveformView`、`MouthTimelineView`、`LatencyCalibrationView` 覆盖。
- FR-17 到 FR-19：由 `DigitalDogError`、`Accessibility`、`AppRootView` 和未来 AI adapter 边界覆盖。

**非功能覆盖：**

- iPad 横竖屏：通过 `AppRootView`、SwiftUI 自适应布局和固定组件尺寸约束覆盖。
- 可访问性：通过 `AccessibilityLabels`、`ReduceMotionPolicy`、VoiceOver 标签和可见错误反馈覆盖。
- 性能感知：通过单一播放时钟、统一时间轴和播放中不改变布局尺寸降低风险。
- 隐私安全：通过本地音频处理、默认不保存录音、不上传文件覆盖。
- 可解释性：通过只读调试台、质量标签、时间轴、波形和延迟校准覆盖。

### 实施就绪验证 ✅

**决策完整性：**

关键技术、状态、数据、渲染、音频、调试、安全和部署决策都已记录。版本基线已在验证阶段补齐：Xcode 26.5 / Swift 6.3，MVP 最低系统建议 iPadOS 17.0+。

**结构完整性：**

目录树已定义到主要文件级别，包含源码、测试、资源和文档位置。未来 AI 接入暂不建目录，避免扩大 MVP 范围。

**模式完整性：**

已覆盖命名、文件组织、时间轴格式、错误格式、状态通信、加载状态、错误恢复和音频入口输出规则。

### 缺口分析结果

**关键缺口：** 无。

**重要缺口：**

- Xcode 工程尚未实际创建，项目结构目前是目标结构而非已存在工程。
- 真实 iPad 设备最低系统版本需要在实施前根据演示设备确认。
- 宠物资产最终采用 SwiftUI Canvas、Rive 还是 Lottie，可在实现第一版视觉时再定，但不得改变嘴型和状态机契约。

**可延后增强：**

- 真实 AI 对话 adapter。
- 云端 TTS / ASR / 音素级对齐。
- 实时麦克风流式分析。
- TestFlight、Xcode Cloud、App Store 分发。
- 正式 Rive / Lottie 资产管线。

### 验证问题处理

本轮验证未发现阻塞实现的问题。已补充工具链版本基线，避免“当前稳定版本”表述过于模糊。

### 架构完整性清单

**需求分析**

- [x] 项目上下文已充分分析
- [x] 规模和复杂度已评估
- [x] 技术约束已识别
- [x] 跨领域关注点已映射

**架构决策**

- [x] 关键决策已记录并补充版本基线
- [x] 技术栈已完整指定
- [x] 集成模式已定义
- [x] 性能考虑已覆盖

**实施模式**

- [x] 命名约定已建立
- [x] 结构模式已定义
- [x] 通信模式已指定
- [x] 流程模式已记录

**项目结构**

- [x] 完整目录结构已定义
- [x] 组件边界已建立
- [x] 集成点已映射
- [x] 需求到结构映射已完成

### 架构就绪评估

**整体状态：** READY FOR IMPLEMENTATION

**信心等级：** 高

**关键优势：**

- 平台方向明确为 iPadOS 原生，不再混用网页架构。
- 所有音频入口统一输出 `LipSyncTimeline`，后续扩展清晰。
- 单一状态源降低宠物动作、嘴型、播放和调试台不同步的风险。
- MVP 范围克制，真实 AI、云端能力和实时流式分析被明确延后。
- 目录结构和强制规则足够指导 AI Agent 一致实现。

**后续增强方向：**

- 引入真实 AI 对话 adapter。
- 替换更高精度 TTS / phoneme timing。
- 建立正式动画资产管线。
- 增加自动化 UI 回归和真机测试矩阵。

### 实施交接

**AI Agent 指南：**

- 严格遵守本文档的架构决策、目录结构和实施模式。
- 不新增第二套状态机、嘴型枚举或时间轴结构。
- SwiftUI View 只发用户意图，不直接操作 AVFoundation。
- 上传和麦克风入口必须保持实验质量标识。
- 所有实现问题优先回到本文档寻找边界答案。

**第一实施优先级：**

创建 Xcode iOS App 项目 `DigitalDog`，选择 SwiftUI、Swift、启用 Tests，然后先实现 `SharedModels`、`State` 和 `LipSync` 的纯逻辑基础。
