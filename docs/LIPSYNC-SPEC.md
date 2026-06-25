# 数字狗语音口型 Demo — 口型同步规格

**创建日期：** 2026-06-23  
**状态：** 草稿  
**来源：** `docs/MVP-SPEC.md`、`docs/UI-SPEC.md`  
**目标版本：** 第一版 MVP

**2026-06-24 平台纠偏：** 第一版展示设备更正为小米 Redmi Pad SE 11 英寸 MIUI / Android 平板。本文中的 iPadOS / Swift 示例均按 Android / Kotlin 原生实现迁移；口型集合、时间轴字段和同步规则不变。

## 目标

定义 Redmi Pad SE / Android 数字狗 demo 的口型同步规则：7 类嘴型、TTS 文本映射、内置示例时间轴、上传音频与麦克风录音的音频分析、时间轴数据结构、平滑策略、延迟校准和验收标准。

第一版目标不是实现完美音素识别，而是让 demo 在 TTS 路径上呈现稳定、可观察、相对准确的口型同步；上传音频和麦克风路径提供基础可用的音频驱动效果，并清晰标记解析质量。

## 范围

### 范围内

- 7 类嘴型定义与使用边界。
- TTS 文本到嘴型时间轴的近似映射。
- 中文和英文输入的第一版映射规则。
- 内置示例音频的预设时间轴规则。
- 上传音频和麦克风录音的基础音频分析规则。
- 时间轴数据结构。
- 口型平滑、最短保持时间、静音回闭口规则。
- 延迟校准。
- 调试指标与验收标准。

### 范围外

- 完整 phoneme 识别。
- 实时流式麦克风口型。
- 服务端 ASR、强制对齐、云端 TTS 时间戳。
- 多语言完整发音字典。
- 训练模型或深度学习口型生成。

## 术语

| 术语 | 含义 |
| --- | --- |
| 嘴型（Mouth Shape） | 数字狗当前嘴型状态 |
| 发音视觉嘴型（Viseme） | 发音对应的视觉嘴型类别 |
| 时间轴（Timeline） | 按时间排列的嘴型片段 |
| 分析帧（Frame） | 音频分析窗口，第一版建议 33ms 或 50ms |
| 置信度（Confidence） | 当前嘴型判断可信度，范围 `0` 到 `1` |
| 延迟偏移（Latency Offset） | 嘴型相对音频播放的偏移，单位毫秒 |

## 7 类嘴型

| ID | 名称 | 用途 | 视觉要求 | 第一版优先级 |
| --- | --- | --- | --- | --- |
| `closed` | 闭口 | 静音、停顿、闭唇音、待机 | 嘴线闭合，无开口 | P0 |
| `small` | 小开口 | 普通短音、弱音、无法精确判断的有声段 | 小幅张口，适合作为默认讲话嘴型 | P0 |
| `wide` | 大开口 | 重音、响亮元音、`a/ah` 类声音 | 纵向明显张口 | P0 |
| `round` | 圆嘴 | `o/u/oo/w` 类声音 | 圆形或椭圆开口 | P0 |
| `smile` | 咧嘴 | `e/i/y` 类声音 | 横向咧嘴，略带微笑 | P0 |
| `teeth` | 齿音 | `f/v/s/z/th/sh/ch` 类摩擦或擦音感觉 | 轻微露齿或上下咬合 | P0 |
| `pant` | 开心喘气 | 宠物反馈、结束动作、非语言表达 | 可爱喘气嘴型，不能滥用于普通发音 | P1 |

## 嘴型使用约束

- 静音段、待机、错误状态和思考状态必须使用 `closed`。
- 普通讲话中无法判断具体发音时，优先使用 `small`。
- `wide`、`round`、`smile`、`teeth` 应只在有明确文本或音频特征时出现。
- `pant` 主要用于宠物反馈，不作为普通语音的默认嘴型。
- 任一嘴型片段不得短于 `80ms`，除非是手动测试模式。
- 连续相同嘴型必须合并成一个时间轴片段。
- 嘴型切换需要 80ms 到 140ms 的视觉过渡，避免硬跳。

## 时间轴数据结构

### LipSyncTimeline

第一版推荐在 Android 客户端中使用 Kotlin 数据结构表达时间轴；如果实现阶段需要跨平台或调试导出，也可以序列化为等价 JSON。

