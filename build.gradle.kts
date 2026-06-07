plugins {
    id("dev.kikugie.loom-back-compat")
    id("maven-publish")
}

version = "${property("mod.version")}+mc${sc.current.version}"
group = property("mod.group") as String
base.archivesName = property("mod.id") as String

repositories {
    // Loom adds the essential Minecraft/Fabric repos automatically.
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://maven.shedaniel.me/")
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    mappings("net.fabricmc:yarn:${property("deps.yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
    modImplementation("com.terraformersmc:modmenu:${property("deps.modmenu")}")
}

loom {
    splitEnvironmentSourceSets()

    mods {
        create("ricoshot") {
            sourceSet(sourceSets["main"])
            sourceSet(sourceSets["client"])
        }
    }

    runs {
        named("server") {
            server()
            name("Minecraft Server")
            runDir("run/server")
        }
        create("client1") {
            client()
            name("Minecraft Client 1")
            runDir("run/client1")
            programArgs("--username", "Player1")
        }
        create("client2") {
            client()
            name("Minecraft Client 2")
            runDir("run/client2")
            programArgs("--username", "Player2")
        }
    }
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
}

tasks.withType<Jar>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.processResources {
    val version = project.version.toString()
    inputs.property("version", version)
    filesMatching("fabric.mod.json") {
        expand("version" to version)
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}

// Builds all versions and collects jars into build/libs/
tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(loomx.modJar.map { it.archiveFile }, loomx.modSourcesJar.map { it.archiveFile })
    into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    dependsOn("build")
}
