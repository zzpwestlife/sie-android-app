plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.hiltAndroid)
    alias(libs.plugins.ksp)
    alias(libs.plugins.protobuf)
}

android {
    namespace = "com.sie.core.datastore"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore)
    api(libs.protobuf.kotlin.lite)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.2"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
                create("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

// Ensure KSP runs after Protobuf generation
tasks.withType<com.google.devtools.ksp.gradle.KspTask>().configureEach {
    val taskName = name
    if (taskName.contains("Debug")) {
        dependsOn("generateDebugProto")
    } else if (taskName.contains("Release")) {
        dependsOn("generateReleaseProto")
    }
}

android {
    sourceSets {
        getByName("debug") {
            java.srcDir("build/generated/source/proto/debug/java")
            java.srcDir("build/generated/source/proto/debug/kotlin")
        }
        getByName("release") {
            java.srcDir("build/generated/source/proto/release/java")
            java.srcDir("build/generated/source/proto/release/kotlin")
        }
    }
}