```swift
enum MouthShape: String, Codable {
  case closed
  case small
  case wide
  case round
  case smile
  case teeth
  case pant
}

enum InputSource: String, Codable {
  case tts
  case sample
  case upload
  case microphone
  case manual
}

struct TextRange: Codable {
  let start: Int
  let end: Int
  let text: String
}

struct LipSyncSegment: Identifiable, Codable {
  let id: String
  let startMs: Int
  let endMs: Int
  let mouth: MouthShape
  let source: InputSource
  let confidence: Double
  let textRange: TextRange?
  let reason: String?
}

struct LipSyncTimeline: Codable {
  let source: InputSource
  let durationMs: Int
  let latencyOffsetMs: Int
  let segments: [LipSyncSegment]
  let quality: TimelineQuality
  let generatedBy: TimelineGenerator
}

enum TimelineQuality: String, Codable {
  case stable
  case medium
  case experimental
}

enum TimelineGenerator: String, Codable {
  case textRule = "text-rule"
  case preset
  case audioAnalysis = "audio-analysis"
  case manual
}
```

### 示例

```json
{
  "source": "tts",
  "durationMs": 1800,
  "latencyOffsetMs": 0,
  "quality": "stable",
  "generatedBy": "text-rule",
  "segments": [
    {
      "id": "seg-001",
      "startMs": 0,
      "endMs": 120,
      "mouth": "closed",
      "source": "tts",
      "confidence": 0.9,
      "reason": "pre-speech preparation"
    },
    {
      "id": "seg-002",
      "startMs": 120,
      "endMs": 340,
      "mouth": "wide",
      "source": "tts",
      "confidence": 0.75,
      "textRange": { "start": 0, "end": 1, "text": "大" },
      "reason": "Chinese final contains a"
    }
  ]
}
```

## 播放时序

### 当前嘴型计算

```text
effectiveTimeMs = audioCurrentTimeMs + latencyOffsetMs
currentSegment = segment where startMs <= effectiveTimeMs < endMs
currentMouth = currentSegment.mouth
```

### 延迟校准

- 范围：`-150ms` 到 `+150ms`。
- 步进：`25ms`。
- 默认：`0ms`。
- 正值表示嘴型提前读取更靠后的时间轴片段。
- 负值表示嘴型滞后读取更靠前的时间轴片段。
- UI 必须显示当前偏移值。

## TTS 文本映射

### 第一版原则

TTS 主路径使用“文本估算 + 时间轴播放”。iPadOS 第一版可以使用 `AVSpeechSynthesizer` 发声，同时用本规格生成本地嘴型时间轴；如果后续接入带 word/phoneme timing 的 TTS 服务，可以替换时间分配层，但保留 7 类嘴型和时间轴数据结构。

第一版流程：

1. 读取输入文本。
2. 清理文本：去除多余空格，保留中文、英文、数字和标点。
3. 切分 token：中文按字符或词，英文按单词。
4. 为每个 token 估算持续时间。
5. 将 token 映射到一个或多个嘴型。
6. 生成 `LipSyncTimeline`。
7. 合并相邻相同嘴型片段。
8. 应用最短保持时间和平滑策略。
9. 播放时同步文本高亮、时间轴和当前嘴型。

### 时长估算

第一版建议：

| 文本类型 | 默认时长 |
| --- | --- |
| 中文单字 | 180ms 到 240ms |
| 英文短词 1-3 字母 | 180ms 到 260ms |
| 英文普通词 4-7 字母 | 280ms 到 420ms |
| 英文长词 8+ 字母 | 420ms 到 680ms |
| 逗号、顿号、短停顿 | 160ms 到 240ms |
| 句号、问号、感叹号 | 260ms 到 420ms |
| 数字 | 按读法拆分，无法拆分时按普通 token 处理 |

若可以从 TTS 播放时长得到总时长，应按总时长等比压缩或拉伸时间轴。

### 中文映射

第一版可以使用轻量规则。若项目引入拼音库，可按拼音 final 映射；若没有拼音库，则用常见字典或字符启发式降级到 `small`。

#### 拼音 initial 映射

| Initial | 嘴型 | 说明 |
| --- | --- | --- |
| `b` / `p` / `m` | `closed` | 闭唇起音，持续短 |
| `f` | `teeth` | 唇齿音 |
| `z` / `c` / `s` / `zh` / `ch` / `sh` | `teeth` | 齿音或擦音视觉提示 |
| `j` / `q` / `x` | `teeth` 或 `smile` | 第一版优先 `teeth` |
| `d` / `t` / `n` / `l` / `g` / `k` / `h` / `r` | `small` | 普通辅音 |
| 无 initial | 根据 final 判断 | 直接进入元音嘴型 |

