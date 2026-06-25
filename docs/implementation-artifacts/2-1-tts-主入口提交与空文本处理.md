# Story 2.1：TTS 主入口提交与空文本处理

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a 设计评审者,
I want 输入一句话并点击“让狗狗说话”,
so that 我可以用最稳定的方式触发数字狗讲话演示.

## Acceptance Criteria

1. Given App 已完成宠物舞台和状态机基础, When 用户在 TTS 文本框输入非空文本并点击“让狗狗说话”, Then 系统接收文本并创建 TTS 播放意图, And 数字狗进入 `thinking` 或等价准备状态, And 嘴型保持 `closed`.
2. Given TTS 文本框为空或只包含空白字符, When 用户点击“让狗狗说话”, Then 不创建讲话 session, And 数字狗进入 `error`, And 嘴型保持 `closed`, And 输入区显示“先输入一句话”, And 用户可继续编辑文本并重试.
3. Given 当前已有 TTS 播放意图或讲话 session 进行中, When 用户再次触发主 CTA, Then 主 CTA 处于禁用或停止状态, And 不会创建互相冲突的第二个讲话 session.
4. Given 任一 TTS 提交、空文本错误或 busy guard 状态, When 顶部状态栏、宠物舞台和调试摘要刷新, Then 它们共享同一 `SpeechDemoState` 或等价单一状态源, 并同步显示当前状态、嘴型、输入来源和解析质量.
5. Given TalkBack 或 Compose UI Test 读取主输入区, When 输入、错误或 CTA 状态变化, Then 输入框、错误文案和主 CTA 均有稳定可读的文本或语义标签, 且 48dp 触控目标不退化.
6. Scope boundary: 本 Story 不实现真实 Android `TextToSpeech` 发声、音频播放、`LipSyncTimeline` 生成、嘴型播放时钟、文本高亮、波形、时间轴 UI、示例音频、上传、麦克风、权限、网络、真实 AI、数据库或新三方依赖.
7. Validation: `:app:testDebugUnitTest`, `:app:assembleDebug`, `:app:compileDebugAndroidTestKotlin`, `:app:lintDebug` 必须通过；如当前环境有可用模拟器或设备, 运行并记录 `:app:connectedDebugAndroidTest`.

## Tasks / Subtasks

- [x] 定义 TTS 提交状态模型与 reducer (AC: 1, 2, 3, 4)
  - [x] 扩展 `SpeechDemoState` 或新增等价模型字段, 表达 TTS 输入文本、输入区错误、当前 TTS 播放意图/session 和 busy 状态.
  - [x] 新增轻量 `SpeechSession`、`TtsPlaybackIntent` 或等价数据结构, 至少记录稳定 id、输入来源、清理后的文本和当前状态；不得在本 Story 存储音频、时间轴或播放时钟.
  - [x] 将 `InputSource` 扩展为包含 `TTS`/`Tts` 等稳定值, 文案显示为 `TTS`, 并保持 `None`、`Manual` 现有行为不变.
  - [x] 新增 `TtsSubmitReducer` 或等价 reducer, 处理 `TextChanged`、`SubmitText`、busy guard 和错误恢复；不要把业务规则写在 Compose `onClick` 内.
  - [x] 非空提交必须 trim 输入并创建唯一的播放意图/session, 将状态置为 `thinking` 或准备态, 嘴型保持 `MouthShape.Closed`, 输入来源置为 TTS.
  - [x] 空白提交必须不创建 session, 将状态置为 `error`, 嘴型保持 `MouthShape.Closed`, 并设置错误文案“先输入一句话”.
  - [x] 已有 session 或播放意图进行中时, 第二次提交不得替换或追加第二个冲突 session；如实现停止状态, 必须显式清理后才能重新提交.
- [x] 接入主输入 UI (AC: 1, 2, 3, 5)
  - [x] 将 `SpeechInputPlaceholder.kt` 从静态占位升级为可交互文本输入区, 使用 Compose/Material3 输入控件和现有设计系统 token.
  - [x] 主输入 placeholder 保持 `输入一句想让狗狗说的话`, 主 CTA 保持 `让狗狗说话`, 空输入错误保持 `先输入一句话`.
  - [x] 错误文案应靠近文本输入区, 可见且可被 TalkBack 读取；用户编辑文本后应能清除错误或至少进入可重试状态.
  - [x] 播放意图/session busy 时, 主 CTA 必须禁用或显示停止/讲话中状态, 并通过测试证明不会创建第二个 session.
  - [x] 示例、上传和麦克风入口仍保持占位/实验入口, 不在本 Story 接入真实行为.
  - [x] 横屏和竖屏 compact 布局都必须保持按钮文字不溢出、输入区不遮挡宠物舞台.
