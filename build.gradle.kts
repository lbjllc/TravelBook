// 2. build.gradle.kts (Project: TravelBook - in your root project folder)
// Replace the entire contents of this file with the simplified version below.

plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false // <-- THIS LINE WAS MISSING
}