#### 拼音 final 映射

| Final 特征 | 嘴型 | 示例 |
| --- | --- | --- |
| 包含 `a` | `wide` | `a`、`ai`、`an`、`ang`、`iao` |
| 包含 `o`、`u`、`ong`、`ou`、`uo` | `round` | `o`、`ou`、`ong`、`guo` |
| 包含 `i`、`ei`、`en`、`eng`、`ing` | `smile` | `li`、`mei`、`ming` |
| 包含 `e` | `smile` 或 `small` | 第一版优先 `smile` |
| 包含 `ü` / `v` | `round` | `yu`、`lü` |
| 无法判断 | `small` | 降级 |

#### 中文 token 分配

一个中文音节建议拆成两段：

- initial 段：占 token 时长 `25%` 到 `35%`。
- final 段：占 token 时长 `65%` 到 `75%`。

如果 initial 是 `closed` 或 `teeth`，应保留短段；否则可直接用 final 嘴型。

示例：

| 文本 | 近似拼音 | 片段 |
| --- | --- | --- |
| `狗` | `gou` | `small` -> `round` |
| `说` | `shuo` | `teeth` -> `round` |
| `话` | `hua` | `small` -> `wide` |
| `米` | `mi` | `closed` -> `smile` |

### 英文映射

第一版不做完整英文音标，使用字母和常见组合启发式。

| 文本特征 | 嘴型 | 示例 |
| --- | --- | --- |
| `m`、`b`、`p` 开头或独立闭唇音 | `closed` | `my`、`baby`、`puppy` |
| `f`、`v`、`th`、`s`、`z`、`sh`、`ch` | `teeth` | `fish`、`this`、`see` |
| `oo`、`o`、`u`、`w`、`ou` | `round` | `you`、`woof`、`hello` |
| `a`、`ah`、`ar` | `wide` | `happy`、`bark` |
| `e`、`ee`、`i`、`y` | `smile` | `see`、`tiny` |
| 其他有声段 | `small` | 降级 |

英文单词建议拆成 2 到 4 个子片段：

- 起始辅音或字母组合。
- 主元音。
- 尾音。
- 词间短闭口。

### 标点与停顿

| 标点 | 嘴型 | 时长 |
| --- | --- | --- |
| `,` / `，` / `、` | `closed` | 160ms 到 240ms |
| `.` / `。` | `closed` | 260ms 到 420ms |
| `?` / `？` | `closed` | 260ms 到 420ms |
| `!` / `！` | `closed` | 220ms 到 360ms |
| 换行 | `closed` | 260ms 到 420ms |

## 内置示例音频

第一版必须至少有一条稳定示例。

推荐策略：

- 示例音频使用预设时间轴，不完全依赖实时分析。
- 示例时间轴覆盖至少 5 类嘴型：`closed`、`small`、`wide`、`round`、`smile`。
- 可选加入 `teeth` 和 `pant`，用于展示完整能力。
- 调试台显示质量为 `stable`。

示例时间轴规则：

```text
0ms-150ms      closed   准备
150ms-360ms    small    普通起音
360ms-620ms    wide     重音
620ms-840ms    round    圆嘴
840ms-1050ms   smile    咧嘴
1050ms-1230ms  teeth    齿音
1230ms-1450ms  small    普通讲话
1450ms-1700ms  closed   结束停顿
1700ms-2100ms  pant     宠物结束反馈
```

## 上传音频分析

### 第一版目标

上传音频路径提供基础口型驱动，不承诺与真实音素完全匹配。调试台必须标记为 `experimental` 或 `medium`。

### 分析流程

1. 使用 iPadOS 文件选择能力读取音频文件。
2. 使用 `AVFoundation` 解码为可分析的 PCM 数据，例如 `AVAudioPCMBuffer`。
3. 转为 mono：多声道取平均。
4. 按固定窗口分析，建议：
   - `frameSize`: 1024 或 2048 samples。
   - `hopSize`: 512 或 1024 samples。
   - 目标时间分辨率：约 30 FPS，即 33ms 左右。
5. 每帧计算：
   - RMS 音量。
   - Peak 音量。
   - 频谱低/中/高频能量。
   - Spectral centroid。
   - Zero-crossing rate。
