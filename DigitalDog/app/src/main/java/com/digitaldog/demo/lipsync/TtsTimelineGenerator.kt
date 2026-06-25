package com.digitaldog.demo.lipsync

import com.digitaldog.demo.sharedmodel.InputSource
import com.digitaldog.demo.sharedmodel.LipSyncSegment
import com.digitaldog.demo.sharedmodel.LipSyncTextRange
import com.digitaldog.demo.sharedmodel.LipSyncTimeline
import com.digitaldog.demo.sharedmodel.MouthShape
import com.digitaldog.demo.sharedmodel.TimelineGenerator
import com.digitaldog.demo.sharedmodel.TimelineQuality

object TtsTimelineGenerator {
    fun generate(text: String): LipSyncTimeline {
        val cleanedText = normalizeText(text)
        if (cleanedText.isBlank()) {
            return LipSyncTimeline.empty(source = InputSource.Tts)
        }

        val segments = mutableListOf<LipSyncSegment>()
        var cursorMs = 0

        tokenize(cleanedText).forEach { token ->
            token.toCues().forEach { cue ->
                val endMs = cursorMs + cue.durationMs
                segments += LipSyncSegment(
                    id = "seg-${(segments.size + 1).toString().padStart(3, '0')}",
                    startMs = cursorMs,
                    endMs = endMs,
                    mouth = cue.mouth,
                    source = InputSource.Tts,
                    confidence = cue.confidence,
                    textRange = cue.textRange,
                    reason = cue.reason,
                )
                cursorMs = endMs
            }
        }

        return LipSyncTimeline(
            source = InputSource.Tts,
            durationMs = cursorMs,
            latencyOffsetMs = 0,
            segments = segments,
            quality = TimelineQuality.Stable,
            generatedBy = TimelineGenerator.TextRule,
        )
    }

    fun normalizeText(text: String): String {
        val trimmed = text.trim()
        val output = StringBuilder()
        var pendingSpace = false

        trimmed.forEach { char ->
            when {
                char == '\n' -> {
                    if (output.isNotEmpty() && output.last() == ' ') {
                        output.deleteAt(output.lastIndex)
                    }
                    if (output.isNotEmpty() && output.last() != '\n') {
                        output.append('\n')
                    }
                    pendingSpace = false
                }
                char.isWhitespace() -> {
                    pendingSpace = output.isNotEmpty() && output.last() != '\n'
                }
                else -> {
                    if (pendingSpace && output.isNotEmpty() && output.last() != '\n') {
                        output.append(' ')
                    }
                    output.append(char)
                    pendingSpace = false
                }
            }
        }

        return output.toString()
    }

    private fun tokenize(text: String): List<TextToken> {
        val tokens = mutableListOf<TextToken>()
        var index = 0

        while (index < text.length) {
            val codePoint = text.codePointAt(index)
            val charCount = Character.charCount(codePoint)
            when {
                codePoint == '\n'.code -> {
                    tokens += TextToken.Pause(index, index + 1, LongPauseMs, "newline pause")
                    index += 1
                }
                Character.isWhitespace(codePoint) -> {
                    index += charCount
                }
                isPausePunctuation(codePoint) -> {
                    tokens += TextToken.Pause(
                        start = index,
                        end = index + charCount,
                        durationMs = punctuationDurationMs(codePoint),
                        reason = "punctuation pause",
                    )
                    index += charCount
                }
                isAsciiLetterOrDigit(codePoint) -> {
                    val start = index
                    while (index < text.length) {
                        val wordCodePoint = text.codePointAt(index)
                        if (!isAsciiLetterOrDigit(wordCodePoint)) {
                            break
                        }
                        index += Character.charCount(wordCodePoint)
                    }
                    tokens += TextToken.Word(start, index, text.substring(start, index))
                }
                isCjk(codePoint) -> {
                    tokens += TextToken.ChineseChar(
                        start = index,
                        end = index + charCount,
                        text = text.substring(index, index + charCount),
                    )
                    index += charCount
                }
                else -> {
                    tokens += TextToken.ChineseChar(
                        start = index,
                        end = index + charCount,
                        text = text.substring(index, index + charCount),
                    )
                    index += charCount
                }
            }
        }

        return tokens
    }

