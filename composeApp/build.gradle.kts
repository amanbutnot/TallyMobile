import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
 //   alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlin.serialization)
    id("app.cash.sqldelight") version "2.1.0"

}

kotlin {
//    cocoapods {
//        version = "1.0.0"   // ← REQUIRED, any valid semver
//        summary = "Easy Karobar shared module"
//        homepage = "https://example.com"
//        ios.deploymentTarget = "14.1"
//
//        pod("SSZipArchive", "2.4.3")
//    }
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

            //Barcode
            implementation(libs.gms.play.services.code.scanner)
            implementation(libs.zxing)


            //html to pdf
            implementation("com.itextpdf:html2pdf:6.2.1")
            implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")



        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation("org.jetbrains.compose.material3:material3:1.10.0-alpha01")
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
         //   implementation(libs.androidx.lifecycle.runtimeCompose)

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

            //For base64 and byte conversion
            implementation(libs.okio) // or latest
         //   implementation(libs.okio.zip) // or latest


            //Material Icons
            implementation(libs.material.icons.extended)

            implementation(libs.coroutines.extensions)

            // kotlinx date time
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")

            //Handle Files
            implementation(libs.filekit.core)
            // Enables FileKit dialogs without Compose dependencies
            implementation(libs.filekit.dialogs)

// Enables FileKit dialogs with Composable utilities
            implementation(libs.filekit.dialogs.compose)

            //Coil Image loading
            implementation(libs.coil.compose)
            implementation("io.coil-kt.coil3:coil-network-ktor3:3.3.0")



            implementation(libs.compass.geocoder)
            implementation(libs.compass.geocoder.mobile)

            // Geolocation
            implementation(libs.compass.geolocation)
            implementation(libs.compass.geolocation.mobile)

            //Permissions
            implementation("com.mohamedrejeb.calf:calf-permissions:0.9.0")


//            //Scan Barcode
//            implementation("io.github.ismai117:KScan:0.5.0")
//
//            //Easy Permission
//            implementation("network.chaintech:cmp-easy-permission:1.0.3")




        }
        nativeMain.dependencies {
            implementation(libs.ktor.client.darwin)
         //   implementation(libs.okio.zip)

            //SqlDelight
            implementation(libs.native.driver)
        }
    }
}

android {
    namespace = "org.prime.easykarobar"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.prime.easykarobar"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 3
        versionName = "1.0.3"
        val sdf = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        val buildDateTime = sdf.format(Date())
        setProperty("archivesBaseName", "Easy_Karobar_$buildDateTime")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
        }
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
            packageName.set("org.prime.easykarobar")
        }
    }
}
