plugins {
    id("dev.isxander.modstitch.base") version "0.8.5"
    id("me.modmuss50.mod-publish-plugin") version "0.8.4" apply false
    id("maven-publish")
}

fun prop(name: String, consumer: (String) -> Unit) {
    (findProperty(name) as? String)?.let(consumer)
}

val minecraft = property("deps.minecraft") as String
val loader = name.substringAfterLast("-")
val isNeoForge = loader == "neoforge"
val javaRelease = if (minecraft.startsWith("26.")) 25 else 21

modstitch {
    minecraftVersion = minecraft

    metadata {
        modId = "minersadvantage"
        modName = "Miners Advantage"
        modVersion = property("mod_version") as String
        modGroup = "uk.co.duelmonster"
        modAuthor = "DuelMonster"
        replacementProperties.putAll(
            mapOf(
                "mod_id" to "minersadvantage",
                "mod_name" to "Miners Advantage",
                "mod_description" to "Modernized rewrite of Miners Advantage for Fabric and NeoForge.",
                "mod_license" to "MIT",
                "mod_author" to "DuelMonster",
                "mod_homepage" to "https://github.com/duelmonster/MinersAdvantage",
                "mod_issue_tracker" to "https://github.com/duelmonster/MinersAdvantage/issues",
                "minecraft_version_range" to "[1.21.11,)",
                "yacl_version_range" to "[3.8.2,)",
                "neoforge_loader_range" to "[10,)"
            )
        )
    }

    loom {
        fabricLoaderVersion = property("deps.fabric_loader") as String
    }

    moddevgradle {
        prop("deps.neoforge") { neoForgeVersion = it }
        defaultRuns()
    }

    mixin {
        addMixinsToModManifest = true
        configs.register("minersadvantage")
    }
}

stonecutter {
    constants.match(loader, "fabric", "neoforge")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaRelease))
    }
}

tasks.withType<JavaCompile> {
    options.release.set(javaRelease)
    dependsOn("stonecutterGenerate")
}

dependencies {
    modstitch.loom {
        modstitchModImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
        modstitchModImplementation("dev.isxander:yet-another-config-lib:${property("deps.yacl")}-fabric") {
            exclude(group = "net.fabricmc.fabric-api")
        }
        modstitchModImplementation("com.terraformersmc:modmenu:${property("deps.modmenu")}")
    }

    modstitch.moddevgradle {
        modstitchModImplementation("dev.isxander:yet-another-config-lib:${property("deps.yacl")}-neoforge")
    }

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

version = "${property("mod_version")}+${minecraft}-${loader}"
base.archivesName.set("MinersAdvantage_${property("mod_version")}+${minecraft}-${loader}")

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "uk.co.duelmonster"
            artifactId = "minersadvantage-${loader}"
            version = "${property("mod_version")}+${minecraft}"
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "local"
            url = uri(rootProject.layout.buildDirectory.dir("maven-local"))
        }
    }
}

val prodJarTask: String = if (tasks.findByName("remapJar") != null) "remapJar" else "jar"

val cleanReleasesTask = if (rootProject.tasks.findByName("cleanReleases") == null) {
    rootProject.tasks.register("cleanReleases") {
        group = "release"
        description = "Deletes all jar files from releases before packaging."
        doLast {
            val releasesDir = rootProject.layout.projectDirectory.dir("releases").asFile
            releasesDir.mkdirs()
            releasesDir.listFiles { f -> f.isFile && f.extension == "jar" }?.forEach { it.delete() }
        }
    }
} else {
    rootProject.tasks.named("cleanReleases")
}

tasks.register<Copy>("packageRelease") {
    group = "release"
    description = "Copies the production jar for this node into releases."
    dependsOn(prodJarTask, cleanReleasesTask)
    from(tasks.named<AbstractArchiveTask>(prodJarTask).map { it.archiveFile })
    into(rootProject.layout.projectDirectory.dir("releases"))
}

tasks.named("build") {
    finalizedBy("packageRelease")
}

if (isNeoForge) {
    tasks.configureEach {
        if (name == "neoForgeIdeSync") {
            enabled = false
        }
    }
}
