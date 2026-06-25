# Story 1.2：首屏宠物舞台与 Redmi Pad SE 横竖屏布局

Status: done

<!-- Validation is optional. Run validate-create-story before dev-story when extra quality review is needed. -->

## Story

As a 设计评审者,  
I want 打开 Redmi Pad SE 后第一屏就看到数字狗舞台和主要入口位置,  
so that 我能立刻理解这是一个可互动数字狗 Demo。

## Acceptance Criteria

1. **Given** App 在 Redmi Pad SE 横屏或接近 1920 x 1200 / 16:10 的 Android 平板模拟器运行，**When** 用户进入首屏，**Then** 页面展示顶部状态栏、宠物舞台、输入区占位、音频入口占位和调试台占位，**And** 宠物舞台是视觉中心，调试台不遮挡嘴巴区域。
2. **Given** App 在 Redmi Pad SE 竖屏或接近 1200 x 1920 的 Android 平板模拟器运行，**When** 用户进入首屏，**Then** 宠物舞台优先展示，调试台以摘要或折叠形态出现，**And** 核心控件不重叠，按钮和标签不溢出。
3. **Given** 用户首次打开 App，**When** 观察首屏 3 秒，**Then** 能判断这是数字狗 Demo、后续可让狗狗讲话、并可测试多种音频入口。
4. **Given** 本 Story 只负责静态首屏信息架构，**When** 开发者实现布局，**Then** 不实现真实 TTS、示例播放、上传、麦克风录音、状态机、口型时间轴或真实数字狗动画，**And** 不新增麦克风、存储、网络或 AI 权限。
5. **Given** 用户使用 TalkBack、字体缩放或触控操作，**When** 焦点进入顶部状态、宠物舞台、主输入区、快速入口和调试摘要，**Then** 关键区域有可读语义，触控目标不小于 48dp，状态不只依赖颜色表达。
6. **Given** 当前环境可运行 Android 构建验证，**When** Story 完成，**Then** `testDebugUnitTest`、`assembleDebug`、`compileDebugAndroidTestKotlin` 和 `lintDebug` 通过；若没有真实平板或模拟器，必须在 Dev Agent Record 中记录未执行 connected test 和真实横竖屏验收的原因。

## Tasks / Subtasks

- [x] 建立静态首屏内容契约和设计 tokens（AC: 1, 2, 3, 5）
  - [x] 扩展 `AppContentContract`，集中维护首屏固定文案、区域可访问描述和测试断言字符串。
  - [x] 新增或迁移轻量设计 tokens 到 `designsystem` 包，至少包含颜色、间距、圆角、基础字号和触控目标尺寸。
  - [x] 保留现有 `Digital Dog Demo` 标题，不改包名、应用名或 Android Manifest。
  - [x] 文案必须使用中文作为主界面语言；英文仅用于产品名、技术标签或后续嘴型 ID。

- [x] 实现 Redmi Pad SE 横屏首屏布局（AC: 1, 3, 4）
  - [x] 在 `DigitalDogApp` 或下沉组件中实现顶部状态栏、宠物舞台区域、调试台占位、主输入区占位和快速入口占位。
  - [x] 横屏主内容使用舞台 / 调试双栏结构，宠物舞台宽度优先级高于调试台，目标比例约 60% / 40%。
  - [x] 下方输入区展示 TTS 文本框占位、主 CTA“让狗狗说话”、次入口“播放示例”和实验入口“上传音频”“开始录音”。
  - [x] 调试台只显示静态占位信息：`同步调试`、`当前嘴型：closed`、`当前状态：待机`、`输入来源：未选择`、`解析质量：稳定演示待开始`。
  - [x] 不添加任何点击后的业务逻辑；按钮可以是禁用、占位或无副作用，但必须保持可见和可测。

- [x] 实现 Redmi Pad SE 竖屏和紧凑布局（AC: 2, 3, 5）
  - [x] 布局根据可用宽高或等价窗口分类选择横屏双栏或竖屏上下结构；优先使用现有 Compose 依赖完成，不为本 Story 引入新自适应布局库。
  - [x] 竖屏顺序为：顶部状态栏、宠物舞台、主输入区、快速入口、调试摘要。
  - [x] 竖屏调试台默认呈摘要或折叠占位，不展开完整波形/时间轴/延迟校准。
  - [x] 宠物舞台在竖屏仍保持明显高度，不能被输入区或调试摘要挤成窄条。
  - [x] 按钮组允许换行或网格排列；按钮文字不得裁切、重叠或溢出容器。

