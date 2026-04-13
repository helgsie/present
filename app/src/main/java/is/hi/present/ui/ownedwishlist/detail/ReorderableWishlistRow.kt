package `is`.hi.present.ui.ownedwishlist.detail

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.zIndex
import `is`.hi.present.ui.components.WishlistItemCard

@Composable
fun ReorderableWishlistRow(
    item: WishlistItemUi,
    index: Int,
    onMove: (Int, Int) -> Unit,
    onDragEnd: () -> Unit,
    onClick: () -> Unit
) {
    var currentIndex by remember(item.id) { mutableIntStateOf(index) }
    var dragOffsetY by remember(item.id) { mutableFloatStateOf(0f) }
    var itemHeightPx by remember(item.id) { mutableIntStateOf(1) }
    var isDragging by remember(item.id) { mutableStateOf(false) }

    LaunchedEffect(index, isDragging) {
        if (!isDragging) {
            currentIndex = index
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { size ->
                itemHeightPx = size.height.coerceAtLeast(1)
            }
            .graphicsLayer {
                translationY = dragOffsetY
            }
            .zIndex(if (isDragging) 1f else 0f)
            .pointerInput(item.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        isDragging = true
                    },
                    onDragCancel = {
                        isDragging = false
                        dragOffsetY = 0f
                        onDragEnd()
                    },
                    onDragEnd = {
                        isDragging = false
                        dragOffsetY = 0f
                        onDragEnd()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetY += dragAmount.y

                        val threshold = itemHeightPx * 0.5f

                        while (dragOffsetY > threshold) {
                            onMove(currentIndex, currentIndex + 1)
                            currentIndex += 1
                            dragOffsetY -= itemHeightPx
                        }

                        while (dragOffsetY < -threshold) {
                            onMove(currentIndex, currentIndex - 1)
                            currentIndex -= 1
                            dragOffsetY += itemHeightPx
                        }
                    }
                )
            }
    ) {
        WishlistItemCard(
            w = item,
            onClick = onClick
        )
    }
}