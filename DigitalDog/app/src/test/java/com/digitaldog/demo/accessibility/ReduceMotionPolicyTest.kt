package com.digitaldog.demo.accessibility

import com.digitaldog.demo.dogrenderer.DogMotionProfile
import com.digitaldog.demo.sharedmodel.PetState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ReduceMotionPolicyTest {
    @Test
    fun normalPolicyKeepsOriginalProfile() {
        val idle = DogMotionProfile.forState(PetState.Idle)

        assertSame(idle, ReduceMotionPolicy.Normal.applyTo(idle))
        assertEquals("正常动态", ReduceMotionPolicy.Normal.label)
        assertFalse(ReduceMotionPolicy.Normal.reducesDecorativeMotion)
    }

    @Test
    fun reducedPolicyHasStableUserFacingLabelAndFreezesDecorativeMotion() {
        val error = DogMotionProfile.forState(PetState.Error)

        val reduced = ReduceMotionPolicy.Reduced.applyTo(error)

        assertEquals("减少动态", ReduceMotionPolicy.Reduced.label)
        assertTrue(ReduceMotionPolicy.Reduced.reducesDecorativeMotion)
        assertEquals(0f, reduced.headTiltDegrees)
        assertFalse(reduced.confusedEyes)
        assertTrue(reduced.mouthRemainsObservable)
    }

    @Test
    fun fromAnimatorScaleTreatsZeroAsReducedMotion() {
        assertEquals(ReduceMotionPolicy.Reduced, ReduceMotionPolicy.fromAnimatorScale(0f))
        assertEquals(ReduceMotionPolicy.Reduced, ReduceMotionPolicy.fromAnimatorScale(0.5f))
        assertEquals(ReduceMotionPolicy.Normal, ReduceMotionPolicy.fromAnimatorScale(1f))
    }
}
