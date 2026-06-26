package com.digitaldog.demo.state

const val DefaultSpeechAnimationDurationMs = 1_800
const val DefaultCompletionFeedbackDurationMs = 700

data class SpeechAnimationState(
    val isSpeaking: Boolean = false,
    val mouthOpen: Boolean = false,
    val estimatedDurationMs: Int = DefaultSpeechAnimationDurationMs,
    val actionCue: DogActionCue = DogActionCue.None,
) {
    companion object {
        val Idle = SpeechAnimationState(
            estimatedDurationMs = 0,
        )
        val Preparing = SpeechAnimationState(
            isSpeaking = false,
            mouthOpen = false,
            actionCue = DogActionCue.EarPerk,
        )
        val Speaking = SpeechAnimationState(
            isSpeaking = true,
            mouthOpen = true,
            actionCue = DogActionCue.BodyBounce,
        )
        val Done = SpeechAnimationState(
            isSpeaking = false,
            mouthOpen = false,
            estimatedDurationMs = DefaultCompletionFeedbackDurationMs,
            actionCue = DogActionCue.TailWag,
        )
    }
}

enum class DogActionCue {
    None,
    EarPerk,
    HeadTilt,
    TailWag,
    Blink,
    BodyBounce,
}
