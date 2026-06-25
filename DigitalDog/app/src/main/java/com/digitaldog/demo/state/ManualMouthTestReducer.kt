package com.digitaldog.demo.state

import com.digitaldog.demo.sharedmodel.InputSource
import com.digitaldog.demo.sharedmodel.MouthShape
import com.digitaldog.demo.sharedmodel.PetState
import com.digitaldog.demo.sharedmodel.TimelineQuality

object ManualMouthTestReducer {
    fun selectMouth(
        current: SpeechDemoState,
        mouth: MouthShape,
    ): SpeechDemoState {
        if (current.isSpeechSessionBusy) {
            return current.copy(
                currentMouth = MouthShape.Closed,
                inputSource = InputSource.Tts,
            )
        }

        return current.copy(
            petState = if (current.inputError != null) PetState.Idle else current.petState,
            currentMouth = mouth,
            inputSource = InputSource.Manual,
            inputError = null,
        )
    }

    fun resetToIdle(current: SpeechDemoState): SpeechDemoState = current.copy(
        petState = PetState.Idle,
        currentMouth = MouthShape.Closed,
        inputSource = InputSource.None,
        timelineQuality = TimelineQuality.Ready,
        inputError = null,
        activeSpeechSession = null,
    )
}
