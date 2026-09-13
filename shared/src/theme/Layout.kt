package de.myposter.academy.shaders.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * The frame every slide shares: a meta row, a heading, then the body.
 *
 * @param id the slide name and its number, shown top left.
 * @param timing the minute range and the step count, shown top right.
 * @param title the heading. Pass null for a slide that carries its own.
 */
@Composable
fun SlideFrame(
    id: String,
    timing: String,
    title: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val deck = MaterialTheme.deck
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    start = deck.padH,
                    end = deck.padH,
                    top = deck.padTop,
                    bottom = deck.padBottom,
                )
    ) {
        SlideMeta(id, timing)
        if (title != null) {
            Spacer(Modifier.height(14.dp))
            SlideHeading(title)
            Spacer(Modifier.height(deck.gapTitle))
        }
        content()
    }
}

/**
 * A slide that centers one block on the dark ground: the title, "Fragen?", the takeaway and the
 * backup divider.
 */
@Composable
fun HeroFrame(
    id: String,
    timing: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    DarkSection {
        val deck = MaterialTheme.deck
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(
                        start = deck.padH,
                        end = deck.padH,
                        top = deck.padTop,
                        bottom = deck.padBottom,
                    )
        ) {
            SlideMeta(id, timing, Modifier.align(Alignment.TopStart))
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                content = content,
            )
        }
    }
}

@Composable
fun SlideMeta(id: String, timing: String, modifier: Modifier = Modifier) {
    Text(
        text = id.substringBefore(" · "),
        modifier = modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodySmall,
        fontFamily = monoFamily,
        color = MaterialTheme.colorScheme.outline,
    )
}

@Composable
fun SlideHeading(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

/** The hero title, in Avenir Next Heavy Italic. */
@Composable
fun HeroTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

/** The blue rule above a hero title. */
@Composable
fun AccentRule(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    val deck = MaterialTheme.deck
    Box(modifier.width(deck.ruleWidth).height(deck.ruleHeight).background(color))
}

/**
 * A step of the slide.
 *
 * A step the speaker has not reached yet stays in place and fades to [DeckTokens.dim], the way the
 * storyboard shows it. Nothing jumps.
 */
@Composable
fun Step(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val target = if (visible) 1f else MaterialTheme.deck.dim
    val alpha = animateFloatAsState(target, tween(250), label = "step").value
    Box(modifier.alpha(alpha)) { content() }
}

/**
 * A step that replaces the one before it. The code walkthrough uses it: eight stacked explanations
 * do not fit the frame at slide type sizes.
 */
@Composable
fun SoloStep(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (visible) Box(modifier) { content() }
}

/** The uppercase label above a card or a step. */
@Composable
fun SectionLabel(
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = MaterialTheme.typography.labelLarge,
        color = color,
    )
}

/** A body line of a slide. */
@Composable
fun Body(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
) {
    Text(text, modifier, color = color, style = style)
}

/** A small line of a slide: a caption, a source note, a card body. */
@Composable
fun Caption(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outline,
) {
    Text(text, modifier, color = color, style = MaterialTheme.typography.bodyMedium)
}

/** A monospace line: a shader fragment, a uniform name, a compiler error. */
@Composable
fun Mono(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    Text(text, modifier, color = color, style = style.copy(fontFamily = monoFamily))
}

/** A rounded card. The cost slides, the kostenmodell and the leiter use it. */
@Composable
fun Card(
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.deck.panel,
    padding: androidx.compose.ui.unit.Dp = 24.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(MaterialTheme.deck.cornerL))
                .background(background)
                .padding(padding),
        content = content,
    )
}

/** A dark code block. */
@Composable
fun CodePanel(
    modifier: Modifier = Modifier,
    padding: androidx.compose.ui.unit.Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(MaterialTheme.deck.cornerM))
                .background(MaterialTheme.deck.codeSurface)
                .padding(padding),
        content = content,
    )
}

/** A number and its caption, side by side. "Die Größenordnung" and the checklist use it. */
@Composable
fun BigStat(
    value: String,
    text: String,
    valueColor: Color = MaterialTheme.colorScheme.onBackground,
    valueWidth: androidx.compose.ui.unit.Dp = 310.dp,
    mono: Boolean = false,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
) {
    // Baseline, not center: the number is much larger than its caption. Centered,
    // the caption floats. On the baseline the two sit on one line, as in print.
    Row(modifier.fillMaxWidth()) {
        Text(
            text = value,
            modifier = Modifier.width(valueWidth).alignByBaseline(),
            style =
                MaterialTheme.typography.displayMedium.let {
                    if (mono) it.copy(fontFamily = monoFamily) else it
                },
            color = valueColor,
        )
        Spacer(Modifier.width(20.dp))
        Text(
            text = text,
            modifier = Modifier.alignByBaseline(),
            style = textStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** A centered block. */
@Composable
fun CenteredBlock(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content,
    )
}

/** Fills the rest of the slide and centers its content vertically. */
@Composable
fun ColumnScope.Fill(
    spacedBy: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.weight(1f).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacedBy, Alignment.CenterVertically),
        content = content,
    )
}

/** A square that stands for one GPU thread. */
@Composable
fun ThreadCell(active: Boolean, size: androidx.compose.ui.unit.Dp = 20.dp) {
    Box(
        Modifier.width(size)
            .height(size)
            .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.deck.idle)
    )
}

/** A row of thread cells. */
@Composable
fun ThreadRow(pattern: List<Boolean>, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        pattern.forEach { ThreadCell(it) }
    }
}

/** A short note in the middle of an empty image slot. */
@Composable
fun CenterText(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            textAlign = TextAlign.Center,
        )
    }
}
