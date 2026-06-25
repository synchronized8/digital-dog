package com.digitaldog.demo.lipsync

import com.digitaldog.demo.sharedmodel.InputSource
import com.digitaldog.demo.sharedmodel.MouthShape
import com.digitaldog.demo.sharedmodel.TimelineGenerator
import com.digitaldog.demo.sharedmodel.TimelineQuality
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TtsTimelineGeneratorTest {
    @Test
    fun blankTextReturnsEmptyStableTtsTimeline() {
        val timeline = TtsTimelineGenerator.generate(" \n\t ")

        assertEquals(InputSource.Tts, timeline.source)
        assertEquals(TimelineQuality.Stable, timeline.quality)
        assertEquals(TimelineGenerator.TextRule, timeline.generatedBy)
        assertEquals(0, timeline.durationMs)
        assertEquals(0, timeline.latencyOffsetMs)
        assertTrue(timeline.segments.isEmpty())
    }

    @Test
    fun chineseTextUsesDictionaryAndFallbackWithoutPantOrUnknownMouths() {
        val timeline = TtsTimelineGenerator.generate("狗说话龘")
        val mouths = timeline.segments.map { it.mouth }

        assertTrue(MouthShape.Small in mouths)
        assertTrue(MouthShape.Round in mouths)
        assertTrue(MouthShape.Teeth in mouths)
        assertTrue(MouthShape.Wide in mouths)
        assertFalse(MouthShape.Pant in mouths)
        assertTrue(
            "unknown Chinese characters should degrade to small",
            timeline.segments.any { it.textRange?.text == "龘" && it.mouth == MouthShape.Small },
        )
        assertTimelineIsContinuous(timelineStartMs = 0, segmentTimes = timeline.segments.map { it.startMs to it.endMs })
    }

    @Test
    fun englishTextUsesHeuristicsForExpectedVisemeFamilies() {
        val timeline = TtsTimelineGenerator.generate("my fish you happy tiny")
        val mouths = timeline.segments.map { it.mouth }.toSet()

        assertTrue(MouthShape.Closed in mouths)
        assertTrue(MouthShape.Teeth in mouths)
        assertTrue(MouthShape.Round in mouths)
        assertTrue(MouthShape.Wide in mouths)
        assertTrue(MouthShape.Smile in mouths)
        assertFalse(MouthShape.Pant in mouths)
    }

    @Test
    fun mixedCaseEnglishGeneratesSameMouthSequence() {
        val lower = TtsTimelineGenerator.generate("my fish you")
        val mixed = TtsTimelineGenerator.generate("My FISH You")

        assertEquals(
            lower.segments.map { it.mouth },
            mixed.segments.map { it.mouth },
        )
    }

    @Test
    fun punctuationAndNewlinesGenerateClosedNonHighlightPauseSegments() {
        val timeline = TtsTimelineGenerator.generate("你好，狗！\nOK?")
        val pauseSegments = timeline.segments.filter { it.reason?.contains("pause") == true }

        assertEquals(4, pauseSegments.size)
        assertTrue(pauseSegments.all { it.mouth == MouthShape.Closed })
        assertTrue(pauseSegments.all { it.textRange == null })
        assertTrue(pauseSegments.all { it.endMs > it.startMs })
    }

    @Test
    fun commonPunctuationGeneratesClosedPauseSegmentsInsteadOfVoicedFallbacks() {
        val timeline = TtsTimelineGenerator.generate("hi: dog; (ok)")
        val punctuationPauses = timeline.segments.filter {
            it.reason == "punctuation pause"
        }

        assertEquals(4, punctuationPauses.size)
        assertTrue(punctuationPauses.all { it.mouth == MouthShape.Closed })
        assertTrue(punctuationPauses.all { it.textRange == null })
    }

    @Test
    fun supplementaryUnicodeCharactersStaySingleFallbackTokens() {
        val timeline = TtsTimelineGenerator.generate("hi 🐶 狗")
        val dogEmojiRanges = timeline.segments.mapNotNull { it.textRange }
            .filter { it.text == "🐶" }

        assertEquals(1, dogEmojiRanges.size)
        assertEquals(3, dogEmojiRanges.single().start)
        assertEquals(5, dogEmojiRanges.single().end)
        assertFalse(timeline.segments.any { it.textRange?.text == "\uD83D" })
        assertFalse(timeline.segments.any { it.textRange?.text == "\uDC36" })
    }

    @Test
    fun textRangesStayWithinCleanedTextAndKeepTokenText() {
        val timeline = TtsTimelineGenerator.generate("  my   狗  ")
        val ranges = timeline.segments.mapNotNull { it.textRange }

        assertTrue(ranges.any { it.text == "my" && it.start == 0 && it.end == 2 })
        assertTrue(ranges.any { it.text == "狗" && it.start == 3 && it.end == 4 })
        assertTrue(ranges.all { it.start >= 0 && it.end >= it.start })
    }

    @Test
    fun generationIsDeterministic() {
        val first = TtsTimelineGenerator.generate("my happy 狗说话!")
        val second = TtsTimelineGenerator.generate("my happy 狗说话!")

        assertEquals(first, second)
    }

    private fun assertTimelineIsContinuous(
        timelineStartMs: Int,
        segmentTimes: List<Pair<Int, Int>>,
    ) {
        var expectedStart = timelineStartMs
        segmentTimes.forEach { (start, end) ->
            assertEquals(expectedStart, start)
            assertTrue(end > start)
            expectedStart = end
        }
    }
}
