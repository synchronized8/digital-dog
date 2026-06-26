---
title: "Sprint Change Proposal: 简化为小狗讲话动画 UI"
status: "approved"
created: "2026-06-25"
approved_at: "2026-06-25"
approved_by: "kejincheng"
triggered_by: "用户要求推翻复杂口型同步设计，改为简单小狗动画 UI"
mode: "incremental-assumed"
scope_classification: "major"
---

# Sprint Change Proposal：简化为小狗讲话动画 UI

## 1. Issue Summary

### 1.1 触发问题

用户在 Story 2.2 之后明确提出：

- 之前需求设计太复杂。
- 只需要一个小狗动画 UI。
- 讲话时张口即可，不需要口型对上文字或声音。
- 不讲话时不张口。
- 做一些俏皮动作。
- 之前设计都可以推翻。

### 1.2 当前问题定义

当前产品、UX、架构和 backlog 把 MVP 定义为“数字狗语音口型同步 Demo”，核心围绕：

- 7 类嘴型。
- TTS 文本到 `LipSyncTimeline`。
- 播放时按时间轴计算当前嘴型。
- TTS 文本高亮。
- 波形、嘴型时间轴、延迟校准。
- 上传音频和麦克风录音的基础分析。

这与新的目标冲突。新的目标不是“同步可信”，而是“看起来像一只会讲话的可爱小狗”：讲话状态开口，不讲话闭口，动作俏皮自然。

### 1.3 支持证据

- PRD FR-10、FR-11、FR-12、FR-15、FR-16 明确要求时间轴、文本高亮、上传/麦克风分析、波形和延迟校准。
- Epics 2/3/4 已拆成大量同步、调试和实验音频输入 Story。
- Architecture 强制所有输入统一输出 `LipSyncTimeline`，并要求当前嘴型由播放时间、时间轴和 `latencyOffsetMs` 计算。
- 已完成 Story 2.2 的实现已经进入文本 token、Unicode、标点停顿、`textRange`、segment invariant 等细节，说明复杂度正在偏离新的 MVP。

## 2. Impact Analysis

### 2.1 Checklist Progress

| Checklist Item | Status | Notes |
| --- | --- | --- |
| 1.1 Identify triggering story | [x] Done | Story 2.2 暴露了 TTS 时间轴方向的复杂度。 |
| 1.2 Define core problem | [x] Done | 需求从“精确口型同步”转为“状态驱动讲话动画”。 |
| 1.3 Gather evidence | [x] Done | PRD、Epics、Architecture、UX、LIPSYNC-SPEC 均有冲突条目。 |
| 2.1 Current epic impact | [x] Done | Epic 2 需要重写；Epic 1 大部分可保留。 |
| 2.2 Epic-level changes | [x] Done | Epic 3/4 建议移出 MVP；Epic 2 重定义。 |
| 2.3 Future epic review | [x] Done | 调试台、上传、麦克风同步能力均延期。 |
| 2.4 New/obsolete epics | [x] Done | 不需要新增大 Epic；需要重组现有 Epic。 |
| 2.5 Priority resequencing | [x] Done | 下一步优先做简单讲话动画和俏皮动作。 |
| 3.1 PRD conflicts | [x] Done | 多个 FR 需要删除、降级或改写。 |
| 3.2 Architecture conflicts | [x] Done | `LipSyncTimeline` 不再是所有入口强制核心。 |
| 3.3 UX conflicts | [x] Done | 调试台与同步组件不再是 MVP 主界面。 |
| 3.4 Other artifacts | [x] Done | Story 文件、scope boundary test、sprint status 需要更新。 |
| 4.1 Direct adjustment | [x] Viable | 可通过改写 Story 和后续代码路径完成。 |
| 4.2 Rollback | [x] Not viable | 不建议立即回滚已完成代码，先停止继续扩大复杂度。 |
| 4.3 MVP review | [x] Viable | 必须重定义 MVP 范围。 |
| 4.4 Recommended path | [x] Done | 推荐 Hybrid：MVP Review + backlog reorganization + direct dev implementation。 |

### 2.2 Epic Impact

**Epic 1：Redmi Pad SE 可互动宠物舞台基础**

保留。该 Epic 已经完成大部分基础能力：Android 外壳、宠物舞台、状态机、嘴型渲染、基础动作、可访问性和减少动态。需要调整的是产品解释：7 类嘴型不再是 MVP 目标，后续正式讲话只使用闭口与开口/讲话嘴。

