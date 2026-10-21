# TubeFy
![TubeFy Logo](https://github.com/rameshvoltella/TubeFy/blob/beta/app/src/main/res/drawable/tubefy_icon.png)


**Discover Your Favorite Music with Ease!**

Transform your YouTube experience into a seamless music streaming journey with our app. Just like Spotify, our app lets you play YouTube videos as songs, giving you access to your favorite tracks without interruption. Whether you're into the latest hits or classic tunes, you can enjoy them all in a Spotify-like interface.

## Key Features:
- **Play YouTube Videos as Songs:** Enjoy your favorite YouTube music videos as if they were on a streaming service, with audio-only playback for a better listening experience.
- **Curated Playlists:** Create and manage playlists with your favorite tracks, just like you would on Spotify.
- **Discover New Music:** Explore new music and discover trending songs easily with personalized recommendations.
- **Seamless Playback:** Enjoy uninterrupted music playback with background support, so you can listen while using other apps.
- **User-Friendly Interface:** Navigate your music library and playlists with an intuitive and sleek interface designed for ease of use.
- **Offline Downloads:** Download songs to your device and listen without an internet connection. Downloaded songs are collected in their own Downloads playlist, and playing from it queues only your downloaded songs.
- **Smart Queueing:** When a song ends, TubeFy automatically queues similar songs that match your taste, in the same language, genre and artist style as what you're listening to, so the music keeps flowing without breaking your mood.
- **Smart Shuffle:** Tap shuffle once for a normal shuffle, and tap it again for Smart Shuffle. It reshuffles your queue so songs by the same artist flow together, and mixes in recommended songs that match the language, mood and style of what's playing. Tap once more to turn shuffle off, which also removes any recommended songs that haven't played yet.
- **Sleep Timer:** Fall asleep to your music. Choose 15, 30, 45 or 60 minutes, or "End of this song", and the music gently fades out and pauses when time is up. The countdown shows on the player, and you can add 5 more minutes or turn the timer off at any time. It keeps running even when your screen is off.
- **YouTube Videos:** A dedicated YouTube tab to search and watch videos, with Trending, Music, Mixes, Live and Gaming, plus "Trending in your state" in your language. The video player supports quality selection up to 1080p, dubbed audio languages, captions, full screen with pinch to zoom, autoplay of the next related video, channel pages, and "Play in background" when you just want to listen.
- **Android Auto:** Use TubeFy right from your car screen. Browse Home, Recently played, Recommended and your Library (Favorites, Downloads and playlists), search with the car's search button, or just say "Hey Google, play <song> on TubeFy".
- **Android TV:** TubeFy shows up on your Android TV / Google TV home screen with a YouTube experience built for the remote: big video grids, search, recent videos and channel pages, and a full-screen player where you can seek with the remote, choose quality, audio language and captions, and see an "Up next" countdown before the next video plays.


Elevate your music experience and enjoy YouTube's vast music library like never before. Download now and start listening!

## How to Install

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
## Video Preview

Click here -> https://youtu.be/YrhG8T8gbU4 (oldplayer)

<div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px;">
    <div style="text-align: center;">
        <h3>Tubefy</h3>
        <img src="https://github.com/rameshvoltella/TubeFy/blob/redesign/screenshots/new/home.jpeg" alt="List" style="width: 40%; height: 40%;">
        <img src="https://github.com/rameshvoltella/TubeFy/blob/redesign/screenshots/new/playeaudio.jpeg" alt="Player" style="width: 40%; height: 40%;">
    </div>
</div>

<div style="text-align: center;">
        <h3>Search and Playlist</h3>
     <img src="https://github.com/rameshvoltella/TubeFy/blob/redesign/screenshots/new/search.jpeg" alt="Search" style="width: 40%; height: 40%;">
          <img src="https://github.com/rameshvoltella/TubeFy/blob/redesign/screenshots/new/album.jpeg" alt="Search" style="width: 40%; height: 40%;">
   

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

## Technologies Used

- **Kotlin**: The app is built using Kotlin, a modern programming language for Android development known for its conciseness and safety features. ([Kotlin GitHub Repository](https://github.com/JetBrains/kotlin))
- **Jetpack Compose**: Jetpack Compose is used for building the user interface (UI) of the app. It offers a modern and declarative way to construct UIs, making it easier to develop and maintain complex UIs. ([Jetpack Compose GitHub Repository](https://github.com/androidx/androidx/tree/androidx-main/compose))
- **MediaSession**: Control and advertise playback using a MediaSession([MediaSession](https://developer.android.com/media/media3/session/control-playback))
- **Room**: Save data in a local database using Room   Part of Android Jetpack([Room](https://developer.android.com/training/data-storage/room))

## MY GITHUB TROPHY

[![trophy](https://github-profile-trophy.vercel.app/?username=rameshvoltella)](https://github.com/ryo-ma/github-profile-trophy)
