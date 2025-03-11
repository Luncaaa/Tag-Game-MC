plugins {
    id("maven-publish")
}

java {
    withJavadocJar()
}

dependencies {
    implementation("org.jetbrains:annotations:26.0.2")
}

tasks {
    javadoc {
        title = "TagGame API " + project.version
        options {
            (this as StandardJavadocDocletOptions).apply {
                // ©
                bottom = "Copyright 2024 Lucaaa. All rights reserved. Licensed under GPL 3.0. View the source code <a href=\"https://github.com/Luncaaa/Tag-Game-MC\">here</a>"
                links = listOf("https://hub.spigotmc.org/javadocs/spigot")
                header = "<div style=\"font-size: 25px\"><a href=\"https://github.com/Luncaaa\">By Lucaaa</a>    |    <a href=\"https://spigotmc.org/resources/authors/lucaaa.1192446/\">More plugins</a></div>"
            }
        }
    }

    assemble {
        finalizedBy(publishToMavenLocal)
    }
}

publishing {
    publications {
        val mavenJava by creating(MavenPublication::class) {
            groupId = "TagGameMC"
            artifactId = "taggame-api"
            version = "${project.version}"

            from(components["java"])
        }
    }
}