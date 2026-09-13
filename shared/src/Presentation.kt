package de.myposter.academy.shaders

import androidx.compose.material3.LocalContentColor
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
import de.myposter.academy.shaders.theme.Palette
import de.myposter.academy.shaders.theme.ShaderTalkTheme
import net.kodein.cup.Presentation
import net.kodein.cup.SLIDE_SIZE_16_9
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
                    size = SLIDE_SIZE_16_9,
                    startTransitions = TransitionSet.fade,
                    endTransitions = TransitionSet.fade,
                )
            },
            backgroundColor = Palette.Background,
        ) { slidesContent ->
            CompositionLocalProvider(LocalContentColor provides Palette.OnBackground) {
                slidesContent()
            }
        }
    }
}
