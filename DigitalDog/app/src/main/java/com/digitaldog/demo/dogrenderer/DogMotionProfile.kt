package com.digitaldog.demo.dogrenderer

import com.digitaldog.demo.sharedmodel.PetState

data class DogMotionProfile(
    val summary: String,
    val breathing: Boolean = false,
    val blink: Boolean = false,
    val earLift: Boolean = false,
    val eyeFocus: Boolean = false,
    val speakingPulse: Boolean = false,
    val tailWag: Boolean = false,
    val confusedEyes: Boolean = false,
    val headTiltDegrees: Float = 0f,
    val mouthCanBeCovered: Boolean = false,
    val mouthRemainsObservable: Boolean = true,
) {
    companion object {
        fun forState(petState: PetState): DogMotionProfile = when (petState) {
            PetState.Idle -> DogMotionProfile(
                summary = "轻微呼吸，偶发眨眼",
                breathing = true,
                blink = true,
            )

            PetState.Listening -> DogMotionProfile(
                summary = "耳朵竖起，眼睛看向用户",
                earLift = true,
                eyeFocus = true,
            )

            PetState.Thinking -> DogMotionProfile(
                summary = "闭口思考，轻微歪头",
                headTiltDegrees = -3f,
            )

            PetState.Speaking -> DogMotionProfile(
                summary = "嘴型优先，低幅头身动作",
                speakingPulse = true,
            )

            PetState.Done -> DogMotionProfile(
                summary = "完成眨眼，轻摆尾巴",
                blink = true,
                tailWag = true,
            )

            PetState.Error -> DogMotionProfile(
                summary = "困惑歪头，提示黄项圈",
                confusedEyes = true,
                headTiltDegrees = 4f,
            )
        }
    }
}