- [x] 将 TTS 意图接入 App 单一状态源 (AC: 1, 3, 4)
  - [x] 在 `DigitalDogApp.kt` 中通过回调把输入变更和主 CTA 提交发送给 reducer, 再用 reducer 输出更新 `currentUiState`.
  - [x] 不允许 `SpeechInputPlaceholder` 直接修改宠物状态、嘴型、输入来源或 session.
  - [x] 顶部状态栏、宠物舞台、横屏调试台和竖屏调试摘要必须从同一状态对象读取 `thinking`/`error`、`closed` 嘴型和 TTS 输入来源.
  - [x] 保留 Story 1.4/1.5 的手动嘴型测试、motion policy 注入、`TagPetStage`、`TagPetFigure`、`TagDogMouth` 和 reduced/static 测试路径.
- [x] 更新内容契约与可访问性语义 (AC: 2, 3, 4, 5)
  - [x] 在 `AppContentContract` 集中新增 TTS 输入 test tag、错误文案、CTA busy/disabled 文案、TTS 输入来源文案和必要 content description.
  - [x] 现有用户可见文案不得散落在测试或 Compose 实现中.
  - [x] 主 CTA 禁用、错误和输入区语义必须稳定, 便于 Compose UI Test 和 TalkBack 验收.
  - [x] 错误状态不能只依赖颜色表达；必须有可见文本和状态摘要.
- [x] 覆盖测试与验收 (AC: 1, 2, 3, 4, 5, 6, 7)
  - [x] 新增 JVM 单元测试覆盖非空提交、空白提交、busy guard、编辑后可重试、session/id 不重复创建和嘴型 closed 规则.
  - [x] 扩展 `PetStateReducerTest` 或新增 `TtsSubmitReducerTest`, 验证 TTS 输入来源、`thinking`/`error` 状态、错误文案和现有 manual mouth 行为互不回归.
  - [x] 扩展 Android Compose test 覆盖输入非空并点击 CTA 后顶部/舞台/调试摘要同步为 TTS + `thinking` + `closed`.
  - [x] 扩展 Android Compose test 覆盖空输入点击 CTA 显示“先输入一句话”、进入 `error`、嘴型 `closed`, 且编辑后可重试.
  - [x] 扩展 Android Compose test 覆盖 busy 时主 CTA 禁用或停止状态, 第二次触发不会创建第二个 session.
  - [x] 增加 scope boundary 测试或更新现有扫描, 确认本 Story 未引入真实 TTS 发声、音频播放、权限、上传、麦克风、`LipSyncTimeline` 生成、网络、数据库或新依赖.
  - [x] 运行完整本地验证命令并记录结果；如有连接设备, 运行 connected Android test 并记录设备名.
- [x] 更新实施记录
  - [x] 更新 Dev Agent Record 的 Debug Log、Completion Notes、File List 和 Change Log.
  - [x] 如果实现过程中选择了不同文件名或模型名, 在 Dev Agent Record 中说明等价关系和原因.

### Review Findings

- [x] [Review][Patch] Active TTS session can be corrupted by manual mouth controls [DigitalDog/app/src/main/java/com/digitaldog/demo/state/ManualMouthTestReducer.kt:12]
- [x] [Review][Patch] Recoverable TTS error can leak into manual mouth mode [DigitalDog/app/src/main/java/com/digitaldog/demo/state/ManualMouthTestReducer.kt:12]
- [x] [Review][Patch] Landscape instrumentation tests rely on unconstrained host viewport [DigitalDog/app/src/androidTest/java/com/digitaldog/demo/DigitalDogAppTest.kt:77]
- [x] [Review][Patch] TTS sync tests do not explicitly verify debug panel or summary semantics [DigitalDog/app/src/androidTest/java/com/digitaldog/demo/DigitalDogAppTest.kt:342]
- [x] [Review][Patch] Scope boundary test misses broader out-of-scope APIs and Java sources [DigitalDog/app/src/test/java/com/digitaldog/demo/StoryTwoOneScopeBoundaryTest.kt:23]
- [x] [Review][Patch] Scope boundary test skips every file named ScopeBoundaryTest [DigitalDog/app/src/test/java/com/digitaldog/demo/StoryTwoOneScopeBoundaryTest.kt:36]

