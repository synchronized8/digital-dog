package com.digitaldog.demo.sharedmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class LipSyncTimelineTest {
    @Test
    fun emptyTimelineUsesZeroDurationAndStableTtsDefaults() {
        val timeline = LipSyncTimeline.empty(source = InputSource.Tts)

        assertEquals(InputSource.Tts, timeline.source)
        assertEquals(TimelineQuality.Stable, timeline.quality)
        assertEquals(TimelineGenerator.TextRule, timeline.generatedBy)
        assertEquals(0, timeline.durationMs)
        assertEquals(0, timeline.latencyOffsetMs)
        assertEquals(emptyList<LipSyncSegment>(), timeline.segments)
    }

    @Test
    fun nonEmptyTimelineDurationMatchesLastSegmentEnd() {
        val timeline = LipSyncTimeline(
            source = InputSource.Tts,
            durationMs = 360,
            latencyOffsetMs = 0,
            quality = TimelineQuality.Stable,
            generatedBy = TimelineGenerator.TextRule,
            segments = listOf(
                LipSyncSegment(
                    id = "seg-001",
                    startMs = 0,
                    endMs = 120,
                    mouth = MouthShape.Closed,
                    source = InputSource.Tts,
                    confidence = 0.9,
                    textRange = null,
                    reason = "pre-speech preparation",
                ),
                LipSyncSegment(
                    id = "seg-002",
                    startMs = 120,
                    endMs = 360,
                    mouth = MouthShape.Wide,
                    source = InputSource.Tts,
                    confidence = 0.75,
                    textRange = LipSyncTextRange(start = 0, end = 1, text = "大"),
                    reason = "Chinese final contains a",
                ),
            ),
        )

        assertEquals(360, timeline.durationMs)
    }

    @Test
    fun timelineRejectsNegativeOrOverlappingSegments() {
        assertThrows(IllegalArgumentException::class.java) {
            LipSyncTimeline(
                source = InputSource.Tts,
                durationMs = 100,
                latencyOffsetMs = 0,
                quality = TimelineQuality.Stable,
                generatedBy = TimelineGenerator.TextRule,
                segments = listOf(
                    LipSyncSegment(
                        id = "seg-001",
                        startMs = -1,
                        endMs = 100,
                        mouth = MouthShape.Small,
                        source = InputSource.Tts,
                        confidence = 0.5,
                    ),
                ),
            )
        }

        assertThrows(IllegalArgumentException::class.java) {
            LipSyncTimeline(
                source = InputSource.Tts,
                durationMs = 180,
                latencyOffsetMs = 0,
                quality = TimelineQuality.Stable,
                generatedBy = TimelineGenerator.TextRule,
                segments = listOf(
                    LipSyncSegment(
                        id = "seg-001",
                        startMs = 0,
                        endMs = 120,
                        mouth = MouthShape.Small,
                        source = InputSource.Tts,
                        confidence = 0.5,
                    ),
                    LipSyncSegment(
                        id = "seg-002",
                        startMs = 100,
                        endMs = 180,
                        mouth = MouthShape.Round,
                        source = InputSource.Tts,
                        confidence = 0.5,
                    ),
                ),
            )
        }
    }

    @Test
    fun textRangeUsesStartInclusiveEndExclusiveOffsets() {
        val range = LipSyncTextRange(start = 2, end = 4, text = "狗狗")

        assertEquals(2, range.start)
        assertEquals(4, range.end)
        assertEquals("狗狗", range.text)
    }

    @Test
    fun textRangeRejectsTextThatDoesNotMatchOffsetSpan() {
        assertThrows(IllegalArgumentException::class.java) {
            LipSyncTextRange(start = 0, end = 1, text = "狗狗")
        }
    }

    @Test
    fun timelineRejectsDuplicateSegmentIds() {
        assertThrows(IllegalArgumentException::class.java) {
            LipSyncTimeline(
                source = InputSource.Tts,
                durationMs = 240,
                latencyOffsetMs = 0,
                quality = TimelineQuality.Stable,
                generatedBy = TimelineGenerator.TextRule,
                segments = listOf(
                    LipSyncSegment(
                        id = "seg-001",
                        startMs = 0,
                        endMs = 120,
                        mouth = MouthShape.Small,
                        source = InputSource.Tts,
                        confidence = 0.5,
                    ),
                    LipSyncSegment(
                        id = "seg-001",
                        startMs = 120,
                        endMs = 240,
                        mouth = MouthShape.Round,
                        source = InputSource.Tts,
                        confidence = 0.5,
                    ),
                ),
            )
        }
    }

    @Test
    fun speechSessionRejectsTimelineWithDifferentSource() {
        val timeline = LipSyncTimeline.empty(source = InputSource.Manual)

        assertThrows(IllegalArgumentException::class.java) {
            SpeechSession(
                id = "tts-1",
                source = InputSource.Tts,
                text = "hello",
                timeline = timeline,
            )
        }
    }
}
