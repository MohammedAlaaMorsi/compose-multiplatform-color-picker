package com.github.mohammedalaamorsi.colorpicker.pickers

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.github.mohammedalaamorsi.colorpicker.data.ColorRange
import com.github.mohammedalaamorsi.colorpicker.data.Colors.gradientColors
import com.github.mohammedalaamorsi.colorpicker.ext.blue
import com.github.mohammedalaamorsi.colorpicker.ext.darken
import com.github.mohammedalaamorsi.colorpicker.ext.drawColorSelector
import com.github.mohammedalaamorsi.colorpicker.ext.green
import com.github.mohammedalaamorsi.colorpicker.ext.lighten
import com.github.mohammedalaamorsi.colorpicker.ext.red
import com.github.mohammedalaamorsi.colorpicker.helper.ColorPickerHelper
import kotlin.math.roundToInt

@ExperimentalComposeUiApi
@Composable
internal fun ClassicColorPicker(
    modifier: Modifier = Modifier,
    showAlphaBar: Boolean,
    onPickedColor: (Color) -> Unit
) {
    var pickerLocation by remember {
        mutableStateOf(Offset.Zero)
    }
    var colorPickerSize by remember {
        mutableStateOf(IntSize(1,1))
    }
    var alpha by remember {
        mutableStateOf(1f)
    }
    var rangeColor by remember {
        mutableStateOf(Color.White)
    }
    var color by remember {
        mutableStateOf(Color.White)
    }
    LaunchedEffect(rangeColor, pickerLocation, colorPickerSize, alpha) {
        val xProgress = 1- (pickerLocation.x / colorPickerSize.width)
        val yProgress = pickerLocation.y / colorPickerSize.height
        color = Color(
            rangeColor
                .red()
                .lighten(xProgress)
                .darken(yProgress),
            rangeColor
                .green()
                .lighten(xProgress)
                .darken(yProgress),
            rangeColor
                .blue()
                .lighten(xProgress)
                .darken(yProgress),
            alpha = (255 * alpha).roundToInt()
        )
    }
    LaunchedEffect(color) {
        onPickedColor(color)
    }
    Column(modifier = Modifier.width(IntrinsicSize.Max)) {
        Box(
            modifier = modifier
                .onSizeChanged {
                    colorPickerSize = it
                }.pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val x = change.position.x.coerceIn(0f, colorPickerSize.width.toFloat())
                        val y = change.position.y.coerceIn(0f, colorPickerSize.height.toFloat())
                        pickerLocation = Offset(x, y)
                    }
                }
                .size(200.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
            ) {
                drawRect(Brush.horizontalGradient(listOf(Color.White, rangeColor)))
                drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
            }
            Canvas(modifier = Modifier.fillMaxSize()) {
                this.drawColorSelector(color.copy(alpha = 1f), pickerLocation)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        ColorSlideBar(colors = gradientColors) {
            val (rangeProgress, range) = ColorPickerHelper.calculateRangeProgress(it.toDouble())
            val red: Int
            val green: Int
            val blue: Int
            when (range) {
                ColorRange.RedToYellow -> {
                    red = 255
                    green = (255 * rangeProgress).roundToInt()
                    blue = 0
                }
                ColorRange.YellowToGreen -> {
                    red = (255 * (1 - rangeProgress)).roundToInt()
                    green = 255
                    blue = 0
                }
                ColorRange.GreenToCyan -> {
                    red = 0
                    green = 255
                    blue = (255 * rangeProgress).roundToInt()
                }
                ColorRange.CyanToBlue -> {
                    red = 0
                    green = (255 * (1 - rangeProgress)).roundToInt()
                    blue = 255
                }
                ColorRange.BlueToPurple -> {
                    red = (255 * rangeProgress).roundToInt()
                    green = 0
                    blue = 255
                }
                ColorRange.PurpleToRed -> {
                    red = 255
                    green = 0
                    blue = (255 * (1 - rangeProgress)).roundToInt()
                }
            }
            rangeColor = Color(red, green, blue)
        }
        if (showAlphaBar) {
            Spacer(modifier = Modifier.height(16.dp))
            ColorSlideBar(colors = listOf(Color.Transparent, rangeColor)) {
                alpha = it
            }
        }
    }
}
