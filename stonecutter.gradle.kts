// ⚙️ Stonecutter controller: auto-generated-ish file that tracks the active version
// and registers aggregate tasks. Stonecutter regenerates this on sync; manual edits
// to the `stonecutter active` line are overwritten — use the IDE task instead.

plugins {
    id("dev.kikugie.stonecutter")
}

// The currently active development node.
// Switch via the IDE "Stonecutter > Switch version" task or Stonecutter Gradle tasks.
stonecutter {
    active("26.3-fabric")
}


// chiseledBuild: builds ALL registered nodes in one shot.
// Run with:  ./gradlew chiseledBuild
tasks.register("chiseledBuild") {
    group = "project"
    description = "Builds ALL Stonecutter nodes (all loader×version combinations)."
    // stonecutter.tasks.named() is a lazy collection — do NOT call .get() on it directly.
    dependsOn(stonecutter.tasks.named("build"))
}

// chiseledPublishAll: publishes ALL nodes to Modrinth + CurseForge (when tokens present).
// Run with:  ./gradlew chiseledPublishAll
tasks.register("chiseledPublishAll") {
    group = "publishing"
    description = "Publishes ALL Stonecutter nodes to configured platform stores."
    dependsOn(stonecutter.tasks.named("publishMods"))
}

// chiseledPackageRelease: copies the production JAR for every node into releases/.
// Run with:  ./gradlew chiseledPackageRelease
tasks.register("chiseledPackageRelease") {
    group = "release"
    description = "Copies the production JAR for ALL Stonecutter nodes into releases/."
    dependsOn(stonecutter.tasks.named("packageRelease"))
}

tasks.register("chiseledPublishAllPublicationsToLocalRepository") {
    group = "publishing"
    description = "Publishes ALL Stonecutter nodes to the local Maven staging repository."
    dependsOn(stonecutter.tasks.named("publishAllPublicationsToLocalRepository"))
}

// The GitHub Packages repository is only declared when its credentials are present,
// so the aggregate is registered under the same condition.
if (!System.getenv("GITHUB_TOKEN").isNullOrBlank() && !System.getenv("GITHUB_ACTOR").isNullOrBlank()) {
    tasks.register("chiseledPublishAllPublicationsToGitHubPackagesRepository") {
        group = "publishing"
        description = "Publishes ALL Stonecutter nodes to GitHub Packages."
        dependsOn(stonecutter.tasks.named("publishAllPublicationsToGitHubPackagesRepository"))
    }
}

// Shared repository declarations for ALL sub-projects (nodes).
// Loader-specific repos (Fabric, NeoForge) are resolved here; each node
// inherits them via allprojects so their dependency resolution doesn't fail.
allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        // NeoForge Maven — NeoForge and MDG artifacts
        maven("https://maven.neoforged.net/releases")
        // Fabric Maven — Fabric Loader and Fabric API
        maven("https://maven.fabricmc.net/")
        // Xander Maven
        maven("https://maven.isxander.dev/releases")
        // Shedaniel Maven — Cloth Config
        maven("https://maven.shedaniel.me/")
        // TerraformersMC — ModMenu
        maven("https://maven.terraformersmc.com/releases/")
        // SpongePowered — Mixin
        maven("https://repo.spongepowered.org/repository/maven-public")
        // ParchmentMC — human-readable parameter mappings
        maven("https://maven.parchmentmc.org/")
        // Fuzss mod resources — PuzzlesLib etc.
        maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/")
        // mod-publish-plugin
        maven("https://maven.modmuss50.me/")
    }
}
