plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.21.11-fabric"

tasks.register("chiseledBuild") {
    group = "project"
    description = "Builds all Stonecutter nodes."
    dependsOn(stonecutter.tasks.named("build"))
}

tasks.register("chiseledPublishAll") {
    group = "publishing"
    description = "Publishes all Stonecutter nodes."
    dependsOn(stonecutter.tasks.named("publishMods"))
}

tasks.register("chiseledPackageRelease") {
    group = "release"
    description = "Packages release jars for all Stonecutter nodes."
    dependsOn(stonecutter.tasks.named("packageRelease"))
}

tasks.register("chiseledPublishAllPublicationsToLocalRepository") {
    group = "publishing"
    description = "Publishes all Stonecutter nodes to the local Maven staging repository."
    dependsOn(stonecutter.tasks.named("publishAllPublicationsToLocalRepository"))
}

// The GitHub Packages repository is only declared when its credentials are present,
// so the aggregate is registered under the same condition.
if (!System.getenv("GITHUB_TOKEN").isNullOrBlank() && !System.getenv("GITHUB_ACTOR").isNullOrBlank()) {
    tasks.register("chiseledPublishAllPublicationsToGitHubPackagesRepository") {
        group = "publishing"
        description = "Publishes all Stonecutter nodes to GitHub Packages."
        dependsOn(stonecutter.tasks.named("publishAllPublicationsToGitHubPackagesRepository"))
    }
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.isxander.dev/releases")
        maven("https://maven.terraformersmc.com/releases/")
        maven("https://repo.spongepowered.org/repository/maven-public")
        maven("https://maven.parchmentmc.org/")
        maven("https://maven.modmuss50.me/")
    }
}
