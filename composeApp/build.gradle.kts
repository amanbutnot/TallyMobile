import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlin.serialization)
    id("app.cash.sqldelight") version "2.1.0"

}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true

          //  linkerOpts("-lsqlite3")
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)

            //SqlDelight
            implementation(libs.android.driver)

            //html to pdf
            implementation("com.itextpdf:html2pdf:6.2.1")


        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            //Navigation
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.tabNavigator)
            implementation(libs.voyager.bottomSheetNavigator)
            implementation(libs.voyager.transitions)
            implementation(libs.voyager.screenModel)

            //Shared Preferences
            implementation(libs.multiplatform.settings.no.arg)
            //ktor
            implementation(libs.bundles.ktor)
            implementation(libs.runtime)

            //Calender and Date and Time
            implementation(libs.kmp.date.time.picker)
            implementation(libs.kotlinx.datetime)

            //For base64 and byte conversion
            implementation(libs.okio) // or latest

            //Material Icons
            implementation(libs.material.icons.extended)

            implementation(libs.coroutines.extensions)

            // kotlinx date time
            implementation(libs.kotlinx.datetime)

            //Handle Files
            implementation("io.github.vinceglb:filekit-core:0.12.0")
            // Enables FileKit dialogs without Compose dependencies
            implementation("io.github.vinceglb:filekit-dialogs:0.12.0")

// Enables FileKit dialogs with Composable utilities
            implementation("io.github.vinceglb:filekit-dialogs-compose:0.12.0")

        }
        nativeMain.dependencies {
            implementation(libs.ktor.client.darwin)

            //SqlDelight
            implementation(libs.native.driver)
        }
    }
}

android {
    namespace = "org.prime.tally"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.prime.tally"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}


sqldelight {
    databases {

        create("TallyDatabase") {
            verifyMigrations.set(false)
            deriveSchemaFromMigrations.set(false)
            packageName.set("org.tally")
        }
    }
}