    private fun TextToken.toCues(): List<MouthCue> = when (this) {
        is TextToken.ChineseChar -> chineseCues(this)
        is TextToken.Word -> englishCues(this)
        is TextToken.Pause -> listOf(
            MouthCue(
                mouth = MouthShape.Closed,
                durationMs = durationMs,
                confidence = 0.9,
                textRange = null,
                reason = reason,
            ),
        )
    }

    private fun chineseCues(token: TextToken.ChineseChar): List<MouthCue> {
        val mappedMouths = ChineseMouthDictionary[token.text.singleOrNull()]
            ?: listOf(MouthShape.Small)
        val totalDuration = ChineseCharDurationMs
        val range = token.range()

        return if (mappedMouths.size == 1) {
            listOf(
                MouthCue(
                    mouth = mappedMouths.first(),
                    durationMs = totalDuration,
                    confidence = 0.56,
                    textRange = range,
                    reason = "Chinese fallback text rule",
                ),
            )
        } else {
            val firstDuration = (totalDuration * 0.3).toInt()
            val secondDuration = totalDuration - firstDuration
            listOf(
                MouthCue(
                    mouth = mappedMouths.first(),
                    durationMs = firstDuration,
                    confidence = 0.7,
                    textRange = range,
                    reason = "Chinese initial text rule",
                ),
                MouthCue(
                    mouth = mappedMouths.last(),
                    durationMs = secondDuration,
                    confidence = 0.72,
                    textRange = range,
                    reason = "Chinese final text rule",
                ),
            )
        }
    }

    private fun englishCues(token: TextToken.Word): List<MouthCue> {
        val word = token.text.lowercase()
        val totalDuration = englishDurationMs(word)
        val range = token.range()
        val mouths = mutableListOf<MouthShape>()

        initialMouth(word)?.let { mouths += it }
        mainVowelMouth(word)?.let { mouths += it }
        finalMouth(word)?.let { mouth ->
            if (mouths.lastOrNull() != mouth) {
                mouths += mouth
            }
        }
        if (mouths.isEmpty()) {
            mouths += MouthShape.Small
        }

        val durations = splitDuration(totalDuration, mouths.size)
        return mouths.mapIndexed { index, mouth ->
            MouthCue(
                mouth = mouth,
                durationMs = durations[index],
                confidence = if (mouth == MouthShape.Small) 0.56 else 0.68,
                textRange = range,
                reason = "English text rule",
            )
        }
    }

    private fun initialMouth(word: String): MouthShape? = when {
        word.startsWith("m") || word.startsWith("b") || word.startsWith("p") -> MouthShape.Closed
        word.startsWith("f") || word.startsWith("v") || word.startsWith("th") ||
            word.startsWith("s") || word.startsWith("z") || word.startsWith("sh") ||
            word.startsWith("ch") -> MouthShape.Teeth
        word.startsWith("w") -> MouthShape.Round
        else -> null
    }

    private fun mainVowelMouth(word: String): MouthShape? = when {
        word.contains("oo") || word.contains("ou") || word.any { it == 'o' || it == 'u' } -> {
            MouthShape.Round
        }
        word.contains("ah") || word.contains("ar") || word.contains('a') -> MouthShape.Wide
        word.contains("ee") || word.any { it == 'e' || it == 'i' || it == 'y' } -> MouthShape.Smile
        else -> MouthShape.Small
    }

    private fun finalMouth(word: String): MouthShape? = when {
        word.endsWith("f") || word.endsWith("v") || word.endsWith("th") ||
            word.endsWith("s") || word.endsWith("z") || word.endsWith("sh") ||
            word.endsWith("ch") -> MouthShape.Teeth
        word.endsWith("m") || word.endsWith("b") || word.endsWith("p") -> MouthShape.Closed
        else -> null
    }

