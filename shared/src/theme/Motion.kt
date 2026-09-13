package de.myposter.academy.shaders.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import net.kodein.cup.TransitionSet

/**
 * The navigation motion of the MYPOSTER app, applied to the deck.
 *
 * `NavigationTransitions.kt` in `myposter-app` moves a screen 56 dp sideways and fades it at the
 * same time. The new screen comes in from the side the reader is going to, the old one leaves to
 * the other side. A pop plays the same motion backwards.
 *
 * A CuP slide change is the same event, so it gets the same motion. One short push tells the room
 * which way the talk moves. A plain fade does not.
 */
private val PUSH_DISTANCE = 56.dp

/**
 * Two springs, as in `PlatformNavDisplay.kt`.
 *
 * The app reads them from the Material motion scheme, which is still experimental. The deck writes
 * them out instead. Same stiffness, same feel, no opt-in.
 */
private val pushSpec: FiniteAnimationSpec<IntOffset> =
    spring(
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntOffset.VisibilityThreshold,
    )

private val fadeSpec: FiniteAnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow)

/** The push distance in pixels, at the density of the caller. */
@Composable private fun pushPx(): Int = with(LocalDensity.current) { PUSH_DISTANCE.roundToPx() }

/**
 * Slide and fade, in the direction of travel.
 *
 * CuP hands every transition an `isForward` flag, so one set covers both directions. The deck
 * therefore uses this same set on the start edge and on the end edge of every slide.
 */
val slidePush: TransitionSet =
    TransitionSet(
        enter = { isForward ->
            val distance = pushPx()
            slideInHorizontally(
                initialOffsetX = { if (isForward) distance else -distance },
                animationSpec = pushSpec,
            ) + fadeIn(animationSpec = fadeSpec)
        },
        exit = { isForward ->
            val distance = pushPx()
            slideOutHorizontally(
                targetOffsetX = { if (isForward) -distance else distance },
                animationSpec = pushSpec,
            ) + fadeOut(animationSpec = fadeSpec)
        },
    )
