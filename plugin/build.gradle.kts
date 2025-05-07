plugins {
    id("java")
    id("com.gradleup.shadow") version("latest.release")
}

repositories {
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("me.clip:placeholderapi:2.11.6")
    implementation("net.kyori:adventure-api:4.21.0")
    implementation("net.kyori:adventure-text-minimessage:4.21.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.21.0")
    implementation("net.kyori:adventure-text-serializer-bungeecord:4.3.4")
    implementation("com.zaxxer:HikariCP:6.3.0")
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