**Epic 2：稳定文字与示例讲话演示**

需要重定义。旧 Epic 2 目标是“TTS 时间轴 + 文本高亮 + 示例时间轴”。新 Epic 2 应改为“小狗讲话状态动画与俏皮反馈”：文本输入或示例只触发 speaking 状态、开口讲话动画、结束反馈和回到 idle。

**Epic 3：口型同步调试与延迟校准**

移出 MVP。当前的波形、嘴型时间轴可视化、文本高亮调试视图、延迟校准都不再支持新的最小目标。

**Epic 4：实验音频输入与可恢复错误**

移出 MVP。上传音频、麦克风录音、音频分析和实验质量标识不再是当前 MVP 必要能力。

### 2.3 Story Impact

| Story | Current Status | Recommendation |
| --- | --- | --- |
| 1.1 | done | 保留。 |
| 1.2 | done | 保留。 |
| 1.3 | done | 保留。 |
| 1.4 | done | 标记为已完成的视觉探索；新 MVP 不再要求 7 类嘴型。 |
| 1.5 | done | 保留并提升重要性，俏皮动作成为核心体验。 |
| 2.1 | done | 保留提交/空文本处理，但改写后续期望：提交触发讲话动画，不生成同步体验。 |
| 2.2 | done | 标记为 superseded by scope change；不要继续基于它扩展。 |
| 2.3 | backlog | 替换为“讲话状态驱动开口与闭口规则”。 |
| 2.4 | backlog | 替换为“俏皮动作编排与讲话结束反馈”。 |
| 2.5 | backlog | 替换为“简化演示入口与 UI 清理”。 |
| 2.6 | backlog | 替换为“简化动画路径测试覆盖”。 |
| Epic 3 stories | backlog | 全部 defer。 |
| Epic 4 stories | backlog | 全部 defer。 |

### 2.4 Artifact Conflicts

**PRD**

- Vision 需要从“口型与声音节奏基本同步”改为“小狗讲话状态动画是否可爱可信”。
- FR-9 需要从 7 类嘴型改为闭口/讲话开口。
- FR-10、FR-11、FR-12、FR-15、FR-16 建议移出 MVP。
- FR-5、FR-6 改为触发讲话动画，不要求文本高亮、时间轴或多嘴型覆盖。
- 成功指标从“口型覆盖能力、调试可解释性、实验入口可用性”改为“讲话开闭口规则、俏皮动作感、主流程完成率”。

**Architecture**

- 删除或降级“所有音频入口必须输出 `LipSyncTimeline`”。
- 删除或降级“当前嘴型必须由播放时间 + 时间轴 + `latencyOffsetMs` 计算”。
- 新增简单模型：`SpeechAnimationState` 或等价状态，包含 `isSpeaking`、`mouthOpen`、`actionCue`、`startedAtMs`、`estimatedDurationMs`。
- `DogRenderer` 只需要消费 `PetState`、`isSpeaking`、`mouthOpen` 和动作提示。
- `LipSyncTimeline` 代码可暂时保留为 legacy/internal，后续实施时删除或断开主路径。

**UX**

- 默认界面从“宠物舞台 + 同步调试台”改为“宠物舞台 + 简单输入/示例入口 + 轻量状态摘要”。
- 移除 MVP 中的波形、嘴型时间轴、文本高亮、延迟校准。
- 强化小狗动作：待机眨眼/呼吸、点击后耳朵竖起或歪头、讲话中轻微弹耳/摇尾/身体小幅律动、结束眨眼或摇尾。

## 3. Recommended Approach

### 3.1 Recommendation

采用 **Hybrid：MVP Review + Backlog Reorganization + Direct Developer Implementation**。

具体策略：

1. 先批准本 Sprint Change Proposal。
2. 更新 PRD、Epics、Architecture、UX 和 sprint status。
3. 将 Epic 3/4 移出 MVP 或标记 deferred。
4. 将 Story 2.3-2.6 替换为简化讲话动画方向。
5. 由 Developer agent 执行新的 Story 2.3：讲话状态驱动开口/闭口与俏皮动作。

### 3.2 Why Not Rollback

不建议立即做大回滚：

- Story 1.1-1.5 的舞台、状态机、嘴型渲染和动作基础仍有价值。
- Story 2.1 的 TTS 输入、空文本错误和 busy session 保护仍可复用。
- Story 2.2 的 timeline 代码虽然不再是产品主路径，但可以先断开或标记 legacy，后续清理比立即回滚风险更低。

