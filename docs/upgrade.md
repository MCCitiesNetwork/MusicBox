# Upgrade and rollback

MusicBox keeps its plugin name and `plugins/MusicBox` data directory. The Java
package is now `com.minecraftcitiesnetwork.musicbox`; this does not change the
persistent item namespace `musicbox:song_hash`.

## Before installing

1. Stop the server cleanly using your normal maintenance procedure.
2. Back up the current MusicBox JAR, the complete `plugins/MusicBox` directory,
   and all worlds containing MusicBox signs, jukeboxes, chests, or player items.
   Take the database backup while the server is stopped, including any SQLite
   sidecar files. Keep the backup outside the plugin directory.
3. Record the server working directory, the exact plugins-directory command-line
   argument, and the plugin data path. Song and folder IDs
   use Java's signed `String.hashCode()` of `File.getPath()`. Preserve the working
   directory, path construction, filenames, and directory structure. Moving or
   renaming songs can invalidate discs, playlists, and signs.
4. Use Java 25 and Paper 26.3 or later. Keep NoteBlockAPI installed. Keep Vault
   and an economy provider installed if MusicBox economy features are enabled.
5. Replace only the MusicBox JAR; ensure no duplicate MusicBox JAR is enabled.
   Retain the current config, language files, songs, and `base.db`.

## Data contract

The SQLite tables remain `playlists`, `playlist_song`, and `signs`. Playlist
ownership, numeric IDs, signed song hashes, and ordering remain in that schema.
Existing config keys and language files remain usable. No replacement-plugin
migration is required. New installations supply their own `.nbs` songs in
`plugins/MusicBox/songs`; the build does not package the upstream example music.

Existing disc tags use the integer `musicbox:song_hash` key. Historical raw
`song_hash` tags are read without rewriting the item, including tags migrated by
Minecraft into `minecraft:custom_data`. New discs keep the same PDC key for
rollback compatibility. Music signs continue
to reference `MASTER`, `ID:<song-hash>`, `CHEST:<folder-hash>`, or
`LIST:<playlist-id>`. Sign flags remain `R`, `E`, `P`, and `I`, and saved sign
locations remain `worldName|x|y|z`.

## Acceptance checks

Use a disposable copy first, with synthetic player data where practical:

- Confirm clean startup and delayed GUI initialization without version-parser
  errors. Exercise `/musicbox`, tab suggestions, permissions, console commands,
  reload, song controls, silent mode, speaker mode, and economy if enabled.
- Play existing songs and discs, edit and reopen an ordered player playlist, and
  confirm ownership and IDs remain unchanged. Temporarily make a referenced
  song unavailable and verify persisted references survive reading and saving.
- Exercise existing signs and flags. Restart with a referenced world or song
  unavailable and confirm saved sign references are retained for recovery.
- Test jukebox insertion, empty-hand removal, replacement, ordinary discs,
  offhand interaction, cancelled interactions, sneak controls, redstone, and
  adjacent-chest next/previous rotation. Confirm item counts are conserved.
- Restart and repeat playback. Check logs for exceptions and verify SQLite
  integrity, row counts, ownership, song hashes, and playlist order.

Automated checks complement these authenticated in-game checks. A successful
build alone does not establish audible playback or GUI behavior on a live server.

## Rollback

Stop the server cleanly. Restore the old JAR and the matching plugin-data and
world snapshots together, then restart with the original server working
directory and dependencies. Restoring only `base.db` does not restore discs,
chests, signs, or player inventories. Retain the failed upgrade snapshot for
investigation; do not overwrite the known-good backup.
