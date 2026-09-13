package de.myposter.academy.shaders

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import de.myposter.academy.shaders.slides.agenda
import de.myposter.academy.shaders.slides.backupSlides
import de.myposter.academy.shaders.slides.codeWalkthrough
import de.myposter.academy.shaders.slides.computeUndKi
import de.myposter.academy.shaders.slides.fragen
import de.myposter.academy.shaders.slides.hookDemo
import de.myposter.academy.shaders.slides.hookZahlen
import de.myposter.academy.shaders.slides.identitaet
import de.myposter.academy.shaders.slides.kostenmodell
import de.myposter.academy.shaders.slides.leiter
import de.myposter.academy.shaders.slides.pipeline
import de.myposter.academy.shaders.slides.preis
import de.myposter.academy.shaders.slides.shaderPlayground
import de.myposter.academy.shaders.slides.simt
import de.myposter.academy.shaders.slides.takeaway
import de.myposter.academy.shaders.slides.titel
import de.myposter.academy.shaders.slides.wannLohntEs
import de.myposter.academy.shaders.slides.warumSchnell
import de.myposter.academy.shaders.slides.wasIstEinShader
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import de.myposter.academy.shaders.theme.ShaderTalkTheme
import net.kodein.cup.Presentation
import net.kodein.cup.SlideSpecs
import net.kodein.cup.Slides
import net.kodein.cup.TransitionSet
import net.kodein.cup.imgexp.imageExport
import net.kodein.cup.laser.laser
import net.kodein.cup.speaker.speakerWindow

/** The deck, in order. 18 slides, then the backup slides for the Q&A. */
val talkSlides: Slides = Slides(
    titel,
    hookDemo,
    hookZahlen,
    agenda,
    warumSchnell,
    simt,
    kostenmodell,
    wasIstEinShader,
    shaderPlayground,
    pipeline,
    codeWalkthrough,
    identitaet,
    leiter,
    computeUndKi,
    preis,
    wannLohntEs,
    takeaway,
    fragen,
    backupSlides,
)

/**
 * The slide size of the deck: half of the 1920 × 1080 px storyboard.
 *
 * CuP's own `SLIDE_SIZE_16_9` is 640 × 360 dp. The deck uses 960 × 540 dp so
 * every storyboard value converts by a plain division by two.
 */
val SLIDE_SIZE: DpSize = DpSize(960.dp, 540.dp)

@Composable
fun ShaderTalk() {
    ShaderTalkTheme {
        Presentation(
            slides = talkSlides,
            configuration = {
                speakerWindow()
                laser()
                imageExport()
                // A full SlideSpecs is required: CuP does not merge defaults into it.
                defaultSlideSpecs = SlideSpecs(
                    size = SLIDE_SIZE,
                    startTransitions = TransitionSet.fade,
                    endTransitions = TransitionSet.fade,
                )
            },
            backgroundColor = MaterialTheme.colorScheme.background,
        ) { slidesContent ->
            // The theme must wrap slidesContent, not only the Presentation.
            // CuP composes the slides here, so a theme applied only outside
            // never reaches them and every text falls back to Material's sans.
            ShaderTalkTheme {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onBackground,
                ) {
                    slidesContent()
                }
            }
        }
    }
}
