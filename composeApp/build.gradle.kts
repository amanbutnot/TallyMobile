
import groovy.json.JsonSlurper
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlin.serialization)
    id("app.cash.sqldelight") version "2.1.0"
    id("com.codingfeline.buildkonfig") version "0.22.0"
}

val brand = findProperty("brand")?.toString() ?: "easykarobar"

val brandConfigFile = rootProject.file("brands/$brand/config.json")
val copyBrandResources by tasks.registering(Copy::class) {
    from(rootProject.file("brands/$brand/android/res"))
    into(project.file("src/androidMain/res"))
}

tasks.named("preBuild") {
    dependsOn(copyBrandResources)
}
check(brandConfigFile.exists()) {
    "Brand config not found: ${brandConfigFile.path}"
}

val brandConfig = JsonSlurper()
    .parse(brandConfigFile) as Map<*, *>

val appName = brandConfig["name"] as String

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
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)

            implementation(libs.android.driver)

            implementation(libs.gms.play.services.code.scanner)
            implementation(libs.zxing)

            implementation("com.itextpdf:html2pdf:6.2.1")
            implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")

            implementation("org.apache.poi:poi-ooxml:5.2.5")
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation("org.jetbrains.compose.material3:material3:1.10.0-alpha01")
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.androidx.lifecycle.viewmodelCompose)

            implementation(libs.voyager.navigator)
            implementation(libs.voyager.tabNavigator)
            implementation(libs.voyager.bottomSheetNavigator)
            implementation(libs.voyager.transitions)
            implementation(libs.voyager.screenModel)

            implementation(libs.multiplatform.settings.no.arg)

            implementation(libs.bundles.ktor)
            implementation(libs.runtime)

            implementation(libs.kmp.date.time.picker)

            implementation(libs.okio)

            implementation(libs.material.icons.extended)

            implementation(libs.coroutines.extensions)

            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")

            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs)
            implementation(libs.filekit.dialogs.compose)

            implementation(libs.coil.compose)
            implementation("io.coil-kt.coil3:coil-network-ktor3:3.3.0")

            implementation(libs.compass.geocoder)
            implementation(libs.compass.geocoder.mobile)

            implementation(libs.compass.geolocation)
            implementation(libs.compass.geolocation.mobile)

            implementation("com.mohamedrejeb.calf:calf-permissions:0.9.0")
         //   implementation(libs.maplibre.compose)
        }

        nativeMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.native.driver)
        }
    }
}

android {
    val packageName = brandConfig["packageName"] as String
    val appName = brandConfig["name"] as String

    namespace = "org.prime.easykarobar"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = packageName
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()

        versionCode = 37
        versionName = "1.9.998"

        resValue("string", "app_name", appName)

        val sdf = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        val buildDateTime = sdf.format(Date())

        setProperty(
            "archivesBaseName",
            appName.replace(" ", "_")
        )
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug{
            isMinifyEnabled = false
            isShrinkResources = false
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
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

buildkonfig {
    packageName = "org.prime.easykarobar"
    objectName = "BuildKonfig"
    
    defaultConfigs {
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "STORE_ID", brandConfig["storeId"]?.toString() ?: "")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "STORE_NAME", brandConfig["storeName"]?.toString() ?: "")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "USERNAME", brandConfig["username"]?.toString() ?: "")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "PASSWORD", brandConfig["password"]?.toString() ?: "")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "REGISTERED_NUMBER", (brandConfig["number"] ?: brandConfig["username"])?.toString() ?: "")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "SPLASH_TOP_COLOR", brandConfig["splashtopcolor"]?.toString() ?: "#FFFFFF")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "PACKAGE_NAME", brandConfig["packageName"]?.toString() ?: "#FFFFFF")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "SPLASH_BOTTOM_COLOR", brandConfig["splashbottomcolor"]?.toString() ?: "#FFFFFF")
    }
}