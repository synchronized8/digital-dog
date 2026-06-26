---
stepsCompleted: [1, 2, 3, 4, 5, 6, 7, 8]
inputDocuments:
  - docs/planning-artifacts/prds/prd-digital-dog-2026-06-23/prd.md
  - docs/planning-artifacts/epics.md
  - docs/MVP-SPEC.md
  - docs/UI-SPEC.md
workflowType: 'architecture'
lastStep: 8
status: 'complete'
completedAt: '2026-06-24'
lastOptimizedAt: '2026-06-25'
project_name: '数字狗可爱讲话动画 Demo'
user_name: 'kejincheng'
date: '2026-06-25'
---

# 架构决策文档：数字狗可爱讲话动画 Demo

## 当前架构口径

当前 MVP 已从“精确口型同步 Demo”简化为“可爱小狗讲话动画 UI”。本文档是当前执行版架构，不再以旧 iPadOS / SwiftUI / AVFoundation 方案或旧 `LipSyncTimeline` 强制路径为准。

第一版架构目标：

- Android 原生平板应用。
- Kotlin + Jetpack Compose。
- Redmi Pad SE 11 英寸 MIUI / Android 平板横屏优先、竖屏可用。
- 单一主状态源管理宠物状态、输入来源、嘴巴状态、错误和动画提示。
- `speaking` 时嘴巴张口或轻微开合，非 `speaking` 时闭口。
- 默认 UI 只包含宠物舞台、文本入口、示例入口和轻量状态摘要。

当前 MVP 不实现：

- 强制 `LipSyncTimeline` 播放路径。
- 波形、嘴型时间轴、文本高亮、延迟校准。
- 上传音频、麦克风录音、音频解码分析。
- 真实 AI、云端 TTS、ASR、账号、历史记录或服务端。

历史存在的 `LipSyncTimeline`、7 类嘴型或调试相关代码已从当前 production 路径移除或收敛为简单模型，不得成为当前 MVP 新功能的依赖。

## 技术基线

| 领域 | 决策 |
| --- | --- |
| 平台 | Android 原生平板应用 |
| 目标设备 | Redmi Pad SE 11 英寸，1920 x 1200 / 16:10 横屏优先 |
| 语言 | Kotlin |
| UI | Jetpack Compose |
| 状态 | 单一 `SpeechDemoStore` 或等价主状态源 |
| 音频 | 当前 MVP 可使用模拟播放时长或系统 TTS 触发流程，但嘴巴不依赖精确音频时钟 |
| 数据持久化 | MVP 不引入数据库；如需偏好项，可使用 DataStore / SharedPreferences |
| 测试 | Kotlin/JVM 单元测试、Compose UI Test、必要的 Android 仪器化测试 |
| 可访问性 | TalkBack、字体缩放、48dp 触控目标、Android 动画缩放 / 减少动态 |

## 核心模型

当前 MVP 需要的模型应保持小而稳定：

```kotlin
enum class PetState {
    Idle,
    Thinking,
    Speaking,
    Done,
    Error,
    Listening, // future AI reserved
}

enum class InputSource {
    None,
    Text,
    Sample,
}

enum class MouthMode {
    Closed,
    Talking,
}

enum class DogActionCue {
    None,
    EarPerk,
    HeadTilt,
    TailWag,
    Blink,
    BodyBounce,
}

data class SpeechAnimationState(
    val isSpeaking: Boolean,
    val mouthMode: MouthMode,
    val actionCue: DogActionCue,
    val estimatedDurationMs: Int,
)

data class DemoUiState(
    val petState: PetState,
    val inputSource: InputSource,
    val mouthMode: MouthMode,
    val actionCue: DogActionCue,
    val text: String,
    val isBusy: Boolean,
    val errorMessage: String?,
    val reduceMotion: Boolean,
)
```

兼容说明：

- 如果当前代码已有 `MouthShape`，可用 `closed` 和一个现有开口形态映射当前 `MouthMode`。
- 如果当前代码已有 `SpeechSession`，当前 MVP 只需要表达“是否有讲话流程运行”和“估算时长”。
- `LipSyncTimeline`、`LipSyncSegment`、`TimelineQuality`、`DebugSnapshot` 不作为当前 MVP 必需模型。

## 状态规则

核心规则必须集中在 reducer/store 层，Compose UI 只发送用户意图。

用户意图：

- `submitText(text: String)`
- `playSample()`
- `stopSpeech()` 或播放中禁用主入口
- `dismissError()` 或编辑文本后恢复
- `onSpeechAnimationFinished()`

状态流：

```text
idle
  -> thinking/preparing
  -> speaking
  -> done
  -> idle
```

错误流：

```text
idle/thinking/speaking
  -> error
  -> idle
```

嘴巴规则：

- `petState == Speaking` 时，`mouthMode = Talking`。
- 其他状态下，`mouthMode = Closed`。
- 空文本、错误、完成反馈结束后必须闭口。
- 当前 MVP 不通过播放时间、时间轴或延迟偏移计算嘴巴。

忙碌规则：

- `isBusy == true` 时，不创建第二个讲话流程。
- 主 CTA 禁用、显示 `讲话中`，或切换成停止动作。
- 示例入口与文本入口共享同一 busy 保护。

## 模块边界

建议目录结构：

