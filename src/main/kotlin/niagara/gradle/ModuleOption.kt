package niagara.gradle

import org.gradle.api.Project
import java.io.Serializable

data class ModuleOption(
    var name: String = "",
    var moduleName: String = "",
    var runtimeProfile: String = "",
    var bajaVersion: String = "",
    var vendor: String = "",
    var vendorVersion: String = "",
    var description: String = "",
    var preferredSymbol: String = "",
    var nre: Boolean = true,
    var installable: Boolean = true,
    var autoload: Boolean = true
) : Serializable {
    constructor(project: Project) : this() {
        val ext = project.extensions.getByType(NiagaraModuleExtension::class.java)
        name = project.name
        val ss = name.split("-")
        moduleName = ext.moduleName ?: ss[0]
        runtimeProfile = ext.runtimeProfile ?: ss[1]
        bajaVersion = ext.bajaVersion
        vendor = "${ext.vendor ?: project.group}"
        vendorVersion = "${ext.vendorVersion ?: project.version}"
        description = "${ext.description ?: (project.description ?: project.parent?.description)}"
        preferredSymbol = ext.preferredSymbol ?: moduleName
    }
}
