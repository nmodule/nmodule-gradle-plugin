import com.diffplug.spotless.LineEnding
import java.nio.charset.Charset

plugins {
    kotlin("jvm") version embeddedKotlinVersion
    id("java-gradle-plugin")
    id("maven-publish")
    id("org.jetbrains.changelog") version "1.3.1"
    id("com.diffplug.spotless") version "5.17.1"
    id("com.star-zero.gradle.githook") version "1.2.1"
    id("com.gradle.plugin-publish") version "0.18.0"
}

group = "com.restartech"
version = "0.3.2"

repositories {
    // maven("https://maven.aliyun.com/repository/central/")
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
    encoding = Charset.forName("UTF-8")
    lineEndings = LineEnding.UNIX

    val ktlintVersion = "0.41.0"
    val ktlintUserData = mapOf(
        "disabled_rules" to "no-wildcard-imports,import-ordering"
    )

    kotlin {
        target("**/*.kt")
        ktlint(ktlintVersion).userData(ktlintUserData)
    }

    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint(ktlintVersion).userData(ktlintUserData)
    }

    freshmark {
        target("**/*.md")
    }
}

githook {
    hooks {
        create("pre-commit") {
            shell = "./gradlew.bat check"
        }
    }
}
