# Static Code Analysis Tool

The Static Code Analysis Tools is a Maven plugin that executes the Maven plugins for FindBugs, Checkstyle and PMD and generates a merged .html report.
It is specifically designed for Eclipse SmartHome and openHAB code as it is tailored to respect their coding guidelines.

This project contains:

 - properties files for the PMD, Checkstyle and FindBugs Maven plugins configuration in the `src/main/resources/configuration` folder;
 - rule sets for the plugins in the `src/main/resources/rulesets` folder;
 - custom rules for PMD, CheckStyle and FindBugs and unit tests for the rules;
 - tool that merges the reports from the individual plugins in a summary report.

# Usage

Add the following profiles to your pom.xml:

```
  <profile>
    <id>check</id>
    <build>
      <pluginManagement>
        <plugins>
          <plugin>
            <groupId>org.openhab.tools</groupId>
            <artifactId>static-code-analysis</artifactId>
            <version>${sat.version}</version>
            <executions>
              <execution>
                <phase>verify</phase>
                <goals>
                  <goal>checkstyle</goal>
                  <goal>pmd</goal>
                  <goal>findbugs</goal>
                  <goal>report</goal>
                </goals>
              </execution>
            </executions>
            <configuration>
              <findbugsPlugins>
                <findbugsPlugin>
                   <groupId>jp.skypencil.findbugs.slf4j</groupId>
                   <artifactId>bug-pattern</artifactId>
                  <version>1.2.4</version>
                </findbugsPlugin>
              </findbugsPlugins>
            </configuration>
          </plugin>
        </plugins>
      </pluginManagement>
    </build>
  </profile>
  <profile>
    <id>check-bundles</id>
    <activation>
      <file>
        <exists>META-INF/MANIFEST.MF</exists>
      </file>
    </activation>
    <build>
      <plugins>
        <plugin>
            <groupId>org.openhab.tools</groupId>
            <artifactId>static-code-analysis</artifactId>
        </plugin>
      </plugins>
    </build>
  </profile>
```

 Execute `mvn clean install -P check` from the root of your project.

 Reports are generated for each module individually and can be found in the `target/code-analysis` directory. The merged report can be found in the root target directory.

 The build will fail if a problem with high priority is found by some of the Maven plugins for PMD, Checkstyle and FindBugs. Each of the plugins has its own way to prioritize the detected problems:

 - for PMD - the build will fail when a rule with Priority "1" is found;
 - for Checkstyle - a rule with severity="Error";
 - for Findbugs - any Matcher with Rank between 1 and 4.

## Maven plugin goals and parameters

**static-code-analysis:pmd**

Description:
    Executes the `maven-pmd-plugin` goal `pmd` with a ruleset file and configuration properties

Parameters:

| Name | Type| Description |
| ------ | ------| -------- |
| **pmdRuleset** | String | Relative path of the XML configuration to use. If not set the default ruleset file will be used |
| **maven.pmd.version** | String | The version of the maven-pmd-plugin that will be used (Default value is **3.7**)|
| **pmdPlugins** | Dependency [] | A list with artifacts that contain additional checks for PMD |

**static-code-analysis:checkstyle**

Description:
    Executes the `maven-checkstyle-plugin` goal `checkstyle` with a ruleset file and configuration properties

Parameters:

| Name | Type| Description |
| ------ | ------| -------- |
| **checkstyleRuleset** | String | Relative path of the XML configuration to use. If not set the default ruleset file will be used |
| **checkstyleFilter** | String | Relative path of the suppressions XML file to use. If not set the default filter file will be used |
| **maven.checkstyle.version** | String | The version of the maven-checkstyle-plugin that will be used (default value is **2.17**)|
| **checkstylePlugins** | Dependency [] | A list with artifacts that contain additional checks for Checkstyle |

**static-code-analysis:findbugs**

Description:
    Executes the `findbugs-maven-plugin` goal `findbugs` with a  ruleset file and configuration properties

Parameters:

