package com.digitaldog.demo

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import com.digitaldog.demo.accessibility.AndroidAnimatorScaleProvider
import com.digitaldog.demo.accessibility.ReduceMotionPolicy
import com.digitaldog.demo.app.AppContentContract
import com.digitaldog.demo.app.AppLayoutMode
import com.digitaldog.demo.app.DigitalDogApp
import com.digitaldog.demo.dogrenderer.DogMotionProfile
import com.digitaldog.demo.sharedmodel.InputSource
import com.digitaldog.demo.sharedmodel.MouthShape
import com.digitaldog.demo.sharedmodel.PetState
import com.digitaldog.demo.sharedmodel.SpeechSession
import com.digitaldog.demo.state.DefaultCompletionFeedbackDurationMs
import com.digitaldog.demo.state.DefaultSpeechAnimationDurationMs
import com.digitaldog.demo.state.DogActionCue
import com.digitaldog.demo.state.SpeechAnimationState
import com.digitaldog.demo.state.SpeechDemoState
import com.digitaldog.demo.state.toPresentation
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class DigitalDogAppTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainActivityShowsCurrentMvpFirstScreen() {
        composeRule.onNodeWithText(AppContentContract.Title).assertIsDisplayed()
        composeRule.onNodeWithTag(AppContentContract.TagStatusBar).assertIsDisplayed()
        composeRule.onNodeWithTag(AppContentContract.TagPetStage).assertIsDisplayed()
        composeRule.onNodeWithTag(AppContentContract.TagSpeechInput).assertIsDisplayed()
        composeRule.onNodeWithTag(AppContentContract.TagQuickActions).assertIsDisplayed()
        composeRule.onNodeWithTag(AppContentContract.TagStatusSummary).assertDisplayedAfterScroll()
        composeRule.onNodeWithText(AppContentContract.PrimaryCta).assertDisplayedAfterScroll()
        composeRule.onNodeWithText(AppContentContract.SampleAudio).assertDisplayedAfterScroll()

        composeRule.onAllNodesWithText("上传音频").assertCountEquals(0)
        composeRule.onAllNodesWithText("开始录音").assertCountEquals(0)
        composeRule.onAllNodesWithText("麦克风").assertCountEquals(0)
        composeRule.onAllNodesWithText("波形").assertCountEquals(0)
        composeRule.onAllNodesWithText("嘴型时间轴").assertCountEquals(0)
        composeRule.onAllNodesWithText("文本高亮").assertCountEquals(0)
        composeRule.onAllNodesWithText("延迟校准").assertCountEquals(0)
        composeRule.onAllNodesWithText("同步调试").assertCountEquals(0)
        composeRule.onAllNodesWithText("手动嘴型测试").assertCountEquals(0)

        composeRule
            .onNodeWithContentDescription(
                expectedStageDescription(
                    uiState = SpeechDemoState(),
                    motionPolicy = AndroidAnimatorScaleProvider.currentPolicy(),
                ),
            )
            .assertIsDisplayed()
        composeRule
            .onNodeWithContentDescription(
                AppContentContract.SpeechInputDescription,
                useUnmergedTree = true,
            )
            .assertExists()
        composeRule
            .onNodeWithContentDescription(
                AppContentContract.QuickActionsDescription,
                useUnmergedTree = true,
            )
            .assertExists()
    }

    @Test
    fun appSupportsLandscapeAndPortraitWithStatusSummary() {
        composeRule.activity.setContent {
            RoomyLandscapeHost {
                DigitalDogApp(
                    layoutMode = AppLayoutMode.Landscape,
                    autoAdvanceSpeech = false,
                )
            }
        }

        composeRule.onNodeWithTag(AppContentContract.TagStatusSummary).assertIsDisplayed()
        composeRule.onNodeWithText(AppContentContract.StatusSummaryTitle).assertIsDisplayed()
        composeRule.onNodeWithText(AppContentContract.CurrentStateIdle).assertIsDisplayed()

        composeRule.activity.setContent {
            DigitalDogApp(layoutMode = AppLayoutMode.Portrait)
        }

        composeRule.onNodeWithTag(AppContentContract.TagStatusSummary).assertDisplayedAfterScroll()
        composeRule.onNodeWithText(AppContentContract.StatusSummaryTitle).assertDisplayedAfterScroll()
        composeRule.onNodeWithText(AppContentContract.CurrentStateIdle).assertDisplayedAfterScroll()
    }

    @Test
    fun injectedStatesSyncTopBarStageAndStatusSummary() {
        listOf(
            PetState.Idle,
            PetState.Listening,
            PetState.Thinking,
            PetState.Speaking,
            PetState.Done,
            PetState.Error,
        ).forEach { petState ->
            val uiState = SpeechDemoState(petState = petState)
            val presentation = uiState.toPresentation()

            composeRule.activity.setContent {
                RoomyLandscapeHost {
                    DigitalDogApp(
                        layoutMode = AppLayoutMode.Landscape,
                        uiState = uiState,
                    )
                }
            }

            composeRule
                .onNodeWithContentDescription(
                    AppContentContract.statusBarDescription(
                        stateLabel = presentation.stateLabel,
                        inputSourceLabel = uiState.inputSource.label,
                        mouthStateLabel = if (petState == PetState.Speaking) {
                            AppContentContract.MouthStateTalking
                        } else {
                            AppContentContract.MouthStateClosed
                        },
                        stateDescription = presentation.stateDescription,
                        collarDescription = presentation.collar.description,
                    ),
                    useUnmergedTree = true,
                )
                .assertExists()
            composeRule
                .onNodeWithContentDescription(
                    expectedStageDescription(
                        uiState = uiState,
                        motionPolicy = ReduceMotionPolicy.Normal,
                    ),
                    useUnmergedTree = true,
                )
                .assertExists()
            composeRule
                .onNodeWithContentDescription(
                    AppContentContract.statusSummaryDescription(
                        mouthStateLabel = if (petState == PetState.Speaking) {
                            AppContentContract.MouthStateTalking
                        } else {
                            AppContentContract.MouthStateClosed
                        },
                        stateLabel = presentation.stateLabel,
                        inputSourceLabel = uiState.inputSource.label,
                        collarDescription = presentation.collar.description,
                    ),
                    useUnmergedTree = true,
                )
                .assertExists()
            composeRule.onNodeWithText(AppContentContract.currentStateText(presentation.stateLabel)).assertIsDisplayed()
        }
    }

    @Test
    fun textSubmitShowsPreparingThenSpeakingMouthAndDisablesPrimaryCta() {
        composeRule.mainClock.autoAdvance = false
        try {
            composeRule.activity.setContent {
                RoomyLandscapeHost {
                    DigitalDogApp(layoutMode = AppLayoutMode.Landscape)
                }
            }

            composeRule
                .onNodeWithTag(AppContentContract.TagTtsInputField)
                .performTextInput("你好数字狗")
            composeRule
                .onNodeWithTag(AppContentContract.TagPrimaryTtsCta)
                .performClick()

            val preparingState = SpeechDemoState(
                petState = PetState.Thinking,
                currentMouth = MouthShape.Closed,
                inputSource = InputSource.Tts,
                speechAnimationState = SpeechAnimationState.Preparing,
            )
            composeRule
                .onNodeWithContentDescription(
                    expectedStageDescription(
                        uiState = preparingState,
                        motionPolicy = ReduceMotionPolicy.Normal,
                    ),
                    useUnmergedTree = true,
                )
                .assertExists()
            composeRule.onNodeWithText(AppContentContract.mouthStateText(AppContentContract.MouthStateClosed)).assertIsDisplayed()
            composeRule.onNodeWithText(AppContentContract.PrimaryCtaBusy).assertIsDisplayed()
            composeRule.onNodeWithTag(AppContentContract.TagPrimaryTtsCta).assertIsNotEnabled()
            composeRule.onNodeWithTag(AppContentContract.TagSampleCta).assertIsNotEnabled()

            composeRule.mainClock.advanceTimeBy(SpeechAnimationState.Preparing.estimatedDurationMs.toLong() + 1L)
            composeRule.waitForIdle()

            val expectedState = SpeechDemoState(
                petState = PetState.Speaking,
                currentMouth = MouthShape.Open,
                inputSource = InputSource.Tts,
                speechAnimationState = SpeechAnimationState.Speaking,
            )
            val presentation = expectedState.toPresentation()

            composeRule
                .onNodeWithContentDescription(
                    expectedStageDescription(
                        uiState = expectedState,
                        motionPolicy = ReduceMotionPolicy.Normal,
                    ),
                    useUnmergedTree = true,
                )
                .assertExists()
            composeRule
                .onNodeWithContentDescription(
                    AppContentContract.statusSummaryDescription(
                        mouthStateLabel = AppContentContract.MouthStateTalking,
                        stateLabel = presentation.stateLabel,
                        inputSourceLabel = expectedState.inputSource.label,
                        collarDescription = presentation.collar.description,
                    ),
                    useUnmergedTree = true,
                )
                .assertExists()
            composeRule.onNodeWithText(AppContentContract.mouthStateText(AppContentContract.MouthStateTalking)).assertIsDisplayed()
            composeRule.onNodeWithText(AppContentContract.inputSourceText(InputSource.Tts.label)).assertIsDisplayed()
            composeRule.onNodeWithText(AppContentContract.PrimaryCtaBusy).assertIsDisplayed()
            composeRule.onNodeWithTag(AppContentContract.TagPrimaryTtsCta).assertIsNotEnabled()
            composeRule.onNodeWithTag(AppContentContract.TagSampleCta).assertIsNotEnabled()
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }

    @Test
    fun sampleSubmitCompletesFeedbackAndRestoresActions() {
        composeRule.mainClock.autoAdvance = false
        try {
            composeRule.activity.setContent {
                RoomyLandscapeHost {
                    DigitalDogApp(layoutMode = AppLayoutMode.Landscape)
                }
            }

            composeRule
                .onNodeWithTag(AppContentContract.TagSampleCta)
                .performClick()

            val preparingState = SpeechDemoState(
                petState = PetState.Thinking,
                currentMouth = MouthShape.Closed,
                inputSource = InputSource.Sample,
                speechAnimationState = SpeechAnimationState.Preparing,
            )
            composeRule
                .onNodeWithContentDescription(
                    expectedStageDescription(
                        uiState = preparingState,
                        motionPolicy = ReduceMotionPolicy.Normal,
                    ),
                    useUnmergedTree = true,
                )
                .assertExists()
            composeRule.onNodeWithText(AppContentContract.PrimaryCtaBusy).assertIsDisplayed()
            composeRule.onNodeWithTag(AppContentContract.TagPrimaryTtsCta).assertIsNotEnabled()
            composeRule.onNodeWithTag(AppContentContract.TagSampleCta).assertIsNotEnabled()
            composeRule.onNodeWithText(AppContentContract.inputSourceText(InputSource.Sample.label)).assertIsDisplayed()

            composeRule.mainClock.advanceTimeBy(SpeechAnimationState.Preparing.estimatedDurationMs.toLong() + 1L)
            composeRule.waitForIdle()

            val speakingState = SpeechDemoState(
                petState = PetState.Speaking,
                currentMouth = MouthShape.Open,
                inputSource = InputSource.Sample,
                speechAnimationState = SpeechAnimationState.Speaking,
            )
            composeRule
                .onNodeWithContentDescription(
                    expectedStageDescription(
                        uiState = speakingState,
                        motionPolicy = ReduceMotionPolicy.Normal,
                    ),
                    useUnmergedTree = true,
                )
                .assertExists()
            composeRule.onNodeWithText(AppContentContract.mouthStateText(AppContentContract.MouthStateTalking)).assertIsDisplayed()

            composeRule.mainClock.advanceTimeBy(DefaultSpeechAnimationDurationMs.toLong() + 1L)
            composeRule.waitForIdle()

            val doneState = SpeechDemoState(
                petState = PetState.Done,
                currentMouth = MouthShape.Closed,
                inputSource = InputSource.Sample,
                speechAnimationState = SpeechAnimationState.Done,
            )
            composeRule
                .onNodeWithContentDescription(
                    expectedStageDescription(
                        uiState = doneState,
                        motionPolicy = ReduceMotionPolicy.Normal,
                    ),
                    useUnmergedTree = true,
                )
                .assertExists()
            composeRule.onNodeWithTag(AppContentContract.TagPrimaryTtsCta).assertIsEnabled()
            composeRule.onNodeWithTag(AppContentContract.TagSampleCta).assertIsEnabled()

            composeRule.mainClock.advanceTimeBy(DefaultCompletionFeedbackDurationMs.toLong() + 1L)
            composeRule.waitForIdle()

            composeRule.onNodeWithText(AppContentContract.CurrentStateIdle).assertIsDisplayed()
            composeRule.onNodeWithTag(AppContentContract.TagSampleCta).assertIsEnabled()
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }

    @Test
    fun emptySubmitShowsRecoverableErrorAndAllowsRetry() {
        composeRule.activity.setContent {
            DigitalDogApp(
                layoutMode = AppLayoutMode.Portrait,
                autoAdvanceSpeech = false,
            )
        }

        composeRule
            .onNodeWithTag(AppContentContract.TagPrimaryTtsCta)
            .performScrollTo()
            .performClick()

        composeRule.onNodeWithText(AppContentContract.EmptyTtsInputError).assertIsDisplayed()

        val errorState = SpeechDemoState(
            petState = PetState.Error,
            inputSource = InputSource.Tts,
        )
        composeRule
            .onNodeWithContentDescription(
                expectedStageDescription(
                    uiState = errorState,
                    motionPolicy = ReduceMotionPolicy.Normal,
                ),
                useUnmergedTree = true,
            )
            .assertExists()

        composeRule
            .onNodeWithTag(AppContentContract.TagTtsInputField)
            .performTextInput("重试一句话")
        composeRule.onAllNodesWithText(AppContentContract.EmptyTtsInputError).assertCountEquals(0)
        composeRule
            .onNodeWithTag(AppContentContract.TagPrimaryTtsCta)
            .performClick()

        composeRule.onNodeWithText(AppContentContract.PrimaryCtaBusy).assertIsDisplayed()
        composeRule.onNodeWithText(AppContentContract.currentStateText(AppContentContract.StatusThinking)).assertDisplayedAfterScroll()
        composeRule.onNodeWithText(AppContentContract.mouthStateText(AppContentContract.MouthStateClosed)).assertDisplayedAfterScroll()
    }

    @Test
    fun primaryActionsKeepMinimumTouchTarget() {
        composeRule.activity.setContent {
            Box(modifier = Modifier.size(width = 900.dp, height = 600.dp)) {
                DigitalDogApp()
            }
        }

        composeRule
            .onNode(hasText(AppContentContract.PrimaryCta) and hasClickAction())
            .assertHeightIsAtLeast(48.dp)
        composeRule
            .onNode(hasText(AppContentContract.SampleAudio) and hasClickAction())
            .assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun compactPortraitKeepsCoreDemoControlsReachableAndReadable() {
        composeRule.activity.setContent {
            Box(modifier = Modifier.size(width = 360.dp, height = 720.dp)) {
                DigitalDogApp(layoutMode = AppLayoutMode.Portrait)
            }
        }

        val textInput = composeRule.onNodeWithTag(AppContentContract.TagTtsInputField)
        val primaryCta = composeRule.onNodeWithTag(AppContentContract.TagPrimaryTtsCta)
        val sampleCta = composeRule.onNodeWithTag(AppContentContract.TagSampleCta)

        composeRule.onNodeWithTag(AppContentContract.TagPetStage).assertIsDisplayed()
        composeRule.onNodeWithTag(AppContentContract.TagSpeechInput).assertDisplayedAfterScroll()
        textInput.assertDisplayedAfterScroll()
        primaryCta.assertDisplayedAfterScroll()
        sampleCta.assertDisplayedAfterScroll()
        composeRule.onNodeWithTag(AppContentContract.TagStatusSummary).assertDisplayedAfterScroll()
        primaryCta
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(120.dp)
        sampleCta
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(120.dp)

        primaryCta.performScrollTo()
        sampleCta.assertIsDisplayed()
        assertBoundsDoNotOverlap(primaryCta, sampleCta, "primary CTA", "sample CTA")

        textInput.performScrollTo()
        primaryCta.assertIsDisplayed()
        assertBoundsDoNotOverlap(textInput, primaryCta, "text input", "primary CTA")
    }

    @Test
    fun injectedOpenMouthWithoutSpeakingReportsOpenMouthSemantics() {
        val openMouthIdle = SpeechDemoState(
            petState = PetState.Idle,
            currentMouth = MouthShape.Open,
            speechAnimationState = SpeechAnimationState.Idle,
        )
        val presentation = openMouthIdle.toPresentation()

        composeRule.activity.setContent {
            RoomyLandscapeHost {
                DigitalDogApp(
                    layoutMode = AppLayoutMode.Landscape,
                    uiState = openMouthIdle,
                    autoAdvanceSpeech = false,
                )
            }
        }

        composeRule
            .onNodeWithContentDescription(
                AppContentContract.statusBarDescription(
                    stateLabel = presentation.stateLabel,
                    inputSourceLabel = openMouthIdle.inputSource.label,
                    mouthStateLabel = AppContentContract.MouthStateOpen,
                    stateDescription = presentation.stateDescription,
                    collarDescription = presentation.collar.description,
                ),
                useUnmergedTree = true,
            )
            .assertExists()
        composeRule
            .onNodeWithContentDescription(
                AppContentContract.statusSummaryDescription(
                    mouthStateLabel = AppContentContract.MouthStateOpen,
                    stateLabel = presentation.stateLabel,
                    inputSourceLabel = openMouthIdle.inputSource.label,
                    collarDescription = presentation.collar.description,
                ),
                useUnmergedTree = true,
            )
            .assertExists()
        composeRule
            .onNodeWithContentDescription(
                expectedStageDescription(
                    uiState = openMouthIdle,
                    motionPolicy = ReduceMotionPolicy.Normal,
                ),
                useUnmergedTree = true,
            )
            .assertExists()
        composeRule
            .onNodeWithText(AppContentContract.mouthStateText(AppContentContract.MouthStateOpen))
            .assertIsDisplayed()
    }

    @Test
    fun defaultTalkBackSemanticsNameMainFlowControlsAndValues() {
        val idle = SpeechDemoState()
        val presentation = idle.toPresentation()

        composeRule.activity.setContent {
            RoomyLandscapeHost {
                DigitalDogApp(
                    layoutMode = AppLayoutMode.Landscape,
                    autoAdvanceSpeech = false,
                )
            }
        }

        composeRule
            .onNodeWithContentDescription(
                AppContentContract.statusBarDescription(
                    stateLabel = presentation.stateLabel,
                    inputSourceLabel = idle.inputSource.label,
                    mouthStateLabel = AppContentContract.MouthStateClosed,
                    stateDescription = presentation.stateDescription,
                    collarDescription = presentation.collar.description,
                ),
                useUnmergedTree = true,
            )
            .assertExists()
        composeRule
            .onNodeWithContentDescription(
                AppContentContract.ttsInputDescription(
                    errorText = null,
                    isBusy = false,
                ),
                useUnmergedTree = true,
            )
            .assertExists()
        composeRule
            .onNodeWithContentDescription(
                AppContentContract.primaryTtsCtaDescription(isBusy = false),
                useUnmergedTree = true,
            )
            .assertExists()
        composeRule
            .onNodeWithContentDescription(
                AppContentContract.statusSummaryDescription(
                    mouthStateLabel = AppContentContract.MouthStateClosed,
                    stateLabel = presentation.stateLabel,
                    inputSourceLabel = idle.inputSource.label,
                    collarDescription = presentation.collar.description,
                ),
                useUnmergedTree = true,
            )
            .assertExists()

        composeRule.onNodeWithTag(AppContentContract.TagPrimaryTtsCta).performClick()
        composeRule
            .onNodeWithContentDescription(
                AppContentContract.ttsInputDescription(
                    errorText = AppContentContract.EmptyTtsInputError,
                    isBusy = false,
                ),
                useUnmergedTree = true,
            )
            .assertExists()
        composeRule.onNodeWithText(AppContentContract.EmptyTtsInputError).assertIsDisplayed()
    }

    @Test
    fun reducedMotionInjectionKeepsSpeakingMouthObservableInStageSemantics() {
        val uiState = SpeechDemoState(
            petState = PetState.Speaking,
            currentMouth = MouthShape.Open,
            inputSource = InputSource.Tts,
            speechAnimationState = SpeechAnimationState.Speaking,
        )

        composeRule.activity.setContent {
            RoomyLandscapeHost {
                DigitalDogApp(
                    layoutMode = AppLayoutMode.Landscape,
                    uiState = uiState,
                    motionPolicy = ReduceMotionPolicy.Reduced,
                )
            }
        }

        composeRule
            .onNodeWithContentDescription(
                expectedStageDescription(
                    uiState = uiState,
                    motionPolicy = ReduceMotionPolicy.Reduced,
                ),
                useUnmergedTree = true,
            )
            .assertExists()
        composeRule.onNodeWithTag(AppContentContract.TagPetFigure).assertExists()
        composeRule.onNodeWithTag(AppContentContract.TagDogMouth, useUnmergedTree = true).assertExists()
    }

    @Test
    fun autoAdvanceSpeechShowsDoneFeedbackThenReturnsIdle() {
        composeRule.mainClock.autoAdvance = false
        try {
            composeRule.activity.setContent {
                RoomyLandscapeHost {
                    DigitalDogApp(layoutMode = AppLayoutMode.Landscape)
                }
            }

            composeRule
                .onNodeWithTag(AppContentContract.TagTtsInputField)
                .performTextInput("完成反馈")
            composeRule
                .onNodeWithTag(AppContentContract.TagPrimaryTtsCta)
                .performClick()

            composeRule.onNodeWithText(AppContentContract.PrimaryCtaBusy).assertIsDisplayed()
            composeRule.onNodeWithText(AppContentContract.mouthStateText(AppContentContract.MouthStateClosed)).assertIsDisplayed()

            composeRule.mainClock.advanceTimeBy(SpeechAnimationState.Preparing.estimatedDurationMs.toLong() + 1L)
            composeRule.waitForIdle()

            composeRule.onNodeWithText(AppContentContract.mouthStateText(AppContentContract.MouthStateTalking)).assertIsDisplayed()

            composeRule.mainClock.advanceTimeBy(DefaultSpeechAnimationDurationMs.toLong() + 1L)
            composeRule.waitForIdle()

            val doneState = SpeechDemoState(
                petState = PetState.Done,
                currentMouth = MouthShape.Closed,
                inputSource = InputSource.Tts,
                speechAnimationState = SpeechAnimationState.Done,
            )
            val donePresentation = doneState.toPresentation()
            composeRule
                .onNodeWithContentDescription(
                    expectedStageDescription(
                        uiState = doneState,
                        motionPolicy = ReduceMotionPolicy.Normal,
                    ),
                    useUnmergedTree = true,
                )
                .assertExists()
            composeRule.onNodeWithText(AppContentContract.currentStateText(donePresentation.stateLabel)).assertIsDisplayed()
            composeRule.onNodeWithText(AppContentContract.inputSourceText(InputSource.Tts.label)).assertIsDisplayed()
            composeRule.onNodeWithText(AppContentContract.PrimaryCta).assertIsDisplayed()
            composeRule.onNodeWithTag(AppContentContract.TagPrimaryTtsCta).assertIsEnabled()

            composeRule.mainClock.advanceTimeBy(DefaultCompletionFeedbackDurationMs.toLong() + 1L)
            composeRule.waitForIdle()

            composeRule.onNodeWithText(AppContentContract.CurrentStateIdle).assertIsDisplayed()
            composeRule.onNodeWithText(AppContentContract.inputSourceText(InputSource.None.label)).assertIsDisplayed()
            composeRule.onNodeWithText(AppContentContract.mouthStateText(AppContentContract.MouthStateClosed)).assertIsDisplayed()
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }

    @Test
    fun doneFeedbackReturnsIdleForEquivalentNonSpeakingCue() {
        composeRule.mainClock.autoAdvance = false
        try {
            val doneWithBlinkCue = SpeechDemoState(
                petState = PetState.Done,
                currentMouth = MouthShape.Closed,
                inputSource = InputSource.Sample,
                speechAnimationState = SpeechAnimationState(
                    isSpeaking = false,
                    mouthOpen = false,
                    estimatedDurationMs = 0,
                    actionCue = DogActionCue.Blink,
                ),
            )

            composeRule.activity.setContent {
                RoomyLandscapeHost {
                    DigitalDogApp(
                        layoutMode = AppLayoutMode.Landscape,
                        uiState = doneWithBlinkCue,
                    )
                }
            }

            composeRule.mainClock.advanceTimeBy(1L)
            composeRule.waitForIdle()

            composeRule.onNodeWithText(AppContentContract.CurrentStateIdle).assertIsDisplayed()
            composeRule.onNodeWithText(AppContentContract.inputSourceText(InputSource.None.label)).assertIsDisplayed()
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }

    @Test
    fun injectedNegativePreparationDurationDoesNotCrashAutoAdvance() {
        composeRule.mainClock.autoAdvance = false
        try {
            val preparingWithNegativeDelay = SpeechDemoState(
                petState = PetState.Thinking,
                currentMouth = MouthShape.Closed,
                inputSource = InputSource.Tts,
                activeSpeechSession = SpeechSession(
                    id = "negative-delay-1",
                    source = InputSource.Tts,
                    text = "负数 duration",
                ),
                speechAnimationState = SpeechAnimationState(
                    isSpeaking = false,
                    mouthOpen = false,
                    estimatedDurationMs = -1,
                    actionCue = DogActionCue.EarPerk,
                ),
            )

            composeRule.activity.setContent {
                RoomyLandscapeHost {
                    DigitalDogApp(
                        layoutMode = AppLayoutMode.Landscape,
                        uiState = preparingWithNegativeDelay,
                    )
                }
            }

            composeRule.mainClock.advanceTimeBy(1L)
            composeRule.waitForIdle()

            composeRule.onNodeWithText(AppContentContract.mouthStateText(AppContentContract.MouthStateTalking)).assertIsDisplayed()
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }
}

private fun SemanticsNodeInteraction.assertDisplayedAfterScroll() {
    runCatching { performScrollTo() }
    assertIsDisplayed()
}

private fun assertBoundsDoNotOverlap(
    first: SemanticsNodeInteraction,
    second: SemanticsNodeInteraction,
    firstLabel: String,
    secondLabel: String,
) {
    val firstBounds = first.getUnclippedBoundsInRoot()
    val secondBounds = second.getUnclippedBoundsInRoot()

    assertFalse(
        "$firstLabel overlaps $secondLabel: $firstBounds vs $secondBounds",
        firstBounds.overlaps(secondBounds),
    )
}

private fun DpRect.overlaps(other: DpRect): Boolean =
    left < other.right &&
        right > other.left &&
        top < other.bottom &&
        bottom > other.top

@Composable
private fun RoomyLandscapeHost(content: @Composable () -> Unit) {
    Box(modifier = Modifier.size(width = 900.dp, height = 760.dp)) {
        content()
    }
}

private fun expectedStageDescription(
    uiState: SpeechDemoState,
    motionPolicy: ReduceMotionPolicy,
): String {
    val presentation = uiState.toPresentation()
    val motionProfile = motionPolicy.applyTo(
        DogMotionProfile.forState(
            petState = uiState.petState,
            actionCue = uiState.speechAnimationState.actionCue,
        ),
    )
    val isSpeakingMouthOpen = uiState.speechAnimationState.mouthOpen ||
        uiState.petState == PetState.Speaking
    val displayMouth = if (isSpeakingMouthOpen) MouthShape.Open else uiState.currentMouth

    return AppContentContract.stageDescription(
        stateLabel = presentation.stateLabel,
        mouthLabel = AppContentContract.mouthSemanticLabel(displayMouth),
        inputSourceLabel = uiState.inputSource.label,
        stateDescription = AppContentContract.stageMouthStateDescription(
            mouth = displayMouth,
            stateDescription = presentation.stateDescription,
            isSpeakingMouthOpen = isSpeakingMouthOpen,
        ),
        collarDescription = presentation.collar.description,
        motionDescription = motionProfile.summary,
        motionPolicyLabel = motionPolicy.label,
    )
}