### 3.3 Effort and Risk

| Area | Effort | Risk | Notes |
| --- | --- | --- | --- |
| 文档重写 | Medium | Low | 需要改 PRD/Epics/Architecture/UX 中的核心范围。 |
| Backlog 重组 | Low | Low | 主要是替换 2.3-2.6、defer Epic 3/4。 |
| 代码实现 | Medium | Medium | 需要绕开或删除 timeline 主路径，改成 speaking animation state。 |
| 测试更新 | Medium | Medium | 旧 timeline 测试要改为 legacy 或删除；新增开闭口/动作测试。 |

## 4. Detailed Change Proposals

### 4.1 PRD Changes

#### Section: Vision

OLD:

```md
第一版不接真实 AI 对话，重点验证两件事：数字狗是否足够可爱好看，以及口型是否能与声音节奏基本同步。
```

NEW:

```md
第一版不接真实 AI 对话，重点验证一件事：数字狗在讲话状态下是否足够可爱、自然、可信。系统只要求讲话时张口或呈现简单讲话嘴，不要求嘴型与文本、音素或声音节奏精确对齐；不讲话、思考、错误和待机时必须闭口。
```

Rationale: 新目标明确取消精确口型同步，把成功标准转向宠物动画表现。

#### Section: Terms

OLD:

```md
嘴型 — 数字狗嘴巴的离散视觉状态。第一版包含 `closed`、`small`、`wide`、`round`、`smile`、`teeth`、`pant`。
嘴型时间轴 — 按时间排列的嘴型片段，用于驱动播放中的嘴型变化。
```

NEW:

```md
讲话嘴 — 数字狗讲话状态下的简单开口表现，可以是一个固定开口或轻微开合循环。
闭口 — 数字狗在待机、思考、错误、完成和未讲话状态下的默认嘴巴状态。
俏皮动作 — 小狗通过眨眼、耳朵、歪头、轻摇尾或身体小幅律动表达生命感的动画。
```

Rationale: 去掉时间轴心智负担，保留用户可感知的视觉状态。

#### Section: Functional Requirements

OLD:

```md
FR-9：系统支持 `closed`、`small`、`wide`、`round`、`smile`、`teeth`、`pant` 七类嘴型。
FR-10：系统可以根据文本为 TTS 路径生成嘴型时间轴，并在播放时驱动嘴型变化。
FR-11：TTS 播放过程中，系统显示与嘴型时间轴同步的文本高亮。
FR-15：调试台显示音频波形和嘴型时间轴。
FR-16：用户可以在调试台调整嘴型相对音频的延迟。
```

NEW:

```md
FR-9：系统支持至少两种嘴巴状态：`closed` 和 `talking/open`。未讲话时必须为 `closed`；讲话时必须进入可观察的张口或简单开合讲话嘴。
FR-10：TTS 或示例触发后，系统根据讲话 session 状态驱动小狗动画，不要求生成嘴型时间轴。
FR-11：第一版不要求文本高亮。
FR-15：第一版不要求波形和嘴型时间轴展示。
FR-16：第一版不要求延迟校准。
```

Rationale: 直接移除新范围不需要的同步调试要求。

### 4.2 Epic Changes

#### Epic 2

OLD:

```md
Epic 2：稳定文字与示例讲话演示

用户可以通过 TTS 主入口和内置示例稳定触发数字狗讲话，看到准备、讲话、结束、回待机的完整循环，并观察到嘴型时间轴和文本高亮同步推进。
```

NEW:

```md
Epic 2：简单讲话动画与俏皮反馈

用户可以通过文本主入口或示例入口触发数字狗讲话动画，看到准备、讲话、结束、回待机的完整循环。讲话时小狗张口或轻微开合，不讲话时闭口；系统不要求文本、音素或声音节奏级同步。
```

Rationale: Epic 2 仍承接主体验，但删除 timeline/highlight 目标。

#### Epic 3

OLD:

```md
Epic 3：口型同步调试与延迟校准
```

NEW:

```md
Epic 3：Deferred - 口型同步调试与延迟校准

该 Epic 移出当前 MVP。只有当后续重新确认需要精确口型同步时，再恢复波形、时间轴、文本高亮和延迟校准。
```

Rationale: 新 MVP 不需要同步调试台。

#### Epic 4

OLD:

