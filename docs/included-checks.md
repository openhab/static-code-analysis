# ESH Guidelines Covered

## A. Code Style
1. [The Java naming conventions should be used.](https://github.com/openhab/static-code-analysis/blob/master/src/main/resources/rulesets/checkstyle/rules.xml#L80) - `severity=info`
2. [Every Java file must have a license header. You can run mvn license:format on the root of the repo to automatically add missing headers.](https://github.com/openhab/static-code-analysis/blob/master/src/main/resources/rulesets/checkstyle/rules.xml#L34) - `severity=info`
3. [Every class, interface and enumeration should have JavaDoc describing its purpose and usage.](https://github.com/openhab/static-code-analysis/blob/master/src/main/resources/rulesets/checkstyle/rules.xml#L66) - `severity=warning`
4. [Every class, interface and enumeration must have an @author tag in its JavaDoc for every author that wrote a substantial part of the file.](https://github.com/openhab/static-code-analysis/blob/master/src/main/resources/rulesets/checkstyle/rules.xml#L97) - `severity=error`
5. Every constant, field and method with default, protected or public visibility should have JavaDoc (optional, but encouraged for private visibility as well). - `Not covered yet`
6. The code must be formatted:
    - java files must use spaces for formatting, rather than tabs - `severity=error`;
    - xml files must use tabs for formatting, rather than spaces - `Not covered yet`
    - json files must use tabs for formatting, rather than spaces - `Not covered yet`
7. Generics must be used where applicable. - `Not covered yet`
8. Code should not show any warnings. Warnings that cannot be circumvented should be suppressed by using the @SuppressWarnings annotation. - `Not covered yet`
9. For dependency injection, OSGi Declarative Services should be used. - `severity=warning`
10. Packages that contain classes that are not meant to be used by other bundles should have “internal” in their package name. - `Work in Progress`

## B. OSGi Bundles
1. Every bundle must contain a Maven pom.xml with a version and artifact name that is in sync with the manifest entry. The pom.xml must reference the correct parent pom (which is usually in the parent folder). - `severity=error`
2. Every bundle must contain an about.html file, providing license information. - `severity=error`
3. Every bundle must contain a build.properties file, which lists all resources that should end up in the binary under bin.includes. - `severity=warning`
4. The manifest must not contain any “Require-Bundle” entries. Instead, “Import-Package” must be used. - `severity=error`
5. [The manifest must not export any internal package.](https://github.com/openhab/static-code-analysis/blob/master/src/main/resources/rulesets/checkstyle/rules.xml#L40) - `severity=error`
6. The manifest must not have any version constraint on package imports, unless this is thoughtfully added. Note that Eclipse automatically adds these constraints based on the version in the target platform, which might be too high in many cases. - `severity=warning`
7. The manifest must include all services in the Service-Component entry. A good approach is to put OSGI-INF/*.xml in there. - `severity=error`
8. Every exported package of a bundle must be imported by the bundle itself again. - `severity=warning`

## C. Language Levels and Libraries
`Not covered yet`

## D. Runtime Behavior
`Not covered yet`

## E. Logging
1. As we are in a dynamic OSGi environment, loggers should be non-static, when ever possible and have the name logger. - `severity=warning`
2. Parametrized logging must be used (instead of string concatenation). - `severity=error`
3. Where ever unchecked exceptions are caught and logged, the exception should be added as a last parameter to the logging. For checked exceptions, this is normally not recommended, unless it can be considered an error situation and the stacktrace would hold additional important information for the analysis. - `Not covered yet`
4. Logging levels should focus on the system itself and describe its state. As every bundle is only one out of many, logging should be done very scarce. It should be up to the user to increase the logging level for specific bundles, packages or classes if necessary. This means in detail:
    - Most logging should be done in debug level. trace can be used for even more details, where necessary. - `Not covered yet`
    - Only few important things should be logged in info level, e.g. a newly started component or a user file that has been loaded. - `Not covered yet`
    - `warn` logging should only be used to inform the user that something seems to be wrong in his overall setup, but the system can nonetheless function as normal, while possibly ignoring some faulty configuration/situation. It can also be     used in situations, where a code section is reached, which is not expected by the implementation under normal              circumstances (while being able to automatically recover from it). - `Not covered yet`
    - `error` logging should only be used to inform the user that something is tremendously wrong in his setup, the system cannot function normally anymore, and there is a need for immediate action. It should also be used if some code fails      irrecoverably and the user should report it as a severe bug. - `Not covered yet`
5. For bindings, you should NOT log errors, if e.g. connections are dropped - this is considered to be an external problem and from a system perspective to be a normal and expected situation. The correct way to inform users about such events is to update the Thing status accordingly. Note that all events (including Thing status events) are anyhow already logged. - `Not covered yet`
6. Likewise, bundles that accept external requests (such as servlets) must not log errors or warnings if incoming requests are incorrect. Instead, appropriate error responses should be returned to the caller. - `Not covered yet`

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