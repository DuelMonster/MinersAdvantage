// 🏗️ Central build script: runs once for every Stonecutter version node (1.21.11-fabric,
// 1.21.11-neoforge, etc.). Modstitch reads the node's gradle.properties to decide which
// platform toolchain (Loom vs ModDevGradle) to activate. One script to rule them all.

import java.io.DataInputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.util.Comparator
import java.util.zip.ZipFile

plugins {
    id("net.neoforged.moddev") version "2.0.147" apply false
    // Modstitch: the unified build plugin that abstracts Fabric Loom and NeoForge MDG.
    // Only one platform is "active" per node — determined by modstitch.platform in
    // the node's versioned gradle.properties.
    id("dev.isxander.modstitch.base") version "0.8.5"
    // mod-publish-plugin: Modrinth + CurseForge publishing (applied conditionally below)
    id("me.modmuss50.mod-publish-plugin") version "2.2.0" apply false
    // Maven publish (standard Gradle plugin — no version needed)
    id("maven-publish")
}

// ────────────────────────────────────────────────────────────
//  Helper: read a versioned property and pass it to a consumer
//  only if it exists. Keeps the block below readable.
// ────────────────────────────────────────────────────────────
fun prop(name: String, consumer: (prop: String) -> Unit) {
    (findProperty(name) as? String?)?.let(consumer)
}

// The Minecraft version for this node (e.g. "1.21.11")
val minecraft = property("deps.minecraft") as String
val mcVersion = minecraft.substringBefore(".").toInt()
// Which loader is this node for? Extracted from the project name (e.g. "1.21.11-fabric" → "fabric")
val loader = name.substringAfterLast("-")
val isFabric = loader == "fabric"
val isNeoForge = loader == "neoforge"
val javaRelease = if (mcVersion >= 26) 25 else 21
val clothConfigVersion = when ("$minecraft-$loader") {
    "1.21.11-fabric" -> "21.11.153"
    "1.21.11-neoforge" -> "21.11.153"
    "26.1.2-fabric" -> "26.1.154"
    "26.1.2-neoforge" -> "26.1.154"
    "26.2-fabric" -> "26.2.155"
    "26.2-neoforge" -> "26.2.155"
    "26.3-fabric" -> "26.3.159"
    "26.3-neoforge" -> "26.3.159"
    else -> error("No Cloth Config version mapping for $minecraft-$loader")
}