6. 使用自适应阈值识别静音与有声段。
7. 将有声段映射到嘴型。
8. 合并和平滑时间轴。

### 帧特征

```ts
interface AudioAnalysisFrame {
  timeMs: number;
  rms: number;
  peak: number;
  lowEnergy: number;
  midEnergy: number;
  highEnergy: number;
  spectralCentroid: number;
  zeroCrossingRate: number;
  voiced: boolean;
}
```

### 静音检测

推荐规则：

```text
noiseFloor = percentile(rmsValues, 15)
speechFloor = max(noiseFloor * 2.5, 0.015)
voiced = rms > speechFloor
```

附加规则：

- 连续静音超过 `100ms` 才切到 `closed`。
- 有声段开始时允许 `40ms` 到 `80ms` attack。
- 有声段结束时允许 `80ms` 到 `120ms` release，避免嘴巴过早闭合。

### 音频特征到嘴型

第一版粗略规则：

| 条件 | 嘴型 | 说明 |
| --- | --- | --- |
| `voiced = false` | `closed` | 静音或停顿 |
| RMS 位于低能量区 | `small` | 轻声 |
| RMS 位于高能量区 | `wide` | 重音或大开口 |
| 低频和中低频占比高，centroid 偏低 | `round` | 近似圆嘴 |
| 中高频占比高但能量不极高 | `smile` | 近似 e/i 明亮音 |
| 高频能量和 zero-crossing rate 偏高 | `teeth` | 近似摩擦音 |
| 无法判断但有声 | `small` | 降级 |

推荐阈值使用相对分位数而不是固定值：

```text
lowRms = percentile(voicedRms, 35)
highRms = percentile(voicedRms, 75)
highCentroid = percentile(centroidValues, 70)
highZcr = percentile(zcrValues, 75)
```

### 解析质量

| Quality | 条件 |
| --- | --- |
| `stable` | 预设时间轴或 TTS 文本驱动 |
| `medium` | 上传音频清晰、静音检测稳定、有声段连续 |
| `experimental` | 麦克风录音、噪声明显、音量过低、无法稳定区分频谱 |

## 麦克风录音分析

### 第一版目标

第一版麦克风不要求实时流式口型。推荐录制后回放并分析，减少 iPadOS 权限处理、实时音频分析和演示稳定性风险。

### 录音流程

1. 用户点击 `开始录音`。
2. 通过 `AVAudioSession` 请求麦克风权限。
3. 授权成功后进入 `listening`。
4. 用户点击 `停止录音`。
5. 使用 `AVAudioRecorder` 或 `AVAudioEngine` 生成可回放音频。
6. 使用与上传音频相同的 `AVFoundation` 分析流程。
7. 回放录音时进入 `speaking` 并按时间轴驱动嘴型。
8. 播放结束进入 `done`，随后回到 `idle`。

### 权限失败

如果用户拒绝权限：

- 宠物进入 `error`。
- 嘴型保持 `closed`。
- 项圈显示黄光。
- 调试台显示输入来源为 `microphone`，质量为 `experimental`。
- UI 显示 `麦克风权限未开启`。
- TTS 和示例音频仍可使用。

### 录音限制

- 第一版建议最长录音 `10s` 到 `15s`。
- 录音少于 `300ms` 应视为无有效输入。
- 音量过低时显示解析质量 `experimental`。

## 时间轴生成规则

### Segment 合并

生成初始片段后执行：

1. 合并相邻相同嘴型。
2. 小于 `80ms` 的片段优先并入相邻置信度更高的片段。
3. 若短片段为 `closed` 且位于两个有声段之间，可保留为自然停顿，但时长至少 `100ms`。
4. 连续高频切换超过 5 次/秒时，降级为更稳定的 `small` / `wide` 组合。

### 最短保持时间

| 嘴型 | 最短时长 |
| --- | --- |
| `closed` | 80ms |
| `small` | 100ms |
| `wide` | 100ms |
| `round` | 100ms |
| `smile` | 100ms |
| `teeth` | 80ms |
| `pant` | 180ms |

### 过渡策略

- 视觉层通过 SwiftUI animation、Canvas 插值或等价渲染方案做 80ms 到 140ms 过渡。
- 时间轴层仍使用离散嘴型，不存储过渡嘴型。
- `closed -> wide` 可以经过视觉插值，但时间轴不必插入 `small`。
- 若视觉上 `closed -> wide` 过猛，可在渲染层自动补过渡，不改变数据层。

