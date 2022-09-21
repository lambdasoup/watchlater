/*
 * Copyright (c) 2015 - 2022
 *
 * Maximilian Hille <mh@lambdasoup.com>
 * Juliane Lehmann <jl@lambdasoup.com>
 *
 * This file is part of Watch Later.
 *
 * Watch Later is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Watch Later is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Watch Later.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.lambdasoup.watchlater.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Modification of [animateContentSize] - that one has "wrap_content"-like semantics in boths dimensions - it animates
 * to the intrinsic size. That does not work well on composables that should instead be fillWidth-sized (or fillHeight-)
 * So this one only wraps vertically.
 */
fun Modifier.animateContentHeight(
    animationSpec: FiniteAnimationSpec<Int> = spring(),
    finishedListener: ((initialValue: Int, targetValue: Int) -> Unit)? = null
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "animateContentHeight"
        properties["animationSpec"] = animationSpec
        properties["finishedListener"] = finishedListener
    }
) {
    val scope = rememberCoroutineScope()
    val animModifier = remember(scope) {
        HeightAnimationModifier(animationSpec, scope)
    }
    animModifier.listener = finishedListener
    this.clipToBounds().then(animModifier)
}

/**
 * This class creates a [LayoutModifier] that measures children, and responds to children's height
 * change by animating to that height. The size reported to parents will be the animated size.
 */
private class HeightAnimationModifier(
    val animSpec: AnimationSpec<Int>,
    val scope: CoroutineScope,
) : LayoutModifierWithPassThroughHeightIntrinsics() {
    var listener: ((startSize: Int, endSize: Int) -> Unit)? = null

    data class AnimData(
        val anim: Animatable<Int, AnimationVector1D>,
        var startSize: Int
    )

    var animData: AnimData? = null

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {

        val placeable = measurable.measure(constraints)

        val measuredSize = IntSize(placeable.width, placeable.height)

        val height = animateTo(measuredSize.height)
        return layout(measuredSize.width, height) {
            placeable.placeRelative(0, 0)
        }
    }

    fun animateTo(targetHeight: Int): Int {
        val data = animData?.apply {
            if (targetHeight != anim.targetValue) {
                startSize = anim.value
                scope.launch {
                    val result = anim.animateTo(targetHeight, animSpec)
                    if (result.endReason == AnimationEndReason.Finished) {
                        listener?.invoke(startSize, result.endState.value)
                    }
                }
            }
        } ?: AnimData(
            Animatable(
                targetHeight, Int.VectorConverter, 1
            ),
            targetHeight
        )

        animData = data
        return data.anim.value
    }
}

internal abstract class LayoutModifierWithPassThroughHeightIntrinsics : LayoutModifier {
    final override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ) = measurable.minIntrinsicHeight(width)

    final override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ) = measurable.maxIntrinsicHeight(width)
}