- [x] 落地静态宠物舞台占位（AC: 1, 2, 3, 5）
  - [x] 在 `petstage` 或 `dogrenderer` 包中创建可复用的静态宠物舞台 / 数字狗占位组件，避免所有 UI 堆在 `DigitalDogApp.kt`。
  - [x] 占位视觉必须传达“软萌宠物 + 轻科技项圈”：可使用 Compose 基础形状表现头部、耳朵、眼睛、闭口嘴线、身体、项圈状态光和地面/窝垫。
  - [x] 当前嘴型必须呈现为闭口 `closed`，不得提前实现 7 类嘴型切换。
  - [x] 嘴巴区域不得被标签、调试台或浮层遮挡。
  - [x] 宠物图形可作为装饰，但舞台必须提供可读状态语义，例如“数字狗待机，嘴型闭合”。

- [x] 建立输入区、快速入口和调试摘要占位组件（AC: 1, 2, 3, 5）
  - [x] 在 `speechinput` 包中创建静态输入区占位组件，展示文本输入占位、主 CTA、示例按钮和实验入口。
  - [x] 在 `debugpanel` 包中创建静态调试台 / 调试摘要占位组件，横屏完整、竖屏摘要。
  - [x] 上传和麦克风入口必须标记为实验入口，但不得使用长段说明文案。
  - [x] 当前状态、当前嘴型、输入来源、解析质量必须以文字展示，不能只靠颜色或图形。

- [x] 补充布局与可访问性测试（AC: 1, 2, 3, 5, 6）
  - [x] 更新现有 JVM smoke test，覆盖新增内容契约和核心中文文案。
  - [x] 更新或新增 Compose Android test，启动 `MainActivity` 并断言顶部状态栏、宠物舞台、主输入区、快速入口和调试台可见。
  - [x] 为横屏/竖屏分支提供可重复测试入口：可通过固定尺寸容器、可注入布局模式或等价方式断言横屏完整调试台与竖屏调试摘要分别出现。
  - [x] 测试应使用语义、content description 或稳定测试标签，不依赖截图像素。
  - [x] 运行 `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk ./gradlew :app:testDebugUnitTest :app:assembleDebug :app:compileDebugAndroidTestKotlin :app:lintDebug`。
  - [x] 如连接了真实 Redmi Pad SE 或 Android 平板模拟器，运行 `connectedDebugAndroidTest` 并记录结果；若未连接，必须如实记录。

- [x] 校验范围边界和回归风险（AC: 4, 6）
  - [x] 不新增 Android 权限，不接入 `TextToSpeech`、Media API、文件选择器、麦克风、网络、数据库或真实 AI。
  - [x] 不新增第二套宠物状态机、嘴型枚举、时间轴结构或播放控制器。
  - [x] 保留 Story 1.1 已通过的基础构建、Activity 启动测试和安全区处理。
  - [x] 更新 Dev Agent Record 的 Debug Log、Completion Notes、File List 和 Change Log。

### Review Findings

- [x] [Review][Patch] 竖屏布局缺少滚动或高度降级机制，字体缩放或分屏时底部内容可能被裁切 [DigitalDog/app/src/main/java/com/digitaldog/demo/app/DigitalDogApp.kt:104]
- [x] [Review][Patch] 紧凑横屏仍强制双栏与四按钮单行，窄横屏窗口容易挤压或溢出 [DigitalDog/app/src/main/java/com/digitaldog/demo/app/DigitalDogApp.kt:60]
- [x] [Review][Patch] 宠物舞台使用固定宽度，窄窗口下地面和数字狗图形可能横向裁切 [DigitalDog/app/src/main/java/com/digitaldog/demo/petstage/PetStagePlaceholder.kt:45]
- [x] [Review][Patch] 顶部状态栏缺少文本溢出、权重或换行策略，大字体下标题和状态信息可能互相挤压 [DigitalDog/app/src/main/java/com/digitaldog/demo/app/DigitalDogApp.kt:125]
- [x] [Review][Patch] 主输入区和快速入口缺少区域级无障碍语义，TalkBack 无法稳定识别关键分组 [DigitalDog/app/src/main/java/com/digitaldog/demo/speechinput/SpeechInputPlaceholder.kt:33]
- [x] [Review][Patch] 竖屏调试摘要缺少“解析质量”文本，未满足关键状态必须文字展示的要求 [DigitalDog/app/src/main/java/com/digitaldog/demo/debugpanel/DebugPanelPlaceholder.kt:75]
- [x] [Review][Patch] 横屏调试台提前暴露“波形占位”和“嘴型时间轴占位”，超出本 Story 静态字段边界 [DigitalDog/app/src/main/java/com/digitaldog/demo/debugpanel/DebugPanelPlaceholder.kt:55]
- [x] [Review][Patch] 实验标签和主 CTA 存在低对比颜色风险，不符合字体缩放/无障碍可读性目标 [DigitalDog/app/src/main/java/com/digitaldog/demo/designsystem/DigitalDogTheme.kt:16]
- [x] [Review][Patch] 布局分支测试绕过自动选择逻辑，且未覆盖真实尺寸、紧凑窗口和触控目标边界 [DigitalDog/app/src/androidTest/java/com/digitaldog/demo/DigitalDogAppTest.kt:33]

