# Story 2.2：TTS 文本到嘴型时间轴生成

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a 开发者,
I want 从用户输入文本生成统一的 `LipSyncTimeline`,
so that 数字狗嘴型、文本高亮和后续调试台能共享同一套时序数据.

## Acceptance Criteria

1. Given 用户提交非空中文或英文文本, When 系统生成 TTS 时间轴, Then 输出 `LipSyncTimeline`, 包含 `source=tts`、`quality=stable`、`durationMs`、`segments` 和 `latencyOffsetMs`, And 每个 segment 使用 `startMs`、`endMs`、`mouth`、`source`、`confidence` 和可选 `textRange`.
2. Given 文本包含中文字符, When 系统映射嘴型, Then 可按拼音或降级规则生成 `small`、`wide`、`round`、`smile`、`teeth`、`closed` 等嘴型, And 无法判断时降级到 `small`, 不生成未知嘴型.
3. Given 文本包含英文单词或常见字母组合, When 系统映射嘴型, Then 可按启发式规则识别闭唇、齿音、圆嘴、大开口和咧嘴倾向, And 其他有声段降级到 `small`.
4. Given 文本包含逗号、句号、问号、感叹号或换行, When 系统生成时间轴, Then 标点和停顿生成 `closed` 片段, And 停顿片段可被文本高亮逻辑识别为不高亮或停顿.
5. Integration: 非空 TTS submit 必须把生成的 `LipSyncTimeline` 存入当前 `SpeechSession` 或等价单一状态源, 并保持准备态嘴型为 `closed`; blank/error/busy 路径不得生成或替换 timeline.
6. Scope boundary: 本 Story 不实现真实 Android `TextToSpeech` 发声、音频播放、播放时钟、当前嘴型解析、相邻片段合并、最短保持和平滑、文本高亮 UI、波形、时间轴 UI、示例音频、上传、麦克风、权限、网络、真实 AI、数据库或新三方依赖.
7. Validation: `:app:testDebugUnitTest`, `:app:assembleDebug`, `:app:compileDebugAndroidTestKotlin`, `:app:lintDebug` 必须通过；如当前环境有可用模拟器或设备, 运行并记录 `:app:connectedDebugAndroidTest`.

## Tasks / Subtasks

- [x] 定义统一时间轴数据结构 (AC: 1)
  - [x] 新增 `LipSyncTimeline`、`LipSyncSegment` 和 `TextRange` 等价 Kotlin data class；建议放在 `DigitalDog/app/src/main/java/com/digitaldog/demo/sharedmodel/`, 以便 TTS、示例、上传、麦克风后续复用.
  - [x] `LipSyncTimeline` 至少包含 `source: InputSource`、`durationMs: Int`、`latencyOffsetMs: Int`、`segments: List<LipSyncSegment>`、`quality: TimelineQuality` 和 `generatedBy` 等价字段.
  - [x] `LipSyncSegment` 至少包含稳定 `id`、`startMs`、`endMs`、`mouth: MouthShape`、`source: InputSource`、`confidence: Double`、可选 `textRange` 和可选 `reason`.
  - [x] 文本范围使用 Kotlin `String` 的 start-inclusive/end-exclusive offset, 并保存对应 `text`; 如命名为 `TextRange`, 注意不要与 Compose `TextRange` 混淆.
  - [x] 扩展 `TimelineQuality` 以支持 `Stable`/`stable`; 保留当前 `Ready` 语义, 不破坏首屏和 Story 2.1 测试.
  - [x] 增加数据模型不变量测试: 非负时间、`startMs < endMs`、segment 单调不重叠、`durationMs` 等于最后一个 segment 的 `endMs` 或空时间轴为 0.
- [x] 实现 TTS 文本 tokenization 与时长估算 (AC: 1, 4)
  - [x] 新增 `TtsTimelineGenerator` 或等价类, 建议放在 `DigitalDog/app/src/main/java/com/digitaldog/demo/lipsync/`.
  - [x] 清理输入: trim 外围空白, 折叠连续空白, 保留中文、英文、数字、常见标点和换行停顿; blank 输入防御性返回空 timeline 或等价无效输入结果, 不崩溃.
  - [x] 中文按字符或轻量 token 切分, 英文按单词/字母组合切分, 数字按普通 token 或可读拆分处理; 不引入拼音、NLP、phoneme 或发音字典依赖.
  - [x] 时长估算遵守规格范围: 中文单字约 180-240ms, 英文短词约 180-260ms, 普通词约 280-420ms, 长词约 420-680ms, 短停顿约 160-240ms, 句末停顿约 260-420ms.
  - [x] `latencyOffsetMs` 默认 0; 不在本 Story 实现延迟校准控件或播放指针.
