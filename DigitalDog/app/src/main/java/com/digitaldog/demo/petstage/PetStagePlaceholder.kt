package com.digitaldog.demo.petstage

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.digitaldog.demo.accessibility.ReduceMotionPolicy
import com.digitaldog.demo.app.AppContentContract
import com.digitaldog.demo.designsystem.DogColors
import com.digitaldog.demo.designsystem.DogShape
import com.digitaldog.demo.designsystem.DogSpacing
import com.digitaldog.demo.designsystem.DogTypography
import com.digitaldog.demo.dogrenderer.DogMouthRenderer
import com.digitaldog.demo.dogrenderer.DogMotionProfile
import com.digitaldog.demo.sharedmodel.MouthShape
import com.digitaldog.demo.state.SpeechDemoState
import com.digitaldog.demo.state.toPresentation

@Composable
fun PetStagePlaceholder(
    modifier: Modifier = Modifier,
    uiState: SpeechDemoState = SpeechDemoState(),
    motionPolicy: ReduceMotionPolicy = ReduceMotionPolicy.Normal,
) {
    val presentation = uiState.toPresentation()
    val motionProfile = motionPolicy.applyTo(DogMotionProfile.forState(uiState.petState))

    Box(
        modifier = modifier
            .heightIn(min = 320.dp)
            .background(DogColors.SurfaceAlt, DogShape.Panel)
            .border(1.dp, DogColors.Border, DogShape.Panel)
            .testTag(AppContentContract.TagPetStage)
            .semantics {
                contentDescription = AppContentContract.stageDescription(
                    stateLabel = presentation.stateLabel,
                    mouthLabel = AppContentContract.mouthSemanticLabel(uiState.currentMouth),
                    inputSourceLabel = uiState.inputSource.label,
                    stateDescription = AppContentContract.stageMouthStateDescription(
                        mouth = uiState.currentMouth,
                        stateDescription = presentation.stateDescription,
                    ),
                    collarDescription = presentation.collar.description,
                    motionDescription = motionProfile.summary,
                    motionPolicyLabel = motionPolicy.label,
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = DogSpacing.Lg)
                .fillMaxWidth(0.78f)
                .height(28.dp)
                .background(Color(0xFFE2EEE9), CircleShape),
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(DogSpacing.Lg),
        ) {
            DigitalDogStaticFigure(
                collarColor = presentation.collar.color,
                mouthShape = uiState.currentMouth,
                motionProfile = motionProfile,
                motionPolicy = motionPolicy,
            )
            Spacer(modifier = Modifier.height(DogSpacing.Md))
            Text(
                text = AppContentContract.StageTitle,
                modifier = Modifier.clearAndSetSemantics {},
                color = DogColors.TextPrimary,
                fontSize = DogTypography.PanelTitle,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = AppContentContract.stageCaption(
                    poseSummary = AppContentContract.stageMouthPoseSummary(
                        mouth = uiState.currentMouth,
                        poseSummary = presentation.poseSummary,
                    ),
                    collarRole = presentation.collar.role,
                    motionSummary = motionProfile.summary,
                ),
                modifier = Modifier.clearAndSetSemantics {},
                color = DogColors.TextSecondary,
                fontSize = DogTypography.Label,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun DigitalDogStaticFigure(
    collarColor: Color,
    mouthShape: MouthShape,
    motionProfile: DogMotionProfile,
    motionPolicy: ReduceMotionPolicy,
) {
    val transition = rememberInfiniteTransition(label = "dog-loop-motion")
    val animationSpec = if (motionPolicy.reducesDecorativeMotion) {
        snap<Float>()
    } else {
        tween(durationMillis = 260)
    }
    val dpAnimationSpec = if (motionPolicy.reducesDecorativeMotion) {
        snap<androidx.compose.ui.unit.Dp>()
    } else {
        tween(durationMillis = 260)
    }
    val bodyScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = animationSpec,
        label = "dog-body-scale",
    )
    val breathingScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = when {
            motionProfile.breathing -> 1.018f
            motionProfile.speakingPulse -> 1.025f
            else -> 1f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (motionProfile.speakingPulse) 700 else 3200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dog-breathing-scale",
    )
    val headTilt by animateFloatAsState(
        targetValue = motionProfile.headTiltDegrees,
        animationSpec = animationSpec,
        label = "dog-head-tilt",
    )
    val leftEarTilt by animateFloatAsState(
        targetValue = if (motionProfile.earLift) -34f else -24f,
        animationSpec = animationSpec,
        label = "dog-left-ear-tilt",
    )
    val rightEarTilt by animateFloatAsState(
        targetValue = if (motionProfile.earLift) 34f else 24f,
        animationSpec = animationSpec,
        label = "dog-right-ear-tilt",
    )
    val earLiftOffset by animateDpAsState(
        targetValue = if (motionProfile.earLift) (-6).dp else 0.dp,
        animationSpec = dpAnimationSpec,
        label = "dog-ear-lift",
    )
    val eyeHeight by animateDpAsState(
        targetValue = if (motionProfile.blink) 10.dp else 18.dp,
        animationSpec = dpAnimationSpec,
        label = "dog-eye-height",
    )
    val blinkEyeHeight by transition.animateFloat(
        initialValue = 18f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3600
                18f at 0
                18f at 3000
                8f at 3060
                8f at 3140
                18f at 3200
                18f at 3600
            },
        ),
        label = "dog-blink-height",
    )
    val leftEyeOffset by animateDpAsState(
        targetValue = if (motionProfile.eyeFocus) (-34).dp else (-38).dp,
        animationSpec = dpAnimationSpec,
        label = "dog-left-eye-focus",
    )
    val rightEyeOffset by animateDpAsState(
        targetValue = if (motionProfile.eyeFocus) 34.dp else 38.dp,
        animationSpec = dpAnimationSpec,
        label = "dog-right-eye-focus",
    )
    val tailOffset by animateDpAsState(
        targetValue = 82.dp,
        animationSpec = dpAnimationSpec,
        label = "dog-tail-wag",
    )
    val wagOffset by transition.animateFloat(
        initialValue = 82f,
        targetValue = if (motionProfile.tailWag) 96f else 82f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dog-tail-wag-loop",
    )
    val wagRotation by transition.animateFloat(
        initialValue = 12f,
        targetValue = if (motionProfile.tailWag) 28f else 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dog-tail-rotation-loop",
    )
    val resolvedBodyScale = if (motionPolicy.reducesDecorativeMotion) bodyScale else breathingScale
    val resolvedEyeHeight = when {
        motionPolicy.reducesDecorativeMotion -> eyeHeight
        motionProfile.blink -> blinkEyeHeight.dp
        else -> eyeHeight
    }
    val resolvedTailOffset = if (motionPolicy.reducesDecorativeMotion) tailOffset else wagOffset.dp
    val resolvedTailRotation = if (motionPolicy.reducesDecorativeMotion) {
        if (motionProfile.tailWag) 24f else 12f
    } else {
        wagRotation
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .testTag(AppContentContract.TagPetFigure),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(width = 150.dp, height = 112.dp)
                .scale(resolvedBodyScale)
                .clearAndSetSemantics {}
                .background(Color(0xFFFFF7F2), CircleShape)
                .border(2.dp, DogColors.Border, CircleShape),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(x = resolvedTailOffset, y = (-86).dp)
                .rotate(resolvedTailRotation)
                .size(width = 42.dp, height = 14.dp)
                .clearAndSetSemantics {}
                .background(DogColors.PetWarm, CircleShape),
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 34.dp)
                .rotate(headTilt)
                .size(width = 176.dp, height = 154.dp)
                .clearAndSetSemantics {}
                .background(Color(0xFFFFFBF7), CircleShape)
                .border(2.dp, DogColors.Border, CircleShape),
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = (-72).dp, y = 40.dp + earLiftOffset)
                .rotate(leftEarTilt)
                .size(width = 48.dp, height = 92.dp)
                .clearAndSetSemantics {}
                .background(DogColors.PetWarm, CircleShape),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = 72.dp, y = 40.dp + earLiftOffset)
                .rotate(rightEarTilt)
                .size(width = 48.dp, height = 92.dp)
                .clearAndSetSemantics {}
                .background(DogColors.PetWarm, CircleShape),
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = leftEyeOffset, y = 98.dp)
                .size(width = 18.dp, height = if (motionProfile.confusedEyes) 12.dp else resolvedEyeHeight)
                .rotate(if (motionProfile.confusedEyes) -12f else 0f)
                .clearAndSetSemantics {}
                .background(DogColors.TextPrimary, CircleShape),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = rightEyeOffset, y = 98.dp)
                .size(width = 18.dp, height = if (motionProfile.confusedEyes) 12.dp else resolvedEyeHeight)
                .rotate(if (motionProfile.confusedEyes) 12f else 0f)
                .clearAndSetSemantics {}
                .background(DogColors.TextPrimary, CircleShape),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 128.dp)
                .size(width = 34.dp, height = 24.dp)
                .clearAndSetSemantics {}
                .background(Color(0xFF36413F), CircleShape),
        )
        DogMouthRenderer(
            mouthShape = mouthShape,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 148.dp)
                .testTag(AppContentContract.TagDogMouth),
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-42).dp)
                .size(width = 132.dp, height = 18.dp)
                .clearAndSetSemantics {}
                .background(collarColor, CircleShape),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-42).dp)
                .size(18.dp)
                .clearAndSetSemantics {}
                .background(collarColor, CircleShape)
                .border(2.dp, Color.White, CircleShape),
        )
    }
}
