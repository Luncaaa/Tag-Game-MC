plugins {
    id("java")
    id("com.gradleup.shadow") version("latest.release")
}

repositories {
    maven("https://repo.extendedclip.com/releases/")
}

dependencies {
    compileOnly("me.clip:placeholderapi:2.12.2")
    implementation("net.kyori:adventure-api:4.26.1")
    implementation("net.kyori:adventure-text-minimessage:4.26.1")
    implementation("net.kyori:adventure-text-serializer-legacy:4.26.1")
    implementation("net.kyori:adventure-platform-bukkit:4.4.1")
    implementation("com.zaxxer:HikariCP:7.0.2")
    implementation(project(":api"))
}

tasks {
    shadowJar {
        exclude("org/slf4j/**")
        exclude("org/intellij/**")
        exclude("org/jetbrains/**")
        minimize()
        relocate("net.kyori", "shaded.net.kyori")
        relocate("com.zaxxer", "shaded.com.zaxxer")
        archiveFileName.set("${project.parent?.name}-${project.version}.jar")
        destinationDirectory.set(file("../build/libs"))

        manifest {
            attributes(
                mapOf(
                    "paperweight-mappings-namespace" to "mojang"
                )
            )
        }
    }

    assemble {
        dependsOn(shadowJar)
    }
}