- [x] 实现中文嘴型映射 (AC: 2)
  - [x] 优先使用内置轻量字典覆盖规格示例和常见评审短句, 例如 `狗 -> small/round`、`说 -> teeth/round`、`话 -> small/wide`、`米 -> closed/smile`; 字典缺失时按字符启发式降级.
  - [x] 中文 token 可拆为 initial/final 等价子片段; initial 约 25%-35%, final 约 65%-75%; 无法判断 final 时生成 `small`.
  - [x] 允许产生 `closed`、`small`、`wide`、`round`、`smile`、`teeth`; 不把 `pant` 用作普通语音嘴型.
  - [x] 任意未知中文字符不得生成 unknown/null 嘴型, 也不得抛异常.
- [x] 实现英文嘴型映射 (AC: 3)
  - [x] 用字母和常见组合启发式识别闭唇 (`m/b/p`)、齿音 (`f/v/th/s/z/sh/ch`)、圆嘴 (`oo/o/u/w/ou`)、大开口 (`a/ah/ar`) 和咧嘴 (`e/ee/i/y`).
  - [x] 英文单词可拆成 2-4 个子片段: 起始辅音或组合、主元音、尾音、词间短闭口; 其他有声段降级到 `small`.
  - [x] 混合大小写输入应产生相同映射结果.
  - [x] 英文映射测试至少覆盖 `my`/`baby`/`puppy`, `fish`/`this`/`see`, `you`/`woof`/`hello`, `happy`/`bark`, `tiny` 等代表性模式.
- [x] 实现标点与停顿片段 (AC: 4)
  - [x] `,`、`，`、`、` 生成短 `closed` 停顿; `.`、`。`、`?`、`？`、`!`、`！` 和换行生成较长 `closed` 停顿.
  - [x] 停顿 segment 的 `textRange` 可为空, 或标记到标点字符并提供 `reason`; 必须让后续文本高亮能识别为停顿/不高亮.
  - [x] 标点不能生成有声嘴型, 不能打乱后续 token 的 text range.
- [x] 接入当前 TTS session 单一状态源 (AC: 1, 5)
  - [x] 扩展 `SpeechSession` 以保存生成的 `LipSyncTimeline` 或等价 timeline 字段; 不新增第二套 session 类型或第二套状态源.
  - [x] 在 `TtsSubmitReducer.submitText` 的非空路径生成 timeline 并存入 session; `petState` 仍为 `Thinking`, `currentMouth` 仍为 `MouthShape.Closed`, `inputSource` 为 `InputSource.Tts`, `timelineQuality` 更新为 `Stable` 或等价稳定质量.
  - [x] 空文本错误路径不得创建 session 或 timeline; busy guard 不得替换现有 session/timeline.
  - [x] `SpeechInputPlaceholder`、`DigitalDogApp`、宠物舞台和调试区仍只通过 state/reducer 通信; View 不直接调用 generator 或写嘴型.
- [x] 更新测试与边界保护 (AC: 1-7)
  - [x] 新增 JVM tests: `LipSyncTimelineTest`、`TtsTimelineGeneratorTest` 或等价文件, 覆盖结构不变量、中文映射、英文映射、标点停顿、混排文本、blank 防御和 deterministic 输出.
  - [x] 扩展 `TtsSubmitReducerTest`, 验证非空提交的 session 持有 `source=tts`、`quality=stable`、`latencyOffsetMs=0`、非空 segments 和稳定 `durationMs`; blank/busy 路径不生成或不替换 timeline.
  - [x] 更新 `StoryTwoOneScopeBoundaryTest`: Story 2.2 开始 `LipSyncTimeline` 是合法能力, 不能继续作为 2.1 禁用词导致新代码失败.
  - [x] 新增 `StoryTwoTwoScopeBoundaryTest` 或等价扫描, 禁止本 Story 引入 `TextToSpeech.speak`、Android TTS engine 初始化、`MediaPlayer`/`AudioTrack`/Media3、播放时钟/current mouth resolver、文本高亮 UI、波形/时间轴 UI、上传、麦克风权限、网络、数据库和新依赖.
  - [x] 运行完整本地验证命令并记录结果；如 `adb devices` 存在可用设备, 运行 connected Android test 并记录设备名.
