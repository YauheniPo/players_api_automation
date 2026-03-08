plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
}

group = "com.automation"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// ── ktlint ────────────────────────────────────────────────────────────────────
ktlint {
    version.set("1.2.1") // ktlint engine version (decoupled from plugin version)
    android.set(false)
    ignoreFailures.set(false) // fail the build on style violations
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.HTML)
    }
    filter {
        exclude("**/generated/**")
    }
}

// ktlintCheck runs automatically before compileKotlin — violations break the build early
tasks.named("compileKotlin") { dependsOn(tasks.named("ktlintMainSourceSetCheck")) }
tasks.named("compileTestKotlin") { dependsOn(tasks.named("ktlintTestSourceSetCheck")) }

// ── separate config to resolve the AspectJ java-agent jar path ────────────────
val aspectjAgent: Configuration by configurations.creating

dependencies {
    aspectjAgent(libs.aspectjweaver)

    // REST Assured
    implementation(libs.restassured.core)

    // Jackson + Kotlin module (needed for data-class deserialization)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.kotlin)

    // Owner — type-safe config from .properties / env vars
    implementation(libs.owner)

    // DataFaker — realistic test-data generation
    implementation(libs.datafaker)

    // Logging
    implementation(libs.slf4j.api)
    testImplementation(libs.logback.classic)

    // TestNG
    testImplementation(libs.testng)

    // AssertJ — fluent assertions
    testImplementation(libs.assertj.core)

    // Allure
    implementation(libs.allure.restassured)
    testImplementation(libs.allure.testng)
    testImplementation(libs.aspectjweaver)
}

// ── common test JVM configuration shared between both test tasks ──────────────
fun Test.applyTestConfig() {
    jvmArgs("-javaagent:${aspectjAgent.singleFile}")
    systemProperty("allure.results.directory", "${layout.buildDirectory.get()}/allure-results")
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = false
    }
}

// ── Full suite run (default) ───────────────────────────────────────────────────
// ./gradlew test
tasks.test {
    useTestNG {
        suites("src/test/resources/testng.xml")
    }
    applyTestConfig()
}

// ── Single test / class run ───────────────────────────────────────────────────
// ./gradlew testSingle --tests "com.automation.tests.AuthTest"
// ./gradlew testSingle --tests "com.automation.tests.AuthTest.loginReturnsValidToken"
// NOTE: tests with dependsOnGroups (PlayerTest) cannot be run individually
tasks.register<Test>("testSingle") {
    useTestNG()
    applyTestConfig()
}
