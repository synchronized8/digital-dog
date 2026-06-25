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
            currentMouth = MouthShape.Closed,
        )
    }
}
