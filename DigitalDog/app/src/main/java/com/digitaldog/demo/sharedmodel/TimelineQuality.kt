package com.digitaldog.demo.sharedmodel

enum class TimelineQuality(
    val stableId: String,
    val label: String,
) {
    Ready(
        stableId = "ready",
        label = "稳定演示待开始",
    ),
    Stable(
        stableId = "stable",
        label = "稳定",
    ),
}
