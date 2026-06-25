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
    Small(
        stableId = "small",
        label = "小开口",
        description = "小椭圆或小弧形开口",
    ),
    Wide(
        stableId = "wide",
        label = "大开口",
        description = "纵向明显张口",
    ),
    Round(
        stableId = "round",
        label = "圆嘴",
        description = "圆形或椭圆开口",
    ),
    Smile(
        stableId = "smile",
        label = "咧嘴",
        description = "横向咧嘴",
    ),
    Teeth(
        stableId = "teeth",
        label = "露齿",
        description = "轻微露齿或上下咬合",
    ),
    Pant(
        stableId = "pant",
        label = "喘气",
        description = "开心喘气嘴型",
    ),
    ;

    val accessibleLabel: String
        get() = "$label $stableId"
}
