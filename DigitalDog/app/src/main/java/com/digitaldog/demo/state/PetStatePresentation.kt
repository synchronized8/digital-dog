package com.digitaldog.demo.state

import androidx.compose.ui.graphics.Color
import com.digitaldog.demo.app.AppContentContract
import com.digitaldog.demo.designsystem.DogColors
import com.digitaldog.demo.sharedmodel.PetState

data class CollarFeedback(
    val color: Color,
    val role: String,
    val description: String,
)

data class PetStatePresentation(
    val stateLabel: String,
    val stateDescription: String,
    val poseSummary: String,
    val collar: CollarFeedback,
)

fun SpeechDemoState.toPresentation(): PetStatePresentation = petState.toPresentation()

fun PetState.toPresentation(): PetStatePresentation = when (this) {
    PetState.Idle -> PetStatePresentation(
        stateLabel = AppContentContract.StatusIdle,
        stateDescription = "闭口待机",
        poseSummary = "闭口待机",
        collar = CollarFeedback(
            color = DogColors.TextSecondary,
            role = "低亮",
            description = "项圈低亮，等待输入",
        ),
    )

    PetState.Listening -> PetStatePresentation(
        stateLabel = AppContentContract.StatusListening,
        stateDescription = "正在聆听",
        poseSummary = "耳朵竖起，眼睛看向用户",
        collar = CollarFeedback(
            color = DogColors.TechBlue,
            role = "科技蓝",
            description = "项圈科技蓝，正在聆听",
        ),
    )

    PetState.Thinking -> PetStatePresentation(
        stateLabel = AppContentContract.StatusThinking,
        stateDescription = "闭口思考",
        poseSummary = "闭口思考，轻微歪头",
        collar = CollarFeedback(
            color = DogColors.TechBlue,
            role = "蓝色呼吸语义",
            description = "项圈蓝色呼吸语义，正在思考",
        ),
    )

    PetState.Speaking -> PetStatePresentation(
        stateLabel = AppContentContract.StatusSpeaking,
        stateDescription = "准备讲话",
        poseSummary = "准备讲话状态",
        collar = CollarFeedback(
            color = DogColors.Coral,
            role = "珊瑚",
            description = "项圈珊瑚提示，讲话状态",
        ),
    )

    PetState.Done -> PetStatePresentation(
        stateLabel = AppContentContract.StatusDone,
        stateDescription = "闭口完成",
        poseSummary = "闭口完成反馈",
        collar = CollarFeedback(
            color = DogColors.SuccessGreen,
            role = "成功绿",
            description = "项圈成功绿，演示完成",
        ),
    )

    PetState.Error -> PetStatePresentation(
        stateLabel = AppContentContract.StatusError,
        stateDescription = "闭口疑惑",
        poseSummary = "闭口疑惑，需要处理",
        collar = CollarFeedback(
            color = DogColors.WarningYellow,
            role = "提示黄",
            description = "项圈提示黄，需要处理",
        ),
    )
}
