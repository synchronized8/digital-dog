package com.digitaldog.demo.speechinput

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.digitaldog.demo.app.AppContentContract
import com.digitaldog.demo.designsystem.DogColors
import com.digitaldog.demo.designsystem.DogShape
import com.digitaldog.demo.designsystem.DogSpacing
import com.digitaldog.demo.designsystem.DogTypography

@Composable
fun SpeechInputPlaceholder(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    inputText: String = "",
    errorText: String? = null,
    isBusy: Boolean = false,
    onTextChanged: (String) -> Unit = {},
    onSubmitText: () -> Unit = {},
    onPlaySample: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DogColors.Surface, DogShape.Panel)
            .border(1.dp, DogColors.Border, DogShape.Panel)
            .padding(DogSpacing.Md)
            .testTag(AppContentContract.TagSpeechInput)
            .semantics {
                contentDescription = AppContentContract.SpeechInputDescription
            },
        verticalArrangement = Arrangement.spacedBy(DogSpacing.Md),
    ) {
        Text(
            text = AppContentContract.MainInputTitle,
            color = DogColors.TextPrimary,
            fontSize = DogTypography.PanelTitle,
            fontWeight = FontWeight.Bold,
        )

        OutlinedTextField(
            value = inputText,
            onValueChange = onTextChanged,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .testTag(AppContentContract.TagTtsInputField)
                .semantics {
                    contentDescription = AppContentContract.ttsInputDescription(
                        errorText = errorText,
                        isBusy = isBusy,
                    )
                },
            placeholder = {
                Text(
                    text = AppContentContract.TextInputPlaceholder,
                    color = DogColors.TextSecondary,
                    fontSize = DogTypography.Body,
                )
            },
            isError = errorText != null,
            shape = DogShape.Panel,
            singleLine = false,
            maxLines = 3,
        )

        if (errorText != null) {
            Text(
                text = errorText,
                color = DogColors.WarningText,
                fontSize = DogTypography.Label,
                fontWeight = FontWeight.Bold,
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(AppContentContract.TagQuickActions)
                .semantics {
                    contentDescription = AppContentContract.QuickActionsDescription
                },
        ) {
            val useCompactActions = compact || maxWidth < 720.dp
            if (useCompactActions) {
                CompactActionGrid(
                    isBusy = isBusy,
                    onSubmitText = onSubmitText,
                    onPlaySample = onPlaySample,
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DogSpacing.Sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PrimaryActionButton(
                        isBusy = isBusy,
                        onClick = onSubmitText,
                        modifier = Modifier.weight(1.25f),
                    )
                    SecondaryActionButton(
                        text = AppContentContract.SampleAudio,
                        isBusy = isBusy,
                        onClick = onPlaySample,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactActionGrid(
    isBusy: Boolean,
    onSubmitText: () -> Unit,
    onPlaySample: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(DogSpacing.Sm),
    ) {
        PrimaryActionButton(
            isBusy = isBusy,
            onClick = onSubmitText,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(DogSpacing.Sm),
            modifier = Modifier.fillMaxWidth(),
        ) {
            SecondaryActionButton(
                text = AppContentContract.SampleAudio,
                isBusy = isBusy,
                onClick = onPlaySample,
                modifier = Modifier.weight(1f),
            )
            Box(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun PrimaryActionButton(
    isBusy: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = !isBusy,
        modifier = modifier
            .heightIn(min = DogSpacing.TouchTarget)
            .testTag(AppContentContract.TagPrimaryTtsCta)
            .semantics {
                contentDescription = AppContentContract.primaryTtsCtaDescription(isBusy)
            },
        shape = DogShape.Button,
        colors = ButtonDefaults.buttonColors(
            containerColor = DogColors.Coral,
            contentColor = DogColors.TextPrimary,
        ),
    ) {
        Text(
            text = if (isBusy) AppContentContract.PrimaryCtaBusy else AppContentContract.PrimaryCta,
            fontSize = DogTypography.Button,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SecondaryActionButton(
    text: String,
    isBusy: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = !isBusy,
        modifier = modifier
            .heightIn(min = DogSpacing.TouchTarget)
            .testTag(AppContentContract.TagSampleCta),
        shape = DogShape.Button,
    ) {
        Text(
            text = text,
            color = DogColors.TextPrimary,
            fontSize = DogTypography.Button,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}
