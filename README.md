# TubeFy
![TubeFy Logo](https://github.com/rameshvoltella/TubeFy/blob/beta/app/src/main/res/drawable/tubefy_icon.png)

**Discover Your Favorite Music with Ease!**

TubeFy turns YouTube into a Spotify-like music app. Play YouTube music as songs with a clean player, build playlists, read synced lyrics, identify songs playing around you, download for offline listening, and watch YouTube videos in any quality, on your phone, in your car with Android Auto, and on Android TV.

## Contents
- [Key Features](#-key-features)
- [Downloads](#-downloads)
- [Identify Song (Shazam)](#-identify-song-shazam)
- [How to Install](#-how-to-install)
- [Screenshots & Video Preview](#-screenshots--video-preview)
- [Technologies Used](#%EF%B8%8F-technologies-used)
- [Disclaimer](#%EF%B8%8F-disclaimer)

## ✨ Key Features

### 🎵 Music
- **Play YouTube Videos as Songs:** Enjoy your favorite YouTube music videos as if they were on a streaming service, with audio-only playback for a better listening experience.
- **Curated Playlists:** Create and manage playlists with your favorite tracks, reorder them by dragging, and save whole albums with one tap.
- **Discover New Music:** A personal Home with your top playlists, "Jump back in", "Recommended for today", language and mood chips, and podcasts in your language.
- **Seamless Playback:** Uninterrupted music with background support, notification controls and lock-screen controls, so you can listen while using other apps.
- **Smart Queueing:** When a song ends, TubeFy automatically queues similar songs that match your taste, in the same language, genre and artist style as what you're listening to, so the music keeps flowing without breaking your mood.
- **Smart Shuffle:** Tap shuffle once for a normal shuffle, and tap it again for Smart Shuffle. It reshuffles your queue so songs by the same artist flow together, and mixes in recommended songs that match the language, mood and style of what's playing. Tap once more to turn shuffle off, which also removes any recommended songs that haven't played yet.
- **Sleep Timer:** Fall asleep to your music. Choose 15, 30, 45 or 60 minutes, or "End of this song", and the music gently fades out and pauses when time is up. The countdown shows on the player, and you can add 5 more minutes or turn the timer off at any time. It keeps running even when your screen is off.

### 🎤 Lyrics & Song Recognition
- **Lyrics:** A Lyrics tab in the music player shows the song's details (singers, movie, lyricist, music director, year) and its lyrics, full screen with the player controls. Timed lyrics follow the song line by line, tapping a line jumps there, and a timing control fixes songs whose video has an intro. Songs in Malayalam, Tamil, Hindi and other scripts can be switched to English letters with one tap. Lyrics of downloaded songs work offline too.
- **Identify Song (Shazam):** Hear a song playing around you? Tap the identify button on Home (or 🎤 in Search), and TubeFy recognises it in a few seconds, shows its title, artist, movie and year, and plays it right away. Every identified song is kept in **Library → Shazam**. See [Identify Song (Shazam)](#-identify-song-shazam) below.

### 📺 YouTube
- **YouTube Videos:** A dedicated YouTube tab to search and watch videos, with search suggestions, Trending, Music, Mixes, Live and Gaming, plus "Trending in your state" in your language. The video player starts at the best quality your screen can show (up to 4K on TVs), and supports dubbed audio languages, captions, full screen with pinch to zoom, swipe controls for brightness and volume, autoplay of the next related video, channel pages, downloads, and "Play in background" when you just want to listen.

### 📥 Downloads
- **Downloads:** Download songs inside the app to listen offline, save songs to your phone with their lyrics, or download YouTube videos in any quality. See [Downloads](#-downloads) below.

### 🚗 📺 Everywhere
- **Android Auto:** Use TubeFy right from your car screen. Browse Home, Recently played, Recommended and your Library (Favorites, Downloads and playlists), search with the car's search button, or just say "Hey Google, play <song> on TubeFy".
- **Android TV:** TubeFy shows up on your Android TV / Google TV home screen with a YouTube experience built for the remote: big video grids, an on-screen keyboard search with suggestions, recent videos and channel pages, and a full-screen player where you can seek with the remote, choose quality, audio language and captions, and see an "Up next" countdown before the next video plays.

Elevate your music experience and enjoy YouTube's vast music library like never before. Download now and start listening!

## 📥 Downloads

TubeFy has three ways to download, depending on what you want to do with the file.

### 🎵 Songs, inside TubeFy (offline listening)
In the music player, tap **Download → In TubeFy**. The song is saved inside the app and appears in your **Downloads** playlist, so you can listen without internet. Its details and lyrics (in the original script and in English letters) are saved with it, so the Lyrics tab works offline too.

### 📁 Songs, to your phone storage (with lyrics)
In the music player, tap **Download → Save to phone storage**. The first time, choose a folder with Android's folder picker; TubeFy remembers it.
- The song is saved as an **audio file** (`.m4a`, best quality) that plays in any music app.
- If the song is already in the folder, TubeFy asks whether to **Replace** it or **Keep both**.
- Every saved song is added to **`TubeFy Song Book.html`** in the same folder. It's a page you can open in any browser, with all your songs' details and lyrics, a song list and a search box. Choose whether the lyrics are saved in the **original script**, in **English letters**, or **both**.

### 🎬 YouTube videos (any quality, or audio only)
On a YouTube video's page, tap **Download** and choose:
- **Video:** any quality the video has, from 144p up to 1080p (MP4 or WebM), with the file size shown. HD video and its sound come separately from YouTube, so TubeFy joins them into one MP4 on your phone, with no quality loss.
- **Audio only:** any audio format (M4A or Opus), including dubbed languages when the video has them.

Videos are saved in a **YouTube** folder inside your chosen folder, for example `TubeFy/YouTube`. Go to **Settings → Downloads** to follow downloads in progress, retry failed ones, and open finished files in any app (VLC, Gallery, MX Player and others) with **Open with**.

## 🎤 Identify Song (Shazam)

TubeFy can recognise a song playing around you (from a radio, a TV, a shop or another phone) and play it for you.

### How to use it
1. Tap the round **identify button** at the bottom-right of **Home**, or the **🎤** in the **Search** box. The first time, allow microphone access.
2. Hold the phone near the music. Whatever TubeFy is playing pauses, so it hears the right song.
3. In a few seconds you see the song's **title, artist, movie/album and year**:
   - **Play** finds it on YouTube Music and starts it.
   - **Search** shows the search results, to pick another version.
4. Every identified song is saved in **Library → Shazam**, where you can play it again, search for other versions or remove it.

### How it works
TubeFy uses the same recognition service as the Shazam app, through the approach of the open-source [SongRec](https://github.com/marin-m/SongRec) project:

1. **Listen:** TubeFy records up to 12 seconds from the microphone, as 16 kHz mono audio.
2. **Fingerprint on the phone:** the audio is turned into a compact *signature*: the strongest frequency peaks over time (FFT every 128 samples, peak spreading and peak picking) in four bands (250–520 Hz, 520–1450 Hz, 1450–3500 Hz, 3500–5500 Hz), encoded in Shazam's binary signature format (magic numbers, CRC-32) and Base64-encoded as a `data:audio/vnd.shazam.sig` URI. This is a Kotlin port of SongRec's signature generator.
3. **Recognise:** the signature is sent in one small HTTPS `POST` to Shazam's recognition endpoint, the one the Shazam app uses:
   ```
   https://amp.shazam.com/discovery/v5/en/US/android/-/tag/<random-uuid>/<random-uuid>
   ```
   with a JSON body containing the signature URI, its length in milliseconds, a timestamp and the phone's time zone. TubeFy asks after 4, 8 and 12 seconds of listening and stops at the first match, so clear audio is usually recognised within about 4 seconds.
4. **Read the answer:** from the response TubeFy uses `track.title`, `track.subtitle` (artist), `track.images` (cover art) and the `Album` and `Released` metadata.
5. **Play:** TubeFy searches YouTube Music for "title artist" and plays the best match.

### Privacy
- The microphone is only used **after you tap** the identify button, and only for those few seconds.
- **Only the fingerprint is sent** (a few KB of frequency peaks), never the recording. The audio can't be rebuilt from it.
- Requests carry the phone's own standard identity; there's no account and nothing is linked to you.
- Identified songs are stored **only on your phone**.

### Limits
- This is **not an official Shazam / Apple API**. It's the service the Shazam app uses, accessed the way SongRec does it. It works today, but can stop working if Shazam changes it, until TubeFy is updated. Too many tries in a short time can be rate-limited for a while.
- Quiet, noisy or live versions of a song may not be recognised; tap **Try again** closer to the music.

### Code
- [`ShazamSignature.kt`](app/src/main/java/com/ramzmania/tubefy/core/recognition/ShazamSignature.kt): fingerprint generation and signature encoding (port of SongRec).
- [`SongRecognizer.kt`](app/src/main/java/com/ramzmania/tubefy/core/recognition/SongRecognizer.kt): microphone recording, the recognition request and reading the answer.
- [`IdentifiedSongs.kt`](app/src/main/java/com/ramzmania/tubefy/core/recognition/IdentifiedSongs.kt): the Library → Shazam history.

**Credits:** the fingerprint algorithm and signature format come from [SongRec](https://github.com/marin-m/SongRec) by Marin M. (GPL-3.0), with request details also documented by [ShazamIO](https://github.com/shazamio/ShazamIO).

> **License note:** the recognition code is derived from SongRec, which is GPL-3.0, while the rest of TubeFy is under the MIT [LICENSE](LICENSE). Because of the GPL, builds that include this code are distributed under GPL-3.0 terms.

## 📲 How to Install

TubeFy isn't on the Google Play Store, so you install it from its APK file. The same APK works on phones, Android TV and Google TV. Get it from the [Releases page](https://github.com/rameshvoltella/TubeFy/releases): open the latest release and download the `.apk` file under **Assets**.

> Menu names can differ slightly between phone and TV brands. If you can't find an option, search for it in the device's Settings.

### 📱 Phone

1. Download the APK on your phone from the [Releases page](https://github.com/rameshvoltella/TubeFy/releases).
2. Open the downloaded file. Android will say it can't install apps from this source: tap **Settings**, turn on **Allow from this source** for your browser or file manager, then go back.
3. Tap **Install**. If Google Play Protect warns that the app is unknown, tap **More details → Install anyway**.
4. Open TubeFy and allow notifications so the music controls show up.

To update, install the newer APK over the old one. Your playlists and downloads are kept.

### 🚗 Android Auto

Android Auto only shows apps from the Play Store unless you allow other apps once:

1. Install TubeFy on your phone (see above).
2. On your phone open **Settings → Connected devices → Connection preferences → Android Auto** (or search "Android Auto" in Settings).
3. Scroll to the bottom and tap **Version** about 10 times, then tap **OK** to turn on developer mode.
4. Tap **⋮ (top right) → Developer settings** and turn on **Unknown sources**.
5. Restart Android Auto (unplug and reconnect the phone). TubeFy now appears with your other media apps in the car.

### 📺 Android TV / Google TV

**Step 1: Turn on Developer options**
- **Google TV:** Settings → System → About → tap **Android TV OS build** 7 times.
- **Android TV:** Settings → Device Preferences → About → tap **Build** 7 times.

You'll see "You are now a developer".

**Step 2: Install the APK** using one of these:

- **With the Downloader app (easiest):**
  1. Install **Downloader** (by AFTVnews) from the TV's Play Store.
  2. Allow it to install apps: Settings → Apps → Security & restrictions → **Unknown sources** → turn on **Downloader** (on some TVs: Device Preferences → Security & restrictions).
  3. Open Downloader, enter the APK link from the [Releases page](https://github.com/rameshvoltella/TubeFy/releases), and tap **Install** when it finishes downloading.
- **From a computer with adb:**
  1. On the TV, go to Settings → System (or Device Preferences) → **Developer options** and turn on **USB debugging** / **Network debugging**.
  2. Find the TV's IP address in Settings → Network & Internet.
  3. On the computer run `adb connect <TV-IP>:5555`, accept the prompt on the TV, then `adb install <the-downloaded-file>.apk`.

**Step 3:** TubeFy appears on the TV home screen (look in **Apps** if it isn't in the first row). It opens straight into the YouTube experience, made for the remote.

## 📸 Screenshots & Video Preview

Click here -> https://youtu.be/YrhG8T8gbU4 (oldplayer)

<div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px;">
    <div style="text-align: center;">
        <h3>Tubefy</h3>
        <img src="https://github.com/rameshvoltella/TubeFy/blob/redesign/screenshots/new/home.jpeg" alt="List" style="width: 40%; height: 40%;">
        <img src="https://github.com/rameshvoltella/TubeFy/blob/redesign/screenshots/new/playeaudio.jpeg" alt="Player" style="width: 40%; height: 40%;">
    </div>
</div>

<div style="text-align: center;">
        <h3>Search, Playlist and Lyrics</h3>
     <img src="https://github.com/rameshvoltella/TubeFy/blob/redesign/screenshots/new/search.jpeg" alt="Search" style="width: 40%; height: 40%;">
          <img src="https://github.com/rameshvoltella/TubeFy/blob/redesign/screenshots/new/album.jpeg" alt="Search" style="width: 40%; height: 40%;">
                    <img src="https://github.com/rameshvoltella/TubeFy/blob/redesign/screenshots/new/lyorg.jpeg" alt="Search" style="width: 40%; height: 40%;">
                              <img src="https://github.com/rameshvoltella/TubeFy/blob/redesign/screenshots/new/lyeng.jpeg" alt="Search" style="width: 40%; height: 40%;">
   

</div>
<div style="text-align: center;">
        <h3>Song Search with Shazam</h3>
     <img src="https://github.com/rameshvoltella/TubeFy/blob/redesign/screenshots/new/sha1.jpeg" alt="Search" style="width: 40%; height: 40%;">
              <img src="https://github.com/rameshvoltella/TubeFy/blob/redesign/screenshots/new/sha1.jpeg" alt="Search" style="width: 40%; height: 40%;">
   

</div>
<div style="text-align: center;">
        <h3>Smart Shuffle</h3>
     <img src="https://github.com/rameshvoltella/TubeFy/blob/redesign/screenshots/new/smart.jpeg" alt="Search" style="width: 40%; height: 40%;">
         
   

</div>

<div style="text-align: center;">
        <h3>Notification, VideoView,Library</h3>
     <img src="https://github.com/rameshvoltella/TubeFy/blob/beta/screenshots/WhatsApp%20Image%202024-08-28%20at%209.09.38%20PM(1).jpeg" alt="Notification" style="width: 40%; height: 40%;">
    <img src="https://github.com/rameshvoltella/TubeFy/blob/redesign/screenshots/new/player.jpeg" alt="MiniPlayer" style="width: 40%; height: 40%;">
        <img src="https://github.com/rameshvoltella/TubeFy/blob/redesign/screenshots/new/lib.jpeg" alt="Playlist" style="width: 40%; height: 40%;">

</div>

<div style="text-align: center;">
        <h3>Download Support</h3>
     <img src="https://github.com/rameshvoltella/TubeFy/blob/redesign/screenshots/new/dolyr.jpeg" alt="Search" style="width: 40%; height: 40%;">
               <img src="https://github.com/rameshvoltella/TubeFy/blob/redesign/screenshots/new/downvid1.jpeg" alt="Search" style="width: 40%; height: 40%;">
                              <img src="https://github.com/rameshvoltella/TubeFy/blob/redesign/screenshots/new/downvid2.jpeg" alt="Search" style="width: 40%; height: 40%;">
                                                            <img src="https://github.com/rameshvoltella/TubeFy/blob/redesign/screenshots/new/downvid3.jpeg" alt="Search" style="width: 40%; height: 40%;">
                              


</div>

<div style="text-align: center;">
        <h3>Offline Lyrics Library</h3>
             <img src="https://github.com/rameshvoltella/TubeFy/blob/redesign/screenshots/new/offline1.jpeg" alt="Search" style="width: 40%; height: 40%;">
     <img src="https://github.com/rameshvoltella/TubeFy/blob/redesign/screenshots/new/offline2.jpeg" alt="Search" style="width: 40%; height: 40%;">
         
   

</div>

<div style="text-align: center;">
        <h3>TV</h3>
     <img src="https://github.com/rameshvoltella/TubeFy/blob/redesign/screenshots/new/tv1.png" alt="Tv1" style="width: 40%; height: 40%;">
    <img src="https://github.com/rameshvoltella/TubeFy/blob/redesign/screenshots/new/tv2.png" alt="Tv2" style="width: 40%; height: 40%;">
        <img src="https://github.com/rameshvoltella/TubeFy/blob/redesign/screenshots/new/tv3.png" alt="Tv3" style="width: 40%; height: 40%;">
                <img src="https://github.com/rameshvoltella/TubeFy/blob/redesign/screenshots/new/tv4.png" alt="Tv4" style="width: 40%; height: 40%;">
                        <img src="https://github.com/rameshvoltella/TubeFy/blob/redesign/screenshots/new/tv5.png" alt="Tv5" style="width: 40%; height: 40%;">

</div>

<div style="text-align: center;">
        <h3>Android Auto(CAR)</h3>
     <img src="https://github.com/rameshvoltella/TubeFy/blob/redesign/screenshots/new/car1.png" alt="Tv1" style="width: 40%; height: 40%;">
          <img src="https://github.com/rameshvoltella/TubeFy/blob/redesign/screenshots/new/car2.png" alt="Tv1" style="width: 40%; height: 40%;">
               <img src="https://github.com/rameshvoltella/TubeFy/blob/redesign/screenshots/new/car3.png" alt="Tv1" style="width: 40%; height: 40%;">

</div>

## 🛠️ Technologies Used

- **Kotlin** and **Jetpack Compose**: the whole app and its UI, including the Android TV screens. ([Kotlin](https://github.com/JetBrains/kotlin), [Jetpack Compose](https://developer.android.com/jetpack/compose))
- **Media3**: ExoPlayer for playback (including DASH streams with every quality, audio language and captions), MediaSession / MediaLibraryService for notification, lock-screen and Android Auto controls, and Transformer to join HD video with its sound for downloads. ([Media3](https://developer.android.com/media/media3))
- **NewPipeExtractor**: reads YouTube and YouTube Music: search, videos, playlists, channels, charts and streams. ([NewPipeExtractor](https://github.com/TeamNewPipe/NewPipeExtractor))
- **Hilt**: dependency injection.
- **Room**: playlists, favorites and the local library. ([Room](https://developer.android.com/training/data-storage/room))
- **WorkManager**: background downloads that survive the app being closed.
- **OkHttp, Retrofit and Moshi**: networking and JSON.
- **Coil**: images and thumbnails.
- **Lyrics:** YouTube Music (Musixmatch / LyricFind) and [LRCLIB](https://lrclib.net) for timed lyrics; Android's built-in transliteration for English letters.
- **Song recognition:** a Kotlin port of [SongRec](https://github.com/marin-m/SongRec)'s Shazam fingerprinting.

## ⚠️ Disclaimer

TubeFy is an independent, open-source project for personal use. It is **not affiliated with, endorsed by or connected to** YouTube, Google, Shazam or Apple. All names and trademarks belong to their owners. Music and videos remain the property of their creators; please respect copyright and the terms of the services you use.

## MY GITHUB TROPHY

[![trophy](https://github-profile-trophy.vercel.app/?username=rameshvoltella)](https://github.com/ryo-ma/github-profile-trophy)