- [x] 更新实施记录
  - [x] 更新 Dev Agent Record 的 Debug Log、Completion Notes、File List 和 Change Log.
  - [x] 如果实现过程中选择了不同文件名、字段名或 timeline 存放位置, 在 Dev Agent Record 中说明等价关系和原因.

### Review Findings

- [x] [Review][Patch] `textRange` can point at the wrong `SpeechSession.text` when submitted text contains repeated spaces or newlines [DigitalDog/app/src/main/java/com/digitaldog/demo/state/TtsSubmitReducer.kt:31]
- [x] [Review][Patch] Supplementary Unicode characters are split into surrogate-fragment fallback tokens [DigitalDog/app/src/main/java/com/digitaldog/demo/lipsync/TtsTimelineGenerator.kt:84]
- [x] [Review][Patch] Common punctuation outside the explicit list becomes voiced `small` mouth text [DigitalDog/app/src/main/java/com/digitaldog/demo/lipsync/TtsTimelineGenerator.kt:94]
- [x] [Review][Patch] Timeline/session model invariants allow invalid shared-contract data [DigitalDog/app/src/main/java/com/digitaldog/demo/sharedmodel/LipSyncTimeline.kt:50]
- [x] [Review][Patch] Story 2.2 scope boundary misses text-highlight/waveform/upload and generic new-dependency creep [DigitalDog/app/src/test/java/com/digitaldog/demo/StoryTwoTwoScopeBoundaryTest.kt:18]

## Dev Notes

### 当前 Story 目标

Story 2.2 是 Epic 2 的 timeline 契约 Story: 把 Story 2.1 已创建的 TTS 文本/session 转成统一 `LipSyncTimeline`, 为 Story 2.3 的平滑与当前嘴型计算、Story 2.4 的播放同步与文本高亮、Story 3 的调试时间轴打数据基础。

本 Story 只生成初始时间轴。相邻相同嘴型合并、最短保持时间、嘴型切换频率限制、`effectiveTimeMs` 当前嘴型解析、真实 TTS 播放、文本高亮 UI 和调试时间轴 UI 都属于后续 Story。

### Story 2.2 需求来源

- `docs/planning-artifacts/epics.md`：Story 2.2 明确要求非空中英文文本输出 `LipSyncTimeline`, 包含 `source=tts`、`quality=stable`、`durationMs`、`segments`、`latencyOffsetMs`, segment 包含 `startMs`、`endMs`、`mouth`、`source`、`confidence` 和可选 `textRange`.
- `docs/LIPSYNC-SPEC.md`：定义 7 类嘴型、timeline 数据结构、TTS 文本映射流程、中文/英文启发式映射、标点停顿和解析质量规则.
- `docs/planning-artifacts/prds/prd-digital-dog-2026-06-23/prd.md`：FR-10 要求 TTS 输入生成嘴型时间轴；FR-11 后续文本高亮依赖 text range；FR-13 的平滑/闭口规则属于后续处理, 不在本 Story 提前实现.
- `docs/planning-artifacts/architecture.md`：所有输入入口必须统一输出 `LipSyncTimeline`, 时间单位为毫秒, TTS/示例/上传/麦克风不得维护私有时间轴结构.
- `docs/planning-artifacts/platform-decision-ipad-display.md`：当前实现平台是 Redmi Pad SE / MIUI Android / Kotlin / Jetpack Compose / TalkBack, 不是旧版 iPadOS/Swift.

### 当前工程状态

