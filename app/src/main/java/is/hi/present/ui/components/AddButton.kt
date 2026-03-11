package `is`.hi.present.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
    val shape = RoundedCornerShape(28.dp)

    val baseGradient = Brush.linearGradient(
        colors = listOf(
            SoftLavender.copy(alpha = 0.98f),
            SoftLavender,
            SoftLavenderDark.copy(alpha = 0.98f)
        ),
        start = Offset(0f, 0f),
        end = Offset(280f, 320f)
    )

    val glossyTop = Brush.verticalGradient(
        colors = listOf(
            White.copy(alpha = 0.34f),
            White.copy(alpha = 0.14f),
            White.copy(alpha = 0.03f),
            White.copy(alpha = 0f)
        ),
        startY = 0f,
        endY = 120f
    )

    val softInnerLight = Brush.linearGradient(
        colors = listOf(
            White.copy(alpha = 0.16f),
            White.copy(alpha = 0.04f),
            White.copy(alpha = 0f)
        ),
        start = Offset(30f, 20f),
        end = Offset(180f, 180f)
    )

    Box(
        modifier = modifier
            .size(78.dp)
            .shadow(
                elevation = 18.dp,
                shape = shape,
                ambientColor = SoftLavenderDark.copy(alpha = 0.30f),
                spotColor = SoftLavenderDark.copy(alpha = 0.30f)
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
                .background(glossyTop)
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(softInnerLight)
        )

        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = contentDescription,
            tint = TextPrimary.copy(alpha = 0.82f),
            modifier = Modifier.size(34.dp)
        )
    }
}