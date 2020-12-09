package niagara.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Jar
import org.gradle.language.jvm.tasks.ProcessResources
import java.io.File

const val PLUGIN_ID = "niagara.module"

class NiagaraModulePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val niagaraHome = System.getenv("niagara_home") ?: project.logger.error("niagara_home env variable not defined")

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
            jar.get().from(uberjar.map { project.zipTree(it) })
        }

        val sourcesJar = project.tasks.register("sourcesJar", Jar::class.java) { sourcesJar ->
            sourcesJar.archiveVersion.set("")
            sourcesJar.archiveClassifier.set("sources")
            val main = project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets.named("main")
            sourcesJar.from(main.get().allSource)
        }

        val install = project.tasks.register("install", Copy::class.java) { task ->
            task.group = BasePlugin.BUILD_GROUP
            task.from(jar, sourcesJar)
            task.into("$niagaraHome/modules")
        }

        project.tasks.named(JavaBasePlugin.BUILD_TASK_NAME) { task ->
            task.dependsOn(install)
        }
    }
}