- 当前工程位于 `DigitalDog/app`, 使用 Kotlin、Jetpack Compose、Material3、JUnit4 和 Android Compose Test.
- 当前没有真实 Android `TextToSpeech` 发声、音频播放、上传、麦克风、权限、网络、数据库或后端.
- `MouthShape` 已支持 7 类嘴型: `Closed`, `Small`, `Wide`, `Round`, `Smile`, `Teeth`, `Pant`; 不要新增第二套枚举.
- `InputSource` 当前包含 `None`, `Manual`, `Tts`; Story 2.2 只需要 `Tts`.
- `TimelineQuality` 当前只有 `Ready`; Story 2.2 需要增加 `Stable` 或等价稳定质量, 并保持首屏 ready 文案和测试不回归.
- `DigitalDog/app/src/main/java/com/digitaldog/demo/lipsync/` 已存在, 适合放置 TTS timeline generator.

### 现有代码状态

- `DigitalDog/app/src/main/java/com/digitaldog/demo/sharedmodel/SpeechSession.kt`
  - 当前字段: `id`, `source`, `text`, `status`.
  - 本 Story 应在此处或等价 session 模型中挂载 `LipSyncTimeline`; 不要另建互相竞争的 TTS session.
- `DigitalDog/app/src/main/java/com/digitaldog/demo/state/SpeechDemoState.kt`
  - 当前字段包含 `ttsInputText`, `inputError`, `activeSpeechSession`, `nextSpeechSessionIndex`.
  - 本 Story 可通过 active session 暴露 timeline, 并在成功提交时把 `timelineQuality` 更新到稳定质量.
- `DigitalDog/app/src/main/java/com/digitaldog/demo/state/TtsSubmitReducer.kt`
  - 当前非空 submit 会 trim 文本、创建 `SpeechSession`, 进入 `PetState.Thinking`, 嘴型保持 `Closed`.
  - 本 Story 应把 timeline generation 放在 reducer/use-case 层, 不是 Compose `onClick` 层.
- `DigitalDog/app/src/main/java/com/digitaldog/demo/speechinput/SpeechInputPlaceholder.kt`
  - 当前只负责文本输入和按钮回调.
  - 本 Story 不需要改 UI 文案或新增时间轴可视化.
- `DigitalDog/app/src/test/java/com/digitaldog/demo/StoryTwoOneScopeBoundaryTest.kt`
  - 当前把 `LipSyncTimeline` 列为禁用 token.
  - 这会在 Story 2.2 合法新增 timeline 后造成误报, 必须调整.

### 架构合规要求

- 使用 Kotlin + Jetpack Compose 项目现有结构；不新增三方依赖.
- 时间单位统一为毫秒; 所有 segment 时间必须非负、单调、不可重叠.
- `LipSyncTimeline` 是 TTS、示例、上传、麦克风后续共享的数据契约, 不要为 TTS 私有化字段或结构.
- 普通语音不得使用 `Pant`; `Pant` 只留给宠物反馈/非语言表达.
- 生成 timeline 不代表开始播放: 当前嘴型仍由状态保持 `Closed`; Story 2.3/2.4 才允许根据 timeline 推进嘴型.
- 调试台、宠物舞台和输入区继续只读/触发状态意图, 不直接驱动 timeline 或嘴型.
- 旧架构文档中的 Swift/iPadOS 示例只作为字段契约参考, 实现时必须迁移为 Android/Kotlin.

### Suggested File Changes

建议新增:

- `DigitalDog/app/src/main/java/com/digitaldog/demo/sharedmodel/LipSyncTimeline.kt`
- `DigitalDog/app/src/main/java/com/digitaldog/demo/sharedmodel/LipSyncSegment.kt` 或与 timeline 同文件
- `DigitalDog/app/src/main/java/com/digitaldog/demo/sharedmodel/TextRange.kt` 或 `LipSyncTextRange.kt`
- `DigitalDog/app/src/main/java/com/digitaldog/demo/sharedmodel/TimelineGenerator.kt`
- `DigitalDog/app/src/main/java/com/digitaldog/demo/lipsync/TtsTimelineGenerator.kt`
- `DigitalDog/app/src/test/java/com/digitaldog/demo/sharedmodel/LipSyncTimelineTest.kt`
- `DigitalDog/app/src/test/java/com/digitaldog/demo/lipsync/TtsTimelineGeneratorTest.kt`
- `DigitalDog/app/src/test/java/com/digitaldog/demo/StoryTwoTwoScopeBoundaryTest.kt`

