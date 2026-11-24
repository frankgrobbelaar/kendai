import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.detekt)
}

val kendaiVersion = providers.gradleProperty("kendai.version").get()

android {
    namespace = "io.github.frankgrobbelaar.kendai"
    compileSdk = 37

    defaultConfig {
        applicationId = "io.github.frankgrobbelaar.kendai"
        // Android 11: most Android e-ink readers run 11 to 13.
        minSdk = 30
        targetSdk = 37
        versionName = kendaiVersion
        versionCode = versionCodeOf(kendaiVersion)
    }

    // The release key comes from the environment: the release workflow sets these from the
    // repository's secrets (docs/releasing.md). Without them a release build is left unsigned.
    val keystore = providers.environmentVariable("KENDAI_KEYSTORE").orNull
    signingConfigs {
        if (keystore != null) {
            create("release") {
                storeFile = file(keystore)
                storePassword = providers.environmentVariable("KENDAI_KEYSTORE_PASSWORD").get()
                keyAlias = providers.environmentVariable("KENDAI_KEY_ALIAS").get()
                keyPassword = providers.environmentVariable("KENDAI_KEY_PASSWORD").get()
            }
        }
    }

    buildTypes {
        release {
            if (keystore != null) signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        warningsAsErrors = true
        abortOnError = true
        checkDependencies = true
        // New library versions are Dependabot's job, not a reason to fail a build.
        disable += setOf("GradleDependency", "NewerVersionAvailable", "AndroidGradlePluginVersion")
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            // Robolectric reaches into FileDescriptor, which Java keeps closed unless asked.
            all {
                it.jvmArgs(
                    "--add-opens=java.base/java.io=ALL-UNNAMED",
                    "--add-exports=java.base/jdk.internal.access=ALL-UNNAMED",
                )
            }
        }
    }
}

kotlin {
    compilerOptions {
        allWarningsAsErrors.set(true)
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.kotlinx.coroutines.android)
    debugImplementation(libs.compose.ui.test.manifest)

    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.compose.ui.test.junit4)

    detektPlugins(libs.compose.rules.detekt)
}

/** `1.2.3` becomes `10203`, so every release has a higher code than the one before. */
fun versionCodeOf(version: String): Int {
    val (major, minor, patch) = version.split(".").map(String::toInt)
    return major * 10_000 + minor * 100 + patch
}
