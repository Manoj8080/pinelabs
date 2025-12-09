plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.nukkadshops.pinelabs"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    // AndroidX
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2")

    // Retrofit + Gson + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
}

val releaseVersion = "1.0.10"

// -----------------------------------------------------
// 1️⃣ ZIP PACKAGE FOR SONATYPE CENTRAL
// -----------------------------------------------------
val bundleReleaseForPublication by tasks.registering(Zip::class) {
    group = "publishing"

    archiveBaseName.set("pinelabs-sdk")
    archiveVersion.set(releaseVersion)
    destinationDirectory.set(layout.buildDirectory.dir("publishing"))

    dependsOn("assembleRelease")

    // Include AAR
    from(layout.buildDirectory.file("outputs/aar/pinelabs-release.aar")) {
        rename { "pinelabs-sdk-$releaseVersion.aar" }
    }

    // Include custom POM from module root
    from("pinelabs-pom.xml") {
        rename { "pom.xml" }
    }
}

// -----------------------------------------------------
// 2️⃣ UPLOAD TASK USING POST (Sonatype Central API)
// -----------------------------------------------------
tasks.register<Exec>("uploadToSonatypeCentral") {
    dependsOn(bundleReleaseForPublication)

    val username = findProperty("ossrhUsername")?.toString()
    val password = findProperty("ossrhPassword")?.toString()

    if (username.isNullOrBlank() || password.isNullOrBlank()) {
        throw GradleException("Missing ossrhUsername / ossrhPassword in gradle.properties")
    }

    val zipFile = layout.buildDirectory.file("publishing/pinelabs-sdk-$releaseVersion.zip")
        .get().asFile.absolutePath

    println("Uploading ZIP: $zipFile")

    commandLine(
        "curl",
        "-u", "$username:$password",
        "-X", "POST",
        "-H", "Content-Type: application/zip",
        "--data-binary", "@${zipFile}",
        "https://central.sonatype.com/api/v1/publisher/upload"
    )
}