建议更新:

- `DigitalDog/app/src/main/java/com/digitaldog/demo/sharedmodel/SpeechSession.kt`
- `DigitalDog/app/src/main/java/com/digitaldog/demo/sharedmodel/TimelineQuality.kt`
- `DigitalDog/app/src/main/java/com/digitaldog/demo/state/TtsSubmitReducer.kt`
- `DigitalDog/app/src/test/java/com/digitaldog/demo/state/TtsSubmitReducerTest.kt`
- `DigitalDog/app/src/test/java/com/digitaldog/demo/StoryTwoOneScopeBoundaryTest.kt`

如果开发者选择不同文件名, 必须保持包边界和行为等价, 并在 File List 中完整记录.

### Testing

- JVM unit test:
  - `LipSyncTimeline` 空/非空不变量.
  - 中文文本如 `狗说话` 或 `米狗说话` 生成稳定 timeline, 覆盖 `small`, `round`, `wide`, `teeth`, `smile` 中至少多个嘴型, 且 unknown 字符降级到 `small`.
  - 英文文本如 `my happy puppy says woof` 覆盖闭唇、wide、round、smile、teeth 或对应启发式输出.
  - 标点和换行生成 `closed` 停顿, 且停顿可通过空 `textRange` 或 `reason` 被后续高亮逻辑识别.
  - 混排文本的 `textRange` 不越界, 不跳过有声 token, 不产生负时长.
  - `TtsSubmitReducer` 成功提交后 session 持有 timeline, blank/busy 路径不生成或不替换 timeline.
  - manual mouth 现有 reducer 行为不回归; active TTS session 仍不能被手动嘴型破坏.
- Scope boundary:
  - 允许 `LipSyncTimeline`, 禁止真实 TTS/audio/playback/UI/debug timeline/permissions/network/db/new dependency.
- 验证命令:
  - `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk ./gradlew :app:testDebugUnitTest`
  - `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk ./gradlew :app:assembleDebug`
  - `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk ./gradlew :app:compileDebugAndroidTestKotlin`
  - `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk ./gradlew :app:lintDebug`
  - 如 `adb devices` 存在可用设备: `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk ./gradlew :app:connectedDebugAndroidTest`

### Previous Story Intelligence

- Story 2.1 已完成并通过 review 补丁:
  - `SpeechSession`, `SpeechSessionStatus`, `SpeechDemoState` TTS 字段和 `TtsSubmitReducer` 已建立.
  - 非空 TTS submit 当前进入 `thinking`, 嘴型 `closed`, 输入来源 `Tts`, 并创建唯一 session id.
  - 空输入错误是可恢复状态, 用户编辑文本可清除错误并重试.
  - active TTS session 会阻止手动嘴型 mutation; Story 2.2 不得让 timeline 接入破坏该保护.
  - `StoryTwoOneScopeBoundaryTest` 的禁用词扫描更严格, 但其 `LipSyncTimeline` 禁用词在 Story 2.2 已过期, 需要同步调整.
  - full local validation 已通过 `:app:testDebugUnitTest`, `:app:assembleDebug`, `:app:compileDebugAndroidTestKotlin`, `:app:lintDebug`.
  - `connectedDebugAndroidTest` 当时未运行, 因为 `adb devices` 无连接设备.
- Story 1.4/1.5 仍是重要保护:
  - 7 类嘴型已有稳定 enum、渲染和手动测试语义.
  - `TagPetStage`, `TagPetFigure`, `TagDogMouth`、motion policy 和 reduced/static 路径不应因 timeline 接入退化.

### Anti-Patterns

- 不要新增第二套 `MouthShape`, `InputSource`, `TimelineQuality` 或 TTS session.
- 不要让 generator 直接更新 `currentMouth`; timeline generation 不是 playback.
- 不要在本 Story 调用 Android `TextToSpeech`, 初始化 TTS engine, 播放音频或申请音频权限.
- 不要实现 `CurrentMouthResolver`, `PlaybackClock`, 文本高亮 UI、波形 UI 或时间轴 UI.
- 不要实现 Story 2.3 的相邻合并、最短保持和平滑规则；如需标注, 明确写为由后续 Story 2.3 处理.
- 不要新增拼音库、发音字典、NLP、Media3、网络、数据库或真实 AI 依赖.
- 不要把上传、麦克风或内置示例路径塞入本 Story.
- 不要把旧文档中的 Swift/AVFoundation/iPad 代码当成实现依据.

