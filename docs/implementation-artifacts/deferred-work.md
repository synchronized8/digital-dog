# Deferred Work

## Deferred from: code review of 2-4-俏皮动作编排与讲话结束反馈 (2026-06-26)

- `nextSpeechSessionIndex` 到达 `Int.MAX_VALUE` 后会溢出为负数，极端长会话下可能产生 session id 碰撞。Source: `DigitalDog/app/src/main/java/com/digitaldog/demo/state/TtsSubmitReducer.kt:83`.
- 响应式布局分支覆盖在累计变更中减少，需要后续补回专门测试。Source: `DigitalDog/app/src/androidTest/java/com/digitaldog/demo/DigitalDogAppTest.kt:1`.
- 首屏无障碍描述断言在累计变更中减少，需要后续补回输入区和示例入口语义覆盖。Source: `DigitalDog/app/src/androidTest/java/com/digitaldog/demo/DigitalDogAppTest.kt:1`.
- 顶栏状态同步断言在累计变更中减少，需要后续补回状态栏描述覆盖。Source: `DigitalDog/app/src/androidTest/java/com/digitaldog/demo/DigitalDogAppTest.kt:1`.

## Deferred from: code review of 2-6-简化讲话动画测试覆盖 (2026-06-26)

- `SpeechSessionStatus` currently has only `Pending` and is not synchronized through the speech lifecycle. This is a pre-existing model cleanup item, because `petState`/`SpeechAnimationState` are the active lifecycle sources for Story 2.6. Source: `DigitalDog/app/src/main/java/com/digitaldog/demo/sharedmodel/SpeechSession.kt:7`.
