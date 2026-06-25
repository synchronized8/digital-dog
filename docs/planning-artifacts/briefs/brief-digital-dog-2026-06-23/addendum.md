# 产品简报附录

**创建日期：** 2026-06-23  
**用途：** 保存对实现有帮助、但不适合放进产品简报正文的细节。

## 来源材料索引

| 来源 | 作用 |
| --- | --- |
| `docs/brainstorming/brainstorming-session-20260623-144420.md` | 产品方向、备选方案、MVP 选择、行动规划 |
| `docs/MVP-SPEC.md` | MVP 范围、需求和验收标准 |
| `docs/UI-SPEC.md` | UI 布局、视觉系统、组件和动效契约 |
| `docs/LIPSYNC-SPEC.md` | 嘴型集合、TTS 映射、音频分析和时间轴规则 |
| `docs/planning-artifacts/platform-decision-ipad-display.md` | iPad 展示端平台决策和技术含义 |

## 未放入简报正文的细节

- 完整 12 项 MVP 需求清单。
- 完整 UI 颜色、字体、间距和 iPad 横竖屏布局契约。
- 完整 `LipSyncTimeline` 数据模型。
- 中文和英文的启发式嘴型映射规则。
- 上传音频和麦克风音频特征提取规则。
- 详细实施顺序和验收清单。

## 已确认决策

- 第一版用于内部对齐和开发规划，不用于公开发布。
- TTS 可以先使用 `AVSpeechSynthesizer` 或模拟时间轴，暂不选择生产级 TTS 服务。
- 内置示例音频可以先使用预设或模拟时间轴。
- 上传和麦克风路径在第一版明确标记为实验能力。
- 数字狗资产实现方式由实施阶段基于 iPadOS 技术栈选择。
- 当前简报不加入外部市场、竞品或用户研究。