```md
Epic 4：实验音频输入与可恢复错误
```

NEW:

```md
Epic 4：Deferred - 实验音频输入与可恢复错误

上传音频、麦克风录音和音频分析移出当前 MVP。当前 MVP 只保留文本/示例触发的小狗讲话动画和基础错误恢复。
```

Rationale: 上传和麦克风是复杂度来源，不再符合最小目标。

### 4.3 Story Changes

#### Story 2.3 Replacement

OLD:

```md
Story 2.3：时间轴平滑、闭口规则与当前嘴型计算
```

NEW:

```md
Story 2.3：讲话状态驱动开口与闭口规则

As a 评审者,
I want 点击“让狗狗说话”后看到小狗张口讲话，不讲话时闭口,
So that 我能快速判断这是一只会回应的可爱数字狗，而不是复杂同步工具。

Acceptance Criteria:

1. Given App 处于 idle，When 用户提交非空文本，Then 小狗进入 preparing/speaking 流程，准备阶段嘴巴保持 closed。
2. Given 小狗进入 speaking，When 讲话动画运行，Then 嘴巴进入 talking/open 状态或轻微开合循环，不要求与文本或声音对齐。
3. Given 小狗未处于 speaking，When 用户观察待机、思考、错误或完成状态，Then 嘴巴必须为 closed。
4. Given 讲话动画结束，When 完成反馈结束，Then 小狗回到 idle 且嘴巴 closed。
5. Given 本 Story 的简化范围，When 开发者实现，Then 不新增波形、文本高亮、延迟校准、上传音频、麦克风录音或音频分析。
```

#### Story 2.4 Replacement

OLD:

```md
Story 2.4：TTS 播放同步与文本高亮
```

NEW:

```md
Story 2.4：俏皮动作编排与讲话结束反馈

As a 评审者,
I want 小狗在准备、讲话和结束时有俏皮但克制的动作,
So that 它看起来像有生命的小宠物，而不是静态开合嘴。

Acceptance Criteria:

1. 讲话前出现短暂耳朵竖起、看向用户或歪头准备动作，嘴巴仍 closed。
2. 讲话中至少出现一种低幅度俏皮动作，例如轻微头部点动、耳朵轻弹、尾巴小幅摆动或身体律动。
3. 讲话结束后出现一次短反馈，例如眨眼、摇尾或开心表情，然后回到 idle。
4. 减少动态模式下保留讲话开口，降低装饰性动作。
5. 所有动作不得导致布局跳动或遮挡嘴巴区域。
```

#### Story 2.5 Replacement

OLD:

```md
Story 2.5：内置示例稳定演示
```

NEW:

```md
Story 2.5：简化演示入口与 UI 清理

As a 使用者,
I want 首屏只有小狗舞台、文本输入、让狗狗说话和示例入口,
So that 我能直接试小狗讲话动画，不被调试项干扰。

Acceptance Criteria:

1. 首屏保留宠物舞台、TTS 输入、主 CTA 和示例入口。
2. 调试台降级为轻量状态摘要，最多显示当前状态、嘴巴状态和输入来源。
3. 不显示波形、嘴型时间轴、文本高亮或延迟校准。
4. 上传音频和麦克风入口从 MVP UI 移除或隐藏到非默认开发入口。
```

#### Story 2.6 Replacement

OLD:

```md
Story 2.6：TTS 与示例路径的逻辑测试覆盖
```

NEW:

```md
Story 2.6：简化讲话动画测试覆盖

As a 开发者,
I want 用测试覆盖讲话状态、开闭口规则和俏皮动作边界,
So that 后续 UI 调整不会破坏最小讲话体验。

Acceptance Criteria:

1. 逻辑测试覆盖：非空提交进入 speaking session；空输入进入 error；busy 状态不创建第二 session。
2. 逻辑测试覆盖：speaking 时嘴巴为 talking/open；非 speaking 状态嘴巴为 closed。
3. UI 测试覆盖：点击主 CTA 后舞台语义显示正在讲话和张口状态。
4. Boundary 测试覆盖：MVP 不新增波形、时间轴 UI、文本高亮、延迟校准、上传、麦克风或新音频分析依赖。
```

### 4.4 Architecture Changes

#### Section: Core Architecture Rule

OLD:

```md
所有音频入口必须统一输出 `LipSyncTimeline`，禁止 TTS、示例、上传、麦克风各自维护私有时间轴结构。
当前嘴型必须通过播放时间、`LipSyncTimeline` 和 `latencyOffsetMs` 计算，不允许绕过时间轴直接驱动嘴型。
```