### Latest Technical Notes

- Kotlin `String` 使用 UTF-16 code unit 索引; `textRange` 若服务于 Compose 文本高亮, 应保持 start-inclusive/end-exclusive offset 并用测试覆盖中文、英文和混排边界.
- Kotlin stdlib 已提供字符分类能力, 例如 `Char.isLetterOrDigit()` 和 Unicode category 判断; 本 Story 的 tokenization 可用标准库完成, 不需要新增依赖.
- Android/Compose UI 不需要为本 Story新增 API; 现有 Material3 输入区只触发 reducer, timeline 生成应保持纯 Kotlin/JVM 可测试.

### References

- [Source: docs/planning-artifacts/epics.md#Story-2.2-TTS-文本到嘴型时间轴生成]
- [Source: docs/LIPSYNC-SPEC.md#时间轴数据结构]
- [Source: docs/LIPSYNC-SPEC.md#TTS-文本映射]
- [Source: docs/LIPSYNC-SPEC.md#中文映射]
- [Source: docs/LIPSYNC-SPEC.md#英文映射]
- [Source: docs/LIPSYNC-SPEC.md#标点与停顿]
- [Source: docs/planning-artifacts/prds/prd-digital-dog-2026-06-23/prd.md#FR-10-TTS-嘴型时间轴]
- [Source: docs/planning-artifacts/prds/prd-digital-dog-2026-06-23/prd.md#FR-11-TTS-文本高亮]
- [Source: docs/planning-artifacts/prds/prd-digital-dog-2026-06-23/prd.md#FR-13-口型平滑与闭口规则]
- [Source: docs/planning-artifacts/architecture.md#2026-06-24-平台纠偏Redmi-Pad-SE--MIUI-Android-平板]
- [Source: docs/planning-artifacts/architecture.md#格式模式]
- [Source: docs/planning-artifacts/platform-decision-ipad-display.md#决策]
- [Source: docs/implementation-artifacts/2-1-tts-主入口提交与空文本处理.md#Previous-Story-Intelligence]
- [Source: https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.text/is-letter-or-digit.html]
- [Source: https://kotlinlang.org/docs/characters.html]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk ./gradlew :app:testDebugUnitTest --tests com.digitaldog.demo.sharedmodel.LipSyncTimelineTest` failed before `LipSyncTimeline` model existed.
- GREEN: `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk ./gradlew :app:testDebugUnitTest --tests com.digitaldog.demo.sharedmodel.LipSyncTimelineTest` passed after shared timeline models and `TimelineQuality.Stable` were added.
- RED: `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk ./gradlew :app:testDebugUnitTest --tests com.digitaldog.demo.lipsync.TtsTimelineGeneratorTest` failed before `TtsTimelineGenerator` existed.
- GREEN: `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk ./gradlew :app:testDebugUnitTest --tests com.digitaldog.demo.lipsync.TtsTimelineGeneratorTest` passed after rule-based generator implementation.
- RED: `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk ./gradlew :app:testDebugUnitTest --tests com.digitaldog.demo.state.TtsSubmitReducerTest` failed before `SpeechSession.timeline` existed.
- GREEN: `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk ./gradlew :app:testDebugUnitTest --tests com.digitaldog.demo.state.TtsSubmitReducerTest` passed after TTS submit generated and stored timeline.
- Boundary: `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk ./gradlew :app:testDebugUnitTest --tests com.digitaldog.demo.StoryTwoOneScopeBoundaryTest --tests com.digitaldog.demo.StoryTwoTwoScopeBoundaryTest` initially failed due old boundary string filtering, then passed after filtering was updated.
- Regression: `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk ./gradlew :app:testDebugUnitTest` initially failed because Story 1.5 still forbade now-valid `LipSyncTimeline`, then passed after that obsolete token was removed.
- Validation: `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk ./gradlew :app:assembleDebug :app:compileDebugAndroidTestKotlin :app:lintDebug` passed.
- Device check: `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk /Users/kejincheng/Library/Android/sdk/platform-tools/adb devices` showed no attached devices, so `:app:connectedDebugAndroidTest` was not run.
- Review patch RED: `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk ./gradlew :app:testDebugUnitTest --tests com.digitaldog.demo.lipsync.TtsTimelineGeneratorTest --tests com.digitaldog.demo.sharedmodel.LipSyncTimelineTest --tests com.digitaldog.demo.state.TtsSubmitReducerTest --tests com.digitaldog.demo.StoryTwoTwoScopeBoundaryTest` failed before review patches, covering text/session mismatch, supplementary Unicode, common punctuation, invariant gaps, and boundary gaps.
- Review patch GREEN: same targeted command passed after patches.
- Review patch validation: `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk ./gradlew :app:testDebugUnitTest :app:assembleDebug :app:compileDebugAndroidTestKotlin :app:lintDebug` passed.
- Review patch device check: `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk /Users/kejincheng/Library/Android/sdk/platform-tools/adb devices` showed no attached devices, so `:app:connectedDebugAndroidTest` was not run after review patches.

### Completion Notes List

- Added shared `LipSyncTimeline`, `LipSyncSegment`, `LipSyncTextRange`, and `TimelineGenerator` models with invariant checks for non-negative monotonic timelines.
- Added `TimelineQuality.Stable` while preserving the existing `Ready` startup quality.
- Implemented `TtsTimelineGenerator` as a deterministic no-dependency text-rule generator for Chinese, English, digits-as-word tokens, punctuation pauses, blank input, stable `source=tts`, `quality=stable`, and `latencyOffsetMs=0`.
- Integrated generated timelines into the existing `SpeechSession` through `TtsSubmitReducer.submitText`; successful TTS submit remains `thinking` + `closed`, while blank and busy paths do not create or replace timelines.
- Updated stale scope boundary tests so Story 2.2 can legally introduce `LipSyncTimeline`, and added Story 2.2 boundary coverage for real TTS/audio/playback/UI/permission/network/database dependency creep.
- Implementation intentionally keeps smoothing, segment merging, current mouth playback, text highlight UI, audio playback, sample/upload/microphone paths, and real Android TTS out of scope for later stories.
- Review patches normalized submitted session text to the same text used for timeline ranges, kept supplementary Unicode characters as single fallback tokens, made common punctuation closed pauses, enforced shared model invariants, and strengthened Story 2.2 boundary coverage.

### File List

- DigitalDog/app/src/main/java/com/digitaldog/demo/lipsync/TtsTimelineGenerator.kt
- DigitalDog/app/src/main/java/com/digitaldog/demo/sharedmodel/LipSyncTimeline.kt
- DigitalDog/app/src/main/java/com/digitaldog/demo/sharedmodel/SpeechSession.kt
- DigitalDog/app/src/main/java/com/digitaldog/demo/sharedmodel/TimelineQuality.kt
- DigitalDog/app/src/main/java/com/digitaldog/demo/state/TtsSubmitReducer.kt
- DigitalDog/app/src/test/java/com/digitaldog/demo/StoryOneFiveScopeBoundaryTest.kt
- DigitalDog/app/src/test/java/com/digitaldog/demo/StoryTwoOneScopeBoundaryTest.kt
- DigitalDog/app/src/test/java/com/digitaldog/demo/StoryTwoTwoScopeBoundaryTest.kt
- DigitalDog/app/src/test/java/com/digitaldog/demo/lipsync/TtsTimelineGeneratorTest.kt
- DigitalDog/app/src/test/java/com/digitaldog/demo/sharedmodel/LipSyncTimelineTest.kt
- DigitalDog/app/src/test/java/com/digitaldog/demo/state/TtsSubmitReducerTest.kt
- docs/implementation-artifacts/2-2-tts-文本到嘴型时间轴生成.md
- docs/implementation-artifacts/sprint-status.yaml

### Change Log

- 2026-06-25: Implemented Story 2.2 TTS text-rule timeline generation, session integration, unit coverage, boundary tests, and validation updates.
- 2026-06-25: Addressed code review findings for text range alignment, Unicode tokenization, punctuation pauses, model invariants, and scope boundary coverage.
