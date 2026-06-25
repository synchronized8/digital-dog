# 平台决策：第一版改为 Redmi Pad SE 11 英寸 MIUI 平板展示端

**日期：** 2026-06-23  
**修订日期：** 2026-06-24  
**状态：** 已纠偏  
**影响范围：** 产品简报、PRD、MVP 规格、UI 规格、口型同步规格、UX 规格、架构设计、Epic、Story、Sprint 状态

## 决策

第一版数字狗语音口型 Demo 的展示载体更正为 **小米 Redmi Pad SE 11 英寸 MIUI / Android 平板原生展示端**。此前文档中的 iPadOS、iPad、Xcode、SwiftUI、AVFoundation、VoiceOver、Reduce Motion 等表述均被本决策覆盖。

第一版优先面向 Redmi Pad SE 横屏演示，竖屏保持核心流程可用；不以桌面网页、手机网页或 iPadOS 原生应用为主要目标。

## 设备基线

- 设备：Redmi Pad SE 11 英寸。
- 系统：MIUI Pad 14，基于 Android 13。
- 屏幕：11 英寸，1920 x 1200，约 207 ppi，最高 90Hz。
- 形态：16:10 横屏优先，竖屏完整可用。
- 音频：四扬声器、麦克风、本地播放和录音能力。

参考来源：小米官方规格页 https://www.mi.com/global/product/redmi-pad-se/specs/

## 技术含义

- 展示端优先使用 Android 原生能力，推荐技术方向为 `Kotlin + Jetpack Compose`。
- TTS 主路径优先使用 Android `TextToSpeech` 或预设 TTS 时间轴。
- 麦克风录音使用 Android 运行时权限、`AudioRecord` 或 `MediaRecorder`。
- 上传音频使用 Android Storage Access Framework / 系统文件选择器读取本地音频。
- 音频播放、解码和基础分析使用 Android Media APIs，必要时引入 Media3；不依赖浏览器 Web Audio。
- 数字狗形象优先使用 Compose Canvas、Lottie Android 或 Rive Android 等 Android 端可控方案；不默认使用 SVG/CSS。
- 可访问性以 TalkBack、字体缩放、触控目标、系统权限提示和 Android 动画缩放设置为基准。

## 产品与 UX 含义

- 首屏理解目标改为“用户拿到 Redmi Pad SE 后 3 秒内看懂这是可互动数字狗 demo”。
- 横屏是主演示形态：宠物舞台与调试台并列，输入区位于下方或侧边稳定区域。
- 竖屏是可用形态：宠物舞台优先，调试台折叠为摘要。
- UI 需针对 11 英寸 16:10 平板优化，不再按 iPad 逻辑尺寸作为主要验收。
- 仍保留 TTS、内置示例、上传音频、麦克风录音四种入口。
- 仍保留真实 AI 语音对话接入预留，但第一版不接真实 AI。

## 验收基准

- Redmi Pad SE 横屏：基于 1920 x 1200 / 16:10 平板视口，宠物舞台、输入入口、示例入口和调试台可同时展示。
- Redmi Pad SE 竖屏：基于 1200 x 1920 / 10:16 平板视口，宠物舞台和主入口优先展示，调试台可折叠。
- 若使用 Android 模拟器，应创建接近 11 英寸、1920 x 1200、Android 13 的平板设备配置。
- 触控目标不小于 `48dp`。
- 嘴巴区域不得被调试台、浮层、系统权限提示后的自定义提示遮挡。
- 麦克风权限拒绝、上传失败和音频解析失败不阻断 TTS 与示例路径。

## 不改动的核心约束

- 数字狗仍是主角，调试台仍为辅助。
- 7 类嘴型集合不变：`closed`、`small`、`wide`、`round`、`smile`、`teeth`、`pant`。
- 宠物状态机不变：`idle`、`listening`、`thinking`、`speaking`、`done`、`error`。
- TTS 仍是主演示入口，内置示例仍是稳定兜底。
- 上传音频和麦克风录音仍是实验入口，不承诺音素级同步。
