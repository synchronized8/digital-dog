package com.digitaldog.demo.app

import com.digitaldog.demo.sharedmodel.MouthShape

object AppContentContract {
    const val Title = "Digital Dog Demo"
    const val StagePlaceholder = "宠物舞台占位"
    const val StageTitle = "宠物舞台"
    const val StatusIdle = "待机"
    const val StatusListening = "聆听"
    const val StatusThinking = "思考"
    const val StatusSpeaking = "讲话中"
    const val StatusDone = "完成"
    const val StatusError = "需要处理"
    const val InputSourceNoneLabel = "未选择"
    const val InputSourceTtsLabel = "文本"
    const val InputSourceSampleLabel = "示例"
    const val InputSourceNone = "输入来源：未选择"
    const val SpeechInputDescription = "主输入区，可以输入文字并播放示例"
    const val QuickActionsDescription = "演示入口，包括让狗狗说话和播放示例"
    const val TextInputPlaceholder = "输入一句想让狗狗说的话"
    const val PrimaryCta = "让狗狗说话"
    const val PrimaryCtaBusy = "讲话中"
    const val EmptyTtsInputError = "先输入一句话"
    const val SampleAudio = "播放示例"
    const val StatusSummaryTitle = "状态摘要"
    const val MouthClosedLabel = "闭合"
    const val CurrentStateIdle = "当前状态：待机"
    const val MainInputTitle = "主输入"
    const val MouthStateClosed = "闭口"
    const val MouthStateOpen = "张口"
    const val MouthStateTalking = "张口讲话"

    val StateLabels = listOf(
        StatusIdle,
        StatusListening,
        StatusThinking,
        StatusSpeaking,
        StatusDone,
        StatusError,
    )

    val StageDescription = stageDescription(
        stateLabel = StatusIdle,
        mouthLabel = mouthSemanticLabel(MouthShape.Closed),
        inputSourceLabel = InputSourceNoneLabel,
        stateDescription = "闭口待机",
        collarDescription = "项圈低亮，等待输入",
        motionDescription = "轻微呼吸，偶发眨眼",
        motionPolicyLabel = "正常动态",
    )
    val StageCaption = stageCaption(
        poseSummary = "闭口待机",
        collarRole = "低亮",
        motionSummary = "轻微呼吸，偶发眨眼",
    )
    val StatusBarDescription = statusBarDescription(
        stateLabel = StatusIdle,
        inputSourceLabel = InputSourceNoneLabel,
        stateDescription = "闭口待机",
        collarDescription = "项圈低亮，等待输入",
    )
    val StatusSummaryDescription = statusSummaryDescription(
        mouthStateLabel = MouthStateClosed,
        stateLabel = StatusIdle,
        inputSourceLabel = InputSourceNoneLabel,
        collarDescription = "项圈低亮，等待输入",
    )

    const val TagStatusBar = "status-bar"
    const val TagPetStage = "pet-stage"
    const val TagSpeechInput = "speech-input"
    const val TagTtsInputField = "tts-input-field"
    const val TagPrimaryTtsCta = "primary-tts-cta"
    const val TagSampleCta = "sample-cta"
    const val TagQuickActions = "quick-actions"
    const val TagStatusSummary = "status-summary"
    const val TagPetFigure = "pet-figure"
    const val TagDogMouth = "dog-mouth"

    fun statusBarDescription(
        stateLabel: String,
        inputSourceLabel: String,
        mouthStateLabel: String? = null,
        stateDescription: String? = null,
        collarDescription: String? = null,
    ) = buildString {
        append("顶部状态栏，当前状态")
        append(stateLabel)
        if (!stateDescription.isNullOrBlank()) {
            append("，")
            append(stateDescription)
        }
        append("，输入来源")
        append(inputSourceLabel)
        if (!mouthStateLabel.isNullOrBlank()) {
            append("，嘴巴状态")
            append(mouthStateLabel)
        }
        if (!collarDescription.isNullOrBlank()) {
            append("，")
            append(collarDescription)
        }
    }