## Dev Notes

### 当前 Story 目标

Story 2.1 是 Epic 2 的入口 Story, 目标是把 TTS 主入口从静态占位推进到可测试的提交意图: 非空文本可以创建一个本地 TTS 播放意图/session 并让数字狗进入 `thinking` 或准备状态；空白文本进入可恢复错误；busy 状态阻止第二个冲突 session。

本 Story 只建立后续 TTS 播放链路的入口和状态边界。真实 Android `TextToSpeech` 发声、文本到嘴型时间轴、当前嘴型播放计算、文本高亮和调试时间轴分别属于后续 Epic 2/3 stories。

### Story 2.1 需求来源

- `docs/planning-artifacts/epics.md`：Story 2.1 明确要求非空文本点击“让狗狗说话”后接收文本并创建 TTS 播放意图, 数字狗进入 thinking/准备状态且嘴型 closed；空文本不创建 session, 进入 error 并显示“先输入一句话”；播放中防止第二个冲突 session.
- `docs/planning-artifacts/prds/prd-digital-dog-2026-06-23/prd.md`：FR-5 要求文字转语音主入口；FR-13 要求思考和错误状态下嘴型为 closed；FR-17 要求空输入为可恢复错误.
- `docs/UI-SPEC.md`：主输入区包含文本输入、主按钮和输入状态提示；空输入进入 error；有文本进入 thinking 后续再 speaking；播放中 CTA 禁用或变为停止；文案为 `让狗狗说话`、`讲话中`、`先输入一句话`.
- `docs/MVP-SPEC.md`：Requirement 5 要求文本输入框和主按钮触发 TTS 或模拟 TTS 流程；Requirement 10 要求空文本点击主按钮不触发讲话并显示可恢复错误.
- `docs/planning-artifacts/platform-decision-ipad-display.md`：当前目标平台是 Redmi Pad SE / MIUI Android / Kotlin / Jetpack Compose / TalkBack, 不是旧版 iPadOS/Swift 实现.

### 当前工程状态

- 项目位于 `DigitalDog/app`, 使用 Kotlin、Jetpack Compose、Material3、JUnit4 和 Android Compose Test.
- 当前代码没有后端、数据库、网络、真实 TTS 发声、音频播放、上传、麦克风或权限链路.
- `docs/planning-artifacts/architecture.md` 顶部平台纠偏优先于后文 iPad/Swift 残留。实现和记录中如遇冲突, 以 Android/Redmi/Compose/TalkBack 为准.
- 当前状态源是 `SpeechDemoState`, `DigitalDogApp` 通过 `currentUiState` 组合顶部状态栏、宠物舞台、输入区和调试区.
- 调试面板和宠物舞台只应读取状态, 不应成为 TTS 提交或宠物状态的第二个来源.

### 现有代码状态

- `DigitalDog/app/src/main/java/com/digitaldog/demo/speechinput/SpeechInputPlaceholder.kt`
  - 当前主输入区是静态占位: 文本框只是 `Box + Text`, 主 CTA、示例、上传、麦克风按钮的 `onClick = {}`.
  - 本 Story 应在此处接入真实文本输入和回调参数, 但业务规则仍应留在 reducer/state 层.
- `DigitalDog/app/src/main/java/com/digitaldog/demo/app/DigitalDogApp.kt`
  - 当前用 `remember(uiState) { mutableStateOf(uiState) }` 保存 `currentUiState`, 手动嘴型测试通过 reducer 更新.
  - 本 Story 应复用这个单一状态更新路径接入 TTS 输入/提交, 并同时支持横屏 `SpeechInputPlaceholder` 与竖屏 compact 版本.
- `DigitalDog/app/src/main/java/com/digitaldog/demo/state/SpeechDemoState.kt`
  - 当前只有 `petState`, `currentMouth`, `inputSource`, `timelineQuality`.
  - 需要新增或等价表达 TTS 文本、错误、active session/intent 和 busy 状态.