```text
DigitalDog/
  app/
    src/main/java/com/digitaldog/demo/
      MainActivity.kt
      AppRoot.kt
      design/
        ColorTokens.kt
        TypeTokens.kt
        SpacingTokens.kt
      state/
        DemoUiState.kt
        SpeechDemoStore.kt
        SpeechDemoReducer.kt
      petstage/
        PetStage.kt
        DogRenderer.kt
        DogAnimationPolicy.kt
      input/
        SpeechInputPanel.kt
        SampleSpeechProvider.kt
      status/
        StatusBar.kt
        StatusSummary.kt
        ErrorFeedback.kt
      accessibility/
        AccessibilityLabels.kt
        ReduceMotionPolicy.kt
    src/test/java/com/digitaldog/demo/
      state/
        SpeechDemoReducerTest.kt
        MouthRuleTest.kt
        ScopeBoundaryTest.kt
    src/androidTest/java/com/digitaldog/demo/
      DigitalDogAppTest.kt
```

边界规则：

- UI 组件只读取 `DemoUiState` 并发送事件。
- `DogRenderer` 只消费 `petState`、`mouthMode`、`actionCue` 和 `reduceMotion`。
- 文本和示例入口只创建讲话意图，不直接修改嘴巴。
- 错误文案由 state 层统一输出，UI 不自行推断底层错误。
- 默认 UI 不引用上传、麦克风、波形、时间轴或延迟校准组件。

## UI 架构

Compose 层建议拆分：

- `AppRoot`：连接 store 和页面布局。
- `StatusBar`：显示 demo 名称、当前状态、输入来源、嘴巴状态。
- `PetStage`：承载小狗舞台、项圈状态和动作表现。
- `DogRenderer`：绘制或组合小狗形象。
- `SpeechInputPanel`：文本输入、主 CTA、示例入口。
- `StatusSummary`：轻量状态摘要。
- `ErrorFeedback`：靠近输入区的错误与恢复提示。

布局：

- 横屏：顶部状态栏 + 大宠物舞台 + 底部输入区/状态摘要。
- 竖屏：顶部状态栏 + 宠物舞台 + 输入区 + 状态摘要。
- 播放中不改变主要组件尺寸，避免小狗舞台和输入区跳动。

## 动画策略

动画应由 `DogActionCue` 和 `PetState` 驱动。

- `Idle`：闭口、轻微呼吸、偶尔眨眼。
- `Thinking`：闭口、耳朵竖起、看向用户或轻微歪头。
- `Speaking`：讲话嘴或轻微开合循环，配合低幅度点头、耳朵轻弹、身体律动或尾巴小摆。
- `Done`：闭口，一次短暂眨眼、摇尾或开心表情。
- `Error`：闭口，疑惑或歪头，项圈提示黄。

减少动态：

- 保留讲话嘴开合。
- 降低耳朵、头部、身体和尾巴装饰动作。
- 禁止用大幅位移表达状态。

## 输入与播放策略

当前 MVP 可以用简单的估算时长驱动讲话流程：

- 文本路径：根据文本长度估算讲话持续时间，或使用固定演示时长。
- 示例路径：使用预设短句和固定演示时长。
- 讲话流程结束后触发 `done`，短反馈后回到 `idle`。

当前 MVP 不需要：

- 音频解码。
- PCM 分析。
- 播放时钟对齐。
- viseme/phoneme 估算。
- 文本高亮。
- 延迟校准。

## 错误处理

当前错误类型：

- 空文本。
- 播放中重复触发。
- 内部演示流程失败。

错误规则：

- 错误状态下小狗闭口。
- 错误文案靠近对应入口。
- 错误不阻塞后续文本或示例操作。
- 旧的麦克风权限拒绝、上传不可解码、音频解析失败属于 Deferred，不进入当前默认错误面。

## 测试策略

必须覆盖：

- 非空文本进入讲话流程。
- 空文本进入 `error` 且不创建讲话流程。
- busy 状态不创建第二个讲话流程。
- `Speaking` 时嘴巴为 `Talking`。
- 非 `Speaking` 状态嘴巴为 `Closed`。
- `Done` 后回到 `Idle` 和 `Closed`。
- 减少动态时仍保留讲话嘴。
- 默认 UI 不出现上传、麦克风、波形、时间轴、文本高亮、延迟校准。
- TalkBack 语义包含当前状态、输入来源、嘴巴状态和错误原因。

测试类型：

- Reducer / store 纯逻辑测试。
- Compose UI 语义测试。
- Redmi Pad SE 横竖屏手动或截图验收。

## 安全与隐私

当前 MVP 不请求麦克风权限，不打开系统文件选择器，不上传数据到服务器。

如未来恢复上传或录音能力，必须重新补充权限、隐私、数据生命周期和错误恢复架构。

## 实施顺序

1. 固定 `DemoUiState`、`PetState`、`InputSource`、`MouthMode` 和 `DogActionCue`。
2. 实现 `SpeechDemoStore` / reducer 规则。
3. 收敛默认 UI：状态栏、宠物舞台、输入区、示例入口、状态摘要。
4. 实现 `DogRenderer` 的闭口和讲话嘴。
5. 加入准备、讲话中、完成和错误动作提示。
6. 补齐空文本、busy、开闭口和范围边界测试。
7. 做 Redmi Pad SE 横竖屏、TalkBack 和减少动态验收。

## Deferred Architecture Backlog

以下架构能力来自旧方案，不属于当前 MVP：

- `LipSyncTimeline` 作为所有入口统一输出。
- `LipSyncSegment`、`TimelineQuality`、`DebugSnapshot` 作为强制模型。
- 当前嘴型由播放时间、时间轴和 `latencyOffsetMs` 计算。
- TTS 文本到嘴型时间轴生成。
- 波形、嘴型时间轴、文本高亮和延迟校准。
- 上传音频、麦克风录音、音频解码、PCM 分析。
- 实验质量分级和完整调试台。
- 真实 AI adapter、ASR、云端 TTS 或实时流式分析。

恢复这些能力前，必须先更新 PRD、MVP、UI、Epic 和架构文档，再进入实施。
