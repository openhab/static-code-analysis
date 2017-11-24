# ESH Guidelines Covered

## A. Code Style

| Guideline | Check | Severity |
|-----------|-------|----------|
| The Java naming conventions should be used. | com.puppycrawl.tools.checkstyle.checks.naming.PackageNameCheck | info |
|  | com.puppycrawl.tools.checkstyle.checks.naming.TypeNameCheck | info |
|  | com.puppycrawl.tools.checkstyle.checks.naming.MethodNameCheck | info |
|  | com.puppycrawl.tools.checkstyle.checks.naming.ConstantNameCheck | info |
|  | com.puppycrawl.tools.checkstyle.checks.naming.LocalFinalVariableNameCheck | info |
|  | com.puppycrawl.tools.checkstyle.checks.naming.LocalVariableName | info |
|  | com.puppycrawl.tools.checkstyle.checks.naming.StaticVariableName | info |
| Every Java file must have a license header. | org.openhab.tools.analysis.checkstyle.ParameterizedRegexpHeaderCheck | error |
| Every class, interface and enumeration should have JavaDoc describing its purpose and usage. | org.openhab.tools.analysis.checkstyle.JavadocFilterCheck | warning |
| Every class, interface and enumeration must have an @author tag in its,JavaDoc for every author that wrote a substantial part of the file. | org.openhab.tools.analysis.checkstyle.AuthorTagCheck | error |
| Every constant, field and method with default, protected or public,visibility should have JavaDoc (optional, but encouraged for private,visibility as well). | https://github.com/openhab/static-code-analysis/issues/219 |  |
| Formatting : java files must use spaces for formatting, rather than tabs | com.puppycrawl.tools.checkstyle.checks.whitespace.FileTabCharacterCheck | error |
| Formatting : xml files must use tabs for formatting, rather than spaces | https://github.com/openhab/static-code-analysis/issues/223 |  |
| Formatting : json files must use tabs for formatting, rather than spaces | Not covered yet |  |
| Generics must be used where applicable. | Not covered yet |  |
| Code should not show any warnings. Warnings that cannot be circumvented,should be suppressed by using the @SuppressWarnings annotation. | Not covered yet |  |
| For dependency injection, OSGi Declarative Services should be used. | org.openhab.tools.analysis.checkstyle.DeclarativeServicesDependencyInjectionCheck | warning |
| Packages that contain classes that are not meant to be used by other bundles should have “internal” in their package name. | org.openhab.tools.analysis.checkstyle.PackageExportsNameCheck | warning |
| We are using null annotations from the Eclipse JDT project. Therefore every bundle should have an optional Import-Package dependency to org.eclipse.jdt.annotation. Classes should be annotated by @NonNullByDefault and return types, parameter types, generic types etc. are annotated with @Nullable only. There is no need for a @NonNull annotation because it is set as default. | https://github.com/openhab/static-code-analysis/issues/218 | |


## B. OSGi Bundles