- `DigitalDog/app/src/main/java/com/digitaldog/demo/sharedmodel/InputSource.kt`
  - 当前只有 `None` 和 `Manual`; Story 2.1 需要增加 TTS 输入来源, 并保持现有 stable id/label 行为.
- `DigitalDog/app/src/main/java/com/digitaldog/demo/state/PetStateReducer.kt`
  - 现有状态事件进入任一 `PetState` 时会将嘴型复位为 `MouthShape.Closed`.
  - TTS 提交 reducer 可复用或保持等价规则, 但不要破坏现有 mock 状态测试.
- `DigitalDog/app/src/main/java/com/digitaldog/demo/state/ManualMouthTestReducer.kt`
  - 当前手动嘴型测试会设置 `InputSource.Manual`, reset 回到默认状态.
  - Story 2.1 不应删除手动入口；如 active TTS session 与手动嘴型冲突, 应在实现中明确选择保守规则并用测试覆盖.
- `DigitalDog/app/src/main/java/com/digitaldog/demo/app/AppContentContract.kt`
  - 当前集中维护文案、semantics 和 test tag.
  - 新增 TTS 文案和 tags 时继续集中在此文件, 避免测试硬编码临时字符串.
- `DigitalDog/app/src/androidTest/java/com/digitaldog/demo/DigitalDogAppTest.kt`
  - 当前覆盖首屏、横竖屏、状态注入、手动嘴型、touch target、motion/reduced/static 语义.
  - Story 2.1 应在此基础上扩展输入交互测试, 不新建绕开 Activity/Compose test 约定的薄测试.

### 架构合规要求

- 使用 Kotlin + Jetpack Compose；不新增三方依赖.
- 单一状态源优先: TTS 输入、错误、session/intent、宠物状态、嘴型和输入来源应从同一个状态对象或同一 store/reducer 路径推导.
- View 只触发用户意图, 例如 text changed 和 submit text；View 不直接调用 Android TTS、音频、时间轴生成或嘴型计算.
- Busy/loading 必须归属到当前 session/intent, 不要新增多个互相冲突的布尔值让状态不一致.
- 播放中或 pending 中禁止创建第二个 TTS session, 除非用户先触发明确停止并清理当前 session.
- 调试台只读状态；不要让 debug 面板反向驱动 TTS 提交或宠物状态.
- 当前嘴型规则: 非空提交的准备态、空输入错误态和 busy guard 都必须保持 `MouthShape.Closed`.

### UI 与可访问性要求

- 主输入区仍是实际 demo 的一部分, 不要加入长段说明性文案.
- 输入框、主 CTA、错误消息和快速入口必须在横屏/竖屏下保持可读, 不得遮挡宠物舞台或调试摘要.
- 主 CTA 最小触控目标保持 48dp.
- 空输入错误文案“先输入一句话”必须靠近输入区, 且通过可见文本和 semantics 可读.
- CTA busy 状态可以选择禁用, 或改成停止/讲话中状态；无论哪种方式, 需要测试证明不会创建第二个 session.
- 上传和麦克风仍是实验入口, 不要在 Story 2.1 申请权限或打开文件选择器.

### Testing

- JVM unit test:
  - 默认状态仍为 idle/closed/none/ready.
  - 非空文本提交 trim 后创建 TTS intent/session, 进入 `thinking` 或准备态, 嘴型 closed, 输入来源 TTS.
  - 空字符串、空格、换行等空白输入不创建 session, 进入 `error`, 嘴型 closed, 错误文案为“先输入一句话”.
  - 输入变化可以清除错误或让下一次提交成功.
  - active session/pending 状态下再次 submit 不创建第二个 session.
  - manual mouth 现有 reducer 行为不回归.
- Android Compose test:
  - 在横屏和竖屏至少各覆盖一次主输入存在、可输入、CTA 可点击或 busy 禁用.
  - 非空输入点击 CTA 后, 顶部状态栏、宠物舞台和调试摘要/面板同步显示 TTS + `thinking` + `closed`.
  - 空输入点击 CTA 后, 输入区显示“先输入一句话”, 数字狗进入 `error`, 嘴型 closed, 用户编辑后可重试.
  - Busy 状态下主 CTA 禁用或显示停止状态, 第二次触发不会创建第二个 session.
  - 现有 `TagPetStage`, `TagPetFigure`, `TagDogMouth`, manual mouth controls 和 motion policy 测试继续通过.
