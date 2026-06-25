package com.digitaldog.demo.sharedmodel

data class LipSyncTextRange(
    val start: Int,
    val end: Int,
    val text: String,
) {
    init {
        require(start >= 0) { "text range start must be non-negative" }
        require(end >= start) { "text range end must be greater than or equal to start" }
        require(text.length == end - start) { "text range text must match offset span" }
    }
}

data class LipSyncSegment(
    val id: String,
    val startMs: Int,
    val endMs: Int,
    val mouth: MouthShape,
    val source: InputSource,
    val confidence: Double,
    val textRange: LipSyncTextRange? = null,
    val reason: String? = null,
) {
    init {
        require(id.isNotBlank()) { "segment id must not be blank" }
        require(startMs >= 0) { "segment start must be non-negative" }
        require(endMs > startMs) { "segment end must be greater than start" }
        require(confidence in 0.0..1.0) { "segment confidence must be between 0 and 1" }
    }
}

data class LipSyncTimeline(
    val source: InputSource,
    val durationMs: Int,
    val latencyOffsetMs: Int,
    val segments: List<LipSyncSegment>,
    val quality: TimelineQuality,
    val generatedBy: TimelineGenerator,
) {
    init {
        require(durationMs >= 0) { "timeline duration must be non-negative" }
        if (segments.isEmpty()) {
            require(durationMs == 0) { "empty timeline duration must be zero" }
        } else {
            require(durationMs == segments.last().endMs) {
                "timeline duration must equal the last segment end"
            }
        }

        var previousEnd = 0
        val segmentIds = mutableSetOf<String>()
        segments.forEach { segment ->
            require(segment.source == source) { "segment source must match timeline source" }
            require(segment.startMs >= previousEnd) { "segments must be monotonic and non-overlapping" }
            require(segmentIds.add(segment.id)) { "segment ids must be unique" }
            previousEnd = segment.endMs
        }
    }

    companion object {
        fun empty(source: InputSource): LipSyncTimeline = LipSyncTimeline(
            source = source,
            durationMs = 0,
            latencyOffsetMs = 0,
            segments = emptyList(),
            quality = TimelineQuality.Stable,
            generatedBy = TimelineGenerator.TextRule,
        )
    }
}

enum class TimelineGenerator(
    val stableId: String,
) {
    TextRule("text-rule"),
    Preset("preset"),
    AudioAnalysis("audio-analysis"),
    Manual("manual"),
}
