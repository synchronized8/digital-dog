package com.digitaldog.demo.app

import com.digitaldog.demo.sharedmodel.MouthShape
import org.junit.Assert.assertEquals
import org.junit.Test

class AppContentContractTest {
    @Test
    fun rootContentContractExposesStoryOneTwoFirstScreenText() {
        assertEquals("Digital Dog Demo", AppContentContract.Title)
        assertEquals("待机", AppContentContract.StatusIdle)
        assertEquals("主输入区，可以输入文字并选择音频测试入口", AppContentContract.SpeechInputDescription)
        assertEquals("多种音频入口，包括播放示例、上传音频和开始录音", AppContentContract.QuickActionsDescription)
        assertEquals("输入一句想让狗狗说的话", AppContentContract.TextInputPlaceholder)
        assertEquals("让狗狗说话", AppContentContract.PrimaryCta)
        assertEquals("讲话中", AppContentContract.PrimaryCtaBusy)
        assertEquals("先输入一句话", AppContentContract.EmptyTtsInputError)
        assertEquals("播放示例", AppContentContract.SampleAudio)
        assertEquals("上传音频", AppContentContract.UploadAudio)
        assertEquals("开始录音", AppContentContract.StartRecording)
        assertEquals("同步调试", AppContentContract.DebugTitle)
        assertEquals(
            "同步调试摘要，当前状态待机，嘴型 closed，输入来源未选择，解析质量稳定演示待开始，项圈低亮，等待输入",
            AppContentContract.DebugSummaryDescription,
        )
        assertEquals(
            "数字狗待机，嘴型闭合 closed，输入来源未选择，闭口待机，项圈低亮，等待输入，动作轻微呼吸，偶发眨眼，动态策略正常动态",
            AppContentContract.StageDescription,
        )
        assertEquals("当前嘴型：closed", AppContentContract.CurrentMouthClosed)
        assertEquals("解析质量：稳定演示待开始", AppContentContract.QualityReady)
    }

    @Test
    fun rootContentContractExposesStableSemanticTags() {
        assertEquals("status-bar", AppContentContract.TagStatusBar)
        assertEquals("pet-stage", AppContentContract.TagPetStage)
        assertEquals("speech-input", AppContentContract.TagSpeechInput)
        assertEquals("quick-actions", AppContentContract.TagQuickActions)
        assertEquals("debug-panel", AppContentContract.TagDebugPanel)
        assertEquals("debug-summary", AppContentContract.TagDebugSummary)
        assertEquals("tts-input-field", AppContentContract.TagTtsInputField)
        assertEquals("primary-tts-cta", AppContentContract.TagPrimaryTtsCta)
    }

    @Test
    fun stateContentContractExposesStoryOneThreeLabelsAndDynamicSemantics() {
        assertEquals(
            listOf("待机", "聆听", "思考", "讲话中", "完成", "需要处理"),
            AppContentContract.StateLabels,
        )
        assertEquals("聆听", AppContentContract.StatusListening)
        assertEquals("思考", AppContentContract.StatusThinking)
        assertEquals("讲话中", AppContentContract.StatusSpeaking)
        assertEquals("完成", AppContentContract.StatusDone)
        assertEquals("需要处理", AppContentContract.StatusError)

        assertEquals(
            "顶部状态栏，当前状态思考，闭口思考，输入来源未选择，项圈蓝色呼吸语义，正在思考",
            AppContentContract.statusBarDescription(
                stateLabel = "思考",
                inputSourceLabel = "未选择",
                stateDescription = "闭口思考",
                collarDescription = "项圈蓝色呼吸语义，正在思考",
            ),
        )
        assertEquals(
            "数字狗思考，嘴型闭合 closed，输入来源未选择，闭口思考，项圈蓝色呼吸语义，正在思考",
            AppContentContract.stageDescription(
                stateLabel = "思考",
                mouthLabel = "闭合 closed",
                inputSourceLabel = "未选择",
                stateDescription = "闭口思考",
                collarDescription = "项圈蓝色呼吸语义，正在思考",
            ),
        )
        assertEquals(
            "当前状态：思考",
            AppContentContract.currentStateText("思考"),
        )
        assertEquals(
            "当前嘴型：closed",
            AppContentContract.currentMouthText("closed"),
        )
        assertEquals(
            "解析质量：稳定演示待开始",
            AppContentContract.qualityText("稳定演示待开始"),
        )
        assertEquals(
            "同步调试完整占位，当前嘴型 closed，当前状态思考，输入来源未选择，解析质量稳定演示待开始，项圈蓝色呼吸语义，正在思考",
            AppContentContract.debugPanelDescription(
                mouthId = "closed",
                stateLabel = "思考",
                inputSourceLabel = "未选择",
                qualityLabel = "稳定演示待开始",
                collarDescription = "项圈蓝色呼吸语义，正在思考",
            ),
        )
        assertEquals(
            "同步调试摘要，当前状态思考，嘴型 closed，输入来源未选择，解析质量稳定演示待开始，项圈蓝色呼吸语义，正在思考",
            AppContentContract.debugSummaryDescription(
                mouthId = "closed",
                stateLabel = "思考",
                inputSourceLabel = "未选择",
                qualityLabel = "稳定演示待开始",
                collarDescription = "项圈蓝色呼吸语义，正在思考",
            ),
        )
    }

