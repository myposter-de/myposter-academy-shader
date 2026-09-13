// Gradle formats. Amper builds.
//
// The project builds with the Kotlin Toolchain (Amper), see `project.yaml`. This
// Gradle build declares no source set and no module. It exists because Spotless
// ships as a Gradle plugin and as a Maven plugin, and nothing else.
//
// Run `./gradlew spotlessApply` to format. Run `./kotlin build` to compile.

rootProject.name = "myposter-academy-shader"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}
