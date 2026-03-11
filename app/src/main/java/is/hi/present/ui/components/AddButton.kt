package `is`.hi.present.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    val baseGradient = Brush.linearGradient(
        colors = listOf(
            White.copy(alpha = 0.30f),
            SoftLavender,
            SoftLavenderDark,
            SoftLavenderDark
        ),
        start = Offset(0f, 340f),
        end = Offset(340f, 0f)
    )

    val glossySpot = Brush.radialGradient(
        colors = listOf(
            White.copy(alpha = 0.34f),
            White.copy(alpha = 0.10f),
            White.copy(alpha = 0f)
        ),
        center = Offset(45f, 145f),
        radius = 95f
    )

    val diagonalShine = Brush.linearGradient(
        colors = listOf(
            White.copy(alpha = 0f),
            White.copy(alpha = 0.16f),
            White.copy(alpha = 0f)
        ),
        start = Offset(35f, 120f),
        end = Offset(170f, 20f)
    )

    Box(
        modifier = modifier
            .size(78.dp)
            .shadow(
                elevation = 18.dp,
                shape = shape,
                ambientColor = SoftLavenderDark.copy(alpha = 0.28f),
                spotColor = SoftLavenderDark.copy(alpha = 0.28f)
            )
            .clip(shape)
            .background(baseGradient)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
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
            modifier = Modifier.size(34.dp)
        )
    }
}