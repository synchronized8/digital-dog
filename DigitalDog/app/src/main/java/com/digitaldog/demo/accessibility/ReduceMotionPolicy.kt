package com.digitaldog.demo.accessibility

import com.digitaldog.demo.dogrenderer.DogMotionProfile

enum class ReduceMotionPolicy(
    val label: String,
    val reducesDecorativeMotion: Boolean,
) {
    Normal(
        label = "正常动态",
        reducesDecorativeMotion = false,
    ) {
        override fun applyTo(profile: DogMotionProfile): DogMotionProfile = profile
    },

    Reduced(
        label = "减少动态",
        reducesDecorativeMotion = true,
    ) {
        override fun applyTo(profile: DogMotionProfile): DogMotionProfile =
            profile.withDecorativeMotionFrozen(summary = "减少动态，保留嘴型和状态摘要")
    },

    Static(
        label = "静态测试",
        reducesDecorativeMotion = true,
    ) {
        override fun applyTo(profile: DogMotionProfile): DogMotionProfile =
            profile.withDecorativeMotionFrozen(summary = "静态测试模式，保留嘴型和状态摘要")
    },
    ;

    abstract fun applyTo(profile: DogMotionProfile): DogMotionProfile

    companion object {
        fun fromAnimatorScale(scaleFactor: Float): ReduceMotionPolicy =
            if (scaleFactor < 1f) Reduced else Normal
    }
}

private fun DogMotionProfile.withDecorativeMotionFrozen(summary: String): DogMotionProfile = copy(
    summary = summary,
    breathing = false,
    blink = false,
    earLift = false,
    eyeFocus = false,
    speakingPulse = false,
    tailWag = false,
    confusedEyes = false,
    headTiltDegrees = 0f,
    mouthCanBeCovered = false,
    mouthRemainsObservable = true,
)
