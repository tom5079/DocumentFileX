plugins {
    id("com.android.library")
    id("kotlin-android")
    id("org.jetbrains.dokka") version "1.4.32"
    `maven-publish`
    signing
}

group = "xyz.quaver"
version = "0.7.3"

android {
    compileSdk = 30
    buildToolsVersion = "30.0.3"

    defaultConfig {
        minSdk = 14
        targetSdk = 30

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
        moduleName = "xyz.quaver.io.documentfilex"
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("androidx.core:core-ktx:1.6.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.4.0")
}

val ossrhUsername: String? by project
val ossrhPassword: String? by project

val sourceJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(android.sourceSets["main"].java.srcDirs)
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
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
                artifact(javadocJar)

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
                            name.set("Minseok Son")
                            email.set("tom5079@naver.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/tom5079/DocumentFileX.git")
                        developerConnection.set("scm:git:ssh://github.com:tom5079/DocumentFileX.git")
                        url.set("https://github.com/tom5079/DocumentFileX")
                    }
                }
            }
        }

        repositories {
            maven {
                val releasesRepoUrl = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
                val snapshotRepoUrl = uri("https://central.sonatype.com/repository/maven-snapshots/")

                url = if (version.toString().endsWith("SNAPSHOT")) snapshotRepoUrl else releasesRepoUrl

                credentials {
                    username = ossrhUsername
                    password = ossrhPassword
                }
            }
        }
    }
    
    signing {
        sign(publishing.publications)
    }
}