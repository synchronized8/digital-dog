package com.digitaldog.demo

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StoryOneFiveScopeBoundaryTest {
    @Test
    fun storyOneFiveDoesNotIntroduceOutOfScopePlatformCapabilities() {
        val mainSourceRoot = sequenceOf(
            File("src/main"),
            File("app/src/main"),
        ).firstOrNull { it.exists() }

        assertTrue("main source root should exist", mainSourceRoot != null)

        val sourceFiles = mainSourceRoot!!
            .walkTopDown()
            .filter { it.isFile && it.extension in setOf("kt", "xml", "kts", "gradle") }
            .toList()
        val configFiles = listOf(
            File("build.gradle.kts"),
            File("app/build.gradle.kts"),
            File("settings.gradle.kts"),
            File("../settings.gradle.kts"),
            File("gradle.properties"),
            File("../gradle.properties"),
        ).filter { it.isFile }
        val sourceText = (sourceFiles + configFiles)
            .distinctBy { it.canonicalPath }
            .joinToString(separator = "\n") { it.readText() }

        val forbiddenTokens = listOf(
            "TextToSpeech",
            "MediaPlayer",
            "AudioTrack",
            "SpeechRecognizer",
            "TimelinePlayer",
            "RECORD_AUDIO",
            "android.permission.INTERNET",
            "RoomDatabase",
            "Retrofit",
        )

        forbiddenTokens.forEach { token ->
            assertFalse("Story 1.5 must not introduce $token", sourceText.contains(token))
        }
    }
}
