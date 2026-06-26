package com.digitaldog.demo.state

import com.digitaldog.demo.sharedmodel.MouthShape
import com.digitaldog.demo.sharedmodel.PetState

enum class PetStateEvent {
    EnterListening,
    EnterThinking,
    EnterSpeaking,
    EnterDone,
    EnterError,
    ReturnToIdle,
}

object PetStateReducer {
    fun reduce(
        current: SpeechDemoState,
        event: PetStateEvent,
    ): SpeechDemoState {
        val nextState = when (event) {
            PetStateEvent.EnterListening -> PetState.Listening
            PetStateEvent.EnterThinking -> PetState.Thinking
            PetStateEvent.EnterSpeaking -> PetState.Speaking
            PetStateEvent.EnterDone -> PetState.Done
            PetStateEvent.EnterError -> PetState.Error
            PetStateEvent.ReturnToIdle -> PetState.Idle
        }

        return current.copy(
            petState = nextState,
            currentMouth = if (nextState == PetState.Speaking) MouthShape.Open else MouthShape.Closed,
            speechAnimationState = when (nextState) {
                PetState.Speaking -> SpeechAnimationState.Speaking
                PetState.Thinking -> SpeechAnimationState.Preparing
                PetState.Done -> SpeechAnimationState.Done
                PetState.Error -> SpeechAnimationState(
                    isSpeaking = false,
                    mouthOpen = false,
                    actionCue = DogActionCue.HeadTilt,
                )
                else -> SpeechAnimationState.Idle
            },
        )
    }
}
