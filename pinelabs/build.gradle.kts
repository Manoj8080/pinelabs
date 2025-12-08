plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    id("signing")
}

android {
    namespace = "com.nukkadshops.pinelabs"
    compileSdk = 36

    defaultConfig {
        minSdk = 21   // REQUIRED to avoid lint errors like statusBarColor issue
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    testImplementation(libs.junit)
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                // Maven Central coordinates
                groupId = "com.nukkadshops.pinelabs"
                artifactId = "pinelabs-sdk"
                version = "1.0.0"

                pom {
                    name.set("Pinelabs SDK")
                    description.set("Android SDK for Pine Labs payments.")
                    url.set("https://github.com/Manoj8080/pinelabs")

                    licenses {
                        license {
                            name.set("Apache License 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0")
                        }
                    }

                    developers {
                        developer {
                            id.set("manoj")
                            name.set("Manoj")
                            email.set("manoj.made@nukkadshops.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:github.com/Manoj8080/pinelabs.git")
                        developerConnection.set("scm:git:ssh://github.com/Manoj8080/pinelabs.git")
                        url.set("https://github.com/Manoj8080/pinelabs")
                    }
                }
            }
        }

        repositories {
            maven {
                name = "centralPortal"
                url = uri("https://central.sonatype.com/api/v1/publisher/upload")
                credentials {
                    username = findProperty("ossrhUsername") as String?
                    password = findProperty("ossrhPassword") as String?
                }
            }
        }

    }

    signing {
        sign(publishing.publications["release"])
    }
}
