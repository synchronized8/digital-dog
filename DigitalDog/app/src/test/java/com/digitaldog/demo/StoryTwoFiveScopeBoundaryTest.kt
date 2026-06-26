package com.digitaldog.demo

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StoryTwoFiveScopeBoundaryTest {
    @Test
    fun storyTwoFiveKeepsDefaultUiFreeOfDeferredDemoAndDebugCapabilities() {
        val projectRoot = sequenceOf(
            File("."),
            File(".."),
        ).firstOrNull { File(it, "app/src/main").exists() }

        assertTrue("project root with app/src/main should exist", projectRoot != null)

        val forbiddenTokens = listOf(
            "UploadAudio",
            "StartRecording",
            "DebugPanel",
            "debugpanel",
            "DebugSnapshot",
            "DebugSummary",
            "audioinput",
            "ManualMouth",
            "ManualMouthTest",
            "Waveform",
            "TimelineView",
            "TimelineComposable",
            "TextHighlight",
            "HighlightedText",
            "LatencyCalibration",
            "DelayCalibration",
            "QualityPanel",
            "TimelineQuality",
            "LipSyncTimeline",
            "lipsync",
            "TtsTimelineGenerator",
            "playback",
            "ACTION_OPEN_DOCUMENT",
            "OpenDocument",
            "GetContent",
            "FilePicker",
            "MediaRecorder",
            "AudioRecord",
            "RECORD_AUDIO",
            "android.permission.RECORD_AUDIO",
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
            "上传音频",
            "开始录音",
            "麦克风",
            "波形",
            "嘴型时间轴",
            "文本高亮",
            "延迟校准",
            "完整调试台",
            "解析质量",
            "同步调试",
            "手动嘴型测试",
        )

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
                val normalizedPath = file.path.replace(File.separatorChar, '/')
                val sanitizedText = if (
                    normalizedPath.contains("/src/test/") ||
                    normalizedPath.contains("/src/androidTest/")
                ) {
                    stripAssertionLiterals(
                        sourceText = file.readText(),
                        forbiddenTokens = forbiddenTokens,
                    )
                } else {
                    file.readText()
                }
                "$normalizedPath\n$sanitizedText"
            }

        forbiddenTokens.forEach { token ->
            assertFalse("Story 2.5 default UI must not introduce $token", sourceText.contains(token))
        }

        assertTrue(
            "Story 2.5 must not introduce new Gradle dependencies",
            unexpectedDependencyDeclarations(projectRoot!!).isEmpty(),
        )
    }

    private fun unexpectedDependencyDeclarations(projectRoot: File): List<String> {
        val allowedDependencyDeclarations = setOf(
            "implementation(composeBom)",
            "androidTestImplementation(composeBom)",
            "implementation(\"androidx.activity:activity-compose:1.9.0\")",
            "implementation(\"androidx.compose.foundation:foundation\")",
            "implementation(\"androidx.compose.material3:material3\")",
            "implementation(\"androidx.compose.ui:ui\")",
            "implementation(\"androidx.compose.ui:ui-tooling-preview\")",
            "debugImplementation(\"androidx.compose.ui:ui-tooling\")",
            "debugImplementation(\"androidx.compose.ui:ui-test-manifest\")",
            "testImplementation(\"junit:junit:4.13.2\")",
            "androidTestImplementation(\"androidx.test.ext:junit:1.2.1\")",
            "androidTestImplementation(\"androidx.test.espresso:espresso-core:3.6.1\")",
            "androidTestImplementation(\"androidx.compose.ui:ui-test-junit4\")",
        )
        val dependencyDeclarationPrefixes = listOf(
            "implementation(",
            "debugImplementation(",
            "testImplementation(",
            "androidTestImplementation(",
        )

        return File(projectRoot, "app/build.gradle.kts")
            .readLines()
            .map { it.trim() }
            .filter { line ->
                dependencyDeclarationPrefixes.any { prefix ->
                    line.startsWith(prefix)
                }
            }
            .filterNot { it in allowedDependencyDeclarations }
    }

    private fun stripAssertionLiterals(
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
