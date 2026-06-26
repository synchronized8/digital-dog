package com.digitaldog.demo.statussummary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.digitaldog.demo.app.AppContentContract
import com.digitaldog.demo.designsystem.DogColors
import com.digitaldog.demo.designsystem.DogShape
import com.digitaldog.demo.designsystem.DogSpacing
import com.digitaldog.demo.designsystem.DogTypography
import com.digitaldog.demo.sharedmodel.PetState
import com.digitaldog.demo.state.SpeechDemoState
import com.digitaldog.demo.state.toPresentation

@Composable
fun StatusSummaryPlaceholder(
    modifier: Modifier = Modifier,
    uiState: SpeechDemoState = SpeechDemoState(),
) {
    val presentation = uiState.toPresentation()
    val stateText = AppContentContract.currentStateText(presentation.stateLabel)
    val mouthText = AppContentContract.mouthStateText(uiState.mouthStateLabel())
    val inputSourceText = AppContentContract.inputSourceText(uiState.inputSource.label)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp)
            .background(DogColors.Surface, DogShape.Panel)
            .border(1.dp, DogColors.Border, DogShape.Panel)
            .padding(DogSpacing.Md)
            .testTag(AppContentContract.TagStatusSummary)
            .semantics {
                contentDescription = AppContentContract.statusSummaryDescription(
                    mouthStateLabel = uiState.mouthStateLabel(),
                    stateLabel = presentation.stateLabel,
                    inputSourceLabel = uiState.inputSource.label,
                    collarDescription = presentation.collar.description,
                )
            },
        verticalArrangement = Arrangement.spacedBy(DogSpacing.Sm),
    ) {
        Text(
            text = AppContentContract.StatusSummaryTitle,
            color = DogColors.TextPrimary,
            fontSize = DogTypography.PanelTitle,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stateText,
            color = presentation.collar.color,
            fontSize = DogTypography.DebugValue,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = mouthText,
            color = DogColors.Coral,
            fontSize = DogTypography.DebugValue,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = inputSourceText,
            color = DogColors.TextSecondary,
            fontSize = DogTypography.DebugValue,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun SpeechDemoState.mouthStateLabel(): String = AppContentContract.mouthStateLabel(
    mouth = currentMouth,
    isSpeakingMouthOpen = speechAnimationState.mouthOpen || petState == PetState.Speaking,
)
