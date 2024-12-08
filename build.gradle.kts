import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import io.github.patrick.gradle.remapper.RemapTask

plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version "8.3.2"
    id("io.github.patrick.remapper") version "1.4.1"
}

group = "com.comphenix.protocol"
version = "5.4.0-SNAPSHOT"
description = "Provides access to the Minecraft protocol"

val mcVersion = "1.21.4"
val isSnapshot = version.toString().endsWith("-SNAPSHOT")
val buildNumber = System.getenv("BUILD_NUMBER") ?: ""
val isJenkins = buildNumber.isNotEmpty()

repositories {
    if (!isJenkins) {
        mavenLocal()
    }

    mavenCentral()

    maven {
        url = uri("https://repo.codemc.io/repository/nms/")
    }

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
    implementation("net.bytebuddy:byte-buddy:1.15.1")
    compileOnly("org.spigotmc:spigot-api:${mcVersion}-R0.1-SNAPSHOT")
    compileOnly("org.spigotmc:spigot:${mcVersion}-R0.1-SNAPSHOT:remapped-mojang")
    compileOnly("io.netty:netty-all:4.0.23.Final")
    compileOnly("net.kyori:adventure-text-serializer-gson:4.14.0")
    compileOnly("com.googlecode.json-simple:json-simple:1.1.1")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.0")
    testImplementation("org.mockito:mockito-core:5.6.0")
    testImplementation("io.netty:netty-common:4.1.97.Final")
    testImplementation("io.netty:netty-transport:4.1.97.Final")
    testImplementation("org.spigotmc:spigot:${mcVersion}-R0.1-SNAPSHOT:remapped-mojang")
    testImplementation("net.kyori:adventure-text-serializer-gson:4.14.0")
    testImplementation("net.kyori:adventure-text-serializer-plain:4.14.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications.withType<MavenPublication> {
        artifactId = "ProtocolLib"
    }

    repositories {
        maven {
            url = if (isSnapshot) {
                uri("https://repo.dmulloy2.net/repository/snapshots/")
            } else {
                uri("https://repo.dmulloy2.net/repository/releases/")
            }

            credentials {
                username = System.getenv("NEXUS_USERNAME")
                password = System.getenv("NEXUS_PASSWORD")
            }
        }
    }
}

tasks {
    processResources {
        val fullVersion = if (isSnapshot && isJenkins) "${version}-${buildNumber}" else version

        eachFile {
            expand("version" to fullVersion)
        }
    }

    test {
        useJUnitPlatform()
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
        }
    }

    shadowJar {
        dependencies {
            include(dependency("net.bytebuddy:byte-buddy:.*"))
        }
        relocate("net.bytebuddy", "com.comphenix.net.bytebuddy")

        manifest {
            attributes(
                "paperweight-mappings-namespace" to "mojang"
            )
        }

        archiveFileName = "ProtocolLib.jar"
    }

    remap {
        dependsOn("shadowJar")

        inputTask.set(getByName<ShadowJar>("shadowJar"))
        version.set(mcVersion)
        action.set(RemapTask.Action.MOJANG_TO_SPIGOT)
    }

    assemble {
        dependsOn("remap")
    }

    javadoc {
        options.encoding = "UTF-8"
    }

    compileJava {
        options.encoding = "UTF-8"
    }
}
