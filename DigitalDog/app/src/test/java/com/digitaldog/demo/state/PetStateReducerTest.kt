package com.digitaldog.demo.state

import com.digitaldog.demo.app.AppContentContract
import com.digitaldog.demo.designsystem.DogColors
import com.digitaldog.demo.sharedmodel.InputSource
import com.digitaldog.demo.sharedmodel.MouthShape
import com.digitaldog.demo.sharedmodel.PetState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PetStateReducerTest {
    @Test
    fun defaultSpeechDemoStateIsIdleClosedAndReadyForDemoInput() {
        val state = SpeechDemoState()

        assertEquals(PetState.Idle, state.petState)
        assertEquals(MouthShape.Closed, state.currentMouth)
        assertEquals(InputSource.None, state.inputSource)
    }

    @Test
    fun petStateExposesStableLowercaseIdsOnlyForCurrentMvpStates() {
        assertEquals(
            listOf("idle", "listening", "thinking", "speaking", "done", "error"),
            PetState.entries.map { it.stableId },
        )
    }

    @Test
    fun mouthShapeExposesOnlyClosedAndOpenForCurrentMvp() {
        assertEquals(
            listOf("closed", "open"),
            MouthShape.entries.map { it.stableId },
        )

        MouthShape.entries.forEach { mouth ->
            assertTrue(mouth.label.isNotBlank())
            assertTrue(mouth.description.isNotBlank())
            assertTrue(mouth.accessibleLabel.contains(mouth.stableId))
        }
    }

    @Test
    fun reducerMovesThroughAllMockStates() {
        val initialState = SpeechDemoState()

        assertEquals(
            PetState.Listening,
            PetStateReducer.reduce(initialState, PetStateEvent.EnterListening).petState,
        )
        assertEquals(
            PetState.Thinking,
            PetStateReducer.reduce(initialState, PetStateEvent.EnterThinking).petState,
        )
        assertEquals(
            PetState.Speaking,
            PetStateReducer.reduce(initialState, PetStateEvent.EnterSpeaking).petState,
        )
        assertEquals(
            PetState.Done,
            PetStateReducer.reduce(initialState, PetStateEvent.EnterDone).petState,
        )
        assertEquals(
            PetState.Error,
            PetStateReducer.reduce(initialState, PetStateEvent.EnterError).petState,
        )
    }

    @Test
    fun reducerOpensMouthOnlyForSpeakingState() {
        val speaking = PetStateReducer.reduce(SpeechDemoState(), PetStateEvent.EnterSpeaking)

        assertEquals(MouthShape.Open, speaking.currentMouth)
        assertTrue(speaking.speechAnimationState.mouthOpen)
        assertEquals(DogActionCue.BodyBounce, speaking.speechAnimationState.actionCue)

        listOf(
            PetStateEvent.EnterListening,
            PetStateEvent.EnterThinking,
            PetStateEvent.EnterDone,
            PetStateEvent.EnterError,
            PetStateEvent.ReturnToIdle,
        ).forEach { event ->
            val state = PetStateReducer.reduce(SpeechDemoState(), event)

            assertEquals(MouthShape.Closed, state.currentMouth)
        }
    }

    @Test
    fun reducerAssignsPlayfulActionCuesForPreparingDoneAndError() {
        val thinking = PetStateReducer.reduce(SpeechDemoState(), PetStateEvent.EnterThinking)
        val done = PetStateReducer.reduce(SpeechDemoState(), PetStateEvent.EnterDone)
        val error = PetStateReducer.reduce(SpeechDemoState(), PetStateEvent.EnterError)

        assertEquals(DogActionCue.EarPerk, thinking.speechAnimationState.actionCue)
        assertFalse(thinking.speechAnimationState.mouthOpen)

        assertEquals(DogActionCue.TailWag, done.speechAnimationState.actionCue)
        assertFalse(done.speechAnimationState.mouthOpen)

        assertEquals(DogActionCue.HeadTilt, error.speechAnimationState.actionCue)
        assertFalse(error.speechAnimationState.mouthOpen)
    }

    @Test
    fun doneAndErrorReturnToIdleOnlyAfterExplicitMockEvent() {
        val doneState = PetStateReducer.reduce(SpeechDemoState(), PetStateEvent.EnterDone)
        val errorState = PetStateReducer.reduce(SpeechDemoState(), PetStateEvent.EnterError)

        assertEquals(PetState.Done, doneState.petState)
        assertEquals(PetState.Error, errorState.petState)
        assertEquals(
            SpeechDemoState(),
            PetStateReducer.reduce(doneState, PetStateEvent.ReturnToIdle),
        )
        assertEquals(
            SpeechDemoState(),
            PetStateReducer.reduce(errorState, PetStateEvent.ReturnToIdle),
        )
    }

    @Test
    fun presentationMapsEveryStateToChineseLabelAndCollarRole() {
        val presentations = PetState.entries.associateWith { it.toPresentation() }

        assertEquals(AppContentContract.StatusIdle, presentations.getValue(PetState.Idle).stateLabel)
        assertEquals(AppContentContract.StatusListening, presentations.getValue(PetState.Listening).stateLabel)
        assertEquals(AppContentContract.StatusThinking, presentations.getValue(PetState.Thinking).stateLabel)
        assertEquals(AppContentContract.StatusSpeaking, presentations.getValue(PetState.Speaking).stateLabel)
        assertEquals(AppContentContract.StatusDone, presentations.getValue(PetState.Done).stateLabel)
        assertEquals(AppContentContract.StatusError, presentations.getValue(PetState.Error).stateLabel)

        assertEquals("低亮", presentations.getValue(PetState.Idle).collar.role)
        assertEquals("科技蓝", presentations.getValue(PetState.Listening).collar.role)
        assertEquals("蓝色呼吸语义", presentations.getValue(PetState.Thinking).collar.role)
        assertEquals("珊瑚", presentations.getValue(PetState.Speaking).collar.role)
        assertEquals("成功绿", presentations.getValue(PetState.Done).collar.role)
        assertEquals("提示黄", presentations.getValue(PetState.Error).collar.role)

        assertEquals(DogColors.TextSecondary, presentations.getValue(PetState.Idle).collar.color)
        assertEquals(DogColors.TechBlue, presentations.getValue(PetState.Listening).collar.color)
        assertEquals(DogColors.TechBlue, presentations.getValue(PetState.Thinking).collar.color)
        assertEquals(DogColors.Coral, presentations.getValue(PetState.Speaking).collar.color)
        assertEquals(DogColors.SuccessGreen, presentations.getValue(PetState.Done).collar.color)
        assertEquals(DogColors.WarningYellow, presentations.getValue(PetState.Error).collar.color)

        assertEquals("闭口思考", presentations.getValue(PetState.Thinking).stateDescription)
        assertEquals("项圈蓝色呼吸语义，正在思考", presentations.getValue(PetState.Thinking).collar.description)
    }
}
