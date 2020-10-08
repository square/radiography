package com.squareup.radiography.sample.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.WithConstraints
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import kotlin.math.roundToInt

@Preview @Composable private fun RadiographyLogoPreview() {
  RadiographyLogo()
}

/**
 * Draws a Radiography logo, nesting the xray screen until it's too small to see.
 */
@Composable fun RadiographyLogo(modifier: Modifier = Modifier) {
  val outerImage = imageResource(R.drawable.logo_outer)
  val innerImage = imageResource(R.drawable.logo_inner)
  val aspectRatio = outerImage.width.toFloat() / outerImage.height.toFloat()

  Box(modifier.aspectRatio(aspectRatio)) {
    Image(outerImage)
    InfiniteMirror(
        centerOffsetFraction = Offset(.5f, .59f),
        scaleFactor = .46f
    ) {
      Image(innerImage)
    }
  }
}

/**
 * Draws the inner, recursive part of the Radiography logo, recursively.
 *
 * The image and density are passed as parameters, instead of being loaded or read from the ambient
 * inside the function, to avoid having to load the same resource or observe to the same ambient
 * multiple times.
 */
@Composable private fun InfiniteMirror(
  centerOffsetFraction: Offset,
  scaleFactor: Float,
  minimumSizeThreshold: Dp = 8.dp,
  content: @Composable () -> Unit
) {
  // Use WithConstraints as a big box that centers the actual content within itself.
  // TODO Scanning doesn't handle subcomposition. https://github.com/square/radiography/issues/93
  WithConstraints(
      Modifier
          .fillMaxSize()
          .wrapContentSize(FractionalAlignment(centerOffsetFraction))
  ) {
    // Don't draw the content if it's going to be too small to see, and stop recursing.
    val minSize = with(DensityAmbient.current) { minimumSizeThreshold.toIntPx() }
    if (constraints.maxHeight < minSize && constraints.maxWidth < minSize) {
      return@WithConstraints
    }

    // Draw the content and recurse at the next scale. Note that the centering is being performed by
    // wrapContentSize above, this modifier just needs to scale.
    Box(Modifier.fillMaxSize(scaleFactor)) {
      content()
      InfiniteMirror(centerOffsetFraction, scaleFactor, minimumSizeThreshold, content)
    }
  }
}

/** Aligns the composable as horizontal and vertical fractions of its bounds. */
private class FractionalAlignment(
  private val offsetFraction: Offset
) : Alignment {
  override fun align(
    size: IntSize,
    layoutDirection: LayoutDirection
  ): IntOffset = IntOffset(
      x = (size.width * offsetFraction.x).roundToInt(),
      y = (size.height * offsetFraction.y).roundToInt()
  )
}
