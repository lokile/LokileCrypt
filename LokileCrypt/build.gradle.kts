import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.vanniktech.publish)
}

android {
    namespace = "com.lokile.encrypter"
    compileSdk = 36

    defaultConfig {
        minSdk = 23

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

mavenPublishing {
    coordinates(
        groupId = "io.github.lokile",
        artifactId = "lokile-crypt",
        version = "1.2.7"
    )

    // Configure POM metadata for the published artifact
    pom {
        // General information
        name.set("lokile-crypt")
        description.set("An Android library for data encryption and decryption")
        inceptionYear.set("2022")
        url.set("https://github.com/lokile/LokileCrypt")

        // License information
        licenses {
            license {
                name.set("Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }

        // Specify developers information
        developers {
            developer {
                id.set("lokile")
                name.set("Loki Le")
                email.set("lokile208@gmail.com")
                url.set("https://github.com/lokile")
            }
        }

        // Specify SCM information
        scm {
            url.set("https://github.com/lokile/LokileCrypt")
            connection.set("scm:git:git://github.com/lokile/LokileCrypt.git")
            developerConnection.set("scm:git:ssh://git@github.com/lokile/LokileCrypt.git")
        }
    }

    // Configure publishing to Maven Central
    publishToMavenCentral()

    // Enable GPG signing for all publications
    if (
        providers.gradleProperty("signingInMemoryKey").isPresent ||
        providers.gradleProperty("signing.secretKeyRingFile").isPresent
    ) {
        signAllPublications()
    }
}
