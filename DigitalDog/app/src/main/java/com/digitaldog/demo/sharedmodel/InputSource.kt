package com.digitaldog.demo.sharedmodel

enum class InputSource(
    val stableId: String,
    val label: String,
) {
    None(
        stableId = "none",
        label = "未选择",
    ),
    Tts(
        stableId = "text",
        label = "文本",
    ),
    Sample(
        stableId = "sample",
        label = "示例",
    ),
}
