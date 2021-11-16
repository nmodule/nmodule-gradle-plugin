package niagara.gradle

const val EXTENSION = "nmodule"

open class NiagaraModuleExtension {
    var name: String? = null
    var moduleName: String? = null
    var runtimeProfile: String? = null
    var bajaVersion = "0"
    var vendor: String? = null
    var vendorVersion: String? = null
    var description: String? = null
    var preferredSymbol: String? = null
    var nre = true
    var installable = true
    var autoload = true
    var installSources = false
}
