import java.util.Base64

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.mhmmad.periodcalculator"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.mhmmad.periodcalculator"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        resourceConfigurations += listOf("en", "en-rGB", "ar", "pt-rBR")
        // Advertising IDs are not health data. Keep configuration outside sources.
        buildConfigField("String", "BANNER_AD_ID", "\"${providers.gradleProperty("huaweiBannerId").getOrElse("")}\"")
        buildConfigField("String", "INTERSTITIAL_AD_ID", "\"${providers.gradleProperty("huaweiInterstitialId").getOrElse("")}\"")
        buildConfigField("Boolean", "ADS_RELEASE_APPROVED", providers.gradleProperty("enableAdRequests").getOrElse("false"))
    }
    flavorDimensions += "monetisation"
    productFlavors {
        create("offline") { dimension = "monetisation"; buildConfigField("Boolean", "ADS_ENABLED", "false") }
        create("huawei") { dimension = "monetisation"; buildConfigField("Boolean", "ADS_ENABLED", "true") }
    }
    buildFeatures { compose = true; buildConfig = true }
    composeOptions { }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    buildTypes {
        getByName("debug") { isMinifyEnabled = false }
        getByName("release") {
            isMinifyEnabled = false
            val keyStore = providers.gradleProperty("releaseStoreFile").orNull
            val storePassword = providers.gradleProperty("releaseStorePassword").orNull
            val alias = providers.gradleProperty("releaseKeyAlias").orNull
            val keyPassword = providers.gradleProperty("releaseKeyPassword").orNull
            if (!keyStore.isNullOrBlank() && !storePassword.isNullOrBlank() && !alias.isNullOrBlank() && !keyPassword.isNullOrBlank()) {
                signingConfig = signingConfigs.maybeCreate("releaseKeys").apply {
                    storeFile = file(keyStore)
                    this.storePassword = storePassword
                    keyAlias = alias
                    this.keyPassword = keyPassword
                }
            }
        }
    }
    androidResources { generateLocaleConfig = false }
}

kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }

ksp { arg("room.schemaLocation", "$projectDir/schemas") }

dependencies {
    val bom = platform("androidx.compose:compose-bom:2025.06.01")
    implementation(bom)
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.1")
    implementation("androidx.room:room-runtime:2.8.5")
    implementation("androidx.room:room-ktx:2.8.5")
    ksp("androidx.room:room-compiler:2.8.5")
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("androidx.work:work-runtime-ktx:2.10.5")
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    add("huaweiImplementation", "com.huawei.hms:ads-prime:3.4.91.300")
    add("huaweiImplementation", "com.huawei.hms:ads-consent:3.4.91.300")
    testImplementation("junit:junit:4.13.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
