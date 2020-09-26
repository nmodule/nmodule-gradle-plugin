import com.diffplug.spotless.LineEnding
import java.nio.charset.Charset

plugins {
    kotlin("jvm") version "1.3.71"
    id("java-gradle-plugin")
    id("maven-publish")
    id("com.diffplug.spotless") version "5.6.1"
    id("com.star-zero.gradle.githook") version "1.2.1"
}

group = "niagara"
version = "0.0.1-SNAPSHOT"

repositories {
    maven("https://maven.aliyun.com/repository/central/")
    mavenCentral()
}

dependencies {
}

gradlePlugin {
    plugins.create("niagaraModulePlugin") {
        id = "niagara.module"
        implementationClass = "niagara.gradle.NiagaraModulePlugin"
    }
}

spotless {
    encoding = Charset.forName("UTF-8")
    lineEndings = LineEnding.UNIX

    val ktlintUserData = mapOf(
        "disabled_rules" to "no-wildcard-imports,import-ordering"
    )

    kotlin {
        target("**/*.kt")
        ktlint().userData(ktlintUserData)
    }

    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint().userData(ktlintUserData)
    }
}

githook {
    hooks {
        create("pre-commit") {
            shell = "./gradlew.bat check"
        }
    }
}
