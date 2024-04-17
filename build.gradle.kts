plugins {
    kotlin("jvm") version Versions.kotlin
    id("io.quarkus") version Versions.quarkus
    id("io.gitlab.arturbosch.detekt") version Versions.detekt
    id("com.github.ben-manes.versions") version Versions.gradleVersions
    id("de.stefan-oltmann.git-versioning") version Versions.gitVersioning
}

description = "Stefans Smart Home Server"
group = "de.stefan_oltmann.smarthome.server"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
    kotlinOptions.javaParameters = true
}

detekt {

    config = files("$projectDir/detekt.yml")

    // Don't break the build. Just report.
    ignoreFailures = true
}

repositories {
    mavenCentral()
}

dependencies {

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}")

    // Quarkus
    implementation(enforcedPlatform("io.quarkus:quarkus-universe-bom:${Versions.quarkus}"))
    implementation("io.quarkus:quarkus-kotlin:${Versions.quarkus}")
    implementation("io.quarkus:quarkus-resteasy-jackson:${Versions.quarkus}")

    // KNX
    implementation("li.pitschmann:knx-core:${Versions.knxCore}")
    implementation("li.pitschmann:knx-core-plugin-audit:${Versions.knxCore}")

    // Serialization
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:${Versions.kotlinCsv}")

    // Persistence
    implementation("com.influxdb:influxdb-client-kotlin:${Versions.influxDb}")

    // Logging
    implementation("org.slf4j:slf4j-api:${Versions.slf4j}")
    implementation("ch.qos.logback:logback-classic:${Versions.logback}")

    // Unit tests
    testImplementation(kotlin("test"))

    // Detekt formatting
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${Versions.detekt}")
}

tasks.test {
    useJUnitPlatform()
}
