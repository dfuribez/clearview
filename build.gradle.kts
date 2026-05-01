plugins {
    kotlin("jvm") version "1.9.24"
    id("com.gradleup.shadow") version "9.3.1"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("net.portswigger.burp.extensions:montoya-api:2026.2")
    implementation("com.miglayout:miglayout-swing:11.3")
    implementation("org.jsoup:jsoup:1.22.2")
    implementation("com.fifesoft:rsyntaxtextarea:3.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "21"
    targetCompatibility = "21"
    options.encoding = "UTF-8"
}

tasks.jar {
    enabled = false
}

tasks.shadowJar {
    archiveClassifier.set("")
    mergeServiceFiles()
}