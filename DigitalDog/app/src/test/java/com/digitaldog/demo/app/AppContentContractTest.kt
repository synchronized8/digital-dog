package com.digitaldog.demo.app

import com.digitaldog.demo.sharedmodel.MouthShape
import org.junit.Assert.assertEquals
import org.junit.Test

class AppContentContractTest {
    @Test
    fun rootContentContractExposesCurrentMvpFirstScreenText() {
        assertEquals("Digital Dog Demo", AppContentContract.Title)
        assertEquals("待机", AppContentContract.StatusIdle)
        assertEquals("主输入区，可以输入文字并播放示例", AppContentContract.SpeechInputDescription)
        assertEquals("演示入口，包括让狗狗说话和播放示例", AppContentContract.QuickActionsDescription)
        assertEquals("输入一句想让狗狗说的话", AppContentContract.TextInputPlaceholder)
        assertEquals("让狗狗说话", AppContentContract.PrimaryCta)
        assertEquals("讲话中", AppContentContract.PrimaryCtaBusy)
        assertEquals("先输入一句话", AppContentContract.EmptyTtsInputError)
        assertEquals("播放示例", AppContentContract.SampleAudio)
        assertEquals("状态摘要", AppContentContract.StatusSummaryTitle)
        assertEquals(
            "状态摘要，当前状态待机，嘴巴状态闭口，输入来源未选择，项圈低亮，等待输入",
            AppContentContract.StatusSummaryDescription,
        )
        assertEquals(
            "数字狗待机，嘴型闭合 closed，输入来源未选择，闭口待机，项圈低亮，等待输入，动作轻微呼吸，偶发眨眼，动态策略正常动态",
            AppContentContract.StageDescription,
        )
    }

    @Test
    fun rootContentContractExposesStableSemanticTags() {
        assertEquals("status-bar", AppContentContract.TagStatusBar)
        assertEquals("pet-stage", AppContentContract.TagPetStage)
        assertEquals("speech-input", AppContentContract.TagSpeechInput)
        assertEquals("quick-actions", AppContentContract.TagQuickActions)
        assertEquals("status-summary", AppContentContract.TagStatusSummary)
        assertEquals("tts-input-field", AppContentContract.TagTtsInputField)
        assertEquals("primary-tts-cta", AppContentContract.TagPrimaryTtsCta)
        assertEquals("sample-cta", AppContentContract.TagSampleCta)
    }

    @Test
    fun stateContentContractExposesCurrentMvpLabelsAndSemantics() {
        assertEquals(
            listOf("待机", "聆听", "思考", "讲话中", "完成", "需要处理"),
            AppContentContract.StateLabels,
        )

        assertEquals("文本", AppContentContract.InputSourceTtsLabel)
        assertEquals("输入来源：文本", AppContentContract.inputSourceText(AppContentContract.InputSourceTtsLabel))
        assertEquals("输入来源：示例", AppContentContract.inputSourceText(AppContentContract.InputSourceSampleLabel))
        assertEquals("嘴巴状态：闭口", AppContentContract.mouthStateText(AppContentContract.MouthStateClosed))
        assertEquals("嘴巴状态：张口", AppContentContract.mouthStateText(AppContentContract.MouthStateOpen))
        assertEquals("嘴巴状态：张口讲话", AppContentContract.mouthStateText(AppContentContract.MouthStateTalking))
        assertEquals("闭口", AppContentContract.mouthStateLabel(MouthShape.Closed))
        assertEquals("张口", AppContentContract.mouthStateLabel(MouthShape.Open))
        assertEquals("张口讲话", AppContentContract.mouthStateLabel(MouthShape.Closed, true))
        assertEquals("张口", AppContentContract.stageMouthStateDescription(MouthShape.Open, "闭口待机"))
        assertEquals("张口", AppContentContract.stageMouthPoseSummary(MouthShape.Open, "闭口待机"))
        assertEquals("张口讲话", AppContentContract.stageMouthPoseSummary(MouthShape.Open, "闭口待机", true))
        assertEquals(
            "顶部状态栏，当前状态讲话中，正在讲话，张口讲话，输入来源文本，嘴巴状态张口讲话，项圈珊瑚提示，讲话状态",
            AppContentContract.statusBarDescription(
                stateLabel = AppContentContract.StatusSpeaking,
                inputSourceLabel = AppContentContract.InputSourceTtsLabel,
                mouthStateLabel = AppContentContract.MouthStateTalking,
                stateDescription = "正在讲话，张口讲话",
                collarDescription = "项圈珊瑚提示，讲话状态",
            ),
        )
    }

    @Test
    fun ttsInputContractExposesCurrentMvpSemantics() {
        assertEquals(
            "文本主输入，当前可编辑，未显示错误",
            AppContentContract.ttsInputDescription(
                errorText = null,
                isBusy = false,
            ),
        )
        assertEquals(
            "文本主输入，当前可编辑，错误：先输入一句话",
            AppContentContract.ttsInputDescription(
                errorText = AppContentContract.EmptyTtsInputError,
                isBusy = false,
            ),
        )
        assertEquals(
            "让狗狗说话，当前已有讲话意图进行中",
            AppContentContract.primaryTtsCtaDescription(isBusy = true),
        )
    }

    @Test
    fun motionContentContractExposesStageMotionAndPolicySemantics() {
        assertEquals("pet-figure", AppContentContract.TagPetFigure)
        assertEquals("dog-mouth", AppContentContract.TagDogMouth)
        assertEquals(
            "数字狗讲话中，嘴型张口 open，输入来源未选择，正在讲话，张口讲话，项圈珊瑚提示，讲话状态，动作嘴型优先，低幅头身动作，动态策略正常动态",
            AppContentContract.stageDescription(
                stateLabel = "讲话中",
                mouthLabel = "张口 open",
                inputSourceLabel = "未选择",
                stateDescription = AppContentContract.stageMouthStateDescription(
                    mouth = MouthShape.Closed,
                    stateDescription = "准备讲话",
                    isSpeakingMouthOpen = true,
                ),
                collarDescription = "项圈珊瑚提示，讲话状态",
                motionDescription = "嘴型优先，低幅头身动作",
                motionPolicyLabel = "正常动态",
            ),
        )
        assertEquals("动作：减少动态，保留嘴型和状态摘要", AppContentContract.motionText("减少动态，保留嘴型和状态摘要"))
        assertEquals("动态策略：减少动态", AppContentContract.motionPolicyText("减少动态"))
    }

    @Test
    fun actionFeedbackSemanticsExposePlayfulMotionWithoutDebugUi() {
        assertEquals("动作：耳朵竖起，眼睛看向用户", AppContentContract.motionText("耳朵竖起，眼睛看向用户"))
        assertEquals("动作：完成眨眼，轻摆尾巴", AppContentContract.motionText("完成眨眼，轻摆尾巴"))
        assertEquals(
            "闭口待机 · 项圈成功绿 · 动作完成眨眼，轻摆尾巴",
            AppContentContract.stageCaption(
                poseSummary = "闭口待机",
                collarRole = "成功绿",
                motionSummary = "完成眨眼，轻摆尾巴",
            ),
        )
    }
}