## 文本高亮规则

TTS 路径必须提供文本高亮。

规则：

- `textRange` 关联到当前 token。
- 当前播放时间落入 segment 时，高亮该 segment 的 `textRange`。
- 多个 segment 对应同一 token 时，该 token 持续高亮。
- 标点停顿时，可高亮前一个 token 或不高亮；推荐不高亮。
- 上传和麦克风路径没有文本高亮，调试台显示 `无文本时间轴`。

## 状态机联动

| Lip Sync 阶段 | 宠物状态 | 嘴型 |
| --- | --- | --- |
| 时间轴未开始 | `idle` 或 `thinking` | `closed` |
| 预备 0-150ms | `listening` 或 `thinking` | `closed` |
| 播放中 | `speaking` | 根据时间轴 |
| 播放结束 | `done` | `closed`，可短暂 `pant` |
| 错误 | `error` | `closed` |

## 手动测试模式

第一版建议提供开发测试入口，用于验证 7 类嘴型。

要求：

- 每个嘴型可单独触发。
- 手动触发来源为 `manual`。
- 不需要播放音频。
- 调试台显示当前嘴型和来源。
- 手动测试不进入正式播放时间轴，或生成短时间轴用于调试。

## 调试台指标

| 指标 | TTS | 示例 | 上传 | 麦克风 |
| --- | --- | --- | --- | --- |
| 当前嘴型 | 必须 | 必须 | 必须 | 必须 |
| 嘴型时间轴 | 必须 | 必须 | 必须 | 必须 |
| 文本高亮 | 必须 | 可选 | 不适用 | 不适用 |
| 波形 | 可选但建议 | 必须 | 必须 | 必须 |
| 解析质量 | `stable` | `stable` | `medium` / `experimental` | `experimental` |
| 延迟校准 | 必须 | 必须 | 必须 | 必须 |
| 置信度 | 可选 | 可选 | 建议 | 建议 |

## 验收标准

- [ ] 系统定义并支持 7 类嘴型：`closed`、`small`、`wide`、`round`、`smile`、`teeth`、`pant`。
- [ ] 静音、待机、思考、错误状态下嘴型为 `closed`。
- [ ] TTS 文本输入可以生成 `LipSyncTimeline`。
- [ ] TTS 时间轴包含 `textRange`，并驱动文本高亮。
- [ ] 中文输入能根据拼音或降级规则映射到 7 类嘴型。
- [ ] 英文输入能根据字母组合启发式映射到 7 类嘴型。
- [ ] 标点和停顿会生成 `closed` 片段。
- [ ] 内置示例音频可以使用预设时间轴稳定演示。
- [ ] 上传音频可以生成音频分析帧和基础嘴型时间轴。
- [ ] 麦克风录音可录制后分析并生成基础嘴型时间轴。
- [ ] 任意播放路径都能显示当前嘴型、时间轴和输入来源。
- [ ] 延迟校准支持 `-150ms` 到 `+150ms`。
- [ ] 相邻相同嘴型被合并，短片段被平滑处理。
- [ ] 嘴型切换不会高频乱跳，普通播放中不超过 5 次/秒。
- [ ] 上传和麦克风路径显示解析质量，不伪装成高精度音素同步。

## 实施顺序

1. 定义 `MouthShape`、`LipSyncSegment`、`LipSyncTimeline` 数据结构。
2. 实现 7 类嘴型手动测试。
3. 实现 TTS 文本 tokenization 和基础时长估算。
4. 实现中文和英文启发式嘴型映射。
5. 实现时间轴合并、最短保持和平滑。
6. 接入 TTS 播放进度，驱动当前嘴型和文本高亮。
7. 加入延迟校准。
8. 为内置示例配置预设时间轴。
9. 实现上传音频解码和帧分析。
10. 实现上传音频嘴型时间轴生成。
11. 实现麦克风录音、回放和复用音频分析。
12. 完成调试台指标展示和验收测试。

## 后续升级路径

- 接入支持 word timing 或 phoneme timing 的 TTS 服务。
- 引入中文拼音库，提高中文 final 映射准确度。
- 引入英文发音字典，提高英文 viseme 映射准确度。
- 使用强制对齐工具生成示例音频精确时间轴。
- 做麦克风实时流式分析。
- 增加口型置信度可视化。
- 将 `pant` 与情绪系统解耦为宠物表情层，而非语音 viseme 层。
