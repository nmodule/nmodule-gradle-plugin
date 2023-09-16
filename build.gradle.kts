import com.diffplug.spotless.LineEnding

plugins {
    kotlin("jvm") version embeddedKotlinVersion
    id("java-gradle-plugin")
    id("maven-publish")
    id("org.jetbrains.changelog") version "1.3.1"
    id("com.diffplug.spotless") version "6.12.0"
    id("com.star-zero.gradle.githook") version "1.2.1"
    id("com.gradle.plugin-publish") version "0.18.0"
}

group = "com.restartech"
version = "0.6.0"

repositories {
    maven("https://maven.aliyun.com/repository/public/")
    mavenCentral()
}

dependencies {
}

gradlePlugin {
    plugins.create("niagaraModulePlugin") {
        id = "com.restartech.nmodule"
        displayName = "Niagara Module Gradle Plugin"
        description = "A gradle plugin to build Niagara module in Kotlin"
        implementationClass = "niagara.gradle.NiagaraModulePlugin"
    }
}

pluginBundle {
    website = "https://github.com/nmodule/nmodule-gradle-plugin"
    vcsUrl = "https://github.com/nmodule/nmodule-gradle-plugin"
    tags = listOf("niagara", "nmodule", "kotlin")
}

spotless {
    lineEndings = com.diffplug.spotless.LineEnding.UNIX

    val editorConfigOverride = mapOf(
        "ij_kotlin_allow_trailing_comma" to true,
        "ij_kotlin_allow_trailing_comma_on_call_site" to true,
        "ktlint_disabled_rules" to "no-wildcard-imports",
    )

    kotlin {
        target("**/*.kt")
        ktlint("0.47.1").editorConfigOverride(editorConfigOverride)
    }

    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint("0.47.1").editorConfigOverride(editorConfigOverride)
    }

    freshmark {
        target("**/*.md")
    }
}

githook {
    hooks {
        create("pre-commit") {
            shell = "./gradlew check"
        }
    }
}