// ────────────────────────────────────────────────────────────
//  Modstitch core configuration
// ────────────────────────────────────────────────────────────
modstitch {
    minecraftVersion = minecraft

    // ── Metadata: populates fabric.mod.json and neoforge.mods.toml templates ──
    metadata {
        modId = "minersadvantage"
        modName = "MinersAdvantage"
        modVersion = property("mod_version") as String
        modGroup = "uk.co.duelmonster"
        modAuthor = "DuelMonster"
        replacementProperties.putAll(
            mapOf(
                "mod_id" to "minersadvantage",
                "mod_name" to "MinersAdvantage",
                "mod_description" to "Modernized rewrite of MinersAdvantage for Fabric and NeoForge.",
                "mod_license" to "MIT",
                "mod_author" to "DuelMonster",
                "mod_homepage" to "https://github.com/duelmonster/MinersAdvantage",
                "mod_issue_tracker" to "https://github.com/duelmonster/MinersAdvantage/issues",
            // Per-node ranges — must vary per Stonecutter node or the packaged
            // neoforge.mods.toml (and thus the built jar) is byte-identical across nodes.
                "minecraft_version_range" to "[$minecraft,)",
                "cloth_config_version_range" to "[$clothConfigVersion,)",
                "neoforge_loader_range" to "[10,)"
            )
        )
    }

    // ── Fabric Loom platform (active when modstitch.platform=loom) ──
    loom {
        // Fabric Loader version — intentionally not stored in versioned properties
        // since Fabric Loader is largely version-independent and rarely needs a per-MC pin.
        fabricLoaderVersion = property("deps.fabric_loader") as String

        configureLoom {
            // Runs for development (matches the VS Code task setup the project already uses)
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

    // ── NeoForge ModDevGradle platform (active when modstitch.platform=moddevgradle) ──
    moddevgradle {
        prop("deps.neoforge") { neoForgeVersion = it }

        // Registers the default client + server run configurations.
        defaultRuns()

        configureNeoForge {
            // Match the Fabric layout so both loaders use the same run-dir convention.
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

    // ── Mixin configuration ──
    mixin {
        // Modstitch's FletchingTable automatically inserts the mixin config file references
        // into fabric.mod.json ("mixins" array) and neoforge.mods.toml ([[mixins]]).
        // No manual manifest edits needed.
        addMixinsToModManifest = true

        // One unified mixin config covers both platforms.
        // Platform-specific mixin classes (MixinTitleScreen) live in the shared package;
        // loader-only mixins can be registered here with isLoom/isModDevGradle guards.
        configs.register("minersadvantage")
    }
}

// ────────────────────────────────────────────────────────────
//  Stonecutter loader constants
//  These constants let the Stitcher comment processor know which
//  loader is active, so //? if fabric { … } blocks work correctly.
// ────────────────────────────────────────────────────────────
stonecutter {
    constants.match(loader, "fabric", "neoforge")
    constants.match(if (minecraft.startsWith("1.21.")) "mc1" else "mc26", "mc1", "mc26")

    replacements.string(current.parsed >= "26.3", "mc26_3_api") {
        replace("return item instanceof AxeItem;", "return item.getDefaultInstance().is(ItemTags.AXES);")
        replace("return item instanceof HoeItem;", "return item.getDefaultInstance().is(ItemTags.HOES);")
        replace("return item instanceof ShovelItem;", "return item.getDefaultInstance().is(ItemTags.SHOVELS);")
        replace("import net.minecraft.world.item.AxeItem;", "import net.minecraft.tags.ItemTags;")
        replace("import net.minecraft.world.item.HoeItem;", "// HoeItem is absent on 26.3.")
        replace("import net.minecraft.world.item.ShovelItem;", "// ShovelItem is absent on 26.3.")
        replace("InputConstants.isKeyDown(window, ", "InputConstants.isKeyDown(")
        replace("org.lwjgl.glfw.GLFW.GLFW_KEY_0", "InputConstants.KEY_0")
        replace("org.lwjgl.glfw.GLFW.GLFW_KEY_2", "InputConstants.KEY_2")
        replace("org.lwjgl.glfw.GLFW.GLFW_KEY_7", "InputConstants.KEY_7")
        replace("org.lwjgl.glfw.GLFW.GLFW_KEY_8", "InputConstants.KEY_8")
        replace("org.lwjgl.glfw.GLFW.GLFW_KEY_KP_0", "InputConstants.KEY_NUMPAD0")
        replace("org.lwjgl.glfw.GLFW.GLFW_KEY_KP_2", "InputConstants.KEY_NUMPAD2")
        replace("org.lwjgl.glfw.GLFW.GLFW_KEY_KP_7", "InputConstants.KEY_NUMPAD7")
        replace("org.lwjgl.glfw.GLFW.GLFW_KEY_KP_8", "InputConstants.KEY_NUMPAD8")
        replace("com.mojang.blaze3d.pipeline.", "com.mojang.renderpearl.api.pipeline.")
        replace("com.mojang.blaze3d.platform.CompareOp", "com.mojang.renderpearl.api.pipeline.CompareOp")
        replace("com.mojang.blaze3d.vertex.VertexFormat", "com.mojang.renderpearl.api.vertex.VertexFormat")
        replace("import net.minecraft.client.renderer.rendertype.OutputTarget;", "// OutputTarget was removed in 26.3.")
        replace(".setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)", "/* OutputTarget was removed in 26.3. */")
    }
}

// Java bytecode target per release line.
// 1.21.x stays on Java 21; 26.x requires Java 25.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaRelease))
    }
}

val generatedModMetadataDir = layout.buildDirectory.dir("generated/sources/maVersionConstants/java/main")
val modVersionForGeneratedMetadata = providers.gradleProperty("mod_version").get()

val generateModMetadataSource = tasks.register("generateModMetadataSource") {
    val outDir = generatedModMetadataDir.get().asFile
    outputs.dir(outDir)
    doLast {
        val packageDir = File(outDir, "uk/co/duelmonster/minersadvantage")
        packageDir.mkdirs()
        val generatedFile = File(packageDir, "GeneratedModMetadata.java")
        generatedFile.writeText(
            """
            package uk.co.duelmonster.minersadvantage;

            /**
             * Build-generated metadata constants sourced from Gradle properties.
             */
            public final class GeneratedModMetadata {
                public static final String MOD_VERSION = "$modVersionForGeneratedMetadata";

                private GeneratedModMetadata() {
                }
            }
            """.trimIndent() + "\n"
        )
    }
}

sourceSets {
    named("main") {
        java.srcDir(generatedModMetadataDir)
        java.exclude(
            if (isFabric) "**/platform/neoforge/**" else "**/platform/fabric/**"
        )
    }
}

tasks.withType<JavaCompile> {
    options.release.set(javaRelease)
    dependsOn(generateModMetadataSource)
    dependsOn("stonecutterGenerate")
}

tasks.matching { it.name == "createMinecraftArtifacts" }.configureEach {
    dependsOn("stonecutterGenerate")
}

// Verify that the built production jar uses the expected class file major version:
// Java 21 -> 65, Java 25 -> 69.
tasks.register("verifyJarBytecode") {
    group = "verification"
    description = "Verifies that production jar bytecode matches the expected Java target for this node."

    val productionJarTaskName = if (tasks.findByName("remapJar") != null) "remapJar" else "jar"
    dependsOn(productionJarTaskName)

    doLast {
        val jarTask = tasks.named<org.gradle.jvm.tasks.Jar>(productionJarTaskName).get()
        val jarFile = jarTask.archiveFile.get().asFile
        check(jarFile.exists()) { "Expected production jar not found: ${jarFile.absolutePath}" }

        val expectedMajor = javaRelease + 44
        var checkedClassCount = 0

        ZipFile(jarFile).use { zip ->
            val entries = zip.entries().asSequence().filter { !it.isDirectory && it.name.endsWith(".class") }
            entries.forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    val data = DataInputStream(input)
                    val magic = data.readInt()
                    check(magic == 0xCAFEBABE.toInt()) { "Invalid class header in ${entry.name} (${jarFile.name})" }
                    data.readUnsignedShort() // minor version
                    val major = data.readUnsignedShort()
                    check(major == expectedMajor) {
                        "Unexpected class file major version in ${entry.name}: got $major, expected $expectedMajor (${jarFile.name})"
                    }
                    checkedClassCount++
                }
            }
        }

        check(checkedClassCount > 0) { "No .class files found in ${jarFile.name}" }
        logger.lifecycle("Verified ${jarFile.name}: $checkedClassCount classes at major version $expectedMajor")
    }
}

