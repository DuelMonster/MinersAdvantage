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