| Guideline | Check | Severity |
|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------|----------|
| Every bundle must contain a Maven pom.xml with a version and artifact name that is in sync with the manifest entry. The pom.xml must reference the correct parent pom (which is usually in the parent folder). | org.openhab.tools.analysis.checkstyle.PomXmlCheck | Error |
| Every bundle must contain an about.html file, providing license information. | org.openhab.tools.analysis.checkstyle.AboutHtmlCheck | Error |
| Every bundle must contain a build.properties file, which lists all resources that should end up in the binary under bin.includes. | org.openhab.tools.analysis.checkstyle.BuildPropertiesCheck | Warning |
| The manifest must not contain any “Require-Bundle” entries. Instead, “Import-Package” must be used. | org.openhab.tools.analysis.checkstyle.RequireBundleCheck | Error |
| The manifest must not export any internal package. | org.openhab.tools.analysis.checkstyle.ExportInternalPackageCheck | Error |
| The manifest must not have any version constraint on package imports, unless this is thoughtfully added. | org.openhab.tools.analysis.checkstyle.ManifestPackageVersionCheck | Warning |
| The manifest must include all services in the Service-Component entry. A good approach is to put OSGI-INF/*.xml in there. | org.openhab.tools.analysis.checkstyle.ServiceComponentManifestCheck | Error |
| Every exported package of a bundle must be imported by the bundle itself again. | org.openhab.tools.analysis.checkstyle.ImportExportedPackagesCheck | Warning |

## C. Language Levels and Libraries

| Guideline | Check | Severity |
|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------|----------|
| Eclipse SmartHome generally targets JavaSE 8 with the following restrictions:
To allow optimized JavaSE 8 runtimes, the set of Java packages to be used is furthermore restricted to Compact Profile 2
Java 5 for org.eclipse.smarthome.protocols.enocean.* | https://github.com/openhab/static-code-analysis/issues/214 | |
| The minimum OSGi framework version supported is OSGi R4.2, no newer features must be used. | Not covered yet |  |
| For logging, slf4j (v1.7.2) is used. | https://github.com/openhab/static-code-analysis/issues/220 |  |

## D. Runtime Behavior


| Guideline | Check | Severity |
|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------|----------|
| Overridden methods from abstract classes or interfaces are expected to return fast unless otherwise stated in their JavaDoc. Expensive operations should therefore rather be scheduled as a job. | Not covered yet | |
| For periodically executed jobs that do not require a fixed rate scheduleWithFixedDelay should be preferred over scheduleAtFixedRate. | org.openhab.tools.analysis.checkstyle.AvoidScheduleAtFixedRateCheck | Warning |
| Bundles need to cleanly start and stop without throwing exceptions or malfunctioning. This can be tested by manually starting and stopping the bundle from the console (stop <bundle-id> resp. start <bundle-id>). | Not covered yet | |
| Bundles must not require any substantial CPU time. Test this e.g. using “top” or VisualVM and compare CPU utilization with your bundle stopped vs. started. | Not covered yet | | 

## E. Logging


| Guideline | Check | Severity |
|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------|----------|
| As we are in a dynamic OSGi environment, loggers should be non-static, when ever possible and have the name logger. | jp.skypencil.findbugs.slf4j.StaticLoggerDetector | Warning |
| Parametrized logging must be used (instead of string concatenation). | jp.skypencil.findbugs.slf4j.ManualMessageDetector | Error |
| Where ever unchecked exceptions are caught and logged, the exception should be added as a last parameter to the logging. For checked exceptions, this is normally not recommended, unless it can be considered an error situation and the stacktrace would hold additional important information for the analysis. | https://github.com/openhab/static-code-analysis/issues/221 ||
| Logging levels should focus on the system itself and describe its state. As every bundle is only one out of many, logging should be done very scarce. It should be up to the user to increase the logging level for specific bundles, packages or classes if necessary. | Not covered yet |  |
| For bindings, you should NOT log errors, if e.g. connections are dropped - this is considered to be an external problem and from a system perspective to be a normal and expected situation. The correct way to inform users about such events is to update the Thing status accordingly. Note that all events (including Thing status events) are anyhow already logged. | Not covered yet |  |
| Likewise, bundles that accept external requests (such as servlets) must not log errors or warnings if incoming requests are incorrect. Instead, appropriate error responses should be returned to the caller. | Not covered yet |  |

# Implemented Checks

## Checkstyle
- [included checks](https://github.com/openhab/static-code-analysis/blob/master/src/main/resources/rulesets/checkstyle/rules.xml)
- [documentation for the checks](http://checkstyle.sourceforge.net/checks.html)

## PMD
- [included rules](https://github.com/openhab/static-code-analysis/blob/master/src/main/resources/rulesets/pmd/rules.xml)
- [documentation for the rules](http://pmd.sourceforge.net/pmd-4.3.0/rules/index.html)

## FindBugs
- [included rules](https://github.com/openhab/static-code-analysis/blob/master/src/main/resources/rulesets/findbugs/visitors.xml)
- [documentation for the rules](http://findbugs.sourceforge.net/bugDescriptions.html)