- Scope boundary:
  - 扫描 main/test/manifest/Gradle 相关文件, 确认没有引入真实 `TextToSpeech.speak`, 音频播放、上传、麦克风权限、`LipSyncTimeline` 生成、网络、数据库或新依赖.
- 验证命令:
  - `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk ./gradlew :app:testDebugUnitTest`
  - `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk ./gradlew :app:assembleDebug`
  - `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk ./gradlew :app:compileDebugAndroidTestKotlin`
  - `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk ./gradlew :app:lintDebug`
  - 如 `adb devices` 存在可用设备: `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk ./gradlew :app:connectedDebugAndroidTest`

### Previous Story Intelligence

- Story 1.5 已完成并通过 review 补丁:
  - 宠物舞台已有 motion-aware profile、reduced/static motion policy 注入和装饰图形语义控制.
  - `DigitalDogApp` 可通过 `motionPolicy` 参数走 normal/reduced/static 测试路径.
  - `TagPetStage`, `TagPetFigure`, `TagDogMouth` 已被 Compose 测试覆盖, 不应因输入区改造退化.
  - full local validation 已通过 `:app:testDebugUnitTest`, `:app:assembleDebug`, `:app:compileDebugAndroidTestKotlin`, `:app:lintDebug`.
  - `connectedDebugAndroidTest` 当时未运行, 因为 `adb devices` 无连接设备.
- Story 1.4 的 7 类嘴型和手动测试仍是重要保护:
  - 手动嘴型选择不覆盖当前 `petState`.
  - 非闭口嘴型的舞台语义不能被描述成闭口待机.
  - `DogMouthRenderer` 依赖稳定 test tag 和当前嘴型状态.

### Files to Update or Create

建议新增:

- `DigitalDog/app/src/main/java/com/digitaldog/demo/state/TtsSubmitReducer.kt`
- `DigitalDog/app/src/main/java/com/digitaldog/demo/sharedmodel/SpeechSession.kt` 或 `TtsPlaybackIntent.kt`
- `DigitalDog/app/src/test/java/com/digitaldog/demo/state/TtsSubmitReducerTest.kt`
- `DigitalDog/app/src/test/java/com/digitaldog/demo/StoryTwoOneScopeBoundaryTest.kt`

建议更新:

- `DigitalDog/app/src/main/java/com/digitaldog/demo/state/SpeechDemoState.kt`
- `DigitalDog/app/src/main/java/com/digitaldog/demo/sharedmodel/InputSource.kt`
- `DigitalDog/app/src/main/java/com/digitaldog/demo/speechinput/SpeechInputPlaceholder.kt`
- `DigitalDog/app/src/main/java/com/digitaldog/demo/app/DigitalDogApp.kt`
- `DigitalDog/app/src/main/java/com/digitaldog/demo/app/AppContentContract.kt`
- `DigitalDog/app/src/test/java/com/digitaldog/demo/state/PetStateReducerTest.kt`
- `DigitalDog/app/src/test/java/com/digitaldog/demo/app/AppContentContractTest.kt`
- `DigitalDog/app/src/androidTest/java/com/digitaldog/demo/DigitalDogAppTest.kt`

如果开发者选择不同文件名, 必须保持包边界和行为等价, 并在 File List 中完整记录。

### Anti-Patterns

- 不要在 Story 2.1 调用 `TextToSpeech.speak`, 初始化真实 TTS engine, 播放音频或申请音频权限.
- 不要生成 `LipSyncTimeline`; Story 2.2 负责文本到嘴型时间轴.
- 不要实现播放时钟、当前嘴型推进、文本高亮、波形、时间轴 UI 或延迟校准.
- 不要让 `SpeechInputPlaceholder` 直接设置 `petState`, `currentMouth` 或 `inputSource`; 它只能触发回调.
- 不要新增第二套宠物状态机、第二套嘴型枚举或第二个互相竞争的 session 状态源.
- 不要删除或弱化手动嘴型测试、motion policy 注入、reduced/static 路径或 existing test tags.
- 不要把上传、麦克风、示例音频、真实 AI、网络、数据库或新依赖带入本 Story.
- 不要把旧架构文档中的 iPadOS、SwiftUI、AVFoundation 方案当作当前实现依据.

### Latest Technical Notes

