package com.digitaldog.demo.sharedmodel

enum class PetState(val stableId: String) {
    Idle("idle"),
    Listening("listening"),
    Thinking("thinking"),
    Speaking("speaking"),
    Done("done"),
    Error("error"),
}
