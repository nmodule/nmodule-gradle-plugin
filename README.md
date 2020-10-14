# nmodule-gradle-plugin

[![Maven metadata URL](https://img.shields.io/maven-metadata/v?label=plugin&logo=gradle&metadataUrl=https%3A%2F%2Fplugins.gradle.org%2Fm2%2Fniagara%2Fmodule%2Fmaven-metadata.xml)](https://plugins.gradle.org/plugin/niagara.module)

A gradle plugin to build Niagara module in Kotlin

# Gradle Project Setup

## settings.gradle.kts

```kotlin
rootProject.name = "moduleName"

arrayOf("rt", "wb", "ux", "se").forEach { profile ->
    val dir = File(rootDir, profile)
    if (dir.isDirectory) {
        include(profile)
        project(dir).name = "${rootProject.name}-$profile"
    }
}
```

## build.gradle.kts

```kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10" apply false
    kotlin("plugin.serialization") version "1.4.10" apply false
    id("niagara.module") version "0.0.1-SNAPSHOT" apply false
}

group = "Vendor"
version = "0.0.1"
description = "niagara module"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "niagara.module")
    dependencies {
        "implementation"(kotlin("stdlib-jdk8"))
        "nmodule"("Tridium:baja:4.0")
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "1.8"
        }
    }
}
```
