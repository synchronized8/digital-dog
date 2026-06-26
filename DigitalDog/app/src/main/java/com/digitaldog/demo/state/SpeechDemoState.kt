package com.digitaldog.demo.state

import com.digitaldog.demo.sharedmodel.InputSource
import com.digitaldog.demo.sharedmodel.MouthShape
import com.digitaldog.demo.sharedmodel.PetState
import com.digitaldog.demo.sharedmodel.SpeechSession

data class SpeechDemoState(
    val petState: PetState = PetState.Idle,
    val currentMouth: MouthShape = MouthShape.Closed,
    val inputSource: InputSource = InputSource.None,
    val ttsInputText: String = "",
    val inputError: String? = null,
    val activeSpeechSession: SpeechSession? = null,
    val nextSpeechSessionIndex: Long = 1L,
    val speechAnimationState: SpeechAnimationState = SpeechAnimationState.Idle,
) {
    val isSpeechSessionBusy: Boolean
        get() = activeSpeechSession != null
}
