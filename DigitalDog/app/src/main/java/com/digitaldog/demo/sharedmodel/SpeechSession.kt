package com.digitaldog.demo.sharedmodel

data class SpeechSession(
    val id: String,
    val source: InputSource,
    val text: String,
    val status: SpeechSessionStatus = SpeechSessionStatus.Pending,
) {
    init {
        require(id.isNotBlank()) { "session id must not be blank" }
    }
}

enum class SpeechSessionStatus {
    Pending,
}
