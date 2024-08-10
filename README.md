# Tag-Game
Play tag in your Minecraft server!

## Links & Support
You can download the plugin here:
- Spigot resource page: https://www.spigotmc.org/resources/tag-game.109807/
- Hangar resource page: https://hangar.papermc.io/Lucaaa/TagGame
- Modrinth resource page: https://modrinth.com/plugin/tag-game-mc
- Wiki: https://lucaaa.gitbook.io/tag-game/
- Javadocs: https://javadoc.jitpack.io/com/github/Luncaaa/Tag-Game-MC/main-SNAPSHOT/javadoc

If you have an issue, found a bug or want to suggest something, you can do it here:
- Spigot discussion page: https://www.spigotmc.org/threads/tag-game.604065/
- GitHub issues: https://github.com/Luncaaa/Tag-Game-MC/issues
- Contact me on Discord: Lucaaa#6268 / luncaaa

## How to compile
The plugin is compiled using Gradle 8.9 and Java 16.
Build the jar running the Gradle task "build"

## Developer API [![](https://jitpack.io/v/Luncaaa/Tag-Game-MC.svg)](https://jitpack.io/#Luncaaa/Tag-Game-MC)
> You can find the docs [here](https://javadoc.jitpack.io/com/github/Luncaaa/Tag-Game-MC/main-SNAPSHOT/javadoc)
<details>
<summary>Maven</summary>

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

```xml
<dependencies>
    <dependency>
        <groupId>com.github.Luncaaa</groupId>
        <artifactId>Tag-Game-MC</artifactId>
        <version>{PLUGIN VERSION}</version>
    </dependency>
</dependencies>
```
</details>

<details>
<summary>Gradle</summary>

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.Luncaaa:Tag-Game-MC:{PLUGIN VERSION}'
}
```
</details>