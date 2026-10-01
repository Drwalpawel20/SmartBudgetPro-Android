plugins {
    id("com.android.application") version "8.6.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.9.0" apply false

}


tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