## Dev Notes

### 当前 Story 目标

本 Story 是 Epic 1 的第二个实施 Story，目标是把 Story 1.1 的“标题 + 宠物舞台占位”升级为可评审的静态首屏骨架。成功标准是：用户第一眼能看懂数字狗 Demo 的信息架构，并能在 Redmi Pad SE 横竖屏看到宠物舞台、主输入入口、示例/实验入口和调试摘要的位置。

本 Story **不是** 状态机、口型、TTS、音频、上传或麦克风实现 Story。所有真实交互能力留给后续 Story：

- Story 1.3：宠物状态机与项圈反馈。
- Story 1.4：7 类嘴型渲染与手动测试。
- Story 1.5：宠物基础动作、可访问性与减少动态。
- Epic 2：TTS 与示例讲话演示。
- Epic 3：调试台波形、时间轴和延迟校准。
- Epic 4：上传与麦克风实验入口。

### 当前工程状态

Story 1.1 已创建 Android 原生工程 `DigitalDog/`，当前可构建，技术基线如下：

- Android Gradle Plugin：8.6.1。
- Kotlin：1.9.24。
- Gradle Wrapper：8.7，已配置 `distributionSha256Sum`。
- Compose BOM：2024.06.00。
- Activity Compose：1.9.0。
- compileSdk / targetSdk：35。
- Java / Kotlin JVM target：17。

Story 1.1 仍处于 `in-progress`，原因是当前环境未连接真实 Redmi Pad SE 或 Android 平板模拟器，无法完成真实启动、权限弹窗和 connected Android test 验收。Story 1.2 可继续创建和实现代码，但不得把设备层验收写成已通过；必须如实记录设备不可用。

### 技术要求

- 平台：Android 原生平板 App。
- 目标设备：小米 Redmi Pad SE 11 英寸，MIUI Pad 14 / Android 13，1920 x 1200，最高 90Hz。
- UI：Jetpack Compose 单屏应用，不引入路由框架。
- 本 Story 优先使用已有 Compose / Material3 / Foundation 依赖；如确需新增依赖，必须先说明原因并获得用户确认。
- 自适应布局可使用 `BoxWithConstraints`、`WindowInsets`、`safeDrawingPadding()`、`Row`、`Column`、`FlowRow`/自定义换行或等价 Compose 能力。
- 不使用 WebView、React Native、Flutter、HTML/CSS/JS 或后端服务。

### 架构合规要求

- `DigitalDogApp` 只负责组合根布局和主题入口，不要继续无限膨胀成所有组件的承载文件。
- 设计 tokens 放入 `designsystem` 包。
- 宠物舞台占位放入 `petstage` 或 `dogrenderer` 包。
- 主输入区占位放入 `speechinput` 包。
- 调试台占位放入 `debugpanel` 包。
- 固定文案、content description 和测试断言字符串可继续集中在 `AppContentContract` 或同等 app 契约对象中。
- Compose UI 只能表达静态展示和未来意图位置，不直接持有音频分析、播放、状态机或口型计算逻辑。

### UI 与布局要求

横屏布局：

```text
+----------------------------------------------------------------+
| 顶部状态栏：Digital Dog Demo / 待机 / 未选择输入                |
+--------------------------------+-------------------------------+
|                                |                               |
|          宠物舞台               |            同步调试            |
|      闭口数字狗 + 项圈           | 当前嘴型 / 状态 / 来源 / 质量  |
|                                |                               |
+--------------------------------+-------------------------------+
| 主输入区：文本输入占位 + 让狗狗说话                              |
| 快速入口：播放示例 / 上传音频（实验）/ 开始录音（实验）          |
+----------------------------------------------------------------+
```

