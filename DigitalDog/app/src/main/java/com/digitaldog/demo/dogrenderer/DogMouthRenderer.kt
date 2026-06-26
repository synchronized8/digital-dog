package com.digitaldog.demo.dogrenderer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.digitaldog.demo.designsystem.DogColors
import com.digitaldog.demo.sharedmodel.MouthShape

@Composable
fun DogMouthRenderer(
    mouthShape: MouthShape,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(width = 82.dp, height = 46.dp)) {
        val mouthColor = DogColors.TextPrimary
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val scale = minOf(size.width / 82f, size.height / 46f)
        fun unit(value: Float) = value * scale

        when (mouthShape) {
            MouthShape.Closed -> drawRoundRect(
                color = mouthColor,
                topLeft = Offset(centerX - unit(30f), centerY - unit(2f)),
                size = Size(width = unit(60f), height = unit(4f)),
            )

            MouthShape.Open -> drawOval(
                color = mouthColor,
                topLeft = Offset(centerX - unit(16f), centerY - unit(18f)),
                size = Size(width = unit(32f), height = unit(36f)),
            )
        }
    }
}
