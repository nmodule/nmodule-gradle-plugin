package niagara.gradle

import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.tasks.*
import org.gradle.api.tasks.Optional
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*
import javax.xml.stream.XMLEventWriter
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.events.XMLEvent

open class GenerateModuleXml : DefaultTask() {

    @Input
    val option: ModuleOption = ModuleOption(project)

    @InputFile
    @Optional
    var moduleInclude: File? = null

    @InputFile
    @Optional
    var modulePermissions: File? = null

    @OutputFile
    var outputFile: File? = null

    @TaskAction
    fun doGenerate() {
        val factory = XMLOutputFactory.newFactory()
        val fw = FileWriter(outputFile)
        val sw = IndentingXMLStreamWriter(factory.createXMLStreamWriter(fw))
        val ew = factory.createXMLEventWriter(fw)
        sw.writeStartDocument()
        // module
        sw.writeStartElement("module")
        sw.writeAttribute("name", option.name)
        sw.writeAttribute("moduleName", option.moduleName)
        sw.writeAttribute("runtimeProfile", option.runtimeProfile)
        sw.writeAttribute("bajaVersion", option.bajaVersion)
        sw.writeAttribute("vendor", option.vendor)
        sw.writeAttribute("vendorVersion", option.vendorVersion)
        sw.writeAttribute("description", option.description)
        sw.writeAttribute("preferredSymbol", option.preferredSymbol)
        sw.writeAttribute("nre", "${option.nre}")
        sw.writeAttribute("installable", "${option.installable}")
        sw.writeAttribute("autoload", "${option.autoload}")
        sw.writeAttribute("buildMillis", "${Date().time}")
        sw.writeAttribute("buildHost", "UNKNOWN")
        // dependencies
        sw.writeStartElement("dependencies")
        project.configurations.getByName("nmodule").dependencies
                .filter {
                    it.group?.matches(Regex("^\\w+$")) ?: false
                }
                .forEach { dep ->
                    sw.writeStartElement("dependency")
                    var suffix = ""
                    if (dep is ModuleDependency) {
                        if (dep.artifacts.isNotEmpty()) {
                            val classifier = dep.artifacts.first().classifier
                            suffix += "-$classifier"
                        }
                    }
                    sw.writeAttribute("name", dep.name + suffix)
                    sw.writeAttribute("vendor", dep.group)
                    sw.writeAttribute("vendorVersion", dep.version)
                    sw.writeEndElement()
                }
        if (option.runtimeProfile == "wb") {
            sw.writeStartElement("dependency")
            sw.writeAttribute("name", "${option.moduleName}-rt")
            sw.writeAttribute("vendor", option.vendor)
            sw.writeAttribute("vendorVersion", option.vendorVersion)
            sw.writeEndElement()
        }
        sw.writeEndElement()
        sw.flush()
        moduleInclude?.let { mergeXml(it, fw, ew) }
        modulePermissions?.let { mergeXml(it, fw, ew) }
        sw.writeEndElement() // end module
        sw.writeEndDocument()
        sw.flush()
    }

    private fun mergeXml(file: File, fw: FileWriter, ew: XMLEventWriter) {
        fw.write("\n\n")
        val inputFactory = XMLInputFactory.newInstance()
        val moduleIncludeReader = inputFactory.createXMLEventReader(FileReader(file))
        ew.add(inputFactory.createFilteredReader(moduleIncludeReader) { ev ->
            ev.eventType != XMLEvent.START_DOCUMENT && ev.eventType != XMLEvent.END_DOCUMENT
        })
        ew.flush()
        fw.write("\n\n")
    }
}
