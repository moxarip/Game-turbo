package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface

@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    borderColor: Color = DarkBorder,
    borderWidth: Dp = 1.dp,
    cornerRadius: Dp = 16.dp,
    glowGradient: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val borderModifier = if (glowGradient) {
        Modifier.border(
            width = borderWidth,
            brush = Brush.verticalGradient(
                listOf(borderColor, borderColor.copy(alpha = 0.2f))
            ),
            shape = shape
        )
    } else {
        Modifier.border(borderWidth, borderColor, shape)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(DarkSurface)
            .then(borderModifier)
            .padding(16.dp),
        content = content
    )
}
