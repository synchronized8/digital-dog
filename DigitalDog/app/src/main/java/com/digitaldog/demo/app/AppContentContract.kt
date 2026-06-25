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
    const val InputSourceTtsLabel = "TTS"
    const val InputSourceNone = "输入来源：未选择"
    const val SpeechInputDescription = "主输入区，可以输入文字并选择音频测试入口"
    const val QuickActionsDescription = "多种音频入口，包括播放示例、上传音频和开始录音"
    const val TextInputPlaceholder = "输入一句想让狗狗说的话"
    const val PrimaryCta = "让狗狗说话"
    const val PrimaryCtaBusy = "讲话中"
    const val EmptyTtsInputError = "先输入一句话"
    const val SampleAudio = "播放示例"
    const val UploadAudio = "上传音频"
    const val StartRecording = "开始录音"
    const val Experimental = "实验"
    const val DebugTitle = "同步调试"
    const val DebugSummaryTitle = "同步调试摘要"
    const val MouthClosedLabel = "闭合"
    const val CurrentMouthClosed = "当前嘴型：closed"
    const val CurrentStateIdle = "当前状态：待机"
    const val QualityReadyLabel = "稳定演示待开始"
    const val QualityReady = "解析质量：稳定演示待开始"
    const val MainInputTitle = "主输入"
    const val QuickEntryTitle = "多种音频入口"
    const val ManualMouthTitle = "手动嘴型测试"
    const val ResetManualMouth = "重置待机"

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
    val DebugSummaryDescription = debugSummaryDescription(
        mouthId = "closed",
        stateLabel = StatusIdle,
        inputSourceLabel = InputSourceNoneLabel,
        qualityLabel = QualityReadyLabel,
        collarDescription = "项圈低亮，等待输入",
    )

    const val TagStatusBar = "status-bar"
    const val TagPetStage = "pet-stage"
    const val TagSpeechInput = "speech-input"
    const val TagTtsInputField = "tts-input-field"
    const val TagPrimaryTtsCta = "primary-tts-cta"
    const val TagQuickActions = "quick-actions"
    const val TagDebugPanel = "debug-panel"
    const val TagDebugSummary = "debug-summary"
    const val TagManualMouthTest = "manual-mouth-test"
    const val TagPetFigure = "pet-figure"
    const val TagDogMouth = "dog-mouth"

    fun statusBarDescription(
        stateLabel: String,
        inputSourceLabel: String,
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

    fun currentMouthText(mouthId: String) = "当前嘴型：$mouthId"

    fun inputSourceText(inputSourceLabel: String) = "输入来源：$inputSourceLabel"

    fun qualityText(qualityLabel: String) = "解析质量：$qualityLabel"

    fun ttsInputDescription(
        errorText: String?,
        isBusy: Boolean,
    ) = buildString {
        append("TTS 主输入，当前")
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

    fun debugPanelDescription(
        mouthId: String,
        stateLabel: String,
        inputSourceLabel: String,
        qualityLabel: String,
        collarDescription: String? = null,
    ) = buildString {
        append("同步调试完整占位，当前嘴型 ")
        append(mouthId)
        append("，当前状态")
        append(stateLabel)
        append("，输入来源")
        append(inputSourceLabel)
        append("，解析质量")
        append(qualityLabel)
        if (!collarDescription.isNullOrBlank()) {
            append("，")
            append(collarDescription)
        }
    }

    fun debugSummaryDescription(
        mouthId: String,
        stateLabel: String,
        inputSourceLabel: String,
        qualityLabel: String,
        collarDescription: String? = null,
    ) = buildString {
        append("同步调试摘要，当前状态")
        append(stateLabel)
        append("，嘴型 ")
        append(mouthId)
        append("，输入来源")
        append(inputSourceLabel)
        append("，解析质量")
        append(qualityLabel)
        if (!collarDescription.isNullOrBlank()) {
            append("，")
            append(collarDescription)
        }
    }

    fun mouthSemanticLabel(mouth: MouthShape) = mouth.accessibleLabel

    fun stageMouthStateDescription(
        mouth: MouthShape,
        stateDescription: String,
    ) = if (mouth == MouthShape.Closed) {
        stateDescription
    } else {
        "手动测试嘴型${mouth.label} ${mouth.stableId}"
    }

    fun stageMouthPoseSummary(
        mouth: MouthShape,
        poseSummary: String,
    ) = if (mouth == MouthShape.Closed) {
        poseSummary
    } else {
        "手动嘴型${mouth.label}"
    }

    fun manualMouthButtonText(mouth: MouthShape) = "${mouth.stableId} ${mouth.label}"

    fun manualMouthTestDescription(
        currentMouth: MouthShape,
        inputSourceLabel: String,
    ) = "手动嘴型测试，当前嘴型${mouthSemanticLabel(currentMouth)}，输入来源$inputSourceLabel"

    fun manualMouthOptionDescription(
        mouth: MouthShape,
        selected: Boolean,
    ) = buildString {
        append("嘴型选项 ")
        append(mouth.stableId)
        append(" ")
        append(mouth.label)
        if (selected) {
            append("，已选中")
        }
    }
}
