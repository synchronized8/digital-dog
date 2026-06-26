package com.digitaldog.demo.sharedmodel

enum class MouthShape(
    val stableId: String,
    val label: String,
    val description: String,
) {
    Closed(
        stableId = "closed",
        label = "闭合",
        description = "柔和闭合嘴线",
    ),
    Open(
        stableId = "open",
        label = "张口",
        description = "讲话时使用的简单张口嘴型",
    ),
    ;

    val accessibleLabel: String
        get() = "$label $stableId"
}
