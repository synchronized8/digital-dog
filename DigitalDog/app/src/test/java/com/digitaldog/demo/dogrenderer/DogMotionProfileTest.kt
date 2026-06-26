package com.digitaldog.demo.dogrenderer

import com.digitaldog.demo.accessibility.ReduceMotionPolicy
import com.digitaldog.demo.sharedmodel.PetState
import com.digitaldog.demo.state.DogActionCue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DogMotionProfileTest {
    @Test
    fun mapsEveryPetStateToMotionProfile() {
        val profiles = PetState.entries.associateWith { DogMotionProfile.forState(it) }

        assertEquals("轻微呼吸，偶发眨眼", profiles.getValue(PetState.Idle).summary)
        assertTrue(profiles.getValue(PetState.Idle).breathing)
        assertTrue(profiles.getValue(PetState.Idle).blink)

        assertEquals("耳朵竖起，眼睛看向用户", profiles.getValue(PetState.Listening).summary)
        assertTrue(profiles.getValue(PetState.Listening).earLift)
        assertTrue(profiles.getValue(PetState.Listening).eyeFocus)

        assertEquals("闭口思考，轻微歪头", profiles.getValue(PetState.Thinking).summary)
        assertEquals(-3f, profiles.getValue(PetState.Thinking).headTiltDegrees)

        assertEquals("嘴型优先，低幅头身动作", profiles.getValue(PetState.Speaking).summary)
        assertTrue(profiles.getValue(PetState.Speaking).speakingPulse)
        assertFalse(profiles.getValue(PetState.Speaking).mouthCanBeCovered)

        assertEquals("完成眨眼，轻摆尾巴", profiles.getValue(PetState.Done).summary)
        assertTrue(profiles.getValue(PetState.Done).blink)
        assertTrue(profiles.getValue(PetState.Done).tailWag)

        assertEquals("困惑歪头，提示黄项圈", profiles.getValue(PetState.Error).summary)
        assertEquals(4f, profiles.getValue(PetState.Error).headTiltDegrees)
        assertTrue(profiles.getValue(PetState.Error).confusedEyes)
    }

    @Test
    fun reducedMotionKeepsMouthObservableAndDisablesDecorativeMotion() {
        val speaking = DogMotionProfile.forState(PetState.Speaking)

        val reduced = ReduceMotionPolicy.Reduced.applyTo(speaking)

        assertFalse(reduced.breathing)
        assertFalse(reduced.blink)
        assertFalse(reduced.earLift)
        assertFalse(reduced.tailWag)
        assertFalse(reduced.speakingPulse)
        assertEquals(0f, reduced.headTiltDegrees)
        assertFalse(reduced.mouthCanBeCovered)
        assertTrue(reduced.mouthRemainsObservable)
        assertEquals("减少动态，保留嘴型和状态摘要", reduced.summary)
    }

    @Test
    fun staticPolicyFreezesDecorativeMotionForStableComposeTests() {
        val done = DogMotionProfile.forState(PetState.Done)

        val staticProfile = ReduceMotionPolicy.Static.applyTo(done)

        assertFalse(staticProfile.blink)
        assertFalse(staticProfile.tailWag)
        assertTrue(staticProfile.mouthRemainsObservable)
        assertEquals("静态测试模式，保留嘴型和状态摘要", staticProfile.summary)
    }

    @Test
    fun actionCuesDrivePlayfulMotionWithoutCoveringMouth() {
        val preparing = DogMotionProfile.forState(PetState.Thinking, DogActionCue.EarPerk)
        val speaking = DogMotionProfile.forState(PetState.Speaking, DogActionCue.BodyBounce)
        val tailFeedback = DogMotionProfile.forState(PetState.Done, DogActionCue.TailWag)
        val blinkFeedback = DogMotionProfile.forState(PetState.Done, DogActionCue.Blink)
        val errorTilt = DogMotionProfile.forState(PetState.Error, DogActionCue.HeadTilt)

        assertEquals("耳朵竖起，眼睛看向用户", preparing.summary)
        assertTrue(preparing.earLift)
        assertTrue(preparing.eyeFocus)
        assertFalse(preparing.mouthCanBeCovered)
        assertTrue(preparing.mouthRemainsObservable)

        assertEquals("嘴型优先，低幅头身动作", speaking.summary)
        assertTrue(speaking.speakingPulse)
        assertFalse(speaking.mouthCanBeCovered)
        assertTrue(speaking.mouthRemainsObservable)

        assertEquals("完成眨眼，轻摆尾巴", tailFeedback.summary)
        assertTrue(tailFeedback.blink)
        assertTrue(tailFeedback.tailWag)
        assertFalse(tailFeedback.mouthCanBeCovered)
        assertTrue(tailFeedback.mouthRemainsObservable)

        assertEquals("完成眨眼", blinkFeedback.summary)
        assertTrue(blinkFeedback.blink)
        assertFalse(blinkFeedback.tailWag)
        assertTrue(blinkFeedback.mouthRemainsObservable)

        assertEquals("困惑歪头，提示黄项圈", errorTilt.summary)
        assertTrue(errorTilt.confusedEyes)
        assertEquals(4f, errorTilt.headTiltDegrees)
        assertTrue(errorTilt.mouthRemainsObservable)
    }

    @Test
    fun reducedMotionFreezesActionCueDecorationButKeepsMouthObservable() {
        val tailFeedback = DogMotionProfile.forState(PetState.Done, DogActionCue.TailWag)

        val reduced = ReduceMotionPolicy.Reduced.applyTo(tailFeedback)

        assertFalse(reduced.blink)
        assertFalse(reduced.tailWag)
        assertFalse(reduced.earLift)
        assertFalse(reduced.speakingPulse)
        assertFalse(reduced.mouthCanBeCovered)
        assertTrue(reduced.mouthRemainsObservable)
        assertEquals("减少动态，保留嘴型和状态摘要", reduced.summary)
    }
}