竖屏布局：

```text
+--------------------------------+
| 顶部状态栏                      |
+--------------------------------+
| 宠物舞台                        |
+--------------------------------+
| 主输入区                        |
+--------------------------------+
| 快速入口，两列或可换行           |
+--------------------------------+
| 同步调试摘要                    |
+--------------------------------+
```

视觉约束：

- 页面背景 `#F7FAF7`，主表面 `#FFFFFF`，次表面 `#EEF6F3`，主文字 `#24302F`，次文字 `#66736F`，科技蓝 `#4C8DFF`，珊瑚 `#FF6F61`，成功绿 `#37A86B`，提示黄 `#E7A93F`，边框 `#DDE7E3`。
- 卡片、按钮和输入框圆角不超过 8dp，除非宠物图形自身需要圆形。
- 不使用大面积单一色相、装饰性渐变球、散景圆点或营销式 hero。
- 字号使用固定 sp，不随视口宽度线性缩放；字间距保持 0。
- 播放中动态内容尚未实现，但本 Story 的静态区域尺寸应为后续波形、时间轴和文本高亮预留稳定空间。

### 可访问性要求

- 所有可见按钮和主要区域必须有可读文本或可访问描述。
- 宠物图形可以作为装饰，但宠物舞台状态必须可被 TalkBack 获取。
- 当前状态、当前嘴型、输入来源、解析质量必须以文字存在。
- 触控目标不小于 48dp。
- 调试摘要不能只靠颜色表达状态。
- 使用 Compose semantics 时注意：语义既服务 TalkBack，也服务 UI 测试。

### 测试要求

- JVM test：验证 `AppContentContract` 或等价契约包含 Story 1.2 所需关键文案。
- Android test：继续使用 `createAndroidComposeRule<MainActivity>()` 覆盖真实 Activity 启动路径。
- 布局分支测试：至少覆盖横屏完整调试台和竖屏摘要调试台两种分支。可通过固定尺寸容器、可注入布局模式或稳定语义标签完成，不要求截图测试。
- Lint 必须通过。
- 如果没有真实 Redmi Pad SE 或 Android 平板模拟器，记录 `adb devices` 结果，不能把真实设备验收声明为完成。

### Previous Story Intelligence

Story 1.1 的经验和约束：

- `local.properties` 不应作为交付文件；本机需要 SDK 时使用 `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk` 或本地未提交配置。
- `DigitalDogApp` 已使用 `safeDrawingPadding()`，后续布局必须保留系统栏安全区处理。
- Android instrumented test 已从 `createComposeRule()` 修正为 `createAndroidComposeRule<MainActivity>()`，后续 UI 测试不得退回只测裸 Composable 而忽略 Activity。
- 当前没有连接设备或模拟器，所有真实启动、权限弹窗、connected test 均需如实记录。
- Story 1.1 未引入权限、网络、TTS、上传、录音、AI、WebView、后端或数据库；Story 1.2 也必须保持这个边界。

### Files to Update or Create

预计更新：

- `DigitalDog/app/src/main/java/com/digitaldog/demo/app/AppContentContract.kt`
- `DigitalDog/app/src/main/java/com/digitaldog/demo/app/DigitalDogApp.kt`
- `DigitalDog/app/src/test/java/com/digitaldog/demo/app/AppContentContractTest.kt`
- `DigitalDog/app/src/androidTest/java/com/digitaldog/demo/DigitalDogAppTest.kt`

预计新增：

- `DigitalDog/app/src/main/java/com/digitaldog/demo/designsystem/DigitalDogTheme.kt` 或等价 tokens 文件
- `DigitalDog/app/src/main/java/com/digitaldog/demo/petstage/PetStagePlaceholder.kt`
- `DigitalDog/app/src/main/java/com/digitaldog/demo/speechinput/SpeechInputPlaceholder.kt`
- `DigitalDog/app/src/main/java/com/digitaldog/demo/debugpanel/DebugPanelPlaceholder.kt`

如开发者选择不同文件名，必须保持包边界等价，并在 File List 中完整记录。

### Anti-Patterns

- 不要把本 Story 实现成完整宠物状态机。
- 不要提前创建 `PetState`、`MouthShape`、`LipSyncTimeline` 等核心模型；它们属于后续 Story，除非本 Story 的静态 UI 必须引用纯文案。
- 不要实现真实按钮点击逻辑、播放逻辑、录音权限、上传文件选择或 TTS。
- 不要为了图标引入额外依赖；本 Story 可以先使用文字按钮。
- 不要在调试台里显示伪造波形或伪造时间轴细节；占位摘要即可。
- 不要让调试台遮挡或压缩宠物嘴巴区域。
- 不要使用旧 iPadOS / SwiftUI / AVFoundation 方案；所有旧 iPad 表述均已被 Redmi Pad SE / Android 决策覆盖。