if (tasks.findByName("compile") == null) {
    tasks.register("compile") {
        group = "build"
        description = "Compiles main Java sources (alias of compileJava)."
        dependsOn("compileJava")
    }
}

// ────────────────────────────────────────────────────────────
//  Task: copyRelatedMods (copy dependency mods to run directories)
//  This copies jars from related_mods/{loader}/ into runs/client/mods
//  and runs/server/mods so that the development environment has the
//  required mod dependencies available.
// ────────────────────────────────────────────────────────────
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

            // Ensure directories exist
            clientModsDir.mkdirs()
            serverModsDir.mkdirs()

            // Copy all jars from the loader-specific related_mods directory to both run directories
            val jarFiles = loaderModsDir.listFiles { file: File -> file.isFile && file.extension == "jar" }
            jarFiles?.forEach { jar: File ->
                jar.copyTo(File(clientModsDir, jar.name), overwrite = true)
                jar.copyTo(File(serverModsDir, jar.name), overwrite = true)
            }
        }
    }
}

// Configure dependencies after all tasks are created (delayed configuration)
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

    fun sanitizeVscodeLaunchJsonFile() {
        val launchFile = rootProject.file(".vscode/launch.json")
        if (!launchFile.exists()) return

        try {
            val original = launchFile.readText()
            val parsed = groovy.json.JsonSlurper().parseText(original)
            val root = parsed as? MutableMap<*, *> ?: return
            val configurations = root["configurations"] as? List<*> ?: return

            val filtered = configurations.filterNot { entry ->
                val mapEntry = entry as? Map<*, *> ?: return@filterNot false
                val name = mapEntry["name"] as? String ?: return@filterNot false
                name.startsWith("NeoForge ")
            }

            if (filtered.size != configurations.size) {
                val mutableRoot = root.toMutableMap()
                mutableRoot["configurations"] = filtered
                val cleaned = groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(mutableRoot))
                launchFile.writeText(cleaned + System.lineSeparator())
            }
        } catch (_: Exception) {
            // Never fail a build because of launch.json sanitization.
        }
    }

    tasks.register("sanitizeVscodeLaunchJson") {
        group = "ide"
        description = "Removes auto-generated NeoForge launch profiles from .vscode/launch.json."

        doLast {
            sanitizeVscodeLaunchJsonFile()
        }
    }

    tasks.matching {
        it.name == "neoForgeIdeSync" ||
        it.name == "prepareClientRun" ||
        it.name == "prepareServerRun"
    }.configureEach {
        finalizedBy("sanitizeVscodeLaunchJson")
    }

    gradle.buildFinished {
        sanitizeVscodeLaunchJsonFile()
    }
}

