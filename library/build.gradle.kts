plugins {
    id("com.android.library")
    id("kotlin-android")
    id("org.jetbrains.dokka")
    `maven-publish`
    signing
}

group = "xyz.quaver"
version = "0.5"

android {
    compileSdkVersion(30)
    buildToolsVersion = "29.0.3"

    defaultConfig {
        minSdkVersion(14)
        targetSdkVersion(30)
        versionCode = 1
        versionName = project.version.toString()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("androidx.core:core-ktx:1.3.2")
    testImplementation("junit:junit:4.13.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.3.0")
}

val ossrhUsername: String by project
val ossrhPassword: String by project

val sourceJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(android.sourceSets["main"].java.srcDirs)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = group.toString()
                artifactId = "documentfilex"
                version = project.version as String

                from(components["release"])
                artifact(sourceJar)

                pom {
                    name.set("documentfilex")
                    description.set("java.io.File compatible SAF implementation")
                    url.set("https://github.com/tom5079/DocumentFileX")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("tom5079")
                            email.set("tom5079@naver.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/tom5079/DocumentFileX.git")
                        url.set("https://github.com/tom5079/DocumentFileX")
                    }
                }
            }
        }

        repositories {
            maven {
                val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
                val snapshotRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots")

                url = if (version.toString().endsWith("SNAPSHOT")) snapshotRepoUrl else releasesRepoUrl

                credentials {
                    username = ossrhUsername
                    password = ossrhPassword
                }
            }
        }
    }
}