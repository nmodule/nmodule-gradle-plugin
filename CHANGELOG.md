# Changelog

## [0.9.0]
### Changed
- Update gradle to 8.11.1
- com.gradle.plugin-publish to 1.3.0
- Update kotlin to 2.1.0

## [0.8.0]
### Changed
- Update gradle to 8.7
- com.gradle.plugin-publish to 1.2.1
- set java sourceCompatibility and targetCompatibility

## [0.7.0]
### Changed
- Change task depends: compileJava -> generateModuleXml -> classes

## [0.6.1]
### Changed
- Update gradle to 8.4

## [0.6.0]
### Changed
- Update gradle to 8.2.1

## [0.5.1]
### Changed
- Change log level to info

## [0.5.0]
### Changed
- Change env niagara_home to niagara.home and get value from gradle property first

## [0.4.1]
### Changed
- Use project.copy in install task

## [0.4.0]
### Changed
- Update gradle to 7.6.1
- Support Java 17

## [0.3.4]
### Changed
- Add DuplicatesStrategy configuration

## [0.3.3]
### Changed
- Remove usage of IndentingXMLStreamWriter

## [0.3.2]
### Changed
- Downgrade gradle to 6.9.2

## [0.3.1]
### Changed
- Remove dependency of build on install
### Fixed
- Add buildFile into generateModuleXml inputs

## [0.3.0]
### Changed
- Update gradle to 7.3
- Disable auto install sources
- Update spotless
### Fixed
- jar duplicatesStrategy

## [0.2.0]
### Added
- nmoduleDepOnly configuration
### Removed
- Auto applying kotlin kapt and serialization plugins

## [0.1.0]
### Added
- Organize module profiles as subprojects
- Configure kotlin plugins
- Add *nmodule* and *uberjar* configurations
