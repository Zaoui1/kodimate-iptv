# Kodimate — Android IPTV Player

**Kodimate** is a premium IPTV player for Android built with Kotlin, Jetpack Compose, and ExoPlayer (Media3).

---

## Features

| Feature | Details |
|---|---|
| **Live TV** | Channel list grouped by category, channel logos, HLS playback |
| **EPG (TV Guide)** | Current programme + time remaining under each channel, per-channel guide screen with timeline |
| **Movies (VOD)** | Poster grid with category filter, rating, ExoPlayer playback |
| **Series** | Poster grid → seasons/episodes → episode playback |
| **Search** | Real-time global search across channels, movies, and series |
| **Favorites** | Heart icon on every item; Favorites tab shows all saved content grouped by type |
| **Continue Watching** | Tracks VOD/episode positions every 10s; shown as a horizontal row |
| **Settings** | Account info, Kodimate about section, logout |

---

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Video**: Media3 / ExoPlayer (HLS, MP4, MKV)
- **Networking**: Retrofit 2 + OkHttp 4
- **Images**: Coil 2
- **Architecture**: MVVM with ViewModel + StateFlow
- **Navigation**: Jetpack Navigation Compose
- **Local storage**: DataStore Preferences (credentials, favorites, watch history)
- **Build**: Kotlin DSL Gradle, minSdk 26, targetSdk 34

---

## Project Structure

```
app/src/main/java/com/iptvplayer/app/
├── data/
│   ├── api/          XtreamApi.kt          — Retrofit interface
│   ├── local/        DataStore files       — credentials, favorites, watch history
│   ├── model/        Models.kt             — all data classes (auth, streams, EPG, …)
│   ├── network/      RetrofitClient.kt
│   └── repository/   XtreamRepository.kt
├── ui/
│   ├── components/   CommonComponents.kt   — shared composables
│   ├── navigation/   NavGraph.kt
│   ├── screens/      all screens
│   ├── theme/        Theme.kt              — Kodimate dark theme
│   └── viewmodel/    all ViewModels
├── IPTVApplication.kt
└── MainActivity.kt
```

---

## Building & Running

### Requirements

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34

### Steps

```bash
# Clone the repo
git clone <repo-url>
cd <repo-dir>

# Build a debug APK
./gradlew assembleDebug

# Install directly on a connected device/emulator
./gradlew installDebug
```

> **Note**: `gradle/wrapper/gradle-wrapper.jar` is included. If it's missing (e.g. after a `.gitignore` exclusion), run `gradle wrapper --gradle-version 8.4` once to regenerate it.

---

## First-time Login

On first launch, Kodimate shows a login screen. Enter:

- **Server URL** — e.g. `http://your-iptv-provider.com:8080`
- **Username** — your Xtream Codes username
- **Password** — your Xtream Codes password

Credentials are saved to encrypted DataStore; subsequent launches skip the login screen.

---

## Xtream Codes API Endpoints Used

| Action | Endpoint |
|---|---|
| Auth | `GET /player_api.php?username=&password=` |
| Live categories | `action=get_live_categories` |
| Live streams | `action=get_live_streams&category_id=` |
| VOD categories | `action=get_vod_categories` |
| VOD streams | `action=get_vod_streams&category_id=` |
| Series categories | `action=get_series_categories` |
| Series | `action=get_series&category_id=` |
| Series info | `action=get_series_info&series_id=` |
| EPG | `action=get_simple_data_table&stream_id=` |

---

## Color Palette

| Token | Hex | Usage |
|---|---|---|
| Background | `#0D0D0D` | App background |
| Surface | `#1A1A1A` | Sidebars, nav bar |
| Card | `#242424` | Content cards |
| Accent | `#0A84FF` | Selection, progress, CTAs |
| Star | `#FFD60A` | Ratings, favorites |

---

*Kodimate — Your Premium IPTV Experience*
