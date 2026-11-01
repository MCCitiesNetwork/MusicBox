# Data compatibility

MusicBox retains its plugin name and `plugins/MusicBox` data directory. Moving
Java packages to `com.minecraftcitiesnetwork.musicbox` does not change the item
namespace `musicbox:song_hash`.

## Songs and identifiers

Song and folder IDs remain the signed Java `String.hashCode()` of
`File.getPath()`. Working-directory, plugins-directory argument, filename, and
folder changes can therefore invalidate existing references even when they
point to equivalent filesystem locations. Native Paper and Bukkit plugin loaders
construct the MusicBox data-folder path in the same way.

New installations supply their own `.nbs` files under `plugins/MusicBox/songs`.
The built plugin does not contain the upstream example music.

## Player playlists and signs

SQLite tables remain `playlists`, `playlist_song`, and `signs`. Numeric IDs,
ownership, signed song hashes, and ordering retain their existing representation.
Unavailable song references survive reads and saves. Saved sign locations remain
stored until restoration succeeds, including when the referenced world is absent.
Existing config keys and language files remain supported.

Music signs retain `MASTER`, `ID:<song-hash>`, `CHEST:<folder-hash>`, and
`LIST:<playlist-id>` references, flags `R`, `E`, `P`, and `I`, and saved locations
in `worldName|x|y|z` format.

## Disc metadata

New discs use the integer `musicbox:song_hash` persistent-data key, matching
existing MusicBox discs. Historical raw `song_hash` tags are also read without
rewriting the item, including tags migrated by Minecraft into
`minecraft:custom_data`. These formats remain readable by the corresponding
older MusicBox implementation; plugin and world state must remain consistent.
