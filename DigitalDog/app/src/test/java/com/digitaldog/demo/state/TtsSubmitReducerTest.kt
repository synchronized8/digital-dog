package com.digitaldog.demo.state

import com.digitaldog.demo.app.AppContentContract
import com.digitaldog.demo.sharedmodel.InputSource
import com.digitaldog.demo.sharedmodel.MouthShape
import com.digitaldog.demo.sharedmodel.PetState
import com.digitaldog.demo.sharedmodel.SpeechSessionStatus
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class TtsSubmitReducerTest {
    @Test
    fun defaultStateHasNoSpeechSessionOrInputError() {
        val state = SpeechDemoState()

        assertEquals("", state.ttsInputText)
        assertNull(state.inputError)
        assertNull(state.activeSpeechSession)
        assertFalse(state.isSpeechSessionBusy)
        assertEquals(MouthShape.Closed, state.currentMouth)
        assertEquals(SpeechAnimationState.Idle, state.speechAnimationState)
    }

    @Test
    fun textChangeUpdatesInputAndClearsRecoverableError() {
        val errorState = TtsSubmitReducer.submitText(
            SpeechDemoState(ttsInputText = "   "),
        )

        val edited = TtsSubmitReducer.updateText(errorState, "你好数字狗")

        assertEquals("你好数字狗", edited.ttsInputText)
        assertNull(edited.inputError)
        assertEquals(PetState.Idle, edited.petState)
        assertEquals(MouthShape.Closed, edited.currentMouth)
        assertEquals(InputSource.None, edited.inputSource)
        assertNull(edited.activeSpeechSession)
        assertEquals(SpeechAnimationState.Idle, edited.speechAnimationState)
    }

    @Test
    fun nonBlankSubmitCreatesTextSpeechSessionAndMovesToThinkingClosedMouth() {
        val state = TtsSubmitReducer.submitText(
            SpeechDemoState(ttsInputText = "  你好，数字狗  "),
        )

        assertEquals("你好，数字狗", state.ttsInputText)
        assertEquals(PetState.Thinking, state.petState)
        assertEquals(MouthShape.Closed, state.currentMouth)
        assertEquals(InputSource.Tts, state.inputSource)
        assertNull(state.inputError)
        assertTrue(state.isSpeechSessionBusy)
        assertEquals("text-1", state.activeSpeechSession?.id)
        assertEquals("你好，数字狗", state.activeSpeechSession?.text)
        assertEquals(InputSource.Tts, state.activeSpeechSession?.source)
        assertEquals(SpeechSessionStatus.Pending, state.activeSpeechSession?.status)
        assertFalse(state.speechAnimationState.isSpeaking)
        assertFalse(state.speechAnimationState.mouthOpen)
        assertEquals(DogActionCue.EarPerk, state.speechAnimationState.actionCue)
    }

    @Test
    fun startSpeakingUsesSingleOpenMouthState() {
        val prepared = TtsSubmitReducer.submitText(
            SpeechDemoState(ttsInputText = "张口讲话"),
        )

        val speaking = TtsSubmitReducer.startSpeaking(prepared)

        assertSame(prepared.activeSpeechSession, speaking.activeSpeechSession)
        assertEquals(PetState.Speaking, speaking.petState)
        assertEquals(MouthShape.Open, speaking.currentMouth)
        assertEquals(InputSource.Tts, speaking.inputSource)
        assertTrue(speaking.speechAnimationState.isSpeaking)
        assertTrue(speaking.speechAnimationState.mouthOpen)
        assertEquals(DogActionCue.BodyBounce, speaking.speechAnimationState.actionCue)
    }

    @Test
    fun textSpeechFlowCompletesFullLifecycleWithClosedMouthOutsideSpeaking() {
        val prepared = TtsSubmitReducer.submitText(
            SpeechDemoState(ttsInputText = "完整流程"),
        )
        val speaking = TtsSubmitReducer.startSpeaking(prepared)
        val done = TtsSubmitReducer.completeSpeaking(speaking)
        val idle = TtsSubmitReducer.returnToIdleAfterCompletion(done)

        assertEquals("text-1", prepared.activeSpeechSession?.id)
        assertEquals(PetState.Thinking, prepared.petState)
        assertEquals(MouthShape.Closed, prepared.currentMouth)
        assertFalse(prepared.speechAnimationState.mouthOpen)

        assertSame(prepared.activeSpeechSession, speaking.activeSpeechSession)
        assertEquals(PetState.Speaking, speaking.petState)
        assertEquals(MouthShape.Open, speaking.currentMouth)
        assertTrue(speaking.speechAnimationState.mouthOpen)

        assertEquals(PetState.Done, done.petState)
        assertEquals(MouthShape.Closed, done.currentMouth)
        assertNull(done.activeSpeechSession)
        assertFalse(done.speechAnimationState.mouthOpen)

        assertEquals(PetState.Idle, idle.petState)
        assertEquals(MouthShape.Closed, idle.currentMouth)
        assertEquals(InputSource.None, idle.inputSource)
        assertNull(idle.activeSpeechSession)
    }

    @Test
    fun sampleSpeechFlowUsesSameMouthRulesAndKeepsTextInputUntouched() {
        val prepared = TtsSubmitReducer.playSample(
            SpeechDemoState(ttsInputText = "保留输入"),
        )
        val speaking = TtsSubmitReducer.startSpeaking(prepared)
        val done = TtsSubmitReducer.completeSpeaking(speaking)
        val idle = TtsSubmitReducer.returnToIdleAfterCompletion(done)

        assertEquals("保留输入", prepared.ttsInputText)
        assertEquals(InputSource.Sample, prepared.inputSource)
        assertEquals(PetState.Thinking, prepared.petState)
        assertEquals(MouthShape.Closed, prepared.currentMouth)
        assertEquals(SpeechAnimationState.Preparing, prepared.speechAnimationState)

        assertSame(prepared.activeSpeechSession, speaking.activeSpeechSession)
        assertEquals(InputSource.Sample, speaking.inputSource)
        assertEquals(PetState.Speaking, speaking.petState)
        assertEquals(MouthShape.Open, speaking.currentMouth)
        assertEquals(SpeechAnimationState.Speaking, speaking.speechAnimationState)

        assertEquals(InputSource.Sample, done.inputSource)
        assertEquals(PetState.Done, done.petState)
        assertEquals(MouthShape.Closed, done.currentMouth)

        assertEquals(PetState.Idle, idle.petState)
        assertEquals(MouthShape.Closed, idle.currentMouth)
        assertEquals(InputSource.None, idle.inputSource)
    }

    @Test
    fun startSpeakingIgnoresStaleSessionOutsideThinkingState() {
        val prepared = TtsSubmitReducer.submitText(
            SpeechDemoState(ttsInputText = "陈旧会话"),
        )
        val staleIdle = prepared.copy(
            petState = PetState.Idle,
            speechAnimationState = SpeechAnimationState.Idle,
        )

        val unchanged = TtsSubmitReducer.startSpeaking(staleIdle)

        assertSame(staleIdle.activeSpeechSession, unchanged.activeSpeechSession)
        assertEquals(PetState.Idle, unchanged.petState)
        assertEquals(MouthShape.Closed, unchanged.currentMouth)
        assertEquals(SpeechAnimationState.Idle, unchanged.speechAnimationState)
    }

    @Test
    fun completeSpeakingShowsDoneFeedbackClosedMouthAndClearsActiveSession() {
        val speaking = TtsSubmitReducer.startSpeaking(
            TtsSubmitReducer.submitText(SpeechDemoState(ttsInputText = "结束后闭口")),
        )

        val completed = TtsSubmitReducer.completeSpeaking(speaking)

        assertEquals(PetState.Done, completed.petState)
        assertEquals(MouthShape.Closed, completed.currentMouth)
        assertEquals(InputSource.Tts, completed.inputSource)
        assertNull(completed.activeSpeechSession)
        assertFalse(completed.isSpeechSessionBusy)
        assertEquals(SpeechAnimationState.Done, completed.speechAnimationState)
        assertEquals(DogActionCue.TailWag, completed.speechAnimationState.actionCue)
        assertFalse(completed.speechAnimationState.mouthOpen)
    }

    @Test
    fun completeSpeakingIgnoresNonSpeakingState() {
        val prepared = TtsSubmitReducer.submitText(
            SpeechDemoState(ttsInputText = "还没讲话"),
        )

        val unchanged = TtsSubmitReducer.completeSpeaking(prepared)

        assertSame(prepared.activeSpeechSession, unchanged.activeSpeechSession)
        assertEquals(PetState.Thinking, unchanged.petState)
        assertEquals(MouthShape.Closed, unchanged.currentMouth)
        assertEquals(SpeechAnimationState.Preparing, unchanged.speechAnimationState)
    }

    @Test
    fun returnToIdleAfterCompletionClearsDoneFeedbackAndInputSource() {
        val speaking = TtsSubmitReducer.startSpeaking(
            TtsSubmitReducer.submitText(SpeechDemoState(ttsInputText = "完成后回待机")),
        )
        val completed = TtsSubmitReducer.completeSpeaking(speaking)

        val idle = TtsSubmitReducer.returnToIdleAfterCompletion(completed)

        assertEquals(PetState.Idle, idle.petState)
        assertEquals(MouthShape.Closed, idle.currentMouth)
        assertEquals(InputSource.None, idle.inputSource)
        assertNull(idle.activeSpeechSession)
        assertEquals(SpeechAnimationState.Idle, idle.speechAnimationState)
    }

    @Test
    fun returnToIdleAfterCompletionIgnoresNonDoneState() {
        val speaking = TtsSubmitReducer.startSpeaking(
            TtsSubmitReducer.submitText(SpeechDemoState(ttsInputText = "仍在说话")),
        )

        val unchanged = TtsSubmitReducer.returnToIdleAfterCompletion(speaking)

        assertSame(speaking.activeSpeechSession, unchanged.activeSpeechSession)
        assertEquals(PetState.Speaking, unchanged.petState)
        assertEquals(MouthShape.Open, unchanged.currentMouth)
        assertTrue(unchanged.speechAnimationState.mouthOpen)
    }

    @Test
    fun returnToIdleAfterCompletionIgnoresDoneStateWithActiveSession() {
        val prepared = TtsSubmitReducer.submitText(
            SpeechDemoState(ttsInputText = "异常完成态"),
        )
        val doneWithActiveSession = prepared.copy(
            petState = PetState.Done,
            currentMouth = MouthShape.Closed,
            speechAnimationState = SpeechAnimationState.Done,
        )

        val unchanged = TtsSubmitReducer.returnToIdleAfterCompletion(doneWithActiveSession)

        assertSame(doneWithActiveSession.activeSpeechSession, unchanged.activeSpeechSession)
        assertEquals(PetState.Done, unchanged.petState)
        assertEquals(InputSource.Tts, unchanged.inputSource)
        assertEquals(SpeechAnimationState.Done, unchanged.speechAnimationState)
    }

    @Test
    fun nonSpeakingStatesDeriveClosedMouthAnimationState() {
        listOf(
            PetState.Idle,
            PetState.Thinking,
            PetState.Done,
            PetState.Error,
        ).forEach { petState ->
            val state = SpeechDemoState(petState = petState)

            assertEquals(MouthShape.Closed, state.currentMouth)
            assertFalse(state.speechAnimationState.mouthOpen)
        }
    }

    @Test
    fun submitNormalizesTextForSimpleSpeechSession() {
        val state = TtsSubmitReducer.submitText(
            SpeechDemoState(ttsInputText = "  my   狗\n\nOK  "),
        )

        assertEquals("my 狗 OK", state.activeSpeechSession?.text)
        assertEquals("my 狗 OK", state.ttsInputText)
    }

    @Test
    fun sampleCreatesSimpleSpeechSessionWithoutTextInput() {
        val state = TtsSubmitReducer.playSample(SpeechDemoState())

        assertEquals(PetState.Thinking, state.petState)
        assertEquals(MouthShape.Closed, state.currentMouth)
        assertEquals(InputSource.Sample, state.inputSource)
        assertEquals("", state.ttsInputText)
        assertTrue(state.isSpeechSessionBusy)
        assertNotNull(state.activeSpeechSession)
        assertEquals("sample-1", state.activeSpeechSession?.id)
        assertEquals(InputSource.Sample, state.activeSpeechSession?.source)
        assertEquals(SpeechAnimationState.Preparing, state.speechAnimationState)
    }

    @Test
    fun blankSubmitCreatesRecoverableErrorWithoutSession() {
        val state = TtsSubmitReducer.submitText(
            SpeechDemoState(ttsInputText = " \n\t "),
        )

        assertEquals(PetState.Error, state.petState)
        assertEquals(MouthShape.Closed, state.currentMouth)
        assertEquals(InputSource.Tts, state.inputSource)
        assertEquals(AppContentContract.EmptyTtsInputError, state.inputError)
        assertNull(state.activeSpeechSession)
        assertFalse(state.isSpeechSessionBusy)
    }

    @Test
    fun formatOnlySubmitCreatesRecoverableErrorWithoutSession() {
        val state = TtsSubmitReducer.submitText(
            SpeechDemoState(ttsInputText = "\u200B\u200C\uFEFF"),
        )

        assertEquals(PetState.Error, state.petState)
        assertEquals(MouthShape.Closed, state.currentMouth)
        assertEquals(InputSource.Tts, state.inputSource)
        assertEquals(AppContentContract.EmptyTtsInputError, state.inputError)
        assertNull(state.activeSpeechSession)
        assertFalse(state.isSpeechSessionBusy)
    }

    @Test
    fun retryAfterEmptyErrorCanCreateSessionFromEditedText() {
        val errorState = TtsSubmitReducer.submitText(
            SpeechDemoState(ttsInputText = ""),
        )
        val edited = TtsSubmitReducer.updateText(errorState, "重新说话")
        val retried = TtsSubmitReducer.submitText(edited)

        assertEquals(PetState.Thinking, retried.petState)
        assertEquals(MouthShape.Closed, retried.currentMouth)
        assertEquals(InputSource.Tts, retried.inputSource)
        assertNull(retried.inputError)
        assertEquals("重新说话", retried.activeSpeechSession?.text)
    }

    @Test
    fun busySubmitKeepsExistingSessionAndDoesNotCreateSecondOne() {
        val first = TtsSubmitReducer.submitText(
            SpeechDemoState(ttsInputText = "第一句"),
        )
        val second = TtsSubmitReducer.submitText(
            TtsSubmitReducer.updateText(first, "第二句"),
        )

        assertSame(first.activeSpeechSession, second.activeSpeechSession)
        assertEquals("text-1", second.activeSpeechSession?.id)
        assertEquals("第一句", second.activeSpeechSession?.text)
        assertEquals("第二句", second.ttsInputText)
        assertTrue(second.isSpeechSessionBusy)
        assertEquals(PetState.Thinking, second.petState)
        assertEquals(MouthShape.Closed, second.currentMouth)
    }

    @Test
    fun busySubmitKeepsActiveSpeakingMouthOpen() {
        val speaking = TtsSubmitReducer.startSpeaking(
            TtsSubmitReducer.submitText(SpeechDemoState(ttsInputText = "第一句")),
        )

        val second = TtsSubmitReducer.submitText(
            TtsSubmitReducer.updateText(speaking, "第二句"),
        )

        assertSame(speaking.activeSpeechSession, second.activeSpeechSession)
        assertEquals(PetState.Speaking, second.petState)
        assertEquals(MouthShape.Open, second.currentMouth)
        assertTrue(second.speechAnimationState.mouthOpen)
        assertEquals("第二句", second.ttsInputText)
    }

    @Test
    fun busyTextSessionIgnoresSampleEntryAndKeepsOriginalSession() {
        val textSession = TtsSubmitReducer.submitText(
            SpeechDemoState(ttsInputText = "文本忙碌"),
        )

        val afterSampleClick = TtsSubmitReducer.playSample(textSession)

        assertSame(textSession.activeSpeechSession, afterSampleClick.activeSpeechSession)
        assertEquals("text-1", afterSampleClick.activeSpeechSession?.id)
        assertEquals(InputSource.Tts, afterSampleClick.inputSource)
        assertEquals("文本忙碌", afterSampleClick.ttsInputText)
        assertEquals(PetState.Thinking, afterSampleClick.petState)
        assertEquals(MouthShape.Closed, afterSampleClick.currentMouth)
    }

    @Test
    fun busySampleSessionIgnoresTextSubmitAndKeepsOriginalSession() {
        val sampleSession = TtsSubmitReducer.playSample(SpeechDemoState())
        val edited = TtsSubmitReducer.updateText(sampleSession, "用户后输入")

        val afterTextSubmit = TtsSubmitReducer.submitText(edited)

        assertSame(sampleSession.activeSpeechSession, afterTextSubmit.activeSpeechSession)
        assertEquals("sample-1", afterTextSubmit.activeSpeechSession?.id)
        assertEquals(InputSource.Sample, afterTextSubmit.inputSource)
        assertEquals("用户后输入", afterTextSubmit.ttsInputText)
        assertEquals(PetState.Thinking, afterTextSubmit.petState)
        assertEquals(MouthShape.Closed, afterTextSubmit.currentMouth)
    }

    @Test
    fun maxSessionIndexWrapsToPositiveIndexForNextSpeechSession() {
        val maxIndexSession = TtsSubmitReducer.submitText(
            SpeechDemoState(
                ttsInputText = "极值会话",
                nextSpeechSessionIndex = Int.MAX_VALUE.toLong(),
            ),
        )

        assertEquals("text-${Int.MAX_VALUE}", maxIndexSession.activeSpeechSession?.id)
        assertTrue(maxIndexSession.nextSpeechSessionIndex > 0)

        val idleAfterMax = TtsSubmitReducer.returnToIdleAfterCompletion(
            TtsSubmitReducer.completeSpeaking(
                TtsSubmitReducer.startSpeaking(maxIndexSession),
            ),
        )
        val nextSession = TtsSubmitReducer.submitText(
            idleAfterMax.copy(ttsInputText = "下一轮"),
        )

        assertEquals("text-${Int.MAX_VALUE.toLong() + 1L}", nextSession.activeSpeechSession?.id)
        assertTrue(nextSession.nextSpeechSessionIndex > 0)
    }
}
