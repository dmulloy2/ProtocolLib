import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    `java-library`
    id("com.gradleup.shadow") version "9.4.0"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

group = "net.dmulloy2"
version = rootProject.version

val mcVersion: String by project
val isSnapshot = version.toString().endsWith("-SNAPSHOT")
val commitHash = System.getenv("COMMIT_SHA") ?: ""
val isCI = commitHash.isNotEmpty()

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
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
    paperweight.paperDevBundle("$mcVersion.build.+")

    compileOnly("io.netty:netty-all:4.2.8.Final")
    compileOnly("net.kyori:adventure-text-serializer-gson:4.25.0")
    compileOnly("commons-lang:commons-lang:2.6")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:6.0.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:6.0.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.0.1")
    testImplementation("org.mockito:mockito-core:5.21.0")
    testImplementation("io.netty:netty-common:4.2.8.Final")
    testImplementation("io.netty:netty-transport:4.2.8.Final")
    testImplementation("net.kyori:adventure-text-serializer-gson:4.25.0")
    testImplementation("net.kyori:adventure-text-serializer-plain:4.25.0")
    testImplementation("commons-lang:commons-lang:2.6")
}

paperweight.reobfArtifactConfiguration =
    io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

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

sourceSets {
    test {
        java.srcDir(rootProject.file("src/test/java"))
        resources.srcDir(rootProject.file("src/test/resources"))
    }
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

    compileJava {
        options.release.set(17)
    }

    shadowJar {
        relocate("net.bytebuddy", "com.comphenix.net.bytebuddy")

        manifest {
            attributes("paperweight-mappings-namespace" to "mojang")
        }

        destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
        archiveFileName.set("ProtocolLib.jar")
    }
}