// ────────────────────────────────────────────────────────────
//  Version string for the built artifact
//  e.g.  0.15.0+1.21.11-fabric
// ────────────────────────────────────────────────────────────
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
        // Local Maven repository (always available, no token required)
        maven {
            name = "local"
            url = uri(rootProject.layout.buildDirectory.dir("maven-local"))
        }

        // Modrinth Maven (publish JARs as Maven artifacts for dependency use)
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

        // GitHub Packages (optional — needs GITHUB_ACTOR + GITHUB_TOKEN env vars)
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

// ────────────────────────────────────────────────────────────
//  Modrinth + CurseForge publishing via mod-publish-plugin
//  Only applied when the publishing API keys are available.
//  Tasks do NOT run during a normal build; invoke them explicitly:
//    ./gradlew :versions/1.21.11-fabric:publishModrinth
//    ./gradlew :versions/1.21.11-neoforge:publishCurseForge
//    ./gradlew chiseledPublishAll   (publishes all nodes)
// ────────────────────────────────────────────────────────────

// Determine the production JAR task name for this node.
// Older Fabric Loom nodes expose "remapJar" as the final artifact; newer ones
// (and all NeoForge nodes) use plain "jar". Check at configuration time so that
// both old and new Loom versions are handled correctly.
val prodJarTask: String = if (tasks.findByName("remapJar") != null) "remapJar" else "jar"

