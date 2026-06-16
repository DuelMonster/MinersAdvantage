pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.isxander.dev/releases/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots")
        maven("https://maven.modmuss50.me/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    id("dev.kikugie.stonecutter") version "0.9.3"
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    create(rootProject) {
        fun mc(mcVersion: String, loaders: Iterable<String>) =
            loaders.forEach { version("$mcVersion-$it", mcVersion) }

        mc("1.21.11", listOf("fabric", "neoforge"))
        mc("26.1.2", listOf("fabric", "neoforge"))
        mc("26.2", listOf("fabric", "neoforge"))

        vcsVersion = "1.21.11-fabric"
    }
}

rootProject.name = "minersadvantage"
