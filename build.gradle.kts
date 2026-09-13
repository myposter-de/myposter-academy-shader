plugins {
    alias(libs.plugins.spotless)
}

// Amper owns `build/`. Gradle writes next to it, not into it.
layout.buildDirectory = layout.projectDirectory.dir("build/gradle")

spotless {
    kotlin {
        // Targets the source trees directly instead of filtering a whole-project glob with
        // targetExclude: an exclude is applied only after Gradle has walked and stat'ed every
        // build/ directory. This also keeps the generated Compose resources out, since they
        // live under build/.
        target(
            fileTree("shared/src") { include("**/*.kt") },
            fileTree("shared/test") { include("**/*.kt") },
            fileTree("desktopApp/src") { include("**/*.kt") },
        )
        ktfmt(libs.versions.ktfmt.get()).kotlinlangStyle()
    }
}