- Android `TextToSpeech` 初始化是异步过程, 通过 `TextToSpeech.OnInitListener` 返回初始化结果；后续真正接入时, engine 适配器必须等待成功初始化后再发声.
- Android `TextToSpeech.shutdown()` 用于释放 TTS engine 资源；后续真实 TTS 集成应放在生命周期明确的 adapter/controller 中, 不要让 Compose View 直接持有 engine.
- Story 2.1 不需要、也不应调用 `TextToSpeech.speak`; 本 Story 只创建本地 TTS 播放意图/session 边界.
- Compose 输入控件和按钮应使用现有 Material3/Compose 能力, 并通过 `enabled`、可见错误文案和 semantics 暴露当前交互状态.

### References

- [Source: docs/planning-artifacts/epics.md#Story-2.1-TTS-主入口提交与空文本处理]
- [Source: docs/planning-artifacts/prds/prd-digital-dog-2026-06-23/prd.md#FR-5-文字转语音主入口]
- [Source: docs/planning-artifacts/prds/prd-digital-dog-2026-06-23/prd.md#FR-13-口型平滑与闭口规则]
- [Source: docs/planning-artifacts/prds/prd-digital-dog-2026-06-23/prd.md#FR-17-可恢复错误状态]
- [Source: docs/UI-SPEC.md#4-主输入区]
- [Source: docs/MVP-SPEC.md#需求]
- [Source: docs/LIPSYNC-SPEC.md#TTS-文本映射]
- [Source: docs/planning-artifacts/platform-decision-ipad-display.md#决策]
- [Source: docs/planning-artifacts/architecture.md#2026-06-24-平台纠偏Redmi-Pad-SE--MIUI-Android-平板]
- [Source: docs/implementation-artifacts/1-5-宠物基础动作-可访问性与-减少动态.md#Previous-Story-Intelligence]
- [Source: https://developer.android.com/reference/android/speech/tts/TextToSpeech]
- [Source: https://developer.android.com/develop/ui/compose/text/user-input]
- [Source: https://developer.android.com/develop/ui/compose/components/button]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Implementation Plan

- 先用 JVM 测试定义 TTS submit reducer、session/intent 和空输入/busy guard 行为.
- 再扩展 `SpeechDemoState`、`InputSource` 与 `AppContentContract`, 保持所有文案和 test tags 集中.
- 将 `SpeechInputPlaceholder` 改为受控输入组件, 通过回调把 text changed 和 submit 交给 `DigitalDogApp` 的单一状态更新路径.
- 用 Compose 测试覆盖非空提交、空输入错误、编辑后重试和 busy guard, 并确认顶部/舞台/调试区同步.
- 最后运行完整 Gradle 验证与 scope boundary 检查.

### Debug Log References

- 2026-06-25 14:13:20 +0800：通过 `bmad-create-story` 创建 Story 2.1, 基于 epics、PRD、architecture、UI-SPEC、MVP-SPEC、LIPSYNC-SPEC、platform decision、现有 Kotlin/Compose 代码和 Story 1.5 完成上下文整合.
- 2026-06-25 14:13:20 +0800：确认 Story 2.1 范围仅覆盖 TTS 主入口提交、空文本错误、可恢复编辑和 busy/session 冲突保护；显式排除真实 TTS 发声、LipSyncTimeline、播放同步、文本高亮和音频入口.
- 2026-06-25 14:19:46 +0800：开始 `bmad-dev-story` 实施, sprint 状态更新为 `in-progress`.
- 2026-06-25 14:22:00 +0800：新增 `TtsSubmitReducerTest`, `StoryTwoOneScopeBoundaryTest`, `AppContentContractTest` Story 2.1 契约断言和 `DigitalDogAppTest` TTS 交互断言；首次运行按预期失败在缺少新模型、reducer、字段和文案.
- 2026-06-25 14:25:00 +0800：实现 `SpeechSession`, `SpeechSessionStatus`, `InputSource.Tts`, `SpeechDemoState` TTS 字段和 `TtsSubmitReducer`; JVM 目标测试转绿.
- 2026-06-25 14:28:00 +0800：将 `SpeechInputPlaceholder` 升级为受控 `OutlinedTextField` + busy CTA, 并在 `DigitalDogApp` 中通过 reducer 接入单一状态源.
- 2026-06-25 14:31:00 +0800：完整 unit suite 首次暴露 `StoryTwoOneScopeBoundaryTest` 扫到旧 boundary-test forbidden token 列表；修正扫描跳过所有 `ScopeBoundaryTest.kt` 文件自身后 unit suite 转绿.
- 2026-06-25 14:34:51 +0800：完整验证通过 `:app:testDebugUnitTest :app:assembleDebug :app:compileDebugAndroidTestKotlin :app:lintDebug`; `adb devices` 无连接设备, 未运行 `:app:connectedDebugAndroidTest`.
- 2026-06-25 15:12:03 +0800：`bmad-code-review` 完成 3 层审查；6 个 patch findings 已修复并勾选, Story 状态更新为 `done`.

### Completion Notes

- Implemented Story 2.1 TTS submit boundary: non-empty text creates a lightweight local `SpeechSession`, enters `thinking`, marks source as `TTS`, and keeps mouth `closed`.
- Empty or whitespace-only submit now creates no session, enters `error`, keeps mouth `closed`, and shows the input-local error text `先输入一句话`.
- Active TTS session now disables the primary CTA with `讲话中`, and reducer-level busy guard prevents a second conflicting session while preserving the user's edited input text.
- `SpeechInputPlaceholder` is now a controlled Compose input surface; it only emits text/submit callbacks, while `DigitalDogApp` updates the single `SpeechDemoState` through `TtsSubmitReducer`.
- No real Android `TextToSpeech`, audio playback, `LipSyncTimeline`, upload, microphone, permission, network, database, or new dependency was introduced.
- Validation passed: `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk ./gradlew :app:testDebugUnitTest :app:assembleDebug :app:compileDebugAndroidTestKotlin :app:lintDebug`.
- `connectedDebugAndroidTest` not run because `/Users/kejincheng/Library/Android/sdk/platform-tools/adb devices` returned no attached devices.
- Review patches fixed: active TTS session now blocks manual mouth mutation; recoverable TTS input errors clear when entering manual mode; landscape tests run in a fixed roomy host; TTS debug panel/summary semantics are asserted; scope boundary scan now includes Java/config sources and broader forbidden APIs without skipping every boundary test.
- Post-review validation passed: `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk ./gradlew :app:testDebugUnitTest :app:assembleDebug :app:compileDebugAndroidTestKotlin :app:lintDebug`.
- Post-review `connectedDebugAndroidTest` not run because `/Users/kejincheng/Library/Android/sdk/platform-tools/adb devices` returned no attached devices.

### File List

- `DigitalDog/app/src/main/java/com/digitaldog/demo/app/AppContentContract.kt`
- `DigitalDog/app/src/main/java/com/digitaldog/demo/app/DigitalDogApp.kt`
- `DigitalDog/app/src/main/java/com/digitaldog/demo/sharedmodel/InputSource.kt`
- `DigitalDog/app/src/main/java/com/digitaldog/demo/sharedmodel/SpeechSession.kt`
- `DigitalDog/app/src/main/java/com/digitaldog/demo/speechinput/SpeechInputPlaceholder.kt`
- `DigitalDog/app/src/main/java/com/digitaldog/demo/state/ManualMouthTestReducer.kt`
- `DigitalDog/app/src/main/java/com/digitaldog/demo/state/SpeechDemoState.kt`
- `DigitalDog/app/src/main/java/com/digitaldog/demo/state/TtsSubmitReducer.kt`
- `DigitalDog/app/src/test/java/com/digitaldog/demo/StoryTwoOneScopeBoundaryTest.kt`
- `DigitalDog/app/src/test/java/com/digitaldog/demo/app/AppContentContractTest.kt`
- `DigitalDog/app/src/test/java/com/digitaldog/demo/state/TtsSubmitReducerTest.kt`
- `DigitalDog/app/src/androidTest/java/com/digitaldog/demo/DigitalDogAppTest.kt`
- `docs/implementation-artifacts/2-1-tts-主入口提交与空文本处理.md`
- `docs/implementation-artifacts/sprint-status.yaml`

### Change Log

- 2026-06-25：创建 Story 2.1 并将 sprint 状态推进到 ready-for-dev.
- 2026-06-25：实现 TTS 主入口提交、空文本错误、busy/session guard、单一状态源接线和测试覆盖；Story 状态更新为 review.
- 2026-06-25：修复代码审查 patch findings 6 项；Story 状态更新为 done.
