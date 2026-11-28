package com.eco.nota.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eco.nota.ui.theme.CardBorderGradient
import com.eco.nota.ui.theme.NotaTheme
import com.eco.nota.ui.theme.ProBackground
import com.eco.nota.ui.theme.ProBorder
import com.eco.nota.ui.theme.ProCardSurfaceLight
import com.eco.nota.ui.theme.TextGray
import com.eco.nota.ui.theme.TextMuted
import com.eco.nota.ui.theme.ambientGlow

@Composable
fun FloatingWindowOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    maxWidth: Dp = 560.dp,
    heightFraction: Float = 0.90f,
    fillMaxHeight: Boolean = false,
    dismissOnClickOutside: Boolean = true,
    windowTestTag: String? = null,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(durationMillis = 220)),
            exit = fadeOut(animationSpec = tween(durationMillis = 180))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.68f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { if (dismissOnClickOutside) onDismiss() }
                    )
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(durationMillis = 210)) +
                scaleIn(
                    initialScale = 0.92f,
                    animationSpec = spring(
                        dampingRatio = 0.76f,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) +
                slideInVertically(
                    initialOffsetY = { it / 16 },
                    animationSpec = tween(durationMillis = 230)
                ),
            exit = fadeOut(animationSpec = tween(durationMillis = 160)) +
                scaleOut(
                    targetScale = 0.95f,
                    animationSpec = tween(durationMillis = 160)
                ) +
                slideOutVertically(
                    targetOffsetY = { it / 20 },
                    animationSpec = tween(durationMillis = 160)
                ),
            modifier = Modifier.fillMaxSize()
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .imePadding()
                    .padding(horizontal = 14.dp, vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                val maxWindowHeight = maxHeight * heightFraction
                val baseModifier = if (fillMaxHeight) {
                    Modifier
                        .fillMaxWidth()
                        .widthIn(max = maxWidth)
                        .height(maxWindowHeight)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .widthIn(max = maxWidth)
                        .heightIn(max = maxWindowHeight)
                }
                val finalModifier = if (windowTestTag != null) {
                    baseModifier.testTag(windowTestTag)
                } else {
                    baseModifier
                }

                Surface(
                    modifier = finalModifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { }
                    ),
                    shape = RoundedCornerShape(24.dp),
                    color = ProBackground,
                    border = BorderStroke(1.dp, CardBorderGradient),
                    shadowElevation = 24.dp,
                    tonalElevation = 8.dp
                ) {
                    Box(
                        modifier = (if (fillMaxHeight) Modifier.fillMaxSize() else Modifier.fillMaxWidth())
                            .ambientGlow()
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

@Composable
fun NotaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextMuted, fontSize = 14.sp) },
        leadingIcon = leadingIcon?.let {
            { Icon(it, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp)) }
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NotaTheme.colors.brandPrimary,
            unfocusedBorderColor = ProBorder,
            focusedContainerColor = ProCardSurfaceLight,
            unfocusedContainerColor = ProCardSurfaceLight,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        keyboardOptions = keyboardOptions,
        singleLine = singleLine
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = ProCardSurfaceLight,
        border = androidx.compose.foundation.BorderStroke(1.dp, com.eco.nota.ui.theme.CardBorderGradient)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = NotaTheme.colors.brandPrimary.copy(alpha = 0.15f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = NotaTheme.colors.brandPrimary, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp), color = Color.White)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, style = NotaTheme.typography.label.copy(fontSize = 11.5.sp), color = TextGray)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun TextButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.TextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(text, style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp), color = NotaTheme.colors.brandPrimary)
    }
}

private fun androidx.compose.ui.unit.Dp.dpToFloat(): Float = this.value
