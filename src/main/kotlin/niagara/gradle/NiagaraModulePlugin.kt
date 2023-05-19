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

        project.logger.warn("niagara.home=$niagaraHome")

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
        val nmoduleDepOnly = project.configurations.create("nmoduleDepOnly")
        val uberjar = project.configurations.create("uberjar")
        project.configurations.getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME) {
            it.extendsFrom(nmodule)
            it.extendsFrom(uberjar)
        }

        val processResources =
            project.tasks.named(JavaPlugin.PROCESS_RESOURCES_TASK_NAME, ProcessResources::class.java).get()

        val generateModuleXml = project.tasks.register("generateModuleXml", GenerateModuleXml::class.java) { task ->
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
            task.dependsOn(JavaPlugin.CLASSES_TASK_NAME)
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
    }
}
