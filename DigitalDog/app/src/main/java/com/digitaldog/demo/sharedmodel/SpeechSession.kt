package com.digitaldog.demo.sharedmodel

data class SpeechSession(
    val id: String,
    val source: InputSource,
    val text: String,
    val status: SpeechSessionStatus = SpeechSessionStatus.Pending,
    val timeline: LipSyncTimeline? = null,
) {
    init {
        require(id.isNotBlank()) { "session id must not be blank" }
        require(timeline == null || timeline.source == source) {
            "session timeline source must match session source"
        }
    }
}

enum class SpeechSessionStatus {
    Pending,
}