### Latest Technical Notes

- Android 官方 Compose adaptive guidance 强调 UI 需要根据显示尺寸、方向、分屏和窗口变化在运行时适配；Story 1.2 的布局分支应基于可用窗口而不是硬编码单一设备。
- Android 官方 Compose semantics 文档说明语义信息会被无障碍服务和测试框架共同使用；本 Story 的主要区域和调试摘要应提供稳定语义。
- Android 官方 Compose testing 文档支持通过测试 API 查找节点、断言属性并操作 UI；本 Story 应优先用语义或文本断言，而不是截图像素断言。
- Compose Material3 Adaptive 1.2.0 已有稳定版本，但本 Story 范围较小，默认不引入该依赖；后续若需要复杂多窗格或窗口姿态能力再评估。

### References

- [epics.md](/Users/kejincheng/project/digital-dog/docs/planning-artifacts/epics.md)：Epic 1、Story 1.2、FR/NFR 覆盖。
- [UI-SPEC.md](/Users/kejincheng/project/digital-dog/docs/UI-SPEC.md)：Redmi Pad SE 横竖屏布局、色彩、组件、文案和 UI 验收清单。
- [MVP-SPEC.md](/Users/kejincheng/project/digital-dog/docs/MVP-SPEC.md)：MVP 范围、首屏需求和推荐实施顺序。
- [ux-design-specification.md](/Users/kejincheng/project/digital-dog/docs/planning-artifacts/ux-design-specification.md)：宠物舞台优先、调试台辅助、可访问性和布局策略。
- [platform-decision-ipad-display.md](/Users/kejincheng/project/digital-dog/docs/planning-artifacts/platform-decision-ipad-display.md)：Redmi Pad SE / Android 原生平台纠偏。
- [sprint-change-proposal-2026-06-24.md](/Users/kejincheng/project/digital-dog/docs/planning-artifacts/sprint-change-proposal-2026-06-24.md)：设备纠偏原因和技术影响。
- [Story 1.1](/Users/kejincheng/project/digital-dog/docs/implementation-artifacts/1-1-运行-redmi-pad-se-原生-android-demo-外壳.md)：当前工程基线、代码审查修复和未完成设备验收。
- Android Developers: Build adaptive apps with Jetpack Compose — https://developer.android.com/develop/ui/compose/build-adaptive-apps
- Android Developers: Semantics in Jetpack Compose — https://developer.android.com/develop/ui/compose/accessibility/semantics
- Android Developers: Test your Compose layout — https://developer.android.com/develop/ui/compose/testing
- AndroidX Compose Material3 Adaptive releases — https://developer.android.com/jetpack/androidx/releases/compose-material3-adaptive

## Dev Agent Record

### Agent Model Used

Codex

### Debug Log References

- 2026-06-24 11:46:01 +0800：自动发现 sprint 中首个 `ready-for-dev` Story：`1-2-首屏宠物舞台与-redmi-pad-se-横竖屏布局`，并将 story / sprint 状态更新为 `in-progress`。
- 2026-06-24 11:47:00 +0800：按 RED 阶段先更新 JVM smoke test 和 Compose Android test，新测试因 `AppContentContract` 新文案、`AppLayoutMode` 和布局语义标签尚未实现而编译失败，符合预期。
- 2026-06-24 11:49:00 +0800：实现静态首屏内容契约、轻量设计 tokens、横竖屏布局分支、宠物舞台占位、输入区占位和调试台占位。
- 2026-06-24 11:50:00 +0800：运行 `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk ./gradlew :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin`，测试与 Android test 编译通过。
- 2026-06-24 11:51:00 +0800：检查主源码和 Gradle 配置，未发现新增 Android 权限、TTS、Media、录音、文件选择、网络、数据库或 WebView 调用。
- 2026-06-24 11:51:36 +0800：运行 `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk ./gradlew :app:testDebugUnitTest :app:assembleDebug :app:compileDebugAndroidTestKotlin :app:lintDebug`，构建成功。
- 2026-06-24 11:51:36 +0800：运行 `adb devices` 未发现已连接设备或模拟器，因此未运行 `connectedDebugAndroidTest`，真实 Redmi Pad SE 横竖屏视觉验收仍待设备或平板模拟器。
- 2026-06-24 12:34:03 +0800：处理 code review 9 个 patch 项，修复竖屏滚动兜底、紧凑横屏断点、按钮区换行降级、舞台固定宽度、状态栏溢出、输入区/快速入口语义、调试摘要解析质量、调试台范围边界、颜色对比和布局测试覆盖。
- 2026-06-24 12:34:03 +0800：运行 `ANDROID_HOME=/Users/kejincheng/Library/Android/sdk ./gradlew :app:testDebugUnitTest :app:assembleDebug :app:compileDebugAndroidTestKotlin :app:lintDebug`，构建成功。
- 2026-06-24 12:34:03 +0800：运行 `/Users/kejincheng/Library/Android/sdk/platform-tools/adb devices` 未发现已连接设备或模拟器，因此仍未运行 `connectedDebugAndroidTest` 和真实 Redmi Pad SE 横竖屏视觉验收。

