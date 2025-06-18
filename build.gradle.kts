import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.github.patrick.gradle.remapper.RemapTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version "9.0.0-beta13"
    id("io.github.patrick.remapper") version "1.4.2"
}

group = "com.comphenix.protocol"
description = "Provides access to the Minecraft protocol"

val mcVersion = "1.21.6"
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
    implementation("net.bytebuddy:byte-buddy:1.17.5")
    compileOnly("org.spigotmc:spigot-api:${mcVersion}-R0.1-SNAPSHOT")
    compileOnly("org.spigotmc:spigot:${mcVersion}-R0.1-SNAPSHOT:remapped-mojang")
    compileOnly("io.netty:netty-all:4.0.23.Final")
    /* 
     * TODO(fix): once you update kyori:adventure-text-serializer-gson please uncomment the TODO in
     * com.comphenix.protocol.wrappers.WrappedComponentStyleTest if the following issue got fixed:
     * https://github.com/KyoriPowered/adventure/issues/1194
     */
    compileOnly("net.kyori:adventure-text-serializer-gson:4.21.0")
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

    register<org.gradle.jvm.tasks.Jar>("javadocJar") {
        archiveClassifier.set("javadoc")
        from(getByName("javadoc"))
    }

    register<org.gradle.jvm.tasks.Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(project.the<JavaPluginExtension>().sourceSets["main"].allJava)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(tasks.getByName("sourcesJar"))
            artifact(tasks.getByName("javadocJar"))

            pom {
                name.set(project.name)
                description.set(project.description)
                url.set("https://github.com/dmulloy2/ProtocolLib")

                developers {
                    developer {
                        id.set("dmulloy2")
                        name.set("Dan Mulloy")
                        url.set("https://dmulloy2.net/")
                    }
                    developer {
                        id.set("aadnk")
                        email.set("kr_stang@hotmail.com")
                        name.set("Kristian S. Stangeland")
                        url.set("https://comphenix.net/")
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

                ciManagement {
                    system.set("Jenkins")
                    url.set("https://ci.dmulloy2.net/job/ProtocolLib")
                }
            }
        }
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