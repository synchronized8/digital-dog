package com.digitaldog.demo

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StoryTwoOneScopeBoundaryTest {
    @Test
    fun storyTwoOneDoesNotImplementOutOfScopeSpeechOrAudioCapabilities() {
        val projectRoot = sequenceOf(
            File("."),
            File(".."),
        ).firstOrNull { File(it, "app/src/main").exists() }

        assertTrue("project root with app/src/main should exist", projectRoot != null)

        val forbiddenTokens = listOf(
            "TextToSpeech",
            "android.speech.tts",
            "MediaPlayer",
            "AudioTrack",
            "SpeechRecognizer",
            "TimelinePlayer",
            "RECORD_AUDIO",
            "android.permission.INTERNET",
            "RoomDatabase",
            "androidx.room",
            "Retrofit",
            "retrofit2",
            "OkHttp",
            "okhttp3",
            "Ktor",
            "ExoPlayer",
            "androidx.media3",
            "Media3",
        )
        val currentTestPath = File(
            projectRoot,
            "app/src/test/java/com/digitaldog/demo/StoryTwoOneScopeBoundaryTest.kt",
        ).canonicalPath

        val filesToScan = listOf(
            File(projectRoot, "app/src/main"),
            File(projectRoot, "app/src/test"),
            File(projectRoot, "app/src/androidTest"),
        ).flatMap { root ->
            root.walkTopDown()
                .filter { it.isFile && it.extension in setOf("kt", "java", "xml", "kts", "gradle", "toml") }
                .toList()
        } + listOf(
            File(projectRoot, "build.gradle.kts"),
            File(projectRoot, "app/build.gradle.kts"),
            File(projectRoot, "settings.gradle.kts"),
            File(projectRoot, "gradle.properties"),
            File(projectRoot, "gradle/libs.versions.toml"),
        ).filter { it.isFile }

        val sourceText = filesToScan
            .distinctBy { it.canonicalPath }
            .joinToString(separator = "\n") { file ->
                when {
                    file.canonicalPath == currentTestPath -> ""
                    file.name.endsWith("ScopeBoundaryTest.kt") -> {
                        stripBoundaryAssertionLiterals(
                            sourceText = file.readText(),
                            forbiddenTokens = forbiddenTokens,
                        )
                    }
                    else -> file.readText()
                }
            }

        forbiddenTokens.forEach { token ->
            assertFalse("Story 2.1 must not introduce $token", sourceText.contains(token))
        }
    }

    private fun stripBoundaryAssertionLiterals(
        sourceText: String,
        forbiddenTokens: List<String>,
    ): String = sourceText
        .lineSequence()
        .filterNot { line ->
            forbiddenTokens.any { token ->
                line.contains('"') && line.contains(token)
            }
        }
        .joinToString(separator = "\n")
}
