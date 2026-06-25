package com.digitaldog.demo.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.digitaldog.demo.accessibility.ReduceMotionPolicy
import com.digitaldog.demo.debugpanel.DebugPanelPlaceholder
import com.digitaldog.demo.debugpanel.DebugSummaryPlaceholder
import com.digitaldog.demo.designsystem.DigitalDogTheme
import com.digitaldog.demo.designsystem.DogColors
import com.digitaldog.demo.designsystem.DogShape
import com.digitaldog.demo.designsystem.DogSpacing
import com.digitaldog.demo.designsystem.DogTypography
import com.digitaldog.demo.petstage.PetStagePlaceholder
import com.digitaldog.demo.sharedmodel.MouthShape
import com.digitaldog.demo.speechinput.SpeechInputPlaceholder
import com.digitaldog.demo.state.ManualMouthTestReducer
import com.digitaldog.demo.state.PetStatePresentation
import com.digitaldog.demo.state.SpeechDemoState
import com.digitaldog.demo.state.TtsSubmitReducer
import com.digitaldog.demo.state.toPresentation

enum class AppLayoutMode {
    Landscape,
    Portrait,
}

private val LandscapeMinWidth = 840.dp
private val LandscapeMinHeight = 680.dp

@Composable
fun DigitalDogApp(
    modifier: Modifier = Modifier,
    layoutMode: AppLayoutMode? = null,
    uiState: SpeechDemoState = SpeechDemoState(),
    motionPolicy: ReduceMotionPolicy = ReduceMotionPolicy.Normal,
) {
    DigitalDogTheme {
        var currentUiState by remember(uiState) {
            mutableStateOf(uiState)
        }

        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(DogSpacing.Lg),
            ) {
                val canUseLandscapeLayout =
                    maxWidth >= LandscapeMinWidth && maxHeight >= LandscapeMinHeight
                val requestedLayout = layoutMode ?: if (
                    maxWidth >= maxHeight &&
                    canUseLandscapeLayout
                ) {
                    AppLayoutMode.Landscape
                } else {
                    AppLayoutMode.Portrait
                }
                val resolvedLayout = if (
                    requestedLayout == AppLayoutMode.Landscape &&
                    !canUseLandscapeLayout
                ) {
                    AppLayoutMode.Portrait
                } else {
                    requestedLayout
                }
                val presentation = currentUiState.toPresentation()
                val onMouthSelected: (MouthShape) -> Unit = { mouth ->
                    currentUiState = ManualMouthTestReducer.selectMouth(currentUiState, mouth)
                }
                val onResetMouth: () -> Unit = {
                    currentUiState = ManualMouthTestReducer.resetToIdle(currentUiState)
                }
                val onTtsTextChanged: (String) -> Unit = { text ->
                    currentUiState = TtsSubmitReducer.updateText(currentUiState, text)
                }
                val onSubmitTtsText: () -> Unit = {
                    currentUiState = TtsSubmitReducer.submitText(currentUiState)
                }

                when (resolvedLayout) {
                    AppLayoutMode.Landscape -> LandscapeHome(
                        uiState = currentUiState,
                        presentation = presentation,
                        motionPolicy = motionPolicy,
                        onMouthSelected = onMouthSelected,
                        onResetMouth = onResetMouth,
                        onTtsTextChanged = onTtsTextChanged,
                        onSubmitTtsText = onSubmitTtsText,
                    )

                    AppLayoutMode.Portrait -> PortraitHome(
                        uiState = currentUiState,
                        presentation = presentation,
                        motionPolicy = motionPolicy,
                        onMouthSelected = onMouthSelected,
                        onResetMouth = onResetMouth,
                        onTtsTextChanged = onTtsTextChanged,
                        onSubmitTtsText = onSubmitTtsText,
                    )
                }
            }
        }
    }
}

@Composable
private fun LandscapeHome(
    uiState: SpeechDemoState,
    presentation: PetStatePresentation,
    motionPolicy: ReduceMotionPolicy,
    onMouthSelected: (MouthShape) -> Unit,
    onResetMouth: () -> Unit,
    onTtsTextChanged: (String) -> Unit,
    onSubmitTtsText: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(DogSpacing.Md),
    ) {
        TopStatusBar(
            uiState = uiState,
            presentation = presentation,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(DogSpacing.Xl),
        ) {
            PetStagePlaceholder(
                modifier = Modifier
                    .weight(3f)
                    .fillMaxHeight(),
                uiState = uiState,
                motionPolicy = motionPolicy,
            )
            DebugPanelPlaceholder(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight(),
                uiState = uiState,
                onMouthSelected = onMouthSelected,
                onResetMouth = onResetMouth,
            )
        }
        SpeechInputPlaceholder(
            modifier = Modifier.fillMaxWidth(),
            inputText = uiState.ttsInputText,
            errorText = uiState.inputError,
            isBusy = uiState.isSpeechSessionBusy,
            onTextChanged = onTtsTextChanged,
            onSubmitText = onSubmitTtsText,
        )
    }
}

