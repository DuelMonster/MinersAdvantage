import java.io.File

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
val clothConfigVersion = when ("$minecraft-$loader") {
    "1.21.11-fabric" -> "21.11.153"
    "1.21.11-neoforge" -> "21.11.153"
    "26.1.2-fabric" -> "26.1.154"
    "26.1.2-neoforge" -> "26.1.154"
    else -> error("No Cloth Config version mapping for $minecraft-$loader")
}

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
                "cloth_config_version_range" to "[$clothConfigVersion,)",
                "neoforge_loader_range" to "[10,)"
            )
        )
    }

    loom {
        fabricLoaderVersion = property("deps.fabric_loader") as String

        configureLoom {
            runs.named("client") {
                setConfigName("Fabric Client")
                ideConfigGenerated(false)
                runDir("runs/client")
                programArgs("--username", "DuelMonster", "--width", "1960", "--height", "1080")
            }
            runs.named("server") {
                setConfigName("Fabric Server")
                ideConfigGenerated(false)
                runDir("runs/server")
            }
        }
    }

    moddevgradle {
        prop("deps.neoforge") { neoForgeVersion = it }
        defaultRuns()

        configureNeoForge {
            runs {
                named("client") {
                    gameDirectory = project.file("runs/client")
                    programArgument("--username")
                    programArgument("DuelMonster")
                    programArgument("--width")
                    programArgument("1960")
                    programArgument("--height")
                    programArgument("1080")
                }
                named("server") {
                    gameDirectory = project.file("runs/server")
                }
            }

            val parchmentMc = findProperty("deps.parchment_mc") as? String
            val parchmentMappings = findProperty("deps.parchment") as? String
            if (!parchmentMc.isNullOrBlank() && !parchmentMappings.isNullOrBlank()) {
                parchment {
                    minecraftVersion = parchmentMc
                    mappingsVersion = parchmentMappings
                }
            }
        }
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

tasks.matching { it.name == "createMinecraftArtifacts" }.configureEach {
    dependsOn("stonecutterGenerate")
}

if (tasks.findByName("compile") == null) {
    tasks.register("compile") {
        group = "build"
        description = "Compiles main Java sources (alias of compileJava)."
        dependsOn("compileJava")
    }
}

tasks.register("copyRelatedMods") {
    group = "run"
    description = "Copy related_mods jars to run directory mods folders."

    doLast {
        val disableRelatedMods =
            ((findProperty("dmNoRelatedMods") as? String)?.toBoolean() == true)
        if (disableRelatedMods) {
            return@doLast
        }

        val actualRoot = rootProject.projectDir
        val nodeVersionDir = project.projectDir
        val projectLoader = project.name.substringAfterLast("-")
        val projectVersion = project.name.substringBeforeLast("-")
        val loaderModsDir = File(actualRoot, "related_mods/$projectLoader/$projectVersion")

        if (loaderModsDir.exists() && loaderModsDir.isDirectory) {
            val clientModsDir = File(nodeVersionDir, "runs/client/mods")
            val serverModsDir = File(nodeVersionDir, "runs/server/mods")

            clientModsDir.mkdirs()
            serverModsDir.mkdirs()

            val jarFiles = loaderModsDir.listFiles { file: File -> file.isFile && file.extension == "jar" }
            jarFiles?.forEach { jar: File ->
                jar.copyTo(File(clientModsDir, jar.name), overwrite = true)
                jar.copyTo(File(serverModsDir, jar.name), overwrite = true)
            }
        }
    }
}

afterEvaluate {
    tasks.matching { it.name.matches(Regex("run.*")) }.configureEach {
        dependsOn("copyRelatedMods")
    }
}

dependencies {
    modstitch.loom {
        modstitchModImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
        modstitchModImplementation("me.shedaniel.cloth:cloth-config-fabric:$clothConfigVersion") {
            exclude(group = "net.fabricmc.fabric-api")
        }
        modstitchModImplementation("com.terraformersmc:modmenu:${property("deps.modmenu")}")
    }

    modstitch.moddevgradle {
        modstitchModImplementation("me.shedaniel.cloth:cloth-config-neoforge:$clothConfigVersion")
    }

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.slf4j:slf4j-api:2.0.16")
    testRuntimeOnly("com.google.code.gson:gson:2.11.0")
    if (isNeoForge) {
        testRuntimeOnly("net.neoforged:neoforge:${property("deps.neoforge")}")
    }
}

repositories {
    maven("https://maven.shedaniel.me/")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

if (isNeoForge) {
    tasks.withType<Test>().configureEach {
        dependsOn("createMinecraftArtifacts")
        doFirst {
            val moddevArtifacts = fileTree(layout.buildDirectory.dir("moddev/artifacts")) {
                include("*.jar")
            }
            classpath += files(moddevArtifacts)
        }
    }
}

if (isNeoForge) {
    tasks.configureEach {
        if (name == "neoForgeIdeSync") {
            enabled = false
        }
    }
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

        val modrinthToken = System.getenv("MODRINTH_TOKEN")
        if (!modrinthToken.isNullOrBlank()) {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
                credentials {
                    username = "minersadvantage"
                    password = modrinthToken
                }
            }
        }

        val ghToken = System.getenv("GITHUB_TOKEN")
        val ghActor = System.getenv("GITHUB_ACTOR")
        if (!ghToken.isNullOrBlank() && !ghActor.isNullOrBlank()) {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/duelmonster/MinersAdvantage")
                credentials {
                    username = ghActor
                    password = ghToken
                }
            }
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
