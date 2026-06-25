package com.digitaldog.demo.state

import com.digitaldog.demo.sharedmodel.InputSource
import com.digitaldog.demo.sharedmodel.MouthShape
import com.digitaldog.demo.sharedmodel.PetState
import com.digitaldog.demo.sharedmodel.SpeechSession
import com.digitaldog.demo.sharedmodel.TimelineQuality

data class SpeechDemoState(
    val petState: PetState = PetState.Idle,
    val currentMouth: MouthShape = MouthShape.Closed,
    val inputSource: InputSource = InputSource.None,
    val timelineQuality: TimelineQuality = TimelineQuality.Ready,
    val ttsInputText: String = "",
    val inputError: String? = null,
    val activeSpeechSession: SpeechSession? = null,
    val nextSpeechSessionIndex: Int = 1,
) {
    val isSpeechSessionBusy: Boolean
        get() = activeSpeechSession != null
}