    private fun splitDuration(totalDurationMs: Int, parts: Int): List<Int> {
        if (parts <= 1) return listOf(totalDurationMs)
        val base = totalDurationMs / parts
        val durations = MutableList(parts) { base }
        durations[durations.lastIndex] += totalDurationMs - base * parts
        return durations
    }

    private fun englishDurationMs(word: String): Int = when {
        word.length <= 3 -> 220
        word.length <= 7 -> 340
        else -> 520
    }

    private fun TextToken.range(): LipSyncTextRange = LipSyncTextRange(
        start = start,
        end = end,
        text = text,
    )

    private fun isPausePunctuation(codePoint: Int): Boolean {
        val char = codePoint.toSingleCharOrNull()
        val type = Character.getType(codePoint)
        return char in ShortPausePunctuation ||
            char in LongPausePunctuation ||
            char in ExclamationPausePunctuation ||
            type == Character.CONNECTOR_PUNCTUATION.toInt() ||
            type == Character.DASH_PUNCTUATION.toInt() ||
            type == Character.START_PUNCTUATION.toInt() ||
            type == Character.END_PUNCTUATION.toInt() ||
            type == Character.INITIAL_QUOTE_PUNCTUATION.toInt() ||
            type == Character.FINAL_QUOTE_PUNCTUATION.toInt() ||
            type == Character.OTHER_PUNCTUATION.toInt()
    }

    private fun punctuationDurationMs(codePoint: Int): Int {
        val char = codePoint.toSingleCharOrNull()
        return when {
        char in ShortPausePunctuation -> ShortPauseMs
        char in ExclamationPausePunctuation -> ExclamationPauseMs
        else -> LongPauseMs
        }
    }

    private fun isAsciiLetterOrDigit(codePoint: Int): Boolean =
        codePoint in 'a'.code..'z'.code ||
            codePoint in 'A'.code..'Z'.code ||
            codePoint in '0'.code..'9'.code

    private fun isCjk(codePoint: Int): Boolean {
        val block = Character.UnicodeBlock.of(codePoint)
        return block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
            block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A ||
            block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B ||
            block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
    }

    private fun Int.toSingleCharOrNull(): Char? =
        if (Character.charCount(this) == 1) {
            toChar()
        } else {
            null
        }

    private sealed class TextToken {
        abstract val start: Int
        abstract val end: Int
        abstract val text: String

        data class ChineseChar(
            override val start: Int,
            override val end: Int,
            override val text: String,
        ) : TextToken()

        data class Word(
            override val start: Int,
            override val end: Int,
            override val text: String,
        ) : TextToken()

        data class Pause(
            override val start: Int,
            override val end: Int,
            val durationMs: Int,
            val reason: String,
        ) : TextToken() {
            override val text: String = ""
        }
    }

    private data class MouthCue(
        val mouth: MouthShape,
        val durationMs: Int,
        val confidence: Double,
        val textRange: LipSyncTextRange?,
        val reason: String,
    )

    private const val ChineseCharDurationMs = 220
    private const val ShortPauseMs = 180
    private const val ExclamationPauseMs = 280
    private const val LongPauseMs = 320

    private val ShortPausePunctuation = setOf(',', '，', '、')
    private val LongPausePunctuation = setOf('.', '。', '?', '？')
    private val ExclamationPausePunctuation = setOf('!', '！')

    private val ChineseMouthDictionary = mapOf(
        '狗' to listOf(MouthShape.Small, MouthShape.Round),
        '说' to listOf(MouthShape.Teeth, MouthShape.Round),
        '話' to listOf(MouthShape.Small, MouthShape.Wide),
        '话' to listOf(MouthShape.Small, MouthShape.Wide),
        '米' to listOf(MouthShape.Closed, MouthShape.Smile),
        '好' to listOf(MouthShape.Small, MouthShape.Wide),
        '你' to listOf(MouthShape.Small, MouthShape.Smile),
        '数' to listOf(MouthShape.Teeth, MouthShape.Round),
        '字' to listOf(MouthShape.Teeth, MouthShape.Small),
    )
}