| Name | Type| Description |
| ------ | ------| -------- |
| **findbugsRuleset** | String | Relative path to the XML that specifies the bug detectors which should be run. If not set the default file will be used|
| **findbugsInclude** | String | Relative path to the XML that specifies the bug instances that will be included in the report. If not set the default file will be used|
| **findbugsExclude** | String | Relative path to the XML that specifies the bug instances that will be excluded from the report. If not set the default file will be used|
| **findbugs.maven.version** | String | The version of the findbugs-maven-plugin that will be used (default value is **3.0.1**)|
| **findbugsPlugins** | Dependency [] | A list with artifacts that contain additional detectors/patterns for FindBugs |

**static-code-analysis:report**

Description:
    Transforms the results from FindBugs, Checkstyle and PMD into a single HTML Report with XSLT

Parameters:

| Name | Type| Description |
| ------ | ------| -------- |
| **report.targetDir** | String | The directory where the individual report will be generated (default value is **${project.build.directory}/code-analysis**) |
| **report.summary.targetDir** | String | The directory where the summary report, containing links to the individual reports will be generated (Default value is **${session.executionRootDirectory}/target**)|
| **report.fail.on.error** | Boolean | Describes of the build should fail if high priority error is found (Default value is **true**)|

## Customization

Different sets of checks can be executed on different types of projects.

The tool executes different checks on OSGi bundles and ESH Bindings. It uses default configuration files for FindBugs, Checkstyle and PMD that are stored in the `src/main/resources/configuration`.

If you want to use a custom set of rules you will have to set the configuration parameters for the individual MOJOs. An example configuration may look like this;

```
  <plugin>
    <groupId>org.openhab.tools</groupId>
    <artifactId>static-code-analysis</artifactId>
    <configuration>
      <checkstyleRuleset>build-tools/checkstyle/binding.xml</checkstyleRuleset>
      <checkstyleFilter>build-tools/checkstyle/suppressions.xml</checkstyleFilter>
      <pmdRuleset>build-tools/pmd/binding.xml</pmdRuleset>
      <findbugsInclude>build-tools/findbugs/binding.xml</findbugsInclude>
      <findbugsExclude>build-tools/findbugs/exclude.xml</findbugsExclude>
      <findbugsRuleset>build-tools/findbugs/visitors.xml</findbugsRuleset>
    </configuration>
  </plugin>
```

Information about the syntax of the configuration files (except the `visitors.xml`) can be found on the web pages of the individual plugins.

The `visitors.xml` contains a list with FindBugs visitors (bug detectors) and has the following syntax:

```
<?xml version="1.0" encoding="UTF-8"?>
<visitors>
  <visitor>AtomicityProblem</visitor>
  ...
<visitors/>
```

### Individual plugin customization

Each of the Maven plugins that are used (for FindBugs, Checkstyle and PMD) are configured by setting user properties that are located in the `src/main/resources/configuration` directory.

You can refer to the following links for more configuration options for the specific Maven plugins:

- https://maven.apache.org/plugins/maven-pmd-plugin/check-mojo.html;
- https://maven.apache.org/plugins-archives/maven-checkstyle-plugin-2.16/checkstyle-mojo.html;
- http://gleclaire.github.io/findbugs-maven-plugin/check-mojo.html.


## Reuse Checks

PMD, Checkstyle and FindBugs come with a set of custom rules that can be used directly in a rule set.

Helpful resources with lists of the available checks and information how to use them:

- for PMD - https://pmd.github.io/pmd-5.4.0/pmd-java/rules/index.html;
- for Checkstyle - http://checkstyle.sourceforge.net/checks.html;
- for FindBugs - Keep in mind that the process for adding a check in FindBugs contains two steps:
   - First you should open the link with [BugDescriptors](http://findbugs.sourceforge.net/bugDescriptions.html), choose the bug that you want to detect and create a Match in `src/main/resources/rulesets/findbugs/YOUR_RULESET`;
   - Next you should find the Detector that finds the Bug that you have selected above (you can use [this list](https://github.com/findbugsproject/findbugs/blob/d1e60f8dbeda0a454f2d497ef8dcb878fa8e3852/findbugs/etc/findbugs.xml)) and add the Detector in the `src/main/resources/configuration/findbugs.properties` under the property `visitors`.

## Write Custom Checks

All of the used static code analysis tools have Java API for writing custom checks.

Checkstyle API is easy to use and to implement checks for different file extensions and languages. Examples are included in the project.

PMD extends this by giving the possibility to define a rule with the XPath syntax. PMD offers even more - a Rule Designer that speeds up the process of developing a new rule! See http://nullpointer.debashish.com/pmd-xpath-custom-rules.

PMD rules with XPatch expressions are defined directly in the rule set (`src/main/resources/rulesets/pmd/xpath`) folder.

Helpful links when writing a custom check for the first time may be:

- for PMD - http://pmd.sourceforge.net/pmd-4.3.0/howtowritearule.html;
- for Checkstyle - http://checkstyle.sourceforge.net/writingchecks.html;
- for FindBugs - https://www.ibm.com/developerworks/library/j-findbug2/.

## Add Tests For The New Checks

You can easily test your custom rules for PMD and Checkstyle.

In order to add a new test for PMD you have to do two things:
- Create a test class in the `src/test/java` folder that extends `SimpleAggregatorTst` and overrides the `setUp()` method;
- Add a .xml file in the `src/test/resources` folder that contains the code to be tested.

Adding a test for Checkstyle is even easier - extend the `BaseCheckTestSupport`.

For more information: https://pmd.github.io/pmd-5.4.1/customizing/rule-guidelines.html.

## Known Problems

- Flooded console output when running Checkstyle in debug mode in Maven  (- X ) - https://github.com/checkstyle/checkstyle/issues/3184;

# ESH Guidelines Covered

## A. Code Style
1. [The Java naming conventions should be used.](https://github.com/openhab/static-code-analysis/blob/master/src/main/resources/rulesets/checkstyle/rules.xml#L80) - `severity=info`
2. [Every Java file must have a license header. You can run mvn license:format on the root of the repo to automatically add missing headers.](https://github.com/openhab/static-code-analysis/blob/master/src/main/resources/rulesets/checkstyle/rules.xml#L34) - `severity=info`
3. [Every class, interface and enumeration should have JavaDoc describing its purpose and usage.](https://github.com/openhab/static-code-analysis/blob/master/src/main/resources/rulesets/checkstyle/rules.xml#L66) - `severity=warning`
4. Every class, interface and enumeration must have an @author tag in its JavaDoc for every author that wrote a substantial part of the file. - `Work in Progress`
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
1. Every bundle must contain a Maven pom.xml with a version and artifact name that is in sync with the manifest entry. The pom.xml must reference the correct parent pom (which is usually in the parent folder). - `Work in Progress`
2. Every bundle must contain an about.html file, providing license information. - `Work in Progress`
3. Every bundle must contain a build.properties file, which lists all resources that should end up in the binary under bin.includes. - `Work in Progress`
4. The manifest must not contain any “Require-Bundle” entries. Instead, “Import-Package” must be used. - [Opened PR](https://github.com/openhab/static-code-analysis/pull/19) - `severity=error`
5. [The manifest must not export any internal package.](https://github.com/openhab/static-code-analysis/blob/master/src/main/resources/rulesets/checkstyle/rules.xml#L40) - `severity=error`
6. The manifest must not have any version constraint on package imports, unless this is thoughtfully added. Note that Eclipse automatically adds these constraints based on the version in the target platform, which might be too high in many cases. - `Work in Progress`
7. The manifest must include all services in the Service-Component entry. A good approach is to put OSGI-INF/*.xml in there. - `Work in Progress`
8. Every exported package of a bundle must be imported by the bundle itself again. - `Work in Progress`

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

## 3rd Party

- The example checks provided in the `static-code-analysis-config` (`MethodLimitCheck`, `CustomClassNameLengthDetector`, `WhileLoopsMustUseBracesRule`) are based on tutorials how to use the API of Checkstyle, FindBugs and PMD. For more info, see javadoc;
- The tool that merges the individual reports is based completely on source files from the https://github.com/MarkusSprunck/static-code-analysis-report that are distributed under a custom license. More information can be found in the [LICENSE](LICENSE) file.
