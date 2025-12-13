import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.github.patrick.gradle.remapper.RemapTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    `java-library`
    `maven-publish`
    `signing`
    id("com.gradleup.shadow") version "9.3.0"
    id("io.github.patrick.remapper") version "1.4.2"
    id("com.vanniktech.maven.publish") version "0.35.0"
}

group = "net.dmulloy2"
description = "Provides access to the Minecraft protocol"

val mcVersion = "1.21.11"
val isSnapshot = version.toString().endsWith("-SNAPSHOT")
val commitHash = System.getenv("COMMIT_SHA") ?: ""
val isCI = commitHash.isNotEmpty()

repositories {
    if (!isCI) {
        mavenLocal()
    }

    mavenCentral()

    maven {
        url = uri("https://repo.codemc.io/repository/nms/")
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
    implementation("net.bytebuddy:byte-buddy:1.18.2")
    compileOnly("org.spigotmc:spigot-api:${mcVersion}-R0.1-SNAPSHOT")
    compileOnly("org.spigotmc:spigot:${mcVersion}-R0.1-SNAPSHOT:remapped-mojang")
    compileOnly("io.netty:netty-all:4.2.8.Final")
    compileOnly("net.kyori:adventure-text-serializer-gson:4.25.0")
    compileOnly("com.googlecode.json-simple:json-simple:1.1.1")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:6.0.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:6.0.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.0.1")
    testImplementation("org.mockito:mockito-core:5.21.0")
    testImplementation("io.netty:netty-common:4.2.8.Final")
    testImplementation("io.netty:netty-transport:4.2.8.Final")
    testImplementation("org.spigotmc:spigot:${mcVersion}-R0.1-SNAPSHOT:remapped-mojang")
    testImplementation("net.kyori:adventure-text-serializer-gson:4.25.0")
    testImplementation("net.kyori:adventure-text-serializer-plain:4.25.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    processResources {
        val fullVersion = if (isSnapshot && isCI) "${version}-${commitHash.take(7)}" else version

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

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates("$group", project.name, "$version")

    pom {
        name.set(project.name)
        description.set(project.description)
        inceptionYear.set("2012")
        url.set("https://github.com/dmulloy2/ProtocolLib")

        developers {
            developer {
                id.set("dmulloy2")
                name.set("Dan Mulloy")
                url.set("https://dmulloy2.net/")
                email.set("dev@dmulloy2.net")
            }
        }

        licenses {
            license {
                name.set("GNU GENERAL PUBLIC LICENSE - Version 2, June 1991")
                url.set("https://www.gnu.org/licenses/gpl-2.0.txt")
                distribution.set("repo")
            }
        }

        scm {
            tag.set("HEAD")
            url.set("https://github.com/dmulloy2/ProtocolLib")
            connection.set("scm:git:git://github.com/dmulloy2/ProtocolLib.git")
            developerConnection.set("scm:git:git@github.com:dmulloy2/ProtocolLib.git")
        }

        issueManagement {
            system.set("GitHub Issues")
            url.set("https://github.com/dmulloy2/ProtocolLib/issues")
        }
    }
}
