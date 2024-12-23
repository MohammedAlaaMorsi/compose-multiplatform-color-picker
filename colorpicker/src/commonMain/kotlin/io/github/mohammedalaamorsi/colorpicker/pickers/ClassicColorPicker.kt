package io.github.mohammedalaamorsi.colorpicker.pickers

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import io.github.mohammedalaamorsi.colorpicker.data.ColorRange
import io.github.mohammedalaamorsi.colorpicker.data.Colors.gradientColors
import io.github.mohammedalaamorsi.colorpicker.ext.blue
import io.github.mohammedalaamorsi.colorpicker.ext.darken
import io.github.mohammedalaamorsi.colorpicker.ext.drawColorSelector
import io.github.mohammedalaamorsi.colorpicker.ext.green
import io.github.mohammedalaamorsi.colorpicker.ext.lighten
import io.github.mohammedalaamorsi.colorpicker.ext.red
import io.github.mohammedalaamorsi.colorpicker.helper.ColorPickerHelper
import io.github.mohammedalaamorsi.colorpicker.helper.ColorPickerHelper.calculateInitialPickerLocation
import kotlin.math.roundToInt

@ExperimentalComposeUiApi
@Composable
internal fun ClassicColorPicker(
    modifier: Modifier = Modifier,
    showAlphaBar: Boolean,
    initialColor: Color = Color.Red,
    onPickedColor: (Color) -> Unit
) {
    var pickerLocation by remember {
        mutableStateOf(Offset.Zero)
    }
    var colorPickerSize by remember {
        mutableStateOf(IntSize(1, 1))
    }
    var alpha by remember {
        mutableStateOf(initialColor.alpha)
    }
    var rangeColor by remember {
        mutableStateOf(initialColor)
    }
    var color by remember {
        mutableStateOf(initialColor)
    }
    LaunchedEffect(colorPickerSize, initialColor) {
        if (colorPickerSize.width > 1 && colorPickerSize.height > 1) {
            pickerLocation = calculateInitialPickerLocation(
                initialColor,
                colorPickerSize,
                rangeColor
            )
        }
    }
    LaunchedEffect(rangeColor, pickerLocation, colorPickerSize, alpha) {
        val xProgress = (1f - (pickerLocation.x / colorPickerSize.width)).coerceIn(0f, 1f)
        val yProgress = (pickerLocation.y / colorPickerSize.height).coerceIn(0f, 1f)
        if(xProgress.isNaN().not()&&yProgress.isNaN().not()){
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
        ColorSlideBar(colors = gradientColors, initialColor = initialColor) {
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
            ColorSlideBar(colors = listOf(Color.Transparent, rangeColor),initialColor=initialColor) {
                alpha = it
            }
        }
    }


}
