plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version("8.1.1")
}

repositories {
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("me.clip:placeholderapi:2.11.6")
    implementation("net.kyori:adventure-api:4.17.0")
    implementation("net.kyori:adventure-text-minimessage:4.17.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.3.3")
    implementation("net.kyori:adventure-platform-bungeecord:4.3.3")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation(project(":api"))
}

tasks {
    shadowJar {
        minimize()
        relocate("net.kyori", "shaded.net.kyori")
        archiveFileName.set("${project.parent?.name}-v${project.version}.jar")
        destinationDirectory.set(file("../build/libs"))

        manifest {
            attributes(
                mapOf(
                    "paperweight-mappings-namespace" to "mojang"
                )
            )
        }
    }

    jar {
        dependsOn(shadowJar)
    }
}