@Composable
private fun PortraitHome(
    uiState: SpeechDemoState,
    presentation: PetStatePresentation,
    motionPolicy: ReduceMotionPolicy,
    onMouthSelected: (MouthShape) -> Unit,
    onResetMouth: () -> Unit,
    onTtsTextChanged: (String) -> Unit,
    onSubmitTtsText: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(DogSpacing.Md),
    ) {
        TopStatusBar(
            uiState = uiState,
            presentation = presentation,
        )
        PetStagePlaceholder(
            modifier = Modifier.fillMaxWidth(),
            uiState = uiState,
            motionPolicy = motionPolicy,
        )
        SpeechInputPlaceholder(
            modifier = Modifier.fillMaxWidth(),
            compact = true,
            inputText = uiState.ttsInputText,
            errorText = uiState.inputError,
            isBusy = uiState.isSpeechSessionBusy,
            onTextChanged = onTtsTextChanged,
            onSubmitText = onSubmitTtsText,
        )
        DebugSummaryPlaceholder(
            modifier = Modifier.fillMaxWidth(),
            uiState = uiState,
            onMouthSelected = onMouthSelected,
            onResetMouth = onResetMouth,
        )
    }
}

@Composable
private fun TopStatusBar(
    modifier: Modifier = Modifier,
    uiState: SpeechDemoState,
    presentation: PetStatePresentation,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .background(DogColors.Surface, DogShape.Panel)
            .border(1.dp, DogColors.Border, DogShape.Panel)
            .padding(horizontal = DogSpacing.Md, vertical = DogSpacing.Sm)
            .testTag(AppContentContract.TagStatusBar)
            .semantics {
                contentDescription = AppContentContract.statusBarDescription(
                    stateLabel = presentation.stateLabel,
                    inputSourceLabel = uiState.inputSource.label,
                    stateDescription = presentation.stateDescription,
                    collarDescription = presentation.collar.description,
                )
            },
    ) {
        if (maxWidth < 560.dp) {
            Column(
                verticalArrangement = Arrangement.spacedBy(DogSpacing.Sm),
            ) {
                StatusTitle(modifier = Modifier.fillMaxWidth())
                StatusInfoRow(
                    modifier = Modifier.fillMaxWidth(),
                    uiState = uiState,
                    presentation = presentation,
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusTitle(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = DogSpacing.Md),
                )
                StatusInfoRow(
                    modifier = Modifier.weight(1f),
                    uiState = uiState,
                    presentation = presentation,
                )
            }
        }
    }
}

@Composable
private fun StatusTitle(modifier: Modifier = Modifier) {
    Text(
        text = AppContentContract.Title,
        modifier = modifier,
        color = DogColors.TextPrimary,
        fontSize = DogTypography.PageTitle,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun StatusInfoRow(
    modifier: Modifier = Modifier,
    uiState: SpeechDemoState,
    presentation: PetStatePresentation,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(DogSpacing.Sm, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusPill(presentation.stateLabel, presentation.collar.color)
        Text(
            text = AppContentContract.inputSourceText(uiState.inputSource.label),
            modifier = Modifier.weight(1f, fill = false),
            color = DogColors.TextSecondary,
            fontSize = DogTypography.Label,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun StatusPill(
    text: String,
    color: androidx.compose.ui.graphics.Color,
) {
    Row(
        modifier = Modifier
            .heightIn(min = DogSpacing.TouchTarget)
            .background(DogColors.SurfaceAlt, DogShape.Panel)
            .border(1.dp, DogColors.Border, DogShape.Panel)
            .padding(horizontal = DogSpacing.Sm),
        horizontalArrangement = Arrangement.spacedBy(DogSpacing.Xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape),
        )
        Text(
            text = text,
            color = DogColors.TextPrimary,
            fontSize = DogTypography.Label,
            fontWeight = FontWeight.Bold,
        )
    }
}
