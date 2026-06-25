package com.digitaldog.demo.sharedmodel

enum class InputSource(
    val stableId: String,
    val label: String,
) {
    None(
        stableId = "none",
        label = "未选择",
    ),
    Manual(
        stableId = "manual",
        label = "手动测试",
    ),
    Tts(
        stableId = "tts",
        label = "TTS",
    ),
}
