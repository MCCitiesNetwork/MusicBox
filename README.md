# MusicBox for Paper

MCCitiesNetwork's modernization of [MusicBox by Spliterash](https://github.com/Spliterash/MusicBox).
MusicBox plays NoteBlockAPI songs through discs, jukeboxes, signs, player
playlists, and its music GUI.

Requires **Java 25**, **Paper 26.3+**, and **NoteBlockAPI 1.6.3**. Commands use
Cloud's native Paper integration. Older Minecraft versions are no longer
supported. Vault is optional for configured economy features.

Build and run the regression suite:

```sh
./gradlew clean :plugin:test build
```

To verify Cloud's native bootstrap against an unpacked Paper 26.3 runtime,
create a text file containing one absolute runtime/library JAR path per line,
then run:

```sh
./gradlew :plugin:paperRuntimeTest -PpaperRuntimeClasspathFile=/path/to/runtime-classpath.txt
```

This optional check initializes the real Minecraft registries and Cloud mappings
without starting a server. Runtime JARs are supplied locally and are not bundled
in the plugin or test fixtures.

Install the shaded JAR from `build/libs` with NoteBlockAPI. The
[data compatibility reference](docs/data-compatibility.md) describes retained
identifiers, metadata, and storage formats.
New installations add their own `.nbs` files under `plugins/MusicBox/songs`.

## Attribution and licensing

This fork retains upstream history and attribution to Spliterash. Upstream does
not currently declare a repository-wide license; this fork does not assign a
new license to upstream code. File-specific copyright and license notices are
retained, including sk89q's notice in `SourcedBlockRedstoneEvent.java`.
The upstream example music is excluded from the built plugin.

## Original upstream README

# MusicBox
Этот плагин добавляет на сервер новые пластинки, таблички, и много чего другого. Полное описание можно посмотреть на spigot

Сюда добавлю лишь видео обзор

[![](http://img.youtube.com/vi/uwygwHRrvkA/0.jpg)](http://www.youtube.com/watch?v=uwygwHRrvkA "")

[Ссылка на spigot](https://www.spigotmc.org/resources/musicbox-music-on-discs.67949/)