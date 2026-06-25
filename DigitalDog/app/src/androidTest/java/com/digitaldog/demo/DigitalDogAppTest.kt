package com.digitaldog.demo

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.unit.dp
import com.digitaldog.demo.app.AppLayoutMode
import com.digitaldog.demo.app.AppContentContract
import com.digitaldog.demo.app.DigitalDogApp
import com.digitaldog.demo.accessibility.AndroidAnimatorScaleProvider
import com.digitaldog.demo.accessibility.ReduceMotionPolicy
import com.digitaldog.demo.dogrenderer.DogMotionProfile
import com.digitaldog.demo.sharedmodel.InputSource
import com.digitaldog.demo.sharedmodel.MouthShape
import com.digitaldog.demo.sharedmodel.PetState
import com.digitaldog.demo.state.SpeechDemoState
import com.digitaldog.demo.state.toPresentation
import org.junit.Rule
import org.junit.Test

class DigitalDogAppTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainActivityShowsFirstScreenSkeleton() {
        composeRule.onNodeWithText(AppContentContract.Title).assertIsDisplayed()
        composeRule.onNodeWithTag(AppContentContract.TagStatusBar).assertIsDisplayed()
        composeRule.onNodeWithTag(AppContentContract.TagPetStage).assertIsDisplayed()
        composeRule.onNodeWithTag(AppContentContract.TagSpeechInput).assertIsDisplayed()
        composeRule.onNodeWithTag(AppContentContract.TagQuickActions).assertIsDisplayed()
        composeRule.onNodeWithText(AppContentContract.PrimaryCta).assertDisplayedAfterScroll()
        composeRule.onNodeWithText(AppContentContract.SampleAudio).assertDisplayedAfterScroll()
        composeRule.onNodeWithText(AppContentContract.UploadAudio).assertDisplayedAfterScroll()
        composeRule.onNodeWithText(AppContentContract.StartRecording).assertDisplayedAfterScroll()
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
    fun appSupportsLandscapeAndPortraitLayoutBranches() {
        composeRule.activity.setContent {
            RoomyLandscapeHost {
                DigitalDogApp(layoutMode = AppLayoutMode.Landscape)
            }
        }

        composeRule.onNodeWithTag(AppContentContract.TagDebugPanel).assertIsDisplayed()
        composeRule.onNodeWithText(AppContentContract.DebugTitle).assertIsDisplayed()
        composeRule.onNodeWithText(AppContentContract.CurrentMouthClosed).assertExists()

        composeRule.activity.setContent {
            DigitalDogApp(layoutMode = AppLayoutMode.Portrait)
        }

        composeRule.onNodeWithTag(AppContentContract.TagDebugSummary).assertExists()
        composeRule.onNodeWithText(AppContentContract.DebugSummaryTitle).assertExists()
        composeRule.onNodeWithText(AppContentContract.CurrentStateIdle).assertExists()
        composeRule.onNodeWithText(AppContentContract.QualityReady).assertExists()
    }

