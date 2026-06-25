package com.digitaldog.demo.state

import com.digitaldog.demo.app.AppContentContract
import com.digitaldog.demo.sharedmodel.InputSource
import com.digitaldog.demo.sharedmodel.MouthShape
import com.digitaldog.demo.sharedmodel.PetState
import com.digitaldog.demo.sharedmodel.SpeechSessionStatus
import com.digitaldog.demo.sharedmodel.TimelineQuality
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class TtsSubmitReducerTest {
    @Test
    fun defaultStateHasNoTtsSessionOrInputError() {
        val state = SpeechDemoState()

        assertEquals("", state.ttsInputText)
        assertNull(state.inputError)
        assertNull(state.activeSpeechSession)
        assertFalse(state.isSpeechSessionBusy)
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
    }

    @Test
    fun nonBlankSubmitCreatesTtsSessionAndMovesToThinkingClosedMouth() {
        val state = TtsSubmitReducer.submitText(
            SpeechDemoState(ttsInputText = "  你好，数字狗  "),
        )

        assertEquals("你好，数字狗", state.ttsInputText)
        assertEquals(PetState.Thinking, state.petState)
        assertEquals(MouthShape.Closed, state.currentMouth)
        assertEquals(InputSource.Tts, state.inputSource)
        assertNull(state.inputError)
        assertTrue(state.isSpeechSessionBusy)
        assertEquals("tts-1", state.activeSpeechSession?.id)
        assertEquals("你好，数字狗", state.activeSpeechSession?.text)
        assertEquals(InputSource.Tts, state.activeSpeechSession?.source)
        assertEquals(SpeechSessionStatus.Pending, state.activeSpeechSession?.status)
        assertEquals(TimelineQuality.Stable, state.timelineQuality)
        assertEquals(InputSource.Tts, state.activeSpeechSession?.timeline?.source)
        assertEquals(TimelineQuality.Stable, state.activeSpeechSession?.timeline?.quality)
        assertEquals(0, state.activeSpeechSession?.timeline?.latencyOffsetMs)
        assertTrue(state.activeSpeechSession?.timeline?.segments?.isNotEmpty() == true)
        assertEquals(
            state.activeSpeechSession?.timeline?.segments?.last()?.endMs,
            state.activeSpeechSession?.timeline?.durationMs,
        )
    }

    @Test
    fun submitStoresTextThatMatchesTimelineTextRanges() {
        val state = TtsSubmitReducer.submitText(
            SpeechDemoState(ttsInputText = "  my   狗\n\nOK  "),
        )
        val session = state.activeSpeechSession!!

        assertEquals("my 狗\nOK", session.text)
        assertEquals("my 狗\nOK", state.ttsInputText)
        session.timeline!!.segments.mapNotNull { it.textRange }.forEach { range ->
            assertEquals(range.text, session.text.substring(range.start, range.end))
        }
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
        assertEquals("tts-1", second.activeSpeechSession?.id)
        assertEquals("第一句", second.activeSpeechSession?.text)
        assertEquals(first.activeSpeechSession?.timeline, second.activeSpeechSession?.timeline)
        assertEquals("第二句", second.ttsInputText)
        assertTrue(second.isSpeechSessionBusy)
        assertEquals(PetState.Thinking, second.petState)
        assertEquals(MouthShape.Closed, second.currentMouth)
    }

    @Test
    fun manualMouthSelectionCannotMutateActiveTtsSessionState() {
        val busyState = TtsSubmitReducer.submitText(
            SpeechDemoState(ttsInputText = "第一句"),
        )

        val manualAttempt = ManualMouthTestReducer.selectMouth(
            current = busyState,
            mouth = MouthShape.Wide,
        )

        assertSame(busyState.activeSpeechSession, manualAttempt.activeSpeechSession)
        assertEquals(PetState.Thinking, manualAttempt.petState)
        assertEquals(MouthShape.Closed, manualAttempt.currentMouth)
        assertEquals(InputSource.Tts, manualAttempt.inputSource)
        assertTrue(manualAttempt.isSpeechSessionBusy)
    }

    @Test
    fun manualMouthSelectionClearsRecoverableTtsInputError() {
        val errorState = TtsSubmitReducer.submitText(
            SpeechDemoState(ttsInputText = " "),
        )

        val manualState = ManualMouthTestReducer.selectMouth(
            current = errorState,
            mouth = MouthShape.Wide,
        )

        assertEquals(PetState.Idle, manualState.petState)
        assertEquals(MouthShape.Wide, manualState.currentMouth)
        assertEquals(InputSource.Manual, manualState.inputSource)
        assertNull(manualState.inputError)
        assertNull(manualState.activeSpeechSession)
        assertFalse(manualState.isSpeechSessionBusy)
    }
}