    @Test
    fun mouthContentContractExposesManualTestLabelsAndSemantics() {
        assertEquals("手动嘴型测试", AppContentContract.ManualMouthTitle)
        assertEquals("重置待机", AppContentContract.ResetManualMouth)
        assertEquals("manual-mouth-test", AppContentContract.TagManualMouthTest)
        assertEquals("dog-mouth", AppContentContract.TagDogMouth)

        assertEquals(
            listOf(
                "closed 闭合",
                "small 小开口",
                "wide 大开口",
                "round 圆嘴",
                "smile 咧嘴",
                "teeth 露齿",
                "pant 喘气",
            ),
            MouthShape.entries.map { AppContentContract.manualMouthButtonText(it) },
        )
        assertEquals("当前嘴型：wide", AppContentContract.currentMouthText("wide"))
        assertEquals("大开口 wide", AppContentContract.mouthSemanticLabel(MouthShape.Wide))
        assertEquals(
            "手动嘴型测试，当前嘴型大开口 wide，输入来源手动测试",
            AppContentContract.manualMouthTestDescription(
                currentMouth = MouthShape.Wide,
                inputSourceLabel = "手动测试",
            ),
        )
        assertEquals(
            "手动测试嘴型大开口 wide",
            AppContentContract.stageMouthStateDescription(
                mouth = MouthShape.Wide,
                stateDescription = "闭口待机",
            ),
        )
        assertEquals(
            "嘴型选项 wide 大开口，已选中",
            AppContentContract.manualMouthOptionDescription(
                mouth = MouthShape.Wide,
                selected = true,
            ),
        )
    }

    @Test
    fun ttsInputContractExposesStoryTwoOneSemantics() {
        assertEquals("TTS", AppContentContract.InputSourceTtsLabel)
        assertEquals("输入来源：TTS", AppContentContract.inputSourceText(AppContentContract.InputSourceTtsLabel))
        assertEquals(
            "TTS 主输入，当前可编辑，未显示错误",
            AppContentContract.ttsInputDescription(
                errorText = null,
                isBusy = false,
            ),
        )
        assertEquals(
            "TTS 主输入，当前可编辑，错误：先输入一句话",
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
        assertEquals(
            "数字狗讲话中，嘴型闭合 closed，输入来源未选择，准备讲话，项圈珊瑚提示，讲话状态，动作嘴型优先，低幅头身动作，动态策略正常动态",
            AppContentContract.stageDescription(
                stateLabel = "讲话中",
                mouthLabel = "闭合 closed",
                inputSourceLabel = "未选择",
                stateDescription = "准备讲话",
                collarDescription = "项圈珊瑚提示，讲话状态",
                motionDescription = "嘴型优先，低幅头身动作",
                motionPolicyLabel = "正常动态",
            ),
        )
        assertEquals(
            "准备讲话状态 · 项圈珊瑚 · 动作嘴型优先，低幅头身动作",
            AppContentContract.stageCaption(
                poseSummary = "准备讲话状态",
                collarRole = "珊瑚",
                motionSummary = "嘴型优先，低幅头身动作",
            ),
        )
        assertEquals("动作：减少动态，保留嘴型和状态摘要", AppContentContract.motionText("减少动态，保留嘴型和状态摘要"))
        assertEquals("动态策略：减少动态", AppContentContract.motionPolicyText("减少动态"))
    }
}