    @Test
    fun mockStateInjectionSyncsTopBarStageAndLandscapeDebugPanel() {
        val petStates = listOf(
            PetState.Idle,
            PetState.Listening,
            PetState.Thinking,
            PetState.Speaking,
            PetState.Done,
            PetState.Error,
        )

        petStates.forEach { petState ->
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
                    AppContentContract.debugPanelDescription(
                        mouthId = uiState.currentMouth.stableId,
                        stateLabel = presentation.stateLabel,
                        inputSourceLabel = uiState.inputSource.label,
                        qualityLabel = uiState.timelineQuality.label,
                        collarDescription = presentation.collar.description,
                    ),
                    useUnmergedTree = true,
                )
                .assertExists()
            composeRule.onNodeWithText(AppContentContract.currentStateText(presentation.stateLabel)).assertIsDisplayed()
            composeRule.onNodeWithText(AppContentContract.currentMouthText(uiState.currentMouth.stableId)).assertIsDisplayed()
            composeRule
                .onAllNodesWithText(AppContentContract.inputSourceText(uiState.inputSource.label))
                .assertCountEquals(2)
            composeRule.onNodeWithText(AppContentContract.qualityText(uiState.timelineQuality.label)).assertIsDisplayed()
        }
    }

    @Test
    fun portraitDebugSummaryReflectsInjectedState() {
        val uiState = SpeechDemoState(petState = PetState.Error)
        val presentation = uiState.toPresentation()

        composeRule.activity.setContent {
            DigitalDogApp(
                layoutMode = AppLayoutMode.Portrait,
                uiState = uiState,
            )
        }

        composeRule.onNodeWithTag(AppContentContract.TagDebugSummary).assertExists()
        composeRule
            .onNodeWithContentDescription(
                AppContentContract.debugSummaryDescription(
                    mouthId = uiState.currentMouth.stableId,
                    stateLabel = presentation.stateLabel,
                    inputSourceLabel = uiState.inputSource.label,
                    qualityLabel = uiState.timelineQuality.label,
                    collarDescription = presentation.collar.description,
                ),
                useUnmergedTree = true,
            )
            .assertExists()
        composeRule.onNodeWithText(AppContentContract.currentStateText(presentation.stateLabel)).assertExists()
        composeRule.onNodeWithText(AppContentContract.currentMouthText(uiState.currentMouth.stableId)).assertExists()
    }

    @Test
    fun manualMouthControlsDriveStageAndLandscapeDebugPanel() {
        composeRule.activity.setContent {
            RoomyLandscapeHost {
                DigitalDogApp(layoutMode = AppLayoutMode.Landscape)
            }
        }

        composeRule
            .onNodeWithTag(AppContentContract.TagManualMouthTest)
            .performScrollTo()
            .assertIsDisplayed()

        MouthShape.entries.forEach { mouth ->
            composeRule
                .onNodeWithText(AppContentContract.manualMouthButtonText(mouth))
                .performScrollTo()
                .performClick()

            composeRule
                .onNodeWithContentDescription(
                    expectedStageDescription(
                        uiState = SpeechDemoState(
                            currentMouth = mouth,
                            inputSource = InputSource.Manual,
                        ),
                        motionPolicy = ReduceMotionPolicy.Normal,
                    ),
                    useUnmergedTree = true,
                )
                .assertExists()
            composeRule
                .onNodeWithContentDescription(
                    AppContentContract.manualMouthTestDescription(
                        currentMouth = mouth,
                        inputSourceLabel = InputSource.Manual.label,
                    ),
                    useUnmergedTree = true,
                )
                .assertExists()
            composeRule.onNodeWithText(AppContentContract.currentMouthText(mouth.stableId)).assertIsDisplayed()
            composeRule
                .onAllNodesWithText(AppContentContract.inputSourceText(InputSource.Manual.label))
                .assertCountEquals(2)
        }

        composeRule.onNodeWithText(AppContentContract.ResetManualMouth).performClick()
        composeRule.onNodeWithText(AppContentContract.CurrentMouthClosed).assertIsDisplayed()
        composeRule
            .onAllNodesWithText(AppContentContract.inputSourceText(InputSource.None.label))
            .assertCountEquals(2)
    }

    @Test
    fun manualMouthControlsUpdatePortraitDebugSummary() {
        composeRule.activity.setContent {
            DigitalDogApp(layoutMode = AppLayoutMode.Portrait)
        }

        composeRule
            .onNodeWithText(AppContentContract.manualMouthButtonText(MouthShape.Wide))
            .performScrollTo()
            .performClick()

        composeRule.onNodeWithTag(AppContentContract.TagDebugSummary).assertExists()
        composeRule.onNodeWithText(AppContentContract.currentMouthText(MouthShape.Wide.stableId)).assertExists()
        composeRule
            .onAllNodesWithText(AppContentContract.inputSourceText(InputSource.Manual.label))
            .assertCountEquals(2)
    }

    @Test
    fun manualMouthControlsKeepMinimumTouchTarget() {
        composeRule.activity.setContent {
            Box(modifier = Modifier.size(width = 900.dp, height = 760.dp)) {
                DigitalDogApp()
            }
        }

        MouthShape.entries.forEach { mouth ->
            composeRule
                .onNode(hasText(AppContentContract.manualMouthButtonText(mouth)) and hasClickAction())
                .assertHeightIsAtLeast(48.dp)
        }
        composeRule
            .onNode(hasText(AppContentContract.ResetManualMouth) and hasClickAction())
            .assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun injectedNonClosedMouthRendersThroughStageAndDebugSemantics() {
        val uiState = SpeechDemoState(currentMouth = MouthShape.Round, inputSource = InputSource.Manual)

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
                expectedStageDescription(
                    uiState = uiState,
                    motionPolicy = ReduceMotionPolicy.Normal,
                ),
                useUnmergedTree = true,
            )
            .assertExists()
        composeRule.onNodeWithText(AppContentContract.currentMouthText(MouthShape.Round.stableId)).assertIsDisplayed()
        composeRule
            .onAllNodesWithText(AppContentContract.inputSourceText(InputSource.Manual.label))
            .assertCountEquals(2)
    }

    @Test
    fun automaticLayoutUsesRoomyLandscapeOnlyWhenWidthAndHeightAllowIt() {
        composeRule.activity.setContent {
            Box(modifier = Modifier.size(width = 900.dp, height = 760.dp)) {
                DigitalDogApp()
            }
        }

        composeRule.onNodeWithTag(AppContentContract.TagDebugPanel).assertExists()

        composeRule.activity.setContent {
            Box(modifier = Modifier.size(width = 700.dp, height = 420.dp)) {
                DigitalDogApp()
            }
        }

        composeRule.onNodeWithTag(AppContentContract.TagDebugPanel).assertDoesNotExist()
        composeRule.onNodeWithTag(AppContentContract.TagDebugSummary).assertExists()
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
        composeRule
            .onNode(hasText(AppContentContract.UploadAudio) and hasClickAction())
            .assertHeightIsAtLeast(48.dp)
        composeRule
            .onNode(hasText(AppContentContract.StartRecording) and hasClickAction())
            .assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun ttsNonEmptySubmitSyncsTopBarStageAndDebugPanel() {
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

        val expectedState = SpeechDemoState(
            petState = PetState.Thinking,
            inputSource = InputSource.Tts,
        )
        val presentation = expectedState.toPresentation()

        composeRule
            .onNodeWithContentDescription(
                AppContentContract.statusBarDescription(
                    stateLabel = presentation.stateLabel,
                    inputSourceLabel = InputSource.Tts.label,
                    stateDescription = presentation.stateDescription,
                    collarDescription = presentation.collar.description,
                ),
                useUnmergedTree = true,
            )
            .assertExists()
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
                AppContentContract.debugPanelDescription(
                    mouthId = expectedState.currentMouth.stableId,
                    stateLabel = presentation.stateLabel,
                    inputSourceLabel = expectedState.inputSource.label,
                    qualityLabel = expectedState.timelineQuality.label,
                    collarDescription = presentation.collar.description,
                ),
                useUnmergedTree = true,
            )
            .assertExists()
        composeRule.onNodeWithText(AppContentContract.currentMouthText(MouthShape.Closed.stableId)).assertIsDisplayed()
        composeRule
            .onAllNodesWithText(AppContentContract.inputSourceText(InputSource.Tts.label))
            .assertCountEquals(2)
        composeRule.onNodeWithText(AppContentContract.PrimaryCtaBusy).assertIsDisplayed()
        composeRule.onNodeWithTag(AppContentContract.TagPrimaryTtsCta).assertIsNotEnabled()
    }

    @Test
    fun ttsEmptySubmitShowsRecoverableErrorAndAllowsRetry() {
        composeRule.activity.setContent {
            DigitalDogApp(layoutMode = AppLayoutMode.Portrait)
        }

        composeRule
            .onNodeWithTag(AppContentContract.TagPrimaryTtsCta)
            .performScrollTo()
            .performClick()

        val errorState = SpeechDemoState(
            petState = PetState.Error,
            inputSource = InputSource.Tts,
        )

        composeRule.onNodeWithText(AppContentContract.EmptyTtsInputError).assertIsDisplayed()
        composeRule
            .onNodeWithContentDescription(
                expectedStageDescription(
                    uiState = errorState,
                    motionPolicy = ReduceMotionPolicy.Normal,
                ),
                useUnmergedTree = true,
            )
            .assertExists()
        val errorPresentation = errorState.toPresentation()
        composeRule
            .onNodeWithContentDescription(
                AppContentContract.debugSummaryDescription(
                    mouthId = errorState.currentMouth.stableId,
                    stateLabel = errorPresentation.stateLabel,
                    inputSourceLabel = errorState.inputSource.label,
                    qualityLabel = errorState.timelineQuality.label,
                    collarDescription = errorPresentation.collar.description,
                ),
                useUnmergedTree = true,
            )
            .assertExists()

        composeRule
            .onNodeWithTag(AppContentContract.TagTtsInputField)
            .performTextInput("重试一句话")
        composeRule.onNodeWithText(AppContentContract.EmptyTtsInputError).assertDoesNotExist()
        composeRule
            .onNodeWithTag(AppContentContract.TagPrimaryTtsCta)
            .performClick()

        composeRule.onNodeWithText(AppContentContract.PrimaryCtaBusy).assertIsDisplayed()
        val retryState = SpeechDemoState(
            petState = PetState.Thinking,
            inputSource = InputSource.Tts,
        )
        val retryPresentation = retryState.toPresentation()
        composeRule
            .onNodeWithContentDescription(
                AppContentContract.debugSummaryDescription(
                    mouthId = retryState.currentMouth.stableId,
                    stateLabel = retryPresentation.stateLabel,
                    inputSourceLabel = retryState.inputSource.label,
                    qualityLabel = retryState.timelineQuality.label,
                    collarDescription = retryPresentation.collar.description,
                ),
                useUnmergedTree = true,
            )
            .assertExists()
        composeRule
            .onAllNodesWithText(AppContentContract.inputSourceText(InputSource.Tts.label))
            .assertCountEquals(2)
    }

    @Test
    fun busyTtsSessionDisablesPrimaryCtaToPreventSecondSubmit() {
        composeRule.activity.setContent {
            DigitalDogApp(
                layoutMode = AppLayoutMode.Portrait,
                uiState = SpeechDemoState(ttsInputText = "已有一句"),
            )
        }

        composeRule
            .onNodeWithTag(AppContentContract.TagPrimaryTtsCta)
            .performScrollTo()
            .performClick()

        composeRule
            .onNodeWithTag(AppContentContract.TagTtsInputField)
            .performTextClearance()
        composeRule
            .onNodeWithTag(AppContentContract.TagTtsInputField)
            .performTextInput("第二句")

        composeRule.onNodeWithTag(AppContentContract.TagPrimaryTtsCta).assertIsNotEnabled()
        composeRule.onNodeWithText(AppContentContract.PrimaryCtaBusy).assertIsDisplayed()
        composeRule.onNodeWithText(AppContentContract.EmptyTtsInputError).assertDoesNotExist()
    }

    @Test
    fun stageSemanticsIncludeMotionSummaryAndPolicyForKeyStates() {
        listOf(
            PetState.Idle,
            PetState.Speaking,
            PetState.Done,
            PetState.Error,
        ).forEach { petState ->
            val uiState = SpeechDemoState(petState = petState)

            composeRule.activity.setContent {
                RoomyLandscapeHost {
                    DigitalDogApp(
                        layoutMode = AppLayoutMode.Landscape,
                        uiState = uiState,
                        motionPolicy = ReduceMotionPolicy.Normal,
                    )
                }
            }

            composeRule
                .onNodeWithContentDescription(
                    expectedStageDescription(
                        uiState = uiState,
                        motionPolicy = ReduceMotionPolicy.Normal,
                    ),
                    useUnmergedTree = true,
                )
                .assertExists()
            composeRule.onNodeWithTag(AppContentContract.TagPetFigure).assertExists()
            composeRule.onNodeWithTag(AppContentContract.TagDogMouth, useUnmergedTree = true).assertExists()
        }
    }

    @Test
    fun reducedMotionInjectionKeepsCurrentMouthObservableInStageSemantics() {
        val uiState = SpeechDemoState(
            petState = PetState.Speaking,
            currentMouth = MouthShape.Wide,
            inputSource = InputSource.Manual,
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
        composeRule.onNodeWithText(AppContentContract.currentMouthText(MouthShape.Wide.stableId)).assertIsDisplayed()
        composeRule.onNodeWithTag(AppContentContract.TagPetFigure).assertExists()
        composeRule.onNodeWithTag(AppContentContract.TagDogMouth, useUnmergedTree = true).assertExists()
    }

    @Test
    fun staticMotionPolicyProvidesStableComposeTestPath() {
        val uiState = SpeechDemoState(petState = PetState.Done)

        composeRule.activity.setContent {
            RoomyLandscapeHost {
                DigitalDogApp(
                    layoutMode = AppLayoutMode.Landscape,
                    uiState = uiState,
                    motionPolicy = ReduceMotionPolicy.Static,
                )
            }
        }

        composeRule
            .onNodeWithContentDescription(
                expectedStageDescription(
                    uiState = uiState,
                    motionPolicy = ReduceMotionPolicy.Static,
                ),
                useUnmergedTree = true,
            )
            .assertExists()
        composeRule.onNodeWithTag(AppContentContract.TagPetFigure).assertExists()
        composeRule.onNodeWithTag(AppContentContract.TagDogMouth, useUnmergedTree = true).assertExists()
    }
}

private fun SemanticsNodeInteraction.assertDisplayedAfterScroll() {
    runCatching { performScrollTo() }
    assertIsDisplayed()
}

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
    val motionProfile = motionPolicy.applyTo(DogMotionProfile.forState(uiState.petState))

    return AppContentContract.stageDescription(
        stateLabel = presentation.stateLabel,
        mouthLabel = AppContentContract.mouthSemanticLabel(uiState.currentMouth),
        inputSourceLabel = uiState.inputSource.label,
        stateDescription = AppContentContract.stageMouthStateDescription(
            mouth = uiState.currentMouth,
            stateDescription = presentation.stateDescription,
        ),
        collarDescription = presentation.collar.description,
        motionDescription = motionProfile.summary,
        motionPolicyLabel = motionPolicy.label,
    )
}
