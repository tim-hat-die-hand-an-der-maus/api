import com.diffplug.spotless.LineEnding
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.spotless)
    kotlin("jvm") version libs.versions.kotlin
    kotlin("kapt") version libs.versions.kotlin
    kotlin("plugin.jpa") version libs.versions.kotlin
    alias(libs.plugins.quarkus)
    alias(libs.plugins.versions)
    kotlin("plugin.allopen") version libs.versions.kotlin
}

group = "consulting.timhatdiehandandermaus"
version = "0.2.0"

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            javaParameters = true
            freeCompilerArgs = listOf(
                "-Xjvm-default=all",
            )
        }
    }
}

kapt {
    arguments {
        arg("mapstruct.defaultComponentModel", "cdi")
        arg("mapstruct.defaultInjectionStrategy", "constructor")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8", version = libs.versions.kotlin.get()))

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

    testImplementation(libs.quarkus.junit)
    testImplementation(libs.flyway.junit)
}

allOpen {
    annotation("io.quarkus.test.junit.QuarkusTest")
    annotation("javax.enterprise.context.RequestScoped")
    annotation("javax.enterprise.context.ApplicationScoped")
    annotation("javax.persistence.Embeddable")
    annotation("javax.persistence.Entity")
    annotation("javax.ws.rs.Path")
}

spotless {
    kotlin {
        ktlint()
        lineEndings = LineEnding.UNIX
        endWithNewline()
    }
    kotlinGradle {
        ktlint()
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
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}