    fun stageDescription(
        stateLabel: String,
        mouthLabel: String,
        inputSourceLabel: String,
        stateDescription: String? = null,
        collarDescription: String? = null,
        motionDescription: String? = null,
        motionPolicyLabel: String? = null,
    ) = buildString {
        append("数字狗")
        append(stateLabel)
        append("，嘴型")
        append(mouthLabel)
        append("，输入来源")
        append(inputSourceLabel)
        if (!stateDescription.isNullOrBlank()) {
            append("，")
            append(stateDescription)
        }
        if (!collarDescription.isNullOrBlank()) {
            append("，")
            append(collarDescription)
        }
        if (!motionDescription.isNullOrBlank()) {
            append("，动作")
            append(motionDescription)
        }
        if (!motionPolicyLabel.isNullOrBlank()) {
            append("，动态策略")
            append(motionPolicyLabel)
        }
    }

    fun stageCaption(
        poseSummary: String,
        collarRole: String,
        motionSummary: String? = null,
    ) = buildString {
        append(poseSummary)
        append(" · 项圈")
        append(collarRole)
        if (!motionSummary.isNullOrBlank()) {
            append(" · 动作")
            append(motionSummary)
        }
    }

    fun currentStateText(stateLabel: String) = "当前状态：$stateLabel"

    fun mouthStateText(mouthState: String) = "嘴巴状态：$mouthState"

    fun inputSourceText(inputSourceLabel: String) = "输入来源：$inputSourceLabel"

    fun ttsInputDescription(
        errorText: String?,
        isBusy: Boolean,
    ) = buildString {
        append("文本主输入，当前")
        append(if (isBusy) "已有讲话意图进行中" else "可编辑")
        if (errorText.isNullOrBlank()) {
            append("，未显示错误")
        } else {
            append("，错误：")
            append(errorText)
        }
    }

    fun primaryTtsCtaDescription(isBusy: Boolean) = if (isBusy) {
        "让狗狗说话，当前已有讲话意图进行中"
    } else {
        "让狗狗说话，提交 TTS 文本"
    }

    fun motionText(motionSummary: String) = "动作：$motionSummary"

    fun motionPolicyText(policyLabel: String) = "动态策略：$policyLabel"

    fun statusSummaryDescription(
        mouthStateLabel: String,
        stateLabel: String,
        inputSourceLabel: String,
        collarDescription: String? = null,
    ) = buildString {
        append("状态摘要，当前状态")
        append(stateLabel)
        append("，嘴巴状态")
        append(mouthStateLabel)
        append("，输入来源")
        append(inputSourceLabel)
        if (!collarDescription.isNullOrBlank()) {
            append("，")
            append(collarDescription)
        }
    }

    fun mouthSemanticLabel(mouth: MouthShape) = mouth.accessibleLabel

    fun mouthStateLabel(
        mouth: MouthShape,
        isSpeakingMouthOpen: Boolean = false,
    ) = when {
        isSpeakingMouthOpen -> MouthStateTalking
        mouth == MouthShape.Open -> MouthStateOpen
        else -> MouthStateClosed
    }

    fun stageMouthStateDescription(
        mouth: MouthShape,
        stateDescription: String,
        isSpeakingMouthOpen: Boolean = false,
    ): String {
        val mouthStateLabel = mouthStateLabel(mouth, isSpeakingMouthOpen)
        return when {
            isSpeakingMouthOpen -> "正在讲话，$mouthStateLabel"
            mouthStateLabel == MouthStateClosed -> stateDescription
            else -> mouthStateLabel
        }
    }

    fun stageMouthPoseSummary(
        mouth: MouthShape,
        poseSummary: String,
        isSpeakingMouthOpen: Boolean = false,
    ): String {
        val mouthStateLabel = mouthStateLabel(mouth, isSpeakingMouthOpen)
        return if (mouthStateLabel == MouthStateClosed) poseSummary else mouthStateLabel
    }
}