### Completion Notes List

- 实现 Story 1.2 静态首屏骨架：顶部状态栏、宠物舞台、同步调试、主输入区和多种音频入口占位。
- 新增轻量设计 tokens，覆盖 Story 要求的色彩、间距、圆角、字号和 48dp 触控目标。
- `DigitalDogApp` 现在根据窗口宽高或测试注入的 `AppLayoutMode` 切换横屏双栏和竖屏上下布局，并保留 `safeDrawingPadding()`。
- 宠物舞台使用 Compose 基础形状表现闭口数字狗、耳朵、眼睛、身体、项圈和地面/窝垫，不实现 7 类嘴型切换和真实动画。
- 输入区展示 TTS 主入口、播放示例、上传音频和开始录音，其中上传/麦克风标记为实验入口；按钮仅为静态占位，无业务逻辑。
- 调试台展示当前嘴型、当前状态、输入来源和解析质量；横屏为完整占位，竖屏为摘要占位。
- Code review 后新增竖屏滚动兜底、宽高断点、防溢出状态栏、响应式按钮区、高对比文本色和输入区/快速入口分组语义。
- 横屏调试台已移除波形/嘴型时间轴可见占位，保留本 Story 允许的静态状态字段；竖屏调试摘要已补充解析质量。
- 测试覆盖 `AppContentContract` 核心文案、Activity 首屏骨架、横屏调试台分支、竖屏调试摘要分支、自动布局断点、区域语义和 48dp 触控目标。
- 未新增 Android 权限、TTS、上传、录音、音频分析、状态机、嘴型枚举、时间轴、播放控制、AI、网络、数据库或 WebView。
- 验证通过：`ANDROID_HOME=/Users/kejincheng/Library/Android/sdk ./gradlew :app:testDebugUnitTest :app:assembleDebug :app:compileDebugAndroidTestKotlin :app:lintDebug`。
- 未执行：`connectedDebugAndroidTest` 和真实 Redmi Pad SE 横竖屏视觉验收；原因是 `adb devices` 无可用设备或模拟器。

### File List

- `DigitalDog/app/src/main/java/com/digitaldog/demo/app/AppContentContract.kt`
- `DigitalDog/app/src/main/java/com/digitaldog/demo/app/DigitalDogApp.kt`
- `DigitalDog/app/src/main/java/com/digitaldog/demo/designsystem/DigitalDogTheme.kt`
- `DigitalDog/app/src/main/java/com/digitaldog/demo/petstage/PetStagePlaceholder.kt`
- `DigitalDog/app/src/main/java/com/digitaldog/demo/speechinput/SpeechInputPlaceholder.kt`
- `DigitalDog/app/src/main/java/com/digitaldog/demo/debugpanel/DebugPanelPlaceholder.kt`
- `DigitalDog/app/src/test/java/com/digitaldog/demo/app/AppContentContractTest.kt`
- `DigitalDog/app/src/androidTest/java/com/digitaldog/demo/DigitalDogAppTest.kt`
- `docs/implementation-artifacts/sprint-status.yaml`
- `docs/implementation-artifacts/1-2-首屏宠物舞台与-redmi-pad-se-横竖屏布局.md`

### Change Log

- 2026-06-24：实现 Story 1.2 Redmi Pad SE 静态首屏骨架、横竖屏布局分支、设计 tokens、宠物舞台/输入区/调试台占位和测试覆盖。
- 2026-06-24：处理 code review patch 项并将 Story 1.2 状态推进到 `done`。
