package com.digitaldog.demo.dogrenderer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
        val highlightColor = Color.White
        val tongueColor = DogColors.PetWarm
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

            MouthShape.Small -> drawOval(
                color = mouthColor,
                topLeft = Offset(centerX - unit(13f), centerY - unit(9f)),
                size = Size(width = unit(26f), height = unit(18f)),
            )

            MouthShape.Wide -> drawOval(
                color = mouthColor,
                topLeft = Offset(centerX - unit(15f), centerY - unit(21f)),
                size = Size(width = unit(30f), height = unit(42f)),
            )

            MouthShape.Round -> drawOval(
                color = mouthColor,
                topLeft = Offset(centerX - unit(16f), centerY - unit(16f)),
                size = Size(width = unit(32f), height = unit(32f)),
            )

            MouthShape.Smile -> drawArc(
                color = mouthColor,
                startAngle = 18f,
                sweepAngle = 144f,
                useCenter = false,
                topLeft = Offset(centerX - unit(32f), centerY - unit(24f)),
                size = Size(width = unit(64f), height = unit(42f)),
                style = Stroke(width = unit(6f), cap = StrokeCap.Round),
            )

            MouthShape.Teeth -> {
                drawRoundRect(
                    color = mouthColor,
                    topLeft = Offset(centerX - unit(28f), centerY - unit(10f)),
                    size = Size(width = unit(56f), height = unit(20f)),
                )
                drawRoundRect(
                    color = highlightColor,
                    topLeft = Offset(centerX - unit(23f), centerY - unit(6f)),
                    size = Size(width = unit(46f), height = unit(7f)),
                )
            }

            MouthShape.Pant -> {
                drawRoundRect(
                    color = mouthColor,
                    topLeft = Offset(centerX - unit(24f), centerY - unit(12f)),
                    size = Size(width = unit(48f), height = unit(24f)),
                )
                drawOval(
                    color = tongueColor,
                    topLeft = Offset(centerX - unit(10f), centerY + unit(2f)),
                    size = Size(width = unit(20f), height = unit(20f)),
                )
            }
        }
    }
}
