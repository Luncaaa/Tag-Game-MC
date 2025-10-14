plugins {
    id("maven-publish")
}

java {
    withJavadocJar()
}

dependencies {
    implementation("org.jetbrains:annotations:26.0.2-1")
}

tasks {
    javadoc {
        title = "TagGame API " + project.version
        options {
            (this as StandardJavadocDocletOptions).apply {
                charSet = "UTF-8"
                encoding = "UTF-8"
                docEncoding = "UTF-8"
                bottom = "Copyright © 2025 Lucaaa. All rights reserved. Licensed under GPL 3.0. View the source code <a href=\"https://github.com/Luncaaa/Tag-Game-MC\">here</a>"
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