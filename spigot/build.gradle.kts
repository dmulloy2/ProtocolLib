import org.gradle.api.attributes.java.TargetJvmVersion

plugins {
    `java-library`
    id("com.gradleup.shadow") version "9.4.0"
}

group = "net.dmulloy2"
version = rootProject.version

val mcVersion: String by project

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.codemc.io/repository/nms/")
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven("https://libraries.minecraft.net/") {
        metadataSources {
            mavenPom()
            artifact()
            ignoreGradleMetadataRedirection()
        }
    }
}

dependencies {
    implementation(project(":"))
    compileOnly("org.spigotmc:spigot-api:${mcVersion}-R0.1-SNAPSHOT")
    compileOnly("org.spigotmc:spigot:${mcVersion}-R0.1-SNAPSHOT")
    compileOnly("io.netty:netty-all:4.2.8.Final")
    compileOnly("net.kyori:adventure-text-serializer-gson:4.25.0")
    compileOnly("com.googlecode.json-simple:json-simple:1.1.1")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:6.0.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.0.1")
    testImplementation("org.mockito:mockito-core:5.21.0")
    testImplementation("io.netty:netty-transport:4.2.8.Final")
    testImplementation("org.spigotmc:spigot:${mcVersion}-R0.1-SNAPSHOT")
}

configurations.matching { it.name == "compileClasspath" || it.name == "runtimeClasspath" || it.name == "testCompileClasspath" || it.name == "testRuntimeClasspath" }.configureEach {
    attributes {
        attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks {
    processResources {
        eachFile {
            expand("version" to project.version)
        }
    }

    test {
        useJUnitPlatform()
    }

    compileJava {
        options.release.set(17)
    }

    shadowJar {
        relocate("net.bytebuddy", "com.comphenix.net.bytebuddy")

        manifest {
            attributes("paperweight-mappings-namespace" to "mojang")
        }

        destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
        archiveFileName.set("ProtocolLib-Spigot.jar")
    }
}
