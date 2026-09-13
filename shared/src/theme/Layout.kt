package de.myposter.academy.shaders.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** The standard frame: a title at the top, the content below. */
@Composable
fun ColumnScope.TitledSlide(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        SlideTitle(title)
        Spacer(Modifier.height(20.dp))
        content()
    }
}

@Composable
fun SlideTitle(text: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            text = text,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Palette.OnBackground,
        )
        Spacer(Modifier.height(6.dp))
        Box(Modifier.width(56.dp).height(3.dp).background(Palette.Accent))
    }
}

/** A body line of a slide. */
@Composable
fun Body(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Palette.OnBackground,
    size: Int = 17,
) {
    Text(text, modifier, color = color, fontSize = size.sp, lineHeight = (size * 1.45).sp)
}

@Composable
fun Body(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Palette.OnBackground,
    size: Int = 17,
) {
    Text(text, modifier, color = color, fontSize = size.sp, lineHeight = (size * 1.45).sp)
}

@Composable
fun Muted(text: String, modifier: Modifier = Modifier, size: Int = 14) {
    Text(text, modifier, color = Palette.Muted, fontSize = size.sp, lineHeight = (size * 1.45).sp)
}

/** A step that fades in. Use it for every progressive reveal of the deck. */
@Composable
fun Reveal(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(350)),
        exit = fadeOut(tween(200)),
    ) {
        content()
    }
}

/** A boxed statement. Use it for the key sentence of a slide. */
@Composable
fun Statement(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = Palette.Accent,
    size: Int = 20,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Palette.Surface)
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(4.dp).height((size * 3).dp).background(accent))
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            color = Palette.OnBackground,
            fontSize = size.sp,
            lineHeight = (size * 1.4).sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

/** A labelled promise that a later slide pays back. */
@Composable
fun Promise(number: Int, text: String, modifier: Modifier = Modifier, paid: Boolean = false) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Palette.Surface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (paid) "Versprechen $number ✓" else "Versprechen $number",
            color = if (paid) Palette.Gpu else Palette.Accent,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.width(14.dp))
        Text(text, color = Palette.OnBackground, fontSize = 17.sp, lineHeight = 24.sp)
    }
}

/** A centered quote. Use it for the title and the takeaway. */
@Composable
fun ColumnScope.CenteredBlock(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 56.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content,
    )
}

@Composable
fun BigNumber(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontSize = 34.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Palette.Muted, fontSize = 13.sp, textAlign = TextAlign.Center)
    }
}
