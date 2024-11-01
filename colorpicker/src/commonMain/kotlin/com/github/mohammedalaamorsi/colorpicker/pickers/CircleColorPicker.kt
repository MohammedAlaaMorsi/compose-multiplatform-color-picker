package com.github.mohammedalaamorsi.colorpicker.pickers


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.github.mohammedalaamorsi.colorpicker.data.ColorRange
import com.github.mohammedalaamorsi.colorpicker.data.Colors.gradientColors
import com.github.mohammedalaamorsi.colorpicker.ext.blue
import com.github.mohammedalaamorsi.colorpicker.ext.darken
import com.github.mohammedalaamorsi.colorpicker.ext.drawColorSelector
import com.github.mohammedalaamorsi.colorpicker.ext.green
import com.github.mohammedalaamorsi.colorpicker.ext.lighten
import com.github.mohammedalaamorsi.colorpicker.ext.red
import com.github.mohammedalaamorsi.colorpicker.helper.BoundedPointStrategy
import com.github.mohammedalaamorsi.colorpicker.helper.ColorPickerHelper
import com.github.mohammedalaamorsi.colorpicker.helper.MathHelper.getBoundedPointWithInRadius
import com.github.mohammedalaamorsi.colorpicker.helper.MathHelper.getLength
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt

@ExperimentalComposeUiApi
@Composable
internal fun CircleColorPicker(
    modifier: Modifier = Modifier,
    showAlphaBar: Boolean,
    showBrightnessBar: Boolean,
    lightCenter: Boolean,
    onPickedColor: (Color) -> Unit
) {
    var radius by remember {
        mutableStateOf(0f)
    }
    var pickerLocation by remember(radius) {
        mutableStateOf(Offset(radius, radius))
    }
    var pickerColor by remember {
        mutableStateOf(
            if (lightCenter) {
                Color.White
            } else {
                Color.Black
            }
        )
    }
    var brightness by remember {
        mutableStateOf(0f)
    }
    var alpha by remember {
        mutableStateOf(1f)
    }
    LaunchedEffect(brightness, pickerColor, alpha) {
        onPickedColor(
            Color(
                pickerColor.red().moveColorTo(!lightCenter, brightness),
                pickerColor.green().moveColorTo(!lightCenter, brightness),
                pickerColor.blue().moveColorTo(!lightCenter, brightness),
                (255 * alpha).roundToInt()
            )
        )
    }
    Column(modifier = Modifier.width(IntrinsicSize.Max)) {
        Canvas(modifier = modifier
            .size(200.dp)
            .onSizeChanged {
                radius = it.width / 2f
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val x = change.position.x
                    val y = change.position.y
                    val angle = (toDegrees(atan2(y - radius, x - radius).toDouble()) + 360) % 360
                    val length = getLength(x, y, radius)
                    val radiusProgress = 1 - (length / radius).coerceIn(0f, 1f)
                    val angleProgress = angle / 360f
                    val (rangeProgress, range) = ColorPickerHelper.calculateRangeProgress(angleProgress)
                    // Continue handling color picker logic here
                        pickerColor = when (range) {
                            ColorRange.RedToYellow -> {
                                Color(
                                    red = 255.moveColorTo(lightCenter, radiusProgress),
                                    green = (255f * rangeProgress)
                                        .moveColorTo(lightCenter, radiusProgress)
                                        .roundToInt(),
                                    blue = 0.moveColorTo(lightCenter, radiusProgress),
                                )
                            }
                            ColorRange.YellowToGreen -> {
                                Color(
                                    red = (255 * (1 - rangeProgress))
                                        .moveColorTo(lightCenter, radiusProgress)
                                        .roundToInt(),
                                    green = 255.moveColorTo(lightCenter, radiusProgress),
                                    blue = 0.moveColorTo(lightCenter, radiusProgress),
                                )
                            }
                            ColorRange.GreenToCyan -> {
                                Color(
                                    red = 0.moveColorTo(lightCenter, radiusProgress),
                                    green = 255.moveColorTo(lightCenter, radiusProgress),
                                    blue = (255 * rangeProgress)
                                        .moveColorTo(lightCenter, radiusProgress)
                                        .roundToInt(),
                                )
                            }
                            ColorRange.CyanToBlue -> {
                                Color(
                                    red = 0.moveColorTo(lightCenter, radiusProgress),
                                    green = (255 * (1 - rangeProgress))
                                        .moveColorTo(lightCenter, radiusProgress)
                                        .roundToInt(),
                                    blue = 255.moveColorTo(lightCenter, radiusProgress),
                                )
                            }
                            ColorRange.BlueToPurple -> {
                                Color(
                                    red = (255 * rangeProgress)
                                        .moveColorTo(lightCenter, radiusProgress)
                                        .roundToInt(),
                                    green = 0.moveColorTo(lightCenter, radiusProgress),
                                    blue = 255.moveColorTo(lightCenter, radiusProgress),
                                )
                            }
                            ColorRange.PurpleToRed -> {
                                Color(
                                    red = 255.moveColorTo(lightCenter, radiusProgress),
                                    green = 0.moveColorTo(lightCenter, radiusProgress),
                                    blue = (255 * (1 - rangeProgress))
                                        .moveColorTo(lightCenter, radiusProgress)
                                        .roundToInt(),
                                )
                            }
                        }
                        pickerLocation = getBoundedPointWithInRadius(
                            x,
                            y,
                            length,
                            radius,
                            BoundedPointStrategy.Inside
                        )
                    }
            }) {
            drawCircle(
                Brush.sweepGradient(gradientColors)
            )
            drawCircle(
                ShaderBrush(
                    RadialGradientShader(
                        Offset(size.width / 2f, size.height / 2f),
                        colors = listOf(
                            if (lightCenter) {
                                Color.White
                            } else {
                                Color.Black
                            }, Color.Transparent
                        ),
                        radius = size.width / 2f
                    )
                )
            )
            drawColorSelector(pickerColor, pickerLocation)
        }
        if (showBrightnessBar) {
            Spacer(modifier = Modifier.height(16.dp))
            ColorSlideBar(
                colors = listOf(
                    if (lightCenter) {
                        Color.Black
                    } else {
                        Color.White
                    }, pickerColor
                )
            ) {
                brightness = 1 - it
            }
        }
        if (showAlphaBar) {
            Spacer(modifier = Modifier.height(16.dp))
            ColorSlideBar(colors = listOf(Color.Transparent, pickerColor)) {
                alpha = it
            }
        }
    }
}

private fun toDegrees(radians: Double) =radians * 180.0 / PI


private fun Int.moveColorTo(toWhite: Boolean, progress: Float): Int {
    return if (toWhite) {
        lighten(progress)
    } else {
        darken(progress)
    }
}

private fun Double.moveColorTo(toWhite: Boolean, progress: Float): Double {
    return if (toWhite) {
        lighten(progress)
    } else {
        darken(progress)
    }
}