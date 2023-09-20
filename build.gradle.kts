import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.1.0"
}

group = "com.comphenix.protocol"
version = "5.1.1-SNAPSHOT"
description = "Provides access to the Minecraft protocol"

val isSnapshot = version.toString().endsWith("-SNAPSHOT")

repositories {
    // mavenLocal() // can speed up build, but may fail in CI
    mavenCentral()

    maven {
        url = uri("https://repo.dmulloy2.net/repository/public/")
    }

    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/groups/public/")
    }

    maven {
        url = uri("https://libraries.minecraft.net/")
        metadataSources {
            mavenPom()
            artifact()
            ignoreGradleMetadataRedirection()
        }
    }
}

dependencies {
    implementation("net.bytebuddy:byte-buddy:1.14.3")
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    compileOnly("org.spigotmc:spigot:1.20-R0.1-SNAPSHOT")
    compileOnly("io.netty:netty-all:4.0.23.Final")
    compileOnly("net.kyori:adventure-text-serializer-gson:4.13.0")
    compileOnly("com.googlecode.json-simple:json-simple:1.1.1")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation("org.mockito:mockito-core:4.11.0")
    testImplementation("org.mockito:mockito-inline:4.11.0")
    testImplementation("io.netty:netty-common:4.1.77.Final")
    testImplementation("io.netty:netty-transport:4.1.77.Final")
    testImplementation("org.spigotmc:spigot:1.20-R0.1-SNAPSHOT")
    testImplementation("net.kyori:adventure-text-serializer-gson:4.13.0")
    testImplementation("net.kyori:adventure-text-serializer-plain:4.13.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    withJavadocJar()
    withSourcesJar()
}

val nexusUsername: String by project
val nexusPassword: String by project

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            afterEvaluate {
                artifactId = "ProtocolLib"
            }
        }
    }

    repositories {
        maven {
            url = uri(if (isSnapshot) "https://repo.dmulloy2.net/repository/snapshots/" else
                "https://repo.dmulloy2.net/repository/releases/")

            credentials {
                username = nexusUsername
                password = nexusPassword
            }
        }
    }
}

tasks {
    named<ShadowJar>("shadowJar") {
        dependencies {
            include(dependency("net.bytebuddy:byte-buddy:.*"))
        }
        relocate("net.bytebuddy", "com.comphenix.net.bytebuddy")
        archiveFileName.set("ProtocolLib.jar")
    }
    compileJava {
        options.encoding = Charsets.UTF_8.name()
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
    test {
        useJUnitPlatform()
    }
    build {
        dependsOn(listOf(shadowJar, test))
    }
    runServer {
        minecraftVersion("1.20.1")
    }
    processResources {
        val includeBuild = isSnapshot && System.getenv("BUILD_NUMBER") != null
        val fullVersion = if (includeBuild) {
            "$version-${System.getenv("BUILD_NUMBER")}"
        } else {
            version
        }

        eachFile {
            expand(mapOf("version" to fullVersion))
        }
    }
}
