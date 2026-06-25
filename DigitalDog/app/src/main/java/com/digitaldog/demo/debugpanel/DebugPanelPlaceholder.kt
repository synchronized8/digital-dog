package com.digitaldog.demo.debugpanel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.digitaldog.demo.app.AppContentContract
import com.digitaldog.demo.designsystem.DogColors
import com.digitaldog.demo.designsystem.DogShape
import com.digitaldog.demo.designsystem.DogSpacing
import com.digitaldog.demo.designsystem.DogTypography
import com.digitaldog.demo.sharedmodel.MouthShape
import com.digitaldog.demo.state.SpeechDemoState
import com.digitaldog.demo.state.toPresentation

@Composable
fun DebugPanelPlaceholder(
    modifier: Modifier = Modifier,
    uiState: SpeechDemoState = SpeechDemoState(),
    onMouthSelected: (MouthShape) -> Unit = {},
    onResetMouth: () -> Unit = {},
) {
    val presentation = uiState.toPresentation()
    val mouthText = AppContentContract.currentMouthText(uiState.currentMouth.stableId)
    val stateText = AppContentContract.currentStateText(presentation.stateLabel)
    val inputSourceText = AppContentContract.inputSourceText(uiState.inputSource.label)
    val qualityText = AppContentContract.qualityText(uiState.timelineQuality.label)

    Column(
        modifier = modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .background(DogColors.Surface, DogShape.Panel)
            .border(1.dp, DogColors.Border, DogShape.Panel)
            .padding(DogSpacing.Md)
            .testTag(AppContentContract.TagDebugPanel)
            .semantics {
                contentDescription = AppContentContract.debugPanelDescription(
                    mouthId = uiState.currentMouth.stableId,
                    stateLabel = presentation.stateLabel,
                    inputSourceLabel = uiState.inputSource.label,
                    qualityLabel = uiState.timelineQuality.label,
                    collarDescription = presentation.collar.description,
                )
            },
        verticalArrangement = Arrangement.spacedBy(DogSpacing.Md),
    ) {
        Text(
            text = AppContentContract.DebugTitle,
            color = DogColors.TextPrimary,
            fontSize = DogTypography.PanelTitle,
            fontWeight = FontWeight.Bold,
        )
        DebugValue(mouthText, DogColors.Coral)
        DebugValue(stateText, presentation.collar.color)
        DebugValue(inputSourceText, DogColors.TextSecondary)
        DebugValue(qualityText, DogColors.SuccessGreen)
        ManualMouthTestControls(
            currentMouth = uiState.currentMouth,
            inputSourceLabel = uiState.inputSource.label,
            onMouthSelected = onMouthSelected,
            onResetMouth = onResetMouth,
        )
    }
}

@Composable
fun DebugSummaryPlaceholder(
    modifier: Modifier = Modifier,
    uiState: SpeechDemoState = SpeechDemoState(),
    onMouthSelected: (MouthShape) -> Unit = {},
    onResetMouth: () -> Unit = {},
) {
    val presentation = uiState.toPresentation()
    val mouthText = AppContentContract.currentMouthText(uiState.currentMouth.stableId)
    val stateText = AppContentContract.currentStateText(presentation.stateLabel)
    val inputSourceText = AppContentContract.inputSourceText(uiState.inputSource.label)
    val qualityText = AppContentContract.qualityText(uiState.timelineQuality.label)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 112.dp)
            .background(DogColors.Surface, DogShape.Panel)
            .border(1.dp, DogColors.Border, DogShape.Panel)
            .padding(DogSpacing.Md)
            .testTag(AppContentContract.TagDebugSummary)
            .semantics {
                contentDescription = AppContentContract.debugSummaryDescription(
                    mouthId = uiState.currentMouth.stableId,
                    stateLabel = presentation.stateLabel,
                    inputSourceLabel = uiState.inputSource.label,
                    qualityLabel = uiState.timelineQuality.label,
                    collarDescription = presentation.collar.description,
                )
            },
        verticalArrangement = Arrangement.spacedBy(DogSpacing.Sm),
    ) {
        Text(
            text = AppContentContract.DebugSummaryTitle,
            color = DogColors.TextPrimary,
            fontSize = DogTypography.PanelTitle,
            fontWeight = FontWeight.Bold,
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(DogSpacing.Xs),
        ) {
            DebugPill(
                text = stateText,
                color = presentation.collar.color,
                modifier = Modifier.fillMaxWidth(),
            )
            DebugPill(
                text = mouthText,
                color = DogColors.Coral,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Text(
            text = inputSourceText,
            color = DogColors.TextSecondary,
            fontSize = DogTypography.DebugValue,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = qualityText,
            color = DogColors.TextSecondary,
            fontSize = DogTypography.DebugValue,
            fontWeight = FontWeight.SemiBold,
        )
        ManualMouthTestControls(
            currentMouth = uiState.currentMouth,
            inputSourceLabel = uiState.inputSource.label,
            onMouthSelected = onMouthSelected,
            onResetMouth = onResetMouth,
        )
    }
}

@Composable
private fun ManualMouthTestControls(
    currentMouth: MouthShape,
    inputSourceLabel: String,
    onMouthSelected: (MouthShape) -> Unit,
    onResetMouth: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(AppContentContract.TagManualMouthTest)
            .semantics {
                contentDescription = AppContentContract.manualMouthTestDescription(
                    currentMouth = currentMouth,
                    inputSourceLabel = inputSourceLabel,
                )
            },
        verticalArrangement = Arrangement.spacedBy(DogSpacing.Sm),
    ) {
        Text(
            text = AppContentContract.ManualMouthTitle,
            color = DogColors.TextPrimary,
            fontSize = DogTypography.PanelTitle,
            fontWeight = FontWeight.Bold,
        )
        MouthShape.entries.chunked(2).forEach { rowShapes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DogSpacing.Sm),
            ) {
                rowShapes.forEach { mouth ->
                    MouthOptionButton(
                        mouth = mouth,
                        selected = mouth == currentMouth,
                        onClick = { onMouthSelected(mouth) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowShapes.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
        OutlinedButton(
            onClick = onResetMouth,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = DogSpacing.TouchTarget),
            shape = DogShape.Button,
        ) {
            Text(
                text = AppContentContract.ResetManualMouth,
                color = DogColors.TextPrimary,
                fontSize = DogTypography.Button,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun MouthOptionButton(
    mouth: MouthShape,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = DogSpacing.TouchTarget)
            .semantics {
                contentDescription = AppContentContract.manualMouthOptionDescription(
                    mouth = mouth,
                    selected = selected,
                )
            },
        shape = DogShape.Button,
    ) {
        Text(
            text = AppContentContract.manualMouthButtonText(mouth),
            color = if (selected) DogColors.Coral else DogColors.TextPrimary,
            fontSize = DogTypography.Button,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DebugValue(
    text: String,
    color: androidx.compose.ui.graphics.Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DogSpacing.Sm),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape),
        )
        Text(
            text = text,
            color = DogColors.TextPrimary,
            fontSize = DogTypography.DebugValue,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun DebugPill(
    text: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(DogColors.SurfaceAlt, DogShape.Panel)
            .border(1.dp, DogColors.Border, DogShape.Panel)
            .padding(horizontal = DogSpacing.Sm, vertical = DogSpacing.Xs),
        horizontalArrangement = Arrangement.spacedBy(DogSpacing.Xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape),
        )
        Text(
            text = text,
            color = DogColors.TextPrimary,
            fontSize = DogTypography.Label,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
