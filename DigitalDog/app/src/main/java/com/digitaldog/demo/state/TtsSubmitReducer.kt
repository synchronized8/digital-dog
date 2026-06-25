package com.digitaldog.demo.state

import com.digitaldog.demo.app.AppContentContract
import com.digitaldog.demo.lipsync.TtsTimelineGenerator
import com.digitaldog.demo.sharedmodel.InputSource
import com.digitaldog.demo.sharedmodel.MouthShape
import com.digitaldog.demo.sharedmodel.PetState
import com.digitaldog.demo.sharedmodel.SpeechSession

object TtsSubmitReducer {
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
        )
    }

    fun submitText(current: SpeechDemoState): SpeechDemoState {
        if (current.isSpeechSessionBusy) {
            return current.copy(currentMouth = MouthShape.Closed)
        }

        val cleanedText = TtsTimelineGenerator.normalizeText(current.ttsInputText)
        if (cleanedText.isBlank()) {
            return current.copy(
                petState = PetState.Error,
                currentMouth = MouthShape.Closed,
                inputSource = InputSource.Tts,
                inputError = AppContentContract.EmptyTtsInputError,
                activeSpeechSession = null,
            )
        }

        val timeline = TtsTimelineGenerator.generate(cleanedText)
        val session = SpeechSession(
            id = "tts-${current.nextSpeechSessionIndex}",
            source = InputSource.Tts,
            text = cleanedText,
            timeline = timeline,
        )

        return current.copy(
            petState = PetState.Thinking,
            currentMouth = MouthShape.Closed,
            inputSource = InputSource.Tts,
            timelineQuality = timeline.quality,
            ttsInputText = cleanedText,
            inputError = null,
            activeSpeechSession = session,
            nextSpeechSessionIndex = current.nextSpeechSessionIndex + 1,
        )
    }
}
