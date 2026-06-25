# Sprint 变更提案：展示设备纠偏为 Redmi Pad SE 11 英寸 MIUI 平板

**日期：** 2026-06-24  
**状态：** 已按用户更正执行  
**触发来源：** 用户更正“MIUI/小米 Redmi Pad SE 11英寸 1.9K 90hz 红米平板SE，这个是平板的信息，优化之前的设计”  
**影响阶段：** Sprint 已启动，Story 1.1 曾进入 dev-story，但未产生实现代码

## 1. 问题概述

此前规划文档把第一版展示设备修正为 iPad，并进一步推导出 iPadOS、Xcode、SwiftUI、AVFoundation、VoiceOver、Reduce Motion 等实施约束。用户现已明确实际展示设备为 **小米 Redmi Pad SE 11 英寸 MIUI 平板**，因此原 iPadOS 技术路线不再成立。

这不是功能范围变化，而是 **平台和设备基线变化**。核心产品目标仍不变：做一只可互动的数字狗，能跟随声音讲话并变嘴型，支持 TTS、内置示例、上传音频和麦克风录音测试入口，并预留真实 AI 语音对话接入。

## 2. 设备事实

第一版展示设备基线更新为：

- 设备：Redmi Pad SE 11 英寸。
- 系统：MIUI Pad 14，基于 Android 13。
- 屏幕：11 英寸，1920 x 1200，约 207 ppi，最高 90Hz。
- 形态：16:10 横屏优先，竖屏完整可用。
- 硬件能力：四扬声器、麦克风、本地音频播放与录音能力。

参考来源：小米官方规格页 https://www.mi.com/global/product/redmi-pad-se/specs/

## 3. 影响分析

### 3.1 产品与 UX

- “iPad 展示端”需要改为“Redmi Pad SE / MIUI 平板展示端”。
- 横屏仍是主演示形态，但验收基准应从 iPad 逻辑尺寸改为 Redmi Pad SE 16:10 视口。
- 竖屏仍需可用，调试台默认折叠。
- 可访问性从 VoiceOver / Reduce Motion 改为 TalkBack / 字体缩放 / Android 动画缩放或减少动态偏好。
- 设计系统不再以 SF Symbols、iPadOS 系统字体为基线，改为 Material Icons 或 Compose 图标、Android 系统字体、Roboto / Noto Sans CJK / MiSans 可替换策略。

### 3.2 架构

- 启动模板从 Xcode iOS App 模板改为 Android Studio 原生 Android 项目。
- 推荐技术路线从 `SwiftUI + AVFoundation` 改为 `Kotlin + Jetpack Compose + Android Media / TTS / AudioRecord 或 MediaRecorder`。
- 音频上传从 iPadOS 文件选择改为 Android Storage Access Framework / 系统文件选择器。
- 麦克风权限从 iPadOS 权限改为 Android 运行时权限。
- 数字狗渲染从 SwiftUI Canvas / iPadOS Lottie/Rive 改为 Compose Canvas / Lottie Android / Rive Android 可替换资产层。
- 测试从 XCTest / Swift Testing 改为 Kotlin/JVM 单元测试、Compose UI Test 和 Android 仪器化测试。

### 3.3 Sprint 与 Story

- 当前 Story 1.1 “运行 iPadOS 原生 Demo 外壳”必须废弃并替换为 Android 平板工程外壳。
- 此前 dev-story 失败原因是缺少完整 Xcode；在新路线下该阻塞消失，但需要改为检查 Android Studio / Android SDK / Gradle / 设备连接或模拟器。
- Sprint 状态应把 Story 1.1 重置为 Android 新 Story 的 `ready-for-dev`，避免继续执行旧 Xcode 任务。

## 4. 推荐决策

采用 **直接调整**。

理由：

- 核心需求没有变化，不需要重新做头脑风暴、PRD 或 Epic 全量重建。
- 尚未生成实现代码，没有迁移成本。
- 变更集中在平台基线、设备验收和 Story 1.1 开发入口。
- 原先围绕数字狗、7 类嘴型、TTS 主路径、示例音频、上传/麦克风实验入口、调试台和 AI 预留的产品结构仍然有效。

## 5. 已执行修正范围

- 更新平台决策文档，明确 Redmi Pad SE 11 英寸 MIUI 平板为第一版展示设备。
- 在 PRD、UX、架构、MVP、UI、LIPSYNC 文档中追加平台纠偏说明，覆盖旧 iPadOS 表述。
- 更新 Epics 中直接影响开发的 FR、NFR、架构附加需求和 Story 1.1。
- 替换 Story 1.1 为 Android / Jetpack Compose 原生工程外壳。
- 更新 sprint 状态，把新 Story 1.1 标为 `ready-for-dev`。

## 6. 后续建议

下一步应重新执行 `bmad-dev-story`，目标 Story 为：

`docs/implementation-artifacts/1-1-运行-redmi-pad-se-原生-android-demo-外壳.md`

开发前需要检查：

- Android Studio 或 Android SDK 是否可用。
- Gradle 是否可执行。
- 是否能连接真实 Redmi Pad SE 或创建接近 1920 x 1200 / Android 13 的平板模拟器。
- 是否能运行 `assembleDebug` 和基础单元测试。
