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

Install the shaded JAR from `build/libs` with NoteBlockAPI. Keep the existing
`plugins/MusicBox` directory and server working directory. Read the
[upgrade and rollback guide](docs/upgrade.md) before upgrading an existing setup.
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