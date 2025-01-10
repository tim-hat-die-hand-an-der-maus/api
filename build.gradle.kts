import com.diffplug.spotless.LineEnding
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.spotless)
    alias(libs.plugins.quarkus)
    kotlin("jvm") version "2.0.21"
    kotlin("kapt") version "2.0.21"
    kotlin("plugin.jpa") version "2.0.21"
    alias(libs.plugins.versions)
    kotlin("plugin.allopen") version "2.0.21"
}

group = "consulting.timhatdiehandandermaus"
version = "0.3.0"

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
        javaParameters = true
        freeCompilerArgs = listOf(
            "-Xjvm-default=all",
        )
    }
}

kapt {
    arguments {
        arg("mapstruct.defaultComponentModel", "cdi")
        arg("mapstruct.defaultInjectionStrategy", "constructor")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(enforcedPlatform("io.quarkus:quarkus-bom:${libs.versions.quarkus.get()}"))

    implementation(libs.cli)

    implementation(libs.mapstruct.runtime)
    kapt(libs.mapstruct.processor)

    implementation(libs.jackson.kotlin)
    implementation(libs.quarkus.jackson)
    implementation(libs.quarkus.kotlin)
    implementation(libs.quarkus.sentry)

    implementation(libs.bundles.db)

    implementation(libs.quarkus.restclient.core)
    implementation(libs.quarkus.restclient.jackson)

    implementation(libs.quarkus.resteasy.core)
    implementation(libs.quarkus.resteasy.jackson)

    implementation(libs.bundles.smallrye)

    implementation(libs.fuzzywuzzy)

    testImplementation(libs.quarkus.junit)
    testImplementation(libs.flyway.junit)
}

allOpen {
    annotation("io.quarkus.test.junit.QuarkusTest")
    annotation("jakarta.enterprise.context.RequestScoped")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Embeddable")
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.ws.rs.Path")
}

spotless {
    kotlin {
        ktlint()
            .editorConfigOverride(
                mapOf(
                    "ij_kotlin_allow_trailing_comma" to "true",
                    "ij_kotlin_allow_trailing_comma_on_call_site" to "true",
                ),
            )
        lineEndings = LineEnding.UNIX
        endWithNewline()
    }
    kotlinGradle {
        ktlint()
            .editorConfigOverride(
                mapOf(
                    "ij_kotlin_allow_trailing_comma" to "true",
                    "ij_kotlin_allow_trailing_comma_on_call_site" to "true",
                ),
            )
        lineEndings = LineEnding.UNIX
        endWithNewline()
    }
    format("markdown") {
        target("**/*.md")
        lineEndings = LineEnding.UNIX
        endWithNewline()
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}
