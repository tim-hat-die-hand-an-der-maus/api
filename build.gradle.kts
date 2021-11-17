import com.diffplug.spotless.LineEnding
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.spotless)
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.quarkus)
    kotlin("plugin.allopen") version libs.versions.kotlin
}

group = "consulting.timhatdiehandandermaus"
version = "0.1.0"

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            javaParameters = true
        }
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8", version = libs.versions.kotlin.get()))

    implementation(enforcedPlatform("io.quarkus:quarkus-bom:${libs.versions.quarkus.get()}"))

    implementation(libs.quarkus.kotlin)
    implementation(libs.quarkus.jackson)
    implementation(libs.quarkus.resteasy.core)
    implementation(libs.quarkus.resteasy.jackson)
    implementation(libs.bundles.smallrye)
    implementation(libs.jackson.kotlin)

    testImplementation(libs.quarkus.junit)
}

allOpen {
    annotation("javax.ws.rs.Path")
    annotation("javax.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

spotless {
    kotlin {
        ktlint(libs.versions.ktlint.get())
        lineEndings = LineEnding.UNIX
        endWithNewline()
    }
    kotlinGradle {
        ktlint(libs.versions.ktlint.get())
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