// `mod_version` can be bumped several times between publishes, so release notes must span every
// CHANGELOG section newer than the version already live on the store, not just the newest one.
fun changelogSectionsSince(full: String, publishedVersion: String?): String {
    val lines = full.lines()
    val headings = lines.indices.filter { lines[it].startsWith("## ") }
    if (headings.isEmpty()) return full.trim()
    val newestOnly = headings.getOrElse(1) { lines.size }
    val stop = publishedVersion
        ?.let { published -> headings.firstOrNull { lines[it].removePrefix("## ").trim() == published } }
        ?.takeIf { it > headings.first() }
        ?: newestOnly
    return lines.subList(headings.first(), stop).joinToString("\n").trim()
}

fun compareModVersions(left: String, right: String): Int {
    fun parts(v: String) = v.split('.').map { it.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }
    val a = parts(left)
    val b = parts(right)
    for (i in 0 until maxOf(a.size, b.size)) {
        val cmp = a.getOrElse(i) { 0 }.compareTo(b.getOrElse(i) { 0 })
        if (cmp != 0) return cmp
    }
    return 0
}

// Modrinth's public version list is the only queryable source for the live version: the CurseForge
// upload API accepts uploads but cannot be queried with the upload token.
fun fetchPublishedModVersion(projectId: String): String? {
    if (projectId.isBlank()) return null
    return runCatching {
        val connection = URI("https://api.modrinth.com/v2/project/$projectId/version")
            .toURL().openConnection() as HttpURLConnection
        connection.connectTimeout = 5_000
        connection.readTimeout = 5_000
        connection.setRequestProperty("User-Agent", "MinersAdvantage/publish")
        val body = try {
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
        Regex("\"version_number\"\\s*:\\s*\"([^\"]+)\"").findAll(body)
            .map { it.groupValues[1].substringBefore('+') }
            .distinct()
            .toList()
            .maxWithOrNull(Comparator { a: String, b: String -> compareModVersions(a, b) })
    }.getOrNull()
}

// Modrinth + CurseForge publishing via mod-publish-plugin.
// Tasks are configured only when publishing tokens are available.
val modrinthToken = System.getenv("MODRINTH_TOKEN")
val curseForgeToken = System.getenv("CURSEFORGE_TOKEN")

tasks.register("publishPreflight") {
    group = "publishing"
    description = "Prints publish token detection before running publish tasks."
    doLast {
        val hasModrinthToken = !modrinthToken.isNullOrBlank()
        val hasCurseForgeToken = !curseForgeToken.isNullOrBlank()
        logger.lifecycle("Publish preflight for ${project.path}:")
        logger.lifecycle("- MODRINTH_TOKEN detected: $hasModrinthToken")
        logger.lifecycle("- CURSEFORGE_TOKEN detected: $hasCurseForgeToken")
        if (!hasModrinthToken && !hasCurseForgeToken) {
            logger.lifecycle("- No publish tokens detected; publish tasks will be no-op.")
        }
    }
}

tasks.register("publishPostSummary") {
    group = "publishing"
    description = "Prints release jars and publish destinations after publish tasks complete."
    doLast {
        val publishArtifact = tasks.named<AbstractArchiveTask>(prodJarTask).get().archiveFile.get().asFile

        val destinations = mutableListOf<String>()
        if (!modrinthToken.isNullOrBlank()) {
            val projectId = findProperty("modrinth_project_id") as? String ?: ""
            destinations += "Modrinth (project: $projectId)"
        }
        if (!curseForgeToken.isNullOrBlank()) {
            val projectId = findProperty("curseforge_project_id") as? String ?: ""
            destinations += "CurseForge (project: $projectId)"
        }
        if (destinations.isEmpty()) {
            destinations += "No external publish target (token(s) missing)"
        }

        logger.lifecycle("Publish summary for ${project.path}:")
        if (publishArtifact.exists()) {
            logger.lifecycle("- Published artifact: ${publishArtifact.name}")
        } else {
            logger.lifecycle("- Published artifact (configured): ${publishArtifact.name}")
            logger.lifecycle("  - Local file not found at: ${publishArtifact.absolutePath}")
        }
        logger.lifecycle("- Published to: ${destinations.joinToString(", ")}")
    }
}

tasks.configureEach {
    if (name.matches(Regex("(?i)^publish(mods|modrinth|curseforge)$"))) {
        dependsOn("publishPreflight")
        finalizedBy("publishPostSummary")
    }
}

if (!modrinthToken.isNullOrBlank() || !curseForgeToken.isNullOrBlank()) {
    apply(plugin = "me.modmuss50.mod-publish-plugin")

    val modrinthProjectId = findProperty("modrinth_project_id") as? String ?: ""
    // Resolved once per build and shared across all Stonecutter nodes.
    val publishedCacheKey = "minersAdvantage.publishedModVersion"
    val publishedVersion = if (rootProject.extra.has(publishedCacheKey)) {
        rootProject.extra[publishedCacheKey] as String
    } else {
        fetchPublishedModVersion(modrinthProjectId).orEmpty().also {
            rootProject.extra[publishedCacheKey] = it
            if (it.isBlank()) {
                logger.lifecycle("[publish] Could not resolve the published version from Modrinth; using the newest CHANGELOG section only.")
            } else {
                logger.lifecycle("[publish] Last published version on Modrinth: $it")
            }
        }
    }.ifBlank { null }

    val releaseChangelog: Provider<String> =
        providers.fileContents(rootProject.layout.projectDirectory.file("CHANGELOG.md"))
            .asText.map { changelogSectionsSince(it, publishedVersion) }.orElse("")

    tasks.register("printReleaseChangelog") {
        group = "publishing"
        description = "Prints the release notes that would be uploaded to Modrinth and CurseForge."
        doLast { println(releaseChangelog.get()) }
    }

    @Suppress("UnstableApiUsage")
    configure<me.modmuss50.mpp.ModPublishExtension> {
        val modVer = property("mod_version") as String

        if (!modrinthToken.isNullOrBlank()) {
            modrinth {
                accessToken = modrinthToken
                projectId = findProperty("modrinth_project_id") as? String ?: ""
                minecraftVersions.add(minecraft)
                modLoaders.add(loader)
                displayName = "MinersAdvantage $modVer+$minecraft-$loader"
                version = "$modVer+$minecraft-$loader"
                type = me.modmuss50.mpp.ReleaseType.STABLE
                file = tasks.named<AbstractArchiveTask>(prodJarTask).map { it.archiveFile.get() }
                changelog = releaseChangelog
                requires("cloth-config")
            }
        }

        if (!curseForgeToken.isNullOrBlank()) {
            curseforge {
                accessToken = curseForgeToken
                projectId = findProperty("curseforge_project_id") as? String ?: ""
                minecraftVersions.add(minecraft)
                modLoaders.add(loader)
                // CurseForge now requires an environment selection in addition to version/loader.
                client.set(true)
                server.set(true)
                displayName = "MinersAdvantage $modVer+$minecraft-$loader"
                version = "$modVer+$minecraft-$loader"
                type = me.modmuss50.mpp.ReleaseType.STABLE
                file = tasks.named<AbstractArchiveTask>(prodJarTask).map { it.archiveFile.get() }
                changelog = releaseChangelog
                requires("cloth-config")
            }
        }
    }
}

if (tasks.findByName("publishMods") == null) {
    tasks.register("publishMods") {
        group = "publishing"
        description = "No-op local publish task when MODRINTH_TOKEN/CURSEFORGE_TOKEN are missing."
        doLast {
            logger.lifecycle("Skipping publishMods: no publishing tokens configured.")
        }
    }
}

// ────────────────────────────────────────────────────────────
//  Release-packaging task (kept for backward compatibility with
//  the CI release workflow — copies the final JAR to releases/)
// ────────────────────────────────────────────────────────────

// Register cleanReleases on the root project once (the first node to configure
// creates it; subsequent nodes simply look it up). This guarantees it runs
// exactly once — before any node copies its JAR into releases/.
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