NEW:

```md
当前 MVP 不要求 `LipSyncTimeline`。TTS 和示例入口只需创建简单的讲话动画 session，由 `PetState` / `SpeechAnimationState` 驱动嘴巴开闭和俏皮动作。

`LipSyncTimeline` 如已存在，可暂时作为 legacy/internal 代码保留，但不得作为当前 MVP 的强制路径。未来若重新引入口型同步，需要通过新的 scope change 恢复。
```

Rationale: 让代码路径匹配新的产品目标，避免继续投资同步系统。

#### Section: Recommended Models

NEW:

```kotlin
data class SpeechAnimationState(
    val isSpeaking: Boolean,
    val mouthOpen: Boolean,
    val actionCue: DogActionCue,
    val estimatedDurationMs: Int,
)

enum class DogActionCue {
    None,
    EarPerk,
    HeadTilt,
    TailWag,
    Blink,
    BodyBounce,
}
```

Rationale: 用简单状态表达新 MVP，而不是用 segment timeline 表达精确同步。

### 4.5 UX Changes

#### Section: Default Layout

OLD:

```md
iPad 横屏采用宠物舞台 + 调试台双栏：左侧或主区域展示数字狗，右侧展示同步调试；底部或主舞台下方放置 TTS 输入和快速入口。
```

NEW:

```md
Redmi Pad SE 横屏默认采用宠物舞台优先布局：小狗舞台占据主视觉，输入区靠近舞台，右侧或下方只保留轻量状态摘要。当前 MVP 不默认展示同步调试台。
```

Rationale: 用户新的目标是小狗动画体验，而不是同步调试工具。

#### Section: Components

OLD:

```md
调试台保留当前嘴型、输入来源、解析质量、波形、时间轴、文本高亮和延迟校准。
```

NEW:

```md
状态摘要只保留当前宠物状态、嘴巴状态和输入来源。波形、时间轴、文本高亮和延迟校准移出 MVP。
```

Rationale: 降低界面和实现复杂度，突出小狗本体。

## 5. Implementation Handoff

### 5.1 Scope Classification

**Major scope** for planning artifacts, because PRD、Epics、Architecture、UX 的核心目标发生变化。

**Moderate implementation** for code, because已有宠物舞台、状态机、嘴型渲染和基础动作可复用，下一步主要是断开 timeline 主路径并实现简单 speaking animation。

### 5.2 Handoff Recipients

- **Product Manager / Architect:** 批准本 proposal 后，更新 PRD、Epics、Architecture、UX，明确新 MVP。
- **Developer agent:** 根据更新后的 Story 2.3 执行实现，避免继续推进旧 Story 2.3-2.6。
- **Code Review agent:** 审查是否彻底避免新增波形、timeline UI、文本高亮、延迟校准、上传和麦克风范围。

### 5.3 Success Criteria

新的 MVP 成功标准：

1. 小狗未讲话时嘴巴始终 closed。
2. 点击非空文本或示例后，小狗进入 speaking 并明显张口。
3. 讲话不要求匹配文本、音素、声音节奏或时间轴。
4. 讲话中至少有一种俏皮动作，结束后有短反馈。
5. 空输入或错误时小狗闭口，并且可恢复。
6. 首屏不出现波形、嘴型时间轴、文本高亮、延迟校准、上传音频、麦克风录音等旧 MVP 复杂能力。

### 5.4 Immediate Next Actions After Approval

1. Update `docs/planning-artifacts/prds/prd-digital-dog-2026-06-23/prd.md`.
2. Update `docs/MVP-SPEC.md`, `docs/UI-SPEC.md`, and `docs/LIPSYNC-SPEC.md` or mark `LIPSYNC-SPEC.md` as superseded for current MVP.
3. Update `docs/planning-artifacts/epics.md`.
4. Update `docs/planning-artifacts/architecture.md`.
5. Update `docs/planning-artifacts/ux-design-specification.md`.
6. Update `docs/implementation-artifacts/sprint-status.yaml`.
7. Create/rewrite Story 2.3 for simplified speaking animation.
8. Run `$bmad-dev-story 2.3` against the simplified story.

## 6. Approval

Approved by user response: `c`.

Implementation route: update planning artifacts, reorganize backlog, create simplified Story 2.3, then route to Developer agent for implementation.
