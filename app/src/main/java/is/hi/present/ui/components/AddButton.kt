package `is`.hi.present.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import `is`.hi.present.ui.theme.SoftLavender
import `is`.hi.present.ui.theme.SoftLavenderDark
import `is`.hi.present.ui.theme.TextPrimary
import `is`.hi.present.ui.theme.White

@Composable
fun AddButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val shape = CircleShape
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "fabScale"
    )

    val buttonOffsetY by animateDpAsState(
        targetValue = if (isPressed) 3.dp else 0.dp,
        animationSpec = tween(durationMillis = 120),
        label = "fabOffsetY"
    )

    val shadowElevation by animateDpAsState(
        targetValue = if (isPressed) 8.dp else 18.dp,
        animationSpec = tween(durationMillis = 120),
        label = "fabShadow"
    )

    val shadowLayerOffsetX by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 5.dp,
        animationSpec = tween(durationMillis = 120),
        label = "shadowLayerOffsetX"
    )

    val shadowLayerOffsetY by animateDpAsState(
        targetValue = if (isPressed) 3.dp else 7.dp,
        animationSpec = tween(durationMillis = 120),
        label = "shadowLayerOffsetY"
    )

    val baseGradient = Brush.linearGradient(
        colors = listOf(
            White.copy(alpha = 0.28f),
            SoftLavender.copy(alpha = 0.98f),
            SoftLavenderDark.copy(alpha = 0.72f)
        ),
        start = Offset(0f, 320f),
        end = Offset(320f, 0f)
    )

    val glossySpot = Brush.radialGradient(
        colors = listOf(
            White.copy(alpha = 0.46f),
            White.copy(alpha = 0.18f),
            White.copy(alpha = 0f)
        ),
        center = Offset(58f, 52f),
        radius = 92f
    )

    val diagonalShine = Brush.linearGradient(
        colors = listOf(
            White.copy(alpha = 0f),
            White.copy(alpha = 0.12f),
            White.copy(alpha = 0f)
        ),
        start = Offset(20f, 135f),
        end = Offset(150f, 18f)
    )

    Box(
        modifier = modifier.size(65.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .offset(x = shadowLayerOffsetX, y = shadowLayerOffsetY)
                .clip(shape)
                .background(SoftLavenderDark.copy(alpha = 0.08f))
        )

        Box(
            modifier = Modifier
                .offset(y = buttonOffsetY)
                .scale(scale)
                .size(75.dp)
                .shadow(
                    elevation = shadowElevation,
                    shape = shape,
                    ambientColor = SoftLavenderDark.copy(alpha = 0.12f),
                    spotColor = SoftLavenderDark.copy(alpha = 0.16f)
                )
                .clip(shape)
                .background(baseGradient)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(shape)
                    .background(glossySpot)
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 4.dp, y = (-4).dp)
                    .clip(shape)
                    .background(diagonalShine)
            )

            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = contentDescription,
                tint = TextPrimary.copy(alpha = 0.82f),
                modifier = Modifier.size(30.dp)
            )
        }
    }
}