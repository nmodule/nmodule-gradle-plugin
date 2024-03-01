package niagara.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.language.jvm.tasks.ProcessResources
import java.io.File

const val PLUGIN_ID = "niagara.module"

class NiagaraModulePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val niagaraHome = project.providers.gradleProperty("niagara.home")
            .getOrElse(System.getenv("niagara.home"))

        project.extensions.extraProperties.set("niagara.home", niagaraHome)

        project.logger.info("niagara.home=$niagaraHome")

        project.plugins.apply("org.jetbrains.kotlin.jvm")

        if (project.parent != null) {
            project.group = project.parent!!.group
            project.version = project.parent!!.version
        }

        project.extensions.create(EXTENSION, NiagaraModuleExtension::class.java)

        project.repositories.flatDir {
            it.dir("$niagaraHome/bin/ext")
            it.dir("$niagaraHome/modules")
        }

        val nmodule = project.configurations.create("nmodule")

        @Suppress("UNUSED_VARIABLE")
        val nmoduleDepOnly = project.configurations.create("nmoduleDepOnly")
        val uberjar = project.configurations.create("uberjar")
        project.configurations.getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME) {
            it.extendsFrom(nmodule)
            it.extendsFrom(uberjar)
        }

        val processResources =
            project.tasks.named(JavaPlugin.PROCESS_RESOURCES_TASK_NAME, ProcessResources::class.java).get()

        val generateModuleXml = project.tasks.register("generateModuleXml", GenerateModuleXml::class.java) { task ->
            task.useProjectsDependencies = setOf("nmodule", "nmoduleDepOnly")
            task.inputs.files(project.buildFile)
            val moduleInclude = File(project.projectDir, "module-include.xml")
            val modulePermissions = File(project.projectDir, "module-permissions.xml")
            if (moduleInclude.exists()) {
                task.moduleInclude = moduleInclude
            }
            if (modulePermissions.exists()) {
                task.modulePermissions = modulePermissions
            }
            task.outputFile = File(processResources.destinationDir, "META-INF/module.xml")
            task.dependsOn(JavaPlugin.COMPILE_JAVA_TASK_NAME)
        }

        project.tasks.named(JavaPlugin.CLASSES_TASK_NAME) { classes ->
            classes.dependsOn(generateModuleXml)
        }

        val jar = project.tasks.named(JavaPlugin.JAR_TASK_NAME, Jar::class.java) { jar ->
            jar.dependsOn(generateModuleXml)
            jar.archiveVersion.set("")
            jar.from("module.palette")
        }

        project.afterEvaluate {
            val ext = project.extensions.getByType(NiagaraModuleExtension::class.java)
            jar.get().from(uberjar.map { project.zipTree(it) }) {
                it.duplicatesStrategy = ext.duplicatesStrategy
            }
        }

        val install = project.tasks.register("install") { task ->
            task.group = BasePlugin.BUILD_GROUP
            task.dependsOn(jar)
            task.doLast {
                val niagaraHomeDir = File(niagaraHome as String)
                val modulesDir = File(niagaraHomeDir, "modules")
                project.copy {
                    it.from(jar)
                    it.into(modulesDir)
                }
            }
        }

        val nmoduleTest = project.configurations.create("nmoduleTest")

        @Suppress("UNUSED_VARIABLE")
        val nmoduleTestDepOnly = project.configurations.create("nmoduleTestDepOnly")

        project.dependencies.add("nmoduleTestDepOnly", "${project.group}:${project.name}:${project.version}")

        project.configurations.getByName(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME) {
            it.extendsFrom(nmoduleTest)
        }

        val processTestResources =
            project.tasks.named(JavaPlugin.PROCESS_TEST_RESOURCES_TASK_NAME, ProcessResources::class.java).get()

        val generateTestModuleXml = project.tasks.register("generateTestModuleXml", GenerateModuleXml::class.java) { task ->

            task.option.name = "${project.name}Test"
            task.option.moduleName = "${project.parent?.project?.name}Test"
            task.option.description = "Tests for ${project.name}"

            task.useProjectsDependencies = setOf("nmoduleTest", "nmoduleTestDepOnly")
            task.inputs.files(project.buildFile)
            val moduleInclude = File(project.projectDir, "moduleTest-include.xml")
            if (moduleInclude.exists()) {
                task.moduleInclude = moduleInclude
            }
            val modulePermissions = File(project.projectDir, "module-permissions.xml")
            if (modulePermissions.exists()) {
                task.modulePermissions = modulePermissions
            }
            task.outputFile = File(processTestResources.destinationDir, "META-INF/module.xml")
            task.dependsOn(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME)
        }

        project.tasks.named(JavaPlugin.TEST_CLASSES_TASK_NAME) { classes ->
            classes.dependsOn(generateTestModuleXml)
        }

        val testJar = project.tasks.register("buildTestJar", Jar::class.java) { task ->
            task.dependsOn(generateTestModuleXml)

            task.from("build/classes/kotlin/test") {
                it.include("test/**")
                it.exclude("test/META-INF/**")
            }

            task.from("build/resources/test") {
                it.include("META-INF/module.xml")
            }

            task.archiveFileName.set("${project.name}Test.jar")
        }

        @Suppress("UNUSED_VARIABLE")
        val installTest = project.tasks.register("installTest") { task ->
            task.group = BasePlugin.BUILD_GROUP
            task.dependsOn(testJar)
            task.dependsOn(install)
            task.doLast {
                val niagaraHomeDir = File(niagaraHome as String)
                val modulesDir = File(niagaraHomeDir, "modules")
                project.copy {
                    it.from(testJar)
                    it.into(modulesDir)
                }
            }
        }
    }
}
