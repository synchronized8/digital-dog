# PRD 附录：数字狗语音口型 Demo

**创建日期：** 2026-06-23  
**用途：** 保存对实现有帮助、但不适合放进 PRD 主体的技术和规格细节索引。

## 来源材料索引

| 来源 | 用途 |
| --- | --- |
| `docs/planning-artifacts/briefs/brief-digital-dog-2026-06-23/brief.md` | 产品方向、范围、成功标准 |
| `docs/planning-artifacts/briefs/brief-digital-dog-2026-06-23/addendum.md` | 已确认决策和未放入产品简报正文的细节 |
| `docs/MVP-SPEC.md` | 12 项 MVP 需求与验收清单 |
| `docs/UI-SPEC.md` | iPad 展示布局、视觉系统、组件、动效和横竖屏契约 |
| `docs/LIPSYNC-SPEC.md` | 口型集合、TTS 映射、上传/麦克风分析和时间轴规则 |
| `docs/planning-artifacts/platform-decision-ipad-display.md` | iPad 展示端平台决策和技术含义 |
| `docs/brainstorming/brainstorming-session-20260623-144420.md` | 早期脑暴、方案选择和行动规划 |

## 不放入 PRD 主体的技术细节

- `LipSyncTimeline`、`LipSyncSegment`、`AudioAnalysisFrame` 数据结构。
- 中文拼音 initial/final 到 7 类嘴型的启发式规则。
- 英文字母组合到嘴型的启发式规则。
- 上传音频 RMS、频谱、ZCR、静音检测等分析规则。
- 嘴型时间轴合并、最短保持时间和平滑策略。
- UI 色彩、字体、间距、组件尺寸和 iPad 横竖屏布局细节。
- 具体实现顺序和工程任务拆分。

## 已确认的实施边界

- MVP 数字狗形象优先用 SwiftUI vector/Canvas、Lottie 或 Rive 等 iPadOS 可控方案实现，保证 7 类嘴型、耳朵、眼睛、头部、身体和尾巴动作可控。
- 内置示例以预设中文短句和预设嘴型时间轴作为稳定基线，真实音频文件不是第一版阻塞项。
- TTS 可以先使用 `AVSpeechSynthesizer`、模拟播放进度或预设时间轴；生产级 TTS 服务选型不属于第一版。
- 上传音频和麦克风录音只承诺基础音频分析驱动，解析质量必须在调试台显式展示。

## 实施阶段需要处理的事项

- 设计并实现 iPadOS 可控数字狗基础形象。
- 为内置示例配置预设中文短句和预设嘴型时间轴。
- 在实施计划中确定 `AVSpeechSynthesizer`、模拟播放进度或预设时间轴的具体落地方式。
- 将 PRD 的 FR 拆成 epics 和 stories。
- 将 `UI-SPEC.md` 和 `LIPSYNC-SPEC.md` 映射到具体组件和模块。
