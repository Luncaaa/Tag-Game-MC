plugins {
    id("java")
}

allprojects {
    apply(plugin = "java")
    group = "me.lucaaa.tag"
    version = "1.3"
}

subprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }

    dependencies {
        compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
        implementation("org.jetbrains:annotations:24.1.0")

    }
}