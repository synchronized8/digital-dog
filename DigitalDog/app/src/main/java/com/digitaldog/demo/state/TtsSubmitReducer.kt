package com.digitaldog.demo.state

import com.digitaldog.demo.app.AppContentContract
import com.digitaldog.demo.sharedmodel.InputSource
import com.digitaldog.demo.sharedmodel.MouthShape
import com.digitaldog.demo.sharedmodel.PetState
import com.digitaldog.demo.sharedmodel.SpeechSession

object TtsSubmitReducer {
    private const val SampleText = "汪，今天也要开心呀"

    fun updateText(
        current: SpeechDemoState,
        text: String,
    ): SpeechDemoState {
        val clearsRecoverableError = current.inputError != null && !current.isSpeechSessionBusy

        return current.copy(
            ttsInputText = text,
            inputError = null,
            petState = if (clearsRecoverableError) PetState.Idle else current.petState,
            currentMouth = if (clearsRecoverableError) MouthShape.Closed else current.currentMouth,
            inputSource = if (clearsRecoverableError) InputSource.None else current.inputSource,
            speechAnimationState = if (clearsRecoverableError) {
                SpeechAnimationState.Idle
            } else {
                current.speechAnimationState
            },
        )
    }

    fun submitText(current: SpeechDemoState): SpeechDemoState {
        val cleanedText = normalizeText(current.ttsInputText)
        return startSpeech(
            current = current,
            source = InputSource.Tts,
            text = cleanedText,
            emptyTextIsError = true,
        )
    }

    fun playSample(current: SpeechDemoState): SpeechDemoState = startSpeech(
        current = current,
        source = InputSource.Sample,
        text = SampleText,
        emptyTextIsError = false,
    )

    private fun startSpeech(
        current: SpeechDemoState,
        source: InputSource,
        text: String,
        emptyTextIsError: Boolean,
    ): SpeechDemoState {
        if (current.isSpeechSessionBusy) {
            return current
        }

        if (text.isBlank() && emptyTextIsError) {
            return current.copy(
                petState = PetState.Error,
                currentMouth = MouthShape.Closed,
                inputSource = InputSource.Tts,
                inputError = AppContentContract.EmptyTtsInputError,
                activeSpeechSession = null,
                speechAnimationState = SpeechAnimationState.Idle,
            )
        }

        val sessionIndex = current.nextSpeechSessionIndex.coerceAtLeast(1L)
        val session = SpeechSession(
            id = "${source.stableId}-$sessionIndex",
            source = source,
            text = text,
        )

        return current.copy(
            petState = PetState.Thinking,
            currentMouth = MouthShape.Closed,
            inputSource = source,
            ttsInputText = if (source == InputSource.Tts) text else current.ttsInputText,
            inputError = null,
            activeSpeechSession = session,
            nextSpeechSessionIndex = nextSessionIndexAfter(sessionIndex),
            speechAnimationState = SpeechAnimationState.Preparing,
        )
    }

    fun startSpeaking(current: SpeechDemoState): SpeechDemoState {
        val activeSession = current.activeSpeechSession ?: return current
        if (current.petState != PetState.Thinking) {
            return current
        }

        return current.copy(
            petState = PetState.Speaking,
            currentMouth = MouthShape.Open,
            inputSource = activeSession.source,
            inputError = null,
            speechAnimationState = SpeechAnimationState.Speaking,
        )
    }

    fun completeSpeaking(current: SpeechDemoState): SpeechDemoState {
        if (current.petState != PetState.Speaking || current.activeSpeechSession == null) {
            return current
        }

        return current.copy(
            petState = PetState.Done,
            currentMouth = MouthShape.Closed,
            inputSource = current.activeSpeechSession.source,
            inputError = null,
            activeSpeechSession = null,
            speechAnimationState = SpeechAnimationState.Done,
        )
    }

    fun returnToIdleAfterCompletion(current: SpeechDemoState): SpeechDemoState {
        if (current.petState != PetState.Done || current.activeSpeechSession != null) {
            return current
        }

        return current.copy(
            petState = PetState.Idle,
            currentMouth = MouthShape.Closed,
            inputSource = InputSource.None,
            inputError = null,
            activeSpeechSession = null,
            speechAnimationState = SpeechAnimationState.Idle,
        )
    }

    private fun normalizeText(text: String): String = text
        .filterNot { Character.getType(it) == Character.FORMAT.toInt() }
        .trim()
        .replace(Regex("\\s+"), " ")

    private fun nextSessionIndexAfter(current: Long): Long = current + 1L
}
