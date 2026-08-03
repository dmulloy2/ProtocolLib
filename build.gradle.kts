plugins {
    `java-library`
    `maven-publish`
    signing
    id("io.github.patrick.remapper") version "1.4.3"
    id("com.vanniktech.maven.publish") version "0.36.0"
}

group = "net.dmulloy2"
description = "Provides access to the Minecraft protocol"

val mcVersion: String by project
val isSnapshot = version.toString().endsWith("-SNAPSHOT")
val isJitPack = System.getenv("JITPACK")?.equals("true", ignoreCase = true) ?: false
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
    compileOnly("org.spigotmc:spigot:${mcVersion}-R0.1-SNAPSHOT")
    compileOnly("io.netty:netty-all:4.2.8.Final")
    compileOnly("net.kyori:adventure-text-serializer-gson:4.25.0")
    compileOnly("com.googlecode.json-simple:json-simple:1.1.1")
    compileOnly("commons-lang:commons-lang:2.6")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

sourceSets {
    test {
        java.setSrcDirs(emptyList<String>())
        resources.setSrcDirs(emptyList<String>())
    }
}

tasks {
    test {
        dependsOn(":paper:test")
    }

    check {
        dependsOn(":paper:check", ":spigot:check")
    }

    javadoc {
        options.encoding = "UTF-8"
    }

    compileJava {
        options.encoding = "UTF-8"
        options.release.set(17)
    }
}

tasks.register("shadowJar") {
    group = "build"
    description = "Builds the Paper and Spigot plugin distributions."
    dependsOn(":paper:shadowJar", ":spigot:shadowJar")
}

mavenPublishing {
    publishToMavenCentral()
    if (!isSnapshot && !isJitPack) {
        signAllPublications()
    }

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
